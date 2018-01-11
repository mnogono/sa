package com.envionsoftware.sounds.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class AudioAnalyser {
    private static final Logger LOG = Logger.getLogger(AudioAnalyser.class.getName());

    // (All durations and apertures are specified in seconds)
    private final double singleSampleDuration;

    int count = 0;
    // Note: only elements 0..count-1 of all arrays are actual.
    double[] amplitudeSamples = new double[0];
    double[] amplitudeSquare = new double[0];
    double[] workMemory = new double[0];

    // Customizable parameters:
    private double globalAmplitudeOfLoudSound = 0.0;
    // - should be customized according global sound system settings, possible value 1000-2000 for 16-bit sound
    private double typicalSignalPercentile = 0.5;
    // - 0.5 means median: "cry" myst be essentially stronger than global typical signal
    private double averagingAperture = 0.2;
    // - for "cry" detection
    private double averagingImpulseAperture = 0.05;
    // - for quick impulse changes detection

    private double localMaxAperture = 0.25;
    // - "cry" must be a maximum during localMaxAperture seconds
    private double minRatioOfGoodMaximumAndTypicalSignal = 2.0;
    private double localMaxWideAperture = 2.0;
    // - "cry" must be much stronger then low signal while localMaxWideAperture seconds ("cry" cannot be too long)
    private double localMaxLowSignalPercentile = 0.1;
    private double minRatioOfGoodMaximumAndLowSignal = 2.0;

    private double maxImpulseDuration = 0.3;
    private double minSilenceNearImpulseDuration = 0.5;
    private double ratioOfImpulseAndSilence = 2.0;

    private double maxDurationBetweenSequentialGoodMaximums = 2.0;

    // Preprocessing results:
    private double maxAbsoluteSignal;
    private double typicalAveragedSignal;

    // Analysis results:
    int averagedCount = 0;
    double[] averagedSquare = new double[0];
    int averagedImpulseCount = 0;
    double[] averagedImpulseSquare = new double[0];
    private List<AudioLocalMaximumApertureInfo> allLocalMaximums = new ArrayList<>();
    private List<AudioLocalMaximumApertureInfo> goodLocalMaximums = new ArrayList<>();
    private int maxNumberOfSequentialGoodMaximums = -1;
    private int totalNumberOfSequentialGoodMaximums = -1;

    public AudioAnalyser(double singleSampleDurationInSeconds) {
        if (singleSampleDurationInSeconds <= 0.0) {
            throw new IllegalArgumentException("Zero or negative duration");
        }
        this.singleSampleDuration = singleSampleDurationInSeconds;
    }

    public double getSingleSampleDuration() {
        return singleSampleDuration;
    }

    public void setSamples(double[] samples) {
        setSamples(samples, 0, samples.length);
    }

    // Very quick operation (almost speed of copying array)
    public void setSamples(double[] samples, int offset, int count) {
        if (samples == null) {
            throw new NullPointerException("Null amplitude samples");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Zero or negative number of amplitude samples");
        }
        if (offset < 0 || offset + count > samples.length) {
            throw new IndexOutOfBoundsException("Invalid offset=" + offset + " or count=" + count +
                " for array double[" + samples.length + "]");
        }
        this.count = samples.length;
        this.amplitudeSamples = allocateIfNecessary(this.amplitudeSamples, count);
        System.arraycopy(samples, offset, this.amplitudeSamples, 0, count);
        this.amplitudeSquare = allocateIfNecessary(this.amplitudeSquare, count);
        for (int k = 0; k < count; k++) {
            this.amplitudeSquare[k] = this.amplitudeSamples[k] * this.amplitudeSamples[k];
        }
        this.workMemory = allocateIfNecessary(this.workMemory, count);
    }

    public double getGlobalAmplitudeOfLoudSound() {
        return globalAmplitudeOfLoudSound;
    }

    public void setGlobalAmplitudeOfLoudSound(double globalAmplitudeOfLoudSound) {
        if (globalAmplitudeOfLoudSound < 0.0) {
            throw new IllegalArgumentException("Negative amplitude of loud sound");
        }
        this.globalAmplitudeOfLoudSound = globalAmplitudeOfLoudSound;
    }

    public double getTypicalSignalPercentile() {
        return typicalSignalPercentile;
    }

    public void setTypicalSignalPercentile(double typicalSignalPercentile) {
        this.typicalSignalPercentile = typicalSignalPercentile;
    }

    public double getAveragingAperture() {
        return averagingAperture;
    }

    public int getAveragingApertureLength() {
        return Math.min(count, (int) (averagingAperture / singleSampleDuration));
    }

    public void setAveragingAperture(double averagingAperture) {
        if (averagingAperture <= 0.0) {
            throw new IllegalArgumentException("Zero or negative aperture");
        }
        this.averagingAperture = averagingAperture;
    }

    public double getAveragingImpulseAperture() {
        return averagingImpulseAperture;
    }

    public int getAveragingImpulseApertureLength() {
        return Math.min(count, (int) (averagingImpulseAperture / singleSampleDuration));
    }

    public void setAveragingImpulseAperture(double averagingImpulseAperture) {
        if (averagingImpulseAperture <= 0.0) {
            throw new IllegalArgumentException("Zero or negative aperture");
        }
        this.averagingImpulseAperture = averagingImpulseAperture;
    }

    public double getLocalMaxAperture() {
        return localMaxAperture;
    }

    public int getLocalMaxApertureLength() {
        int result = (int) (localMaxAperture / singleSampleDuration);
        if (result % 2 == 0) {
            // odd values required
            result++;
        }
        return result;
    }

    public void setLocalMaxAperture(double localMaxAperture) {
        if (localMaxAperture <= 0.0) {
            throw new IllegalArgumentException("Zero or negative aperture");
        }
        this.localMaxAperture = localMaxAperture;
    }

    public double getLocalMaxLowSignalPercentile() {
        return localMaxLowSignalPercentile;
    }

    public double getMinRatioOfGoodMaximumAndTypicalSignal() {
        return minRatioOfGoodMaximumAndTypicalSignal;
    }

    public void setMinRatioOfGoodMaximumAndTypicalSignal(double minRatioOfGoodMaximumAndTypicalSignal) {
        this.minRatioOfGoodMaximumAndTypicalSignal = minRatioOfGoodMaximumAndTypicalSignal;
    }

    public double getLocalMaxWideAperture() {
        return localMaxWideAperture;
    }

    public int getLocalMaxWideApertureLength() {
        return (int) (localMaxWideAperture / singleSampleDuration);
    }

    public void setLocalMaxWideAperture(double localMaxWideAperture) {
        if (localMaxWideAperture <= 0.0) {
            throw new IllegalArgumentException("Zero or negative aperture");
        }
        this.localMaxWideAperture = localMaxWideAperture;
    }

    public void setLocalMaxLowSignalPercentile(double localMaxLowSignalPercentile) {
        this.localMaxLowSignalPercentile = localMaxLowSignalPercentile;
    }

    public double getMinRatioOfGoodMaximumAndLowSignal() {
        return minRatioOfGoodMaximumAndLowSignal;
    }

    public void setMinRatioOfGoodMaximumAndLowSignal(double minRatioOfGoodMaximumAndLowSignal) {
        this.minRatioOfGoodMaximumAndLowSignal = minRatioOfGoodMaximumAndLowSignal;
    }

    public double getMaxImpulseDuration() {
        return maxImpulseDuration;
    }

    public void setMaxImpulseDuration(double maxImpulseDuration) {
        if (maxImpulseDuration < 0.0) {
            throw new IllegalArgumentException("Negative duration");
        }
        this.maxImpulseDuration = maxImpulseDuration;
    }

    public double getMinSilenceNearImpulseDuration() {
        return minSilenceNearImpulseDuration;
    }

    public void setMinSilenceNearImpulseDuration(double minSilenceNearImpulseDuration) {
        if (minSilenceNearImpulseDuration < 0.0) {
            throw new IllegalArgumentException("Negative duration");
        }
        this.minSilenceNearImpulseDuration = minSilenceNearImpulseDuration;
    }

    public double getRatioOfImpulseAndSilence() {
        return ratioOfImpulseAndSilence;
    }

    public void setRatioOfImpulseAndSilence(double ratioOfImpulseAndSilence) {
        if (ratioOfImpulseAndSilence < 1.0) {
            throw new IllegalArgumentException("Ratio of impulse and silence must be >= 1.0");
        }
        this.ratioOfImpulseAndSilence = ratioOfImpulseAndSilence;
    }

    public double getMaxDurationBetweenSequentialGoodMaximums() {
        return maxDurationBetweenSequentialGoodMaximums;
    }

    public void setMaxDurationBetweenSequentialGoodMaximums(double maxDurationBetweenSequentialGoodMaximums) {
        if (maxDurationBetweenSequentialGoodMaximums <= 0.0) {
            throw new IllegalArgumentException("Zero or negative duration");
        }
        this.maxDurationBetweenSequentialGoodMaximums = maxDurationBetweenSequentialGoodMaximums;
    }

    public double singleSampleDuration() {
        return singleSampleDuration;
    }

    public int count() {
        return count;
    }

    public double[] amplitudeSamples() {
        return Arrays.copyOf(amplitudeSamples, count);
    }

    public double[] amplitudeSquare() {
        return Arrays.copyOf(amplitudeSquare, count);
    }

    public double maxAbsoluteSignal() {
        return maxAbsoluteSignal;
    }

    public double typicalAveragedSignal() {
        return typicalAveragedSignal;
    }

    public int averagedCount() {
        return averagedCount;
    }

    public double[] averagedSquare() {
        return Arrays.copyOf(averagedSquare, averagedCount);
    }

    public int averagedImpulseCount() {
        return averagedImpulseCount;
    }

    public double[] averagedImpulseSquare() {
        return Arrays.copyOf(averagedImpulseSquare, averagedImpulseCount);
    }

    public List<AudioLocalMaximumApertureInfo> allLocalMaximums() {
        return allLocalMaximums;
    }

    public List<AudioLocalMaximumApertureInfo> goodLocalMaximums() {
        return goodLocalMaximums;
    }

    public int maxNumberOfSequentialGoodMaximums() {
        return maxNumberOfSequentialGoodMaximums;
    }

    public int totalNumberOfSequentialGoodMaximums() {
        return totalNumberOfSequentialGoodMaximums;
    }

    public double sampleTimeStamp(int sampleIndex) {
        return sampleIndex * singleSampleDuration;
    }

    // Usually called for large duration
    public void preprocess() {
        average();
        this.maxAbsoluteSignal = Math.sqrt(AnalysingTools.maxValue(amplitudeSquare, count));
        this.typicalAveragedSignal = Math.sqrt(AnalysingTools.percentile(
            averagedSquare, 0, averagedCount, workMemory, typicalSignalPercentile));
    }

    // Usually called every 1-2 seconds for interval 3-5 seconds
    public void analyze() {
        average();
        // - Must be called also here, for a case when preprocess was called for another samples.
        // But it is very quick procedure.
        averageImpulse();
        int aperture = getLocalMaxApertureLength();
        int wideAperture = getLocalMaxWideApertureLength();
        allLocalMaximums.clear();
        goodLocalMaximums.clear();
        AudioLocalMaximumApertureInfo info = new AudioLocalMaximumApertureInfo();
        for (int index = 0; index < averagedCount; index++) {
            final int impulseIndex = index + (averagedImpulseCount - averagedCount) / 2;
            info.analyseSample(this, index, impulseIndex, aperture, wideAperture);
            if (info.isLocalMaximum()) {
                final AudioLocalMaximumApertureInfo clone = info.clone();
                allLocalMaximums.add(clone);
                if (info.isGoodLocalMaximum()) {
                    goodLocalMaximums.add(clone);
                    // no problems to share the same instance between two lists:
                    // it cannot be modified outside this package
                }
            }
        }
    }

    public void postprocessFoundMaximums() {
        int maxCount = 0;
        int totalCount = 0;
        int count = 1;
        for (int k = 1, n = goodLocalMaximums.size(); k < n; k++) {
            double distance =
                goodLocalMaximums.get(k).getLocalMaximumSampleTimeStamp()
                - goodLocalMaximums.get(k - 1).getLocalMaximumSampleTimeStamp();
            if (distance <= maxDurationBetweenSequentialGoodMaximums) {
                if (count == 1) {
                    // start of the series
                    totalCount++;
                }
                count++;
                totalCount++;
            } else {
                count = 1;
            }
            maxCount = Math.max(maxCount, count);
        }
        this.maxNumberOfSequentialGoodMaximums = maxCount == 1 ? 0 : maxCount;
        this.totalNumberOfSequentialGoodMaximums = totalCount;
    }

    double averagedMin(int minIndexInAveragedSquare, int maxIndexInAveragedSquare) {
        minIndexInAveragedSquare = Math.max(minIndexInAveragedSquare, 0);
        maxIndexInAveragedSquare = Math.min(maxIndexInAveragedSquare, averagedCount - 1);
        double result = Double.POSITIVE_INFINITY;
        for (int k = minIndexInAveragedSquare; k <= maxIndexInAveragedSquare; k++) {
            if (averagedSquare[k] < result) {
                result = averagedSquare[k];
            }
        }
        return result;
    }

    double averagedPercentile(int minIndexInAveragedSquare, int maxIndexInAveragedSquare, double level) {
        minIndexInAveragedSquare = Math.max(minIndexInAveragedSquare, 0);
        maxIndexInAveragedSquare = Math.min(maxIndexInAveragedSquare, averagedCount - 1);
        return AnalysingTools.percentile(
            averagedSquare,
            minIndexInAveragedSquare, maxIndexInAveragedSquare - minIndexInAveragedSquare + 1,
            workMemory,
            level);
    }

    private void average() {
        int aperture = getAveragingApertureLength();
        this.averagedCount = AnalysingTools.averageCount(count, aperture);
        this.averagedSquare = allocateIfNecessary(this.averagedSquare, this.averagedCount);
        AnalysingTools.average(averagedSquare, amplitudeSquare, count, aperture);
    }

    private void averageImpulse() {
        int aperture = getAveragingImpulseApertureLength();
        this.averagedImpulseCount = AnalysingTools.averageCount(count, aperture);
        this.averagedImpulseSquare = allocateIfNecessary(this.averagedImpulseSquare, this.averagedImpulseCount);
        AnalysingTools.average(averagedImpulseSquare, amplitudeSquare, count, aperture);
    }

    private static double[] allocateIfNecessary(double[] array, int len) {
        if (array == null || len <= array.length) {
            return array;
        } else {
            LOG.config("Allocating double[" + len + "]...");
            return new double[len];
        }
    }
}
