
import static fouriertransformadmin.UtilityFunctions.reverseBitOrder;
import org.apache.commons.math3.complex.Complex;

public class FFTBluesteinAlgorithm extends FourierTransformAbsAlgorithm {

    @Override
    protected Complex[] transform(Complex[] input) {
        return calculateTransform(input, false);
    }

    @Override
    protected Complex[] inverseTransform(Complex[] input) {
        return calculateTransform(input, true);
    }

    public static Complex[] calculateTransform(Complex[] input, boolean inverseTransform) {
        // define M as the smallest power of two that is greater than or equal to 2N − 2
        int N = input.length;
        int M = 2 * N - 2;
        int log2N = 31 - Integer.numberOfLeadingZeros(M);  // Equal to floor(log2(n))
        if (1 << log2N != M) {
            M = Integer.highestOneBit(2 * N - 2) << 1;
        }
        // A. (Preprocessing) Compute the symmetric Toeplitz H: for given N, compute the scalar constants
        Complex[] hLArray = new Complex[N];
        Complex[] omegaNPower = new Complex[N];
        double angle = Math.PI / N;
        if (inverseTransform) {
            angle *= -1;
        }
        for (int l = 0; l < N; l++) {
            int lSquared = (int) ((long) l * l % (N * 2));  // This is more accurate than l^2 = l * l
            double finalAngle = angle * lSquared;
            double real = Math.cos(finalAngle);
            double imag = Math.sin(finalAngle);
            hLArray[l] = new Complex(real, imag);
            omegaNPower[l] = new Complex(real, -1 * imag);
        }
        /*
         B. (Preprocessing) Embed H in the circulant H(2): define M as the smallest power
         of two that is greater than or equal to 2N − 2, and compute the vector h(2) of
         length M:
         */
        Complex[] h2LArray = new Complex[M];
        h2LArray[0] = hLArray[0];
        for (int l = 1; l < N; l++) {
            h2LArray[l] = h2LArray[M - l] = hLArray[l];
        }
        if (M > 2 * N - 2) {
            for (int l = N; l < M - N + 1; l++) {
                h2LArray[l] = new Complex(0, 0);
            }
        }
        /*
         C. (Preprocessing) Compute the diagonal D in Lemma 13.2: use the radix-2 FFT
         to compute the DFT matrix-vector product
         */
        Complex[] hHatArray = calculateFFTRadix2(h2LArray, false, !inverseTransform);
        /*
         D. Given x_l, define the extended vector y(2) of length M
         */
        Complex[] y2Array = new Complex[M];
        for (int l = 0; l < N; l++) {
            /*//@REMOVE_LINE
             if(inverseTransform) {
             //@COUNT{iMULTIPLY, 1}
             } else {
             //@COUNT{MULTIPLY, 1}
             }
             *///@REMOVE_LINE
            y2Array[l] = input[l].multiply(omegaNPower[l]);
        }
        for (int l = N; l < M; l++) {
            y2Array[l] = new Complex(0, 0);
        }
        //E. Compute Ωy(2): use the radix-2 FFT to compute the DFT matrix-vector product
        Complex[] y2ArrayTransform = calculateFFTRadix2(y2Array, false, !inverseTransform);
        //F. Compute ˆz = D (Ωy^(2)): scale Yr by hˆr = D[r, r]; i
        Complex[] zHatArray = new Complex[M];
        for (int r = 0; r < M; r++) {
            /*//@REMOVE_LINE
             if(inverseTransform) {
             //@COUNT{iMULTIPLY, 1}
             } else {
             //@COUNT{MULTIPLY, 1}
             }
             *///@REMOVE_LINE
            zHatArray[r] = hHatArray[r].multiply(y2ArrayTransform[r]);
        }
        //G. Compute z(2) = Ω−1ˆz : use the radix-2 inverse FFT to compute the inverse DFT matrix-vector product
        Complex[] zArray = calculateFFTRadix2(zHatArray, true, !inverseTransform);
        //H. Extract the Xr’s from the top N elements in z(2)
        Complex[] result = new Complex[N];
        if (inverseTransform) {
            for (int r = 0; r < N; r++) {
                //@COUNT{iMULTIPLY, 2}
                result[r] = omegaNPower[r].multiply(zArray[r]).divide(N);
            }
        } else {
            for (int r = 0; r < N; r++) {
                //@COUNT{MULTIPLY, 1}
                result[r] = omegaNPower[r].multiply(zArray[r]);
            }
        }
        return result;
    }

    /**
     *
     * @param input
     * @param inverseTransform
     * @return result in bit reversed order (does not apply bit-reversal, we
     * save O(n) time)
     */
    private static Complex[] calculateFFTRadix2NR(Complex[] input, boolean inverseTransform) {
        int N = input.length;
        int powerOfTwo = 31 - Integer.numberOfLeadingZeros(N);
        if (1 << powerOfTwo != N) {
            throw new IllegalArgumentException("Length of <input> must be a power of 2 (i.e. 2^N, N = 1, 2, ...), actual: " + input.length);
        } else {
            int halfN = N / 2;
            Complex[] omegaValues = new Complex[halfN];
            double angle = 2 * Math.PI / N;
            if (!inverseTransform) {
                angle *= -1;
            }
            for (int k = 0; k < halfN; k++) {
                double angleK = angle * k;
                omegaValues[k] = new Complex(Math.cos(angleK), Math.sin(angleK));
            }
            for (int size = N, halfSize = size / 2, step = 1; size > 1; step = step * 2, size = halfSize, halfSize = halfSize / 2) {
                for (int k = 0; k < step; k++) {
                    int first = k * size;
                    int last = first + halfSize;
                    int twiddle = 0;
                    for (int j = first; j < last; j++) {
                        Complex omega = omegaValues[twiddle];
                        Complex temp = input[j];
                        if (inverseTransform && step == N / 2) {
                            //@COUNT{iMULTIPLY, 2}
                            input[j] = temp.add(input[j + halfSize]).divide(N);
                            input[j + halfSize] = omega.multiply(temp.subtract(input[j + halfSize])).divide(N);
                        } else {
                            input[j] = temp.add(input[j + halfSize]);
                            input[j + halfSize] = omega.multiply(temp.subtract(input[j + halfSize]));
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
                        twiddle = twiddle + step;
                    }
                }
            }
            return input;
        }
    }

    /**
     *
     * @param input
     * @param inverseTransform
     * @return result in natural (input) order
     */
    private static Complex[] calculateFFTRadix2(Complex[] input, boolean inverseTransform, boolean isOriginalTransform) {
        int N = input.length;
        int powerOfTwo = 31 - Integer.numberOfLeadingZeros(N);
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
                        /*//@REMOVE_LINE
                         if(isOriginalTransform) {
                         //@COUNT{MULTIPLY, 2}
                         } else {
                         //@COUNT{iMULTIPLY, 2}
                         }
                         *///@REMOVE_LINE
                        output[jPlusXDistance] = output[j].subtract(omegaMulty).divide(N);
                        output[j] = output[j].add(omegaMulty).divide(N);
                    } else {
                        output[jPlusXDistance] = output[j].subtract(omegaMulty);
                        output[j] = output[j].add(omegaMulty);
                    }
                    /*//@REMOVE_LINE
                     if(isOriginalTransform) {
                     //@COUNT{MULTIPLY, 1}
                     //@COUNT{ADD, 2}
                     } else {
                     //@COUNT{iMULTIPLY, 1}
                     //@COUNT{iADD, 2}
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
