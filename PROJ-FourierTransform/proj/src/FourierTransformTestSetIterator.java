
import static fouriertransformadmin.Constants.TEST_CASE_FIELDS_DELIMITER;
import static fouriertransformadmin.Constants.TEST_CASE_COMPLEX_NUMBERS_DELIMITER;
import static fouriertransformadmin.Constants.TEST_CASE_NUMBERS_DELIMITER;
import static fouriertransformadmin.UtilityFunctions.replaceCommaDot;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.complex.Complex;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author Ziga
 */
public class FourierTransformTestSetIterator extends DefaultTestSetIterator {

    String filePath;
    String testFileName;

    @Override
    public void initIterator() {
        super.initIterator();

        String fileName = testSet.getTestSetDescriptionFile();
        filePath = testSet.entity_rootdir;
        testFileName = filePath + File.separator + fileName;
    }

    @Override
    public TestCase getCurrent() {
        if (currentInputLine == null) {
            ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No valid input!");
            return null;
        }
        String[] fields = currentInputLine.split(TEST_CASE_FIELDS_DELIMITER);
        int minFieldLength = 5;
        if (fields.length < minFieldLength) {
            String message = String.format("not enough fields, min: %d, found: %d", minFieldLength, fields.length);
            reportInvalidDataFormat(message);
            return null;
        }
        String testName = fields[0];
        int size = 0;
        try {
            size = Integer.parseInt(fields[1]);
        } catch (Exception e) {
            reportInvalidDataFormat("'n' is not a number");
            return null;
        }
        String type = fields[2];

        // unique identificator of a test
        EParameter testIDPar = EResultDescription.getTestIDParameter("Test-" + Integer.toString(lineNumber));
        EParameter parameter1 = new EParameter("Test", "Test name", ParameterType.STRING, testName);
        EParameter parameter2 = new EParameter("N", "Number of elements", ParameterType.INT, size);
        EParameter parameter3 = new EParameter("Type", "Data type of the test", ParameterType.STRING, type);
        FourierTransformTestCase testCase = new FourierTransformTestCase();
        // ID
        testCase.addParameter(testIDPar);
        // input parameters
        testCase.addParameter(parameter1);
        testCase.addParameter(parameter2);
        testCase.addParameter(parameter3);
        ParsedTestData testData = parseData(type, fields, size);

        if (testData != null) {
            EParameter parameter4 = new EParameter("MaxAError", "Maximum allowed error (maximum absolute allowed difference from the actual value)", ParameterType.DOUBLE, testData.minimumAllowedError);
            testCase.addParameter(parameter4, true);
            testCase.input = testData.inputData;
            testCase.solution = testData.solution;
            testCase.minAllowedError = testData.minimumAllowedError;
            return testCase;
        } else {
            return null;
        }
    }

    private ParsedTestData parseData(String type, String[] fields, int size) {
        Complex[] inputData = new Complex[size];
        Complex[] algorithmSolution = new Complex[size];
        double minimumError = 0;
        if (fields.length < 5) {
            String message = String.format("not enough fields, min: %d, found: %d", 5, fields.length);
            reportInvalidDataFormat(message);
            return null;
        }
        String errorString = fields[3];
        try {
            String commaReplacedWithDot = replaceCommaDot(errorString);
            minimumError = Double.parseDouble(commaReplacedWithDot);
        } catch (Exception e) {
            reportInvalidDataFormat("invalid number input of minimum error, allowed double, found: " + errorString);
            return null;
        }
        if (type.equalsIgnoreCase("INLINE")) {
            if (fields.length < 6) {
                String message = String.format("not enough fields for INLINE type, min: %d, found: %d", 6, fields.length);
                reportInvalidDataFormat(message);
                return null;
            }
            String[] data = fields[4].split(TEST_CASE_NUMBERS_DELIMITER);
            if (data.length != size) {
                String message = String.format("invalid number of input data, required: %d, actual: %d", size, data.length);
                reportInvalidDataFormat(message);
                return null;
            }
            String[] solution = fields[5].split(TEST_CASE_NUMBERS_DELIMITER);
            if (solution.length != size) {
                String message = String.format("invalid number of algorithm solution data, required: %d, actual: %d", size, solution.length);
                reportInvalidDataFormat(message);
                return null;
            }
            for (int i = 0; i < data.length; i++) {
                try {
                    String commaReplacedWithDot = replaceCommaDot(data[i]);
                    inputData[i] = new Complex(Double.parseDouble(commaReplacedWithDot), 0);
                } catch (Exception e) {
                    reportInvalidDataFormat("invalid type of inline input data - data[" + i + "]: " + data[i]);
                    return null;
                }
                String[] solutionPoint = solution[i].split(TEST_CASE_COMPLEX_NUMBERS_DELIMITER);
                if (solutionPoint.length != 2) {
                    String message = String.format("invalid format of algorithm solution data, "
                            + "must contain real and complex part (i.e. 2" + TEST_CASE_COMPLEX_NUMBERS_DELIMITER + "-1), "
                            + "required: %d, actual: %d", 2, solutionPoint.length);
                    reportInvalidDataFormat(message);
                    return null;
                } else {
                    String realPart = solutionPoint[0];
                    String imaginaryPart = solutionPoint[1];
                    try {
                        String commaReplacedWithDot = replaceCommaDot(realPart);
                        double real = Double.parseDouble(commaReplacedWithDot);
                        commaReplacedWithDot = replaceCommaDot(imaginaryPart);
                        double imaginary = Double.parseDouble(commaReplacedWithDot);
                        algorithmSolution[i] = new Complex(real, imaginary);
                    } catch (Exception e) {
                        reportInvalidDataFormat("invalid type of inline agorithm solution data - data[" + i + "]: " + solution[i]);
                        return null;
                    }
                }
            }
        } else if (type.equalsIgnoreCase("IDENTICAL")) {
            if (fields.length < 5) {
                String message = String.format("not enough fields for IDENTICAL type, min: %d, found: %d", 5, fields.length);
                reportInvalidDataFormat(message);
                return null;
            }
            String numberString = fields[4];
            try {
                String commaReplacedWithDot = replaceCommaDot(numberString);
                double number = Double.parseDouble(commaReplacedWithDot);
                for (int i = 0; i < inputData.length; i++) {
                    inputData[i] = new Complex(number, 0);
                    algorithmSolution[i] = new Complex(0, 0);
                }
                algorithmSolution[0] = new Complex(number * inputData.length, 0);
            } catch (Exception e) {
                reportInvalidDataFormat("invalid type of IDENTICAL number (required double): " + numberString);
                return null;
            }
        } else if (type.equalsIgnoreCase("ALTERNATE")) {
            if (fields.length < 5) {
                String message = String.format("not enough fields for ALTERNATE type, min: %d, found: %d", 5, fields.length);
                reportInvalidDataFormat(message);
                return null;
            }
            if (size % 2 != 0) {
                String message = String.format("data size for ALTERNATE type must be even, input: %d", size);
                reportInvalidDataFormat(message);
                return null;
            }
            String numberString = fields[4];
            try {
                String commaReplacedWithDot = replaceCommaDot(numberString);
                double number = Double.parseDouble(commaReplacedWithDot);
                for (int i = 0; i < inputData.length; i++) {
                    if (i % 2 == 0) {
                        inputData[i] = new Complex(number, 0);
                    } else {
                        inputData[i] = new Complex(-1 * number, 0);
                    }
                    algorithmSolution[i] = new Complex(0, 0);
                }
                algorithmSolution[inputData.length / 2] = new Complex(number * inputData.length, 0);
            } catch (Exception e) {
                reportInvalidDataFormat("invalid type of ALTERNATE number (required double): " + numberString);
                return null;
            }
        }
        return new ParsedTestData(minimumError, inputData, algorithmSolution);
    }

    private void reportInvalidDataFormat(String note) {
        String msg = String.format("Invalid input data in file %s in line %d.", testFileName, lineNumber);
        if (!note.isEmpty()) {
            msg += " (" + note + ")";
        }

        ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, msg);
    }

    // TEST
    public static void main(String args[]) {
        String dataroot = System.getenv("ALGATOR_DATA_ROOT"); // a folder with the "projects" folder
        String projName = "FourierTransform";

        List<ETestSet> testSets = getTestSetsFromProject(dataroot, projName);
        if (testSets.isEmpty()) {
            System.out.println("No test sets found in project: " + projName);
        } else {
            for (ETestSet testSet : testSets) {
                FourierTransformTestSetIterator testSetIterator = new FourierTransformTestSetIterator();
                testSetIterator.setTestSet(testSet);

                iterateAndPrintTests(testSetIterator);
            }
        }
    }

    private static List<ETestSet> getTestSetsFromProject(String dataroot, String projName) {
        List<ETestSet> output = new ArrayList<>();
        File testDIR = new File(dataroot + File.separatorChar + "projects" + File.separatorChar + "PROJ-" + projName + File.separatorChar + "tests");
        if (testDIR.isDirectory() && testDIR.canRead()) {
            File[] files = testDIR.listFiles(createTestFileNameFilter());
            for (File testSet : files) {
                output.add(new ETestSet(testSet));
            }
        }
        return output;
    }

    private static void iterateAndPrintTests(FourierTransformTestSetIterator iterator) {
        int count = 0;
        System.out.println("---------------------------START---------------------------");
        System.out.println("Test Set Name: " + iterator.testFileName);
        while (iterator.hasNext()) {
            iterator.readNext();
            TestCase testCase = iterator.getCurrent();
            System.out.println("Test case[" + count + "]: " + testCase);
            count += 1;
        }
        if (count == 0) {
            System.out.println("test set was empty");
        }
        System.out.println("---------------------------END------------------------------\n\n");
    }

    private static FilenameFilter createTestFileNameFilter() {
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return dir.getName().equalsIgnoreCase("tests") && name.endsWith(".atts");
            }
        };
        return filter;
    }

    private static class ParsedTestData {

        public final Complex[] inputData;
        public final Complex[] solution;
        public final double minimumAllowedError;

        public ParsedTestData(double minAllowedError, Complex[] algorithmInputData, Complex[] algorithmSolution) {
            inputData = algorithmInputData;
            solution = algorithmSolution;
            minimumAllowedError = minAllowedError;
        }

    }
}
