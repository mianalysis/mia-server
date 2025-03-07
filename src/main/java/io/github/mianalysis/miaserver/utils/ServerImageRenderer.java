package io.github.mianalysis.miaserver.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.renderer.ImageRenderer;

public class ServerImageRenderer implements ImageRenderer {
    private byte[] outputImage = null;

    @Override
    public void render(Image image, String title, LUT lut, boolean normalise, String displayMode, Overlay overlay) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImagePlus ipl = image.getImagePlus();

            if (displayMode.equals("Composite"))
                ipl.setDisplayMode(IJ.COMPOSITE);
            else
                ipl.setDisplayMode(IJ.COLOR);

            if (ipl.getOverlay() != null)
                ipl.flattenStack();

            BufferedImage bufferedImage = ipl.getBufferedImage();
            ImageIO.write(bufferedImage, "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        outputImage = stream.toByteArray();

    }

    public void clearLastOutput() {
        outputImage = null;
    }

    public byte[] getLastOutputImage() {
        return outputImage;
    }
}
