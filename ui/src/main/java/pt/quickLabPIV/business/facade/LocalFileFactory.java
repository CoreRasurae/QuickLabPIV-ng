package pt.quickLabPIV.business.facade;

import java.io.File;

public class LocalFileFactory implements IFileFactory {

    @Override
    public File createFile(String path) {
        return new File(path);
    }

    @Override
    public File createFile(File parent, String path) {
        return new File(parent, path);
    }

}
