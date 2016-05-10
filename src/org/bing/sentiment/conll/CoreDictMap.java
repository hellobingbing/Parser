package org.bing.sentiment.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CoreDictMap {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Map<String , String> dictMap = new HashMap<String, String>();
		String dictPath = "." + File.separator + "dict" + File.separator + "ansj-core.dic";
		try {
			dictMap = getDictMap(dictPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<Map.Entry<String, String>> iter = dictMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> me = iter.next();
			System.out.println(me.getKey() + "-->" + me.getValue());
		}
		
	}
	
	public static Map<String, String> getDictMap(String dictPath) throws IOException{
		Map<String, String> dictMap = new HashMap<String, String>();
		File file = new File(dictPath);
		InputStream input = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(input);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while((line = br.readLine()) != null){
			String[] lineArr = line.split("\t");
			dictMap.put(lineArr[1], getMaxPOS(lineArr[5]));
		}
		br.close();
		return dictMap;
		
	}
	
	public static String getMaxPOS(String str){
		if(str.equals("null")){
			return "null";
		}
		int len = str.length();
		String posStr = str.substring(1, len - 1);
		String[] somePOS = posStr.split(", ");
		int max_count = 0;
		String max_pos = null;
		for(String pos : somePOS){
			String[] tmp = pos.split("=");
			if(Integer.parseInt(tmp[1]) >= max_count){
				max_count = Integer.parseInt(tmp[1]);
				max_pos = tmp[0];
			}
			
		}
		return max_pos;
		
	}

}
