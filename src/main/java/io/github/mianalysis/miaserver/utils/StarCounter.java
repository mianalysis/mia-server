package io.github.mianalysis.miaserver.utils;

import java.util.HashSet;

public class StarCounter {
    private static HashSet<String> starNames = new HashSet<>();
    
    public static void reset() {
        starNames = new HashSet<>();
    }

    public static int getCount() {
        return starNames.size();
    }

    public static void addStar(String starName) {
        starNames.add(starName);
    }

    public static HashSet<String> getAllStars() {
        return starNames;
    }
}
