//
// Created by mnogono on 13.01.18.
//

#include "audio_analyser.h"

using namespace std;

AudioAnalyser::AudioAnalyser(double singleSampleDurationSeconds) : singleSampleDuration(singleSampleDurationSeconds) {
    if (singleSampleDurationSeconds <= 0.0) {
        throw logic_error("Zero or negative duration");
    }
}

const double AudioAnalyser::getSingleSampleDuration() const {
    return singleSampleDuration;
}

double AudioAnalyser::getGlobalAmplitudeOfLoudSound() const {
    return globalAmplitudeOfLoudSound;
}

void AudioAnalyser::setGlobalAmplitudeOfLoudSound(double globalAmplitudeOfLoudSound) {
    if (globalAmplitudeOfLoudSound <= 0.0) {
        throw logic_error("Negative amplitude of loud sound");
    }

    this->globalAmplitudeOfLoudSound = globalAmplitudeOfLoudSound;
}

double AudioAnalyser::getTypicalSignalPercentile() const {
    return typicalSignalPercentile;
}

void AudioAnalyser::setTypicalSignalPercentile(double typicalSignalPercentile) {
    this->typicalSignalPercentile = typicalSignalPercentile;
}

double AudioAnalyser::getAveragingApertureLength() const {
    return averagingAperture;
}

void AudioAnalyser::setAveragingAperture(double averagingAperture) {
    if (averagingAperture <= 0.0) {
        throw logic_error("Zero or negative aperture");
    }

    this->averagingAperture = averagingAperture;
}

double AudioAnalyser::getAveragingImpulseApertureLength() const {
    return min(count, static_cast<const int>(averagingImpulseAperture / singleSampleDuration));
}

void AudioAnalyser::setAveragingImpulseAperture(double averagingImpulseAperture) {
    if (averagingImpulseAperture <= 0.0) {
        throw logic_error("Zero or negative aperture");
    }

    this->averagingImpulseAperture = averagingImpulseAperture;
}

double AudioAnalyser::getLocalMaxAperture() const {
    return localMaxAperture;
}

double AudioAnalyser::getLocalMaxApertureLength() const {
    int result = static_cast<int>(localMaxAperture / singleSampleDuration);
    if (result % 2 == 0) {
        //odd values required
        result++;
    }
    return result;
}

void AudioAnalyser::setLocalMaxAperture(double localMaxAperture) {
    if (localMaxAperture <= 0.0) {
        throw logic_error("Zero or negative aperture");
    }

    this->localMaxAperture = localMaxAperture;
}

double AudioAnalyser::getLocalMaxLowSignalPercentile() const {
    return localMaxLowSignalPercentile;
}

int AudioAnalyser::getLocalMaxWideApertureLength() const {
    return static_cast<int>(localMaxWideAperture / singleSampleDuration);
}

void AudioAnalyser::setLocalMaxWideAperture(double localMaxWideAperture) {
    if (localMaxAperture <= 0.0) {
        throw logic_error("Zero or negative aperture");
    }

    this->localMaxWideAperture = localMaxWideAperture;
}

double AudioAnalyser::getMinRatioOfGoodMaximumAndLowSignal() const {
    return minRatioOfGoodMaximumAndLowSignal;
}

void AudioAnalyser::setMinRatioOfGoodMaximumAndLowSignal(double minRatioOfGoodMaximumAndLowSignal) {
    this->minRatioOfGoodMaximumAndLowSignal = minRatioOfGoodMaximumAndLowSignal;
}

double AudioAnalyser::getMaxImpulseDuration() const {
    return maxImpulseDuration;
}

void AudioAnalyser::setMaxImpulseDuration(double maxImpulseDuration) {
    this->maxImpulseDuration = maxImpulseDuration;
}

double AudioAnalyser::getMinSilenceNearImpulseDuration() const {
    return minSilenceNearImpulseDuration;
}

void AudioAnalyser::setMinSilenceNearImpulseDuration(double minSilenceNearImpulseDuration) {
    this->minSilenceNearImpulseDuration = minSilenceNearImpulseDuration;
}

double AudioAnalyser::getRatioOfImpulseAndSilence() const {
    return ratioOfImpulseAndSilence;
}

void AudioAnalyser::setRatioOfImpulseAndSilence(double ratioOfImpulseAndSilence) {
    this->ratioOfImpulseAndSilence = ratioOfImpulseAndSilence;
}

double AudioAnalyser::getMaxDurationBetweenSequentialGoodMaximums() const {
    return maxDurationBetweenSequentialGoodMaximums;
}

void AudioAnalyser::setMaxDurationBetweenSequentialGoodMaximums(double maxDurationBetweenSequentialGoodMaximums) {
    if (maxDurationBetweenSequentialGoodMaximums <= 0.0) {
        throw logic_error("Zero or negative aperture");
    }
    this->maxDurationBetweenSequentialGoodMaximums = maxDurationBetweenSequentialGoodMaximums;
}
