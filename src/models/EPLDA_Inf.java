

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



public class EPLDA_Inf
{
	public double alpha; // Hyper-parameter alpha
	public double beta; // Hyper-parameter alpha
        
        public double [][] alphaArr; // Hyper-parameter alpha
	public double[][] betaArr; // Hyper-parameter alpha
        
	public int numberOfTopics; // Number of topics
	public int numberOfIterations; // Number of Gibbs sampling iterations
	public int topWords; // Number of most probable words for each topic

	public double alphaSum; // alpha * numberOfTopics
	public double betaSum; // beta * vsize

	public List<List<Integer>> corpus; // Word ID-based corpus
	public List<List<Integer>> topicAssignments; // Topics assignments for words
													
	public int numberOfDocuments; // Number of documents in the corpus
	public int numWordsInCorpus; // Number of words in the corpus

	public HashMap<String, Integer> word2IdVocabulary; // Vocabulary to get ID
														
	public HashMap<Integer, String> id2WordVocabulary; // Vocabulary to get word
														
	public int vsize; // The number of word types in the corpus

	// numberOfDocuments * numberOfTopics matrix
	// Given a document: number of its words assigned to each topic
	public int[][] docTopicCount;
	// Number of words in every document
	public int[] sumDocTopicCount;
	// numberOfTopics * vsize matrix
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

	public String expName = "epLDAinf";
	public String orgExpName = "epLDAinf";
	public String tAssignsFilePath = "";
	
        public int savestep = 0;

	public EPLDA_Inf(String pathToTrainingParasFile,
		String pathToUnseenCorpus, int inNumIterations, int inTopWords,
		String inExpName, int inSaveStep)
		throws Exception
	{
		HashMap<String, String> paras = parseTrainingParasFile(pathToTrainingParasFile);
		if (!paras.get("-model").equals("epLDA")) {
			throw new Exception("Wrong pre-trained model!!!");
		}
		alpha = new Double(paras.get("-alpha"));
		beta = new Double(paras.get("-beta"));
		numberOfTopics = new Integer(paras.get("-ntopics"));

		numberOfIterations = inNumIterations;
		topWords = inTopWords;
		savestep = inSaveStep;
		expName = inExpName;
		orgExpName = expName;

		String trainingCorpus = paras.get("-corpus");
		String trainingCorpusfolder = trainingCorpus.substring(
			0,
			Math.max(trainingCorpus.lastIndexOf("/"),
				trainingCorpus.lastIndexOf("\\")) + 1);
		String topicAssignment4TrainFile = trainingCorpusfolder
			+ paras.get("-name") + ".topicAssignments";

		word2IdVocabulary = new HashMap<String, Integer>();
		id2WordVocabulary = new HashMap<Integer, String>();
		initializeWordCount(trainingCorpus, topicAssignment4TrainFile);

		corpusPath = pathToUnseenCorpus;
		folderPath = pathToUnseenCorpus.substring(
			0,
			Math.max(pathToUnseenCorpus.lastIndexOf("/"),
				pathToUnseenCorpus.lastIndexOf("\\")) + 1);
		System.out.println("Reading unseen corpus: " + pathToUnseenCorpus);
		corpus = new ArrayList<List<Integer>>();
		numberOfDocuments = 0;
		numWordsInCorpus = 0;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToUnseenCorpus));
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
						// Skip this unknown-word
					}
				}
				numberOfDocuments++;
				numWordsInCorpus += document.size();
				corpus.add(document);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		docTopicCount = new int[numberOfDocuments][numberOfTopics];
		sumDocTopicCount = new int[numberOfDocuments];
		sampleTopic = new double[numberOfTopics];
		for (int i = 0; i < numberOfTopics; i++) {
			sampleTopic[i] = 1.0 / numberOfTopics;
		}

		alphaSum = numberOfTopics * alpha;
		betaSum = vsize * beta;

		System.out.println("Corpus size: " + numberOfDocuments + " docs, "
			+ numWordsInCorpus + " words");
		System.out.println("Vocabuary size: " + vsize);
		System.out.println("Number of topics: " + numberOfTopics);
		System.out.println("alpha: " + alpha);
		System.out.println("beta: " + beta);
		System.out.println("Number of sampling iterations: " + numberOfIterations);
		System.out.println("Number of top topical words: " + topWords);

		init();
	}

	private HashMap<String, String> parseTrainingParasFile(
		String pathToTrainingParasFile)
		throws Exception
	{
		HashMap<String, String> paras = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToTrainingParasFile));
			for (String line; (line = br.readLine()) != null;) {

				if (line.trim().length() == 0)
					continue;

				String[] paraOptions = line.trim().split("\\s+");
				paras.put(paraOptions[0], paraOptions[1]);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return paras;
	}

	private void initializeWordCount(String pathToTrainingCorpus,
		String pathToTopicAssignmentFile)
	{
		System.out.println("Loading pre-trained model...");
		List<List<Integer>> trainCorpus = new ArrayList<List<Integer>>();
		BufferedReader br = null;
		try {
			int indexWord = -1;
			br = new BufferedReader(new FileReader(pathToTrainingCorpus));
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
				trainCorpus.add(document);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		vsize = word2IdVocabulary.size();
		topicWordCount = new int[numberOfTopics][vsize];
		sumTopicWordCount = new int[numberOfTopics];

		try {
			br = new BufferedReader(new FileReader(pathToTopicAssignmentFile));
			int docId = 0;
			for (String line; (line = br.readLine()) != null;) {
				String[] strTopics = line.trim().split("\\s+");
				for (int j = 0; j < strTopics.length; j++) {
					int wordId = trainCorpus.get(docId).get(j);
					int topic = new Integer(strTopics[j]);
					topicWordCount[topic][wordId] += 1;
					sumTopicWordCount[topic] += 1;
				}
				docId++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void init()
		throws IOException
	{
		System.out.println("Randomly initializing topic assignments ...");

		topicAssignments = new ArrayList<List<Integer>>();

		for (int i = 0; i < numberOfDocuments; i++) {
			List<Integer> topics = new ArrayList<Integer>();
			int docSize = corpus.get(i).size();
			for (int j = 0; j < docSize; j++) {
				int topic = FuncUtils.nextDiscrete(sampleTopic); // Sample a topic
				// Increase counts
				docTopicCount[i][topic] += 1;
				topicWordCount[topic][corpus.get(i).get(j)] += 1;
				sumDocTopicCount[i] += 1;
				sumTopicWordCount[topic] += 1;

				topics.add(topic);
			}
			topicAssignments.add(topics);
		}
	}

	public void inference()
		throws IOException
	{
		writeParameters();
	

		System.out.println("Running Gibbs sampling inference: ");

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
				docTopicCount[dIndex][topic] -= 1;
				// docTopicSum[dIndex] -= 1;
				topicWordCount[topic][word] -= 1;
				sumTopicWordCount[topic] -= 1;

				// Sample a topic
				for (int tIndex = 0; tIndex < numberOfTopics; tIndex++) {
					sampleTopic[tIndex] = (docTopicCount[dIndex][tIndex] + alpha)
						* ((topicWordCount[tIndex][word] + beta) / (sumTopicWordCount[tIndex] + betaSum));
				
				}
				topic = FuncUtils.nextDiscrete(sampleTopic);

				// Increase counts
				docTopicCount[dIndex][topic] += 1;
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
		writer.write("-model" + "\t" + "LDA");
		writer.write("\n-corpus" + "\t" + corpusPath);
		writer.write("\n-ntopics" + "\t" + numberOfTopics);
		writer.write("\n-alpha" + "\t" + alpha);
		writer.write("\n-beta" + "\t" + beta);
		writer.write("\n-niters" + "\t" + numberOfIterations);
		writer.write("\n-twords" + "\t" + topWords);
		writer.write("\n-name" + "\t" + expName);
		if (tAssignsFilePath.length() > 0)
			writer.write("\n-initFile" + "\t" + tAssignsFilePath);
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

	public void writeTopTopicalWords()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".topWords"));

		for (int tIndex = 0; tIndex < numberOfTopics; tIndex++) {
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
		writeTopTopicalWords();
		
		writeTopicAssignments();
		
	}

	public static void main(String args[])
		throws Exception
	{
		EPLDA_Inf lda = new EPLDA_Inf(
			"test/epLDA.paras", "test/test.txt", 1000,10, "epLDAinf",
			0);
		lda.inference();
	}
}
