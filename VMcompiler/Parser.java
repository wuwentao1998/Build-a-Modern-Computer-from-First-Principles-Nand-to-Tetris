import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Parser {
    private Map<Integer, String> codes = new LinkedHashMap<>();
    private int totalNum = -1;
    private int currentNum = -1;
    private String[] currentLine = null;
    private commandtype currentTpye;
    private String functionName;
    private CodeWriter code;
    private enum commandtype {
        C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF,
        C_FUNCTION, C_RETURN, C_CALL
    }
    public static final String[] ARITHMETIC = { "add", "sub", "neg", "eq",
            "gt", "lt", "and", "or", "not" };

    public void setCode(CodeWriter code) {
        this.code = code;
    }

    public Parser(File file) {
        FileReader fr = null;

        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);

        String currentLine = null;
        int lineNumber = -1;
        try {
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("//") || currentLine.trim().length() < 1) {
                    continue;
                }
                if (currentLine.indexOf("//") > 0) {
                    currentLine = currentLine.substring(0,currentLine.indexOf("//"));
                }
                currentLine = currentLine.trim();
                codes.put(++lineNumber,currentLine);
            }
            totalNum = codes.size();

            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean hasMoreCommands() {
        return currentNum < totalNum;
    }

    private void advance() {
        if (hasMoreCommands()) {
            currentNum++;
            currentLine = codes.get(currentNum).split(" ");//按空格分割这一行命令
            currentTpye = commandType();
            if (currentTpye.compareTo(commandtype.C_FUNCTION) == 0) {
                functionName = arg1();
            }
        }
    }

    private commandtype commandType() {
        if (Arrays.asList(ARITHMETIC).contains(currentLine[0])) { //将数组变为list然后调用内置方法判断
            return commandtype.C_ARITHMETIC;
        } else if (currentLine[0].equals("push")) {
            return commandtype.C_PUSH;
        } else if (currentLine[0].equals("pop")) {
            return commandtype.C_POP;
        }else if (currentLine[0].equals("label")) {
            return commandtype.C_LABEL;
        }else if (currentLine[0].equals("goto")) {
            return commandtype.C_GOTO;
        }else if (currentLine[0].equals("if-goto")) {
            return commandtype.C_IF;
        }else if (currentLine[0].equals("function")) {
            return commandtype.C_FUNCTION;
        }else if (currentLine[0].equals("return")) {
            return commandtype.C_RETURN;
        } else if (currentLine[0].equals("call")) {
            return commandtype.C_CALL;
        } else {
            return null;
        }

    }

    public String arg1() {
        if (commandType().equals(commandtype.C_ARITHMETIC)) {
            return currentLine[0];
        } else if (commandType().equals(commandtype.C_RETURN)) {
            throw new RuntimeException("No arg1 if type is C_RETURN");
        } else {
            return currentLine[1];
        }
    }

    public int arg2() {
        if (commandType().compareTo(commandtype.C_PUSH) == 0
                || commandType().compareTo(commandtype.C_POP) == 0
                || commandType().compareTo(commandtype.C_FUNCTION) == 0
                || commandType().compareTo(commandtype.C_CALL) == 0) {
            return Integer.parseInt(currentLine[2]);
        } else {
            throw new RuntimeException("No arg2 if type is "
                    + commandType().toString());
        }
    }

    public void prase() {
        while (!hasMoreCommands()) {
            advance();

            if (currentTpye.compareTo(commandtype.C_PUSH) == 0
                    || currentTpye.compareTo(commandtype.C_POP) == 0) {
                code.writePushPop(currentLine[0], arg1(), arg2());
            } else if (currentTpye.compareTo(commandtype.C_ARITHMETIC) == 0) {
                code.writeArithmetic(currentLine[0]);
            } else if (currentTpye.compareTo(commandtype.C_LABEL) == 0) {
                code.writeLabel(MessageFormat.format(CodeWriter.LABEL_PATTEN1,
                        functionName, arg1()));
            } else if (currentTpye.compareTo(commandtype.C_IF) == 0) {
                code.writeIf(MessageFormat.format(CodeWriter.LABEL_PATTEN1,
                        functionName, arg1()));
            } else if (currentTpye.compareTo(commandtype.C_GOTO) == 0) {
                code.writeGoto(MessageFormat.format(CodeWriter.LABEL_PATTEN1,
                        functionName, arg1()));
            } else if (currentTpye.compareTo(commandtype.C_FUNCTION) == 0) {
                code.writeFunction(arg1(), arg2());
            } else if (currentTpye.compareTo(commandtype.C_RETURN) == 0) {
                code.writeReturn();
            } else if (currentTpye.compareTo(commandtype.C_CALL) == 0) {
                code.writeCall(arg1(), arg2());
            }
        }
    }

}
