// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
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
