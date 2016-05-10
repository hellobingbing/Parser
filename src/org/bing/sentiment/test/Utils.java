package org.bing.sentiment.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.bing.sentiment.dependencytree.CompactString;
import org.bing.sentiment.dependencytree.Dependence;
import org.bing.sentiment.dependencytree.Node;
import org.bing.sentiment.dependencytree.SentenceInfo;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import YaraParser.Learning.AveragedPerceptron;
import YaraParser.Structures.IndexMaps;
import YaraParser.Structures.InfStruct;
import YaraParser.TransitionBasedSystem.Configuration.Configuration;
import YaraParser.TransitionBasedSystem.Parser.KBeamArcEagerParser;


public class Utils {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		
	}
	
	public static String readSent() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("请输入要解析的句子: ");
		return br.readLine();
	}
	public static List<String> readSent(String filePath) throws IOException{
		List<String> sentences = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
		String line = null;
		while((line = br.readLine()) != null){
			if(!line.equals("")){
				sentences.add(line);
			}
		}
		br.close();
		return sentences;
	}
	
	public static SentenceInfo getSegResult(String sent){
		
		List<Term> list = ToAnalysis.parse(sent);
//		System.out.println("中文分词结果:\n\n" + list + "\n");
		int wordNum = list.size();
		
		int[] word_id = new int[wordNum];
		String[] word_real = new String[wordNum];
		String[] word_pos = new String[wordNum];
		
		for(int i = 0;i < wordNum;i++){
//			System.out.println((i + 1) + "\t" + list.get(i).getName() + "\t" + list.get(i).getNatrue().natureStr);
			word_id[i] = i + 1;
			word_real[i] = list.get(i).getName();
			word_pos[i] = list.get(i).getNatrue().natureStr;
		}
		
		SentenceInfo si = new SentenceInfo(word_id, word_real, word_pos);
		
		return si;
		
	}
	public static List<SentenceInfo> getSegResult(List<String> sentences){
		List<SentenceInfo> sentenceList = new ArrayList<SentenceInfo>();
		for(String sent : sentences){
			List<Term> list = ToAnalysis.parse(sent);
			int wordNum = list.size();
			int[] word_id = new int[wordNum];
			String[] word_real = new String[wordNum];
			String[] word_pos = new String[wordNum];
			for(int i = 0;i < list.size();i++){
				word_id[i] = i+1;
				word_real[i] = list.get(i).getName();
				word_pos[i] = list.get(i).getNatrue().natureStr;
			}
			SentenceInfo si = new SentenceInfo(word_id, word_real, word_pos);
			sentenceList.add(si);
		}
		return sentenceList;
		
	}
	
	public static void printDependence(String modelPath,SentenceInfo sentence){
    	
//      String modelFile = args[0];
        int numOfThreads = 8;

        InfStruct infStruct = null;
        try {
        	infStruct = new InfStruct(modelPath);
        } catch (Exception e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }

        ArrayList<Integer> dependencyLabels = infStruct.dependencyLabels;
        IndexMaps maps = infStruct.maps;
        AveragedPerceptron averagedPerceptron = new AveragedPerceptron(infStruct);

        int featureSize = averagedPerceptron.featureSize();
        KBeamArcEagerParser parser = new KBeamArcEagerParser(averagedPerceptron, dependencyLabels, featureSize, maps, numOfThreads);
      
      
//        String[] words = {"我","是","中华人民共和国","公民","。"};
//        String[] tags = {"r","v","ns","n","w"};
        int[] id = sentence.getWord_id();
        String[] words = sentence.getWord_real();
        String[] tags = sentence.getWord_pos();

        Configuration bestParse = null;
        try {
			bestParse = parser.parse(maps.makeSentence(words, tags, infStruct.options.rootFirst, infStruct.options.lowercase), infStruct.options.rootFirst, infStruct.options.beamWidth, numOfThreads);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("依存句法结果:\n");
        if (infStruct.options.rootFirst) {
        	for (int i = 0; i < words.length; i++) {
        		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + bestParse.state.getHead(i + 1) + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
        	}
        } else {
        	for (int i = 0; i < words.length; i++) {
        		int head = bestParse.state.getHead(i + 1);
        		if (head == words.length+1)
        			head = 0;
        		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + head + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
        	}
        }
        parser.shutDownLiveThreads();
//        System.exit(0);
    }
	public static void printDependence(String modelPath,List<SentenceInfo> sentenceList){
		int numOfThreads = 8;

        InfStruct infStruct = null;
        try {
        	infStruct = new InfStruct(modelPath);
        } catch (Exception e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }

        ArrayList<Integer> dependencyLabels = infStruct.dependencyLabels;
        IndexMaps maps = infStruct.maps;
        AveragedPerceptron averagedPerceptron = new AveragedPerceptron(infStruct);

        int featureSize = averagedPerceptron.featureSize();
        KBeamArcEagerParser parser = new KBeamArcEagerParser(averagedPerceptron, dependencyLabels, featureSize, maps, numOfThreads);
        
        System.out.println("依存句法结果:\n");
        for(SentenceInfo sentence : sentenceList){
        	int[] id = sentence.getWord_id();
            String[] words = sentence.getWord_real();
            String[] tags = sentence.getWord_pos();
            Configuration bestParse = null;
            try {
    			bestParse = parser.parse(maps.makeSentence(words, tags, infStruct.options.rootFirst, infStruct.options.lowercase), infStruct.options.rootFirst, infStruct.options.beamWidth, numOfThreads);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            
            if (infStruct.options.rootFirst) {
            	for (int i = 0; i < words.length; i++) {
            		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + bestParse.state.getHead(i + 1) + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
            	}
            } else {
            	for (int i = 0; i < words.length; i++) {
            		int head = bestParse.state.getHead(i + 1);
            		if (head == words.length+1)
            			head = 0;
            		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + head + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
            	}
            }
            System.out.println();
        }
        parser.shutDownLiveThreads();
//        System.exit(0);
	}
	
	public static List<String[]> getDependenceResult(String modelPath,SentenceInfo sentence){
		List<String[]> oneSentence = new ArrayList<String[]>();
		int numOfThreads = 8;

        InfStruct infStruct = null;
        try {
        	infStruct = new InfStruct(modelPath);
        } catch (Exception e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }

        ArrayList<Integer> dependencyLabels = infStruct.dependencyLabels;
        IndexMaps maps = infStruct.maps;
        AveragedPerceptron averagedPerceptron = new AveragedPerceptron(infStruct);

        int featureSize = averagedPerceptron.featureSize();
        KBeamArcEagerParser parser = new KBeamArcEagerParser(averagedPerceptron, dependencyLabels, featureSize, maps, numOfThreads);
        
        int[] id = sentence.getWord_id();
        String[] words = sentence.getWord_real();
        String[] tags = sentence.getWord_pos();
        Configuration bestParse = null;
        try {
			bestParse = parser.parse(maps.makeSentence(words, tags, infStruct.options.rootFirst, infStruct.options.lowercase), infStruct.options.rootFirst, infStruct.options.beamWidth, numOfThreads);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (infStruct.options.rootFirst) {
        	for (int i = 0; i < words.length; i++) {
//        		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + bestParse.state.getHead(i + 1) + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
        		String[] oneWord = new String[5];
        		oneWord[0] = Integer.toString(id[i]);
        		oneWord[1] = words[i];
        		oneWord[2] = tags[i];
        		oneWord[3] = Integer.toString(bestParse.state.getHead(i + 1));
        		oneWord[4] = maps.revWords[bestParse.state.getDependency(i + 1)];
        		oneSentence.add(oneWord);
        	}
        } else {
        	for (int i = 0; i < words.length; i++) {
        		int head = bestParse.state.getHead(i + 1);
        		if (head == words.length+1)
        			head = 0;
//        		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + head + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
        		String[] oneWord = new String[5];
        		oneWord[0] = Integer.toString(id[i]);
        		oneWord[1] = words[i];
        		oneWord[2] = tags[i];
        		oneWord[3] = Integer.toString(head);
        		oneWord[4] = maps.revWords[bestParse.state.getDependency(i + 1)];
        		oneSentence.add(oneWord);
        	}
        }
        parser.shutDownLiveThreads();
        return oneSentence;
        
	}
	public static List<List<String[]>> getDependenceResult(String modelPath,List<SentenceInfo> sentenceList){
		List<List<String[]>> dependenceResult = new ArrayList<List<String[]>>();
		int numOfThreads = 8;

        InfStruct infStruct = null;
        try {
        	infStruct = new InfStruct(modelPath);
        } catch (Exception e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }

        ArrayList<Integer> dependencyLabels = infStruct.dependencyLabels;
        IndexMaps maps = infStruct.maps;
        AveragedPerceptron averagedPerceptron = new AveragedPerceptron(infStruct);

        int featureSize = averagedPerceptron.featureSize();
        KBeamArcEagerParser parser = new KBeamArcEagerParser(averagedPerceptron, dependencyLabels, featureSize, maps, numOfThreads);
        
        for(SentenceInfo sentence : sentenceList){
        	List<String[]> oneSentence = new ArrayList<String[]>();
        	
        	int[] id = sentence.getWord_id();
            String[] words = sentence.getWord_real();
            String[] tags = sentence.getWord_pos();
            Configuration bestParse = null;
            try {
    			bestParse = parser.parse(maps.makeSentence(words, tags, infStruct.options.rootFirst, infStruct.options.lowercase), infStruct.options.rootFirst, infStruct.options.beamWidth, numOfThreads);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            
            if (infStruct.options.rootFirst) {
            	for (int i = 0; i < words.length; i++) {
//            		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + bestParse.state.getHead(i + 1) + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
            		String[] oneWord = new String[5];
            		oneWord[0] = Integer.toString(id[i]);
            		oneWord[1] = words[i];
            		oneWord[2] = tags[i];
            		oneWord[3] = Integer.toString(bestParse.state.getHead(i + 1));
            		oneWord[4] = maps.revWords[bestParse.state.getDependency(i + 1)];
            		oneSentence.add(oneWord);
            	}
            } else {
            	for (int i = 0; i < words.length; i++) {
            		int head = bestParse.state.getHead(i + 1);
            		if (head == words.length+1)
            			head = 0;
//            		System.out.println(id[i] + "\t" + words[i] + "\t" + tags[i] + "\t" + head + "\t" + maps.revWords[bestParse.state.getDependency(i + 1)]);
            		String[] oneWord = new String[5];
            		oneWord[0] = Integer.toString(id[i]);
            		oneWord[1] = words[i];
            		oneWord[2] = tags[i];
            		oneWord[3] = Integer.toString(head);
            		oneWord[4] = maps.revWords[bestParse.state.getDependency(i + 1)];
            		oneSentence.add(oneWord);
            	}
            }
            dependenceResult.add(oneSentence);
        }
        parser.shutDownLiveThreads();
        return dependenceResult;
        
	}

}
