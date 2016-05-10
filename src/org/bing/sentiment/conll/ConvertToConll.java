package org.bing.sentiment.conll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class ConvertToConll {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String srcPath = "." + File.separator + "comment_partial" + File.separator + "comment_100_short_seged.txt";
		String desPath = "." + File.separator + "comment_partial" + File.separator + "comment_100_short_conll.conll";
		
		
		try {
			getConllFile(srcPath, desPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 将分词后的comments转化成conll格式的文本
	 * @param srcPath
	 * @param desPath
	 */
	public static void getConllFile(String srcPath, String desPath) throws IOException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(srcPath))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(desPath))));
		String line = null;
		while((line = br.readLine()) != null){
			line = line.trim();
			String[] lineArr = line.split(" ");
			int index = 1;
			for(String str : lineArr){
				String[] word = str.split("/");
				if(word.length == 2){
					bw.write(index + "\t" + word[0] + "\t_\t" + word[1] + "\t_\t_\t0\tnull\t_\t_\r\n");
					index++;
				}
//				index++;
			}
			bw.write("\r\n");
		}
		
		bw.close();
		br.close();
		
	}

}
