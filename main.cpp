#include <assert.h>
#include "sound_analysis_config.h"
#include "analysis_tools.h"

using namespace std;

int main() {
    int ints[] = {-1,2,3};
    double *vals = AnalysisTools::intToDouble(ints, 3);
    assert(vals[0] == -1.0);
    assert(vals[1] == 2.0);
    assert(vals[2] == 3.0);

    double maxVal = AnalysisTools::maxValue(vals, 3);
    assert(maxVal == 3.0);

    cout << "Hello, World!" << endl;
    return 0;
}