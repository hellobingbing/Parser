package org.bing.sentiment.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InfoMap {
	
	public static final String filePath = "." + File.separator + "test" + File.separator + "sogou_reduced" + File.separator + "ShortSentences_sogou_reduced.txt";
	public static final String dictPath = "." + File.separator + "test" + File.separator + "P_N_Hownet.txt";
	public static final String negWordsPath = "." + File.separator + "test" + File.separator + "Negative_Words.txt";
	
	private static Map<Integer, String> sentencesMap = null;
	private static Map<String, String> dictMap = null;
	private static Map<String, String> negWordsMap = null;
//	private static Map<SemgrexPattern, String> patternMap = null;
	
	public static Map<Integer, String> getSentencesMap(String filePath) throws IOException{
		sentencesMap = new HashMap<Integer, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)),"UTF-8"));
		String line = null;
		int i = 1;
		while((line = br.readLine())!= null){
			if(!line.equals("")){
				sentencesMap.put(i, line);
				i++;
			}
		}
		br.close();
		return sentencesMap;
	}
	
	public static Map<String, String> getDictMap(String dictPath) throws IOException{
		dictMap = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dictPath)),"UTF-8"));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.startsWith("#")){
				continue;
			}
			String[] lineArr = line.split("\\s+");
			if(lineArr[0].length() > 1){
				dictMap.put(lineArr[0], lineArr[1]);
			}
		}
		br.close();
		return dictMap;
	}
	
	public static Map<String, String> getNegWordsMap(String negWordsPath) throws IOException{
		negWordsMap = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(negWordsPath)),"UTF-8"));
		String line = null;
		while((line = br.readLine()) != null){
			negWordsMap.put(line, null);
		}
		br.close();
		return negWordsMap;
	}
	
//	public static Map<SemgrexPattern, String> getPatternMap(String dictPath){
//		
//	}

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		Map<String, String> map = InfoMap.getNegWordsMap(InfoMap.negWordsPath);
		Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> me = iter.next();
			System.out.println(me.getKey() + "-->" + me.getValue());
		}
		
		
	}

}
