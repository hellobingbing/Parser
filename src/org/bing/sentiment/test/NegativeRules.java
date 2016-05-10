package org.bing.sentiment.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bing.sentiment.dependencytree.CompactString;
import org.bing.sentiment.dependencytree.Dependence;
import org.bing.sentiment.dependencytree.Node;
import org.bing.sentiment.dependencytree.SentenceInfo;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;

public class NegativeRules {
	
	public static final String POSITIVE = "e_p";
	public static final String NEGATIVE = "e_n";
	public static final String modelPath = "." + File.separator + "model_ansj" + File.separator + "model-new-cdt_iter20";
	
	public Map<String, String> negDict = null;
	
	public NegativeRules() throws IOException{
		this.negDict = InfoMap.getNegWordsMap(InfoMap.negWordsPath);
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		String sentence = Utils.readSent();
		SentenceInfo sentenceInfo = Utils.getSegResult(sentence);
		List<String[]> oneSentence = Utils.getDependenceResult(modelPath, sentenceInfo);
		List<Node> nodeList = Dependence.getNodeList(oneSentence);
		Node root = Dependence.getTree(nodeList);
		StringBuffer buf = new StringBuffer();
		CompactString.getCompactString_kuohao(root, buf, true);
		SemanticGraph graph = SemanticGraph.valueOf(buf.toString());
		System.out.println(graph);
		
		
	}
	
	
	
	public static String getEmotion(int flag, Map<String,String> negDict, SemanticGraph graph, IndexedWord indexedWord, String emotion) throws Exception{
		switch (flag) {
		
		case 1://情感词作为谓语
			SemgrexPattern pattern1_1 = 
			SemgrexPattern.compile("{word:" + indexedWord.word() + ";tag:" + indexedWord.tag() + "}=={$} >>ADV {}=neg");
			SemgrexPattern pattern1_2 = 
			SemgrexPattern.compile("{word:" + indexedWord.word() + ";tag:" + indexedWord.tag() + "}=={$} >>CMP {}=neg");
//			SemgrexPattern pattern1_3 = 
//			SemgrexPattern.compile("{word:" + indexedWord.word() + ";tag:" + indexedWord.tag() + "}=={$} >SBV ({} >>ATT {}=neg)");
//			SemgrexPattern pattern1_4 = 
//			SemgrexPattern.compile("{word:" + indexedWord.word() + ";tag:" + indexedWord.tag() + "}=={$} >VOB ({} >>ATT {}=neg)");
			
			SemgrexMatcher matcher1_1 = pattern1_1.matcher(graph);
			SemgrexMatcher matcher1_2 = pattern1_2.matcher(graph);
//			SemgrexMatcher matcher1_3 = pattern1_3.matcher(graph);
//			SemgrexMatcher matcher1_4 = pattern1_4.matcher(graph);
			
			while(matcher1_1.find()){
				if(negDict.containsKey(matcher1_1.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
			while(matcher1_2.find()){
				if(negDict.containsKey(matcher1_2.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
//			while(matcher1_3.find()){
//				if(negDict.containsKey(matcher1_3.getNode("neg").word())){
//					emotion = getReverseEmotion(emotion);
//				}
//			}
//			while(matcher1_4.find()){
//				if(negDict.containsKey(matcher1_4.getNode("neg").word())){
//					emotion = getReverseEmotion(emotion);
//				}
//			}
			break;
		
		case 2://情感词作为谓语的补语
			SemgrexPattern pattern2_1 = 
			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$}=neg >CMP {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "}");
			SemgrexPattern pattern2_2 = 
			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >CMP {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >>ADV {}=neg");
			SemgrexPattern pattern2_3 = 
			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >CMP {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >>CMP {}=neg");
//			SemgrexPattern pattern2_4 = 
//			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >CMP {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >SBV ({} >>ATT {}=neg)");
//			SemgrexPattern pattern2_5 = 
//			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >CMP {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >VOB ({} >>ATT {}=neg)");
			
			SemgrexMatcher matcher2_1 = pattern2_1.matcher(graph);
			SemgrexMatcher matcher2_2 = pattern2_2.matcher(graph);
			SemgrexMatcher matcher2_3 = pattern2_3.matcher(graph);
//			SemgrexMatcher matcher2_4 = pattern2_4.matcher(graph);
//			SemgrexMatcher matcher2_5 = pattern2_5.matcher(graph);
			
			while(matcher2_1.find()){
				if(negDict.containsKey(matcher2_1.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
			while(matcher2_2.find()){
				if(negDict.containsKey(matcher2_2.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
			while(matcher2_3.find()){
				if(negDict.containsKey(matcher2_3.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
//			while(matcher2_4.find()){
//				if(negDict.containsKey(matcher2_4.getNode("neg").word())){
//					emotion = getReverseEmotion(emotion);
//				}
//			}
//			while(matcher2_5.find()){
//				if(negDict.containsKey(matcher2_5.getNode("neg").word())){
//					emotion = getReverseEmotion(emotion);
//				}
//			}
			break;
			
		case 3://情感词作为谓语的状语
			SemgrexPattern pattern3_1 =
			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >ADV {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >>ADV {}=neg");
			SemgrexPattern pattern3_2 = 
			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >ADV {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >>CMP {}=neg");
			SemgrexPattern pattern3_3 = 
			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$}=neg >ADV {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "}");
//			SemgrexPattern pattern3_4 = 
//			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >ADV {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >SBV ({} >>ATT {}=neg)");
//			SemgrexPattern pattern3_5 = 
//			SemgrexPattern.compile("{tag:/v.*|a.*/}=={$} >ADV {word:" + indexedWord.word()  + ";tag:" + indexedWord.tag() + "} >VOB ({} >>ATT {}=neg)");
			
			SemgrexMatcher matcher3_1 = pattern3_1.matcher(graph);
			SemgrexMatcher matcher3_2 = pattern3_2.matcher(graph);
			SemgrexMatcher matcher3_3 = pattern3_3.matcher(graph);
//			SemgrexMatcher matcher3_4 = pattern3_4.matcher(graph);
//			SemgrexMatcher matcher3_5 = pattern3_5.matcher(graph);
			
			while(matcher3_1.find()){
				if(negDict.containsKey(matcher3_1.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
			while(matcher3_2.find()){
				if(negDict.containsKey(matcher3_2.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
			while(matcher3_3.find()){
				if(negDict.containsKey(matcher3_3.getNode("neg").word())){
					emotion = getReverseEmotion(emotion);
				}
			}
//			while(matcher3_4.find()){
//				if(negDict.containsKey(matcher3_4.getNode("neg").word())){
//					emotion = getReverseEmotion(emotion);
//				}
//			}
//			while(matcher3_5.find()){
//				if(negDict.containsKey(matcher3_5.getNode("neg").word())){
//					emotion = getReverseEmotion(emotion);
//				}
//			}
			break;
			
		default:
			break;
		}
		return emotion;
		
	}
	
	public static String getReverseEmotion(String emotion){
		if(emotion.equals(NEGATIVE)){
			return POSITIVE;
		}else if (emotion.equals(POSITIVE)) {
			return NEGATIVE;
		}else {
			return emotion;
		}
	}
	
	
}
