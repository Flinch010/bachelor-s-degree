
import org.apache.commons.math3.complex.Complex;

public class FFTFrequencyRadix2Algorithm extends FourierTransformAbsAlgorithm {

    @Override
    protected Complex[] transform(Complex[] input) {
        return calculateTransform(input, false, false);
    }

    @Override
    protected Complex[] inverseTransform(Complex[] input) {
        return calculateTransform(input, true, true);
    }

    private static Complex[] calculateTransform(Complex[] input, boolean inverseTransform, boolean isFirstSplit) {
        int powerOfTwo = 31 - Integer.numberOfLeadingZeros(input.length);
        if (1 << powerOfTwo != input.length) {
            throw new IllegalArgumentException("Length of the <input> must be a power of 2 (i.e. 2^N, N = 1, 2, ...), actual: " + input.length);
        } else {
            if (input.length >= 2) {
                int halfN = input.length / 2;
                Complex[] even = new Complex[halfN];
                Complex[] odd = new Complex[halfN];
                double angle = 2 * Math.PI / input.length;
                if (!inverseTransform) {
                    angle *= -1;
                }
                for (int k = 0; k < halfN; k++) {
                    int kPlusHalfN = k + halfN;
                    double angleK = angle * k;
                    Complex omega = new Complex(Math.cos(angleK), Math.sin(angleK));
                    even[k] = input[k].add(input[kPlusHalfN]);
                    odd[k] = input[k].subtract(input[kPlusHalfN]).multiply(omega);
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
                Complex[] evenFFT = calculateTransform(even, inverseTransform, false);
                Complex[] oddFFT = calculateTransform(odd, inverseTransform, false);
                /*//@REMOVE_LINE
                 if(!inverseTransform) {
                 //@COUNT{CALL, 2}
                 }
                 *///@REMOVE_LINE
                Complex[] transformed = new Complex[input.length];
                for (int k = 0; k < halfN; k++) {
                    int doubleK = k * 2;
                    if (inverseTransform && isFirstSplit) {
                        transformed[doubleK] = evenFFT[k].divide(input.length);
                        transformed[doubleK + 1] = oddFFT[k].divide(input.length);
                        //@COUNT{iMULTIPLY, 2}
                    } else {
                        transformed[doubleK] = evenFFT[k];
                        transformed[doubleK + 1] = oddFFT[k];
                    }
                }
                return transformed;
            } else {
                return input;
            }
        }
    }

}
