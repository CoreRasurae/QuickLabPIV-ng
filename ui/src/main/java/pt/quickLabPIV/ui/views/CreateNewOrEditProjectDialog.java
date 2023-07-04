package pt.quickLabPIV.ui.views;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.models.LocalRemoteEnum;
import pt.quickLabPIV.ui.models.ProjectModel;
import pt.quickLabPIV.ui.views.panels.RemoteProjectPanel;

public class CreateNewOrEditProjectDialog extends JDialog {
    public enum CreateOrEditMode {
        CREATE,
        EDIT;
    }
    
    private boolean canceled = true;
    private LocalRemoteEnum projectType;
    private ProjectModel project;
    private CreateOrEditMode mode;
    
    /**
     * 
     */
    private static final long serialVersionUID = 1437017839122932623L;
    private final JPanel contentPanel = new JPanel();
    private JTextField titleTextField;
    private JTextArea textArea;
    private JTextField dateTextField;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final RemoteProjectPanel remotePanel = new RemoteProjectPanel();
    private JPanel localRemotePanel;
    private JLabel lblTitle;
    private JLabel lblDate;
    private JLabel lblDescription;
    private JButton okButton;
    private JRadioButton rdbtnLocalProject;
    private JRadioButton rdbtnRemoteProject;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            CreateNewOrEditProjectDialog dialog = new CreateNewOrEditProjectDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public CreateNewOrEditProjectDialog() {
        setModal(true);
        setTitle("Create new project...");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 450, 354);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {450};
        gridBagLayout.rowHeights = new int[] {151, 35, 30, 35, 0};
        gridBagLayout.columnWeights = new double[]{1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        getContentPane().setLayout(gridBagLayout);
        contentPanel.setBorder(new TitledBorder(null, "Project details", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        GridBagConstraints gbc_contentPanel = new GridBagConstraints();
        gbc_contentPanel.gridheight = 4;
        gbc_contentPanel.fill = GridBagConstraints.BOTH;
        gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
        gbc_contentPanel.gridx = 0;
        gbc_contentPanel.gridy = 0;
        getContentPane().add(contentPanel, gbc_contentPanel);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] {110, 31};
        gbl_contentPanel.rowHeights = new int[]{15, 0, 0, 147, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, 1.0};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            lblTitle = new JLabel("Title");
            GridBagConstraints gbc_lblTitle = new GridBagConstraints();
            gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
            gbc_lblTitle.anchor = GridBagConstraints.NORTHWEST;
            gbc_lblTitle.gridx = 0;
            gbc_lblTitle.gridy = 0;
            contentPanel.add(lblTitle, gbc_lblTitle);
        }
        {
            titleTextField = new JTextField();
            lblTitle.setLabelFor(titleTextField);
            GridBagConstraints gbc_titleTextField = new GridBagConstraints();
            gbc_titleTextField.insets = new Insets(0, 0, 5, 0);
            gbc_titleTextField.fill = GridBagConstraints.HORIZONTAL;
            gbc_titleTextField.gridx = 1;
            gbc_titleTextField.gridy = 0;
            contentPanel.add(titleTextField, gbc_titleTextField);
            titleTextField.setColumns(10);
        }
        {
            lblDate = new JLabel("Date");
            GridBagConstraints gbc_lblDate = new GridBagConstraints();
            gbc_lblDate.anchor = GridBagConstraints.NORTHWEST;
            gbc_lblDate.insets = new Insets(0, 0, 5, 5);
            gbc_lblDate.gridx = 0;
            gbc_lblDate.gridy = 1;
            contentPanel.add(lblDate, gbc_lblDate);
        }
        {
            dateTextField = new JTextField();
            lblDate.setLabelFor(dateTextField);
            GridBagConstraints gbc_dateTextField = new GridBagConstraints();
            gbc_dateTextField.insets = new Insets(0, 0, 5, 0);
            gbc_dateTextField.fill = GridBagConstraints.HORIZONTAL;
            gbc_dateTextField.gridx = 1;
            gbc_dateTextField.gridy = 1;
            contentPanel.add(dateTextField, gbc_dateTextField);
            dateTextField.setColumns(10);
        }
        {
            lblDescription = new JLabel("Description");
            GridBagConstraints gbc_lblDescription = new GridBagConstraints();
            gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
            gbc_lblDescription.gridheight = 2;
            gbc_lblDescription.insets = new Insets(0, 0, 0, 5);
            gbc_lblDescription.gridx = 0;
            gbc_lblDescription.gridy = 2;
            contentPanel.add(lblDescription, gbc_lblDescription);
        }
        {
            textArea = new JTextArea();
            lblDescription.setLabelFor(textArea);
            textArea.setTabSize(4);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            GridBagConstraints gbc_textArea = new GridBagConstraints();
            gbc_textArea.gridheight = 2;
            gbc_textArea.fill = GridBagConstraints.BOTH;
            gbc_textArea.gridx = 1;
            gbc_textArea.gridy = 2;
            contentPanel.add(textArea, gbc_textArea);
        }
        {
            JPanel buttonPane = new JPanel();
            GridBagConstraints gbc_buttonPane = new GridBagConstraints();
            gbc_buttonPane.anchor = GridBagConstraints.SOUTH;
            gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
            gbc_buttonPane.gridx = 0;
            gbc_buttonPane.gridy = 5;
            getContentPane().add(buttonPane, gbc_buttonPane);
            {
                GridBagLayout gbl_buttonPane = new GridBagLayout();
                gbl_buttonPane.columnWidths = new int[] {217, 104};
                gbl_buttonPane.rowHeights = new int[] {35};
                gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, 0.0};
                gbl_buttonPane.rowWeights = new double[]{0.0};
                buttonPane.setLayout(gbl_buttonPane);
                {
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.setHorizontalAlignment(SwingConstants.TRAILING);
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            setVisible(false);
                        }
                    });
                    okButton = new JButton("Create");
                    okButton.setHorizontalAlignment(SwingConstants.LEADING);
                    okButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (validateFields()) {
                                canceled = false;
                                setVisible(false);
                            }
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
                    cancelButton.setActionCommand("Cancel");
                    GridBagConstraints gbc_cancelButton = new GridBagConstraints();
                    gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
                    gbc_cancelButton.gridx = 2;
                    gbc_cancelButton.gridy = 0;
                    buttonPane.add(cancelButton, gbc_cancelButton);
                }
            }
        }
        {
            localRemotePanel = new JPanel();
            localRemotePanel.setBorder(new TitledBorder(null, "Project type", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            GridBagConstraints gbc_localRemotePanel = new GridBagConstraints();
            gbc_localRemotePanel.insets = new Insets(0, 0, 5, 0);
            gbc_localRemotePanel.fill = GridBagConstraints.BOTH;
            gbc_localRemotePanel.gridx = 0;
            gbc_localRemotePanel.gridy = 4;
            getContentPane().add(localRemotePanel, gbc_localRemotePanel);
            {
                rdbtnLocalProject = new JRadioButton("Local project");
                rdbtnLocalProject.setHorizontalAlignment(SwingConstants.CENTER);
                rdbtnLocalProject.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JRadioButton source = (JRadioButton)e.getSource();
                        if (source.isSelected()) {
                            showRemoteProjectPanel(false);
                        }
                    }
                });
                GridBagLayout gbl = new GridBagLayout();
                gbl.columnWeights = new double[]{1.0, 0.0, 0.0};
                localRemotePanel.setLayout(gbl);
                buttonGroup.add(rdbtnLocalProject);
                rdbtnLocalProject.setSelected(true);
                GridBagConstraints gbc_rdbtnLocalProject = new GridBagConstraints();
                gbc_rdbtnLocalProject.insets = new Insets(0, 0, 0, 5);
                gbc_rdbtnLocalProject.gridy = 0;
                gbc_rdbtnLocalProject.gridx = 1;
                localRemotePanel.add(rdbtnLocalProject, gbc_rdbtnLocalProject);
            }
            {
                rdbtnRemoteProject = new JRadioButton("Remote project");
                rdbtnRemoteProject.setHorizontalAlignment(SwingConstants.CENTER);
                rdbtnRemoteProject.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JRadioButton source = (JRadioButton)e.getSource();
                        if (source.isSelected()) {
                            showRemoteProjectPanel(true);
                        }
                    }
                });
                buttonGroup.add(rdbtnRemoteProject);
                GridBagConstraints gbc_rdbtnRemoteProject = new GridBagConstraints();
                gbc_rdbtnRemoteProject.gridy = 0;
                gbc_rdbtnRemoteProject.gridx = 2;
                localRemotePanel.add(rdbtnRemoteProject, gbc_rdbtnRemoteProject);
            }
        }
        
        showRemoteProjectPanel(false);
    }

    protected boolean validateFields() {
        String validateTitle = getTextField().getText();
        
        if (validateTitle.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Project title cannot be empty.", "Validation error", 
                    JOptionPane.OK_OPTION | JOptionPane.WARNING_MESSAGE);
            return false;
        }

        project.setTitle(validateTitle);
        project.setDate(getDateTextField().getText());
        project.setDescription(getTextArea().getText());
        project.setProjectType(projectType);
        if (projectType == LocalRemoteEnum.Remote) {
            project.setRemoteServer(remotePanel.getRemoteAddress());
        }
        return true;
    }
    
    private void showRemoteProjectPanel(boolean show) {
        JPanel localRemotePanel = getLocalRemotePanel();
        if (show) {
            projectType = LocalRemoteEnum.Remote;
            //
            buttonGroup.setSelected(rdbtnRemoteProject.getModel(), true);
            //
            remotePanel.setSize(remotePanel.getPreferredSize());
            remotePanel.setVisible(true);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 3;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            localRemotePanel.add(remotePanel, gbc);
        } else {
            projectType = LocalRemoteEnum.Local;
            //
            buttonGroup.setSelected(rdbtnLocalProject.getModel(), true);
            //
            localRemotePanel.remove(remotePanel);
        }
        pack();
    }
    
    protected JTextField getTextField() {
        return titleTextField;
    }
    
    protected JTextArea getTextArea() {
        return textArea;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
       
    protected JPanel getLocalRemotePanel() {
        return localRemotePanel;
    }

    protected JTextField getDateTextField() {
        return dateTextField;
    }

    public void setCreateOrEditMode(CreateOrEditMode _mode) {
        mode = _mode;
        switch (mode) {
        case CREATE:
            project = new ProjectModel();
            okButton.setText("Create");
            setTitle("Create new project");
            break;
        case EDIT:
            project = null;
            okButton.setText("Apply");
            setTitle("Edit project");
            break;
        default:
            throw new UIException("Unkown mode", "Unrecognized mode passe to CreateNewOrEditProjectDialog");
        }
    }
    
    public void setProject(ProjectModel model) {
        if (CreateOrEditMode.EDIT == mode) {
            project = model;            
            titleTextField.setText(project.getTitle());
            dateTextField.setText(project.getDate());
            textArea.setText(project.getDescription());
            remotePanel.setRemoteAddress(model.getRemoteServer());
        }
        
        if (project.getProjectType() == null) {
        	project.setProjectType(LocalRemoteEnum.Local);
        }
        
        if (project.getProjectType() == LocalRemoteEnum.Remote) {
            showRemoteProjectPanel(true);
        }
    }
    
    public ProjectModel getProject() {
        return project;
    }
    protected JButton getOkButton() {
        return okButton;
    }
    protected JRadioButton getRdbtnLocalProject() {
        return rdbtnLocalProject;
    }
    protected JRadioButton getRdbtnRemoteProject() {
        return rdbtnRemoteProject;
    }
}
