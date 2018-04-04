
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;


public class CodeWriter {
    private String asmName;
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

    public void setFileName(String filename) {
        this.asmName = filename;
        File file = new File(filePath + asmName + ".asm");
        if (file.exists()) {
            file.delete();
        }

        try {
            fw = new FileWriter(file);
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

    private String judge(String segment) {
        if (segment.equalsIgnoreCase("local")) {
            return "LCL";
        } else if (segment.equalsIgnoreCase("argument")) {
            return "ARG";
        } else if (segment.equalsIgnoreCase("this")) {
            return "THIS";
        } else {
            return "THAT";
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
