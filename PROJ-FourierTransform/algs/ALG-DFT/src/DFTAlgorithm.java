
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author Ziga
 */
public class DFTAlgorithm extends FourierTransformAbsAlgorithm {

    @Override
    protected Complex[] transform(Complex[] input) {
        return calculateTransform(input, false);
    }

    @Override
    protected Complex[] inverseTransform(Complex[] input) {
        return calculateTransform(input, true);
    }

    private static Complex[] calculateTransform(Complex[] input, boolean inverseTransform) {
        Complex[] transformed = new Complex[input.length];
        double piFrac = 2 * Math.PI / input.length;
        if (!inverseTransform) {
            piFrac *= -1;
        }
        for (int n = 0; n < input.length; n++) {
            Complex fn = new Complex(0, 0);
            for (int r = 0; r < input.length; r++) {
                Complex fr = input[r];
                double exponent = piFrac * n * r;
                double cos = Math.cos(exponent);
                double sin = Math.sin(exponent);
                Complex number = new Complex(cos, sin);
                number = number.multiply(fr);
                fn = fn.add(number);
                /*//@REMOVE_LINE
                 if(inverseTransform) {
                 //@COUNT{iMULTIPLY, 1}
                 //@COUNT{iADD, 1}
                 } else {
                 //@COUNT{MULTIPLY, 1}
                 //@COUNT{ADD, 1}
                 }
                 *///@REMOVE_LINE
            }
            if (inverseTransform) {
                //@COUNT{iMULTIPLY, 1}
                fn = fn.divide(input.length);
            }
            transformed[n] = fn;
        }
        return transformed;
    }

}
