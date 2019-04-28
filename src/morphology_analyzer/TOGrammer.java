package morphology_analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOGrammer {
	static ArrayList<String> grammerList = new ArrayList<>();
	static ArrayList<String> grammerListFirst = new ArrayList<>();// S'->.S
	static ArrayList<String> grammerListLast = new ArrayList<>();// S'->S.
	static ArrayList<String> gotoToken = new ArrayList<>();// goto表的符号
	static ArrayList<String> actionToken = new ArrayList<>();// action表的符号
	static ArrayList<ArrayList<String[]>> projectSet = new ArrayList<>();// 存放所有state
	static Map<String, String[]> stardardConbineGrammer = new HashMap<>();// 文法用S->A|aA表示
	static Map<String, Set<String>> firstList = new TreeMap<String, Set<String>>();// first集合
	static ArrayList<String[]> gotoList = new ArrayList<>();// 跳转表 {"本状态 活前缀 跳转状态"}、{}
	static Map<String, Integer> token2Number = new LinkedHashMap<>();// a在表的第一个，b在表的第二个
	static String startToken ;
	static ArrayList<String[]> grammerBlock = new ArrayList<>();
	static ArrayList<String> termimalToken = new ArrayList<>();// 终结符号有哪些
	static Stack<String> stateStack = new Stack<String>();
	static Stack<String> tokenStack = new Stack<String>();
	static Stack<String> valueStack = new Stack<String>();//存放id的真实值,e.g.:id->x
	static String[][] analyList = new String[projectSet.size()][token2Number.size()];
	static int newTempCount = 0;//生成新的变量名称序号
	static int newLempCount = 0;//生成新的变量名称序号
	static Map<String, Integer> valueTokenMatch = new LinkedHashMap<>();//<L1,1>
	static ArrayList<String> threeAddress = new ArrayList<>();// 装三地址的列表
	static ArrayList<String> exchangethreeAddress = new ArrayList<>();// 装三地址的列表
	public static void main(String[] args) {
		System.out.println("----------------Start------------");
		String GName[] = {"G_assignment.txt","G_declear.txt","G_declare_assignment.txt","G_while.txt","G.txt"};
		String wordName[] = {"word_assignment.txt","word_declare.txt","word_declare_assignment.txt","word_while.txt","word.txt"};
		String oneWordName = "src/"+args[1];
		
		// TODO 读取文法
		readGrammer("src/"+args[0]);// 输入的必须是增广文法
		
		//TODO 构建block
		termimalToken.add("num");
		termimalToken.add("int");
		termimalToken.add("id");
		termimalToken.add("E'");
		termimalToken.add("E''");
		termimalToken.add("else");
		termimalToken.add("if");
		termimalToken.add("&&");
		termimalToken.add(";");
		termimalToken.add("digit");
		termimalToken.add("do");
		termimalToken.add("then");
		termimalToken.add("while");
		//打印出文法
		for(int i = 0;i<grammerList.size();i++) {
			System.out.println(i+" "+grammerList.get(i));
		}
		
		// TODO 给文法的表达式加上点=> S->.A
		for (String s : grammerList) {
			String[] list = s.split("->");
			String newStartGrammer = list[0] + "->" + "." + list[1];
			grammerListFirst.add(newStartGrammer);
		}
		// TODO 给文法的表达式末尾加上点=> S->.A
		for (String s : grammerList) {
			if(!s.equals("A->ε")){ 
				String newStartGrammer = s + ".";
				grammerListLast.add(newStartGrammer);
			}else {
				grammerListLast.add("A->.ε");
			}
		}
		
		
		// TODO 计算first集合
		countFirst();
		System.out.println("验证first集合");
		String content = ""; for(Entry<String, Set<String>> entry :firstList.entrySet()){ 
			content += entry.getKey() + "  :  " + entry.getValue()+ "\n"; 
			System.out.println(entry.getKey() + "  :  " + entry.getValue()); 
		}
		
		
		// TODO 如何求goto/action的符号有哪些
		countGotoActionList();
		 
		ArrayList<String[]> startSet = new ArrayList<>();// 状态1
		ArrayList<String[]> oneOfSet = new ArrayList<>();// 状态1
		String a[] = { grammerListFirst.get(0), "$" };
		startSet.add(a);
		oneOfSet.addAll(closure(startSet));// 取出第一条
		projectSet.add(oneOfSet);// 把状态加入状态大集合

		// 检验闭包求的是否正确 
		for (String[] s : oneOfSet) { 
			System.out.println(s[0] + "," +s[1]); }
		
		
		// TODO 使用goto来遍历后面的状态
		int projectStateNumber = projectSet.size();
		System.out.println("当前状态个数：" + projectStateNumber);
		for (int i = 0; (i < projectStateNumber); i++) {
			System.out.println("遍历状态：" + i);
			System.out.println("--------------输入状态satrt--------");
			for (String[] s : projectSet.get(i)) {
				System.out.println(s[0] + "," + s[1]);
			}
			System.out.println("--------------输入状态end-----------");
			GOTO1(projectSet.get(i), i);// 状态集合，当前状态的标号
			System.out.println("当前状态个数：" + projectSet.size()+" i"+(projectStateNumber - 1));
			if (i == projectStateNumber - 1) {// 检查state数目是否变化,到最后一个了
				projectStateNumber = projectSet.size();
			}
		}
		

		// ------------------打印效果start--------------------------
		System.out.println("-----------总的状态---------：" + projectSet.size());
		for (int k = 0; k < projectSet.size(); k++) {
			System.out.println("............" + k);
			for (String[] s : projectSet.get(k)) {
				System.out.println(s[0] + "," + s[1]);
			}
		}

		System.out.println("-----------goto状态---------：" + gotoList.size());
		for (String[] s : gotoList) {
			System.out.println(s[0] + " " + s[1] + " " + s[2]);
		}
		// ------------------打印效果end--------------------------
		
		
		// 创建LR1分析表
		analyList = reateLRAnalyTable();
		
		// --------------------------查看分析表的效果start -------------------
		//success
		System.out.println("查看分析表的效果");
		System.out.printf("%-7s", "");
		for (String s : token2Number.keySet()) {
			System.out.printf("%-7s", s);
		}
		System.out.print("\n");
		for (int i = 0; i < projectSet.size(); i++) {
			System.out.printf("%-7d", i);
			for (int j = 0; j < token2Number.size(); j++) {
				System.out.printf("%-7s", analyList[i][j]);
			}
			System.out.print("\n");
		}
		// --------------------------查看分析表的效果end -------------------
		
		//TODO 开始建立栈来识别
		ArrayList<String> wordList = new ArrayList<>();
		wordList = readWordList(oneWordName,wordList);
		for(String s :wordList) {
			System.out.print(s+" ");
		}
		System.out.print("\n");
		
		//TODO 对赋值式子的语法制导翻译
		judge(wordList,args[2]);
		
		//TODO 对while式子的语法制导翻译
		
		System.out.println("打印"+args[2]+"的三地址：");
		//TODO 检查三地址
		for(int i = 0;i<threeAddress.size();i++ ) {
			System.out.println(i+" "+threeAddress.get(i));
		}
		
		System.out.println("Token和行数的转换：");
		for(String s :valueTokenMatch.keySet()) {
			System.out.println(s +" "+ valueTokenMatch.get(s));
		}
		
		System.out.println("转换后效果：");
		for(int i = 0;i<threeAddress.size();i++ ) {
			String newThreeAddress ="";
			for(String s :valueTokenMatch.keySet()) {
				if(threeAddress.get(i).contains(s)) {
					newThreeAddress = threeAddress.get(i).replace(s, String.valueOf(valueTokenMatch.get(s)));
					break;
				}else {
					newThreeAddress = threeAddress.get(i);
				}
			}
			exchangethreeAddress.add(newThreeAddress);
		}
		
		for(int i = 0;i<exchangethreeAddress.size();i++ ) {
			System.out.println(i+" "+exchangethreeAddress.get(i));
		}
	}

	// 读入文法
	static void readGrammer(String name) {
		try {
			FileReader fr = new FileReader(name);
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				grammerList.add(str);
			}
			bf.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static ArrayList<String> readWordList(String name,ArrayList<String> list){
		try {
			FileReader fr = new FileReader(name);
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				list.add(str);
			}
			bf.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	// 计算goto/action表的元素
	static void countGotoActionList() {
		String content = "";
		// 先把头符号s'找出来
		startToken = grammerList.get(0).split("->")[0];
		for (Entry<String, Set<String>> entry : firstList.entrySet()) {
			//GOTO不能有开头？？？ && !startToken.equals(entry.getKey()
			if (!gotoToken.contains(entry.getKey())) {
				gotoToken.add(entry.getKey());
			}
			for (String s : entry.getValue()) {
				if (!actionToken.contains(s) && (!s.equals("ε"))) {
					actionToken.add(String.valueOf(s));
				}
			}
			// content += entry.getKey() + " : " + entry.getValue()+ "\n";
			// System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}

	//返回左移n位字符串方法
	static String moveToLeft(String str,int position) {
		String str1=str.substring(position);
		String str2=str.substring(0, position);
		return str1+str2;
	}
	
	//重写GOTO函数
	//输入：（1）一个set，一个状态集合；（2）当前的state的序号
	//目标：记录下跳转的状态，和调用求闭包的函数，得出其他的状态集合
	static void GOTO1(ArrayList<String[]> set, int currentStateNumber) {
		//TODO 重新修改TODO函数
		Map<String, Set<String>> countNewState = new TreeMap<String, Set<String>>();//计算有几个跳转		
		for (int i = 0;i<set.size();i++) {// ["s->a.B","$"]
			String grammer = set.get(i)[0];
			String grammerBack = set.get(i)[1];
			System.out.println("GOTO:" + grammer);
			//leftRightlist[0]:s leftRightlist[1]:a.B
			String leftRightlist[] = grammer.split("->");
			String leftGrammer = leftRightlist[0];//leftRightlist[0]:s
			String rightGrammer = leftRightlist[1];//leftRightlist[1]:a.B
			int index = rightGrammer.indexOf(".");// 找到.的位置 a.B
			//TODO 计算出doc的下一个字符是什么
			//如果是S->A.就不放进去了，因为没有下一个状态了
			if(index!=(rightGrammer.length()-1)) {
			String ifSepecial [] = countSpecialToken(rightGrammer.substring(index+1));//后边的东西是否是关键字
			String docFollow="";
			if(!ifSepecial[0].equals("")) {//终结符号    .int+A
				docFollow = ifSepecial[0];
			}else {
				docFollow = String.valueOf(rightGrammer.charAt(index + 1));
			}
			if(countNewState.containsKey(docFollow)) {//如果已经包含了这个key
				//记录下右边的语法的位置 L  :  [1, 5]
				String recordIndex = String.valueOf(i);
				countNewState.get(docFollow).add(recordIndex);
			}else {
				Set<String> oneRightGrammer = new TreeSet<>();
				String recordIndex = String.valueOf(i);
				oneRightGrammer.add(recordIndex);
				countNewState.put(docFollow, oneRightGrammer );
			}
			}
		}
		System.out.println("可能有的新状态：");
		for(Entry<String, Set<String>> entry :countNewState.entrySet()){ 
			//content += entry.getKey() + "  :  " + entry.getValue()+ "\n"; 
			System.out.println(entry.getKey() + "  :  " + entry.getValue()); 
		}
		//TODO 先使用一个map<String ,set>来刻画要有几个新的state
		for(String oneStateKey : countNewState.keySet()) {//每次的循环都是一个状态
			//System.out.println("oneStateKey:"+oneStateKey);
			Set<String> set1 = countNewState.get(oneStateKey);
			ArrayList<String[]> oneOfSet = new ArrayList<>();// 状态1
			int nextStateNumber = projectSet.size();// 新建状态的序号
			for(String value : set1) {//遍历set里边的元素
				String grammer1 = set.get(Integer.valueOf(value))[0];//这个就是具体的index
				String grammer1Back =  set.get(Integer.valueOf(value))[1];
				System.out.println("GOTOoneset:" + grammer1);
				//leftRightlist[0]:s leftRightlist[1]:a.B
				String leftRightlist[] = grammer1.split("->");
				String leftGrammer = leftRightlist[0];//leftRightlist[0]:s
				String rightGrammer = leftRightlist[1];//leftRightlist[1]:a.B
				int index = rightGrammer.indexOf(".");// 找到.的位置 a.B
				//没有S->A.和S->.的情况出现了
				//只用分成两种情况(1) A.B .B  (2)A.BC .BC  
				String docFollow = rightGrammer.substring(index+1);
				String ifSepecial [] = countSpecialToken(docFollow);//后边的东西是否是关键字
				//System.out.println("判断是否是关键字的串："+removeDoc);
				//System.out.println("结果："+Arrays.toString(ifSepecial));
				//TODO 求出doc后面的字符
				String docFollowToken = "";
				int docFollowTokenLength = 0;
				String secondFollowString="";
				if(!ifSepecial[0].equals("")) {//终结符号    .int+A
					docFollowToken = ifSepecial[0];
					docFollowTokenLength =  docFollowToken.length();
					secondFollowString = ifSepecial[1];
				}else {
					docFollowToken =  String.valueOf(rightGrammer.charAt(index+1));
					docFollowTokenLength =  1;
					if(index+2<=rightGrammer.length()-1) {//.BC  .BCD
						secondFollowString = rightGrammer.substring(index+2);;
					}
					//否则的话就是空串
				}
				
				String newGrammer="";
				if(index==0) {//.A .AB的形式
					newGrammer = leftGrammer+"->"+docFollowToken+"."+secondFollowString;
				}else {//A.B  A.BC
					newGrammer = leftGrammer+"->"+rightGrammer.substring(0, index)+docFollowToken+"."+secondFollowString;
				}
				System.out.println("新组号的串"+newGrammer);
				
				//TODO 判断这个语法是否已经有了，有了的话就不需要继续去生成了
				int flagIfadd=1;
				for (int kk = 0; kk < projectSet.size(); kk++) {
					// System.out.println("............");
					String[] lll = projectSet.get(kk).get(0);
					// System.out.println("重要："+lll[0]+" "+lll[1]);
					if (lll[0].equals(newGrammer) && lll[1].equals(grammer1Back)) {
						System.out.println("已经存在了state");
						flagIfadd = 0;// 不再需要重新创建
						nextStateNumber = kk;// 调回以前的状态state
					}
				}
					
				//TODO 判断是放这个语法而已，还是放进这个语法的闭包
				if(flagIfadd!=0) {
				ArrayList<String[]> startSet = new ArrayList<>();// 状态1
				String a[] = { newGrammer, grammer1Back };
				startSet.add(a);// 构建出初始状态
				oneOfSet.addAll(closure(startSet));// 把闭包的所有状态都加入oneset集合
				}
			}
			//检查构建的集合是否正确
			System.out.println("-------------start构建的新state"+projectSet.size());
			for(String[] oneset: oneOfSet) {
				System.out.println("构建的新state:"+oneset[0]+" "+oneset[1]);
			}
			System.out.println("-------------end构建的新state"+projectSet.size());
			//就算没有新状态的产生，也会有状态goto的添加
			String[] gotoStateList = new String[3];
			gotoStateList[0] = String.valueOf(currentStateNumber);
			gotoStateList[1] = oneStateKey ;
			gotoStateList[2] = String.valueOf(nextStateNumber);
			if(!actionToken.contains(oneStateKey) && !(gotoToken.contains(oneStateKey))) {//把他添加进action表项
				actionToken.add(oneStateKey);
			}
			gotoList.add(gotoStateList);
			if(oneOfSet.size()!=0) {
			projectSet.add(oneOfSet);// 把状态加入状态大集合
			}
			}
			
		//TODO 对每个语句进行处理
	}
	
	// 计算闭包
	// 传入：集合I
	// 输出 集合的闭包
	static ArrayList<String[]> closure(ArrayList<String[]> set) {
		int length = set.size();
		ArrayList<String[]> newElement = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			System.out.println("求闭包的串"+set.get(i)[0]+" "+set.get(i)[1]);
			String grammer = set.get(i)[0];
			String lastToken = set.get(i)[1];// "$"
			String docFollow  = "";
			// TODO 查看.后边的下一个符号是谁
			String[] grammerList = grammer.split("->");
			int index = grammerList[1].indexOf(".");// 找到.的位置
			// s->.a s->A.B s->AB. s->a. a->.ε
			// (index!=grammerList[1].length()-1) //.不在字符串末尾
			if ((index != grammerList[1].length() - 1)) {// 不是这种：S->A.
				// .后边不是空串
				//TODO !!!!!!!修改 docFollow要判断是不是关键字 (TODO )
				String docFollowString = grammerList[1].split("\\.")[1];
				String ifSepecial [] = countSpecialToken(docFollowString);//后边的东西是否是关键字
				if(ifSepecial[0].equals("")) {
					docFollow = String.valueOf(grammerList[1].charAt(index + 1));// .后边的字符
				}else {
					docFollow  = ifSepecial[0];
				}
				System.out.println(".........................后边的字"+docFollow);
				if (!docFollow.equals("ε") && !actionToken.contains(docFollow)) {//后边不是终结符,不是空串
					// System.out.println(docFollow);
					// TODO 计算lastToken
					if (index + 2 <= grammerList[1].length() - 1) {// 如果S后边还有字符，e.g.:s->A.BC
						// System.out.println("需要遍历后继的："+grammerList[1]);//S->L=R
						//判断followfollow是不是关键字，把docFollow替换掉，剩下的串判断顶头的是不是关键字
						String docFollowFollowString = docFollowString.substring(docFollow.length());//=R
						String ifSepecial1 [] = countSpecialToken(docFollowFollowString);//后边的东西是否是关键字
						//TODO !!!!!!前边添加终结符号，一定要添加完全，不然这里会少
						//因为默认如何判断回来不是终结符，就会认为是B大写字母
						if(ifSepecial1[0].equals("")) {//后边不是特殊终结符
							//还有两种可能(1)是大写字母B 
							System.out.println("第二个字符followfollow:"+docFollowFollowString);
							String docFollowFollow = String.valueOf(docFollowFollowString.charAt(0));//+或者是B
							if(gotoToken.contains(docFollowFollow)) {//是大写字母
								//TODO 原来写错如s->A.BC，C包含空串，才是相加，不包含空串，则直接覆盖
								if(firstList.get(docFollowFollow).contains("ε")) {
									lastToken = lastToken + getLRFirst(docFollowFollow,lastToken);
								}else {
									//如果不包含ε
									for(String k : firstList.get(docFollowFollow)) {
										lastToken = lastToken+"|"+k;
									}
								}
								
							}else {
								if(!lastToken.contains(docFollowFollow)) {//+ 单个字节的终结符号
									//TODO 原来写错了
									//lastToken = lastToken + "|"+docFollowFollow;
									lastToken = docFollowFollow;
								}
							}
						}else {
							//TODO 判断如果原来都没有，现在才加进去
							if(!lastToken.contains(ifSepecial1[0])) {
								lastToken = lastToken + "|"+ifSepecial1[0];
							}
						}

					}
					
					// 找出所有的后继加进another列表
					String str = docFollow + "->";// S->
					System.out.println("求闭包，开头串："+str);
					for (String s : grammerListFirst) {// 查找文法中包含S开头的式子
						if ((s.indexOf(str) != -1)) {//前缀跟文法表达式相同
							//并且s没有出现在set里边过
							int ifSetSelf = 1;
							//如果这个类型是E->.E+T类型
							
							//--------------------新加上的-------------------
							String selfType[] = s.split("->");
							String selfDocFollow = String.valueOf(selfType[1].charAt(1));
							if(selfType[0].equals(selfDocFollow)) {//E.equal(E)
								//lastToken要多加上一些东西
								String ifSepecial2 [] = countSpecialToken(selfType[1].substring(2));//+T
								if((!ifSepecial2[0].equals("")) && (!lastToken.contains(ifSepecial2[0]))) {//是多个字符组成的终结符号
										lastToken = lastToken + "|"+ifSepecial2[0];
								}else {
									String doubleFollow = String.valueOf(selfType[1].charAt(2));
									if((!gotoToken.contains(doubleFollow)) &&(!lastToken.contains(doubleFollow))) {//是大写字母
										lastToken = lastToken + "|"+ doubleFollow;
									}
								}
							}
							//--------------------新加上的-------------------
							
							for(String []oneG:set) {
								if((oneG[0].contains(s))) {//新找到的表达式，set里边已经有了
									//找到了E->E+T,lastToken多了一个+号
									if(!oneG[1].contains(lastToken)) {
										oneG[1] = oneG[1]+"|"+lastToken;
									}
									ifSetSelf = 0 ;
								}
							}
							if(ifSetSelf==1) {
								String a[] = { s, lastToken };
								newElement.add(a);
							}
						}
					}
				}
			}
			if (i == length - 1) {// 遍历到最后了,查看是否再遍历
				set.addAll(newElement);
				length = length + newElement.size();
				newElement.clear();// 清空下次使用
			}
		}
		return set;
	}
	
	//输入：doc后边的字符串，检查是否有特殊字符，如何移动
	//输出:(1)如果word[0]==""，说明没有匹配，老老实实移动一个字节(2)!="，说明有关键字，要变成 word[0]+"."+word[1]
	static String[] countSpecialToken(String sentence){
		//int+id  TF
		String[] word = {"",""};
		for(String s :termimalToken) {
			String REGEX = s;
			Pattern pattern = Pattern.compile(REGEX); //创建一个以字符串为标准的模式
			Matcher matcher = pattern.matcher(sentence);//判断是否match
			if(matcher.lookingAt()) {//有匹配的东西
				word[0] = s;
				word[1] = sentence.replaceFirst(s, "");
				break;
			}
			//System.out.println("lookingAt(): "+matcher.lookingAt());
		}
		//System.out.println(Arrays.toString(word));
		return word;
	}
	
	// 计算文法的first集合
	static void countFirst() {
		// TODO 先声称目标的输入：S->a|aB
		Map<String, ArrayList<String>> stardartRight = new TreeMap<String, ArrayList<String>>();
		for (String s : grammerList) {
			// System.out.println(s);
			String list[] = s.split("->");
			if (stardartRight.containsKey(list[0])) {// 已经存在左边的式子
				// System.out.println("已经有重复的了，需要添加："+list[1]);
				stardartRight.get(list[0]).add(list[1]);
			} else {// 还没有存在
				ArrayList<String> a = new ArrayList<String>();
				a.add(list[1]);
				stardartRight.put(list[0], a);
				// a.clear();
			}
		}

		// TODO 转换成标准的合并语法
		for (String m : stardartRight.keySet()) {
			String grammerLeft = m;
			ArrayList<String> grammerRight = stardartRight.get(m);
			String[] rightList = (String[]) grammerRight.toArray(new String[grammerRight.size()]);
			// System.out.println(m+" "+Arrays.toString(rightList));//构建完成
			stardardConbineGrammer.put(grammerLeft, rightList);
		}
		for (String k : stardardConbineGrammer.keySet()) {
			System.out.println("查找first集合："+k+" "+Arrays.toString(stardardConbineGrammer.get(k)));
			findEveryFirst(k, stardardConbineGrammer.get(k));// 对每一个非终结符调用查找first的函数
		}
	}

	static Set<String> findEveryFirst(String curNode, String[] rightNodes) {
		String nextNode = "";
		if (firstList.containsKey(curNode))
			return firstList.get(curNode);
		Set<String> st = new TreeSet<String>();
		for (int i = 0; i < rightNodes.length; ++i) {//有几个 aB|a, 2个
			System.out.println("当前i:"+rightNodes[i]);
			for (int j = 0; j < rightNodes[i].length(); ++j) {//
				String[] ifSprcial = countSpecialToken(rightNodes[i]);
				if(ifSprcial[0].equals("")) {//没有特殊符号
					nextNode = "" + rightNodes[i].charAt(j);
				}else {
					nextNode = ifSprcial[0];
				}
				System.out.println("nextNode"+j+" "+nextNode);
				
				if (!stardardConbineGrammer.containsKey(nextNode)) {// 终结点
					st.add(nextNode);//直接加进去
					break;
				} else {// 非终结点
					/*if (j + 1 < rightNodes[i].length() && rightNodes[i].charAt(j + 1) == '\'') {
						nextNode += rightNodes[i].charAt(j + 1);
						++j;
					}*/
					if ((!curNode.equals(nextNode)) ) {//S->BA 找B的first集合
						Set<String> tmpSt = findEveryFirst(nextNode, stardardConbineGrammer.get(nextNode));//遍历
						st.addAll(tmpSt);
						if (!tmpSt.contains("$"))
							break;
					}else {//E->E+F;
						break;
					}
				}
			}
		}
		firstList.put(curNode, st);
		return st;
	}

	// 求first集合
	static String getLRFirst(String s,String lastToken) {
		/**
		 * 如果lastToken里边有了就不要了
		 * @return : |a|b
		 */
		
		String firstString = "";
		//System.out.println("查找"+s+"的first集合");
		if(firstList.get(s) != null) {
			for (String ss : firstList.get(s)) {
				if (!(ss.equals("ε")) &&  (!lastToken.contains(String.valueOf(ss)))) {// 不包含空
					// System.out.println("jin "+ss);
					firstString = firstString + "|" + ss;
				}
			}
		}
		// System.out.println("找："+firstString);
		return firstString;
	}

	static String[][] reateLRAnalyTable() {
		// TODO 把goto和action表结合起来
		System.out.println("构建DFA状态机");
		int count = 0;
		for (String s : actionToken) {
			System.out.println("action添加"+s+" "+count);
			token2Number.put(s, count);
			count++;
		}
		
		/*
		for (String[] s : gotoList) {//不出从没添加的
			//System.out.println(s[0] + " " + s[1] + " " + s[2]);
			if(!token2Number.keySet().contains(s[1]) && !(gotoToken.contains(s[1])) && !(startToken.equals(s[1]))) {//还没包括的，添加进去
				token2Number.put(s[1], count);
				count++;
			}
		}
		*/
		if(!token2Number.keySet().contains("$")) {
			System.out.println("$添加"+"$"+" "+count);
			token2Number.put("$", count);
			count++;
		}
		for (String s : gotoToken) {
			System.out.println("goto添加"+s+" "+count);
			token2Number.put(s, count);
			count++;
		}
		
		
		for (String s : token2Number.keySet()) {
			System.out.println(s + " " + token2Number.get(s));
		}

		// 创建String[state个数][action+goto的个数]数组:
		String[][] analyList = new String[projectSet.size()][count];
		for (int i = 0; i < projectSet.size(); i++) {
			for (int j = 0; j < token2Number.size(); j++) {
				analyList[i][j] = "";
			}
		}
		for (int i = 0; i < projectSet.size(); i++) {// 从每一行开始填表
			// 三元数组{"本状态 活前缀 跳转状态"}
			for (String s[] : gotoList) {// 遍历跳转列表
				if (s[0].equals(String.valueOf(i))) {// 是改栏的状态
					if (actionToken.contains(s[1])) {// 是动作表，需要判断是规约还是移进
						// 先全填上移入
						analyList[i][token2Number.get(s[1])] = "S" + s[2];
					} else {// 是goto表的项，只需要填数字
						//System.out.println("error:"+s[2]);
						//System.out.println("index:"+s[1]);
						analyList[i][token2Number.get(s[1])] = s[2];
					}
				}
			}
		}

		System.out.println("-----------规约总结---------：" + projectSet.size());
		for (int k = 0; k < projectSet.size(); k++) {
			System.out.println("............" + k);
			for (String[] s : projectSet.get(k)) {
				if (grammerListLast.contains(s[0])) {// 含有终结状态
					if(!(s[0].split("->")[0].equals(startToken))) {//不是终结状态
						System.out.println(k + "含有终结状态:" + s[0]+","+s[1]);
						String[] rToken = s[1].split("\\|");
						System.out.println("分解效果："+Arrays.toString(rToken));
						for (String token : rToken) {
							System.out.println(k+" "+token+" "+token2Number.get(token)+" 规约式子："+ grammerListLast.indexOf(s[0]));
							analyList[k][token2Number.get(token)] = "R" + grammerListLast.indexOf(s[0]);
						}
					}else {//含有终结符的式子
						analyList[k][token2Number.get("$")] = "acc";
					}

				}
			}
		}
		return analyList;
	}
	
	static String getNewTemp(){
		newTempCount++;
		return "t"+String.valueOf(newTempCount);
	}
	
	static String getNewLemp(){
		newLempCount++;
		return "L"+String.valueOf(newLempCount);
	}
	
	static void gen(String s) {
		threeAddress.add(s);
		//System.out.println("---------三地址---------"+s);
	}
	
	//对规约的各种情况进行分析
	static void SDT_assignment(int index){
		if(index==0) {//S->id=E;
			String s1 = valueStack.pop();//第一次pop
			String s2 = valueStack.pop();
			gen(s2+"="+s1);
		}else if(index==1) {//E->E+E
			String s = getNewTemp();
			//把两个pop出来
			String s1 = valueStack.pop();
			String s2 = valueStack.pop();
			gen(s+"="+s1+"+"+s2);
			valueStack.push(s);
		}else if(index==2) {//E->E*E
			String s = getNewTemp();
			//把两个pop出来
			String s1 = valueStack.pop();
			String s2 = valueStack.pop();
			gen(s+"="+s1+"*"+s2);
			valueStack.push(s);
		}else if(index==3) {//E->(E)
			//不用理睬
		}else if(index==4) {//E->id
			//不用理睬
		}
	}
	
	//while的语法制导翻译
	static void SDT_while(int index){
		String B1true="";//while的if判断
		String B1false="";
		String B2true="";//if判断(嵌套在while里边的if)
		String B2false="";
		if(index==0) {//P->S

		}else if(index==1) {//S->ifBthenQ 

		}else if(index==2) {//S->ifBthenQelseP
			String s1 = valueStack.pop();
			valueTokenMatch.put(s1,threeAddress.size());//需要到后边才知道
		}else if(index==3) {//S->whileBdoS
			
		}else if(index==4 || index==5) {//B->E<E  B->E>E
			B1true = "t"+getNewLemp();
			B1false = "f"+getNewLemp();
			//把两个pop出来
			String s1 = valueStack.pop();
			String s2 = valueStack.pop();
			if(index==4) {
				gen("if "+s2+" < "+s1+" goto "+B1true);
			}else {
				gen("if "+s2+" > "+s1+" goto "+B1true);
			}
			gen("goto "+B1false);
			valueTokenMatch.put(B1false,0);//需要到后边才知道
			valueTokenMatch.put(B1true,threeAddress.size());
			valueStack.push(B1true);
			valueStack.push(B1false);
		}else if(index==6 || index==7) {//E->E+E E->E-E
			String s = getNewTemp();
			//把两个pop出来
			String s1 = valueStack.pop();
			String s2 = valueStack.pop();
			if(index==6) {
				gen(s+"="+s1+"+"+s2);
			}else {
				gen(s+"="+s1+"-"+s2);
			}
			valueStack.push(s);
		}else if(index==8) {//E->id
			
		}else if(index==9 || index==10) {//Q->id=E; P->id=E;
			String s1 = valueStack.pop();//第一次pop
			String s2 = valueStack.pop();
			gen(s2+"="+s1);
			gen("goto 0");//回到开头S0
			String s3 = valueStack.pop();
			if(s3.contains("f")) {//需要改变
				valueTokenMatch.put(s3, threeAddress.size());
			}
		}
	}
	static void judge(ArrayList<String> wordList,String SDTname){
		stateStack.push(new String("0"));//状态栈的开始是0
		tokenStack.push(new String("$"));
		String action = "";//动作
		String token = "";//当前字符
		String currentState = "";//当前状态
		String currentToken = "";//当前符号栈栈顶状态
		String stackPeek = "";
		int count=0;
		stackPeek = stateStack.peek();
		int hang = Integer.valueOf(stackPeek);//行坐标
		token = wordList.get(count);
		int lie = token2Number.get(token);//列坐标
		int iter = 1;
		System.out.printf("%-9s","");
		System.out.printf("%-65s","状态栈");
		System.out.printf("%-60s","符号栈");
		System.out.printf("%-10s","当前符号");
		System.out.printf("\n");
		
		Common c = new Common();
		ArrayList<Map<String, String>> valuemap = c.getValueMap();
		int valueMapCOunt=0;//从0开始遍历value
		boolean ifSuccess = false;
		while(ifSuccess!=true) {
			
			System.out.printf("%-5s",iter);
			System.out.printf("%-40s",stateStack);
			System.out.printf("%-40s",tokenStack);
			System.out.printf("%-10s",token);
			System.out.printf("\n");
			
			//System.out.printf("%-40s\n",valueStack);
			//TODO 如果当前放进来的值是id的话，把它对应的vlaue也放进valueStack
			if(token.equals("id")) {
				String value = valuemap.get(valueMapCOunt).get("id");
				valueMapCOunt ++;
				valueStack.push(value);
			}
			action = analyList[hang][lie];
			//System.out.println("当前操作"+action);
			
			if(action.equals("acc")) {
				System.out.println("成功");
				ifSuccess = true;
				action = "R0";
			}
			if(action.contains("S")) {//S1
				//压入状态站和符号
				//System.out.println("移入");
				tokenStack.push(new String(token));
				currentState = action.replace("S","");
				stateStack.push(new String(currentState));
				if(count<wordList.size()) {
					count++;
				}
			}else {//R1
				//System.out.println("规约");
				String index = action.replace("R","");
				String rString = grammerList.get(Integer.valueOf(index));//用哪个式子来规约
				//TODO !!!!!答案：要规约的式子
				System.out.println("规约"+rString);
				//TODO 语法制导方案,采用何种方案
				if(SDTname.equals("assignment")) {
					SDT_assignment(Integer.valueOf(index));
				}else if(SDTname.equals("while")){
					SDT_while(Integer.valueOf(index));
				}
				String right = rString.split("->")[1];//int +BT
				String left = rString.split("->")[0];
				
				//TODO 把右边的东西放进一个list
				ArrayList<String> rWord = new ArrayList<>();
				int specialIndex = 0;
				String specilaRight =right;
				
				while(specialIndex<right.length()) {
					//System.out.println("总长度："+right.length()+" 现长度："+specialIndex);
					String ifSepecial [] = countSpecialToken(specilaRight);
					if(!ifSepecial[0].equals("")) {//是关键字
						rWord.add(ifSepecial[0]);
						specilaRight  = ifSepecial[1];//剩下的串
						//System.out.println("找到的词"+ifSepecial[0]+" 剩下的串："+specilaRight);
						specialIndex = specialIndex+ifSepecial[0].length();
					}else {
						rWord.add(String.valueOf(right.charAt(specialIndex)));
						specilaRight = specilaRight.substring(1);
						specialIndex++;
					}
				}
				for(String s : rWord) {
					//System.out.print("规约单词"+s+" ");
				}
				//System.out.print("\n");
				//把符号栈里边相同的弹出去
				for(int y =rWord.size()-1;y>=0;y-- ) {
					if(tokenStack.peek().equals(rWord.get(y))) {//符号栈跟规约右边
						tokenStack.pop();
						stateStack.pop();
					}
				}
				//System.out.println("规约弹出后的状态："+stateStack+" "+tokenStack);
				//弹出完毕，把右边放进来
				tokenStack.push(new String(left));
				//System.out.println("规约弹放入后的状态："+stateStack+" "+tokenStack);
				currentState = stateStack.peek();
				currentToken = tokenStack.peek();
				//System.out.println("当前状态："+currentState+"当前符号栈字符："+currentToken);
				hang = Integer.valueOf(currentState);
				lie = Integer.valueOf(token2Number.get(currentToken));
				action = analyList[hang][lie];
				stateStack.push(new String(action));
				//System.out.println("规约完毕后的状态："+stateStack+" "+tokenStack);
			}
			if(ifSuccess!=true) {
			token = wordList.get(count);
			//System.out.println("当前字符："+token);
			stackPeek = stateStack.peek();
			hang = Integer.valueOf(stackPeek);
			//System.out.println("下一个符号："+token);
			lie = token2Number.get(token);
			iter++;
			}
		}
	}
	
}
