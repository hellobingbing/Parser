package org.bing.sentiment.dependencytree;

import java.util.ArrayList;
import java.util.List;

public class CompactString {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String[] word1 = new String[]{"1","没","d","2","ADV"};
		String[] word2 = new String[]{"2","觉得","v","0","HED"};
		String[] word3 = new String[]{"3","他","r","5","ATT"};
		String[] word4 = new String[]{"4","的","uj","3","RAD"};
		String[] word5 = new String[]{"5","屏幕","n","6","SBV"};
		String[] word6 = new String[]{"6","好","a","2","VOB"};
		List<String[]> sent = new ArrayList<String[]>();
		sent.add(word1);
		sent.add(word2);
		sent.add(word3);
		sent.add(word4);
		sent.add(word5);
		sent.add(word6);
		
//		String[] word1 = new String[]{"1","还","d","3","ADV"};
//		String[] word2 = new String[]{"2","没","d","3","ADV"};
//		String[] word3 = new String[]{"3","发现","v","0","HED"};
//		String[] word4 = new String[]{"4","可以","v","5","ADV"};
//		String[] word5 = new String[]{"5","退货","v","7","ATT"};
//		String[] word6 = new String[]{"6","的","uj","5","RAD"};
//		String[] word7 = new String[]{"7","问题","n","3","VOB"};
//		List<String[]> sent = new ArrayList<String[]>();
//		sent.add(word1);
//		sent.add(word2);
//		sent.add(word3);
//		sent.add(word4);
//		sent.add(word5);
//		sent.add(word6);
//		sent.add(word7);
		
		List<Node> nodeList = Dependence.getNodeList(sent);
		Node root = Dependence.getTree(nodeList);

//		Dependence.printTree(root,"----");
		StringBuffer buf = new StringBuffer();
		
		getCompactString(root, buf, true);
		System.out.println(buf.toString());
		
	}
	/**
	 * 得到一句话对应的compactstring,然后可以根据这个string生成semanticgraph
	 * @param root 树的根节点
	 * @param buf 存字符串
	 * @param showPOS 是否显示词性
	 */
	public static void getCompactString(Node root, StringBuffer buf, boolean showPOS){
		if(root.getChildNode().size() != 0){
			buf.append("[");
		}
		
		buf.append(root.getWord());
		if(showPOS){
			buf.append("/");
			buf.append(root.getPos());
		}
		
		List<Node> childList = null;
		childList = root.getChildNode();
		for(Node child : childList){
			buf.append(" " + child.getDependence() + ">");
			getCompactString(child, buf, showPOS);
		}
		// 右半部分括号"]"根据不是叶子节点来确定添加
		if(root.getChildNode().size() != 0){
			buf.append("]");
		}
		
	}
	
	public static void getCompactString_kuohao(Node root, StringBuffer buf, boolean showPOS){
		buf.append("[");
		buf.append(root.getWord());
		if(showPOS){
			buf.append("/");
			buf.append(root.getPos());
		}
		
		List<Node> childList = null;
		childList = root.getChildNode();
		for(Node child : childList){
			buf.append(" " + child.getDependence() + ">");
			getCompactString_kuohao(child, buf, showPOS);
		}
		buf.append("]");
	}
	

}
