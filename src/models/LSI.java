/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Theresa
 */
public class LSI {
     
    public  double [][] getAlpha(){
   
         ArrayList<String> docs = getTestData();

     
        ArrayList<String> voc = getVocabulary();

        int[][] data = computeDataMatrix(docs,voc);
        ArrayList<Double> idf = new LSA().computeIDF(docs,voc);
        
         double[][] tf_idf = new LSA().computeTFIDF(idf, data);

         Matrix mat = new Matrix(tf_idf);

        SingularValueDecomposition svd = mat.svd();

        Matrix U = svd.getU();
        double[][] u = U.getArray();
           u = convertMatrixtoPLSA(u);
           return u;
    }
     public  double[][] getBeta(){
    
         ArrayList<String> docs = getTestData();
        ArrayList<String> voc = getVocabulary();

        int[][] data = computeDataMatrix(docs,voc);
        ArrayList<Double> idf = new LSA().computeIDF(docs,voc);
        
         double[][] tf_idf = new LSA().computeTFIDF(idf, data);

         Matrix mat = new Matrix(tf_idf);

        SingularValueDecomposition svd = mat.svd();

        Matrix V = svd.getV();
        double[][] v = V.getArray();
           v= convertMatrixtoPLSA(v);
           
          return v;
    }
    
      public static double[][] convertMatrixtoPLSA(double[][] mat)
    {
        for(int i=0; i<mat.length;i++)
        { double norm=0.0;
            for(int j=0; j<mat[0].length;j++)
            { double sum=0.0;
                for(int k= 0; k<mat.length;k++)
                {
                    sum = Math.exp(Math.pow(mat[k][j], 2));
                }
              mat[i][j] = Math.exp(Math.pow(mat[i][j], 2)) / sum;

               norm += mat[i][j];
            }

          for (int j = 0; j <mat[0].length;j++)
            {
            	mat[i][j] /= norm;
            }
        }

        return mat;
    }

    
    
     public ArrayList<String> getTestData()
    {
        ArrayList<String> td= new ArrayList<>();
        
        try{
      BufferedReader  br = new BufferedReader(new FileReader("test/corpus.txt"));
      for (String doc; (doc = br.readLine()) != null;) {
      br.readLine();
      String[] words = doc.trim().split("\\s+");
      String word= words.toString();
       td.add(word);
      }
        }catch(Exception ex){}
      // M = td.size();
       
        return td;
    }

      public ArrayList<String> getVocabulary()
    {
       int vocabularySize=0;
       HashMap<Integer, String> id2WordVocabulary= new HashMap<>();
       HashMap<String,Integer> word2IdVocabulary= new HashMap<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader("test/corpus.txt"));
           int indexWord = -1;
            for (String doc; (doc = br.readLine()) != null;) {

            if (doc.trim().length() == 0)
                    continue;

            String[] words = doc.trim().split("\\s+");
            List<Integer> document = new ArrayList<Integer>();

            for (String word : words) {
                    if (word2IdVocabulary.containsKey(word)) {
                            document.add(word2IdVocabulary.get(word));
                    }
                    else {
            indexWord += 1;
            word2IdVocabulary.put(word, indexWord);
            id2WordVocabulary.put(indexWord, word);
            document.add(indexWord);
            
            }
          }
            }
        }catch(Exception ex){}

        vocabularySize = word2IdVocabulary.size();
       
        ArrayList<String> voc=new ArrayList<>();
        for (int id = 0; id < vocabularySize; id++)
             voc.add(id2WordVocabulary.get(id));
        return voc;
    }

      public int[][] computeDataMatrix(ArrayList<String> docs,ArrayList<String> voc)
    {
        LSA r=new LSA();
        String doc ="",word="";
        int f=0;

        int[][] data = new int[docs.size()][voc.size()];

        for(int i=0; i<docs.size(); i++)
        {   doc = docs.get(i);

            for(int j=0; j<voc.size();j++)
            {
                word = voc.get(j);
               f =  r.wordFreq(word, doc);
               data[i][j] = f;
            }

        }
        return data;
    }

}
