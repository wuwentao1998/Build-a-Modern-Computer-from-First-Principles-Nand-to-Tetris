package assembler;

public class Launch{

        public static void main(String[] args) {
            if (args.length < 1 || args.length > 1) {
                System.out.println("Please enter exactly one argument!");
                return;
            }
            SymbolTable symTable = new SymbolTable();
            Code code = new Code();
            String t = args[0];
            Parser parser = new Parser(args[0], symTable, code);
            parser.compile();
        }

}
