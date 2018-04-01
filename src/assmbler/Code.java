package assembler;
import java.util.HashMap;

public class Code {
    public static HashMap<String, String> destTable = new HashMap<String, String>();
    public static HashMap<String, String> compTable = new HashMap<String, String>();
    public static HashMap<String, String> jumpTable = new HashMap<String, String>();

    public static final String C_PRIFIX = "111";

    public Code() {
        destTable.put("null", "000");
        destTable.put("M", "001");
        destTable.put("D", "010");
        destTable.put("MD", "011");
        destTable.put("A", "100");
        destTable.put("AM", "101");
        destTable.put("AD", "110");
        destTable.put("AMD", "111");

        compTable.put("0", "0101010");
        compTable.put("1", "0111111");
        compTable.put("-1", "0111010");
        compTable.put("D", "0001100");
        compTable.put("A", "0110000");
        compTable.put("!D", "0001101");
        compTable.put("!A", "0110001");
        compTable.put("-D", "0001111");
        compTable.put("-A", "0110011");
        compTable.put("D+1", "0011111");
        compTable.put("A+1", "0110111");
        compTable.put("D-1", "0001110");
        compTable.put("A-1", "0110010");
        compTable.put("D+A", "0000010");
        compTable.put("D-A", "0010011");
        compTable.put("A-D", "0000111");
        compTable.put("D&A", "0000000");
        compTable.put("D|A", "0010101");
        compTable.put("M", "1110000");
        compTable.put("!M", "1110001");
        compTable.put("-M", "1110011");
        compTable.put("M+1", "1110111");
        compTable.put("M-1", "1110010");
        compTable.put("D+M", "1000010");
        compTable.put("D-M", "1010011");
        compTable.put("M-D", "1000111");
        compTable.put("D&M", "1000000");
        compTable.put("D|M", "1010101");

        jumpTable.put("null", "000");
        jumpTable.put("JGT", "001");
        jumpTable.put("JEQ", "010");
        jumpTable.put("JGE", "011");
        jumpTable.put("JLT", "100");
        jumpTable.put("JNE", "101");
        jumpTable.put("JLE", "110");
        jumpTable.put("JMP", "111");
    }

    public String dest(String s) {
        String ret = destTable.get(s);
        if (ret == null) {
            throw new RuntimeException("Can not find dest expresstion '" + s + "'");
        }
        return ret;
    }

    public String comp(String s) {
        String t = s;
        String ret = compTable.get(s);
        if (ret == null) {
            throw new RuntimeException("Can not find comp expresstion '" + s + "'");
        }
        return ret;
    }

    public String jump(String s) {
        String ret = jumpTable.get(s);
        if (ret == null) {
            throw new RuntimeException("Can not find jump expresstion '" + s + "'");
        }
        return ret;
    }

    public String getATypeBinary(final int address) {
        String biStr = Integer.toBinaryString(address);
        StringBuffer strB = new StringBuffer();
        for(int i=0; i<16-biStr.length(); i++) {
            strB.append('0');
        }
        return strB.append(biStr).toString();
    }

}
