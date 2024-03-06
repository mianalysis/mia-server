package com.example.demo.utils;

import java.util.Iterator;
import java.util.TreeMap;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GUISeparator;

public class ModuleGroups extends TreeMap<Integer, ModuleGroup> {
    private int groupIdx = 0;
    private int maxIdx = 0;

    public ModuleGroups(Modules modules) {
        int startIdx = 0;
        int endIdx = -1;

        if (modules == null || modules.size() == 0)
            return;

        // Loading any modules before the first separator (those which run by default
        // once at the beginning) and the first separator
        GUISeparator prevSeparator = null;
        Iterator<Module> iterator = modules.iterator();
        while (iterator.hasNext()) {
            endIdx++;
            Module module = iterator.next();
            if (module instanceof GUISeparator
                    && (boolean) module.getParameterValue(GUISeparator.SHOW_PROCESSING, null)) {
                prevSeparator = (GUISeparator) module;
                if (endIdx != 0)
                    put(-1, new ModuleGroup(0, endIdx, "", ""));
                startIdx = endIdx;
                break;
            }
        }

        // Loading the main module groups
        int count = 0;
        while (iterator.hasNext()) {
            endIdx++;
            Module module = iterator.next();
            if (module instanceof GUISeparator
                    && (boolean) module.getParameterValue(GUISeparator.SHOW_PROCESSING, null)) {
                maxIdx = count;
                if (prevSeparator == null)
                    put(count++, new ModuleGroup(startIdx, endIdx, "", ""));
                else
                    put(count++,
                            new ModuleGroup(startIdx, endIdx, prevSeparator.getNickname(), prevSeparator.getNotes()));

                prevSeparator = (GUISeparator) module;
                startIdx = endIdx;
            }
        }

        // Adding the final group
        maxIdx = count;
        if (prevSeparator == null)
            put(count++, new ModuleGroup(startIdx, endIdx + 1, "", ""));
        else
            put(count++,
                    new ModuleGroup(startIdx, endIdx + 1, prevSeparator.getNickname(), prevSeparator.getNotes()));

    }

    public boolean hasPreprocessingGroup() {
        return containsKey(-1);

    }

    public ModuleGroup getPreprocessingGroup() {
        if (hasPreprocessingGroup())
            return get(-1);
        else
            return null;
    }

    public ModuleGroup getGroup(int idx) {
        return get(idx);
    }

    public ModuleGroup getCurrentGroup() {
        return get(groupIdx);
    }

    public boolean previousGroup() {
        if (groupIdx == 0)
            return false;

        groupIdx--;

        return true;

    }

    public boolean nextGroup() {
        if (groupIdx == maxIdx-1)
            return false;

        groupIdx++;

        return true;

    }
}
