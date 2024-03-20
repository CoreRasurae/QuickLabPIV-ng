// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.business.facade.DataExportEnvFacade;
import pt.quickLabPIV.business.facade.PIVConfigurationFacade;
import pt.quickLabPIV.business.facade.PIVExecutionEnvironmentFacade;
import pt.quickLabPIV.business.facade.ProjectFacade;
import pt.quickLabPIV.business.facade.ProjectOpenException;
import pt.quickLabPIV.business.transfer.CommandLineOptionsEnum;
import pt.quickLabPIV.business.transfer.ExecuteLocalShellPIV;
import pt.quickLabPIV.exceptions.InvalidExecutionEnvException;
import pt.quickLabPIV.exceptions.InvalidProjectFileException;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.controllers.DataProcessingEnvFacade;
import pt.quickLabPIV.ui.controllers.NavigationFacade;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ExecutionEnvModel;
import pt.quickLabPIV.ui.views.PIVConfigurationDialog.PIVConfigurationTabEnum;
import pt.quickLabPIV.ui.views.PIVImagePreProcessingDialog.PIVImagePreProcessingTabEnum;
import pt.quickLabPIV.ui.views.panels.ImagePanel;
import javax.swing.SwingConstants;

public class QuickLabPIVng {
    private AutoBinding<AppContextModel, BufferedImage, ImagePanel, BufferedImage> imageBinding;
    private AppContextModel appContextModel;
    private JFrame frmQuicklabPivng;
    private JMenu mnProject;
    private JMenuItem mntmSaveProject;
    private JMenuItem mntmSaveProjectAs;
    private ImagePanel panel;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        ExecutionOptions option = parseArgs(args);
        
        Properties options = CommandLineOptionsEnum.parseOptions(args);
        
        switch (option) {
        case ShellRunMode:
            executeShellRunMode(options);
            break;
        case UIMode:            
            executeUiMode(options);
            break;
        default:
            System.err.println("Unknown command line option");
            System.out.println("Usage: QuickLabPIVng <--shellMode>");
            System.exit(1);
        }
    }

    private static ExecutionOptions parseArgs(String[] args) {
        ExecutionOptions option = ExecutionOptions.UIMode;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("--shellMode")) {
                option = ExecutionOptions.ShellRunMode;
            } else {
                System.err.println("Unknown command line option");
                System.out.println("Usage: QuickLabPIVng <--shellMode>");
                System.exit(1);
            }
        }
        return option;
    }

    private static void executeUiMode(final Properties options) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {                
                try {
                    QuickLabPIVng window = new QuickLabPIVng();
                    window.frmQuicklabPivng.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public QuickLabPIVng() {
        NavigationFacade.setLookAndFeel();
        initialize();
    }
    
    public void exit() {
        ExitConfirmationDialog dialog = new ExitConfirmationDialog();
        dialog.setAppContextModel(appContextModel);
        dialog.showDialog();
        if (dialog.isQuit()) {
            System.exit(0);
        }
        
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    /**
     * Initialize the contents of the frame.
     * @wbp.parser.entryPoint
     */
    private void initialize() {
        //Good for multi-screen configurations... is it?
        //GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        /*for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        	GraphicsConfigTemplate gct = new GraphicsConfigTemplate() {

				@Override
				public GraphicsConfiguration getBestConfiguration(GraphicsConfiguration[] gc) {
					return null;
				}

				@Override
				public boolean isGraphicsConfigSupported(GraphicsConfiguration gc) {
					// TODO Auto-generated method stub
					return false;
				}
        		
        	};
        	gd.getBestConfiguration(null);
        }*/
        //int width = gd.getDisplayMode().getWidth();
        //int height = gd.getDisplayMode().getHeight();
    	Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

        int height = (int)dimension.getHeight();
        int width = (int)dimension.getWidth();

        frmQuicklabPivng = new JFrame();
        frmQuicklabPivng.setTitle("QuickLab PIV-ng");
        
        frmQuicklabPivng.setBounds(100, 100, width - 200, height - 200);
        frmQuicklabPivng.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menuBar = new JMenuBar();
        frmQuicklabPivng.setJMenuBar(menuBar);
        
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic('F');
        menuBar.add(mnFile);
        
        JMenuItem mntmNewProject = new JMenuItem("New Project");
        mntmNewProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewProject();
            }
        });
        mnFile.add(mntmNewProject);
        
        JMenuItem mntmLoadProject = new JMenuItem("Load Project");
        mntmLoadProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openProject();
            }
        });
        mnFile.add(mntmLoadProject);
        
        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        
        mntmSaveProject = new JMenuItem("Save Project");
        mntmSaveProject.setEnabled(false);
        mntmSaveProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                appContextModel = NavigationFacade.saveProject((JComponent)e.getSource(), appContextModel);
            }
        });
        mnFile.add(mntmSaveProject);
        
        mntmSaveProjectAs = new JMenuItem("Save Project as...");
        mntmSaveProjectAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                appContextModel = NavigationFacade.saveProjectAs((JComponent)e.getSource(), appContextModel);
            }
        });
        mntmSaveProjectAs.setEnabled(false);
        mnFile.add(mntmSaveProjectAs);
        
        JSeparator separator_2 = new JSeparator();
        mnFile.add(separator_2);
        
        JMenuItem mntmExecuteBatch = new JMenuItem("Execute batch...");
        mnFile.add(mntmExecuteBatch);
        
        JSeparator separator_1 = new JSeparator();
        mnFile.add(separator_1);
        mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK));
        mnFile.add(mntmExit);
        
        JMenu mnEdit = new JMenu("Edit");
        mnEdit.setMnemonic('E');
        menuBar.add(mnEdit);
        
        JMenu mnImageEnhancement = new JMenu("Image enhancement");
        mnEdit.add(mnImageEnhancement);
        
        JSeparator separator = new JSeparator();
        mnEdit.add(separator);
        
        JMenuItem mntmCut = new JMenuItem("Cut");
        mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CUT, 0));
        mnEdit.add(mntmCut);
        
        JMenuItem mntmCopy = new JMenuItem("Copy");
        mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0));
        mnEdit.add(mntmCopy);
        
        JMenuItem mntmPaste = new JMenuItem("Paste");
        mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0));
        mnEdit.add(mntmPaste);
        
        mnProject = new JMenu("Project");
        mnProject.setEnabled(false);
        mnProject.setMnemonic('P');
        menuBar.add(mnProject);
        
        JMenu mnProjectConfiguration = new JMenu("Project configuration");
        mnProject.add(mnProjectConfiguration);
        
        JMenuItem mntmProjectDetails = new JMenuItem("Project details");
        mntmProjectDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editPoject();
            }
        });
        mnProjectConfiguration.add(mntmProjectDetails);
        
        JMenuItem mntmRunEnvironment = new JMenuItem("Run environment");
        mnProjectConfiguration.add(mntmRunEnvironment);
        
        JMenu mnExecutionEnv = new JMenu("Execution environment");
        mnProject.add(mnExecutionEnv);
        
        JMenuItem mntmProcessingConfig = new JMenuItem("Data processing environment configuration");
        mntmProcessingConfig.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configProcessingEnv();
            }
        });
        mnExecutionEnv.add(mntmProcessingConfig);
        
        JMenuItem mntmDataExportEnv = new JMenuItem("Data export environment configuration");
        mntmDataExportEnv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configDataExportEnv();
            }
        });
        mnExecutionEnv.add(mntmDataExportEnv);
        
        JMenu mnImagePreparation = new JMenu("Image preparation");
        mnProject.add(mnImagePreparation);
        
        JMenuItem mntmImageSelection = new JMenuItem("Image selection");
        mntmImageSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVImageSelectionDialog();
            }
        });
        mnImagePreparation.add(mntmImageSelection);
        
        JMenuItem mntmImageFiltering = new JMenuItem("Image filtering");
        mntmImageFiltering.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showImagePreProcessingDialog(PIVImagePreProcessingTabEnum.ImageFiltering);
            }
        });
        mnImagePreparation.add(mntmImageFiltering);
        
        JMenuItem mntmImageRotation = new JMenuItem("Image rotation");
        mntmImageRotation.setEnabled(false);
        mnImagePreparation.add(mntmImageRotation);
        
        JMenuItem mntmImageOrthorectification = new JMenuItem("Image orthorectification");
        mntmImageOrthorectification.setEnabled(false);
        mnImagePreparation.add(mntmImageOrthorectification);
        
        JMenuItem mntmImagePositioning = new JMenuItem("Image positioning");
        mntmImagePositioning.setEnabled(false);
        mnImagePreparation.add(mntmImagePositioning);
        
        JMenuItem mntmImageCalibration = new JMenuItem("Image calibration");
        mntmImageCalibration.setEnabled(false);
        mnImagePreparation.add(mntmImageCalibration);
        
        JMenu mnPivConfiguration = new JMenu("PIV configuration");
        mnProject.add(mnPivConfiguration);
        
        JMenuItem mntmInterrogationArea = new JMenuItem("Interrogation Area");
        mntmInterrogationArea.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.InterrogationAreaTab);
            }
        });
        mnPivConfiguration.add(mntmInterrogationArea);
        
        JMenuItem mntmClippingMode = new JMenuItem("Clipping mode");
        mntmClippingMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.ClippingModeTab);
            }
        });
        mnPivConfiguration.add(mntmClippingMode);
        
        JMenuItem mntmVelocityInheritance = new JMenuItem("Velocity inheritance");
        mntmVelocityInheritance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.VelocityInheritanceTab);
            }
        });
        
        JMenuItem mntmWarpingMode = new JMenuItem("Warping mode");
        mntmWarpingMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.WarpingModeTab);
            }
        });
        mnPivConfiguration.add(mntmWarpingMode);
        mnPivConfiguration.add(mntmVelocityInheritance);
        
        JMenuItem mntmSuperposition = new JMenuItem("Superposition");
        mntmSuperposition.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.SuperpositionTab);
            }
        });
        mnPivConfiguration.add(mntmSuperposition);
        
        JMenuItem mntmSubpixelInterpolation = new JMenuItem("Sub-pixel interpolation");
        mntmSubpixelInterpolation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.SubPixelInterpolationTab);
            }
        });
        mnPivConfiguration.add(mntmSubpixelInterpolation);
        
        JMenuItem mntmVelocityStabilization = new JMenuItem("Velocity stabilization");
        mntmVelocityStabilization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.VelocityStabilizationTab);
            }
        });
        mnPivConfiguration.add(mntmVelocityStabilization);
        
        JMenuItem mntmValidation = new JMenuItem("Validation");
        mntmValidation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPIVConfigurationDialog(PIVConfigurationTabEnum.ValidationTab);
            }
        });
        mnPivConfiguration.add(mntmValidation);
        
        JMenuItem mntmStartPiv = new JMenuItem("Start PIV");
        mntmStartPiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executePIV();
            }
        });
        mnProject.add(mntmStartPiv);
        
        JMenu mnHelp = new JMenu("Help");
        mnHelp.setHorizontalAlignment(SwingConstants.LEFT);
        mnHelp.setMnemonic('H');
        menuBar.add(mnHelp);
        
        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showAboutDialog();
            }
        });
        mnHelp.add(mntmAbout);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        frmQuicklabPivng.getContentPane().add(scrollPane, BorderLayout.CENTER);
        panel = new ImagePanel();
        scrollPane.add(panel);
        panel.setFocusable(true);
        scrollPane.setViewportView(panel);
        initDataBindings();
    }
    
    protected void configDataExportEnv() {
        appContextModel = NavigationFacade.configDataExportEnv(frmQuicklabPivng, appContextModel);
    }

    protected void configProcessingEnv() {
        appContextModel = NavigationFacade.configProcessingEnv(frmQuicklabPivng, appContextModel);        
    }

    private void rebind() {
        imageBinding.unbind();
        imageBinding.setSourceObject(appContextModel);
        imageBinding.bind();
    }

    private void createNewProject() {
        appContextModel = NavigationFacade.createNewProject(appContextModel);
        if (appContextModel != null) {
            enableProjectMenus();
            rebind();
        }
    }

    protected void enableProjectMenus() {
        mnProject.setEnabled(true);
        mntmSaveProject.setEnabled(true);
        mntmSaveProjectAs.setEnabled(true);    
    }
    
    private void openProject() {
        AppContextModel loadedAppContextModel = NavigationFacade.loadProject(appContextModel);
        if (loadedAppContextModel != null && appContextModel != loadedAppContextModel) {
            appContextModel = loadedAppContextModel;
            enableProjectMenus();
            rebind();
        }
    }
    
    private void editPoject() {
        appContextModel = NavigationFacade.editProject(frmQuicklabPivng, appContextModel);
    }
    
    private void showPIVConfigurationDialog(PIVConfigurationTabEnum startTab) {
        appContextModel = NavigationFacade.showPIVConfiguration(frmQuicklabPivng, appContextModel, startTab);
    }
    
    private void showPIVImageSelectionDialog() {
        appContextModel = NavigationFacade.showPIVImageSelectionDialog(frmQuicklabPivng, appContextModel);
    }

    private void showImagePreProcessingDialog(PIVImagePreProcessingTabEnum startTab) {
        appContextModel = NavigationFacade.showImagePreProcessingDialog(frmQuicklabPivng, appContextModel, startTab);
    }
    
    private void showAboutDialog() {
        appContextModel = NavigationFacade.showAboutDialog(frmQuicklabPivng, appContextModel);
    }
    
    protected JMenu getMnProject() {
        return mnProject;
    }

    protected JMenuItem getMntmSaveProject() {
        return mntmSaveProject;
    }
    
    protected JMenuItem getMntmSaveProjectAs() {
        return mntmSaveProjectAs;
    }
    protected void initDataBindings() {
        BeanProperty<AppContextModel, BufferedImage> appContextModelBeanProperty = BeanProperty.create("selectedImagesModel.image");
        BeanProperty<ImagePanel, BufferedImage> imagePanelBeanProperty = BeanProperty.create("image");
        imageBinding = Bindings.createAutoBinding(UpdateStrategy.READ, appContextModel, appContextModelBeanProperty, panel, imagePanelBeanProperty, "imageBinding");
        imageBinding.bind();
    }
    
    private void executePIV() {
        //Check is project is saved...
        if (appContextModel.isPendingChanges()) {
            JOptionPane.showMessageDialog(frmQuicklabPivng, "Please save the project before executing the PIV Processing.\n" +
                    "The location and name of the saved project will be used to infer the\n" + 
                    "location and name of the PIV results file with the processed data.", "PIV processing", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!DataExportEnvFacade.validateDataExportConfiguration(appContextModel)) {
            JOptionPane.showMessageDialog(frmQuicklabPivng, "Please check the Data Export environment configuration.\n" +
                    "Cannot proceed. Inconsistent configuration was found for the data export environment.", "PIV processing", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            DataProcessingEnvFacade.validate(appContextModel);
        } catch (InvalidExecutionEnvException ex) {            
            ExecutionEnvModel execEnv = appContextModel.getExecutionEnvironment();
            execEnv = DataProcessingEnvFacade.getDeviceListAndCheckExecutionModelValidity(execEnv);
            appContextModel.setExecutionEnvironment(execEnv);
        }
        
        try {
            DataProcessingEnvFacade.validate(appContextModel);
        } catch (InvalidExecutionEnvException ex) {
            JOptionPane.showMessageDialog(frmQuicklabPivng, "Please check the Data Processing environment configuration.\n" +
                    "Cannot proceed. Inconsistent configuration was found for the execution environment.", "PIV processing", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!PIVConfigurationFacade.validateCoherencyWithDataProcessingEnv(appContextModel)) {
            JOptionPane.showMessageDialog(frmQuicklabPivng, "Please check the PIV configuration.\n" +
                    "Cannot proceed. Inconsistent configuration was found for the sub-pixel interpolation mode.", "PIV processing", JOptionPane.ERROR_MESSAGE);
            return;            
        }

        if (!PIVConfigurationFacade.validateExportFileStorageLimits(appContextModel)) {
            JOptionPane.showMessageDialog(frmQuicklabPivng, "Please check the Data export configuration.\n" +
                    "Current configuration will exceed export file storage limits of 4GB.\n" +
                    "Please change the export configuration to split export data into multiple volumes.\n" +
                    "The recommended maximum number of maps per exported volume file is: " + 
                    PIVConfigurationFacade.getMaximumNumberOfMapsPerVolume(appContextModel), "PIV processing", JOptionPane.ERROR_MESSAGE);
            return;            
        }
        
        //Convert parameters and Execute
        NavigationFacade.executeProject(frmQuicklabPivng, appContextModel);
    }
    
    private static void executeShellRunMode(Properties options) {
        if (options.containsKey(CommandLineOptionsEnum.PROJECT_FILE.key())) {
           File projectFile = new File(options.getProperty(CommandLineOptionsEnum.PROJECT_FILE.key()));
           if (!projectFile.isFile() || !projectFile.exists()) {
               throw new InvalidProjectFileException("Project file not found: " + projectFile.getAbsolutePath());
           }
           
           try {
               AppContextModel result = ProjectFacade.loadProject(projectFile);
               ExecuteLocalShellPIV executor = new ExecuteLocalShellPIV(result, options);
               executor.execute();              
           } catch (ProjectOpenException e) {
               throw new InvalidProjectFileException("Invalid project file: " + projectFile.getAbsolutePath(), e);
           }
        } else {
            System.out.println("Please specify a project file");
            CommandLineOptionsEnum.showShellModeOptionsHelper();           
        }
    }
}
