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
	static Map<String,String> token = new LinkedHashMap();//�洢������б�
	//�����ý����ķ�ʽ����������" "�ָ���ʶ�� 
	static ArrayList<String> result = new  ArrayList<>();
	public static void main(String[] args) {
		//��ȡ�ļ����б�
		//Step1:�ѹؼ��ֶ����б�
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
		
		//Step2:������һ��һ���Ķ�ȡtxt�ļ�
		//String fileName ="src/sentence_assignment.txt";
		String fileName ="src/"+args[0];
		char []sentence = new char[50];
		sentence = tt.readText(fileName);
		//Step3:���ֿ�ʼɨ��
		boolean success =  tt.scannerSentence(sentence);
		//Step4�� ��ӡ�����
		tt.printResult(result);
		if(success) {
			System.out.println("�ɹ�");
		}else {
			System.out.println("ʧ��");
		}
		//�ѽ��д���ĵ�
		writeToText();
	}
	
	//�ѽ��д���ĵ�
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
	
	// ��ӡ�����
	public void printResult(ArrayList<String> result) {
		 for (String s : result) { 
			   System.out.println("<" +s+">"); 
		 }
	}
	
	//������һ��һ���Ķ�ȡtxt�ļ�
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
			//�鿴��ȡ���Ƿ���ȷ
			//System.out.println(Arrays.toString(sentence));
			return sentence;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sentence;
	}
	
	
	public boolean scannerSentence(char []word) {
		/**
		 * ���ַ��������������Ҫ�������´���
		 * (1)����/*ע�ͺ�//ע�ͣ���Ҫ���Ե�
		 * (2)����_��ͷ��Ϊ��ʶ��
		 * (3)����Ӣ����ĸ��ͷ�ģ���Ҫ���ճ�������word�����ж��Ǳ�ʶ�����ǹؼ���
		 * (4)�������ֿ�ͷ����������������
		 * (5)���ڽ��޷�������һһʶ��
		 * (6)���ڿհ��ַ�������
		 *@param ��ȡ�����ַ���
		 */
		lexical tt = new lexical();
		//�������$���ž�ֱ��ֹͣ
		for(int i = 0;(i<word.length-1 || word[i]!='$');) {
			//���˵�ע��
			if(word[i]=='/' && word[i+1]=='/') {// //ע��
				while(word[i] !='\n') {
					i++;//���˵���ߵĶ���
					System.out.print(word[i]);
				}
				System.out.print("\n");
			}else if(word[i]=='/' && word[i+1]=='*') {
				System.out.print("�����˵�����䣺");
				i=i+2;
				while(word[i] != '*' || word[i + 1] != '/') {
					System.out.print(word[i]);
					i++;//���˵���ߵĶ���
					if(word[i]=='$') {
						System.out.print("ע�ͳ���û���ҵ� */ \n");
						return false;
						//System.exit(0);
					}
				}
				i = i+2;//��һ��б��Ҳ��Ҫ��
				System.out.print("\n");
			}else if(word[i] == ' ' || word[i] == '\n' || word[i] == '\t'){//���˵������ַ�
				//System.out.println("�����ַ���"+word[i]);
				i++;
			}else if(isAlpha(word[i])) {//�������ĸ��ͷҪ�ж��ǲ��ǹؼ��ֻ�������������
				i = tt.alphaAction(word[i],i,word,false);
			}else if(word[i]=='_') {//�»��߿�ͷ-->����
				i = tt.alphaAction(word[i],i,word,true);
			}else if(isDigit(word[i])) {//������
				i = tt.digitAction(word[i],i,word);
			}else if(word[i]=='$') {
				break;
			}else{// �����
				i = tt.otherAction(word[i],i,word);
			}
			
		}
		return true;
	}
	
	//�ж�����ַ��ǲ�����ĸ
	public boolean isAlpha(char c) {
		if((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
			return true;
		}else {
			return false;
		}
	}
	
	//�ж�����ַ��ǲ�������
    public static boolean isDigit(char c){
        if(c >= '0' && c <= '9')
            return true;
        return false;
    }
    
    //�ж��������������ǹؼ��֡������µ����,���ĸ�����˵���ǲ�����������
    public int alphaAction(char c,int i,char word[],boolean isDeclareWord){
        /**
         * ֻҪ����ĸ���»��ߡ����֣��Ͳ��ϵĶ�ȡ��
         * ����ȡ��ɱ��word��ʱ���ж����word�ǲ��ǹؼ���
         * ����Ѿ���ȡ���»��ߣ�Ҳ����������������������isDeclareWord�������
         * @param c��ǰ��ȡ�����ַ�
         * @param i��ǰ��ȡ�����±�
         * @param word �����ַ���
         * @param true��ʾ��ǰ��ȡ�����»��ߣ��϶����ǹؼ���
         * @return ������һ����Ҫ�������±�
         */
    	int newStart=0;
    	//��ArrayList��װÿ���ַ���Ȼ��forѭ������һ������    	
    	ArrayList<String> collectWord = new ArrayList();
    	while((isAlpha(c)) || (isDigit(c)) || c == '_') {//�ɴ�Сд��ĸ�������Լ��»������
    		collectWord.add(String.valueOf(c));
    		i=i+1;
    		c = word[i];
    	}
    	String oneWholeWord = "";
    	for(String s:collectWord) {
    		oneWholeWord = oneWholeWord +s;
    	}
    	//ѧ��ʹ��StringBuffer
    	/*
    	StringBuffer collectWord = new StringBuffer();
    	while((isAlpha(c)) || (isDigit(c)) || c == '_') {
    		collectWord.append(c);
    		i=i+1;
    		c = word[i];
    	}
    	String oneWholeWord = word.toString();
    	*/
    	//��һ�����ʱ�����������ʼ�ж���ʲô����
    	if(isDeclareWord==true) {
    		String matchResult = "IDN,"+oneWholeWord;
    		result.add(matchResult);
    	}else {
    		if(specailWordList.contains(oneWholeWord)) {//�����еģ����ǹؼ���
        		String matchResult = oneWholeWord.toUpperCase()+","+"-,"+oneWholeWord;
        		System.out.println(oneWholeWord);
        		result.add(matchResult);
    		}else {//����û�еģ����Ǳ���
        		String matchResult = "IDN,"+oneWholeWord+","+"id";
        		System.out.println("����"+oneWholeWord);
        		result.add(matchResult);
    		}
    	}
    	newStart = i;
    	return newStart;
    }
    
    //������ǰ����ͬ
    //���ִ���
    public int digitAction(char c,int i,char word[]){
    	int newStart=0;
    	StringBuffer digit = new StringBuffer();
    	while(isDigit(c)) {
    		digit.append(c);
    		i=i+1;
    		c = word[i];
    	}
    	String oneWholeDigit = digit.toString();
		String matchResult = "CONST,"+oneWholeDigit+","+"����";
		System.out.println("����"+oneWholeDigit);
		result.add(matchResult);
    	newStart = i;
    	return newStart;
    }
    
    //�������ŵĴ���
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
