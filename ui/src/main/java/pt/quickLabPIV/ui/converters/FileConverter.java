package pt.quickLabPIV.ui.converters;

import java.io.File;

public class FileConverter extends ConverterWithForwardValidator<File, String> {

    @Override
    public File convertReverse(String path) {
        File f = new File(path);
        return f;
    }

    @Override
    public String convertForwardAfterValidation(File file) {
        return file.getAbsolutePath();
    }

}
