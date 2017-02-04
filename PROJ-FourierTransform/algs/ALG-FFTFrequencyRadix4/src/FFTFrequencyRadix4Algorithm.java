
import org.apache.commons.math3.complex.Complex;

public class FFTFrequencyRadix4Algorithm extends FourierTransformAbsAlgorithm {

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
                double angle = 2 * Math.PI / input.length;
                Complex i;
                if (!inverseTransform) {
                    angle *= -1;
                    i = new Complex(0, 1);
                } else {
                    i = new Complex(0, -1);
                }

                for (int k = 0; k < quarterN; k++) {
                    int kPlusTripeQuarterN = k + tripleQuarterN;
                    int kPlusHalfN = k + halfN;
                    int kPlusQuarterN = k + quarterN;
                    double angleK = angle * k;
                    Complex omega = new Complex(Math.cos(angleK), Math.sin(angleK));
                    Complex omegaToPowerOf2 = omega.multiply(omega);
                    Complex omegaToPowerOf3 = omega.multiply(omegaToPowerOf2);
                    Complex a = input[k];
                    Complex b = input[kPlusQuarterN];
                    Complex c = input[kPlusHalfN];
                    Complex d = input[kPlusTripeQuarterN];
                    Complex imaginaryTimesD = new Complex(-1 * i.getImaginary() * d.getImaginary(), i.getImaginary() * d.getReal());
                    Complex imaginaryTimesB = new Complex(-1 * i.getImaginary() * b.getImaginary(), i.getImaginary() * b.getReal());

                    Complex aPlusC = a.add(c);
                    Complex aMinusC = a.subtract(c);
                    Complex bPlusD = b.add(d);
                    Complex imaginarySub = imaginaryTimesB.subtract(imaginaryTimesD);

                    first[k] = aPlusC.add(bPlusD);
                    second[k] = aMinusC.subtract(imaginarySub).multiply(omega);
                    third[k] = aPlusC.subtract(bPlusD).multiply(omegaToPowerOf2);
                    fourth[k] = aMinusC.add(imaginarySub).multiply(omegaToPowerOf3);
                    /*//@REMOVE_LINE
                     if(inverseTransform) {
                     //@COUNT{iMULTIPLY, 3}
                     //@COUNT{iADD, 8}
                     } else {
                     //@COUNT{MULTIPLY, 3}
                     //@COUNT{ADD, 8}
                     }
                     *///@REMOVE_LINE
                }

                Complex[] firstFFT = calculateTransform(first, inverseTransform, false);
                Complex[] secondFFT = calculateTransform(second, inverseTransform, false);
                Complex[] thirdFFT = calculateTransform(third, inverseTransform, false);
                Complex[] fourthFFT = calculateTransform(fourth, inverseTransform, false);
                /*//@REMOVE_LINE
                 if(!inverseTransform) {
                 //@COUNT{CALL, 4}
                 }
                 *///@REMOVE_LINE
                Complex[] transformed = new Complex[input.length];
                for (int k = 0; k < quarterN; k++) {
                    int quadrupleK = 4 * k;
                    if (inverseTransform && isFirstSplit) {
                        //@COUNT{iMULTIPLY, 4}
                        transformed[quadrupleK] = firstFFT[k].divide(input.length);
                        transformed[quadrupleK + 1] = secondFFT[k].divide(input.length);
                        transformed[quadrupleK + 2] = thirdFFT[k].divide(input.length);
                        transformed[quadrupleK + 3] = fourthFFT[k].divide(input.length);
                    } else {
                        transformed[quadrupleK] = firstFFT[k];
                        transformed[quadrupleK + 1] = secondFFT[k];
                        transformed[quadrupleK + 2] = thirdFFT[k];
                        transformed[quadrupleK + 3] = fourthFFT[k];
                    }
                }
                return transformed;
            } else {
                return input;
            }
        }
    }

}
