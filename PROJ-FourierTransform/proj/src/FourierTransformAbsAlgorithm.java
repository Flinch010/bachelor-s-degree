
import fouriertransformadmin.AlgorithmError;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.global.ErrorStatus;
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author Ziga
 */
public abstract class FourierTransformAbsAlgorithm extends AbsAlgorithm {

    private FourierTransformTestCase testCase;
    private Complex[] transformed;
    private Complex[] inverseTransformed;

    @Override
    public ErrorStatus init(TestCase test) {
        if (test instanceof FourierTransformTestCase) {
            testCase = (FourierTransformTestCase) test;
            return ErrorStatus.STATUS_OK;
        } else {
            return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, "Invalid test:" + test);
        }
    }

    @Override
    public void run() {
        transformed = transform(testCase.input);
        timer.next();
        inverseTransformed = inverseTransform(testCase.solution);
    }

    @Override
    public ParameterSet done() {
        ParameterSet result = new ParameterSet(testCase.getParameters());

        AlgorithmError transformError = getAlgorithmErrors(transformed, testCase.solution);
        String passedTransform = transformError.max < testCase.minAllowedError ? "OK" : "NOK";
        EParameter passedTransformP = new EParameter("Check - transform",
                "Max diff from actual < Min allowed error",
                ParameterType.STRING,
                passedTransform);
        result.addParameter(passedTransformP, true);
        EParameter minTransfrormError = new EParameter("Emin",
                "Minimum error (minimum absolute difference between actual and calculated transform)",
                ParameterType.DOUBLE,
                transformError.min);
        result.addParameter(minTransfrormError, true);
        EParameter maxTransformError = new EParameter("Emax",
                "Maximum error (maximum absolute difference between actual and calculated transform)",
                ParameterType.DOUBLE,
                transformError.max);
        result.addParameter(maxTransformError, true);
        EParameter avgTransformError = new EParameter("Eavg",
                "Average error (average absolute difference between actual and calculated transform)",
                ParameterType.DOUBLE,
                transformError.average
        );
        result.addParameter(avgTransformError, true);

        AlgorithmError inverseTransfromError = getAlgorithmErrors(inverseTransformed, testCase.input);
        String passedInverseTransform = inverseTransfromError.max < testCase.minAllowedError ? "OK" : "NOK";
        EParameter passedInverseP = new EParameter("Check - inverse transform",
                "Max diff from actual < Min allowed error",
                ParameterType.STRING,
                passedInverseTransform);
        result.addParameter(passedInverseP, true);
        
        return result;
    }

    private static AlgorithmError getAlgorithmErrors(Complex[] calculated, Complex[] actual) {
        double minError = 0;
        double maxError = 0;
        double sum = 0;
        if (actual.length > 0) {
            minError = Double.MAX_VALUE;
            for (int i = 0; i < actual.length; i++) {
                Complex actualValue = actual[i];
                Complex calculatedValue = calculated[i];
                Complex substracted = actualValue.subtract(calculatedValue);
                double real = Math.abs(substracted.getReal());
                double imaginary = Math.abs(substracted.getImaginary());
                sum += real + imaginary;
                if (real > imaginary) {
                    if (real > maxError) {
                        maxError = real;
                    }
                    if (imaginary < minError) {
                        minError = imaginary;
                    }
                } else {
                    if (imaginary > maxError) {
                        maxError = imaginary;
                    }
                    if (real < minError) {
                        minError = real;
                    }
                }
            }
            sum /= actual.length * 2;
        }
        return new AlgorithmError(minError, maxError, sum);
    }

    protected abstract Complex[] transform(Complex[] original);

    protected abstract Complex[] inverseTransform(Complex[] transformed);

}
