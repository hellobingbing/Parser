package org.bing.sentiment.conll;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ToANSJ_POS {
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		String srcPath = "." + File.separator + "conll" + File.separator + "test.conll06";
		String tagPath = "." + File.separator + "dict" + File.separator + "tag_beida.txt";
		String dictPath = "." + File.separator + "dict" + File.separator + "ansj-core.dic";
		String desPath = "." + File.separator + "conll" + File.separator + "newtest_new.conll06";
		long startTime = System.currentTimeMillis();
		try {
			convertPOS(srcPath, dictPath, tagPath, desPath);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + "ms");
		
		
	}
	
	public static void convertPOS(String srcPath,String dictPath,String tagPath,String desPath) throws IOException{
		
//		List<String> words = getWords(srcPath);
		List<String> lines = getWordLines(srcPath);
		Map<String, String> dictMap = CoreDictMap.getDictMap(dictPath);
		List<String> tags = getTagList(tagPath);
		File desFile = new File(desPath);
		OutputStream output = new FileOutputStream(desFile);
		OutputStreamWriter osw = new OutputStreamWriter(output);
		BufferedWriter bw = new BufferedWriter(osw);
		for(String line : lines){
			// 如果是换行符
			if(line.equals("\r\n")){
				bw.write("\r\n");
				continue;
			}
			String[] lineArr = line.split("\t");
			String num = lineArr[0];
			String word = lineArr[1];
			String tag = lineArr[3];
			String core_num = lineArr[6];
			String core = lineArr[7];
			
			// 如果是数词m的话就直接用m标记，不用查ansj字典。
			if(tag.equals("m")){
				bw.write(num + "\t" + word + "\t_\tm\t_\t_\t" + core_num + "\t" + core + "\t_\t_\r\n");
				continue;
			}
			// 在字典HashMap中找到该词的词性
			if(dictMap.containsKey(word) && !dictMap.get(word).equals("null")){
				String pos = dictMap.get(word);
				bw.write(num + "\t" + word + "\t_\t" + pos + "\t_\t_\t" + core_num + "\t" + core + "\t_\t_\r\n");
			}else{
				// 在字典中找不到的话
				// test中人名nh用ansj的nr替换
				String newTag = null;
				if(tag.equals("nh")){
					newTag = "nr";
				}else if(tag.equals("nd")){
					// test中方位词nd用ansj的f替换
					newTag = "f";
				}else if(tag.equals("nl")){
					// test中地名nl用ansj中的ns替换
					newTag = "ns";
				}else if(tag.equals("ni")){
					// test中组织名ni用ansj中的nt替换
					newTag = "nt";
				}else if(tag.equals("nt")){
					// test中时间名词nt用ansj中的t替换
					newTag = "t";
				}else if(tags.contains(tag)){
					// 如果该词性在ansj中也有，并且标记含义也相同，即同时出现的话就用原词性代替
					newTag = tag;
				}else {
					// test中特有的词性
					newTag = "***";
					}
				bw.write(num + "\t" + word + "\t_\t" + newTag + "\t_\t_\t" + core_num + "\t" + core + "\t_\t_\r\n");
				}
			
			}
		bw.close();
		osw.close();
		output.close();
		
	}

	/**
	 * test每一行的list	空行\r\n
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static List<String> getWordLines(String path) throws IOException{
		List<String> lines = new ArrayList<String>();
		File file = new File(path);
		Scanner scan = new Scanner(file);
		String line = null;
		while(scan.hasNextLine()){
			line = scan.nextLine();
			if(line.equals("")){
				lines.add("\r\n");
				continue;
			}
			lines.add(line);
		}
		scan.close();
		return lines;
	}
	/**
	 * 读入ansj中的所有词性
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static List<String> getTagList(String path) throws IOException{
		List<String> tags = new ArrayList<String>();
		File file = new File(path);
		Scanner scan = new Scanner(file);
		String line = null;
		while(scan.hasNextLine()){
			line = scan.nextLine();
			tags.add(line);
		}
		scan.close();
		return tags;
	}

}
