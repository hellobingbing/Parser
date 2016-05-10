package org.bing.sentiment.conll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ANSJ_CoreDict {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
//		String str = "{j=1, ng=0, nr=9, nt=0, q=439, r=0, v=4}";
//		String pos = findMaxPOS(str);
//		System.out.println(pos);
		
		
	}
	/**
	 * 
	 * @param str {j=1, ng=0, nr=9, nt=0, q=439, r=0, v=4}
	 * @return
	 */
	public static String findMaxPOS(String str){
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
	/**
	 * 
	 * @return {j=1, ng=0, nr=9, nt=0, q=439, r=0, v=4}
	 */
	public static String getSomeTags(List<String> dict,String word){
		
		for(String line : dict){
			String[] temp = line.split("\t");
			if(temp[1].equals(word)){
				return temp[5];
			}
		}
		return "null";
		
	}
	/**
	 * 将字典读入内存
	 * @param path
	 * @return
	 */
	public static List<String> readDict(String path) throws IOException{
		List<String> dict = new ArrayList<String>();
		File file = new File(path);
		Scanner scan = new Scanner(file);
		String line = null;
		while(scan.hasNextLine()){
			line = scan.nextLine();
			dict.add(line);
		}
		scan.close();
		return dict;
	}

}
