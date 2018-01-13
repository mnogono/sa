//
// Created by mnogono on 13.01.18.
//

#include "sound_analysis_config.h"

#ifndef CLION_TEST_AUDIO_ANALYSER_H
#define CLION_TEST_AUDIO_ANALYSER_H

#include "audio_local_maximum_aperture_info.h"

class AudioAnalyser {
private:
    /** (All durations and apertures are specified in seconds) */
    const double singleSampleDuration;

    /** size of follow arrays */
    int count = 0;

    /** Note: only elements 0..count-1 of all arrays are actual */
    double *amplitudeSamples;
    double *amplitudeSquare;
    double *workMemory;

    /** customizable parameters: */
    double globalAmplitudeOfLoudSound = 0.0;

    /** should be customized according global sound system settings,
     * possible value 1000-2000 for 16-bit sound */
    double typicalSignalPercentile = 0.5;

    /** 0.5 means median: "cry" must be essentially stronger then global typical signal */
    double averagingAperture = 0.2;

    /** for "cry" detection */
    double averagingImpulseAperture = 0.05;

    /** for quick impulse changes detection */
    double localMaxAperture = 0.25;

    /** "cry" must be a maximum during localMaxAperture seconds */
    double minRatioOfGoodMaximumTypicalSignal = 2.0;
    double localMaxWideAperture = 2.0;

    /** "cry" must be much stronger then low signal while localMaxWideAperture second
     * ("cry" cannot be too long)
     */
    double localMaxLowSignalPercentile = 0.1;
    double minRatioOfGoodMaximumAndLowSignal = 2.0;

    double maxImpulseDuration = 0.3;
    double minSilenceNearImpulseDuration = 0.5;
    double ratioOfImpulseAndSilence = 2.0;

    double maxDurationBetweenSequentialGoodMaximums = 2.0;

    /** Preprocessing results: */
    double maxAbsoluteSignal;
    double typicalAveragedSignal;

    /** Analysis results: */
    size_t averagedCount = 0;
    double *averagedSquare;

    size_t averagedImpulseCount = 0;
    double averagedImpulseSquare;

    std::vector<AudioLocalMaximumApertureInfo> allLocalMaximums;
    std::vector<AudioLocalMaximumApertureInfo> goodLocalMaximums;

    int maxNumberOfSequentialGoodMaximums = -1;
    int totalNumberOfSequentialGoodMaximums = -1;

public:
    explicit AudioAnalyser(double singleSampleDurationSeconds);

    const double getSingleSampleDuration() const;

    double getGlobalAmplitudeOfLoudSound() const;

    void setGlobalAmplitudeOfLoudSound(double globalAmplitudeOfLoudSound);

    double getTypicalSignalPercentile() const;

    void setTypicalSignalPercentile(double typicalSignalPercentile);

    double getAveragingApertureLength() const;

    void setAveragingAperture(double averagingAperture);

    double getAveragingImpulseApertureLength() const;

    void setAveragingImpulseAperture(double averagingImpulseAperture);

    double getLocalMaxAperture() const;

    double getLocalMaxApertureLength() const;

    void setLocalMaxAperture(double localMaxAperture);

    double getLocalMaxLowSignalPercentile() const;

    int getLocalMaxWideApertureLength() const;

    void setLocalMaxWideAperture(double localMaxWideAperture);

    double getMinRatioOfGoodMaximumAndLowSignal() const;

    void setMinRatioOfGoodMaximumAndLowSignal(double minRatioOfGoodMaximumAndLowSignal);

    double getMaxImpulseDuration() const;

    void setMaxImpulseDuration(double maxImpulseDuration);

    double getMinSilenceNearImpulseDuration() const;

    void setMinSilenceNearImpulseDuration(double minSilenceNearImpulseDuration);

    double getRatioOfImpulseAndSilence() const;

    void setRatioOfImpulseAndSilence(double ratioOfImpulseAndSilence);

    double getMaxDurationBetweenSequentialGoodMaximums() const;

    void setMaxDurationBetweenSequentialGoodMaximums(double maxDurationBetweenSequentialGoodMaximums);
};

#endif //CLION_TEST_AUDIO_ANALYSER_H
