package com.example.demo;

public class ModuleGroup {
    int startIdx = -1;
    int endIdx = -1;
    String title = "";
    String description = "";    

    public ModuleGroup(int startIdx, int endIdx, String title, String description) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.title = title;
        this.description = description;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }

    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
}
