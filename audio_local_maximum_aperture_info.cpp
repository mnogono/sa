//
// Created by mnogono on 13.01.18.
//

#include "audio_local_maximum_aperture_info.h"
#include "audio_analyser.h"

using namespace std;

AudioLocalMaximumApertureInfo::AudioLocalMaximumApertureInfo() {
    reset();
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
            a.averagedMin(minWideIndexInAveragedSquare, averagedSampleIndex),
            a.averagedMin(averagedSampleIndex, maxWideIndexInAveragedSquare)));
    // - important! signal must be essentially less both BEFORE and AFTER maximum
    if (minAveragedAbsoluteAmplitude * a.getMinRatioOfGoodMaximumAndLowSignal() > maxAveragedAbsoluteAmplitude) {
        // optimization: percentile cannot be less than minimum
        tooLowRatioToLowSignal = true;
        return;
    }
    const double percentile = a.getLocalMaxLowSignalPercentile();
    lowPercentileAveragedAbsoluteAmplitude = sqrt(max(
            a.averagedPercentile(minWideIndexInAveragedSquare, averagedSampleIndex, percentile),
            a.averagedPercentile(averagedSampleIndex, maxWideIndexInAveragedSquare, percentile)));
    if (lowPercentileAveragedAbsoluteAmplitude
        * a.getMinRatioOfGoodMaximumAndLowSignal() > maxAveragedAbsoluteAmplitude) {
        tooLowRatioToLowSignal = true;
        return;
    }

    if (checkIsShortImpulse(a, averagedImpulseSampleIndex, currentAmplitudeSquare)) {
        return;
    }

    goodLocalMaximum = true;
}

int AudioLocalMaximumApertureInfo::findNearestImpulse(AudioAnalyser &a,
                                                             int averagedImpulseSampleIndex,
                                                             double amplitudeSquare) {
    int searchWindowHalf = (int) (a.getAveragingAperture() / a.getSingleSampleDuration()) / 2;
    // Impulse is averaged by smaller aperture, so, its peak can be little left or right from the local
    // maximum in averaged graph (averaged by usual aperture). But the distance should not be greater
    // than the averaging aperture.
    for (int k = 0; k < searchWindowHalf; k++) {
        const int left = averagedImpulseSampleIndex - k;
        if (left >= 0 && a.getAveragedImpulseSquare()[left] >= amplitudeSquare) {
            return left;
        }
        const int right = averagedImpulseSampleIndex + k;
        if (right < a.getAveragedImpulseCount() && a.getAveragedImpulseSquare()[right] >= amplitudeSquare) {
            return right;
        }
    }

    return -1;
}

bool AudioLocalMaximumApertureInfo::checkIsShortImpulse(AudioAnalyser &a, int averagedImpulseSampleIndex,
                                                        double currentAmplitudeSquare) {
    const int nearestImpulse = findNearestImpulse(a, averagedImpulseSampleIndex, currentAmplitudeSquare);
    if (nearestImpulse == -1) {
        cannotFindShortImpulse = true;
        return false;
    }
    const double ratio = a.getRatioOfImpulseAndSilence();
    double silenceSquare = currentAmplitudeSquare / (ratio * ratio);
    const int maxImpulseDuration = (int) (a.getMaxImpulseDuration() / a.getSingleSampleDuration());
    int left = nearestImpulse;
    while (left >= 0 && nearestImpulse - left < maxImpulseDuration
           && a.getAveragedImpulseSquare()[left] >= silenceSquare) {
        left--;
    }
    int right = nearestImpulse;
    while (right < a.getAveragedImpulseCount() && right - nearestImpulse < maxImpulseDuration
           && a.getAveragedImpulseSquare()[right] >= silenceSquare) {
        right++;
    }
    impulseDuration = (right - left) * a.getSingleSampleDuration();
    if (right - left >= maxImpulseDuration) {
        return false;
    }
    shortImpulse = true;
    const int shiftToSamples = static_cast<int>(a.getAveragingImpulseApertureLength() / 2);
    shortImpulseLeft = left + shiftToSamples;
    shortImpulseRight = right + shiftToSamples;
    shortImpulseAbsoluteAmplitude = sqrt(silenceSquare);
    tooShortSilenceBeforeImpulse = false;
    tooShortSilenceAfterImpulse = false;
    const int minSilenceDuration = (int) (a.getMinSilenceNearImpulseDuration() / a.getSingleSampleDuration());
    for (int k = left, kMin = max(0, left - minSilenceDuration); k >= kMin; k--) {
        if (a.getAveragedImpulseSquare()[k] > silenceSquare) {
            tooShortSilenceBeforeImpulse = true;
            break;
        }
    }
    for (int k = right, kMax = min(a.getAveragedImpulseCount() - 1, right + minSilenceDuration); k <= kMax; k++) {
        if (a.getAveragedImpulseSquare()[k] > silenceSquare) {
            tooShortSilenceAfterImpulse = true;
            break;
        }
    }
    return !tooShortSilenceBeforeImpulse && !tooShortSilenceAfterImpulse;
}

void AudioLocalMaximumApertureInfo::reset() {
    localMaximum = false;
    goodLocalMaximum = false;
    tooLowRatioToTypicalSignal = false;
    tooLowRatioToLowSignal = false;
    cannotFindShortImpulse = false;
    shortImpulse = false;
    tooShortSilenceBeforeImpulse = tooShortSilenceAfterImpulse = false;
    localMaximumSampleIndex = -1;
    minAveragedAbsoluteAmplitude = nan("");
    lowPercentileAveragedAbsoluteAmplitude = nan("");
    localMaximumSampleTimeStamp = nan("");
    wideApertureHalfDuration = nan("");
    impulseDuration = nan("");
    shortImpulseAbsoluteAmplitude = nan("");

}
