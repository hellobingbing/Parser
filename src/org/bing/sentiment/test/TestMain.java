package org.bing.sentiment.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.bing.sentiment.dependencytree.CompactString;
import org.bing.sentiment.dependencytree.Dependence;
import org.bing.sentiment.dependencytree.Node;
import org.bing.sentiment.dependencytree.SentenceInfo;

import YaraParser.Learning.AveragedPerceptron;
import YaraParser.Structures.IndexMaps;
import YaraParser.Structures.InfStruct;
import YaraParser.TransitionBasedSystem.Configuration.Configuration;
import YaraParser.TransitionBasedSystem.Parser.KBeamArcEagerParser;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;

public class TestMain {
	
	public static final String modelPath = "." + File.separator + "model_ansj" + File.separator + "model-new-cdt_iter20";
	public static final String dictPath = "." + File.separator + "test" + File.separator + "P_N_Hownet.txt";
	public static final String negWordsPath = "." + File.separator + "test" + File.separator + "Negative_Words.txt";
	public static final String filePath = "." + File.separator + "test" + File.separator + "sogou_reduced" + File.separator + "ShortSentences_sogou_reduced.txt";
	
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		// 词典读入操作
		Map<String, String> dictMap = InfoMap.getDictMap(dictPath);
		Map<String, String> negDict = InfoMap.getNegWordsMap(negWordsPath);
		
		// 读入文件中的所有句子
//		Map<Integer, String> sentencesMap = InfoMap.getSentencesMap(filePath);
		
//		long start = System.currentTimeMillis();
//		
//		String weiyu_Path = "." + File.separator + "test" + File.separator + "sogou_reduced" + File.separator + "weiyu.txt";
//		String buyu_Path = "." + File.separator + "test" + File.separator + "sogou_reduced" + File.separator + "buyu.txt";
//		String zhuangyu_Path = "." + File.separator + "test" + File.separator + "sogou_reduced" + File.separator + "zhuangyu.txt";
//		BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(weiyu_Path)),"UTF-8"));
//		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(buyu_Path)),"UTF-8"));
//		BufferedWriter bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(zhuangyu_Path)),"UTF-8"));
//		
//		parse(sentencesMap, dictMap, negDict, bw1, bw2, bw3);
//		
//		bw1.close();
//		bw2.close();
//		bw3.close();
//		
//		long end = System.currentTimeMillis();
//		System.out.println((end - start) + "ms");
		
		String sentence = "投资者乐观不起来。";
		parseSingleSentence(sentence, dictMap, negDict);
		
	}
	
	public static void parseSingleSentence(String sentence,Map<String, String> dictMap,Map<String, String> negDict) throws IOException{
		SentenceInfo sentenceInfo = Utils.getSegResult(sentence);
		List<String[]> oneSentence = Utils.getDependenceResult(modelPath, sentenceInfo);
		List<Node> nodeList = Dependence.getNodeList(oneSentence);
		Node root = Dependence.getTree(nodeList);
		StringBuffer buf = new StringBuffer();
		CompactString.getCompactString_kuohao(root, buf, true);
		SemanticGraph graph = SemanticGraph.valueOf(buf.toString());
//		System.out.println(graph);
		String[] words = sentenceInfo.getWord_real();
		// 循环每一个词语
		for(String word : words){
			// word 即为情感词
			if(dictMap.containsKey(word)){
				
				try {
					
					// 情感词作为谓语
		            SemgrexPattern pattern1_1 = SemgrexPattern.compile("{word:" + word + ";tag:/v.*/}=={$}=sentiment >VOB {}=object");
		            SemgrexMatcher matcher1_1 = pattern1_1.matcher(graph);
		            SemgrexPattern pattern1_2 = SemgrexPattern.compile("{word:" + word + ";tag:/a.*|b.*/}=={$}=sentiment >SBV {}=object");
		            SemgrexMatcher matcher1_2 = pattern1_2.matcher(graph);
		            // 情感词作为补语
		            SemgrexPattern pattern2_1 = SemgrexPattern.compile("{tag:/v.*/}=={$} >VOB {}=object >CMP {word:" + word + "}=sentiment");
		            SemgrexMatcher matcher2_1 = pattern2_1.matcher(graph);
		            SemgrexPattern pattern2_2 = SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >SBV {}=object >CMP {word:" + word + "}=sentiment");
		            SemgrexMatcher matcher2_2 = pattern2_2.matcher(graph);
		            // 情感词作为状语
		            SemgrexPattern pattern3_1 = SemgrexPattern.compile("{tag:/v.*/}=={$} >VOB {}=object >ADV {word:" + word + "}=sentiment");
		            SemgrexMatcher matcher3_1 = pattern3_1.matcher(graph);
		            SemgrexPattern pattern3_2 = SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >SBV {}=object >ADV {word:" + word + "}=sentiment");
		            SemgrexMatcher matcher3_2 = pattern3_2.matcher(graph);
		            
		            String emotion_before = null;
		            String emotion_after = null;
		            String object = null;
		            // 情感词作为谓语
		            if(matcher1_1.find()){
//		            	object = AttributeRules.getCompleteObject(1, graph, matcher1_1.getNode("sentiment"), matcher1_1.getNode("object"));
		            	object = AttributeRules.getCompleteObject(1, sentence, graph, matcher1_1.getNode("sentiment"), matcher1_1.getNode("object"));
		            	emotion_before = dictMap.get(matcher1_1.getNode("sentiment").word());
		            	emotion_after = NegativeRules.getEmotion(1, negDict, graph, matcher1_1.getNode("sentiment"), emotion_before);
		            	if(emotion_before.equals(emotion_after)){
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_before);
		            	}else {
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
						}
		            }else if (matcher1_2.find()) {
//		            	object = AttributeRules.getCompleteObject(2, graph, matcher1_2.getNode("sentiment"), matcher1_2.getNode("object"));
		            	object = AttributeRules.getCompleteObject(2, sentence, graph, matcher1_2.getNode("sentiment"), matcher1_2.getNode("object"));
		            	emotion_before = dictMap.get(matcher1_2.getNode("sentiment").word());
		            	emotion_after = NegativeRules.getEmotion(1, negDict, graph, matcher1_2.getNode("sentiment"), emotion_before);
		            	if(emotion_before.equals(emotion_after)){
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_before);
		            	}else {
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
						}
					}
		            // 情感词作为补语
		            if(matcher2_1.find()){
//		            	object = AttributeRules.getCompleteObject(3, graph, matcher2_1.getNode("sentiment"), matcher2_1.getNode("object"));
		            	object = AttributeRules.getCompleteObject(3, sentence, graph, matcher2_1.getNode("sentiment"), matcher2_1.getNode("object"));
		            	emotion_before = dictMap.get(matcher2_1.getNode("sentiment").word());
		            	emotion_after = NegativeRules.getEmotion(2, negDict, graph, matcher2_1.getNode("sentiment"), emotion_before);
		            	if(emotion_before.equals(emotion_after)){
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_before);
		            	}else {
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
						}
		            }else if (matcher2_2.find()) {
//		            	object = AttributeRules.getCompleteObject(4, graph, matcher2_2.getNode("sentiment"), matcher2_2.getNode("object"));
		            	object = AttributeRules.getCompleteObject(4, sentence, graph, matcher2_2.getNode("sentiment"), matcher2_2.getNode("object"));
		            	emotion_before = dictMap.get(matcher2_2.getNode("sentiment").word());
		            	emotion_after = NegativeRules.getEmotion(2, negDict, graph, matcher2_2.getNode("sentiment"), emotion_before);
		            	if(emotion_before.equals(emotion_after)){
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_before);
		            	}else {
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
						}
					}
		            // 情感词作为状语
		            if(matcher3_1.find()){
//		            	object = AttributeRules.getCompleteObject(5, graph, matcher3_1.getNode("sentiment"), matcher3_1.getNode("object"));
		            	object = AttributeRules.getCompleteObject(5, sentence, graph, matcher3_1.getNode("sentiment"), matcher3_1.getNode("object"));
		            	emotion_before = dictMap.get(matcher3_1.getNode("sentiment").word());
		            	emotion_after = NegativeRules.getEmotion(3, negDict, graph, matcher3_1.getNode("sentiment"), emotion_before);
		            	if(emotion_before.equals(emotion_after)){
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_before);
		            	}else {
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
						}
		            }else if (matcher3_2.find()) {
//		            	object = AttributeRules.getCompleteObject(6, graph, matcher3_2.getNode("sentiment"), matcher3_2.getNode("object"));
		            	object = AttributeRules.getCompleteObject(6, sentence, graph, matcher3_2.getNode("sentiment"), matcher3_2.getNode("object"));
		            	emotion_before = dictMap.get(matcher3_2.getNode("sentiment").word());
		            	emotion_after = NegativeRules.getEmotion(3, negDict, graph, matcher3_2.getNode("sentiment"), emotion_before);
		            	if(emotion_before.equals(emotion_after)){
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_before);
		            	}else {
		            		System.out.println(sentence + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");;
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
			}
		}
		
	}
	
	/*
	 * 依存句法分析每一个句子
	 */
	public static void parse(Map<Integer, String> sentencesMap,Map<String, String> dictMap,Map<String, String> negDict,BufferedWriter bw1,BufferedWriter bw2,BufferedWriter bw3) throws IOException{

		// TODO Auto-generated method stub
		
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
		// iteration
		Iterator<Map.Entry<Integer, String>> iter_sentences = sentencesMap.entrySet().iterator();
//		NegativeRules negativeRules = new NegativeRules();
		while(iter_sentences.hasNext()){
//			boolean flag = false;
			Map.Entry<Integer, String> me = iter_sentences.next();
			
			if(me.getValue().contains("纯属个人观点")){
				continue;
			}else if (me.getValue().startsWith("如果")) {
				continue;
			}
			
			List<Term> list = ToAnalysis.parse(me.getValue());
			int wordNum = list.size();
			int[] id = new int[wordNum];
			String[] words = new String[wordNum];
			String[] tags = new String[wordNum];
			for(int i = 0;i < list.size();i++){
				id[i] = i+1;
				words[i] = list.get(i).getName();
				tags[i] = list.get(i).getNatrue().natureStr;
			}
//			SentenceInfo sentenceInfo = new SentenceInfo(me.getKey(), id, words, tags);
			// 先不管是否含有情感词，直接依存句法分析
			List<String[]> oneSentence = new ArrayList<String[]>();
        	
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
            List<Node> nodeList = new ArrayList<Node>();
            nodeList = Dependence.getNodeList(oneSentence);
            Node root = Dependence.getTree(nodeList);
            StringBuffer buf = new StringBuffer();
            CompactString.getCompactString_kuohao(root, buf, true);
            SemanticGraph graph = SemanticGraph.valueOf(buf.toString());
            
            
			// 循环每一个词语
			for(String word : words){
				// word 即为情感词
				if(dictMap.containsKey(word)){
					
					try {
						
						// 情感词作为谓语
			            SemgrexPattern pattern1_1 = SemgrexPattern.compile("{word:" + word + ";tag:/v.*/}=={$}=sentiment >VOB {}=object");
			            SemgrexMatcher matcher1_1 = pattern1_1.matcher(graph);
			            SemgrexPattern pattern1_2 = SemgrexPattern.compile("{word:" + word + ";tag:/a.*|b.*/}=={$}=sentiment >SBV {}=object");
			            SemgrexMatcher matcher1_2 = pattern1_2.matcher(graph);
			            // 情感词作为补语
			            SemgrexPattern pattern2_1 = SemgrexPattern.compile("{tag:/v.*/}=={$} >VOB {}=object >CMP {word:" + word + "}=sentiment");
			            SemgrexMatcher matcher2_1 = pattern2_1.matcher(graph);
			            SemgrexPattern pattern2_2 = SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >SBV {}=object >CMP {word:" + word + "}=sentiment");
			            SemgrexMatcher matcher2_2 = pattern2_2.matcher(graph);
			            // 情感词作为状语
			            SemgrexPattern pattern3_1 = SemgrexPattern.compile("{tag:/v.*/}=={$} >VOB {}=object >ADV {word:" + word + "}=sentiment");
			            SemgrexMatcher matcher3_1 = pattern3_1.matcher(graph);
			            SemgrexPattern pattern3_2 = SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >SBV {}=object >ADV {word:" + word + "}=sentiment");
			            SemgrexMatcher matcher3_2 = pattern3_2.matcher(graph);
			            
			            String emotion_before = null;
			            String emotion_after = null;
			            String object = null;
			            // 情感词作为谓语
			            if(matcher1_1.find()){
//			            	object = AttributeRules.getCompleteObject(1, graph, matcher1_1.getNode("sentiment"), matcher1_1.getNode("object"));
			            	object = AttributeRules.getCompleteObject(1, me.getValue(), graph, matcher1_1.getNode("sentiment"), matcher1_1.getNode("object"));
			            	emotion_before = dictMap.get(matcher1_1.getNode("sentiment").word());
			            	emotion_after = NegativeRules.getEmotion(1, negDict, graph, matcher1_1.getNode("sentiment"), emotion_before);
			            	if(emotion_before.equals(emotion_after)){
			            		bw1.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_before);
			            		bw1.write("\r\n");
			            	}else {
								bw1.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
								bw1.write("\r\n");
							}
			            }else if (matcher1_2.find()) {
//			            	object = AttributeRules.getCompleteObject(2, graph, matcher1_2.getNode("sentiment"), matcher1_2.getNode("object"));
			            	object = AttributeRules.getCompleteObject(2, me.getValue(), graph, matcher1_2.getNode("sentiment"), matcher1_2.getNode("object"));
			            	emotion_before = dictMap.get(matcher1_2.getNode("sentiment").word());
			            	emotion_after = NegativeRules.getEmotion(1, negDict, graph, matcher1_2.getNode("sentiment"), emotion_before);
			            	if(emotion_before.equals(emotion_after)){
			            		bw1.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_before);
			            		bw1.write("\r\n");
			            	}else {
								bw1.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
								bw1.write("\r\n");
							}
						}
			            // 情感词作为补语
			            if(matcher2_1.find()){
//			            	object = AttributeRules.getCompleteObject(3, graph, matcher2_1.getNode("sentiment"), matcher2_1.getNode("object"));
			            	object = AttributeRules.getCompleteObject(3, me.getValue(), graph, matcher2_1.getNode("sentiment"), matcher2_1.getNode("object"));
			            	emotion_before = dictMap.get(matcher2_1.getNode("sentiment").word());
			            	emotion_after = NegativeRules.getEmotion(2, negDict, graph, matcher2_1.getNode("sentiment"), emotion_before);
			            	if(emotion_before.equals(emotion_after)){
			            		bw2.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_before);
			            		bw2.write("\r\n");
			            	}else {
								bw2.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
								bw2.write("\r\n");
							}
			            }else if (matcher2_2.find()) {
//			            	object = AttributeRules.getCompleteObject(4, graph, matcher2_2.getNode("sentiment"), matcher2_2.getNode("object"));
			            	object = AttributeRules.getCompleteObject(4, me.getValue(), graph, matcher2_2.getNode("sentiment"), matcher2_2.getNode("object"));
			            	emotion_before = dictMap.get(matcher2_2.getNode("sentiment").word());
			            	emotion_after = NegativeRules.getEmotion(2, negDict, graph, matcher2_2.getNode("sentiment"), emotion_before);
			            	if(emotion_before.equals(emotion_after)){
			            		bw2.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_before);
			            		bw2.write("\r\n");
			            	}else {
								bw2.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
								bw2.write("\r\n");
							}
						}
			            // 情感词作为状语
			            if(matcher3_1.find()){
//			            	object = AttributeRules.getCompleteObject(5, graph, matcher3_1.getNode("sentiment"), matcher3_1.getNode("object"));
			            	object = AttributeRules.getCompleteObject(5, me.getValue(), graph, matcher3_1.getNode("sentiment"), matcher3_1.getNode("object"));
			            	emotion_before = dictMap.get(matcher3_1.getNode("sentiment").word());
			            	emotion_after = NegativeRules.getEmotion(3, negDict, graph, matcher3_1.getNode("sentiment"), emotion_before);
			            	if(emotion_before.equals(emotion_after)){
			            		bw3.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_before);
			            		bw3.write("\r\n");
			            	}else {
								bw3.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
								bw3.write("\r\n");
							}
			            }else if (matcher3_2.find()) {
//			            	object = AttributeRules.getCompleteObject(6, graph, matcher3_2.getNode("sentiment"), matcher3_2.getNode("object"));
			            	object = AttributeRules.getCompleteObject(6, me.getValue(), graph, matcher3_2.getNode("sentiment"), matcher3_2.getNode("object"));
			            	emotion_before = dictMap.get(matcher3_2.getNode("sentiment").word());
			            	emotion_after = NegativeRules.getEmotion(3, negDict, graph, matcher3_2.getNode("sentiment"), emotion_before);
			            	if(emotion_before.equals(emotion_after)){
			            		bw3.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_before);
			            		bw3.write("\r\n");
			            	}else {
								bw3.write(me.getKey() + "\t" + me.getValue() + "\t" + object + "\t" + word + "\t" + emotion_after + "\t" + "情感反转");
								bw3.write("\r\n");
							}
						}
						
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					
				}
			}
			
		}
		parser.shutDownLiveThreads();
//        System.exit(0);
        
	}
	

}
