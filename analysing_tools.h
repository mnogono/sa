//
// Created by mnogono on 12.01.18.
//

#include "sound_analysis_config.h"

#ifndef CLION_TEST_ANALYSIS_TOOLS_H
#define CLION_TEST_ANALYSIS_TOOLS_H

class AnalysingTools {
private:
    AnalysingTools();
public:
    /**
     * Convert int arrays to doubles
     * @param [in] values converted array
     * @param [in] size values size
     * @return results pointer of allocated memory
     */
    static double * intToDouble(const int *values, size_t size);

    /**
     * Find max value under values array
     * @param [in] values
     * @param [in] count should be less or equals of values size
     * @return
     */
    static double maxValue(const double *values, int count);

    /**
     * TODO
     * @param [in] values
     * @param [in] offset
     * @param [in] count
     * @param [in|out] workMemory
     * @param [in] level
     * @return
     */
    static double percentile(const double *values, size_t offset, size_t count, double *workMemory, double level);

    /**
     * TODO
     * @param [in] count
     * @param [in] aperture
     * @return
     */
    static int averageCount(int count, int aperture);

    /**
     * TODO
     * @param result
     * @param values
     * @param count
     * @param aperture
     */
    static void average(double *result, const double *values, int count, int aperture);
};

#endif //CLION_TEST_ANALYSIS_TOOLS_H


