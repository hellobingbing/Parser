package org.bing.sentiment.dependencytree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ParseConllFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String filePath = "." + File.separator + "comment_partial" + File.separator + "comment_100_short_parsed.conll";
		List<List<String[]>> lines = null;
		try {
			lines = readConllLines(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printTree(lines);
		
		
	}
	
	public static List<List<String[]>> readConllLines(String filePath) throws IOException{
		List<List<String[]>> lines = new ArrayList<List<String[]>>();
		List<String[]> sentence = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.equals("")){
				lines.add(sentence);
				lines.add(null);
				sentence = new ArrayList<String[]>();
				continue;
				
			}
			String[] lineArr = line.split("\t");
			String[] word_5_Arr = new String[5];
			word_5_Arr[0] = lineArr[0];
			word_5_Arr[1] = lineArr[1];
			word_5_Arr[2] = lineArr[3];
			word_5_Arr[3] = lineArr[6];
			word_5_Arr[4] = lineArr[7];
			sentence.add(word_5_Arr);
		}
		br.close();
	 	return lines;
		
	}
	
	public static void printConllLines(String filePath){
		List<List<String[]>> lines = null;
		try {
			lines = readConllLines(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(List<String[]> sentence : lines){
			if(sentence == null){
				System.out.println();
				continue;
			}
			for(String[] wordArr : sentence){
				for(String str : wordArr){
					System.out.print(str + "\t");
				}
				System.out.println();
			}
			
		}
		
	}
	
	public static void printTree(List<List<String[]>> lines){
		for(List<String[]> sentence : lines){
			if(sentence == null){
				System.out.println();
				continue;
			}
			List<Node> nodeList = Dependence.getNodeList(sentence);
			Node root = Dependence.getTree(nodeList);
			Dependence.printTree(root, "----");
		}
		
	}

	
}
