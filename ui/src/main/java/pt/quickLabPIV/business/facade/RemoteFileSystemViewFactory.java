package pt.quickLabPIV.business.facade;

import javax.swing.filechooser.FileSystemView;

import pt.quickLabPIV.exceptions.UIException;

public class RemoteFileSystemViewFactory implements IFileSystemViewFactory {

    @Override
    public FileSystemView createView() {
        throw new UIException("Remote projects are unsupported yet", "Cannot create remote file system view");
    }

}
