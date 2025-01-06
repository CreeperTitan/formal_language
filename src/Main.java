import java.util.*;

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
class InToPost {
    public static void insert(StringBuilder s, int n,char ch) {
        s.insert(n+1,ch);
    }
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


public class Main {

    public static void main(String[] args) {
        //输入处理
        Scanner scanner=new Scanner(System.in);

        //正则表达式--后缀表达式
        String postfix= InToPost.infixToPostfix(scanner.nextLine());
        System.out.println(postfix);

    }
}
