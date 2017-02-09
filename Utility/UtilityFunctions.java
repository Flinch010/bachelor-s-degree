/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fouriertransformadmin;

import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author Ziga
 */
public class UtilityFunctions {

    public static String replaceCommaDot(String input) {
        return input.replaceAll(",", ".");
    }

    public static String arrayToString(Object[] array) {
        String output = "";
        for (Object obj : array) {
            output += obj + ",\t";
        }
        if (output.length() > 2) {
            output = output.substring(0, output.length() - 2);
        }
        return output;
    }

    /**
     *
     * @param input array to reorder
     * @param powerOfTwo number of required bits to represent the last index of
     * the input array
     * @return reverse bit ordered array
     */
    public static Complex[] reverseBitOrder(Complex[] input, int powerOfTwo) {
        Complex[] output = new Complex[input.length];
        for (int i = 0; i < input.length; i++) {
            int j = Integer.reverse(i) >>> (32 - powerOfTwo);
            if (j > i) {
                output[i] = input[j];
                output[j] = input[i];
            } else if (j == i) {
                output[i] = input[i];
            }
        }
        return output;
    }
    
    public static double calculateLogarithm(int x, int base) {
        return Math.log10(x) / Math.log10(base);
    }
}
