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


    bool checkIsShortImpulse(AudioAnalyser &a, int averagedImpulseSampleIndex, double currentAmplitudeSquare);

    static int findNearestImpulse(AudioAnalyser &a, int averagedImpulseSampleIndex, double amplitudeSquare);

    void reset();
};


#endif //CLION_TEST_AUDIO_LOCAL_MAXIMUM_APERTURE_INFO_H
