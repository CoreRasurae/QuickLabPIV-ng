package pt.quickLabPIV.business.facade;

import javax.swing.filechooser.FileSystemView;

public class LocalFileSystemViewFactory implements IFileSystemViewFactory {

    @Override
    public FileSystemView createView() {
        FileSystemView view = FileSystemView.getFileSystemView();
        return view;
    }

}
