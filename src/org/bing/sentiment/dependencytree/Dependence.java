package org.bing.sentiment.dependencytree;

import java.util.ArrayList;
import java.util.List;


public class Dependence{
	public static void main(String[] args){
		
		String[] word1 = new String[]{"1","还","d","3","ADV"};
		String[] word2 = new String[]{"2","没","d","3","ADV"};
		String[] word3 = new String[]{"3","发现","v","0","HED"};
		String[] word4 = new String[]{"4","可以","v","5","ADV"};
		String[] word5 = new String[]{"5","退货","v","7","ATT"};
		String[] word6 = new String[]{"6","的","uj","5","RAD"};
		String[] word7 = new String[]{"7","问题","n","3","VOB"};
		
		List<String[]> sent = new ArrayList<String[]>();
		sent.add(word1);
		sent.add(word2);
		sent.add(word3);
		sent.add(word4);
		sent.add(word5);
		sent.add(word6);
		sent.add(word7);
		
		List<Node> nodeList = getNodeList(sent);
		Node root = getTree(nodeList);

		printTree(root,"----");
		
		
	}
	
	public static List<Node> getNodeList(List<String[]> sentence){
		List<Node> nodeList = new ArrayList<Node>();
//		Node tree = null;
		
		for(String[] word : sentence){
			Node node = new Node(Integer.parseInt(word[0]), word[1], word[2], Integer.parseInt(word[3]), word[4]);
			nodeList.add(node);
			
		}
		return nodeList;
		
	}
	
	public static Node getTree(List<Node> nodeList){
		Node root = null;
		for(Node node : nodeList){
			if(node.getParentId() == 0){
				root = node;
				//
				root.setChildNode(nodeList);
			}else {
				node.setParentNode(nodeList);
				node.setChildNode(nodeList);
				node.setSiblingNode(nodeList);
			}
			
		}
		return root;
		
	}
	
	public static void printTree(Node tree,String prefix){
		
		String pre = "----";
		if(tree.getParentId() == 0){
			List<Node> childList = new ArrayList<Node>();
			System.out.println("[" + tree.getId() + "]" + tree.getWord() + "[" + tree.getPos() + "]");
			childList = tree.getChildNode();
			for(Node node : childList){
				printTree(node,prefix);
			}
			
		}else {
			List<Node> childList = new ArrayList<Node>();
			System.out.println(prefix + tree.getDependence() + "[" + tree.getId() + "]" + tree.getWord() + "[" + tree.getPos() + "]");
			childList = tree.getChildNode();
			for(Node node : childList){
				printTree(node,prefix + pre);
			}
			
		}
		
	}
	
	/**
	 * 深度遍历 find a triple
	 * @param root
	 * @param word1
	 * @param word2
	 * @param dependence
	 */
//	public static void traverse(Node root,String word1,String word2,String dependence){
//		if(!root.getWord().equals(word1)){
//			List<Node> childNode = root.getChildNode();
//			for(Node child : childNode){
//				traverse(child, word1, word2, dependence);
//			}
//		}else {
////			getSpecificTriple(root, word1, word2, dependence);
//			List<Node> childNode = root.getChildNode();
//			for(Node child : childNode){
//				if(child.getWord().equals(word2) && child.getDependence().equals(dependence)){
//					getSpecificTriple(root, word1, word2, dependence);
//				}else if (child.getWord().equals(word2) && !child.getDependence().equals(dependence)) {
//					getSpecificTriple(root, word1, word2, dependence);
//				}else if (child.getDependence().equals(dependence) && !child.getWord().equals(word2)) {
//					getSpecificTriple(root, word1, word2, dependence);
//				}
//			}
//			
//		}
//		
//	}
	
//	public static Triple getSpecificTriple(Node root,String word1,String word2,String dependence){
//		return null;
//		
//	}
	
	
};


