//
// Created by mnogono on 13.01.18.
//

#include "sound_analysis_config.h"

#ifndef CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H
#define CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H

class AudioAnalyser;

class AudioLocalMaximumApertureInfo {
    friend class AudioAnalyser;
private:
    bool localMaximum;
    bool goodLocalMaximum;
    bool tooLowRatioToTypicalSignal;
    bool tooLowRatioToLowSignal;
    bool cannotFindShortImpulse;
    bool shortImpulse;
    bool tooShortSilenceBeforeImpulse;
    bool tooShortSilenceAfterImpulse;
    int localMaximumSampleIndex;
    double localMaximumSampleTimeStamp;
    double maxAveragedAbsoluteAmplitude;

    /** absolute value of current sample */
    double minAveragedAbsoluteAmplitude = nan("");

    double lowPercentileAveragedAbsoluteAmplitude = nan("");
    int shortImpulseLeft;
    int shortImpulseRight;
    double shortImpulseAbsoluteAmplitude = nan("");

    /** Following 2 values for toString() method only (debugging needs): */
    double wideApertureHalfDuration;
    double impulseDuration;

public:
    AudioLocalMaximumApertureInfo();

    bool isLocalMaximum() const;

    bool isGoodLocalMaximum() const;

    int getLocalMaximumSampleIndex() const;

    double getLocalMaximumSampleTimeStamp() const;

    double getMaxAveragedAbsoluteAmplitude() const;

    double getMinAveragedAbsoluteAmplitude() const;

    double getLowPercentileAveragedAbsoluteAmplitude() const;

    bool isShortImpulse() const;

    int getShortImpulseLeft() const;

    int getShortImpulseRight() const;

    double getShortImpulseAbsoluteAmplitude();

    std::string toString();

    void analyseSample(AudioAnalyser &a,
                       int averagedSampleIndex,
                       int averagedImpulseSampleIndex,
                       int aperture,
                       int wideAperture);

//
//    // Uses a.workMemory
//
//
//private bool checkIsshortImpulse(AudioAnalyser a, int averagedImpulseSampleIndex, double currentAmplitudeSquare) {
//        final int nearestImpulse = findNearestImpulse(a, averagedImpulseSampleIndex, currentAmplitudeSquare);
//        if (nearestImpulse == -1) {
//            cannotFindShortImpulse = true;
//            return false;
//        }
//        final double ratio = a.getRatioOfImpulseAndSilence();
//        double silenceSquare = currentAmplitudeSquare / (ratio * ratio);
//        int maxImpulseDuration = (int) (a.getMaxImpulseDuration() / a.getSingleSampleDuration());
//        int left = nearestImpulse;
//        while (left >= 0 && nearestImpulse - left < maxImpulseDuration
//               && a.averagedImpulseSquare[left] >= silenceSquare) {
//            left--;
//        }
//        int right = nearestImpulse;
//        while (right < a.averagedImpulseCount && right - nearestImpulse < maxImpulseDuration
//               && a.averagedImpulseSquare[right] >= silenceSquare) {
//            right++;
//        }
//        impulseDuration = (right - left) * a.getSingleSampleDuration();
//        if (right - left >= maxImpulseDuration) {
//            return false;
//        }
//        this.shortImpulse = true;
//        final int shiftToSamples = a.getAveragingImpulseApertureLength() / 2;
//        this.shortImpulseLeft = left + shiftToSamples;
//        this.shortImpulseRight = right + shiftToSamples;
//        this.shortImpulseAbsoluteAmplitude = Math.sqrt(silenceSquare);
//        this.tooShortSilenceBeforeImpulse = false;
//        this.tooShortSilenceAfterImpulse = false;
//        int minSilenceDuration = (int) (a.getMinSilenceNearImpulseDuration() / a.getSingleSampleDuration());
//        for (int k = left, kMin = Math.max(0, left - minSilenceDuration); k >= kMin; k--) {
//            if (a.averagedImpulseSquare[k] > silenceSquare) {
//                tooShortSilenceBeforeImpulse = true;
//                break;
//            }
//        }
//        for (int k = right, kMax = Math.min(a.averagedImpulseCount - 1, right + minSilenceDuration); k <= kMax; k++) {
//            if (a.averagedImpulseSquare[k] > silenceSquare) {
//                tooShortSilenceAfterImpulse = true;
//                break;
//            }
//        }
//        return !tooShortSilenceBeforeImpulse && !tooShortSilenceAfterImpulse;
//    }
//
//private static int findNearestImpulse(AudioAnalyser a, int averagedImpulseSampleIndex, double amplitudeSquare) {
//        int searchWindowHalf = (int) (a.getAveragingAperture() / a.getSingleSampleDuration()) / 2;
//        // Impulse is averaged by smaller aperture, so, its peak can be little left or right from the local
//        // maximum in averaged graph (averaged by usual aperture). But the distance should not be greater
//        // than the averaging aperture.
//        for (int k = 0; k < searchWindowHalf; k++) {
//            final int left = averagedImpulseSampleIndex - k;
//            if (left >= 0 && a.averagedImpulseSquare[left] >= amplitudeSquare) {
//                return left;
//            }
//            final int right = averagedImpulseSampleIndex + k;
//            if (right < a.averagedImpulseCount && a.averagedImpulseSquare[right] >= amplitudeSquare) {
//                return right;
//            }
//        }
//        return -1;
//    }
//
//private void reset() {
//        localMaximum = false;
//        goodLocalMaximum = false;
//        tooLowRatioToTypicalSignal = false;
//        tooLowRatioToLowSignal = false;
//        cannotFindShortImpulse = false;
//        shortImpulse = false;
//        tooShortSilenceBeforeImpulse = tooShortSilenceAfterImpulse = false;
//        localMaximumSampleIndex = -1;
//        minAveragedAbsoluteAmplitude = Double.NaN;
//        lowPercentileAveragedAbsoluteAmplitude = Double.NaN;
//        localMaximumSampleTimeStamp = Double.NaN;
//        wideApertureHalfDuration = Double.NaN;
//        impulseDuration = Double.NaN;
//        shortImpulseAbsoluteAmplitude = Double.NaN;
//    }
};


#endif //CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H
