
import org.apache.commons.math3.complex.Complex;

public class FFTTimeRadix4Algorithm extends FourierTransformAbsAlgorithm {

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
        if ((1 << powerOfTwo != input.length) || (powerOfTwo & 1) == 1) {
            throw new IllegalArgumentException("Length of the <input> must be an even power of 2 (i.e. 2^2N, N = 1, 2, ...), actual: " + input.length);
        } else {
            if (input.length >= 4) {
                int halfN = input.length / 2;
                int quarterN = halfN / 2;
                int tripleQuarterN = 3 * quarterN;

                Complex[] first = new Complex[quarterN];
                Complex[] second = new Complex[quarterN];
                Complex[] third = new Complex[quarterN];
                Complex[] fourth = new Complex[quarterN];
                for (int i = 0; i < quarterN; i++) {
                    int index = 4 * i;
                    first[i] = input[index];
                    second[i] = input[index + 1];
                    third[i] = input[index + 2];
                    fourth[i] = input[index + 3];
                }

                /*//@REMOVE_LINE
                 if(!inverseTransform) {
                 //@COUNT{CALL, 4}
                 }
                 *///@REMOVE_LINE
                Complex[] firstFFT = calculateTransform(first, inverseTransform, false);
                Complex[] secondFFT = calculateTransform(second, inverseTransform, false);
                Complex[] thirdFFT = calculateTransform(third, inverseTransform, false);
                Complex[] fourthFFT = calculateTransform(fourth, inverseTransform, false);
                Complex[] transformed = new Complex[input.length];
                double angle = 2 * Math.PI / input.length;
                Complex i;
                if (!inverseTransform) {
                    angle *= -1;
                    i = new Complex(0, 1);
                } else {
                    i = new Complex(0, -1);
                }
                for (int k = 0; k < quarterN; k++) {
                    Complex g0k = firstFFT[k];
                    double kAngle = k * angle;
                    Complex g1k = new Complex(Math.cos(kAngle), Math.sin(kAngle)).multiply(secondFFT[k]);
                    double kAngle2 = k * angle * 2;
                    Complex g2k = new Complex(Math.cos(kAngle2), Math.sin(kAngle2)).multiply(thirdFFT[k]);
                    double kAngle3 = k * angle * 3;
                    Complex g3k = new Complex(Math.cos(kAngle3), Math.sin(kAngle3)).multiply(fourthFFT[k]);

                    Complex g0kPlusg2k = g0k.add(g2k);
                    Complex g0kMinusg2k = g0k.subtract(g2k);
                    Complex g1kPlusg3k = g1k.add(g3k);
                    Complex g1kMinusg3k = g1k.subtract(g3k);
                    Complex imaginaryTimesG1kMinusG3k = new Complex(-1 * i.getImaginary() * g1kMinusg3k.getImaginary(), i.getImaginary() * g1kMinusg3k.getReal());
                    if (inverseTransform && isFirstSplit) {
                        //@COUNT{iMULTIPLY, 4}
                        transformed[k] = g0kPlusg2k.add(g1kPlusg3k).divide(input.length);
                        transformed[k + quarterN] = g0kMinusg2k.subtract(imaginaryTimesG1kMinusG3k).divide(input.length);
                        transformed[k + halfN] = g0kPlusg2k.subtract(g1kPlusg3k).divide(input.length);
                        transformed[k + tripleQuarterN] = g0kMinusg2k.add(imaginaryTimesG1kMinusG3k).divide(input.length);
                    } else {
                        transformed[k] = g0kPlusg2k.add(g1kPlusg3k);
                        transformed[k + quarterN] = g0kMinusg2k.subtract(imaginaryTimesG1kMinusG3k);
                        transformed[k + halfN] = g0kPlusg2k.subtract(g1kPlusg3k);
                        transformed[k + tripleQuarterN] = g0kMinusg2k.add(imaginaryTimesG1kMinusG3k);
                    }
                    /*//@REMOVE_LINE
                     if(inverseTransform) {
                     //@COUNT{iMULTIPLY, 3}
                     //@COUNT{iADD, 8}
                     } else {
                     //@COUNT{MULTIPLY, 3}
                     //twiddle factor multiplication and imaginar unit times g1kMinusg3k
                     //@COUNT{ADD, 8}
                     }
                     *///@REMOVE_LINE
                }
                return transformed;
            } else {
                return input;
            }
        }
    }
}
