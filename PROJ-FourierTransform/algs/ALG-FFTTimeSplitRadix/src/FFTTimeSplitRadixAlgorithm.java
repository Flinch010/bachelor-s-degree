
import org.apache.commons.math3.complex.Complex;

public class FFTTimeSplitRadixAlgorithm extends FourierTransformAbsAlgorithm {

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
            if (input.length >= 4) {
                int halfN = input.length / 2;
                int quarterN = halfN / 2;
                int tripleQuarterN = 3 * quarterN;

                Complex[] radix2 = new Complex[halfN];
                Complex[] radix4Part1 = new Complex[quarterN];
                Complex[] radix4Part2 = new Complex[quarterN];

                for (int k = 0; k < quarterN; k++) {
                    //radix-2 part
                    int doubleK = k * 2;
                    radix2[k] = input[doubleK];
                    radix2[k + quarterN] = input[doubleK + halfN];

                    //radix-4 part
                    int index = doubleK * 2;
                    radix4Part1[k] = input[index + 1];
                    radix4Part2[k] = input[index + 3];
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
                    double tripleAngle = angleK * 3;
                    Complex omegaToPowerOf3 = new Complex(Math.cos(tripleAngle), Math.sin(tripleAngle));

                    Complex radix4Part1FFTTimesOmega = radix4Part1FFT[k].multiply(omega);
                    Complex radix4Part2FFTTimesOmega3 = radix4Part2FFT[k].multiply(omegaToPowerOf3);
                    Complex radix4Sum = radix4Part1FFTTimesOmega.add(radix4Part2FFTTimesOmega3);
                    Complex radix4Sub = radix4Part1FFTTimesOmega.subtract(radix4Part2FFTTimesOmega3);
                    Complex imaginaryTimesRadix4Sub = new Complex(-1 * i.getImaginary() * radix4Sub.getImaginary(), i.getImaginary() * radix4Sub.getReal());

                    Complex a = radix2FFT[k];
                    Complex b = radix2FFT[kPlusQuarterN];
                    if (inverseTransform && isFirstSplit) {
                        //@COUNT{iMULTIPLY, 4}
                        transformed[k] = a.add(radix4Sum).divide(input.length);
                        transformed[kPlusQuarterN] = b.subtract(imaginaryTimesRadix4Sub).divide(input.length);
                        transformed[kPlusHalfN] = a.subtract(radix4Sum).divide(input.length);
                        transformed[kPlusTripeQuarterN] = b.add(imaginaryTimesRadix4Sub).divide(input.length);
                    } else {
                        transformed[k] = a.add(radix4Sum);
                        transformed[kPlusQuarterN] = b.subtract(imaginaryTimesRadix4Sub);
                        transformed[kPlusHalfN] = a.subtract(radix4Sum);
                        transformed[kPlusTripeQuarterN] = b.add(imaginaryTimesRadix4Sub);
                    }
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
            for (int i = 0; i < halfN; i++) {
                int doubleIndex = 2 * i;
                even[i] = input[doubleIndex];
                odd[i] = input[doubleIndex + 1];
            }
            Complex[] evenFFT = calculateRadix2Transform(even, inverseTransform, false);
            Complex[] oddFFT = calculateRadix2Transform(odd, inverseTransform, false);
            /*//@REMOVE_LINE
             if(!inverseTransform) {
             //@COUNT{CALL, 2}
             }
             *///@REMOVE_LINE
            Complex[] transformed = new Complex[input.length];
            double angle = 2 * Math.PI / input.length;
            if (!inverseTransform) {
                angle *= -1;
            }
            for (int r = 0; r < halfN; r++) {
                Complex omega = new Complex(Math.cos(r * angle), Math.sin(r * angle));
                Complex oddOmegaMulty = oddFFT[r].multiply(omega);
                if (inverseTransform && isFirstSplit) {
                    //@COUNT{iMULTIPLY, 2}
                    transformed[r] = evenFFT[r].add(oddOmegaMulty).divide(input.length);
                    transformed[r + halfN] = evenFFT[r].subtract(oddOmegaMulty).divide(input.length);
                } else {
                    transformed[r] = evenFFT[r].add(oddOmegaMulty);
                    transformed[r + halfN] = evenFFT[r].subtract(oddOmegaMulty);
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
            return transformed;
        } else {
            return input;
        }
    }

}
