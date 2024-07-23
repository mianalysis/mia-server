package io.github.mianalysis.miaserver.results;

import org.json.JSONObject;

public interface Result {
    public String getName();
    public JSONObject getJSON();
}
