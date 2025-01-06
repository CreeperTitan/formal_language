import java.util.*;

//字符串处理
class InToPost {
    public static void insert(StringBuilder s, int n,char ch) {
        s.insert(n+1,ch);
    }
    //预处理
    public static String preprocess(String s) {
        StringBuilder s1 = new StringBuilder(s);
        int i=0,length=s1.length();
        while(i<length){
            if(s1.charAt(i)=='1'||s1.charAt(i)=='0'||s1.charAt(i)=='*'||s1.charAt(i)==')'){
                if(s1.charAt(i+1)=='1'||s1.charAt(i+1)=='0'||s1.charAt(i+1)=='('){
                    insert(s1,i,'&');
                }
            }
            i++;
        }
        return s1.toString();
    }
    //标记优先级
    public static int priority(char ch) {
        return switch (ch) {
            case '*' -> 3;
            case '&' -> 2;
            case '+' -> 1;
            case '(' -> 0;
            default -> -1;
        };
    }
    // 中缀表达式转后缀表达式
    public static String infixToPostfix(String s) {
        // 用 StringBuilder 来构建输出的后缀表达式
        StringBuilder postfix = new StringBuilder();
        s=preprocess(s);
        Stack<Character> operator = new Stack<>();  // 运算符栈
        // 遍历输入字符串
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // 如果是操作数（0 或 1），直接添加到后缀表达式中
            if (c == '0' || c == '1') {
                postfix.append(c);
            } else {
                // 如果是左括号，压栈
                if (c == '(') {
                    operator.push(c);
                }
                // 如果是右括号，弹栈直到遇到左括号
                else if (c == ')') {
                    while (!operator.isEmpty() && operator.peek() != '(') {
                        postfix.append(operator.pop());
                    }
                    operator.pop();  // 弹出 '('
                }
                // 如果是运算符
                else {
                    while (!operator.isEmpty() && priority(operator.peek()) >= priority(c)) {
                        postfix.append(operator.pop());  // 弹出栈中优先级大于等于当前运算符的运算符
                    }
                    operator.push(c);  // 当前运算符入栈
                }
            }
        }
        // 弹出栈中剩余的运算符
        while (!operator.isEmpty()) {
            postfix.append(operator.pop());
        }
        return postfix.toString();  // 返回构建好的后缀表达式
    }
}

//定义NFA状态
class NfaState {
    int index;//NFA状态号
    char input;//状态弧上的值
    int chTrans;//状态弧转移到的状态号
    Set<Integer> epTrans;//ε转移到的状态集合

    public NfaState(int index) {
        this.index = index;
        this.input = '#';
        this.chTrans = -1;
        this.epTrans = new HashSet<>();
    }
}
// 定义NFA类
class NFA {
    NfaState head;
    NfaState tail;

    // 构造方法
    public NFA(int sum) {
        this.head = new NfaState(sum);
        this.tail = new NfaState(sum + 1);
    }

    // 添加带字符的转换
    public void add(NfaState n1, NfaState n2, char ch) {
        n1.input = ch;
        n1.chTrans = n2.index;
    }

    // 添加ε转换
    public void add(NfaState n1, NfaState n2) {
        n1.epTrans.add(n2.index);
    }
}

class StrToNFA{
    private static int nfaStateNum=0;
    //后缀表达式转NFA
    public static NFA strToNFA(String s) {
        Stack<NFA> nfaStack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '1' || ch == '0') {  // 操作数：字符
                NFA n = new NFA(nfaStateNum);
                nfaStateNum += 2;  // NFA状态总数增加2

                n.add(n.head, n.tail, ch);  // 创建一个字符的NFA
                nfaStack.push(n);  // 将该NFA推入栈
            }
            else if (ch == '*') {  // 闭包运算符
                NFA n1 = nfaStack.pop();  // 从栈中弹出一个NFA

                NFA n = new NFA(nfaStateNum);
                nfaStateNum += 2;  // NFA状态总数增加2

                // 闭包处理：n1的尾通过ε转移指向n1的头、n1的尾通过ε转移指向n的尾
                n.add(n1.tail, n.head);
                n.add(n1.tail, n.tail);
                n.add(n.head, n1.head);
                n.add(n.head, n.tail);

                nfaStack.push(n);  // 将处理后的NFA推入栈
            }
            else if (ch == '+') {  // 或运算符
                NFA n2 = nfaStack.pop();  // 从栈中弹出两个NFA
                NFA n1 = nfaStack.pop();

                NFA n = new NFA(nfaStateNum);
                nfaStateNum += 2;

                // 或运算符处理：新建头部和尾部，通过ε转移连接两个NFA
                n.add(n.head, n1.head);
                n.add(n.head, n2.head);
                n.add(n1.tail, n.tail);
                n.add(n2.tail, n.tail);

                nfaStack.push(n);  // 将合并后的NFA推入栈
            }
            else if (ch == '&') {  // 连接运算符
                NFA n2 = nfaStack.pop();  // 从栈中弹出两个NFA
                NFA n1 = nfaStack.pop();

                NFA n = new NFA(nfaStateNum);
                nfaStateNum += 2;

                // 连接操作：n1的尾通过ε转移指向n2的头
                n.add(n1.tail, n2.head);

                n.head = n1.head;  // 新NFA的头为n1的头
                n.tail = n2.tail;  // 新NFA的尾为n2的尾

                nfaStack.push(n);  // 将合并后的NFA推入栈
            }
        }

        return nfaStack.peek();  // 最后栈顶的NFA即为结果
    }
}

/********************NFA转DFA********************/
// 定义 DFA 转换弧
class Edge {
    char input; // 弧上的值
    int trans;  // 弧所指向的状态号

    public Edge(char input, int trans) {
        this.input = input;
        this.trans = trans;
    }
}
// 定义 DFA 状态
class DFAState {
    boolean endBeing; // 是否为终态，是为 true，不是为 false
    int index;        // DFA 状态的状态号
    Set<Integer> closure; // NFA 的 ε-move() 闭包
    int edgeNumber;   // DFA 状态上的射出弧数
    List<Edge> edges; // DFA 状态上的射出弧

    public DFAState(int index) {
        this.index = index;
        this.endBeing = false; // 默认状态为非终态
    }
}
// 定义 DFA 结构
class DFA {
    int startState;  // DFA 的初态
    Set<Integer> endStates;  // DFA 的终态集
    Set<Character> terminator; // DFA 的终结符集（仅包括 '0' 和 '1'）
    int[][] trans;  // DFA 的转移矩阵

}
class NFAToDFA {

    // NFA 状态集合
    static List<NfaState> nfaStates = new ArrayList<>();
    static DFAState[] dfaStates = new DFAState[100];
    static int dfaStateNumber = 0;


    /**
     * 计算一个状态集的 ε-closure
     *
     * @param states 状态集
     * @return ε-closure 的结果集
     */
    public static Set<Integer> epsilonClosure(Set<Integer> states) {
        Stack<Integer> stack = new Stack<>();
        Set<Integer> result = new HashSet<>(states);

        // 将状态集中的每个元素压入栈中
        for (int state : states) {
            stack.push(state);
        }

        // 处理栈中的元素
        while (!stack.isEmpty()) {
            int currentState = stack.pop();

            // 遍历当前状态的 ε 转移集合
            for (int epState : nfaStates.get(currentState).epTrans) {
                if (!result.contains(epState)) {
                    result.add(epState); // 将新状态加入结果集
                    stack.push(epState); // 并压入栈中继续处理
                }
            }
        }

        return result; // 返回最终的 ε-closure 集合
    }

    /**
     * 计算一个状态集的 move(char) 的 ε-closure
     *
     * @param states 状态集
     * @param ch     转移字符
     * @return move(char) 的 ε-closure
     */
    public static Set<Integer> moveEpsilonClosure(Set<Integer> states, char ch) {
        Set<Integer> temp = new HashSet<>();

        // 遍历当前状态集中的每个状态
        for (int state : states) {
            NfaState nfaState = nfaStates.get(state);

            // 如果状态的输入符号匹配
            if (nfaState.input == ch) {
                temp.add(nfaState.chTrans); // 将转换后的状态加入 temp
            }
        }

        // 计算 temp 的 ε-closure
        return epsilonClosure(temp);
    }

    /**
     * 判断一个状态集是否为终态
     *
     * @param nfa    给定的 NFA
     * @param states 状态集
     * @return 是否为终态
     */
    public static boolean isEndState(NFA nfa, Set<Integer> states) {
        for (int state : states) {
            if (state == nfa.tail.index) { // 如果包含 NFA 的终态
                return true; // 是终态
            }
        }
        return false; // 否则不是终态
    }
    // 将 NFA 转换为 DFA
    public static DFA nfaToDfa(NFA n, String str) {
        DFA d = new DFA();
        Set<Set<Integer>> states = new HashSet<>(); // 存储已处理的状态集

        // 初始化 DFA 转移矩阵为 -1（表示没有转换）
        for (int i = 0; i < 100; i++) {
            Arrays.fill(d.trans[i], -1);
        }

        // 处理终结符集
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '0' || str.charAt(i) == '1') {
                d.terminator.add(str.charAt(i));
            }
        }

        // 初始化 DFA 起始状态
        d.startState = 0;
        Set<Integer> tempSet = new HashSet<>();
        tempSet.add(n.head.index);

        // 求 DFA 初态的 ε-closure
        dfaStates[0] = new DFAState(0);
        dfaStates[0].closure = epsilonClosure(tempSet);
        dfaStates[0].endBeing = isEndState(n, dfaStates[0].closure);

        dfaStateNumber++; // 增加 DFA 状态总数

        Queue<Integer> queue = new LinkedList<>();
        queue.add(d.startState); // 将起始状态加入队列

        while (!queue.isEmpty()) {
            int num = queue.poll();

            for (char terminator : d.terminator) {
                // 计算每个终结符的 ε-closure(move(ch))
                Set<Integer> temp = moveEpsilonClosure(dfaStates[num].closure, terminator);

                if (!states.contains(temp) && !temp.isEmpty()) {
                    states.add(temp);

                    // 新建一个 DFA 状态
                    dfaStates[dfaStateNumber] = new DFAState(dfaStateNumber);
                    dfaStates[dfaStateNumber].closure = temp;

                    // 更新 DFA 状态的转移
                    dfaStates[num].edges.set(dfaStates[num].edgeNumber, new Edge(terminator, dfaStateNumber));
                    dfaStates[num].edgeNumber++;

                    // 更新转移矩阵
                    d.trans[num][terminator - 'a'] = dfaStateNumber;

                    // 判断新状态是否为终态
                    dfaStates[dfaStateNumber].endBeing = isEndState(n, dfaStates[dfaStateNumber].closure);

                    queue.add(dfaStateNumber); // 将新状态加入队列
                    dfaStateNumber++;
                } else {
                    // 状态集相同，更新转移信息
                    for (int i = 0; i < dfaStateNumber; i++) {
                        if (temp.equals(dfaStates[i].closure)) {
                            dfaStates[num].edges.set(dfaStates[num].edgeNumber, new Edge(terminator, i));
                            dfaStates[num].edgeNumber++;

                            d.trans[num][terminator - 'a'] = i;
                            break;
                        }
                    }
                }
            }
        }

        // 计算 DFA 的终态集
        for (int i = 0; i < dfaStateNumber; i++) {
            if (dfaStates[i].endBeing) {
                d.endStates.add(i);
            }
        }

        return d;
    }
}
public class Main {
    public static void main(String[] args) {
        //输入处理
        Scanner scanner=new Scanner(System.in);

        //正则表达式--后缀表达式
        String postfix= InToPost.infixToPostfix(scanner.nextLine());
        System.out.println(postfix);
        //后缀表达式--NFA
        NFA nfa = StrToNFA.strToNFA(postfix);
    }
}
