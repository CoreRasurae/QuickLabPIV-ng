package pt.quickLabPIV.business.facade;

import java.io.File;

public interface IFileFactory {
    public File createFile(String path);
    
    public File createFile(File parent, String path);
}
