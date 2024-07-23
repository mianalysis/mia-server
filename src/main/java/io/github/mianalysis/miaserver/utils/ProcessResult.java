package io.github.mianalysis.miaserver.utils;

import org.json.JSONObject;

public class ProcessResult extends JSONObject {
    private static ProcessResult instance = new ProcessResult();

    private ProcessResult() {
    };

    public static ProcessResult getInstance() {
        return instance;
    }

    public void clear() {
        instance = new ProcessResult();
    }
}
