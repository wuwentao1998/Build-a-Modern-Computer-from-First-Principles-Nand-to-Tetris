import com.sun.org.apache.bcel.internal.classfile.Code;

import java.io.File;
import java.io.FileFilter;

public class VMTranslator {
        public static CodeWriter cWriter = null;//必须是static的,因为并没有创建VMTranslator的实例

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Lack of arguments!");
            return;
        }
        File file = new File(args[0].trim());

        if (!file.exists()) {
            System.out.println("Can't find the file!");
            return;
        }

        if (file.isFile()) {
            if (file.getName().lastIndexOf(".vm") < 0) {
                System.out.println("The file is not a vm file!");
                return;
            }
            cWriter = new CodeWriter();
            cWriter.setPath(file.getPath().substring(0, file.getPath().lastIndexOf(file.getName())));
            doTrans(file);
        } else if (file.isDirectory()) {
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().lastIndexOf(".vm") > 0;
                }
            };

            cWriter = new CodeWriter();
            cWriter.setPath(file.getPath());
            cWriter.setAsmName(file.getPath());
            for (File f : file.listFiles(filter)) {
                doTrans(f);
            }

        }
        cWriter.close();
    }

    private static void doTrans(File file) {
        cWriter.setFileName(file.getName().substring(0, file.getName().indexOf(".vm")));
        Parser parser = new Parser(file);
        parser.setCode(cWriter);
        parser.prase();
    }
}
