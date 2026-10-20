package io.github.mianalysis.miaserver.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import ij.plugin.RGBStackConverter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetLookupTable;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.miaserver.ServerCategories;
import io.github.mianalysis.miaserver.utils.ProcessResult;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DisplaySimpleImage extends Module {

    public static final String ADD_IMAGE = "Add image";

    public static final String IMAGE_TO_DISPLAY = "Image name";

    public static final String TYPE = "Type";

    public static final String SHOW_PROBE_CONTROL = "Show probe control";

    public static final String SHOW_SELECT_CONTROL = "Show select control";

    public static final String SHOW_ZOOM_CONTROL = "Show zoom control";

    public static final String DEFAULT_CONTROl = "Default control";

    public static final String ABOUT_TEXT = "About text";

    public interface Types {
        String COLOUR = "Colour";
        String COMPOSITE = "Composite";

        String[] ALL = new String[] { COLOUR, COMPOSITE };
    }

    public interface DefaultControls {
        String MOVE = "Move";
        String PROBE = "Probe";
        String SELECT = "Select";

        String[] ALL = new String[] { MOVE, PROBE, SELECT };

    }

    public JSONObject getCoreImageJSON(int hash, Workspace workspace) {
        boolean showProbeControl = parameters.getValue(SHOW_PROBE_CONTROL, workspace);
        boolean showSelectControl = parameters.getValue(SHOW_SELECT_CONTROL, workspace);
        boolean showZoomControl = parameters.getValue(SHOW_ZOOM_CONTROL, workspace);
        String defaultControl = parameters.getValue(DEFAULT_CONTROl, workspace);
        String aboutText = parameters.getValue(ABOUT_TEXT, workspace);

        JSONObject imageJSON = new JSONObject();
        imageJSON.put("hashcode", hash);
        imageJSON.put("showprobecontrol", showProbeControl);
        imageJSON.put("showselectcontrol", showSelectControl);
        imageJSON.put("showzoomcontrol", showZoomControl);
        imageJSON.put("defaultcontrol", defaultControl);
        imageJSON.put("aboutText", aboutText);

        return imageJSON;

    }

    public static int getImageHash(Image image) {
        int hash = 0;

        ImagePlus ipl = image.getImagePlus();

        for (int c = 0; c < ipl.getNChannels(); c++) {
            ipl.setC(c + 1);

            ImageProcessor ipr = ipl.getChannelProcessor();

            for (int x = 0; x < ipl.getWidth(); x++)
                for (int y = 0; y < ipl.getHeight(); y++)
                    hash = hash * 31 + ipr.get(x, y);

        }

        // Adding in LUT info
        for (LUT lut : ipl.getLuts()) {
            hash = hash * 31 + lut.hashCode();

            for (int i = 0; i < 256; i++)
                hash = hash * 31 + lut.getRGB(i) * i;

        }

        return hash;

    }

    public DisplaySimpleImage(Modules modules) {
        super("Display simple image", modules);
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
        LinkedHashMap<Integer, Parameters> imageParameters = ((ParameterGroup) parameters.getParameter(ADD_IMAGE))
                .getCollections(true);

        // int hash = 31 * getImageHash(image);
        int hash = 1;
        ProcessResult processResult = ProcessResult.getInstance();
        JSONObject imageJSON = getCoreImageJSON(hash, workspace);
        processResult.image = imageJSON;

        // Checking if this hash is already present. If it is, don't bother updating
        // pixel information
        // if (workspace.getMetadata().containsKey("ImageHash")) {
        // if (workspace.getMetadata().get("ImageHash").equals(hash)) {
        // imageJSON.put("channels", new JSONObject());

        // ProcessResult.getInstance().put("image", imageJSON);

        // if (showOutput)
        // image.show();

        // return Status.PASS;

        // }
        // }

        // Otherwise, proceed as usual
        // try {
        // ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // try {
        // // ipr.setLut(LUT.createLutFromColor(Color.WHITE));
        // ImageIO.write(image.getImagePlus().getProcessor().getBufferedImage(), "png",
        // stream);
        // } catch (IOException e) {
        // imageJSON.put("Error", e.getMessage());
        // throw new RuntimeException(e);
        // }

        ImagePlus[] ipls = new ImagePlus[imageParameters.size()];
        int i = 0;
        int nChannels = 0;
        ArrayList<LUT> luts = new ArrayList<>();
        for (Parameters currImageParameters : imageParameters.values()) {
            ImagePlus currIpl = workspace.getImage(currImageParameters.getValue(IMAGE_TO_DISPLAY, workspace))
                    .getImagePlus().duplicate();
            for (LUT lut : currIpl.getLuts())
                luts.add(lut);
            nChannels += currIpl.getNChannels();
            ipls[i] = currIpl;
            i++;
        }
        
        ImagePlus ipl = Concatenator.run(ipls);
        
        if (ipl.getStack().size() > 1)
            ipl = HyperStackConverter.toHyperStack(ipl, nChannels, 1, 1);

        if (!ipl.isComposite())
            ipl = new CompositeImage(ipl, CompositeImage.COLOR);
        
        for (int c=0;c<ipl.getNChannels();c++)
            ((CompositeImage) ipl).setChannelLut(luts.get(c), c+1);

        // imageJSON.put("pixels",
        // Base64.getEncoder().encodeToString(stream.toByteArray()));
        RGBStackConverter.convertToRGB(ipl);
        ColorProcessor ipr = (ColorProcessor) ipl.getProcessor();
        int imSize = ipr.getWidth() * ipr.getHeight();
        byte[] reds = new byte[imSize];
        byte[] greens = new byte[imSize];
        byte[] blues = new byte[imSize];
        ipr.getRGB(reds, greens, blues);
        // imageJSON.put("reds", reds);
        // imageJSON.put("greens", greens);
        // imageJSON.put("blues", blues);
        processResult.reds = reds;
        processResult.greens = greens;
        processResult.blues = blues;

        workspace.getMetadata().put("ImageHash", hash);
        // } catch (Exception e) {
        // imageJSON.put("Error", e.getMessage());
        // e.printStackTrace();
        // }

        // ProcessResult.getInstance().put("image", imageJSON);

        if (showOutput)
            ipl.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        Parameters templateParameters = new Parameters();
        templateParameters.add(new InputImageP(IMAGE_TO_DISPLAY, this));

        parameters.add(new ParameterGroup(ADD_IMAGE, this, templateParameters, 1));
        parameters.add(new BooleanP(SHOW_PROBE_CONTROL, this, false));
        parameters.add(new BooleanP(SHOW_SELECT_CONTROL, this, false));
        parameters.add(new BooleanP(SHOW_ZOOM_CONTROL, this, false));
        parameters.add(new ChoiceP(DEFAULT_CONTROl, this, DefaultControls.MOVE, DefaultControls.ALL));
        parameters.add(new TextAreaP(ABOUT_TEXT, this, true));

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
}