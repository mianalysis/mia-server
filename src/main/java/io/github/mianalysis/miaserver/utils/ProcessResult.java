package io.github.mianalysis.miaserver.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessResult {
    public String workflowName = null;
    public JSONArray message = null;
    public JSONArray modules = null;
    public JSONObject image = null;
    public JSONArray overlays = null;
    public boolean hasPrevious = false;
    public boolean hasNext = false;
    public JSONObject clickListener = null;
    
    public byte[] reds = null;
    public byte[] greens = null;
    public byte[] blues = null;

    private static ProcessResult instance = new ProcessResult();

    private ProcessResult() {
    };

    public static ProcessResult getInstance() {
        return instance;
    }

    public void clear() {
        instance = new ProcessResult();
    }

    public String getWorkflowName() {
        return  workflowName;
    } 

    public JSONArray getMessage() {
        return message;
    }

    public JSONArray getModules() {
        return modules;
    }

    public JSONObject getImage() {
        return image;
    }

    public JSONArray getOverlays() {
        return overlays;
    }

    public void setOverlays(JSONArray overlays) {
        this.overlays = overlays;
    }

    public boolean hasPreviousGroup() {
        return hasPrevious;
    }

    public boolean hasNextGroup() {
        return hasNext;
    }

    public JSONObject getClickListener() {
        return clickListener;
    }

    public void setClickListener(JSONObject clickListener) {
        this.clickListener = clickListener;
    }

    public byte[] getReds() {
        return reds;
    }

    public byte[] getGreens() {
        return greens;
    }

    public byte[] getBlues() {
        return blues;
    }

    public int getStarCount() {
        return StarCounter.getStarCount();
    }

    public int getWorkflowStarCount() {
        return StarCounter.getWorkflowStarCount(workflowName);
    }
}
