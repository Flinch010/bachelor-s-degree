
import org.apache.commons.math3.complex.Complex;

public class FFTTimeRadix2Algorithm extends FourierTransformAbsAlgorithm {

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
                for (int i = 0; i < halfN; i++) {
                    int doubleIndex = 2 * i;
                    even[i] = input[doubleIndex];
                    odd[i] = input[doubleIndex + 1];
                }
                /*//@REMOVE_LINE
                 if(!inverseTransform) {
                 //@COUNT{CALL, 2}
                 }
                 *///@REMOVE_LINE
                Complex[] evenFFT = calculateTransform(even, inverseTransform, false);
                Complex[] oddFFT = calculateTransform(odd, inverseTransform, false);
                Complex[] transformed = new Complex[input.length];
                double angle = 2 * Math.PI / input.length;
                if (!inverseTransform) {
                    angle *= -1;
                }
                for (int r = 0; r < halfN; r++) {
                    Complex omega = new Complex(Math.cos(r * angle), Math.sin(r * angle));
                    /*//@REMOVE_LINE
                     if(inverseTransform) {
                     //@COUNT{iMULTIPLY, 1}
                     //@COUNT{iADD, 2}
                     } else {
                     //@COUNT{MULTIPLY, 1}
                     //@COUNT{ADD, 2}
                     }
                     *///@REMOVE_LINE
                    Complex oddOmegaMulty = oddFFT[r].multiply(omega);

                    if (inverseTransform && isFirstSplit) {
                        //@COUNT{iMULTIPLY, 2}
                        transformed[r] = evenFFT[r].add(oddOmegaMulty).divide(input.length);
                        transformed[r + halfN] = evenFFT[r].subtract(oddOmegaMulty).divide(input.length);
                    } else {
                        transformed[r] = evenFFT[r].add(oddOmegaMulty);
                        transformed[r + halfN] = evenFFT[r].subtract(oddOmegaMulty);
                    }
                }
                return transformed;
            } else {
                return input;
            }
        }
    }

}
