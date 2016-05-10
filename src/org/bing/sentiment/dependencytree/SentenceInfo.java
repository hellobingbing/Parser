package org.bing.sentiment.dependencytree;

public class SentenceInfo {
	
	private int sentence_id ;
	
	private int[] word_id ;
	private String[] word_real ;
	private String[] word_pos ;
	
	public SentenceInfo(){
		
	}
	
	public SentenceInfo(int[] word_id,String[] word_real,String[] word_pos){
		this.setWord_id(word_id);
		this.setWord_real(word_real);
		this.setWord_pos(word_pos);
	}
	
	public SentenceInfo(int sentence_id,int[] word_id,String[] word_real,String[] word_pos){
		this.setSentence_id(sentence_id);
		this.setWord_id(word_id);
		this.setWord_real(word_real);
		this.setWord_pos(word_pos);
	}
	
	public void setSentence_id(int sentence_id){
		this.sentence_id = sentence_id;
	}
	public int getSentence_id(){
		return this.sentence_id;
	}
	
	public void setWord_id(int[] word_id){
		this.word_id = word_id;
	}
	public int[] getWord_id(){
		return this.word_id;
	}
	
	public void setWord_real(String[] word_real){
		this.word_real = word_real;
	}
	public String[] getWord_real(){
		return this.word_real;
	}
	
	public void setWord_pos(String[] word_pos){
		this.word_pos = word_pos;
	}
	public String[] getWord_pos(){
		return this.word_pos;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(this.sentence_id + "\t");
		for(int i = 0;i < word_real.length;i++){
			buf.append(word_real[i] + "/" + word_pos[i] + ",");
		}
		return buf.toString();
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
