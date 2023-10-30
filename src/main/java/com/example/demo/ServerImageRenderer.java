package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import ij.gui.Overlay;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.renderer.ImageRenderer;

public class ServerImageRenderer implements ImageRenderer {
    private byte[] outputImage = null;

    @Override
    public void render(Image image, String arg1, LUT arg2, boolean arg3, boolean arg4, Overlay arg5) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image.getImagePlus().getBufferedImage(), "png", stream);
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
