//
// Created by mnogono on 12.01.18.
//

#include "sound_analysis_config.h"

#ifndef CLION_TEST_ANALYSIS_TOOLS_H
#define CLION_TEST_ANALYSIS_TOOLS_H

class AnalysisTools {
private:
    AnalysisTools();
public:
    /**
     * Convert int arrays to doubles
     * @param [in] values
     * @param [in] size
     * @return results pointer of allocated memory
     */
    static double * intToDouble(const int *values, size_t size);

    /**
     * Find max value under values
     * @param [in] values
     * @param [in] count should be less or equals of values size
     * @return
     */
    static double maxValue(const double *values, int count);
};

#endif //CLION_TEST_ANALYSIS_TOOLS_H


