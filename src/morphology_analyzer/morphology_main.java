package morphology_analyzer;

import java.util.ArrayList;
import java.util.Map;

public class morphology_main {
	public static void main(String[] args) {
		//���룺C�������
		//���:����ַָ�����Ԫʽ����
		//TODO ���ôʷ��������ҳ�<id,4>
		//String name_wordAnalyzer[] = {"sentence_assignment.txt"}; 
		//String name_grammerAnalyzer[] = {"G_assignment.txt","word_assignment.txt","assignment"}; //���������ֱ�Ϊ(1)�ķ�(2)����(3)SDT����
		//String name_wordAnalyzer[] = {"sentence_while.txt"}; 
		//String name_grammerAnalyzer[] = {"G_while.txt","word_while.txt","while"}; //���������ֱ�Ϊ(1)�ķ�(2)����(3)SDT����
		String name_wordAnalyzer[] = {"sentence_declare_assignment.txt"}; 
		String name_grammerAnalyzer[] = {"G_declare_assignment.txt","word_declare_assignment.txt","declare_assignment"}; //���������ֱ�Ϊ(1)�ķ�(2)����(3)SDT����
		
		lexical wordAnalyzer = new lexical();
		wordAnalyzer.main(name_wordAnalyzer);
		
		
		//��֤�Ƿ��valueMap��ֵд���˹�������
		Common c = new Common();
		ArrayList<Map<String, String>> valuemap = c.getValueMap();
		System.out.println("��֤�Ƿ�д��valueMap "+valuemap.size());
		for(Map<String, String> s :valuemap) {
			for(String k :s.keySet()) {
				System.out.println(k+" "+s.get(k));
			}
		}
		
		//TODO �ҳ�LR(1)������,�����﷨�Ƶ�����
		TOGrammer g = new TOGrammer();
		g.main(name_grammerAnalyzer);
	}
}
