// package io.github.mianalysis.miaserver.modules;

// import java.awt.Polygon;
// import java.io.IOException;
// import java.net.URISyntaxException;
// import java.util.ArrayList;
// import java.util.List;

// import org.apache.commons.compress.archivers.ArchiveException;
// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ai.nets.samj.install.EfficientSamEnvManager;
// import ai.nets.samj.install.SamEnvManagerAbstract;
// import ai.nets.samj.models.AbstractSamJ;
// import ai.nets.samj.models.EfficientSamJ;
// import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
// import io.github.mianalysis.mia.MIA;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Obj;
// import io.github.mianalysis.mia.object.Objs;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
// import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.InputObjectsP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;
// import io.github.mianalysis.miaserver.ServerCategories;

// /**
//  * Created by Stephen Cross on 31/10/2024.
//  */
// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class ApplySegmentAnything extends Module {

//     /**
//     * 
//     */
//     public static final String INPUT_SEPARATOR = "Image input/object output";

//     /**
//      * 
//      */
//     public static final String INPUT_IMAGE = "Input image";

//     /**
//      * 
//      */
//     public static final String INPUT_OBJECTS = "Input objects (centroids)";

//     /**
//      * 
//      */
//     public static final String OUTPUT_OBJECTS = "Output objects";

//     protected static AbstractSamJ samJ = null;
//     protected static String currImageName = "";

//     public ApplySegmentAnything(Modules modules) {
//         super("Apply Segment Anything", modules);
//         if (samJ == null)
//             samJ = initialiseSAMJ();
//     }

//     @Override
//     public Category getCategory() {
//         return ServerCategories.SCHOOLS;
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.0.0";
//     }

//     @Override
//     public String getDescription() {
//         return "";

//     }

//     public AbstractSamJ initialiseSAMJ() {
//         AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
//         AbstractSamJ.MAX_ENCODED_SIDE = 3000;

//         AbstractSamJ loadedSamJ = null;
//         try {
//             String environmentPath = SamEnvManagerAbstract.DEFAULT_DIR;
//             SamEnvManagerAbstract manager = EfficientSamEnvManager.create(environmentPath);

//             if (!manager.checkEverythingInstalled())
//                 manager.installEverything();

//             loadedSamJ = EfficientSamJ.initializeSam(manager);

//         } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
//                 | MambaInstallException e) {
//             e.printStackTrace();
//         }

//         return loadedSamJ;

//     }

//     public List<Polygon> getPolygonsFromObjectCentroid(Obj inputObject) {
//         ArrayList<int[]> pts = new ArrayList<>();
//         pts.add(new int[] { (int) Math.round(inputObject.getXMean(true)),
//                 (int) Math.round(inputObject.getYMean(true)) });

//         try {
//             return samJ.processPoints(pts);
//         } catch (IOException | InterruptedException | RuntimeException e) {
//             MIA.log.writeError(e);
//         }

//         return null;

//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting parameters
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
//         String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

//         Image inputImage = workspace.getImages().get(inputImageName);
//         Objs inputObjects = workspace.getObjects(inputObjectsName);

//         if (!inputImageName.equals(currImageName)) {
//             MIA.log.writeDebug("Loading image into Segment Anything");
//             try {
//                 samJ.setImage(inputImage.getImgPlus());
//                 currImageName = inputImageName;
//             } catch (IOException | RuntimeException | InterruptedException e) {
//                 MIA.log.writeError(e);
//             }
//         }

//         // Creating output objects
//         Objs outputObjects = new Objs(outputObjectsName, inputImage.getImagePlus());

//         for (Obj inputObject : inputObjects.values()) {
//             List<Polygon> polygons = getPolygonsFromObjectCentroid(inputObject);
//             if (polygons == null)
//                 return Status.PASS;

//             Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
//             for (Polygon polygon : polygons)
//                 try {
//                     outputObject.addPointsFromPolygon(polygon, 0);
//                 } catch (PointOutOfRangeException e) {
//                 }
//         }

//         workspace.addObjects(outputObjects);

//         if (showOutput)
//             outputObjects.convertToImageIDColours().show(false);

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
//         parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         return parameters;
//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
