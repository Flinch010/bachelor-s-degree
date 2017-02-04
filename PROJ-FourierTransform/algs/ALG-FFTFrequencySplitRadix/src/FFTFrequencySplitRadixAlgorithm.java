
import org.apache.commons.math3.complex.Complex;

public class FFTFrequencySplitRadixAlgorithm extends FourierTransformAbsAlgorithm {

    @Override
    protected Complex[] transform(Complex[] values) {
        return calculateTransform(values, false, false);
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
            if (input.length >= 4) {
                int halfN = input.length / 2;
                int quarterN = halfN / 2;
                int tripleQuarterN = 3 * quarterN;

                Complex[] radix2 = new Complex[halfN];
                Complex[] radix4Part1 = new Complex[quarterN];
                Complex[] radix4Part2 = new Complex[quarterN];
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
                    Complex a = input[k];
                    Complex b = input[kPlusQuarterN];
                    Complex c = input[kPlusHalfN];
                    Complex d = input[kPlusTripeQuarterN];

                    //radix-2 part
                    radix2[k] = a.add(c);
                    radix2[k + quarterN] = b.add(d);

                    //radix-4 part
//                    Complex imaginaryTimesB = new Complex(-1 * i.getImaginary() * b.getImaginary(), i.getImaginary() * b.getReal());
//                    Complex imaginaryTimesD = new Complex(-1 * i.getImaginary() * d.getImaginary(), i.getImaginary() * d.getReal());
                    Complex bMinusD = b.subtract(d);
                    Complex aMinusC = a.subtract(c);
                    
                    Complex imaginaryTimesSub = new Complex(-1 * i.getImaginary() * bMinusD.getImaginary(), i.getImaginary() * bMinusD.getReal());

                    double angleK = angle * k;
                    Complex omega = new Complex(Math.cos(angleK), Math.sin(angleK));
                    double angleK3 = angleK * 3;
                    Complex omegaToPowerOf3 = new Complex(Math.cos(angleK3), Math.sin(angleK3));

                    radix4Part1[k] = aMinusC.subtract(imaginaryTimesSub).multiply(omega);
                    radix4Part2[k] = aMinusC.add(imaginaryTimesSub).multiply(omegaToPowerOf3);

                    /*//@REMOVE_LINE
                     if(inverseTransform) {
                     //@COUNT{iMULTIPLY, 2}
                     //@COUNT{iADD, 6}
                     } else {
                     //@COUNT{MULTIPLY, 2}
                     //@COUNT{ADD, 6}
                     }
                     *///@REMOVE_LINE
                }

                Complex[] radix2FFT = calculateTransform(radix2, inverseTransform, false);
                Complex[] radix4Part1FFT = calculateTransform(radix4Part1, inverseTransform, false);
                Complex[] radix4Part2FFT = calculateTransform(radix4Part2, inverseTransform, false);
                /*//@REMOVE_LINE
                 if(!inverseTransform) {
                 //@COUNT{CALL, 3}
                 }
                 *///@REMOVE_LINE
                Complex[] transformed = new Complex[input.length];
                for (int k = 0; k < quarterN; k++) {
                    int doubleK = 2 * k;
                    int quadrupleK = 2 * doubleK;
                    if (inverseTransform && isFirstSplit) {
                        //@COUNT{iMULTIPLY, 4}
                        transformed[doubleK] = radix2FFT[k].divide(input.length);
                        transformed[quadrupleK + 1] = radix4Part1FFT[k].divide(input.length);
                        transformed[doubleK + halfN] = radix2FFT[k + quarterN].divide(input.length);
                        transformed[quadrupleK + 3] = radix4Part2FFT[k].divide(input.length);

                    } else {
                        transformed[doubleK] = radix2FFT[k];
                        transformed[quadrupleK + 1] = radix4Part1FFT[k];
                        transformed[doubleK + halfN] = radix2FFT[k + quarterN];
                        transformed[quadrupleK + 3] = radix4Part2FFT[k];
                    }
                }
                return transformed;
            } else if (input.length >= 2) {
                return calculateRadix2Transform(input, inverseTransform, isFirstSplit);
            } else {
                return input;
            }
        }
    }

    private static Complex[] calculateRadix2Transform(Complex[] input, boolean inverseTransform, boolean isFirstSplit) {
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
            Complex[] evenFFT = calculateRadix2Transform(even, inverseTransform, false);
            Complex[] oddFFT = calculateRadix2Transform(odd, inverseTransform, false);
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
