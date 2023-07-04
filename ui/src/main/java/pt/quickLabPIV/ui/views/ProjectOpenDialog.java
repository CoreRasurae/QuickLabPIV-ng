package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import pt.quickLabPIV.ui.controllers.NavigationFacade;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.views.panels.ProjectPreviewPanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProjectOpenDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 7133515281901276727L;
    private static File lastOpenedFile = new File(System.getProperty("user.dir"));            
    
    private AppContextModel appContextModel;
    private File selectedProjectFile;
    private final JPanel contentPanel = new JPanel();
    private JFileChooser fc = new JFileChooser(lastOpenedFile);
    private ProjectPreviewPanel previewPane = new ProjectPreviewPanel();
    private final FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory() || f.getName().toLowerCase().endsWith(".xml")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Project files (*.xml)";
        }
    };


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ProjectOpenDialog dialog = new ProjectOpenDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public ProjectOpenDialog() {
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModal(true);
        setTitle("Open Project");
        setBounds(100, 100, 822, 346);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(fileFilter);
        fc.addPropertyChangeListener(previewPane);
        fc.setAccessory(previewPane);
        fc.setControlButtonsAreShown(false);
        {
            JSplitPane splitPane = new JSplitPane() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 3924295579790286127L;
                int location = -1;
                
                @Override
                public void setDividerLocation(int location) {
                    if (location < 0) {
                        super.setDividerLocation(location);
                        location = super.getDividerLocation();
                    }
                    repaint();
                }
                
                @Override
                public int getLastDividerLocation() {
                    return location;
                }

                @Override
                public int getDividerLocation() {
                    return location;
                }

            };
            GridBagConstraints gbc_splitPane = new GridBagConstraints();
            gbc_splitPane.fill = GridBagConstraints.BOTH;
            gbc_splitPane.gridx = 0;
            gbc_splitPane.gridy = 0;
            contentPanel.add(splitPane, gbc_splitPane);
            splitPane.setLeftComponent(fc);
            splitPane.setRightComponent(previewPane);
        }
        {
            JPanel buttonPane = new JPanel();
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            GridBagLayout gbl_buttonPane = new GridBagLayout();
            gbl_buttonPane.columnWidths = new int[]{698, 44, 70, 0};
            gbl_buttonPane.rowHeights = new int[]{27, 0};
            gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
            gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
            buttonPane.setLayout(gbl_buttonPane);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedProjectFile = fc.getSelectedFile();
                        if (selectedProjectFile == null) {
                            NavigationFacade.projectLoadFileWithNoProjectFileSelected(ProjectOpenDialog.this);
                            return;
                        }
                        
                        lastOpenedFile = selectedProjectFile;
                        if (!NavigationFacade.projectConfirmOpenWithPendingChanges(ProjectOpenDialog.this, appContextModel)) {
                            selectedProjectFile = null;
                        }

                        setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                GridBagConstraints gbc_okButton = new GridBagConstraints();
                gbc_okButton.anchor = GridBagConstraints.NORTHWEST;
                gbc_okButton.insets = new Insets(0, 0, 0, 5);
                gbc_okButton.gridx = 1;
                gbc_okButton.gridy = 0;
                buttonPane.add(okButton, gbc_okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedProjectFile = null;
                        setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                GridBagConstraints gbc_cancelButton = new GridBagConstraints();
                gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
                gbc_cancelButton.gridx = 2;
                gbc_cancelButton.gridy = 0;
                buttonPane.add(cancelButton, gbc_cancelButton);
            }
        }
    }

    public void setAppContextModel(AppContextModel model) {
        appContextModel = model;
    }
    
    public File getSelectedProjectFile() {
        return selectedProjectFile;
    }
}
