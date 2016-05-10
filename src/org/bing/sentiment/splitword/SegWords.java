package org.bing.sentiment.splitword;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Scanner;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;

public class SegWords {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String srcPath = "." + File.separator + "comment_partial" + File.separator + "comment_100_short.txt";
		String desPath = "." + File.separator + "comment_partial" + File.separator + "comment_100_short_seged.txt";
		try {
			main_split(srcPath, desPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String srcPath = "." + File.separator + "comment_partial" + File.separator + "comment_100.txt";
//		String desPath = "." + File.separator + "comment_partial" + File.separator + "comment_100_short.txt";
//		try {
//			splitSentence(srcPath, desPath);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public static void main_split(String srcPath,String desPath) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(srcPath))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(desPath))));
		String line = null;
		String segedLine = null;
		while((line = br.readLine()) != null){
			if(line != null && !line.equals("")){
				segedLine = splitWord_toanalysis(line);
				bw.write(segedLine + "\r\n");
			}
			
		}
		bw.close();
		br.close();
		
	}
	
	/**
	 * 对一句话分词,词与词之间用空格分隔�?
	 * org.ansj.splitWord.analysis.ToAnalysis;
	 * @param comment
	 * @return
	 */
	public static String splitWord_toanalysis(String comment){
		if(comment.startsWith("心　　得： ") || comment.startsWith("标　　签： ")){
			comment = comment.substring(6);
		}
		List<Term> wordList = ToAnalysis.parse(comment);
		StringBuffer buf = new StringBuffer();
		int len = wordList.size();
		// 判断分词后的集合长度，如果小�?20返回
//		if(len < 20){
//			return null;
//		}
		int index = 0;
		for(Term term : wordList){
			if(index < len - 1){
				buf.append(term);
				buf.append(" ");
			}else if(index == len - 1){
				buf.append(term);
			}
			index++;
		}
		return buf.toString();
	}
	/**
	 * org.ansj.splitWord.analysis.BaseAnalysis;
	 * @param comment
	 * @return
	 */
	public static String splitWord_baseanalysis(String comment){
		if(comment.startsWith("心　　得： ") || comment.startsWith("标　　签： ")){
			comment = comment.substring(6);
		}
		List<Term> wordList = BaseAnalysis.parse(comment);
		StringBuffer buf = new StringBuffer();
		int len = wordList.size();
		// 判断分词后的集合长度，如果小�?20返回
//		if(len < 20){
//			return null;
//		}
		int index = 0;
		for(Term term : wordList){
			if(index < len - 1){
				buf.append(term);
				buf.append(" ");
			}else if(index == len - 1){
				buf.append(term);
			}
			index++;
		}
		return buf.toString();
	}
	/**
	 * 每一条评论按。？！分为短句
	 * @param srcPath
	 * @param desPath
	 * @throws IOException
	 */
	public static void splitSentence(String srcPath,String desPath) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(srcPath))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(desPath))));
		String line = null;
		while((line = br.readLine()) != null){
			line = line.trim();
			String[] lineArr = line.split("[。？！]");
			for(String str : lineArr){
				bw.write(str + "\r\n");
			}
		}
		bw.close();
		br.close();
		
	}
	

}
