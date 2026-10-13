package io.github.mianalysis.miaserver.utils;

public class StarCounter {
    private static int starCount = 0;

    public static void reset() {
        starCount = 0;
    }

    public static int getCount() {
        return starCount;
    }

    public void incrementCount() {
        starCount++;
    }
}
