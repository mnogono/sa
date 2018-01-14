#include <cstring>
#include "analysing_tools.h"

using namespace std;

AnalysingTools::AnalysingTools() = default;

double *AnalysingTools::intToDouble(const int *values, int size) {
    double *result = new double[size];
    for (int i = 0; i < size; ++i) {
        result[i] = static_cast<double>(values[i]);
    }
    return result;
}

double AnalysingTools::maxValue(const double *values, int count) {
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

static int cmpDbl(const void *v1, const void *v2) {
    double val1 = *static_cast<const double *>(v1);
    double val2 = *static_cast<const double *>(v2);

    if (val1 < val2) {
        return -1;
    } else if (val2 > val1) {
        return 1;
    }
    return 0;
}

double AnalysingTools::percentile(const double *values, int offset, int count, double *workMemory,
                                 double level) {
    if (count <= 0) {
        return nan("");
    }

    memcpy(static_cast<void *>(workMemory),
           static_cast<const void *>(&values[offset]),
           static_cast<size_t>(count));
    qsort(static_cast<void *>(workMemory),
          static_cast<size_t>(count),
          sizeof(double),
          cmpDbl
    );
    size_t index = static_cast<size_t>(round(level * count));
    if (index > count - 1) {
        index = static_cast<size_t>(count - 1);
    }
    if (index < 0) {
        index = 0;
    }

    return workMemory[index];
}

int AnalysingTools::averageCount(int count, int aperture) {
    aperture = max(aperture, 1);
    if (aperture >= count) {
        return 0;
    }
    return count - aperture + 1;
}

void AnalysingTools::average(double *result, const double *values, int count, int aperture) {
    aperture = max(aperture, 1);
    const int resultLen = averageCount(count, aperture);
    if (resultLen == 0) {
        return;
    }

    double sum = 0.0;
    for (int k = 0; k < aperture; ++k) {
        sum += values[k];
    }

    const double apertureInv = 1.0 / aperture;
    result[0] = sum * apertureInv;
    for (int i = aperture, k = 1; k < resultLen; ++i, ++k) {
        sum += values[i] - values[k - 1];
        result[k] = sum * apertureInv;
    }
}
