package com.example.demo.utils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.process.ImageProcessor;
import ij.process.LUT;
import io.github.mianalysis.mia.object.image.Image;

public class ProcessResult {
    public static String message = null;
    public static Image image = null;
    public static boolean showImageControls = false;

    public static void clear() {
        message = null;
        image = null;
    }

    public static JSONObject getResultJSON() throws JSONException, InterruptedException {
        JSONObject json = new JSONObject();

        json.put("message", message);
        json.put("image", getImageJSON());
        json.put("showimagecontrols", showImageControls);

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

        return Base64.getEncoder().encodeToString(stream.toByteArray());

    }

    public static JSONObject getImageJSON() throws InterruptedException {
        JSONObject imageJSON = new JSONObject();
        imageJSON.put("name",image.getName());

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

}
