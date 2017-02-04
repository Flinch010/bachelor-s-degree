
import static fouriertransformadmin.UtilityFunctions.reverseBitOrder;
import org.apache.commons.math3.complex.Complex;

public class FFTTimeRadix2IterativeAlgorithm extends FourierTransformAbsAlgorithm {

    @Override
    protected Complex[] transform(Complex[] input) {
        return calculateTransform(input, false);
    }

    @Override
    protected Complex[] inverseTransform(Complex[] input) {
        return calculateTransform(input, true);
    }

    private static Complex[] calculateTransform(Complex[] input, boolean inverseTransform) {
        int N = input.length;
        int powerOfTwo = 31 - Integer.numberOfLeadingZeros(N);
        if (1 << powerOfTwo != N) {
            throw new IllegalArgumentException("Length of the <input> must be a power of 2 (i.e. 2^N, N = 1, 2, ...), actual: " + N);
        } else {
            int halfN = N / 2;
            Complex[] omegaValues = new Complex[halfN];
            double angle = 2 * Math.PI / N;
            if (!inverseTransform) {
                angle *= -1;
            }
            //calculate omegas
            for (int k = 0; k < halfN; k++) {
                double angleK = angle * k;
                omegaValues[k] = new Complex(Math.cos(angleK), Math.sin(angleK));
            }
            Complex[] output = reverseBitOrder(input, powerOfTwo);

            for (int transformSize = 2; transformSize <= N; transformSize *= 2) {
                int xDistance = transformSize / 2;
                int twiddleFactorStep = N / transformSize;
                for (int i = 0; i < N; i += transformSize) {
                    for (int j = i, k = 0; j < i + xDistance; j++, k += twiddleFactorStep) {
                        int jPlusXDistance = j + xDistance;
                        Complex omegaMulty = omegaValues[k].multiply(output[jPlusXDistance]);
                        if (inverseTransform && transformSize == N) {
                            //@COUNT{iMULTIPLY, 2}
                            output[jPlusXDistance] = output[j].subtract(omegaMulty).divide(N);
                            output[j] = output[j].add(omegaMulty).divide(N);
                        } else {
                            output[jPlusXDistance] = output[j].subtract(omegaMulty);
                            output[j] = output[j].add(omegaMulty);
                        }
                        /*//@REMOVE_LINE
                         if(inverseTransform) {
                         //@COUNT{iMULTIPLY, 1}
                         //@COUNT{iADD, 2}
                         } else {
                         //@COUNT{MULTIPLY, 1}
                         //@COUNT{ADD, 2}
                         }
                         *///@REMOVE_LINE
                    }
                }
                // Prevent overflow in 'transformSize *= 2'
                if (transformSize == N) {
                    break;
                }
            }
            return output;
        }
    }

}
