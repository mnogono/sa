package com.envionsoftware.sounds.analysis;

import java.util.Arrays;

public class AnalysingTools {
    // Disable instantiation:
    private AnalysingTools() {}

    public static double[] intToDouble(int[] values) {
        double[] result = new double[values.length];
        for (int k = 0; k < result.length; k++) {
            result[k] = (double) values[k];
        }
        return result;
    }

    // Functions below are C-like: they work with first count elements of the passed arrays
    // and do not try to allocate memory (for maximal speed).
    static double maxValue(double[] values, int count) {
        double result = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < count; i++) {
            result = Math.max(result, values[i]);
        }
        return result;
    }

    static double percentile(double[] values, int offset, int count, double[] workMemory, double level) {
        if (count <= 0) {
            // to be on the safe side
            return Double.NaN;
        }
        //TODO!! use histogram 256 values between min and max (precision will be enough, O(256)+O(count) ops)
        System.arraycopy(values, offset, workMemory, 0, count);
        Arrays.sort(workMemory, 0, count);
        // - the simplest, but not the fastest way
        int index = (int) Math.round(level * count);
        if (index > count - 1) {
            index = count - 1;
        }
        if (index < 0) {
            index = 0;
        }
        return workMemory[index];
    }

    static int averageCount(int count, int aperture) {
        aperture = Math.max(aperture, 1);
        if (aperture >= count) {
            return 0;
        }
        return count - aperture + 1;
    }

    static void average(double[] result, double[] values, int count, int aperture) {
        aperture = Math.max(aperture, 1);
        final int resultLen = averageCount(count, aperture);
        if (resultLen == 0) {
            return;
        }
        double sum = 0.0;
        for (int k = 0; k < aperture; k++) {
            sum += values[k];
        }
        double apertureInv = 1.0 / aperture;
        result[0] = sum * apertureInv;
        for (int i = aperture, k = 1; k < resultLen; i++, k++) {
            sum += values[i] - values[k - 1];
            result[k] = sum * apertureInv;
        }
    }
}
