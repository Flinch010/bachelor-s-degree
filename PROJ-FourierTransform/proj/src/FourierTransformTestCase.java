
import static fouriertransformadmin.UtilityFunctions.arrayToString;
import org.apache.commons.math3.complex.Complex;
import si.fri.algotest.entities.TestCase;

/**
 *
 * @author Ziga
 */
public class FourierTransformTestCase extends TestCase {

    public Complex[] input;
    public Complex[] solution;
    public double minAllowedError;

    @Override
    public String toString() {
        return super.toString() + "\nMin. allowed error: " + minAllowedError + "\ninput data: " + arrayToString(input) + "\nsolution: " + arrayToString(solution) +"\n";
    }

   
}
