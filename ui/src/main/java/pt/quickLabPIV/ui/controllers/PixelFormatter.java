package pt.quickLabPIV.ui.controllers;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class PixelFormatter extends AbstractFormatter {
    private final String regexPattern = "[0-9]*";
    private final Pattern compiledPattern = Pattern.compile(regexPattern);
    
    /**
     * 
     */
    private static final long serialVersionUID = 2171785036992636770L;

    public PixelFormatter() {
        
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        Matcher m = compiledPattern.matcher(text);
        if (!m.matches()) {            
            throw new ParseException("Entered text is not a valid pixel value", m.end());
        }
        
        int value;
        try {
            value = Integer.parseInt(text);
        }catch (NumberFormatException e) {
            throw new ParseException("Cannot convert text to pixel value", 0);
        }
        
        setEditValid(true);
        return value;
    }

    @Override
    public String valueToString(Object obj) throws ParseException {
        if (!(obj instanceof Integer)) {
            throw new ParseException("Value is not an integer value", 0);
        }
        
        int value = (Integer)obj;
        if (value < 0) {
            throw new ParseException("Value cannot be negative", 0);
        }
        
        return String.valueOf(value);
    }

}
