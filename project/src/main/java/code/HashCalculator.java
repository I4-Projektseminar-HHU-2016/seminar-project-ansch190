package code;

import java.util.concurrent.Callable;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die den Hash in Threads berechnet.
 */
public class HashCalculator implements Callable<String> {

    public HashCalculator(double[][] data){
        vals = data;
        initCoefficients();
    }

    @Override
    public String call() throws Exception{
        return hash();
    }

    //--- Allgemein ---//

    private double[][] vals;

    private static int size = 32;
    private static int smallerSize = 8;

    //--- Hashing ---//

    private String hash(){

        //double[][] dctVals = applyDCT(vals);
        vals = applyDCT(vals);

        double total = 0;

        for(int x = 0; x < smallerSize; x++){
            for(int y = 0; y < smallerSize; y++){
                total += vals[x][y];
            }
        }
        total -= vals[0][0];

        double avg = total / (double) ((smallerSize * smallerSize) - 1);

        String hash = "";

        for(int x = 0; x < smallerSize; x++){
            for(int y = 0; y < smallerSize; y++){
                if(x != 0 && y != 0){
                    hash += (vals[x][y] > avg ? "1" : "0");
                }
            }
        }

        return hash;
    }

    //--- DCT ---//

    private double[] c;

    private void initCoefficients(){
        c = new double[size];

        for(int i = 1; i < size; i++){
            c[i] = 1;
        }
        c[0] = 1 / Math.sqrt(2.0);
    }

    private double[][] applyDCT(double[][] f){
        int N = size;

        double[][] F = new double[N][N];
        for(int u = 0; u < N; u++){
            for(int v = 0; v < N; v++){
                double sum = 0.0;
                for(int i = 0; i < N; i++){
                    for(int j = 0; j < N; j++){
                        sum += Math.cos(((2 * i + 1) / (2.0 * N)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * N)) * v * Math.PI) * (f[i][j]);
                    }
                }
                sum *= ((c[u] * c[v]) / 4.0);
                F[u][v] = sum;
            }
        }
        return F;
    }

}
