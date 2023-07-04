package pt.quickLabPIV.business.facade;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternFilenameFilter implements FilenameFilter {
    private Pattern pattern;
    
    public PatternFilenameFilter(Pattern _pattern) {
        pattern = _pattern;
    }
    
    @Override
    public boolean accept(File dir, String name) {
        Matcher m = pattern.matcher(name);           
        return m.matches();
    }
}
