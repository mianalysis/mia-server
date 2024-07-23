package io.github.mianalysis.miaserver.modules;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.miaserver.results.Result;
import io.github.mianalysis.miaserver.utils.ProcessResult;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;


@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DisplayImage extends Module implements Result {

    public static final String IMAGE = "Image to display";

    public static final String TYPE = "Type";

    public static final String SHOW_CHANNEL_CONTROLS = "Show channel controls";

    public interface Types {
        String COLOUR = "Colour";
        String COMPOSITE = "Composite";

        String[] ALL = new String[] { COLOUR, COMPOSITE };
    }

    public static void main(String[] args) {
        // The following must be called before initialising ImageJ
        LegacyInjector.preinit();

        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(DisplayImage.class);

    }

    public static JSONObject getImageJSON(Image image, boolean showImageControls) throws InterruptedException {
        JSONObject imageJSON = new JSONObject();
        imageJSON.put("name",image.getName());
        imageJSON.put("showcontrols", showImageControls);

        JSONArray channelArray = new JSONArray();

        ImagePlus ipl = image.getImagePlus();

        ipl.setDisplayMode(IJ.COMPOSITE);

        LUT[] luts = ipl.getLuts();

        ImagePlus[] channels = ChannelSplitter.split(ipl);

        for (int c = 0; c < channels.length; c++) {
            JSONObject channelObject = new JSONObject();

            // Adding pixel information
            // ipl.setPosition((c + 1), z, t);
            ImageProcessor ipr = channels[c].getProcessor();
            channelObject.put("pixels", getChannelString(ipr));

            // Adding LUT information
            LUT lut = luts[c];

            byte[] reds = new byte[256];
            lut.getReds(reds);
            channelObject.put("red", reds[255] & 0xFF);

            byte[] greens = new byte[256];
            lut.getGreens(greens);
            channelObject.put("green", greens[255] & 0xFF);

            byte[] blues = new byte[256];
            lut.getBlues(blues);
            channelObject.put("blue", blues[255] & 0xFF);

            channelObject.put("strength",1);
            channelObject.put("index",c);

            channelArray.put(channelObject);

        }

        imageJSON.put("channels",channelArray);

        if (ipl.getOverlay() != null) {
            ImagePlus blankIpl = IJ.createImage("Overlay", ipl.getWidth(), ipl.getHeight(), 1, 8);
            blankIpl.setOverlay(ipl.getOverlay().duplicate());
            ImagePlus overlayIpl = blankIpl.flatten();

            JSONObject channelObject = new JSONObject();

            // Adding pixel information
            ImageProcessor ipr = overlayIpl.getProcessor();
            channelObject.put("pixels", getChannelString(ipr));

            // Adding LUT information
            LUT lut = ipl.getLuts()[0];

            byte[] reds = new byte[256];
            lut.getReds(reds);
            channelObject.put("red", reds[255] & 0xFF);

            byte[] greens = new byte[256];
            lut.getGreens(greens);
            channelObject.put("green", greens[255] & 0xFF);

            byte[] blues = new byte[256];
            lut.getBlues(blues);
            channelObject.put("blue", blues[255] & 0xFF);

            channelObject.put("strength",1);
            channelObject.put("index",channels.length);

            channelArray.put(channelObject);

        }

        return imageJSON;

    }

    public static String getChannelString(ImageProcessor ipr) throws InterruptedException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // ipr.setLut(LUT.createLutFromColor(Color.WHITE));
            ImageIO.write(ipr.getBufferedImage(), "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(stream.toByteArray());

    }

    public DisplayImage(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Display image", modules);
    }

    @Override
    public Category getCategory() {
        return ServerCategories.SCHOOLS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        String imageName = parameters.getValue(IMAGE, workspace);
        boolean showImageControls = parameters.getValue(SHOW_CHANNEL_CONTROLS, workspace);

        Image image = workspace.getImage(imageName);

        try {
            JSONObject imageJSON = getImageJSON(image, showImageControls);
            ProcessResult.getInstance().put("image", imageJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(IMAGE, this));
        parameters.add(new BooleanP(SHOW_CHANNEL_CONTROLS, this, false));
    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public JSONObject getJSON() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getJSON'");
    }
}