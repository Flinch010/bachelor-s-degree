
import static fouriertransformadmin.UtilityFunctions.reverseBitOrder;
import org.apache.commons.math3.complex.Complex;

public class FFTTimeRadix4IterativeAlgorithm extends FourierTransformAbsAlgorithm {

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
        if ((1 << powerOfTwo != N) || (powerOfTwo & 1) == 1) {
            throw new IllegalArgumentException("Length of the <input> must be an even power of 2 (i.e. 2^2N, N = 1, 2, ...), actual: " + N);
        } else {
            int quarterN = N / 4;
            Complex[] omegaValues = new Complex[quarterN];
            Complex[] omega2Values = new Complex[quarterN];
            Complex[] omega3Values = new Complex[quarterN];
            double angle = 2 * Math.PI / N;
            Complex imaginary;
            if (!inverseTransform) {
                angle *= -1;
                imaginary = new Complex(0, 1);
            } else {
                imaginary = new Complex(0, -1);
            }
            for (int k = 0; k < quarterN; k++) {
                double angleK = angle * k;
                double angleK2 = angleK * 2;
                double angleK3 = angleK * 3;
                omegaValues[k] = new Complex(Math.cos(angleK), Math.sin(angleK));
                omega2Values[k] = new Complex(Math.cos(angleK2), Math.sin(angleK2));
                omega3Values[k] = new Complex(Math.cos(angleK3), Math.sin(angleK3));
            }
            Complex[] output = reverseBitOrder(input, powerOfTwo);

            for (int transformSize = 4; transformSize <= N; transformSize *= 4) {
                int xDistance = transformSize / 4;
                int twiddleFactorStep = N / transformSize;
                for (int i = 0; i < N; i += transformSize) {
                    for (int j = i, k = 0; j < i + xDistance; j++, k += twiddleFactorStep) {
                        Complex omega = omegaValues[k];
                        Complex omega2 = omega2Values[k];
                        Complex omega3 = omega3Values[k];

                        int yPosition = j;
                        int gPosition = j + xDistance;
                        int zPosition = j + 2 * xDistance;
                        int hPosition = j + 3 * xDistance;
                        Complex y = output[yPosition];
                        Complex g = output[gPosition];
                        Complex z = output[zPosition];
                        Complex h = output[hPosition];

                        Complex omegaZ = omega.multiply(z);
                        Complex omega2g = omega2.multiply(g);
                        Complex omega3h = omega3.multiply(h);

                        Complex yPlusOmega2g = y.add(omega2g);
                        Complex yMinusOmega2g = y.subtract(omega2g);

                        Complex omegaZPlusOmega3H = omegaZ.add(omega3h);
                        Complex omegaZMinusOmega3H = omegaZ.subtract(omega3h);

                        Complex imaginaryTimesOmegaZMinusOmega3H = new Complex(-1 * imaginary.getImaginary() * omegaZMinusOmega3H.getImaginary(), imaginary.getImaginary() * omegaZMinusOmega3H.getReal());

                        if (inverseTransform && transformSize == N) {
                            //@COUNT{iMULTIPLY, 4}
                            output[yPosition] = yPlusOmega2g.add(omegaZPlusOmega3H).divide(N);
                            output[gPosition] = yMinusOmega2g.subtract(imaginaryTimesOmegaZMinusOmega3H).divide(N);
                            output[zPosition] = yPlusOmega2g.subtract(omegaZPlusOmega3H).divide(N);
                            output[hPosition] = yMinusOmega2g.add(imaginaryTimesOmegaZMinusOmega3H).divide(N);
                        } else {
                            output[yPosition] = yPlusOmega2g.add(omegaZPlusOmega3H);
                            output[gPosition] = yMinusOmega2g.subtract(imaginaryTimesOmegaZMinusOmega3H);
                            output[zPosition] = yPlusOmega2g.subtract(omegaZPlusOmega3H);
                            output[hPosition] = yMinusOmega2g.add(imaginaryTimesOmegaZMinusOmega3H);
                        }
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
                }
                // Prevent overflow in 'transformSize *= 4'
                if (transformSize == N) {
                    break;
                }
            }
            return output;
        }
    }

}
