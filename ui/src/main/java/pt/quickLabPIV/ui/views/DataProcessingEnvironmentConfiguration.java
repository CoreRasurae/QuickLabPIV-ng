package pt.quickLabPIV.ui.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import pt.quickLabPIV.libs.external.ButtonColumn;
import pt.quickLabPIV.libs.external.DisabledPanel;
import pt.quickLabPIV.business.facade.AppContextFacade;
import pt.quickLabPIV.business.facade.PIVExecutionEnvironmentFacade;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.controllers.DataProcessingEnvFacade;
import pt.quickLabPIV.ui.converters.NullGenericIntegerConverter;
import pt.quickLabPIV.ui.models.AppContextModel;
import pt.quickLabPIV.ui.models.ExecutionEnvModel;
import pt.quickLabPIV.ui.models.OpenCLAssignmentsTableModel;
import pt.quickLabPIV.ui.models.OpenCLDeviceModel;
import pt.quickLabPIV.ui.models.OpenCLDeviceTableModel;

public class DataProcessingEnvironmentConfiguration extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessingEnvironmentConfiguration.class);
    private AutoBinding<AppContextModel, Boolean, JRadioButton, Boolean> openCLEnabledBinding;
    private AutoBinding<AppContextModel, Integer, JComboBox<Integer>, Object> cpuThreadsBinding;

    /**
     * 
     */
    private static final long serialVersionUID = -6956177228619933162L;
    
    Action testAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = -739326994619628777L;

        @Override
        public void actionPerformed(ActionEvent e) {
            JTable table = (JTable)e.getSource();
            int row = Integer.valueOf(e.getActionCommand());
            PIVExecutionEnvironmentFacade.testProfileGPU(table, row);
        }
        
    };

    private final JPanel contentPanel = new JPanel();
    private final ButtonGroup buttonGroupOpenCL = new ButtonGroup();
    private JTable tableOpenClAssignment;
    private OpenCLAssignmentsTableModel assignmentModel = new OpenCLAssignmentsTableModel();
    private AppContextModel appContextModel;
    private OpenCLDeviceTableModel model = new OpenCLDeviceTableModel(this);
    private JTable tableDevicesSel;
    private JComboBox<Integer> comboBox;
    private JRadioButton rdbtnEnable;
    private JRadioButton rdbtnDisabled;
    private ButtonColumn buttonColumn;
    private JPanel panelDeviceSelection;
    private boolean cancelled = false;
    private JPanel panelOpenClAssignment;

    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            DataProcessingEnvironmentConfiguration dialog = new DataProcessingEnvironmentConfiguration();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create the dialog.
     */
    public DataProcessingEnvironmentConfiguration() {
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("PIV Data Processing environment configuration");
        setBounds(100, 100, 450, 300);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{450, 0};
        gridBagLayout.rowHeights = new int[]{228, 35, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc_contentPanel = new GridBagConstraints();
        gbc_contentPanel.fill = GridBagConstraints.BOTH;
        gbc_contentPanel.insets = new Insets(0, 0, 5, 0);
        gbc_contentPanel.gridx = 0;
        gbc_contentPanel.gridy = 0;
        getContentPane().add(contentPanel, gbc_contentPanel);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{159, 0};
        gbl_contentPanel.rowHeights = new int[]{22, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
            gbc_tabbedPane.fill = GridBagConstraints.BOTH;
            gbc_tabbedPane.gridx = 0;
            gbc_tabbedPane.gridy = 0;
            contentPanel.add(tabbedPane, gbc_tabbedPane);
            {
                JPanel panelOpenCL1 = new JPanel();
                panelOpenCL1.setToolTipText("");
                tabbedPane.addTab("Configuration page 1", null, panelOpenCL1, null);
                GridBagLayout gbl_panelOpenCL1 = new GridBagLayout();
                gbl_panelOpenCL1.columnWidths = new int[]{322, 0};
                gbl_panelOpenCL1.rowHeights = new int[]{45, 23, 0};
                gbl_panelOpenCL1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelOpenCL1.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                panelOpenCL1.setLayout(gbl_panelOpenCL1);
                {
                    JPanel panelGenericConfigs = new JPanel();
                    GridBagConstraints gbc_panelGenericConfigs = new GridBagConstraints();
                    gbc_panelGenericConfigs.fill = GridBagConstraints.HORIZONTAL;
                    gbc_panelGenericConfigs.anchor = GridBagConstraints.NORTH;
                    gbc_panelGenericConfigs.insets = new Insets(0, 0, 5, 0);
                    gbc_panelGenericConfigs.gridx = 0;
                    gbc_panelGenericConfigs.gridy = 0;
                    panelOpenCL1.add(panelGenericConfigs, gbc_panelGenericConfigs);
                    panelGenericConfigs.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Generic configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                    GridBagLayout gbl_panelGenericConfigs = new GridBagLayout();
                    gbl_panelGenericConfigs.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
                    gbl_panelGenericConfigs.rowHeights = new int[]{0, 0, 0};
                    gbl_panelGenericConfigs.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
                    gbl_panelGenericConfigs.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
                    panelGenericConfigs.setLayout(gbl_panelGenericConfigs);
                    {
                        comboBox = new JComboBox<>(DataProcessingEnvFacade.getCpuCoresComboBoxModel());
                        GridBagConstraints gbc_comboBox = new GridBagConstraints();
                        gbc_comboBox.gridwidth = 3;
                        gbc_comboBox.insets = new Insets(0, 0, 5, 5);
                        gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
                        gbc_comboBox.gridx = 2;
                        gbc_comboBox.gridy = 0;
                        panelGenericConfigs.add(comboBox, gbc_comboBox);
                    }
                    {
                        rdbtnDisabled = new JRadioButton("Disabled");
                        rdbtnDisabled.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                updateOpenCLEnabledPanelState();
                            }
                        });
                        buttonGroupOpenCL.add(rdbtnDisabled);
                        GridBagConstraints gbc_rdbtnDisabled = new GridBagConstraints();
                        gbc_rdbtnDisabled.insets = new Insets(0, 0, 0, 5);
                        gbc_rdbtnDisabled.gridx = 2;
                        gbc_rdbtnDisabled.gridy = 1;
                        panelGenericConfigs.add(rdbtnDisabled, gbc_rdbtnDisabled);
                    }
                    {
                        rdbtnEnable = new JRadioButton("Enabled");
                        rdbtnEnable.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                updateOpenCLEnabledPanelState();
                            }
                        });
                        buttonGroupOpenCL.add(rdbtnEnable);
                        GridBagConstraints gbc_rdbtnEnable = new GridBagConstraints();
                        gbc_rdbtnEnable.insets = new Insets(0, 0, 0, 5);
                        gbc_rdbtnEnable.gridx = 3;
                        gbc_rdbtnEnable.gridy = 1;
                        panelGenericConfigs.add(rdbtnEnable, gbc_rdbtnEnable);
                    }
                    {
                        JLabel lblOpenclEnabled = new JLabel("OpenCL usage");
                        GridBagConstraints gbc_lblOpenclEnabled = new GridBagConstraints();
                        gbc_lblOpenclEnabled.anchor = GridBagConstraints.WEST;
                        gbc_lblOpenclEnabled.insets = new Insets(0, 0, 0, 5);
                        gbc_lblOpenclEnabled.gridx = 0;
                        gbc_lblOpenclEnabled.gridy = 1;
                        panelGenericConfigs.add(lblOpenclEnabled, gbc_lblOpenclEnabled);
                    }
                    {
                        JLabel lblNumberOfCpu = new JLabel("Number of CPU threads");
                        GridBagConstraints gbc_lblNumberOfCpu = new GridBagConstraints();
                        gbc_lblNumberOfCpu.insets = new Insets(0, 0, 5, 5);
                        gbc_lblNumberOfCpu.gridx = 0;
                        gbc_lblNumberOfCpu.gridy = 0;
                        panelGenericConfigs.add(lblNumberOfCpu, gbc_lblNumberOfCpu);
                    }
                }
                {
                    panelDeviceSelection = new JPanel();
                    GridBagConstraints gbc_panelDeviceSelection = new GridBagConstraints();
                    gbc_panelDeviceSelection.fill = GridBagConstraints.BOTH;
                    gbc_panelDeviceSelection.gridx = 0;
                    gbc_panelDeviceSelection.gridy = 1;
                    panelOpenCL1.add(panelDeviceSelection, gbc_panelDeviceSelection);
                    panelDeviceSelection.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "OpenCL device selection", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
                    GridBagLayout gbl_panelDeviceSelection = new GridBagLayout();
                    gbl_panelDeviceSelection.columnWidths = new int[]{220, 0};
                    gbl_panelDeviceSelection.rowHeights = new int[]{1, 0};
                    gbl_panelDeviceSelection.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                    gbl_panelDeviceSelection.rowWeights = new double[]{1.0, Double.MIN_VALUE};
                    panelDeviceSelection.setLayout(gbl_panelDeviceSelection);
                    {
                        JScrollPane scrollPaneDeviceSelection = new JScrollPane();
                        GridBagConstraints gbc_scrollPaneDeviceSelection = new GridBagConstraints();
                        gbc_scrollPaneDeviceSelection.fill = GridBagConstraints.BOTH;
                        gbc_scrollPaneDeviceSelection.gridx = 0;
                        gbc_scrollPaneDeviceSelection.gridy = 0;
                        panelDeviceSelection.add(scrollPaneDeviceSelection, gbc_scrollPaneDeviceSelection);
                        {
                            tableDevicesSel = new JTable() {
                                @Override
                                public String getToolTipText(MouseEvent e) {
                                    String tip = null;
                                    Point p = e.getPoint();
                                    int rowIndex = rowAtPoint(p);
                                    int colIndex = columnAtPoint(p);
                                    String text = model.getToolTipText(rowIndex, colIndex);
                                    if (!tableDevicesSel.isEnabled()) {
                                        return "Panel disabled since no OpenCL devices are selected/enabled";
                                    } else {
                                        return text;
                                    }
                                    
                                }
                            };
                            tableDevicesSel.setRowSelectionAllowed(false);
                            scrollPaneDeviceSelection.setViewportView(tableDevicesSel);
                            tableDevicesSel.setShowGrid(true);
                            tableDevicesSel.setAutoscrolls(true);
                            tableDevicesSel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                            tableDevicesSel.setDefaultRenderer(Object.class, new OpenCLDeviceTableCellRenderer());
                            tableDevicesSel.setShowHorizontalLines(true);
                            tableDevicesSel.setShowVerticalLines(true);
                        }
                    }
                }
            }
            {
                JPanel panelOpenCL2 = new JPanel();
                panelOpenCL2.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentShown(ComponentEvent evt) {
                        updateTableOpenClAssignment();
                    }
                });
                tabbedPane.addTab("Configuration Page 2", null, panelOpenCL2, null);
                GridBagLayout gbl_panelOpenCL2 = new GridBagLayout();
                gbl_panelOpenCL2.columnWidths = new int[]{0, 0};
                gbl_panelOpenCL2.rowHeights = new int[]{0, 0};
                gbl_panelOpenCL2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelOpenCL2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
                panelOpenCL2.setLayout(gbl_panelOpenCL2);
                {
                    panelOpenClAssignment = new JPanel();
                    panelOpenClAssignment.setBorder(new TitledBorder(new LineBorder(new Color(64, 64, 64), 1, true), "Assignment of OpenCL devices to CPU Java threads", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
                    GridBagConstraints gbc_panelOpenClAssignment = new GridBagConstraints();
                    gbc_panelOpenClAssignment.fill = GridBagConstraints.BOTH;
                    gbc_panelOpenClAssignment.gridx = 0;
                    gbc_panelOpenClAssignment.gridy = 0;
                    panelOpenCL2.add(panelOpenClAssignment, gbc_panelOpenClAssignment);
                    GridBagLayout gbl_panelOpenClAssignment = new GridBagLayout();
                    gbl_panelOpenClAssignment.columnWidths = new int[]{0, 0};
                    gbl_panelOpenClAssignment.rowHeights = new int[]{0, 0};
                    gbl_panelOpenClAssignment.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                    gbl_panelOpenClAssignment.rowWeights = new double[]{1.0, Double.MIN_VALUE};
                    panelOpenClAssignment.setLayout(gbl_panelOpenClAssignment);
                    {
                        JScrollPane scrollPanelOpenClAssignment = new JScrollPane();
                        GridBagConstraints gbc_scrollPanelOpenClAssignment = new GridBagConstraints();
                        gbc_scrollPanelOpenClAssignment.fill = GridBagConstraints.BOTH;
                        gbc_scrollPanelOpenClAssignment.gridx = 0;
                        gbc_scrollPanelOpenClAssignment.gridy = 0;
                        panelOpenClAssignment.add(scrollPanelOpenClAssignment, gbc_scrollPanelOpenClAssignment);
                        {
                            tableOpenClAssignment = new JTable();
                            tableOpenClAssignment.setRowSelectionAllowed(false);
                            tableOpenClAssignment.setDefaultRenderer(String.class, new OpenCLAssignmentTableCellRenderer());
                            scrollPanelOpenClAssignment.setViewportView(tableOpenClAssignment);
                        }
                    }
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            GridBagConstraints gbc_buttonPane = new GridBagConstraints();
            gbc_buttonPane.insets = new Insets(0, 0, 5, 0);
            gbc_buttonPane.anchor = GridBagConstraints.NORTH;
            gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
            gbc_buttonPane.gridx = 0;
            gbc_buttonPane.gridy = 1;
            getContentPane().add(buttonPane, gbc_buttonPane);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        validateAndClose();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        cancelAndClose();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        initDataBindings();
    }
    
    protected void updateTableOpenClAssignment() {
        //Update configurations based on imported configuration and user updates 
        List<OpenCLDeviceModel> openClModels = model.getModels();
        List<OpenCLDeviceModel> selectedOpenClModels = Collections.emptyList();
        List<Integer> selectedOpenClIndices = Collections.emptyList();
        if (openClModels != null) {
            selectedOpenClModels = new ArrayList<>(openClModels.size());
            selectedOpenClIndices = new ArrayList<>(openClModels.size());
            for (int idx = 0; idx < openClModels.size(); idx++) {
                if (openClModels.get(idx).isSelected() && openClModels.get(idx).getPerformanceScore() > 0.0f) {
                    selectedOpenClModels.add(openClModels.get(idx));
                    selectedOpenClIndices.add(idx + 1);
                }
            }
        }
        
        if (selectedOpenClModels.size() > 0 && rdbtnEnable.isSelected()) {
           panelOpenClAssignment.setToolTipText(null);
           panelOpenClAssignment.setEnabled(true);
           assignmentModel.updateSelectedModels(selectedOpenClModels, selectedOpenClIndices);
           //
           if (appContextModel.getExecutionEnvironment().getOpenClAssignments().size() == 0 || 
               appContextModel.getExecutionEnvironment().getValidAssignments().stream().allMatch(valid -> valid == false)) {
               int nrOfCpus = appContextModel.getExecutionEnvironment().getCpuThreads();
               List<Long> openClAssignments = new ArrayList<>(nrOfCpus);
               List<Boolean> validAssignments = new ArrayList<>(nrOfCpus);
               int openClsPerCPU = nrOfCpus / selectedOpenClModels.size();
               if (openClsPerCPU == 0) {
                   openClsPerCPU = 1;
               }
               //
               for (int idx = 0, openClIdx = 0, usageCount = 0; idx < nrOfCpus; idx++) {
                   openClAssignments.add(selectedOpenClModels.get(openClIdx).getId());
                   validAssignments.add(true);
                   usageCount++;
                   if (usageCount == openClsPerCPU) {
                       usageCount = 0;
                       openClIdx++;
                   }              
               }           
               assignmentModel.updateCpuAssignments(openClAssignments, validAssignments);
           } else {
               int nrCpuThreads = appContextModel.getExecutionEnvironment().getCpuThreads();
               List<Long> openClAssignments = appContextModel.getExecutionEnvironment().getOpenClAssignments();
               List<Boolean> validAssignments = appContextModel.getExecutionEnvironment().getValidAssignments();
               int currentAssignmentsSize = openClAssignments.size();

               //First invalidate all assignments that refer to no longer selected devices
               for (int i = 0; i < openClAssignments.size(); i++) {
            	   if (!validAssignments.get(i)) {
            		   continue;
            	   }
            	   
            	   boolean found = false;
            	   for (OpenCLDeviceModel model : selectedOpenClModels) {
            		   if (model.getId() == openClAssignments.get(i)) {
            		       found = true;
            		   }
            	   }
            	   
            	   if (!found) {
            	       validAssignments.set(i, false);
            	   }
               }
               
               //Adjust assignments for different thread sizes
               if (nrCpuThreads < currentAssignmentsSize) {
                   openClAssignments = openClAssignments.subList(0, nrCpuThreads);
                   validAssignments = validAssignments.subList(0, nrCpuThreads);
               } else if (nrCpuThreads > currentAssignmentsSize) {
                   for (int i = 0; i < nrCpuThreads - currentAssignmentsSize; i++) {
                       openClAssignments.add(0L);
                       validAssignments.add(false);
                   }
               }
               
               assignmentModel.updateCpuAssignments(openClAssignments, validAssignments);
           }

           JComboBox<String> cBox = assignmentModel.getComboBoxWithOptions();
           TableColumn column = tableOpenClAssignment.getColumnModel().getColumn(1);
           column.setCellEditor(new DefaultCellEditor(cBox));
        } else {
           panelOpenClAssignment.setToolTipText("Panel disabled since no OpenCL devices are selected/enabled");
           panelOpenClAssignment.setEnabled(false); 
        }
        
    }

    protected void updateOpenCLEnabledPanelState() {
        if (rdbtnEnable.isSelected()) {
            DisabledPanel.enable(panelDeviceSelection);
            //Let's prepare the page 2 separator tab, if there are already device assignments available,
            //this way the user does not need to explicitly open the page 2 separator tab to pass validation,
            //if no changes are made to the configuration and only OpenCL was re-enabled.
            if (appContextModel.getExecutionEnvironment().getOpenClAssignments().size() > 0) {
                updateTableOpenClAssignment();
            }
        } else {
            DisabledPanel.disable(panelDeviceSelection);
        }
    }

    static class OpenCLDeviceTableCellRenderer extends DefaultTableCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = -4653890255744110323L;
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            OpenCLDeviceTableModel model = (OpenCLDeviceTableModel)table.getModel();
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);            
            if (!table.isEnabled()) {
                c.setBackground(Color.GRAY);
                c.setForeground(Color.DARK_GRAY);
            } else {
                if (!isSelected) {
                    c.setBackground(model.getRowBackgroundColor(row));
                    c.setForeground(model.getRowForegroundColor(row));
                } else {
                    c.setForeground(model.getRowBackgroundColor(row));
                }
            }            

            return c;
        }
    }
    
    static class OpenCLAssignmentTableCellRenderer extends DefaultTableCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = 7858684540531295954L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            OpenCLAssignmentsTableModel model = (OpenCLAssignmentsTableModel)table.getModel();
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);            
            if (!table.isEnabled()) {
                c.setBackground(Color.GRAY);
                c.setForeground(Color.DARK_GRAY);
            } else {
                c.setForeground(model.getRowForegroundColor(row));
                c.setBackground(model.getRowBackgroundColor(row));
            }            

            return c;
        }
    }
    
    public void setAppContext(AppContextModel ctx) {
        appContextModel = ctx;
        ExecutionEnvModel execModel = AppContextFacade.getOrCreateDefaultExecutionEnvModel(appContextModel);
        execModel = DataProcessingEnvFacade.getDeviceListAndCheckExecutionModelValidity(execModel);        
        model.setModels(execModel.getOpenClDevices());
        tableDevicesSel.setModel(model);
        resizeTableColumnWidth(tableDevicesSel);
        tableOpenClAssignment.setModel(assignmentModel);
        //
        cpuThreadsBinding.unbind();
        cpuThreadsBinding.setSourceObject(appContextModel);
        cpuThreadsBinding.bind();
        //
        openCLEnabledBinding.unbind();
        openCLEnabledBinding.setSourceObject(appContextModel);
        openCLEnabledBinding.bind();

        //Ensure that at least one radio button is always selected. 
        updateOpenCLEnabledPanelState();
        if (!execModel.isEnableOpenCL()) {
            rdbtnDisabled.setSelected(true);
        }
       
        //Let's prepare the page 2 separator tab, if there are already device assignments available,
        //this way the user does not need to explicitly open the page 2 separator tab to pass validation,
        //if no changes are made to the configuration.
        if (execModel.getOpenClAssignments().size() > 0) {
            updateTableOpenClAssignment();
        }
        
        buttonColumn = new ButtonColumn(tableDevicesSel, testAction, 10);
     }
    
    private void resizeTableColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();

        int minWidth = 15;  //Minimum width
        int maxWidth = 350;
        int totalWidth = 0;
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = minWidth;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);                
            }
            TableCellRenderer renderer = columnModel.getColumn(column).getCellRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, columnModel.getColumn(column).getHeaderValue(), false, false, -1, column);
            width = Math.max(comp.getPreferredSize().width + 1, width);
            if (width > maxWidth) {
                width = maxWidth;
            }
            totalWidth += width;
            columnModel.getColumn(column).setPreferredWidth(width);
        }

        int height = table.getTableHeader().getPreferredSize().height;
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableCellRenderer renderer = columnModel.getColumn(column).getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, columnModel.getColumn(column).getHeaderValue(), false, false, -1, column);
            height = Math.max(comp.getPreferredSize().height + 1, height);
        }
        table.getTableHeader().setPreferredSize(new Dimension(totalWidth, height));
        table.getParent().setPreferredSize(new Dimension(totalWidth, height));
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public AppContextModel getAppContext() {
        return appContextModel;
    }
    
    protected void initDataBindings() {
        BeanProperty<AppContextModel, Integer> appContextModelBeanProperty = BeanProperty.create("executionEnvironment.cpuThreads");
        BeanProperty<JComboBox<Integer>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
        cpuThreadsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContextModel, appContextModelBeanProperty, comboBox, jComboBoxBeanProperty, "cpuThreadsBinding");
        cpuThreadsBinding.setConverter(new NullGenericIntegerConverter());
        cpuThreadsBinding.bind();
        //
        BeanProperty<AppContextModel, Boolean> appContextModelBeanProperty_1 = BeanProperty.create("executionEnvironment.enableOpenCL");
        BeanProperty<JRadioButton, Boolean> jRadioButtonBeanProperty = BeanProperty.create("selected");
        openCLEnabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, appContextModel, appContextModelBeanProperty_1, rdbtnEnable, jRadioButtonBeanProperty, "openCLEnabledBinding");
        openCLEnabledBinding.bind();
    }
    
    protected JPanel getPanelDeviceSelection() {
        return panelDeviceSelection;
    }
    
    protected JPanel getPanelOpenClAssignment() {
        return panelOpenClAssignment;
    }
    
    protected void cancelAndClose() {
        try {
            cancelled = true;
            setVisible(false);
        } finally {
            if (model != null) {
                model.unregisterListeners();
            }
        }
    }

    private boolean validateData() {
        List<Long> cpuAssignments = assignmentModel.getOpenClAssignments();
        List<Boolean> validAssignments = assignmentModel.getValidAssignments();
        appContextModel.getExecutionEnvironment().setOpenClAssignments(cpuAssignments, validAssignments);
        
        DataProcessingEnvFacade.validate(appContextModel);
        
        return true;
    }
    
    protected void validateAndClose() {
        try {
            if (validateData()) {
                try {
                    cancelled = false;
                    setVisible(false);
                } finally {
                    if (model != null) {
                        model.unregisterListeners();
                    }
                }
            }
        } catch (UIException ex) {
            JOptionPane.showMessageDialog(this, "Please re-check the processing environemnt configuration\n" +
                    ex.getTitleMessage(), "Data Processing environment configuration inconsistency", JOptionPane.ERROR_MESSAGE);
            logger.warn(ex.getTitleMessage(), ex);
        }
    }

}
