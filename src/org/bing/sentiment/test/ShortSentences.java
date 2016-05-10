package org.bing.sentiment.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ShortSentences {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		
	}
	
	public static String getTextString(String dirPath) throws IOException{
		StringBuffer buf = new StringBuffer();
		BufferedReader br = null;
		
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for(int i = 0;i < files.length;i++){
			br = new BufferedReader(new InputStreamReader(new FileInputStream(files[i]), "GBK"));
			String line = null;
			while((line = br.readLine()) != null){
				if(!line.equals("")){
					buf.append(line.trim().replaceAll("[ |　|	|&nbsp|&nbsp;|“|”]", "")).append("+++");
				}
			}
		}
		
		br.close();
		return buf.toString();
		
	}
	
	public static void getShortSentence(String text,String outputPath) throws IOException{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath))));
		String[] shortSentences = text.split("[。|?|？|！|!|，|,|；|;|+++]");
		for(String sentence : shortSentences){
			if(sentence != null && !sentence.equals("") && sentence.length() > 4 && sentence.length() < 50){
				if(sentence.contains("纯属个人观点")){
					continue;
				}
				if(sentence.matches("[\\pP|\\pS|\\d]+")){
					continue;
				}
				bw.write(sentence);
				bw.write("\r\n");
			}
		}
		bw.close();
		
	}
	

}
