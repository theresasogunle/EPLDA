package models;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import utility.FuncUtils;
public class EPLDA
{
	public double [][] alphaArr; // Hyper-parameter alpha
	public double[][] betaArr; // Hyper-parameter alpha
        
        public double alpha;
        public double beta;
        
	public int numTopics; // Number of topics
	public int numberOfIterations; // Number of Gibbs sampling iterations
	public int topWords; // Number of most probable words for each topic

	public double alphaSum; // alpha * numTopics
	public double betaSum; // beta * vsize

	public List<List<Integer>> corpus; // Word ID-based corpus
	public List<List<Integer>> topicAssignments; // Topics assignments for words
													// in the corpus
	public int numberOfDocuments; // Number of documents in the corpus
	public int numWordsInCorpus; // Number of words in the corpus

	public HashMap<String, Integer> word2IdVocabulary; // Vocabulary to get ID
														// given a word
	public HashMap<Integer, String> id2WordVocabulary; // Vocabulary to get word
														// given an ID
	public int vsize; // The number of word types in the corpus

	// numberOfDocuments * numTopics matrix
	// Given a document: number of its words assigned to each topic
	public int[][] documentTopicCount;
	// Number of words in every document
	public int[] sumDocumentTopicCount;
	// numTopics * vsize matrix
	// Given a topic: number of times a word type assigned to the topic
	public int[][] topicWordCount;
	// Total number of words assigned to a topic
	public int[] sumTopicWordCount;

	// Double array used to sample a topic
	public double[] sampleTopic;

	// Path to the directory containing the corpus
	public String folderPath;
	// Path to the topic modeling corpus
	public String corpusPath;

	public String expName = "LDAmodel";
	public String orgExpName = "LDAmodel";
	public String topicAssignmentFilePath = "";
	public int savestep = 0;
   
	public EPLDA(String pathToCorpus, int inNumTopics,
		double[][] inAlpha, double[][] inBeta, int inNumIterations, int inTopWords)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, "LDAmodel");
	}

	public EPLDA(String pathToCorpus, int inNumTopics,
		double [][]inAlpha, double [][]inBeta, int inNumIterations, int inTopWords,
		String inExpName)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, inExpName, "", 0);
	}

	public EPLDA(String pathToCorpus, int inNumTopics,
		double[][] inAlpha, double[][] inBeta, int inNumIterations, int inTopWords,
		String inExpName, String pathToTAfile)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, inExpName, pathToTAfile, 0);
	}

	public EPLDA(String pathToCorpus, int inNumTopics,
		double[][] inAlpha, double[][] inBeta, int inNumIterations, int inTopWords,
		String inExpName, int inSaveStep)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, inExpName, "", inSaveStep);
	}

	public EPLDA(String pathToCorpus, int inNumTopics,
		double [][] inAlpha, double[][] inBeta, int inNumIterations, int inTopWords,
		String inExpName, String pathToTAfile, int inSaveStep)
		throws Exception
	{
		alphaArr = inAlpha;
		betaArr = inBeta;
		numTopics = inNumTopics;
		numberOfIterations = inNumIterations;
		topWords = inTopWords;
		savestep = inSaveStep;
		expName = inExpName;
		orgExpName = expName;
		corpusPath = pathToCorpus;
		folderPath = pathToCorpus.substring(
			0,
			Math.max(pathToCorpus.lastIndexOf("/"),
				pathToCorpus.lastIndexOf("\\")) + 1);

		System.out.println("Reading topic modeling corpus: " + pathToCorpus);

		word2IdVocabulary = new HashMap<String, Integer>();
		id2WordVocabulary = new HashMap<Integer, String>();
		corpus = new ArrayList<List<Integer>>();
		numberOfDocuments = 0;
		numWordsInCorpus = 0;

		BufferedReader br = null;
		try {
			int indexWord = -1;
			br = new BufferedReader(new FileReader(pathToCorpus));
			for (String doc; (doc = br.readLine()) != null;) {
                                   
				if (doc.trim().length() == 0)
					continue;
                              
                                
				String []words = doc.trim().split("\\s+");
                           
                               
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

				numberOfDocuments++;
				numWordsInCorpus += document.size();
				corpus.add(document);
			
                        }
                }
		catch (Exception e) {
			e.printStackTrace();
		}

		vsize = word2IdVocabulary.size(); // vsize = indexWord
		documentTopicCount = new int[numberOfDocuments][numTopics];
		topicWordCount = new int[numTopics][vsize];
		sumDocumentTopicCount = new int[numberOfDocuments];
		sumTopicWordCount = new int[numTopics];

		sampleTopic = new double[numTopics];
		for (int i = 0; i < numTopics; i++) {
			sampleTopic[i] = 1.0 / numTopics;
		}
            for (double[] alphaArr1 : alphaArr) {
                for (int j = 0; j < alphaArr1.length; j++) {
                    alpha = Math.abs(alphaArr1[j]);
                }
            } 
            for (double[] betaArr1 : betaArr) {
                for (int j = 0; j < betaArr1.length; j++) {
                    beta = betaArr1[j];
                }
            } 
		alphaSum = numTopics * alpha;
		betaSum = vsize * beta;

		System.out.println("Corpus size: " + numberOfDocuments + " docs, "
			+ numWordsInCorpus + " words");
		System.out.println("Vocabuary size: " + vsize);
		System.out.println("Number of topics: " + numTopics);
		System.out.println("alpha: " + alpha);
		System.out.println("beta: " + beta);
		System.out.println("Number of sampling iterations: " + numberOfIterations);
		System.out.println("Number of top topical words: " + topWords);
			
                //topic assignments
                init();
	}

	
	public void init()
		throws IOException
	{
		System.out.println("Randomly assigning topics ...");

		topicAssignments = new ArrayList<List<Integer>>();

		for (int i = 0; i < numberOfDocuments; i++) {
			List<Integer> topics = new ArrayList<Integer>();
			int docSize = corpus.get(i).size();
			for (int j = 0; j < docSize; j++) {
				int topic = FuncUtils.nextDiscrete(sampleTopic); // Sample a topic
				// Increase counts
				documentTopicCount[i][topic] += 1;
				topicWordCount[topic][corpus.get(i).get(j)] += 1;
				sumDocumentTopicCount[i] += 1;
				sumTopicWordCount[topic] += 1;

				topics.add(topic);
			}
			topicAssignments.add(topics);
		}
	}

	
	

	public void estimate()
		throws IOException
	{
		writeParameters();
		

		System.out.println(" Using Gibbs sampling: ");

		for (int iter = 1; iter <= numberOfIterations; iter++) {

			System.out.println("\tSampling iteration: " + (iter));
			// System.out.println("\t\tPerplexity: " + computePerplexity());

			sampleInSingleIteration();

			if ((savestep > 0) && (iter % savestep == 0)
				&& (iter < numberOfIterations)) {
				System.out.println("\t\tSaving the output from the " + iter
					+ "^{th} sample");
				expName = orgExpName + "-" + iter;
				write();
			}
		}
		expName = orgExpName;

		System.out.println("Writing output from the last sample ...");
		write();

		System.out.println("Sampling completed!");

	}

	public void sampleInSingleIteration()
	{
           
		for (int dIndex = 0; dIndex < numberOfDocuments; dIndex++) {
			int docSize = corpus.get(dIndex).size();
			for (int wIndex = 0; wIndex < docSize; wIndex++) {
				// Get current word and its topic
				int topic = topicAssignments.get(dIndex).get(wIndex);
				int word = corpus.get(dIndex).get(wIndex);

				// Decrease counts
				documentTopicCount[dIndex][topic] -= 1;
				// docTopicSum[dIndex] -= 1;
				topicWordCount[topic][word] -= 1;
				sumTopicWordCount[topic] -= 1;
                            for (double[] alphaArr1 : alphaArr) {
                                for (int j = 0; j < alphaArr1.length; j++) {
                                    alpha = Math.abs(alphaArr1[j]);
                                }
                            } 
                            for (double[] betaArr1 : betaArr) {
                                for (int j = 0; j < betaArr1.length; j++) {
                                    beta = betaArr1[j];
                                }
                            } 
				// Sample a topic
				for (int tIndex = 0; tIndex < numTopics; tIndex++) {
					sampleTopic[tIndex] = (documentTopicCount[dIndex][tIndex] + alpha)
						* ((topicWordCount[tIndex][word] + beta) / (sumTopicWordCount[tIndex] + betaSum));
					
				}
				topic = FuncUtils.nextDiscrete(sampleTopic);

				// Increase counts
				documentTopicCount[dIndex][topic] += 1;
				// docTopicSum[dIndex] += 1;
				topicWordCount[topic][word] += 1;
				sumTopicWordCount[topic] += 1;

				// Update topic assignments
				topicAssignments.get(dIndex).set(wIndex, topic);
			}
		}
	}

	public void writeParameters()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".paras"));
		writer.write("-model" + "\t" + "epLDA");
		writer.write("\n-corpus" + "\t" + corpusPath);
		writer.write("\n-ntopics" + "\t" + numTopics);
		writer.write("\n-alpha" + "\t" + alpha);
		writer.write("\n-beta" + "\t" + beta);
		writer.write("\n-niters" + "\t" + numberOfIterations);
		writer.write("\n-twords" + "\t" + topWords);
		writer.write("\n-name" + "\t" + expName);
		if (topicAssignmentFilePath.length() > 0)
			writer.write("\n-initFile" + "\t" + topicAssignmentFilePath);
		if (savestep > 0)
			writer.write("\n-sstep" + "\t" + savestep);

		writer.close();
	}

	

	public void writeTopicAssignments()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".topicAssignments"));
		for (int dIndex = 0; dIndex < numberOfDocuments; dIndex++) {
			int docSize = corpus.get(dIndex).size();
			for (int wIndex = 0; wIndex < docSize; wIndex++) {
				writer.write(topicAssignments.get(dIndex).get(wIndex) + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public void writeTopTopicWords()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".topWords"));

		for (int tIndex = 0; tIndex < numTopics; tIndex++) {
			writer.write("Topic" + new Integer(tIndex) + ":");

			Map<Integer, Integer> wordCount = new TreeMap<Integer, Integer>();
			for (int wIndex = 0; wIndex < vsize; wIndex++) {
				wordCount.put(wIndex, topicWordCount[tIndex][wIndex]);
			}
			wordCount = FuncUtils.sortByValueDescending(wordCount);

			Set<Integer> mostLikelyWords = wordCount.keySet();
			int count = 0;
			for (Integer index : mostLikelyWords) {
				if (count < topWords) {
					double pro = (topicWordCount[tIndex][index] + beta)
						/ (sumTopicWordCount[tIndex] + betaSum);
					pro = Math.round(pro * 1000000.0) / 1000000.0;
					writer.write(" " + id2WordVocabulary.get(index) + "(" + pro
						+ ")");
					count += 1;
				}
				else {
					writer.write("\n\n");
					break;
				}
			}
		}
		writer.close();
	}

	

	public void write()
		throws IOException
	{
		writeTopTopicWords();
		writeTopicAssignments();
                writeParameters();
		
	}

	public static void main(String args[])
		throws Exception
	{
        LSI s=new LSI();
        //    System.out.println(s.getAlpha());
  //  removeStopWords("test/corpus.txt");
      
		EPLDA lda = new EPLDA("test/train.txt", 10, s.getAlpha(),
			s.getBeta(), 1000, 10, "epLDA");
		lda.estimate();
	}
        
public static void removeStopWords(String file)throws IOException{
    Stopwords stop = new Stopwords();
    ArrayList<String> wordList = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new FileReader(file));
    for (String doc; (doc = br.readLine()) != null;) {

        if (doc.trim().length() == 0)
                continue;

        String []words = doc.trim().split("\\s+");
        
        for(String word: words){
        
        wordList.add(word);
        if(stop.is(word)){
        wordList.remove(word);
        }
        }
       
        
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("test/train.txt"));
		for (int i = 0; i < wordList.size(); i++) {
			
				writer.write(wordList.get(i) + " ");
			
			
		}
               
		writer.close();
        
    }
    System.out.println("Done!");
}        
}
