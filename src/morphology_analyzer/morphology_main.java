package morphology_analyzer;

import java.util.ArrayList;
import java.util.Map;

public class morphology_main {
	public static void main(String[] args) {
		//输入：C语言语句
		//输出:三地址指令或四元式序列
		//TODO 先用词法分析，找出<id,4>
		//String name_wordAnalyzer[] = {"sentence_assignment.txt"}; 
		//String name_grammerAnalyzer[] = {"G_assignment.txt","word_assignment.txt","assignment"}; //三个参数分别为(1)文法(2)单词(3)SDT名称
		//String name_wordAnalyzer[] = {"sentence_while.txt"}; 
		//String name_grammerAnalyzer[] = {"G_while.txt","word_while.txt","while"}; //三个参数分别为(1)文法(2)单词(3)SDT名称
		String name_wordAnalyzer[] = {"sentence_declare_assignment.txt"}; 
		String name_grammerAnalyzer[] = {"G_declare_assignment.txt","word_declare_assignment.txt","declare_assignment"}; //三个参数分别为(1)文法(2)单词(3)SDT名称
		
		lexical wordAnalyzer = new lexical();
		wordAnalyzer.main(name_wordAnalyzer);
		
		
		//验证是否把valueMap的值写进了公共区域
		Common c = new Common();
		ArrayList<Map<String, String>> valuemap = c.getValueMap();
		System.out.println("验证是否写入valueMap "+valuemap.size());
		for(Map<String, String> s :valuemap) {
			for(String k :s.keySet()) {
				System.out.println(k+" "+s.get(k));
			}
		}
		
		//TODO 找出LR(1)分析表,进行语法制导翻译
		TOGrammer g = new TOGrammer();
		g.main(name_grammerAnalyzer);
	}
}
