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
    
     /*
     number of recursive calls = sum of first P terms of geometric series 4, 16, 64... (terms of this series are possible input data lenghts (n))
     this sum, which is equal to number of recursive calls, can also be interpreted as number of nodes in a quadtree (excluding top node)
     ;where P = log_4(n) ; n - input data length
     a0 = 4, r = 4
     sum of first P terms in geometric series = a0 * (1 - r^P)/(1 - r);
     */
    public static double calculateNumCALLRadix4(int n) {
        double depth = calculateLogarithm(n, 4);
        return 4 * (1 - Math.pow(4, depth)) / (1 - 4);
    }

    /*
     same principle as in radix-4 recursive call calculation
     2, 4, 8, 16,...
     a0 = 2, r = 2
     */
    public static double calculateNumCALLRadix2(int n) {
        //or if we want to write it as a sum of number of nodes in binary tree (excluding top node)
        //( 2^(log_2(n) + 1) - 2
        //return (n << 1) - 2;
        double depth = calculateLogarithm(n, 2);
        return 2 * (1 - Math.pow(2, depth)) / (1 - 2);
    }

    /*
    NRC - number of recursive calls for Split radix algoritm
    
    n must be a power of 2, ie. 2^t
    
               2, if n = 2
    NRC(n) = { 5, if n = 4
               2 * NRC(n/4) + NRC(n/2) + 3, if n > 4
    
    */
    public static double calculateNumCALLSplitRadix(int n) {
        double sum = 0;
        double powerOfTwo = calculateLogarithm(n, 2);
        if (powerOfTwo > 0) {
            double a0 = 2;
            sum = a0;
            if (powerOfTwo > 1) {
                double aKminusTwo = a0;
                double aKminusOne = 5;
                sum = aKminusOne;
                for (int i = 3; i <= powerOfTwo; i++) {
                    sum = 3 + 2 * aKminusTwo + aKminusOne;
                    aKminusTwo = aKminusOne;
                    aKminusOne = sum;
                }
            }
        }
        return sum;
    }
}
