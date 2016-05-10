package org.bing.sentiment.dependencytree;

import java.util.ArrayList;
import java.util.List;

public class MatchParentheses {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String str = "123((2*(1 + 2)*5/3)))((1 + 1)))*((2 + 2)123";
		List<MyStr> list = new ArrayList<MyStr>();
		List<String> newList = new ArrayList<String>();
//		do{
//			str = getString(str, list);
//		}while(str != null && str.indexOf("(") != -1);
//		
//		for(MyStr myStr : list){
//			System.out.println(myStr.str.substring(myStr.startIndex, myStr.endIndex + 1));
//		}
		
		getCompleteParentheses(str, list);
		newList = getParenthesesString(list);
		for(String newStr : newList){
			System.out.println(newStr);
		}
		
	}
	/**
	 * 返回最长的一个成对()中嵌套的所有()
	 * @param str
	 * @param list
	 * @return
	 */
	public static void getNestedParentheses(String str,List<MyStr> list){
		char[] strArr = str.toCharArray();
		boolean isStart = false;
		MyStr myStr = new MyStr();
		myStr.str = str;
		for(int i = 0;i < strArr.length;i++){
			char c = strArr[i];
			if(c == '('){
				if(!isStart){
					myStr.startIndex = i;
					isStart = true;
				}
				myStr.layer++;
			}else if (c == ')' && myStr.layer > 0) {
				myStr.layer--;
				if(myStr.layer == 0){
					myStr.endIndex = i;
					list.add(myStr);
					// 递归寻找成对的()字符串中并列的
//					if(i <= strArr.length - 1){
//						String newStr = str.substring(i + 1);
//						do{
//							newStr = getString(newStr, list);
//						}while(newStr != null && newStr.indexOf('(') != -1);
//					}
					// 直到扫描一遍字符串为止
					break;
				}
			}
		}
		if(myStr.endIndex != 0){
			String newStr = str.substring(myStr.startIndex + 1, myStr.endIndex);
			getNestedParentheses(newStr, list);
		}
		
		
	}
	/**
	 * 将一个长字符串，截取出并列的整个成对的(),存入list
	 * @param str
	 * @param list
	 */
	public static void getCompleteParentheses(String str,List<MyStr> list){
		char[] strArr = str.toCharArray();
		boolean isStart = false;
		MyStr myStr = new MyStr();
		myStr.str = str;
		for(int i = 0;i < strArr.length;i++){
			char c = strArr[i];
			if(c == '('){
				if(!isStart){
					myStr.startIndex = i;
					isStart = true;
				}
				myStr.layer++;
			}else if (c == ')' && myStr.layer > 0) {
				myStr.layer--;
				if(myStr.layer == 0){
					myStr.endIndex = i;
					list.add(myStr);
					// 递归寻找成对的()字符串中并列的
					if(i <= strArr.length - 1){
						String newStr = str.substring(i + 1);
						
//						do{
//							getCompleteString(newStr, list);
//						}while(newStr != null && newStr.indexOf('(') != -1);
						if(newStr != null && newStr.indexOf('(') != -1){
							getCompleteParentheses(newStr, list);
						}
					}
					// 直到扫描一遍字符串为止
					break;
				}
			}
		}
	}
	
	public static List<String> getParenthesesString(List<MyStr> list){
		List<String> newList = new ArrayList<String>();
		for(MyStr myStr : list){
			newList.add(myStr.str.substring(myStr.startIndex, myStr.endIndex + 1));
		}
		return newList;
	}
	

}

class MyStr{
	int startIndex ;
	int endIndex ;
	int layer ;
	String str ;
};
