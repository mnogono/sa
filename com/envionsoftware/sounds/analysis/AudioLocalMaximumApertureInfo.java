package com.envionsoftware.sounds.analysis;

import java.util.Locale;

public class AudioLocalMaximumApertureInfo implements Cloneable {
    private boolean localMaximum;
    private boolean goodLocalMaximum;
    private boolean tooLowRatioToTypicalSignal;
    private boolean tooLowRatioToLowSignal;
    private boolean cannotFindShortImpulse;
    private boolean shortImpulse;
    private boolean tooShortSilenceBeforeImpulse;
    private boolean tooShortSilenceAfterImpulse;
    private int localMaximumSampleIndex;
    private double localMaximumSampleTimeStamp;
    private double maxAveragedAbsoluteAmplitude;
    // - absolute value of current sample
    private double minAveragedAbsoluteAmplitude = Double.NaN;

    private double lowPercentileAveragedAbsoluteAmplitude = Double.NaN;
    private int shortImpulseLeft;
    private int shortImpulseRight;
    private double shortImpulseAbsoluteAmplitude = Double.NaN;

    // Following 2 values for toString() method only (debugging needs):
    private double wideApertureHalfDuration;
    private double impulseDuration;

    AudioLocalMaximumApertureInfo() {
        reset();
    }

    public boolean isLocalMaximum() {
        return localMaximum;
    }

    public boolean isGoodLocalMaximum() {
        return goodLocalMaximum;
    }

    public int getLocalMaximumSampleIndex() {
        return localMaximumSampleIndex;
    }

    public double getLocalMaximumSampleTimeStamp() {
        return localMaximumSampleTimeStamp;
    }

    public double getMaxAveragedAbsoluteAmplitude() {
        return maxAveragedAbsoluteAmplitude;
    }

    public double getMinAveragedAbsoluteAmplitude() {
        return minAveragedAbsoluteAmplitude;
    }

    public double getLowPercentileAveragedAbsoluteAmplitude() {
        return lowPercentileAveragedAbsoluteAmplitude;
    }

    public boolean isShortImpulse() {
        return shortImpulse;
    }

    public int getShortImpulseLeft() {
        return shortImpulseLeft;
    }

    public int getShortImpulseRight() {
        return shortImpulseRight;
    }

    public double getShortImpulseAbsoluteAmplitude() {
        return shortImpulseAbsoluteAmplitude;
    }

    /**
     * Returns new identical copy of this object.
     *
     * @return new identical copy of this object.
     */
    @Override
    public AudioLocalMaximumApertureInfo clone() {
        try {
            return (AudioLocalMaximumApertureInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Cannot occur when implements Cloneable");
        }
    }

    @Override
    public String toString() {
        return "local maximum info"
            + (!localMaximum ? " [NOT A MAXIMUM OR TOO LOW]" :
            String.format(Locale.US, ": %s, sample #%d (time %.3f), "
                    + "averaged amplitude: %.2f, "
                    + "min and low percentile while ±%.3f sec: %.2f and %.2f, "
                    + "strong signal duration: %.3f sec",
                (goodLocalMaximum ? "GOOD" : "weak")
                    + (tooLowRatioToTypicalSignal ? " relating typical" : "")
                    + (tooLowRatioToLowSignal ? " relating low" : "")
                    + (shortImpulse ? " (short impulse)" : "")
                    + (tooShortSilenceBeforeImpulse ? " (but short silence before)" : "")
                    + (tooShortSilenceAfterImpulse ? " (but short silence after)" : ""),
                localMaximumSampleIndex, localMaximumSampleTimeStamp,
                maxAveragedAbsoluteAmplitude,
                wideApertureHalfDuration, minAveragedAbsoluteAmplitude, lowPercentileAveragedAbsoluteAmplitude,
                impulseDuration));
    }

    // Uses a.workMemory
    void analyseSample(
        AudioAnalyser a,
        int averagedSampleIndex,
        int averagedImpulseSampleIndex,
        int aperture,
        int wideAperture)
    {
        if (averagedSampleIndex < 0 || averagedSampleIndex >= a.averagedCount) {
            throw new IndexOutOfBoundsException("averagedSampleIndex=" + averagedSampleIndex);
        }
        if (aperture <= 0) {
            throw new IllegalArgumentException("Zero or negative aperture");
        }
        if (wideAperture <= 0) {
            throw new IllegalArgumentException("Zero or negative wide aperture");
        }
        reset();
        if (averagedImpulseSampleIndex < 0 || averagedImpulseSampleIndex >= a.averagedImpulseCount) {
            // cannot check impulse; impossible in normal situation when impulse average width <= usual average width
            return;
        }
        final int apertureHalf = aperture / 2;
        // Actual aperture size will be 2 * apertureHalf + 1
        final int minIndexInAveragedSquare = averagedSampleIndex - apertureHalf;
        final int maxIndexInAveragedSquare = averagedSampleIndex + apertureHalf;
        if (minIndexInAveragedSquare < 0 || maxIndexInAveragedSquare >= a.averagedCount) {
            // not enough data for stable results
            return;
        }
        final double currentAmplitudeSquare = a.averagedSquare[averagedSampleIndex];
        this.maxAveragedAbsoluteAmplitude = Math.sqrt(currentAmplitudeSquare);
        if (this.maxAveragedAbsoluteAmplitude < a.getGlobalAmplitudeOfLoudSound()) {
            return;
        }
        for (int k = 1; k < apertureHalf; k++) {
            if (a.averagedSquare[averagedSampleIndex - k] >= currentAmplitudeSquare
                || a.averagedSquare[averagedSampleIndex + k] > currentAmplitudeSquare)
            {
                // - in other words, in an improbable case of several equal values we choose
                // the left sample as a local maximum, but other values are not considered as a maximum
                return;
            }
        }
        this.localMaximum = true;
        final int shiftToSamples = a.getAveragingApertureLength() / 2;
        this.localMaximumSampleIndex = averagedSampleIndex + shiftToSamples;
        this.localMaximumSampleTimeStamp = a.sampleTimeStamp(localMaximumSampleIndex);
        final int wideApertureHalf = wideAperture / 2;
        this.wideApertureHalfDuration = a.getSingleSampleDuration() * wideApertureHalf;
        if (a.typicalAveragedSignal() * a.getMinRatioOfGoodMaximumAndTypicalSignal() > maxAveragedAbsoluteAmplitude) {
            this.tooLowRatioToTypicalSignal = true;
            return;
        }
        final int minWideIndexInAveragedSquare = averagedSampleIndex - wideApertureHalf;
        final int maxWideIndexInAveragedSquare = averagedSampleIndex + wideApertureHalf;
        // can be little outside the range; then will be truncated
        this.minAveragedAbsoluteAmplitude = Math.sqrt(Math.max(
            a.averagedMin(minWideIndexInAveragedSquare, averagedSampleIndex),
            a.averagedMin(averagedSampleIndex, maxWideIndexInAveragedSquare)));
        // - important! signal must be essentially less both BEFORE and AFTER maximum
        if (minAveragedAbsoluteAmplitude * a.getMinRatioOfGoodMaximumAndLowSignal() > maxAveragedAbsoluteAmplitude) {
            // optimization: percentile cannot be less than minimum
            this.tooLowRatioToLowSignal = true;
            return;
        }
        final double percentile = a.getLocalMaxLowSignalPercentile();
        this.lowPercentileAveragedAbsoluteAmplitude = Math.sqrt(Math.max(
            a.averagedPercentile(minWideIndexInAveragedSquare, averagedSampleIndex, percentile),
            a.averagedPercentile(averagedSampleIndex, maxWideIndexInAveragedSquare, percentile)));
//        System.out.printf("Found maximum %.3f at %d (%d) in aperture 2*%d%n",
//            Math.sqrt(currentAmplitudeSquare), averagedSampleIndex, localMaximumSampleIndex, apertureHalf);
        if (lowPercentileAveragedAbsoluteAmplitude
            * a.getMinRatioOfGoodMaximumAndLowSignal() > maxAveragedAbsoluteAmplitude)
        {
            this.tooLowRatioToLowSignal = true;
            return;
        }
        if (shortImpulse(a, averagedImpulseSampleIndex, currentAmplitudeSquare)) {
            return;
        }
        this.goodLocalMaximum = true;
    }

    private boolean shortImpulse(AudioAnalyser a, int averagedImpulseSampleIndex, double currentAmplitudeSquare) {
        final int nearestImpulse = findNearestImpulse(a, averagedImpulseSampleIndex, currentAmplitudeSquare);
        if (nearestImpulse == -1) {
            cannotFindShortImpulse = true;
            return false;
        }
        final double ratio = a.getRatioOfImpulseAndSilence();
        double silenceSquare = currentAmplitudeSquare / (ratio * ratio);
        int maxImpulseDuration = (int) (a.getMaxImpulseDuration() / a.getSingleSampleDuration());
        int left = nearestImpulse;
        while (left >= 0 && nearestImpulse - left < maxImpulseDuration
            && a.averagedImpulseSquare[left] >= silenceSquare) {
            left--;
        }
        int right = nearestImpulse;
        while (right < a.averagedImpulseCount && right - nearestImpulse < maxImpulseDuration
            && a.averagedImpulseSquare[right] >= silenceSquare) {
            right++;
        }
        impulseDuration = (right - left) * a.getSingleSampleDuration();
        if (right - left >= maxImpulseDuration) {
            return false;
        }
        this.shortImpulse = true;
        final int shiftToSamples = a.getAveragingImpulseApertureLength() / 2;
        this.shortImpulseLeft = left + shiftToSamples;
        this.shortImpulseRight = right + shiftToSamples;
        this.shortImpulseAbsoluteAmplitude = Math.sqrt(silenceSquare);
        this.tooShortSilenceBeforeImpulse = false;
        this.tooShortSilenceAfterImpulse = false;
        int minSilenceDuration = (int) (a.getMinSilenceNearImpulseDuration() / a.getSingleSampleDuration());
        for (int k = left, kMin = Math.max(0, left - minSilenceDuration); k >= kMin; k--) {
            if (a.averagedImpulseSquare[k] > silenceSquare) {
                tooShortSilenceBeforeImpulse = true;
                break;
            }
        }
        for (int k = right, kMax = Math.min(a.averagedImpulseCount - 1, right + minSilenceDuration); k <= kMax; k++) {
            if (a.averagedImpulseSquare[k] > silenceSquare) {
                tooShortSilenceAfterImpulse = true;
                break;
            }
        }
        return !tooShortSilenceBeforeImpulse && !tooShortSilenceAfterImpulse;
    }

    private static int findNearestImpulse(AudioAnalyser a, int averagedImpulseSampleIndex, double amplitudeSquare) {
        int searchWindowHalf = (int) (a.getAveragingAperture() / a.getSingleSampleDuration()) / 2;
        // Impulse is averaged by smaller aperture, so, its peak can be little left or right from the local
        // maximum in averaged graph (averaged by usual aperture). But the distance should not be greater
        // than the averaging aperture.
        for (int k = 0; k < searchWindowHalf; k++) {
            final int left = averagedImpulseSampleIndex - k;
            if (left >= 0 && a.averagedImpulseSquare[left] >= amplitudeSquare) {
                return left;
            }
            final int right = averagedImpulseSampleIndex + k;
            if (right < a.averagedImpulseCount && a.averagedImpulseSquare[right] >= amplitudeSquare) {
                return right;
            }
        }
        return -1;
    }

    private void reset() {
        localMaximum = false;
        goodLocalMaximum = false;
        tooLowRatioToTypicalSignal = false;
        tooLowRatioToLowSignal = false;
        cannotFindShortImpulse = false;
        shortImpulse = false;
        tooShortSilenceBeforeImpulse = tooShortSilenceAfterImpulse = false;
        localMaximumSampleIndex = -1;
        minAveragedAbsoluteAmplitude = Double.NaN;
        lowPercentileAveragedAbsoluteAmplitude = Double.NaN;
        localMaximumSampleTimeStamp = Double.NaN;
        wideApertureHalfDuration = Double.NaN;
        impulseDuration = Double.NaN;
        shortImpulseAbsoluteAmplitude = Double.NaN;
    }
}
