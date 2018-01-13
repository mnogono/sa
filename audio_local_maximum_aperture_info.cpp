//
// Created by mnogono on 13.01.18.
//

#include "audio_local_maximum_aperture_info.h"
#include "audio_analyser.h"

using namespace std;

AudioLocalMaximumApertureInfo::AudioLocalMaximumApertureInfo() {
    //reset();
}

bool AudioLocalMaximumApertureInfo::isLocalMaximum() const {
    return localMaximum;
}

bool AudioLocalMaximumApertureInfo::isGoodLocalMaximum() const {
    return goodLocalMaximum;
}

int AudioLocalMaximumApertureInfo::getLocalMaximumSampleIndex() const {
    return localMaximumSampleIndex;
}

double AudioLocalMaximumApertureInfo::getLocalMaximumSampleTimeStamp() const {
    return localMaximumSampleTimeStamp;
}

double AudioLocalMaximumApertureInfo::getMaxAveragedAbsoluteAmplitude() const {
    return maxAveragedAbsoluteAmplitude;
}

double AudioLocalMaximumApertureInfo::getMinAveragedAbsoluteAmplitude() const {
    return minAveragedAbsoluteAmplitude;
}

double AudioLocalMaximumApertureInfo::getLowPercentileAveragedAbsoluteAmplitude() const {
    return lowPercentileAveragedAbsoluteAmplitude;
}

bool AudioLocalMaximumApertureInfo::isShortImpulse() const {
    return shortImpulse;
}

int AudioLocalMaximumApertureInfo::getShortImpulseLeft() const {
    return shortImpulseLeft;
}

int AudioLocalMaximumApertureInfo::getShortImpulseRight() const {
    return shortImpulseRight;
}

double AudioLocalMaximumApertureInfo::getShortImpulseAbsoluteAmplitude() {
    return shortImpulseAbsoluteAmplitude;
}

std::string AudioLocalMaximumApertureInfo::toString() {
    std::stringstream ss;
    ss << "local maximum info";
    if (!localMaximum) {
        ss << " [NOT A MAXIMUM OR TOO LOW]";
    } else {
        ss << ": " << (goodLocalMaximum) ? "GOOD" : "weak";
        ss << (tooLowRatioToTypicalSignal) ? " relating typical" : "";
        ss << (tooLowRatioToLowSignal) ? " relating low" : "";
        ss << (shortImpulse) ? " (short impulse)" : "";
        ss << (tooShortSilenceBeforeImpulse) ? " (but short silence before)" : "";
        ss << (tooShortSilenceAfterImpulse) ? " (but short silence after)" : "";
        ss << ", sample #" << localMaximumSampleIndex;
        ss << " (time " << localMaximumSampleTimeStamp << "), ";
        ss << "averaged amplitude: " << maxAveragedAbsoluteAmplitude << ", ";
        ss << "min and low percentile while ±" << wideApertureHalfDuration << " ";
        ss << "sec: " << minAveragedAbsoluteAmplitude << " and " << lowPercentileAveragedAbsoluteAmplitude << ", ";
        ss << "strong signal duration: " << impulseDuration << " sec";
    }
}


void AudioLocalMaximumApertureInfo::analyseSample(
        AudioAnalyser &a,
        int averagedSampleIndex,
        int averagedImpulseSampleIndex,
        int aperture,
        int wideAperture) {

    if (averagedSampleIndex < 0 || averagedSampleIndex >= a.getAveragedCount()) {
        throw logic_error("averagedSampleIndex=" + averagedSampleIndex);
    }
    if (aperture <= 0) {
        throw logic_error("Zero or negative aperture");
    }
    if (wideAperture <= 0) {
        throw logic_error("Zero or negative wide aperture");
    }

    reset();

    if (averagedImpulseSampleIndex < 0 || averagedImpulseSampleIndex >= a.getAveragedImpulseCount()) {
        // cannot check impulse; impossible in normal situation when impulse average width <= usual average width
        return;
    }

    const int apertureHalf = aperture / 2;
    // Actual aperture size will be 2 * apertureHalf + 1
    const int minIndexInAveragedSquare = averagedSampleIndex - apertureHalf;
    const int maxIndexInAveragedSquare = averagedSampleIndex + apertureHalf;
    if (minIndexInAveragedSquare < 0 || maxIndexInAveragedSquare >= a.getAveragedCount()) {
        // not enough data for stable results
        return;
    }

    double *averagedSquare = a.getAveragedSquare();
    const double currentAmplitudeSquare = averagedSquare[averagedSampleIndex];
    maxAveragedAbsoluteAmplitude = sqrt(currentAmplitudeSquare);
    if (maxAveragedAbsoluteAmplitude < a.getGlobalAmplitudeOfLoudSound()) {
        return;
    }
    for (int k = 1; k < apertureHalf; k++) {
        if (averagedSquare[averagedSampleIndex - k] >= currentAmplitudeSquare
            || averagedSquare[averagedSampleIndex + k] > currentAmplitudeSquare) {
            // - in other words, in an improbable case of several equal values we choose
            // the left sample as a local maximum, but other values are not considered as a maximum
            return;
        }
    }

    localMaximum = true;
    const int shiftToSamples = a.getAveragingApertureLength() / 2;
    localMaximumSampleIndex = averagedSampleIndex + shiftToSamples;
    localMaximumSampleTimeStamp = a.sampleTimeStamp(localMaximumSampleIndex);
    const int wideApertureHalf = wideAperture / 2;
    wideApertureHalfDuration = a.getSingleSampleDuration() * wideApertureHalf;
    if (a.getTypicalAveragedSignal() * a.getMinRatioOfGoodMaximumAndTypicalSignal() > maxAveragedAbsoluteAmplitude) {
        tooLowRatioToTypicalSignal = true;
        return;
    }
    const int minWideIndexInAveragedSquare = averagedSampleIndex - wideApertureHalf;
    const int maxWideIndexInAveragedSquare = averagedSampleIndex + wideApertureHalf;
    // can be little outside the range; then will be truncated
    minAveragedAbsoluteAmplitude = sqrt(max(
            a.getAveragedMin(minWideIndexInAveragedSquare, averagedSampleIndex),
            a.getAveragedMin(averagedSampleIndex, maxWideIndexInAveragedSquare)));
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
        * a.getMinRatioOfGoodMaximumAndLowSignal() > maxAveragedAbsoluteAmplitude) {
        this.tooLowRatioToLowSignal = true;
        return;
    }
    if (checkIsshortImpulse(a, averagedImpulseSampleIndex, currentAmplitudeSquare)) {
        return;
    }
    this.goodLocalMaximum = true;
}