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
   
     /**
     *
     * @return k & n for best fit: y(x) = k * x + n
     */
    public static double[] calculateLinearRegresion(double[] x, double[] y) {
        double[] kAndN = new double[2];
        if (x.length != y.length || x.length <= 1) {
            throw new IllegalArgumentException("Length of input arrays must be greater than one and equal");
        }
        double koeficient;
        double n;
        if (x.length == 2) {
            koeficient = calculateLineKoeficient(x[0], x[1], y[0], y[1]);
            n = y[0] - koeficient * x[0];
        } else {
            double sx = 0;
            double sy = 0;
            double sxx = 0;
            double sxy = 0;
            for (int i = 0; i < x.length; i++) {
                sxx += x[i] * x[i];
                sxy += x[i] * y[i];
                sy += y[i];
                sx += x[i];
            }
            koeficient = (x.length * sxy - sx * sy) / (x.length * sxx - sx * sx);
            n = (sy * sxx - sx * sxy) / (x.length * sxx - sx * sx);
        }
        kAndN[0] = koeficient;
        kAndN[1] = n;
        return kAndN;
    }

    public static double calculateLineKoeficient(double x1, double x2, double y1, double y2) {
        return (y2 - y1) / (x2 - x1);
    }

    /**
     *
     * @return a & b koeficients for log fit y(x) = a + b * ln(x)
     */
    public double[] calculateLogFit(double[] x, double[] y) {
        if (x.length != y.length || x.length <= 1) {
            throw new IllegalArgumentException("Length of input arrays must be greater than one and equal");
        }
        double sxx = 0;
        double sxy = 0;
        double sx = 0;
        double sy = 0;
        for (int i = 0; i < x.length; i++) {
            double lnx = calculateLogarithm(x[i], Math.E);
            sxx += lnx * lnx;
            sxy += lnx * y[i];
            sy += y[i];
            sx += lnx;
        }
        double b = (x.length * sxy - sy * sx) / (x.length * sxx - sx * sx);
        double a = (sy - b * sx) / x.length;
        return new double[]{a, b};
    }

    /**
     *
     * @return a & b koeficients for log fit y(x) = a + b * x * ln(x)
     */
    public double[] calculateNLogFit(double[] x, double[] y) {
        if (x.length != y.length || x.length <= 1) {
            throw new IllegalArgumentException("Length of input arrays must be greater than one and equal");
        }
        double sxx = 0;
        double sxy = 0;
        double sx = 0;
        double sy = 0;
        for (int i = 0; i < x.length; i++) {
            double lnx = x[i] * calculateLogarithm(x[i], Math.E);
            sxx += lnx * lnx;
            sxy += lnx * y[i];
            sy += y[i];
            sx += lnx;
        }
        double b = (x.length * sxy - sy * sx) / (x.length * sxx - sx * sx);
        double a = (sy - b * sx) / x.length;
        return new double[]{a, b};
    }
    
     /*
     number of recursive calls = sum of first P terms of geometric series 4, 16, 64... (terms of this series are possible input data lenghts (n))
     this sum, which is equal to number of recursive calls, can also be interpreted as number of nodes in a quadtree (excluding top node)
     ;where P = log_4(n) ; n - input data length
     a0 = 4, r = 4
     sum of first P terms in geometric series = a0 * (1 - r^P)/(1 - r);
     */
    public static double calculateNumCALLRadix4(int n) {
        //n = 4^P
        return 4 * (1 - n) / (1 - 4);
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
        //n = 2^P
        return 2 * (1 - n) / (1 - 2);
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
        if (n > 1) {
            double a0 = 2;
            sum = a0;
            if (n > 2) {
                double aKminusTwo = a0;
                double aKminusOne = 5;
                sum = aKminusOne;
                for (int i = 8; i <= n; i *= 2) {
                    sum = 3 + 2 * aKminusTwo + aKminusOne;
                    aKminusTwo = aKminusOne;
                    aKminusOne = sum;
                }
            }
        }
        return sum;
    }
}
