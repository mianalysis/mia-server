package io.github.mianalysis.miaserver.modules;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.CompositeConverter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.miaserver.ServerCategories;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

/**
 * Created by Stephen on 15/05/2017.
 */

/**
 * Load image into MIA workspace. This module can be configured to import images
 * from a variety of locations (selected using the "Import mode" control).
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class SimpleImageLoader extends Module {

    /**
    * 
    */
    public static final String LOADER_SEPARATOR = "Core image loading controls";

    /**
     * Name assigned to the image.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
     * Controls where the image will be loaded from:<br>
     * <ul>
     * <li>"Current file" (default option) will import the current root-file for the
     * workspace (this is the file specified in the "Input control" module).</li>
     * <li>"From ImageJ" will load the active image fromm ImageJ.</li>
     * <li>"Image sequence (alphabetical)" will load a series of images matching a
     * specified name format in alphabetical order. The format of the names to be
     * loaded is specified by the "Sequence root name" parameter.</li>
     * <li>"Image sequence (zero-based)" will load a series of images with numbered
     * elements. The format of the names to be loaded is specified by the "Sequence
     * root name" parameter.</li>
     * <li>"Matching format" will load the image matching a filename based on the
     * root-file for the workspace and a series of rules.</li>
     * <li>"Specific file" will load the image at the location specified by "File
     * path".</li>
     * </ul>
     */
    public static final String IMPORT_MODE = "Import mode";

    /**
     * Format for a generic filename. Plain text can be mixed with global variables
     * or metadata values currently stored in the workspace. Global variables are
     * specified using the "V{name}" notation, where "name" is the name of the
     * variable to insert. Similarly, metadata values are specified with the
     * "M{name}" notation.
     */
    public static final String GENERIC_FORMAT = "Generic format";

    /**
     * List of the currently-available metadata values for this workspace. These can
     * be used when compiling a generic filename.
     */
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    /**
     * Option to include the current series number when compiling filenames. This
     * may be necessary when working with multi-series files, as there will be
     * multiple analyses completed for the same root file.
     */
    public static final String INCLUDE_SERIES_NUMBER = "Include series number";

    /**
     * Path to file to be loaded.
     */
    public static final String FILE_PATH = "File path";

    /**
    * 
    */
    public static final String CALIBRATION_SEPARATOR = "Spatial and intensity calibration";

    /**
     * Option to use the automatically-applied spatial calibration or manually
     * specify these values.
     */
    public static final String SET_SPATIAL_CAL = "Set manual spatial calibration";
    public static final String XY_CAL = "XY calibration (dist/px)";
    public static final String Z_CAL = "Z calibration (dist/px)";

    /**
     * Option to use the automatically-applied temporal calibration or manually
     * specify this value.
     */
    public static final String SET_TEMPORAL_CAL = "Set manual temporal calibration";
    public static final String FRAME_INTERVAL = "Frame interval (time/frame)";

    public SimpleImageLoader(Modules modules) {
        super("Load image (simple)", modules);
    }

    public interface ImportModes {
        String CURRENT_FILE = "Current file";
        String GENERIC_FORMAT = "Generic format";
        String SPECIFIC_FILE = "Specific file";

        String[] ALL = new String[] { CURRENT_FILE, GENERIC_FORMAT, SPECIFIC_FILE };

    }

    void setDummySpatialCalibration(ImagePlus ipl) {
        Calibration cal = ipl.getCalibration();

        cal.pixelWidth = Double.NaN;
        cal.pixelHeight = Double.NaN;
        cal.pixelDepth = Double.NaN;

    }

    void setDummyTemporalCalibration(ImagePlus ipl) {
        Calibration cal = ipl.getCalibration();

        cal.frameInterval = Double.NaN;
        cal.fps = Double.NaN;

    }

    void parseImageJSpatialCalibration(ImagePlus ipl, String path) {
        Calibration cal = ipl.getCalibration();

        // Checking if spatial units match those selected in InputControl
        String currUnit = cal.getUnit().toLowerCase();
        Unit<Length> currUnitOME = SpatialUnit.getOMEUnit(currUnit);
        Unit<Length> targetUnitOME = SpatialUnit.getOMEUnit();

        if (currUnitOME == null) {
            if (path == null) {
                MIA.log.writeWarning(
                        "Can't apply spatial units for image loaded from ImageJ.  Spatially calibrated values will be unavailable.");
            } else {
                MIA.log.writeWarning("Can't apply spatial units for file \"" + new File(path).getName()
                        + "\".  Spatially calibrated values will be unavailable.");
            }
            setDummySpatialCalibration(ipl);

        } else if (currUnitOME != targetUnitOME) {
            cal.pixelWidth = currUnitOME.convertValue(cal.pixelWidth, targetUnitOME);
            cal.pixelHeight = currUnitOME.convertValue(cal.pixelHeight, targetUnitOME);
            cal.pixelDepth = currUnitOME.convertValue(cal.pixelDepth, targetUnitOME);
            cal.setUnit(targetUnitOME.getSymbol());
        }
    }

    void parseImageJTemporalCalibration(ImagePlus ipl, String path) {
        Calibration cal = ipl.getCalibration();

        // Checking if spatial units match those selected in InputControl
        String currUnit = cal.getTimeUnit().toLowerCase();
        Unit<Time> currUnitOME = TemporalUnit.getOMEUnit(currUnit);
        Unit<Time> targetUnitOME = TemporalUnit.getOMEUnit();

        if (currUnitOME == null && ipl.getNFrames() != 1) {
            if (path == null) {
                MIA.log.writeWarning(
                        "Can't apply temporal units for image loaded from ImageJ.  Temporally calibrated values will be unavailable.");
            } else {
                MIA.log.writeWarning("Can't apply temporal units for file \"" + new File(path).getName()
                        + "\".  Temporally calibrated values will be unavailable.");
            }
            setDummyTemporalCalibration(ipl);

        } else if (currUnitOME != targetUnitOME) {
            cal.frameInterval = currUnitOME.convertValue(cal.frameInterval, targetUnitOME);
            cal.fps = 1 / targetUnitOME.convertValue(cal.frameInterval, UNITS.SECOND);
            cal.setTimeUnit(targetUnitOME.getSymbol());
        }
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
        return "Load image into MIA workspace.  This module can be configured to import images from a variety of locations (selected using the \"Import mode\" control).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String importMode = parameters.getValue(IMPORT_MODE, workspace);
        String filePath = parameters.getValue(FILE_PATH, workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
        boolean setSpatialCalibration = parameters.getValue(SET_SPATIAL_CAL, workspace);
        boolean setTemporalCalibration = parameters.getValue(SET_TEMPORAL_CAL, workspace);
        double frameInterval = parameters.getValue(FRAME_INTERVAL, workspace);

        ImagePlus ipl = null;
        try {
            switch (importMode) {
                case ImportModes.CURRENT_FILE:
                    File file = workspace.getMetadata().getFile();
                    if (file == null) {
                        MIA.log.writeWarning("No input file/folder selected.");
                        return Status.FAIL;
                    }

                    if (!file.exists()) {
                        MIA.log.writeWarning("File \"" + file.getAbsolutePath() + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    ipl = IJ.openImage(file.getAbsolutePath());
                    if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                        parseImageJSpatialCalibration(ipl, file.getAbsolutePath());
                    if (!setTemporalCalibration)
                        parseImageJTemporalCalibration(ipl, file.getAbsolutePath());

                    break;

                case ImportModes.GENERIC_FORMAT:
                    Metadata metadata = (Metadata) workspace.getMetadata().clone();
                    // path = FileTools.getGenericName(metadata, genericFormat);
                    String absolutePath = metadata.insertMetadataValues(genericFormat);
                    String filepath = FilenameUtils.getFullPath(absolutePath);
                    String filename = FilenameUtils.getName(absolutePath);

                    // If name includes "*" get first instance of wildcard
                    if (filename.contains("*"))
                        filename = new File(filepath).list(new WildcardFileFilter(filename))[0];

                    String path = filepath + filename;

                    file = new File(path);

                    if (!file.exists()) {
                        MIA.log.writeWarning("File \"" + file.getAbsolutePath() + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    ipl = IJ.openImage(file.getAbsolutePath());
                    if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                        parseImageJSpatialCalibration(ipl, file.getAbsolutePath());
                    if (!setTemporalCalibration)
                        parseImageJTemporalCalibration(ipl, file.getAbsolutePath());

                    break;

                case ImportModes.SPECIFIC_FILE:
                    if (!(new File(filePath)).exists()) {
                        MIA.log.writeWarning("File \"" + filePath + "\" not found.  Skipping file.");
                        return Status.FAIL;
                    }

                    ipl = IJ.openImage(filePath);
                    if (!setSpatialCalibration && ipl.getCalibration().getUnit().equals("pixel"))
                        parseImageJSpatialCalibration(ipl, filePath);
                    if (!setTemporalCalibration)
                        parseImageJTemporalCalibration(ipl, filePath);
                    break;

            }
        } catch (Exception e) {
            MIA.log.writeWarning(e);
            return Status.FAIL;
        }

        if (ipl == null)
            return Status.FAIL;

        // If necessary, setting the spatial calibration
        if (setSpatialCalibration) {
            double xyCal = parameters.getValue(XY_CAL, workspace);
            double zCal = parameters.getValue(Z_CAL, workspace);

            writeStatus("Setting spatial calibration (XY = " + xyCal + ", Z = " + zCal +
                    ")");
            Calibration calibration = ipl.getCalibration();

            calibration.pixelHeight = xyCal;
            calibration.pixelWidth = xyCal;
            calibration.pixelDepth = zCal;
            calibration.setUnit(SpatialUnit.getOMEUnit().getSymbol());

            ipl.setCalibration(calibration);
            ipl.updateChannelAndDraw();

        }

        // If necessary, setting the spatial calibration
        if (setTemporalCalibration) {
            writeStatus("Setting temporal calibration (frame interval = " + frameInterval
                    + ")");
            Calibration calibration = ipl.getCalibration();

            calibration.frameInterval = frameInterval;
            calibration.fps = 1 / TemporalUnit.getOMEUnit().convertValue(frameInterval,
                    UNITS.SECOND);
            calibration.setTimeUnit(TemporalUnit.getOMEUnit().getSymbol());

            ipl.setCalibration(calibration);
            ipl.updateChannelAndDraw();

        }

        // Converting RGB to 3-channel
        if (ipl.getBitDepth() == 24)
            ipl = CompositeConverter.makeComposite(ipl);

        // Adding image to workspace
        writeStatus("Adding image (" + outputImageName + ") to workspace");
        Image outputImage = ImageFactory.createImage(outputImageName, ipl);
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(LOADER_SEPARATOR, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(IMPORT_MODE, this, ImportModes.CURRENT_FILE, ImportModes.ALL));
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, ParameterState.MESSAGE, 130));
        parameters.add(new BooleanP(INCLUDE_SERIES_NUMBER, this, true));
        parameters.add(new FilePathP(FILE_PATH, this));

        parameters.add(new SeparatorP(CALIBRATION_SEPARATOR, this));
        parameters.add(new BooleanP(SET_SPATIAL_CAL, this, false));
        parameters.add(new DoubleP(XY_CAL, this, 1d));
        parameters.add(new DoubleP(Z_CAL, this, 1d));
        parameters.add(new BooleanP(SET_TEMPORAL_CAL, this, false));
        parameters.add(new DoubleP(FRAME_INTERVAL, this, 1d));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(LOADER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(IMPORT_MODE));
        switch ((String) parameters.getValue(IMPORT_MODE, workspace)) {
            case ImportModes.CURRENT_FILE:
                break;

            case ImportModes.GENERIC_FORMAT:
                returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;

            case ImportModes.SPECIFIC_FILE:
                returnedParameters.add(parameters.getParameter(FILE_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALIBRATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SET_SPATIAL_CAL));
        if ((boolean) parameters.getValue(SET_SPATIAL_CAL, workspace)) {
            returnedParameters.add(parameters.getParameter(XY_CAL));
            returnedParameters.add(parameters.getParameter(Z_CAL));
        }

        returnedParameters.add(parameters.getParameter(SET_TEMPORAL_CAL));
        if ((boolean) parameters.getValue(SET_TEMPORAL_CAL, workspace))
            returnedParameters.add(parameters.getParameter(FRAME_INTERVAL));

        return returnedParameters;

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
        boolean valid = true;

        // If using the generic metadata extractor, check the values are available
        if (parameters.getValue(IMPORT_MODE, null).equals(ImportModes.GENERIC_FORMAT)) {
            MetadataRefs metadataRefs = modules.getMetadataRefs(this);
            String genericFormat = parameters.getValue(GENERIC_FORMAT, null);
            valid = metadataRefs.hasRef(genericFormat);
            parameters.getParameter(GENERIC_FORMAT).setValid(valid);

        }

        return valid;

    }

    void addParameterDescriptions() {
        parameters.get(OUTPUT_IMAGE).setDescription("Name assigned to the image.");

        parameters.get(IMPORT_MODE).setDescription("Controls where the image will be loaded from:<br><ul>"

                + "<li>\"" + ImportModes.CURRENT_FILE
                + "\" (default option) will import the current root-file for the workspace (this is the file specified in the \""
                + new InputControl(null).getName() + "\" module).</li>"

                + "<li>\"" + ImportModes.GENERIC_FORMAT
                + "\" will generate a name from metadata values stored in the current workspace.</li>"

                + "<li>\"" + ImportModes.SPECIFIC_FILE + "\" will load the image at the location specified by \""
                + FILE_PATH + "\".</li></ul>");

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

        parameters.get(INCLUDE_SERIES_NUMBER).setDescription(
                "Option to include the current series number when compiling filenames.  This may be necessary when working with multi-series files, as there will be multiple analyses completed for the same root file.");

        parameters.get(FILE_PATH).setDescription("Path to file to be loaded.");

        parameters.get(SET_SPATIAL_CAL).setDescription(
                "Option to use the automatically-applied spatial calibration or manually specify these values.");

        parameters.get(XY_CAL).setDescription(
                "Distance per pixel in the XY plane.  Units for this are specified in the main \"Input control\" module.");

        parameters.get(Z_CAL).setDescription(
                "Distance per slice (Z-axis).  Units for this are specified in the main \"Input control\" module.");

        parameters.get(SET_TEMPORAL_CAL).setDescription(
                "Option to use the automatically-applied temporal calibration or manually specify this value.");

        parameters.get(FRAME_INTERVAL).setDescription(
                "Time duration between the start of consecutive frames.  Units for this are specified in the main \"Input control\" module.");

    }
}

// when dataisgood, Gemma = given food
// i = 42^1000000000000000000000000000000000000000000 [dontend]
