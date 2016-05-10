package org.bing.sentiment.dependencytree;

import java.util.ArrayList;
import java.util.List;

public class Node {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	private int id ;
	private String word ;
	private String pos ;
	private int parentId ;
	private String dependence ;
	private List<Node> childNodeList = new ArrayList<Node>();
	private List<Node> siblingNodeList = new ArrayList<Node>();
	private Node parentNode ;
	
	public Node(){
		
	}
	public Node(int id,String word,String pos,int parentId,String dependence){
		this.setId(id);
		this.setWord(word);
		this.setPos(pos);
		this.setParentId(parentId);
		this.setDependence(dependence);
	}
	
	public void setId(int id){
		this.id = id;
	}
	public int getId(){
		return this.id;
	}
	
	public void setWord(String word){
		this.word = word;
	}
	public String getWord(){
		return this.word;
	}
	
	public void setPos(String pos){
		this.pos = pos;
	}
	public String getPos(){
		return this.pos;
	}
	
	public void setParentId(int parentId){
		this.parentId = parentId;
	}
	public int getParentId(){
		return this.parentId;
	}
	
	public void setDependence(String dependence){
		this.dependence = dependence;
	}
	public String getDependence(){
		return this.dependence;
	}
	 
	public void setChildNode(List<Node> nodeList){
		for(Node node : nodeList){
			if(this.id == node.getParentId()){
				this.childNodeList.add(node);
			}
		}
		
	}
	public List<Node> getChildNode(){
		return this.childNodeList;
	}
	
	public void setSiblingNode(List<Node> nodeList){
		for(Node node : nodeList){
			if(this.parentId == node.getParentId() && this.id != node.getId()){
				this.siblingNodeList.add(node);
			}
		}
		
	}
	public List<Node> getSiblingNode(){
		return this.siblingNodeList;
	}
	
	public void setParentNode(List<Node> nodeList){
		for(Node node : nodeList){
			if(this.parentId == node.getId()){
				this.parentNode = node;
			}
		}
		
	}
	public Node getParentNode(){
		return this.parentNode;
	}
	
	public String toString(){
		return this.id + "\t" + this.word + "\t" + this.pos + "\t" + this.parentId + "\t" + this.dependence;
	}
	
	
	
}
