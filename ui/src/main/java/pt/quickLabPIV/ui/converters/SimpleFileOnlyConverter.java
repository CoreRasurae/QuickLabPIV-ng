package pt.quickLabPIV.ui.converters;

import java.io.File;

import org.jdesktop.beansbinding.Converter;

public class SimpleFileOnlyConverter extends Converter<File, String> {
    
    @Override
    public File convertReverse(String name) {
        File f = new File(name);
        return f;
    }

    @Override
    public String convertForward(File file) {
        return file.getName();
    }

}
