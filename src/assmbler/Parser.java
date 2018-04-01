package assembler;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private final String path;
    private String filename;
    private SymbolTable sybTable;
    private Code code;
    private int AllLine;
    private int CurrentLine = 0;
    private HashMap<Integer, String> codelist;
    private String CurrentCode;

    private enum CommandType {
        A_COMMAND, C_COMMAND, L_COMMAND
    }

    public Parser(final String path, final SymbolTable sybTable, final Code code) {
        this.path = path;
        this.code = code;
        this.sybTable = sybTable;
        codelist = new HashMap<Integer, String>();
        initial();
    }

    private void initial() {
        File file = new File(path);

        //检验文件合法性

        if (!file.exists()) {
            System.out.println("File '" + file.getAbsolutePath() + "' doesn't exist!");
            return;
        }

        filename = file.getName(); //path可能包含路径,不好分割
        if (! filename.substring(filename.lastIndexOf(".")+1).equals("asm")) {//字符创不能用不等号比较是否相等
            System.out.println("Assembler only accept asm file!");
            return;
        }

        FileReader fr = null; //try中创建的fr不能在后面使用
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);

        //创建符号表,去掉注释

        String temp = null;
        int currentline = -1;
        try {
            temp = br.readLine();
        }catch(IOException e ){
            System.out.println("读取失败！");
        }

        while (temp != null) { //不能在while中创建变量,因为不像for循环中有分号
            if (temp.startsWith("//") || temp.trim().equals("")) {
                try {
                    temp = br.readLine();
                }catch(IOException e ){
                    System.out.println("读取失败！");
                }
                continue;
            }
            if (temp.indexOf("//") > 0) {
                temp = temp.substring(0, temp.indexOf("//")).trim();
            }
            temp = temp.trim();

            if (temp.startsWith("(") && temp.endsWith(")")) {
                sybTable.addEntry(temp.substring(temp.indexOf("(") + 1, temp.lastIndexOf(")")), currentline + 1);
                continue;
            }
            codelist.put(++currentline, temp);

            try {
                temp = br.readLine();
            }catch(IOException e ){
                System.out.println("读取失败！");
            }
        }

        AllLine = currentline;


    }

    public boolean hasMoreCommands() {
        return CurrentLine < AllLine;
    }

    public void advance() {
        if (hasMoreCommands()) {
            CurrentCode = codelist.get(CurrentLine++);
        } else {
            throw new RuntimeException("No more line!");
        }
    }

    public CommandType commandType() {
        if (CurrentCode.startsWith("@")) {
            return CommandType.A_COMMAND;
        }

        if (CurrentCode.startsWith("(") && CurrentCode.endsWith(")")) {
            return CommandType.L_COMMAND;
        }

        return CommandType.C_COMMAND;
    }

    public String symbol() {
        if (commandType().equals(CommandType.A_COMMAND)) {
            return CurrentCode.substring(CurrentCode.indexOf("@") + 1);
        } else if (commandType().equals(CommandType.L_COMMAND)) {
            return CurrentCode.substring(CurrentCode.indexOf("("), CurrentCode.lastIndexOf(")"));
        } else {
            throw new RuntimeException("Line " + CurrentLine + " instruction isn't A_COMMAND or L_COMMAND type!");
        }
    }

    public String dest() {
        if (commandType().equals(CommandType.C_COMMAND)) {
            int start = CurrentCode.indexOf("=");
            if (start > 0) {
                return CurrentCode.substring(0, start);
            } else {
                return "null";
            }
        } else {
            throw new RuntimeException("Line " + CurrentLine + " instruction isn't C_COMMAND type!");
        }
    }

    public String comp() {
        if (commandType().equals(CommandType.C_COMMAND)) {
            int start = CurrentCode.indexOf("=") +1; // substring包含开头,不包含结尾
            int end = CurrentCode.lastIndexOf(";");
            if (end > 0) {
                return CurrentCode.substring(start > 0 ? start : 0, end);
            } else {
                return CurrentCode.substring(start > 0 ? start : 0);
            }
        } else {
            throw new RuntimeException("Line " + CurrentLine + " instruction isn't C_COMMAND type!");
        }
    }

    public String jump() {
        if (commandType().equals(CommandType.C_COMMAND)) {
            int start = CurrentCode.indexOf(";") +1;
            if (start > 0) {
                return CurrentCode.substring(start);
            } else {
                return "null";
            }
        } else {
            throw new RuntimeException("Line " + CurrentLine + " instruction isn't C_COMMAND type!");
        }
    }

    private ArrayList<String> prase() {
        ArrayList<String> codes = new ArrayList<String>();
        int address = -1;
        int varAddress = 0x0F;
        StringBuilder order = new StringBuilder();//便于修改string

        while (hasMoreCommands()) {
            advance();
            if (commandType().compareTo(CommandType.A_COMMAND) == 0) {
                String symbol = symbol();
                if (isNumeric(symbol)) {
                    address = Integer.parseInt(symbol);
                } else {
                    if (sybTable.contains(symbol)) {
                        address = sybTable.GetAddress(symbol);
                    } else {
                        address = ++varAddress;
                        sybTable.addEntry(symbol, address);
                    }
                }
                codes.add(code.getATypeBinary(address) + "/n");

            }

            if (commandType().compareTo(CommandType.C_COMMAND) == 0) {
                order.delete(0, order.length());
                order.append(Code.C_PRIFIX).append(code.comp(comp())).append(code.dest(dest())).append(code.jump(jump()));
                codes.add(order.toString() + "/n");
            }
        }
        return codes;
    }

    private static boolean isNumeric(final String str) {
        for(int i = 0;i < str.length();i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
    return true;
    }

    public void compile() {
        ArrayList<String> codes = prase();
        String destpath = path.substring(0,path.lastIndexOf("."));
        File dest = new File(destpath  + ".hack");
        FileWriter fw = null;
        try {
            fw = new FileWriter(dest);
            for (String str : codes) {
                fw.write(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

