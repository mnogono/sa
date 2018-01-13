//
// Created by mnogono on 12.01.18.
//

#include "analysis_tools.h"

using namespace std;

AnalysisTools::AnalysisTools() = default;

double *AnalysisTools::intToDouble(const int *values, size_t size) {
    double *result = new double[size];
    for (size_t i = 0; i < size; ++i) {
        result[i] = static_cast<double>(values[i]);
    }
    return result;
}

double AnalysisTools::maxValue(const double *values, int count) {
    if (count <= 0) {
        if (numeric_limits<double>::is_iec559) {
            return -numeric_limits<double>::infinity();
        } else if (numeric_limits<double>::has_infinity) {
            return -numeric_limits<double>::infinity();
        } else {
            return 0;
        }
    } else {
        double result = values[0];
        for (int i = 0; i < count; ++i) {
            if (values[i] > result) {
                result = values[i];
            }
        }
        return result;
    }
}