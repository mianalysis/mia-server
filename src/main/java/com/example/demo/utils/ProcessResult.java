package com.example.demo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.object.image.Image;

public class ProcessResult {
    public static String message = null;
    public static Image image = null;

    public static void clear() {
        message = null;
        image = null;
    }

    public static JSONObject getResultJSON() throws JSONException, InterruptedException {
        JSONObject json = new JSONObject();

        json.put("message", message);
        json.put("image",getImage());

        return json;

    }

    public static String getImage() throws InterruptedException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            while (image == null) {
                System.out.print("WAITING");
			    Thread.sleep(10);
            }
            ImagePlus ipl = image.getImagePlus();

            ipl.setDisplayMode(IJ.COMPOSITE);

            if (ipl.getOverlay() != null)
                ipl.flattenStack();

            ImageIO.write(ipl.getBufferedImage(), "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sun.misc.BASE64Encoder encoder= new sun.misc.BASE64Encoder();
        return encoder.encode(stream.toByteArray());

    }
}
