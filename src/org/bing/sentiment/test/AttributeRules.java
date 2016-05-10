package org.bing.sentiment.test;

import java.io.File;
import java.util.List;

import org.bing.sentiment.dependencytree.CompactString;
import org.bing.sentiment.dependencytree.Dependence;
import org.bing.sentiment.dependencytree.Node;
import org.bing.sentiment.dependencytree.SentenceInfo;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;

public class AttributeRules {
	
	public static String modelPath = "." + File.separator + "model_ansj" + File.separator + "model-new-cdt_iter20";

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
	
	public static String getCompleteObject(int flag,SemanticGraph graph,IndexedWord sentiment,IndexedWord object) throws Exception{
		
		String attribute = "";
		String completeObject = "";
		
		switch (flag) {
		case 1:
			SemgrexPattern pattern1_1 = 
			SemgrexPattern.compile("{word:" + sentiment.word() + ";tag:" + sentiment.tag() + "}=={$} >VOB ({word:" + object.word() + ";tag:" + object.tag() + "}=object 1,3>>ATT {}=att)");
			SemgrexMatcher matcher1_1 = pattern1_1.matcher(graph);
			
			while(matcher1_1.find()){
				attribute = matcher1_1.getNode("att").word() + attribute;
			}
			completeObject = attribute + object.word();
			break;
			
		case 2:
			SemgrexPattern pattern1_2 = 
			SemgrexPattern.compile("{word:" + sentiment.word() + ";tag:" + sentiment.tag() + "}=={$} >SBV ({word:" + object.word() + ";tag:" + object.tag() + "}=object 1,3>>ATT {}=att)");
			SemgrexMatcher matcher1_2 = pattern1_2.matcher(graph);
			
			while(matcher1_2.find()){
				attribute = matcher1_2.getNode("att").word() + attribute;
			}
			completeObject = attribute + object.word();
			break;
			
		case 3:
			SemgrexPattern pattern2_1 = 
			SemgrexPattern.compile("{tag:/v.*/}=={$} >CMP {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >VOB ({word:" + object.word() + ";tag:" + object.tag() + "}=object 1,3>>ATT {}=att)");
			SemgrexMatcher matcher2_1 = pattern2_1.matcher(graph);
			
			while(matcher2_1.find()){
				attribute = matcher2_1.getNode("att").word() + attribute;
			}
			completeObject = attribute + object.word();
			break;
			
		case 4:
			SemgrexPattern pattern2_2 = 
			SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >CMP {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >SBV ({word:" + object.word() + ";tag:" + object.tag() + "}=object 1,3>>ATT {}=att)");
			SemgrexMatcher matcher2_2 = pattern2_2.matcher(graph);
			
			while(matcher2_2.find()){
				attribute = matcher2_2.getNode("att").word() + attribute;
			}
			completeObject = attribute + object.word();
			break;
			
		case 5:
			SemgrexPattern pattern3_1 = 
			SemgrexPattern.compile("{tag:/v.*/}=={$} >ADV {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >VOB ({word:" + object.word() + ";tag:" + object.tag() + "}=object 1,3>>ATT {}=att)");
			SemgrexMatcher matcher3_1 = pattern3_1.matcher(graph);
			
			while(matcher3_1.find()){
				attribute = matcher3_1.getNode("att").word() + attribute;
			}
			completeObject = attribute + object.word();
			break;
			
		case 6:
			SemgrexPattern pattern3_2 = 
			SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >ADV {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >SBV ({word:" + object.word() + ";tag:" + object.tag() + "}=object 1,3>>ATT {}=att)");
			SemgrexMatcher matcher3_2 = pattern3_2.matcher(graph);
			
			while(matcher3_2.find()){
				attribute = matcher3_2.getNode("att").word() + attribute;
			}
			completeObject = attribute + object.word();
			break;
			
		default:
			completeObject = object.word();
			break;
		}
		return completeObject;
	}
	
	public static String getCompleteObject(int flag,String oneSentence,SemanticGraph graph,IndexedWord sentiment,IndexedWord object) throws Exception{
		String completeObject = object.word();
		int start = 0;
		int end = 0;
		int starts = oneSentence.length()-1;
		int ends = oneSentence.length()-1;
		switch (flag) {
		case 1:
			SemgrexPattern pattern1_1 = 
			SemgrexPattern.compile("{word:" + sentiment.word() + ";tag:" + sentiment.tag() + "}=={$} >VOB ({word:" + object.word() + ";tag:" + object.tag() + "}=obj >> {}=att)");
			SemgrexMatcher matcher1_1 = pattern1_1.matcher(graph);
			while(matcher1_1.find()){
				start = oneSentence.indexOf(matcher1_1.getNode("obj").word());
				if(oneSentence.indexOf(matcher1_1.getNode("att").word()) >= end){
					end = oneSentence.indexOf(matcher1_1.getNode("att").word()) + matcher1_1.getNode("att").word().length();
				}
			}
			if(start < end && start >= 0 && end <= oneSentence.length()){
				completeObject = oneSentence.substring(start, end);
			}else {
				matcher1_1.reset();
				while(matcher1_1.find()){
					ends = oneSentence.indexOf(matcher1_1.getNode("obj").word()) + matcher1_1.getNode("obj").word().length();
					if(oneSentence.indexOf(matcher1_1.getNode("att").word()) < starts){
						starts = oneSentence.indexOf(matcher1_1.getNode("att").word());
					}
				}
				if(starts < ends && starts >= 0 && ends <= oneSentence.length()){
					completeObject = oneSentence.substring(starts, ends);
				}
			}
			
			break;
			
		case 2:
			SemgrexPattern pattern1_2 = 
			SemgrexPattern.compile("{word:" + sentiment.word() + ";tag:" + sentiment.tag() + "}=={$} >SBV ({word:" + object.word() + ";tag:" + object.tag() + "}=obj >> {}=att)");
			SemgrexMatcher matcher1_2 = pattern1_2.matcher(graph);
			while(matcher1_2.find()){
				ends = oneSentence.indexOf(matcher1_2.getNode("obj").word()) + matcher1_2.getNode("obj").word().length();
				if(oneSentence.indexOf(matcher1_2.getNode("att").word()) < starts){
					starts = oneSentence.indexOf(matcher1_2.getNode("att").word());
				}
			}
			if(starts < ends && starts >= 0 && ends <= oneSentence.length()){
				completeObject = oneSentence.substring(starts, ends);
			}
			
			break;
			
		case 3:
			SemgrexPattern pattern2_1 = 
			SemgrexPattern.compile("{tag:/v.*/}=={$} >CMP {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >VOB ({word:" + object.word() + ";tag:" + object.tag() + "}=obj >> {}=att)");
			SemgrexMatcher matcher2_1 = pattern2_1.matcher(graph);
			while(matcher2_1.find()){
				start = oneSentence.indexOf(matcher2_1.getNode("obj").word());
				if(oneSentence.indexOf(matcher2_1.getNode("att").word()) >= end){
					end = oneSentence.indexOf(matcher2_1.getNode("att").word()) + matcher2_1.getNode("att").word().length();
				}
			}
			if(start < end && start >= 0 && end <= oneSentence.length()){
				completeObject = oneSentence.substring(start, end);
			}else {
				matcher2_1.reset();
				while(matcher2_1.find()){
					ends = oneSentence.indexOf(matcher2_1.getNode("obj").word()) + matcher2_1.getNode("obj").word().length();
					if(oneSentence.indexOf(matcher2_1.getNode("att").word()) < starts){
						starts = oneSentence.indexOf(matcher2_1.getNode("att").word());
					}
				}
				if(starts < ends && starts >= 0 && ends <= oneSentence.length()){
					completeObject = oneSentence.substring(starts, ends);
				}
			}
			
			break;
			
		case 4:
			SemgrexPattern pattern2_2 = 
			SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >CMP {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >SBV ({word:" + object.word() + ";tag:" + object.tag() + "}=obj >> {}=att)");
			SemgrexMatcher matcher2_2 = pattern2_2.matcher(graph);
			while(matcher2_2.find()){
				ends = oneSentence.indexOf(matcher2_2.getNode("obj").word()) + matcher2_2.getNode("obj").word().length();
				if(oneSentence.indexOf(matcher2_2.getNode("att").word()) < starts){
					starts = oneSentence.indexOf(matcher2_2.getNode("att").word());
				}
			}
			if(starts < ends && starts >= 0 && ends <= oneSentence.length()){
				completeObject = oneSentence.substring(starts, ends);
			}
			
			break;
			
		case 5:
			SemgrexPattern pattern3_1 = 
			SemgrexPattern.compile("{tag:/v.*/}=={$} >ADV {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >VOB ({word:" + object.word() + ";tag:" + object.tag() + "}=obj >> {}=att)");
			SemgrexMatcher matcher3_1 = pattern3_1.matcher(graph);
			while(matcher3_1.find()){
				start = oneSentence.indexOf(matcher3_1.getNode("obj").word());
				if(oneSentence.indexOf(matcher3_1.getNode("att").word()) >= end){
					end = oneSentence.indexOf(matcher3_1.getNode("att").word()) + matcher3_1.getNode("att").word().length();
				}
			}
			if(start < end && start >= 0 && end <= oneSentence.length()){
				completeObject = oneSentence.substring(start, end);
			}else {
				matcher3_1.reset();
				while(matcher3_1.find()){
					ends = oneSentence.indexOf(matcher3_1.getNode("obj").word()) + matcher3_1.getNode("obj").word().length();
					if(oneSentence.indexOf(matcher3_1.getNode("att").word()) < starts){
						starts = oneSentence.indexOf(matcher3_1.getNode("att").word());
					}
				}
				if(starts < ends && starts >= 0 && ends <= oneSentence.length()){
					completeObject = oneSentence.substring(starts, ends);
				}
			}
			
			break;
			
		case 6:
			SemgrexPattern pattern3_2 = 
			SemgrexPattern.compile("{tag:/a.*|b.*/}=={$} >ADV {word:" + sentiment.word() + ";tag:" + sentiment.tag() + "} >SBV ({word:" + object.word() + ";tag:" + object.tag() + "}=obj >> {}=att)");
			SemgrexMatcher matcher3_2 = pattern3_2.matcher(graph);
			while(matcher3_2.find()){
				ends = oneSentence.indexOf(matcher3_2.getNode("obj").word()) + matcher3_2.getNode("obj").word().length();
				if(oneSentence.indexOf(matcher3_2.getNode("att").word()) < starts){
					starts = oneSentence.indexOf(matcher3_2.getNode("att").word());
				}
			}
			if(starts < ends && starts >= 0 && ends <= oneSentence.length()){
				completeObject = oneSentence.substring(starts, ends);
			}
			
			break;
			
		default:
			completeObject = object.word();
			break;
		}
		return completeObject;
	}
	
}
