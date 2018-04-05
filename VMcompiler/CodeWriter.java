import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;


public class CodeWriter {
    private String asmName;
    private String curVMFileName;
    private FileWriter fw;
    private String filePath;
    private String LINE_SEPARATOR = System.getProperty("line.separator");
    private int seq = -1;
    private static final String LABEL_PATTEN3 = "{0}.{1}.{2}";//模板
    private static final String LABEL_PATTEN2 = "{0}.{1}";
    public static final String LABEL_PATTEN1 = "{0}${1}";

    public void setPath(String filePath) {
        this.filePath = filePath.endsWith(File.separator) ? filePath : filePath
                + File.separator;
    }

    public void setAsmName(final String asmStr) {
        this.asmName = asmStr;
    }

    public void setFileName(String filename) {
        String asmFileStr = (asmName == null) ? filename : asmName;
        File file = new File(filePath + asmFileStr + ".asm");
        if (file.exists()) {
            file.delete();
        }

        try {
            fw = new FileWriter(file);
            writeInit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArithmetic(String command) {
        StringBuilder strBuf = new StringBuilder();
        String lab1, lab2, lab3, result;

        if (command.equalsIgnoreCase("add") || command.equalsIgnoreCase("sub") ||
                command.equalsIgnoreCase("and") || command.equalsIgnoreCase("or")) {
            strBuf.append("@").append("SP").append(LINE_SEPARATOR)
                    .append("AM=M-1").append(LINE_SEPARATOR).append("D=M")
                    .append(LINE_SEPARATOR).append("@").append("SP")
                    .append(LINE_SEPARATOR).append("AM=M-1")
                    .append(LINE_SEPARATOR).append("[wildcard]")
                    .append(LINE_SEPARATOR).append("@").append("SP")
                    .append(LINE_SEPARATOR).append("M=M+1")
                    .append(LINE_SEPARATOR);
        } else if (command.equalsIgnoreCase("eq") || command.equalsIgnoreCase("gt") ||
                command.equalsIgnoreCase("lt")) {
            ++seq;
            lab1 = MessageFormat.format(LABEL_PATTEN3, "COMP",
                    seq, "TRUE");
            lab2 = MessageFormat.format(LABEL_PATTEN3, "COMP",
                    seq, "FALSE");
            lab3 = MessageFormat.format(LABEL_PATTEN3, "COMP",
                    seq, "END");

            strBuf.append("@").append("SP").append(LINE_SEPARATOR)
                    .append("AM=M-1").append(LINE_SEPARATOR).append("D=M")
                    .append(LINE_SEPARATOR).append("@").append("SP")
                    .append(LINE_SEPARATOR).append("AM=M-1")
                    .append(LINE_SEPARATOR).append("D=M-D")
                    .append(LINE_SEPARATOR).append("@").append(lab1)
                    .append(LINE_SEPARATOR).append("[wildcard]")
                    .append(LINE_SEPARATOR).append("@").append(lab2)
                    .append(LINE_SEPARATOR).append("0;JMP")
                    .append(LINE_SEPARATOR).append("(").append(lab1)
                    .append(")").append(LINE_SEPARATOR).append("@")
                    .append("SP").append(LINE_SEPARATOR).append("A=M")
                    .append(LINE_SEPARATOR).append("M=-1")
                    .append(LINE_SEPARATOR).append("@").append("SP")
                    .append(LINE_SEPARATOR).append("M=M+1")
                    .append(LINE_SEPARATOR).append("@").append(lab3)
                    .append(LINE_SEPARATOR).append("0;JMP")
                    .append(LINE_SEPARATOR).append("(").append(lab2)
                    .append(")").append(LINE_SEPARATOR).append("@")
                    .append("SP").append(LINE_SEPARATOR).append("A=M")
                    .append(LINE_SEPARATOR).append("M=0")
                    .append(LINE_SEPARATOR).append("@").append("SP")
                    .append(LINE_SEPARATOR).append("M=M+1")
                    .append(LINE_SEPARATOR).append("(").append(lab3)
                    .append(")").append(LINE_SEPARATOR);
        }else if ("neg".equalsIgnoreCase(command)
                || "not".equalsIgnoreCase(command)) {
            strBuf.append("@").append("SP").append(LINE_SEPARATOR)
                    .append("AM=M-1").append(LINE_SEPARATOR)
                    .append("[wildcard]").append(LINE_SEPARATOR).append("@")
                    .append("SP").append(LINE_SEPARATOR).append("M=M+1")
                    .append(LINE_SEPARATOR);
        }
        result = strBuf.toString();

        switch (Arrays.asList(Parser.ARITHMETIC).indexOf(command)){
        case 0:
        result = result.replace("[wildcard]", "M=D+M");
        break;
        case 1:
        result = result.replace("[wildcard]", "M=M-D");
        break;
        case 2:
        result = result.replace("[wildcard]", "M=-M");
        break;
        case 3:
        result = result.replace("[wildcard]", "D;JEQ");
        break;
        case 4:
        result = result.replace("[wildcard]", "D;JGT");
        break;
        case 5:
        result = result.replace("[wildcard]", "D;JLT");
        break;
        case 6:
        result = result.replace("[wildcard]", "M=D&M");
        break;
        case 7:
        result = result.replace("[wildcard]", "M=D|M");
        break;
        default:
        result = result.replace("[wildcard]", "M=!M");
        break;
    }

		try {
            fw.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePushPop(String command, String segment, int index) {
        StringBuilder strBuf = new StringBuilder();

        if (command.equalsIgnoreCase("push")) {
            if (segment.equalsIgnoreCase("constant")) {
                strBuf.append("@").append(index).append(LINE_SEPARATOR)
                        .append("D=A").append(LINE_SEPARATOR).append("@")
                        .append("SP").append(LINE_SEPARATOR).append("A=M")
                        .append(LINE_SEPARATOR).append("M=D")
                        .append(LINE_SEPARATOR).append("@").append("SP")
                        .append(LINE_SEPARATOR).append("M=M+1")
                        .append(LINE_SEPARATOR);
            } else if (segment.equalsIgnoreCase("local")
                    || segment.equalsIgnoreCase("argument")
                    || segment.equalsIgnoreCase("this")
                    || segment.equalsIgnoreCase("that")) {

                String seg = judge(segment);

                strBuf.append("@").append(index).append(LINE_SEPARATOR)
                        .append("D=A").append(LINE_SEPARATOR).append("@")
                        .append(seg).append(LINE_SEPARATOR).append("A=M")
                        .append(LINE_SEPARATOR).append("D=D+A")
                        .append(LINE_SEPARATOR).append("A=D")
                        .append(LINE_SEPARATOR).append("D=M")
                        .append(LINE_SEPARATOR).append("@").append("SP")
                        .append(LINE_SEPARATOR).append("A=M")
                        .append(LINE_SEPARATOR).append("M=D")
                        .append(LINE_SEPARATOR).append("@").append("SP")
                        .append(LINE_SEPARATOR).append("M=M+1")
                        .append(LINE_SEPARATOR);
            } else if (segment.equalsIgnoreCase("temp")
                    || segment.equalsIgnoreCase("pointer")
                    || segment.equalsIgnoreCase("static")) {
                String var = MessageFormat.format(LABEL_PATTEN2, asmName, index);
                int base = 5;
                if (segment.equalsIgnoreCase("pointer")) {
                    base = 3;
                }

                String varStr = segment.equalsIgnoreCase("static") ? var : "R"
                        + (base + index);

                strBuf.append("@").append(varStr).append(LINE_SEPARATOR)
                        .append("D=M").append(LINE_SEPARATOR).append("@")
                        .append("SP").append(LINE_SEPARATOR).append("A=M")
                        .append(LINE_SEPARATOR).append("M=D")
                        .append(LINE_SEPARATOR).append("@").append("SP")
                        .append(LINE_SEPARATOR).append("M=M+1")
                        .append(LINE_SEPARATOR);
            }
        } else if (command.equalsIgnoreCase("pop")) {
                if (segment.equalsIgnoreCase("local")
                        || segment.equalsIgnoreCase("argument")
                        || segment.equalsIgnoreCase("this")
                        || segment.equalsIgnoreCase("that")) {

                    String seg = judge(segment);


                    strBuf.append("@").append(index).append(LINE_SEPARATOR)
                            .append("D=A").append(LINE_SEPARATOR).append("@")
                            .append(seg).append(LINE_SEPARATOR).append("A=M")
                            .append(LINE_SEPARATOR).append("D=D+A")
                            .append(LINE_SEPARATOR).append("@").append(seg)
                            .append(LINE_SEPARATOR).append("M=D")
                            .append(LINE_SEPARATOR).append("@").append("SP")
                            .append(LINE_SEPARATOR).append("AM=M-1")
                            .append(LINE_SEPARATOR).append("D=M")
                            .append(LINE_SEPARATOR).append("@").append(seg)
                            .append(LINE_SEPARATOR).append("A=M")
                            .append(LINE_SEPARATOR).append("M=D")
                            .append(LINE_SEPARATOR).append("@").append(index)
                            .append(LINE_SEPARATOR).append("D=A")
                            .append(LINE_SEPARATOR).append("@").append(seg)
                            .append(LINE_SEPARATOR).append("A=M")
                            .append(LINE_SEPARATOR).append("D=A-D")
                            .append(LINE_SEPARATOR).append("@").append(seg)
                            .append(LINE_SEPARATOR).append("M=D")
                            .append(LINE_SEPARATOR);
                }

                if (segment.equalsIgnoreCase("temp")
                        || segment.equalsIgnoreCase("pointer")
                        || segment.equalsIgnoreCase("static")) {
                    String var = MessageFormat.format(LABEL_PATTEN2, asmName, index);
                    int base = 5;
                    if (segment.equalsIgnoreCase("pointer")) {
                        base = 3;
                    }

                    String varStr = segment.equalsIgnoreCase("static") ? var : "R"
                            + (base + index);

                    strBuf.append("@").append("SP").append(LINE_SEPARATOR)
                            .append("AM=M-1").append(LINE_SEPARATOR).append("D=M")
                            .append(LINE_SEPARATOR).append("@")
                            .append(varStr)
                            .append(LINE_SEPARATOR).append("M=D")
                            .append(LINE_SEPARATOR);
                }
            }

        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        }

    public void writeInit() {
        StringBuilder strBuf = new StringBuilder();
        int spIndex = 0x100;
        strBuf.append("@").append(spIndex).append(LINE_SEPARATOR)
                .append("D=A").append(LINE_SEPARATOR).append("@").append("SP")
                .append(LINE_SEPARATOR).append("M=D").append(LINE_SEPARATOR);
        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeCall("Sys.init", 0);
    }

    public void writeLabel(String label) {
        StringBuilder strBuf = new StringBuilder();

        strBuf.append("(").append(label).append(")").append(LINE_SEPARATOR);

        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeGoto(String label) {
        StringBuilder strBuf = new StringBuilder();

        strBuf.append("@").append(label).append(LINE_SEPARATOR)
                .append("0;JMP").append(LINE_SEPARATOR);

        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIf(String label) {
        StringBuilder strBuf = new StringBuilder();

        strBuf.append("@").append("SP").append(LINE_SEPARATOR)
                .append("AM=M-1").append(LINE_SEPARATOR).append("D=M")
                .append(LINE_SEPARATOR).append("@").append(label)
                .append(LINE_SEPARATOR).append("D;JNE").append(LINE_SEPARATOR);

        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCall(String functionName, int numArgs) {
        StringBuilder strBuf = new StringBuilder();
        String retLab = "RETURN" + (++seq);
        // PUSH return-address
        strBuf.append("@")
                .append(retLab)
                .append(LINE_SEPARATOR)
                .append("D=A")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=M+1")
                .append(LINE_SEPARATOR)
                // PUSH LCL
                .append("@")
                .append("LCL")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=M+1")
                .append(LINE_SEPARATOR)
                // PUSH ARG
                .append("@")
                .append("ARG")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=M+1")
                .append(LINE_SEPARATOR)
                // PUSH this
                .append("@")
                .append("this")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=M+1")
                .append(LINE_SEPARATOR)
                // PUSH that
                .append("@").append("that").append(LINE_SEPARATOR)
                .append("D=M").append(LINE_SEPARATOR).append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=M+1")
                .append(LINE_SEPARATOR)
                // ARG = SP-n-5
                .append("@").append("SP").append(LINE_SEPARATOR).append("D=M")
                .append(LINE_SEPARATOR).append("@").append(numArgs)
                .append(LINE_SEPARATOR).append("D=D-A").append(LINE_SEPARATOR)
                .append("@").append(5).append(LINE_SEPARATOR).append("D=D-A")
                .append(LINE_SEPARATOR).append("@").append("ARG")
                .append(LINE_SEPARATOR).append("M=D")
                .append(LINE_SEPARATOR)
                // LCL = SP
                .append("@").append("SP").append(LINE_SEPARATOR).append("D=M")
                .append(LINE_SEPARATOR).append("@").append("LCL")
                .append(LINE_SEPARATOR).append("M=D").append(LINE_SEPARATOR);

        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // GOTO f
        writeGoto(functionName);
        // (return-address)
        writeLabel(retLab);
    }

    public void writeReturn() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("@")
                .append("LCL")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("frame")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // RET = *(FRAME-5)
                .append("@")
                .append(5)
                .append(LINE_SEPARATOR)
                .append("D=D-A")
                .append(LINE_SEPARATOR)
                .append("A=D")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("ret")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // *ARG = pop
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=M-1")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("ARG")
                .append(LINE_SEPARATOR)
                .append("A=M")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // SP = ARG+1
                .append("@")
                .append("ARG")
                .append(LINE_SEPARATOR)
                .append("D=M+1")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("SP")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // that = *(FRAME-1)
                .append("@")
                .append("frame")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append(1)
                .append(LINE_SEPARATOR)
                .append("D=D-A")
                .append(LINE_SEPARATOR)
                .append("A=D")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("that")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // this = *(FRAME-2)
                .append("@")
                .append("frame")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append(2)
                .append(LINE_SEPARATOR)
                .append("D=D-A")
                .append(LINE_SEPARATOR)
                .append("A=D")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("this")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // ARG = *(FRAME-3)
                .append("@").append("frame").append(LINE_SEPARATOR)
                .append("D=M").append(LINE_SEPARATOR).append("@").append(3)
                .append(LINE_SEPARATOR)
                .append("D=D-A")
                .append(LINE_SEPARATOR)
                .append("A=D")
                .append(LINE_SEPARATOR)
                .append("D=M")
                .append(LINE_SEPARATOR)
                .append("@")
                .append("ARG")
                .append(LINE_SEPARATOR)
                .append("M=D")
                .append(LINE_SEPARATOR)
                // LCL = *(FRAME-4)
                .append("@").append("frame").append(LINE_SEPARATOR)
                .append("D=M").append(LINE_SEPARATOR).append("@").append(4)
                .append(LINE_SEPARATOR).append("D=D-A").append(LINE_SEPARATOR)
                .append("A=D").append(LINE_SEPARATOR).append("D=M")
                .append(LINE_SEPARATOR).append("@").append("LCL")
                .append(LINE_SEPARATOR).append("M=D")
                .append(LINE_SEPARATOR)
                // JUMP TO Caller
                .append("@").append("ret").append(LINE_SEPARATOR)
                .append("A=M").append(LINE_SEPARATOR).append("0;JMP")
                .append(LINE_SEPARATOR);

        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFunction(String functionName, int numLocals) {
        writeLabel(functionName);
        StringBuilder strBuf = new StringBuilder();
        for (int i = 0; i < numLocals; i++) {
            strBuf.append("@").append("SP").append(LINE_SEPARATOR).append("A=M")
                    .append(LINE_SEPARATOR).append("M=0")
                    .append(LINE_SEPARATOR).append("@").append("SP")
                    .append(LINE_SEPARATOR).append("M=M+1")
                    .append(LINE_SEPARATOR);
        }
        try {
            fw.write(strBuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String judge(String segment) {
        if (segment.equalsIgnoreCase("local")) {
            return "LCL";
        } else if (segment.equalsIgnoreCase("argument")) {
            return "ARG";
        } else if (segment.equalsIgnoreCase("this")) {
            return "this";
        } else {
            return "that";
        }
    }

    public void close() {
        if (fw != null) {

            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
