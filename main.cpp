#include "sound_analysis_config.h"
#include "analysing_tools.h"

using namespace std;

int main() {
    int ints[] = {-1,2,3};
    double *vals = AnalysingTools::intToDouble(ints, 3);
    assert(vals[0] == -1.0);
    assert(vals[1] == 2.0);
    assert(vals[2] == 3.0);

    double maxVal = AnalysingTools::maxValue(vals, 3);
    assert(maxVal == 3.0);

    cout << "Hello, World!" << endl;
    system("pause");
    return 0;
}