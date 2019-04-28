package morphology_analyzer;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class lexical {
	static ArrayList<String> specailWordList = new  ArrayList<>();
	static Map<String,String> token = new LinkedHashMap();//存储结果的列表
	//可以用解析的方式，把它按照" "分割来识别 
	static ArrayList<String> result = new  ArrayList<>();
	public static void main(String[] args) {
		//读取文件的列表
		//Step1:把关键字读入列表
		lexical tt = new lexical();
		String [] specailWord = {"auto", "break", "case", "char", "const", "continue",
		                         "default", "do", "double", "else", "enum", "extern",
		                         "float", "for", "goto", "if", "int", "long",
		                         "register", "return", "short", "signed", "sizeof", "static",
		                         "struct", "switch", "typedef", "union", "unsigned", "void",
		                         "volatile", "while","then"};
		
		for(String s : specailWord) {
			specailWordList.add(s);
		}
		
		//Step2:按照字一个一个的读取txt文件
		//String fileName ="src/sentence_assignment.txt";
		String fileName ="src/"+args[0];
		char []sentence = new char[50];
		sentence = tt.readText(fileName);
		//Step3:逐字开始扫描
		boolean success =  tt.scannerSentence(sentence);
		//Step4： 打印所结果
		tt.printResult(result);
		if(success) {
			System.out.println("成功");
		}else {
			System.out.println("失败");
		}
		//把结果写进文档
		writeToText();
	}
	
	//把结果写进文档
	static void writeToText() {
		Common c = new Common();
		for(String s : result) {
			Map<String, String> oneValue = new LinkedHashMap<>();
			String[] splitResult  =  s.split(",");
			if(splitResult[0].equals("IDN")) {
			oneValue.put(splitResult[2], splitResult[1]);
			c.addValueMap(oneValue);
			}
			System.out.println(splitResult[2]);
		}
	}
	
	// 打印所结果
	public void printResult(ArrayList<String> result) {
		 for (String s : result) { 
			   System.out.println("<" +s+">"); 
		 }
	}
	
	//按照字一个一个的读取txt文件
	public char[] readText(String fileName) {
		char []sentence = new char[200];
		int i = 0;
		try {
			Reader reader = null;  
			reader = new InputStreamReader(new FileInputStream(fileName)); 
			int tempchar;  
			while((tempchar = reader.read()) != -1) {
				if (((char) tempchar) != '\r') {  
					sentence[i] = (char)(tempchar);
					i++;  
				}
			}
			reader.close();  
			//查看读取的是否正确
			//System.out.println(Arrays.toString(sentence));
			return sentence;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sentence;
	}
	
	
	public boolean scannerSentence(char []word) {
		/**
		 * 对字符数组逐个遍历，要进行以下处理
		 * (1)对于/*注释和//注释，需要忽略掉
		 * (2)对于_打头的为标识符
		 * (3)对于英文字母开头的：需要接收成完整的word，再判断是标识符还是关键字
		 * (4)对于数字开头，接收完整的数字
		 * (5)对于界限符，进行一一识别
		 * (6)对于空白字符，忽略
		 *@param 读取到的字符串
		 */
		lexical tt = new lexical();
		//如果遇到$符号就直接停止
		for(int i = 0;(i<word.length-1 || word[i]!='$');) {
			//过滤掉注释
			if(word[i]=='/' && word[i+1]=='/') {// //注释
				while(word[i] !='\n') {
					i++;//过滤掉后边的东西
					System.out.print(word[i]);
				}
				System.out.print("\n");
			}else if(word[i]=='/' && word[i+1]=='*') {
				System.out.print("被过滤掉的语句：");
				i=i+2;
				while(word[i] != '*' || word[i + 1] != '/') {
					System.out.print(word[i]);
					i++;//过滤掉后边的东西
					if(word[i]=='$') {
						System.out.print("注释出错，没有找到 */ \n");
						return false;
						//System.exit(0);
					}
				}
				i = i+2;//另一个斜杠也不要了
				System.out.print("\n");
			}else if(word[i] == ' ' || word[i] == '\n' || word[i] == '\t'){//过滤掉无用字符
				//System.out.println("无用字符："+word[i]);
				i++;
			}else if(isAlpha(word[i])) {//如果是字母开头要判断是不是关键字或者是声明变量
				i = tt.alphaAction(word[i],i,word,false);
			}else if(word[i]=='_') {//下划线开头-->变量
				i = tt.alphaAction(word[i],i,word,true);
			}else if(isDigit(word[i])) {//数字类
				i = tt.digitAction(word[i],i,word);
			}else if(word[i]=='$') {
				break;
			}else{// 界符号
				i = tt.otherAction(word[i],i,word);
			}
			
		}
		return true;
	}
	
	//判断这个字符是不是字母
	public boolean isAlpha(char c) {
		if((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
			return true;
		}else {
			return false;
		}
	}
	
	//判断这个字符是不是数字
    public static boolean isDigit(char c){
        if(c >= '0' && c <= '9')
            return true;
        return false;
    }
    
    //判断是声明变量还是关键字。返回新的起点,第四个参数说明是不是声明变量
    public int alphaAction(char c,int i,char word[],boolean isDeclareWord){
        /**
         * 只要是字母、下划线、数字，就不断的读取。
         * 当读取完成变成word的时候，判断这个word是不是关键字
         * 如果已经读取到下划线，也共用这个函数，所以添加了isDeclareWord这个变量
         * @param c当前读取到的字符
         * @param i当前读取到的下标
         * @param word 整个字符串
         * @param true表示当前读取到了下划线，肯定不是关键字
         * @return 返回下一个需要遍历的下标
         */
    	int newStart=0;
    	//用ArrayList来装每个字符，然后for循环串成一个句子    	
    	ArrayList<String> collectWord = new ArrayList();
    	while((isAlpha(c)) || (isDigit(c)) || c == '_') {//由大小写字母、数字以及下划线组成
    		collectWord.add(String.valueOf(c));
    		i=i+1;
    		c = word[i];
    	}
    	String oneWholeWord = "";
    	for(String s:collectWord) {
    		oneWholeWord = oneWholeWord +s;
    	}
    	//学会使用StringBuffer
    	/*
    	StringBuffer collectWord = new StringBuffer();
    	while((isAlpha(c)) || (isDigit(c)) || c == '_') {
    		collectWord.append(c);
    		i=i+1;
    		c = word[i];
    	}
    	String oneWholeWord = word.toString();
    	*/
    	//对一个单词遍历结束，开始判断是什么类型
    	if(isDeclareWord==true) {
    		String matchResult = "IDN,"+oneWholeWord;
    		result.add(matchResult);
    	}else {
    		if(specailWordList.contains(oneWholeWord)) {//表里有的，就是关键字
        		String matchResult = oneWholeWord.toUpperCase()+","+"-,"+oneWholeWord;
        		System.out.println(oneWholeWord);
        		result.add(matchResult);
    		}else {//表里没有的，就是变量
        		String matchResult = "IDN,"+oneWholeWord+","+"id";
        		System.out.println("变量"+oneWholeWord);
        		result.add(matchResult);
    		}
    	}
    	newStart = i;
    	return newStart;
    }
    
    //参数与前边相同
    //数字处理
    public int digitAction(char c,int i,char word[]){
    	int newStart=0;
    	StringBuffer digit = new StringBuffer();
    	while(isDigit(c)) {
    		digit.append(c);
    		i=i+1;
    		c = word[i];
    	}
    	String oneWholeDigit = digit.toString();
		String matchResult = "CONST,"+oneWholeDigit+","+"常量";
		System.out.println("常量"+oneWholeDigit);
		result.add(matchResult);
    	newStart = i;
    	return newStart;
    }
    
    //其他符号的处理
    public int otherAction(char c,int i,char word[]){
    	String matchResult = "";
    	if(c=='(') {
    		matchResult = "SLP,-,(";
    		result.add(matchResult);
    		i++;
    	}else if(c==')'){
    		matchResult = "SRP,-,)";
    		result.add(matchResult);
    		i++;
    	}else if(c=='{'){
    		matchResult = "LP,-";
    		result.add(matchResult);
    		i++;
    	}else if(c=='}'){
    		matchResult = "RP,-";
    		result.add(matchResult);
    		i++;
    	}else if(c=='+'){
    		if(word[i+1]=='+') {
        		matchResult = "INC,-";
        		result.add(matchResult);
    			i = i+2;
    		}else {
        		matchResult = "ADD,-,+";
        		System.out.println("+");
        		result.add(matchResult);
        		i++;
    		}
    	}else if(c=='-'){
    		if(word[i+1]=='-') {
        		matchResult = "IND,-,--";
        		result.add(matchResult);
    			i = i+2;
    		}else {
        		matchResult = "SUB,-,-";
        		result.add(matchResult);
        		i++;
    		}
    	}else if (c=='*') {
    		matchResult = "MUL,-,*";
    		result.add(matchResult);
    		i++;
    	}else if (c=='/') {
    		matchResult = "DIV,-";
    		result.add(matchResult);
    		i++;
    	}else if (c=='!') {// != !
    		if(word[i+1]=='=') {
        		matchResult = "NE,-";
        		result.add(matchResult);
    			i = i+2;
    		}else {
        		matchResult = "NOT,-";
        		result.add(matchResult);
        		i++;
    		}
    	}else if (c=='=') {// != !
    		if(word[i+1]=='=') {
        		matchResult = "EE,-,==";
        		System.out.println("==");
        		result.add(matchResult);
    			i = i+2;
    		}else {
        		matchResult = "E,-,=";
        		System.out.println("=");
        		result.add(matchResult);
        		i++;
    		}
    	}else if (c==';') {
    		matchResult = "SEMI,-,;";
    		System.out.println(";");
    		result.add(matchResult);
    		i++;
    	}else if (c=='>') {
    		matchResult = "More,-,>";
    		System.out.println(";");
    		result.add(matchResult);
    		i++;
    	}else if (c=='<') {
    		matchResult = "LESS,-,<";
    		System.out.println(";");
    		result.add(matchResult);
    		i++;
    	}
    	return i;
    }
    
}
