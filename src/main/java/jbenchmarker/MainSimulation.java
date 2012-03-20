/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jbenchmarker;

import crdt.CRDT;
import crdt.Factory;
import crdt.simulator.CausalSimulator;
import crdt.simulator.Trace;
import crdt.simulator.random.RandomTrace;
import crdt.simulator.random.StandardSeqOpProfile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import jbenchmarker.core.MergeAlgorithm;

/**
 *
 * @author score
 */
public class MainSimulation {

    static int base = 100;

    public static void main(String[] args) throws Exception {

        if (args.length < 12) {
            System.err.println("Arguments :");
            System.err.println("- Factory : a jbenchmaker.core.ReplicaFactory implementation ");
            System.err.println("- Number of execu : ");
            System.err.println("- duration : ");
            System.err.println("- perIns : ");
            System.err.println("- perBlock : ");
            System.err.println("- avgBlockSize : ");
            System.err.println("- sdvBlockSize : ");
            System.err.println("- probability : ");
            System.err.println("- delay : ");
            System.err.println("- sdv : ");
            System.err.println("- replicas : ");
            System.err.println("- thresold : ");
            System.exit(1);
        }

        Factory<CRDT> rf = (Factory<CRDT>) Class.forName(args[0]).newInstance();
        int nbExec = Integer.valueOf(args[1]);
        int nb = 1;
        if (nbExec > 1) {
            nb = nbExec + 1;
        }
        long duration = Long.valueOf(args[2]);
        double perIns = Double.valueOf(args[3]);
        double perBlock = Double.valueOf(args[4]);
        int avgBlockSize = Integer.valueOf(args[5]);
        double sdvBlockSize = Double.valueOf(args[6]);
        double probability = Double.valueOf(args[7]);
        long delay = Long.valueOf(args[8]);
        double sdv = Double.valueOf(args[9]);
        int replicas = Integer.valueOf(args[10]);
        int thresold = Integer.valueOf(args[11]);
        int scaleMemory = Integer.valueOf(args[12]);
        long ltime[][] = null, rtime[][] = null, mem[][] = null;
        int minSizeGen = 0, minSizeInteg = 0;
        int cop = 0, uop = 0;
        String[] nameUsr = args[0].split("\\.");
        
        for (int ex = 0; ex < nbExec; ex++) {
            Trace trace = new RandomTrace(duration, RandomTrace.FLAT,
                    new StandardSeqOpProfile(perIns, perBlock, avgBlockSize, sdvBlockSize), probability, delay, sdv, replicas);
            CausalSimulator cd = new CausalSimulator(rf);
            cd.runWithMemory(trace, scaleMemory);

            if (ltime == null) {
                cop = ((MergeAlgorithm)cd.getReplicas().get(0)).getHistory().size();
                uop = cd.replicaGenerationTimes().size();
                ltime = new long[nb][uop];
                rtime = new long[nb][cop];
                minSizeGen = uop;
                minSizeInteg = cop;
            }
            
            List<Long> l = cd.replicaGenerationTimes();
            if(l.size() < minSizeGen)
                minSizeGen = l.size();
            toArrayLong(ltime[ex], l, minSizeGen);  
            

            for (int r : cd.replicaRemoteTimes().keySet()) {
                if (minSizeInteg > ((MergeAlgorithm) cd.getReplicas().get(0)).getHistory().size()) {
                    minSizeInteg = ((MergeAlgorithm) cd.getReplicas().get(0)).getHistory().size();
                }
                for (int i = 0; i < minSizeInteg; i++) {
                    rtime[ex][i] += cd.replicaRemoteTimes().get(r).get(i);
                }
            }
            for (int i = 0; i < minSizeInteg; i++) {
                rtime[ex][i] /= replicas;
            }

            cd = null;
            trace = null;
            System.gc();
        }

        String file = writeToFile(ltime, nameUsr[1], "usr", minSizeGen);
        treatFile(nameUsr[1], file, "usr");
        String file2 = writeToFile(rtime, nameUsr[1], "local", minSizeInteg);
        treatFile(nameUsr[1], file2, "local");
        
//        String file3 = writeToFile(mem, nameUsr[1], "mem");
//        treatFile(nameUsr[1], file3, "mem");
    }
    
    
     private static void toArrayLong(long[] t, List<Long> l, int minSize) {
        for (int i = 0; i < minSize-1; i++) {
            t[i] = l.get(i);
        }
    }
     
     /**
     * Write all array in a file
     */
    private static String writeToFile(long[][] data, String algo, String type, int minSize) throws IOException {
        String nameFile = algo + '-' + type + ".res";
        BufferedWriter out = new BufferedWriter(new FileWriter(nameFile));
        for (int op = 0; op < minSize; op++) {
            for (int ex = 0; ex < data.length; ex++) {
                out.append(data[ex][op] + "\t");
            }
            out.append("\n");
        }
        out.close();
        return nameFile;
    }
    
    
    static void treatFile(String Algo,String File,String result) throws IOException
    {
        int Tmoyen = 0;
        int cmpt = 0;
        String Line;
        String fileName = Algo+"-"+result+".data";
        PrintWriter ecrivain =  new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        InputStream ips1=new FileInputStream(File);
        InputStreamReader ipsr1=new InputStreamReader(ips1);
        BufferedReader br1=new BufferedReader(ipsr1);
        try{
           Line= br1.readLine();
            while (Line != null)
            {
                for(int i=0 ; i<base; i++)
                {
                    if(Line != null)
                    {
                        Tmoyen += getLastValue(Line);
                        Line=  br1.readLine();
                    	cmpt++;
                    }
                    else
                        break;
                }
               Tmoyen = Tmoyen/cmpt;
               float tMicro = Tmoyen/1000; // microSeconde
               ecrivain.println(tMicro);
               Tmoyen=0;cmpt = 0;
            }
             br1.close(); 
             ecrivain.close();
          }		
          catch (Exception e){
                System.out.println(e.toString());
        }
        
    }
    static int getLastValue(String ligne)
    {
       String tab[] = ligne.split("\t");
       float t = Float.parseFloat(tab[(tab.length)-1]);
       return ((int)t);
    }
    

    public static void computeAverage(long[][] data, double thresold, int minSize) {
        int nbExpe = data.length - 1;//une colonne réserver à la moyenne
        for (int op = 0; op < minSize; op++) {
            long sum = 0;
            for (int ex = 0; ex < nbExpe; ex++) { // calculer moyenne de la ligne
                sum += data[ex][op];
            }
            long moy = sum / nbExpe, sum2 = 0, k = 0;
            for (int ex = 0; ex < nbExpe; ex++) {
                if (data[ex][op] < thresold * moy) {
                    sum2 += data[ex][op];
                    k++;
                }
            }
            if(k != 0)
                data[nbExpe][op] = sum2 /k;
        }
    }
    
    
}