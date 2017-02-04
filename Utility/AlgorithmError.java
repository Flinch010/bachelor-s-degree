/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fouriertransformadmin;

/**
 *
 * @author Ziga
 */
public class AlgorithmError {
    
    public final double min;
    public final double max;
    public final double average;
    
    public AlgorithmError(double minError, double maxError, double averageError) {
        this.max = maxError;
        this.min = minError;
        this.average = averageError;
    }
    
    
}
