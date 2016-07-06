package br.ufc.quixada.classificador;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.naivebayes.BayesUtils;
import org.apache.mahout.classifier.naivebayes.NaiveBayesModel;
import org.apache.mahout.classifier.naivebayes.StandardNaiveBayesClassifier;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.vectorizer.TFIDF;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import br.ufc.quixada.escritor.arquivo.EscreverArquivo;


public class Classifier {

		private static String modelPath="/home/rayner/model";
		private static String labelIndexPath="/home/rayner/labelindex";
		private static String dictionaryPath="/home/rayner/outVec/dictionary.file-0";
		private static String documentFrequencyPath="/home/rayner/outVec/df-count/part-r-00000";
		
		EscreverArquivo escreverArquivo = new EscreverArquivo();
		
		public static Map<String, Integer> readDictionnary(Configuration conf, Path dictionnaryPath) {
	        Map<String, Integer> dictionnary = new HashMap<String, Integer>();
	        for (Pair<Text, IntWritable> pair : new SequenceFileIterable<Text, IntWritable>(dictionnaryPath, true, conf)) {
	            dictionnary.put(pair.getFirst().toString(), pair.getSecond().get());
	        }
	        return dictionnary;
	    }
	    public static Map<Integer, Long> readDocumentFrequency(Configuration conf, Path documentFrequencyPath) {
	        Map<Integer, Long> documentFrequency = new HashMap<Integer, Long>();
	        for (Pair<IntWritable, LongWritable> pair : new SequenceFileIterable<IntWritable, LongWritable>(documentFrequencyPath, true, conf)) {
	            documentFrequency.put(pair.getFirst().get(), pair.getSecond().get());
	        }
	        return documentFrequency;
	    }

	    public void classify(String conteudoArquivo)throws Exception {
	    	Configuration configuration = new Configuration();      
	    	NaiveBayesModel model = NaiveBayesModel.materialize(new Path(modelPath), configuration);       
	    	StandardNaiveBayesClassifier classifier = new StandardNaiveBayesClassifier(model);
	    	//Lendo os sequence files
	    	Map<Integer, String> labels = BayesUtils.readLabelIndex(configuration, new Path(labelIndexPath));
	    	Map<String, Integer> dictionary = readDictionnary(configuration, new Path(dictionaryPath));
	    	Map<Integer, Long> documentFrequency = readDocumentFrequency(configuration, new Path(documentFrequencyPath));
	    	int labelCount = labels.size();
	    	int documentCount = documentFrequency.get(-1).intValue();
	    	//Todo o conteúdo do arquivo está na varíavel conteudoArquivo
	    	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
	    	Multiset<String> words = ConcurrentHashMultiset.create();
	    	// Usando o analyzer para extrair as palavras
	    	TokenStream ts = analyzer.tokenStream("text", new StringReader(conteudoArquivo));
	    	CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
	    	ts.reset();
	    	int wordCount = 0;
	    	while (ts.incrementToken()) {
	    	                if (termAtt.length() > 0) {
	    	                    String word = ts.getAttribute(CharTermAttribute.class).toString();
	    	                    Integer wordId = dictionary.get(word);
	    	                    // Se a palavra não está no dicionário, nós pulamos a mesma.
	    	                    if (wordId != null) {
	    	                        words.add(word);
	    	                        wordCount++;
	    	                    }
	    	                }
	    	
	    	}
	    	//Criaçao do vetor 
	    	Vector vector = new RandomAccessSparseVector(10000);
	    	TFIDF tfidf = new TFIDF();
	    	for (Object entryAlt:words.entrySet()) {
	    	                Entry<String> entry = (Entry<String>) entryAlt;
	    	            	 String word = entry.getElement();
	    	                int count = entry.getCount();
	    	                Integer wordId = dictionary.get(word);
	    	                Long freq = documentFrequency.get(wordId);
	    	                double tfIdfValue = tfidf.calculate(count, freq.intValue(), wordCount, 									documentCount);
	    	                vector.setQuick(wordId, tfIdfValue);
	    	            }
	    	//Rodando o classificador
	    	Vector resultVector = classifier.classifyFull(vector);
	    	double bestScore = -Double.MAX_VALUE;
	    	int bestCategoryId = -1;
	    	//Iterando sobre as tags e escolhendo a melhor.
	    	for(int i =0;i<resultVector.size(); i++) {
	    					Element element = resultVector.getElement(i);
	    	                int categoryId = element.index();
	    	                double score = element.get();
	    	                if (score > bestScore) {
	    	                    bestScore = score;
	    	                    bestCategoryId = categoryId;
	    	                }

	    	            }
	    	escreverArquivo.escrever(labels.get(bestCategoryId), conteudoArquivo);
	    }
	    
}
