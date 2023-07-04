package pt.quickLabPIV.business.facade;

import java.io.File;

import pt.quickLabPIV.exceptions.UIException;

public class RemoteFileFactory implements IFileFactory {

    @Override
    public File createFile(String path) {
        throw new UIException("Remote files are not supported yet");
    }

    @Override
    public File createFile(File parent, String path) {
        throw new UIException("Remote files are not supported yet");
    }

}
