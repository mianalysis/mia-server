package io.github.mianalysis.miaserver.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StarCounter {
    private static HashMap<String, HashSet<String>> starNames = new HashMap<>();

    public static void reset() {
        starNames = new HashMap<>();
    }

    public static int getStarCount() {
        int count = 0;
        for (HashSet<String> currStarNames : starNames.values())
            count += currStarNames.size();
        return count;
    }

    public static void addStar(String workflowName, String starName) {
        starNames.putIfAbsent(workflowName, new HashSet<>());
        starNames.get(workflowName).add(starName);
    }

    public static HashMap<String, HashSet<String>> getAllStars() {
        return starNames;
    }

    public static Set<String> getWorkflowStars(String workflowName) {
        return starNames.get(workflowName);
    }

    public static int getWorkflowStarCount(String workflowName) {
        if (!starNames.containsKey(workflowName))
            return 0;

        return starNames.get(workflowName).size();
        
    }
}
