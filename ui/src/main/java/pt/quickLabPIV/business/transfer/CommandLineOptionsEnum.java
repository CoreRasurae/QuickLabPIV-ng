package pt.quickLabPIV.business.transfer;

import java.util.Properties;

import pt.quickLabPIV.exceptions.InvalidOptionException;

public enum CommandLineOptionsEnum {
    PROJECT_FILE("projectFile", " <project file to load.xml>"),
    PIXEL_DEPTH("pixelDepth", " <xx> xx bits per pixel, either 8 or 16");
    
    
    private String optionKey;
    private String optionHelp;
    
    private CommandLineOptionsEnum(String _optionKey, String helpMessage) {
        optionKey = _optionKey;
        optionHelp = helpMessage;
    }
        
    public static Properties parseOptions(String[] args) {
        Properties options = new Properties();
        for (int index = 0; index < args.length; index++) {
            if (args[index].equals(PIXEL_DEPTH.commandLineOptionKey())) {
                int bits = 8;
                
                if (args.length <= index + 1) {
                  throw new InvalidOptionException("--pixelDepth requires an argument");  
                }
                
                try {
                    bits = Integer.parseInt(args[index+1]);
                } catch (NumberFormatException e) {
                    throw new InvalidOptionException("--pixelDepth must be either 8, 10, 12 or 16 bits");  
                }
                
                if (bits != 8 && bits != 10 && bits != 12 && bits != 16) {
                    throw new InvalidOptionException("--pixelDepth must be either 8, 10, 12 or 16 bits");
                }
                
               options.put(PIXEL_DEPTH.key(), bits);
               index++;
            }
            
            if (args[index].equals(PROJECT_FILE.commandLineOptionKey())) {
                if (args.length <= index + 1) {
                    throw new InvalidOptionException("--projectFile requires an argument");  
                }
                                
                String argument = args[index+1];
                if (args[index+1].startsWith("\"")) {
                    int lastIndex = index + 2;
                    if (args.length > lastIndex) {
                        for (; args.length > lastIndex && !args[lastIndex].endsWith("\""); lastIndex++);
                    }
                    if (args.length > lastIndex) {
                        throw new InvalidOptionException("Couldn't find end of argument for --projectFile option");
                    }
                    
                    StringBuilder sb = new StringBuilder(100);
                    for (int i = index + 1; i <= lastIndex; i++) {
                        sb.append(args[i]);
                    }
                    argument = sb.toString();
                    index = lastIndex;
                }
                
                options.put(PROJECT_FILE.key(), argument);
                index++;                
            }
        }
        
        return options;   
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        
        sb.append("--");
        sb.append(optionKey);
        sb.append(" - ");
        sb.append(optionHelp);
        
        return sb.toString();
    }
    
    public static void showShellModeOptionsHelper() {
        System.out.println("--shellMode options\n");
        System.out.println("");
        for (CommandLineOptionsEnum option : values()) {
            System.out.println(option);
        }
    }
    
    public String key() {
        return optionKey;
    }
    
    public String commandLineOptionKey() {
        return "--" + optionKey;
    }
}
