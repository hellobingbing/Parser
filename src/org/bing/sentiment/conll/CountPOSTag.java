package org.bing.sentiment.conll;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CountPOSTag {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String path_testconll06 = "E:" + File.separator + "JAVA" + File.separator + "YaraParser" + File.separator + "data" + File.separator + "test.conll06";
		String path_commentsconll = "E:" + File.separator + "JAVA" + File.separator + "ANSJ" + File.separator + "comment" + File.separator + "comments.conll";
		String path_commentparsed = "E:" + File.separator + "JAVA" + File.separator + "ANSJ" + File.separator + "comment" + File.separator + "comments_parsed.conll.tmp";
		Set<String> POSTag_testconll06 = null;
		Set<String> POSTag_commentsconll = null;
		Set<String> Tag_commentsparsed = null;
//		try {
//			POSTag_testconll06 = count(path_testconll06);
//			POSTag_commentsconll = count(path_commentsconll);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(POSTag_testconll06.size());
//		for(String tag : POSTag_testconll06){
//			System.out.println(tag);
//		}
//		System.out.println();
//		System.out.println(POSTag_commentsconll.size());
//		for(String tag : POSTag_commentsconll){
//			System.out.println(tag);
//		}
		
		try {
			Tag_commentsparsed = countParsing(path_commentparsed);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("the number of tags is : " + Tag_commentsparsed.size());
		for(String str : Tag_commentsparsed){
			System.out.println(str);
		}
		
	}
	
	/**
	 * 得出所有的词性
	 * @param pathName
	 * @return 词性的集合
	 * @throws IOException
	 */
	public static HashSet<String> count(String pathName)throws IOException{
		HashSet<String> posSet = new HashSet<String>();
		File file = new File(pathName);
		Scanner scan = new Scanner(file);
		String line = null;
		while(scan.hasNextLine()){
			line = scan.nextLine();
			if(line.equals("")){
				continue;
			}
			String[] lineArr = line.split("\\t");
			posSet.add(lineArr[3]);
			
		}
		scan.close();
		return posSet;
	}
	
	public static HashSet<String> countParsing(String pathName) throws IOException{
		HashSet<String> set = new HashSet<String>();
		File file = new File(pathName);
		Scanner scan = new Scanner(file);
		String line = null;
		while(scan.hasNextLine()){
			line = scan.nextLine();
			if(line.equals("")){
				continue;
			}
			set.add(line.split("\\t")[1]);
			
		}
		return set;
	}

}
