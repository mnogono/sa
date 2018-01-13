//
// Created by mnogono on 13.01.18.
//

#include "sound_analysis_config.h"

#ifndef CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H
#define CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H

class AudioLocalMaximumApertureInfo {
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
};


#endif //CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H
