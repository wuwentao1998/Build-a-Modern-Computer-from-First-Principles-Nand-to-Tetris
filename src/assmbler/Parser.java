package assmbler;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Parser {
    private final String path;
    private SymbolTable sybTable;
    private Code code;
    private int AllLine;
    private int CurrentLine = 0;
    private HashMap<Integer,String> codelist;
    private String CurrentCode;

    private enum CommandType {
        A_COMMAND, C_COMMAND, L_COMMAND
    }

    public Parser (final String path, final SymbolTable sybTable, final Code code) {
        this.path = path;
        this.code = code;
        this.sybTable = sybTable;
        codelist = new HashMap<Integer, String>();
        initial();
    }

    private  void initial() throws IOException { //否则br.readline()总是要抛出异常
        File file = new File(path);

        //检验文件合法性

        if (!file.exists()) {
            System.out.println("File '" + file.getAbsolutePath() + "' doesn't exist!");
            return;
        }

        String filename = file.getName(); //path可能包含路径,不好分割
        if (filename.substring(filename.lastIndexOf(".")) != "asm") {
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

        String temp =null;
        int currentline = -1;
        while ((temp = br.readLine()) != null) { //不能在while中创建变量因为不像for循环中有;
            if (temp.startsWith("//") || temp.trim().equals("")) {
                continue;
            }
            if (temp.indexOf("//") > 0) {
                temp = temp.substring(0,temp.indexOf("//")).trim();
            }
            temp = temp.trim();

            if (temp.startsWith("(") && temp.endsWith(")")) {
                sybTable.addEntry(temp.substring(temp.indexOf("(")+1,temp.lastIndexOf(")")),currentline+1);
            }
            codelist.put(++currentline, temp);
        }

        AllLine = currentline;


    }

    public boolean hasMoreCommands() {
        return CurrentLine < AllLine;
    }

    public void advance() {
        if (hasMoreCommands()) {
            CurrentCode = codelist.get(CurrentLine++);

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
        if (commandType() == CommandType.A_COMMAND ) {
           return CurrentCode.substring(CurrentCode.indexOf("@")+1);
        }
        else if (commandType() == CommandType.L_COMMAND) {
            return CurrentCode.substring(CurrentCode.indexOf("("), CurrentCode.lastIndexOf(")"));
        }
        else {
            throw new RuntimeException("Line " + CurrentLine + " instruction isn't A_COMMAND or L_COMMAND type!");
        }
    }

    public void dest(String dest) {
        if (commandType() == CommandType.C_COMMAND) {

        }
    }

    public void comp(String comp) {
        if (commandType() == CommandType.C_COMMAND) {

        }
    }

    public void jump(String jump) {
        if (commandType() == CommandType.C_COMMAND) {

        }
    }
}
