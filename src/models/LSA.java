 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;


import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author franco
 */
public class LSA
{

    public double[][] computeTFIDF(ArrayList<Double> IDF, int DataMatrix[][])
    {
        int M = DataMatrix.length;
        int V =DataMatrix[0].length;
        double[][] tf_idf = new double[M][V];

        for(int i =0; i<M;i++)
        { double norm = 0.0;
            for(int j=0;j<V;j++)
            {
               tf_idf[i][j] =  DataMatrix[i][j]*IDF.get(j);
                norm += tf_idf[i][j];
            }
            //Normalization
            if(norm==0.0)
                norm = 1.0;
             for (int j = 0; j < V; j++)
            {
            	tf_idf[i][j] /= norm;
            }
        }

        return tf_idf;
    }



    public ArrayList<Double> computeIDF(ArrayList<String> docs,ArrayList<String> voc)
    {
        int D = docs.size();
        ArrayList<Double> IDF = new ArrayList<Double>();
        double div = 0.0,idf=0.0;
         String doc ="",term="";

         for(int i=0; i<voc.size();i++)
         {  int count =0;
             term = voc.get(i);
             for(int j=0;j<docs.size();j++)
             {
                doc = docs.get(j);

                if(wordFreq(term,doc)>0)
                 count++;
             }
             div = (double) D/ (double) (1+count);
             idf = Math.log10(div);

             if(idf<0.0)
                 idf = 0.0;

             IDF.add(idf);
             
         }


        return IDF;
    }
    public int wordFreq(String w, String sentence)
    {
        w = cleanWord(w);
        int count =0;
        Scanner scan = new Scanner(sentence);
        while(scan.hasNext())
        {
            String n = scan.next();
            n = cleanWord(n);
            if(n.equals(w))
            {
                count++;
            }
        }
        return count;
    }

         public String cleanWord(String str)
    {
                str = strip(str);
                str = str.toLowerCase();


              return str;
    }

    public String strip(String s)
    {
        if(s.endsWith(".")||s.endsWith(",")||s.endsWith(";")||s.endsWith("?")||s.endsWith("!")
                ||s.endsWith(":")||s.endsWith(")")||s.startsWith("("))
            s = s.substring(0,s.length()-1);

        return s.toLowerCase();
    }

   

}
