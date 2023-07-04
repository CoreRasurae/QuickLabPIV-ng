package pt.quickLabPIV.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;

import pt.quickLabPIV.ProgressReport;
import pt.quickLabPIV.business.transfer.ExecuteLocalPIVWorker;
import pt.quickLabPIV.business.transfer.LocalPIVExecutionMonitorWorker;
import pt.quickLabPIV.business.transfer.PIVCompletionStatus;
import pt.quickLabPIV.exceptions.UIException;
import pt.quickLabPIV.ui.controllers.Utils;

public class ExecutionProgressDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = -7394039415032868448L;
    private final JPanel contentPanel = new JPanel();
    private JProgressBar progressBar;
    private JLabel lblAvgTimePerImage;
    private JLabel lblElapsedTime;
    private JLabel lblRemainingTime;
    private JLabel lblExecutionDetails;
    private LocalPIVExecutionMonitorWorker monitorWorker = null;
    private JButton cancelButton;
    private boolean finished = false;
    private JLabel lblDestination;
    private String outputPathAndFilename = "";
    private ProgressPainter painter;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ExecutionProgressDialog dialog = new ExecutionProgressDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ProgressPainter implements Painter<JProgressBar> {

        private Color light, dark;
        private GradientPaint gradPaint;

        public ProgressPainter(Color light, Color dark) {
            this.light = light;
            this.dark = dark;
        }
        
        public void changeColors(Color _light, Color _dark) {
            light = _light;
            dark = _dark;
            revalidate();
            repaint();
        }

        @Override
        public void paint(Graphics2D g, JProgressBar c, int w, int h) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gradPaint = new GradientPaint((w / 2.0f), 0, light, (w / 2.0f), (h / 2.0f), dark, true);
            g.setPaint(gradPaint);
            g.fillRect(2, 2, (w - 5), (h - 5));

            Color outline = new Color(0, 85, 0);
            g.setColor(outline);
            g.drawRect(2, 2, (w - 5), (h - 5));
            Color trans = new Color(outline.getRed(), outline.getGreen(), outline.getBlue(), 100);
            g.setColor(trans);
            g.drawRect(1, 1, (w - 3), (h - 3));
        }
    }    
    /**
     * Create the dialog.
     */
    public ExecutionProgressDialog(Frame parent) {
    	super(parent);
    	setModal(true);
    	setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 480, 251);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 49, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JLabel lblExecutionProgress = new JLabel("Execution progress");
            GridBagConstraints gbc_lblExecutionProgress = new GridBagConstraints();
            gbc_lblExecutionProgress.insets = new Insets(0, 0, 5, 0);
            gbc_lblExecutionProgress.gridx = 0;
            gbc_lblExecutionProgress.gridy = 1;
            contentPanel.add(lblExecutionProgress, gbc_lblExecutionProgress);
        }
        {
            lblExecutionDetails = new JLabel("0/??? images");
            GridBagConstraints gbc_lblExecutionDetails = new GridBagConstraints();
            gbc_lblExecutionDetails.insets = new Insets(0, 0, 5, 0);
            gbc_lblExecutionDetails.gridx = 0;
            gbc_lblExecutionDetails.gridy = 2;
            contentPanel.add(lblExecutionDetails, gbc_lblExecutionDetails);
        }
        {
            lblDestination = new JLabel("No output file specified");
            GridBagConstraints gbc_lblDestination = new GridBagConstraints();
            gbc_lblDestination.insets = new Insets(0, 0, 5, 0);
            gbc_lblDestination.gridx = 0;
            gbc_lblDestination.gridy = 3;
            contentPanel.add(lblDestination, gbc_lblDestination);
        }
        {
            progressBar = new JProgressBar();
            progressBar.setStringPainted(true);
            GridBagConstraints gbc_progressBar = new GridBagConstraints();
            gbc_progressBar.insets = new Insets(0, 0, 5, 0);
            gbc_progressBar.fill = GridBagConstraints.BOTH;
            gbc_progressBar.gridx = 0;
            gbc_progressBar.gridy = 4;
            contentPanel.add(progressBar, gbc_progressBar);
        }
        {
            JPanel panel = new JPanel();
            GridBagConstraints gbc_panel = new GridBagConstraints();
            gbc_panel.fill = GridBagConstraints.BOTH;
            gbc_panel.gridx = 0;
            gbc_panel.gridy = 5;
            contentPanel.add(panel, gbc_panel);
            GridBagLayout gbl_panel = new GridBagLayout();
            gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
            gbl_panel.rowHeights = new int[]{0, 31, 0};
            gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
            gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
            panel.setLayout(gbl_panel);
            {
                JLabel lblAvgTimePerImgTitle = new JLabel("Avg. time per Image");
                GridBagConstraints gbc_lblAvgTimePerImgTitle = new GridBagConstraints();
                gbc_lblAvgTimePerImgTitle.insets = new Insets(0, 0, 5, 5);
                gbc_lblAvgTimePerImgTitle.gridx = 0;
                gbc_lblAvgTimePerImgTitle.gridy = 0;
                panel.add(lblAvgTimePerImgTitle, gbc_lblAvgTimePerImgTitle);
            }
            {
                JLabel lblElapsedTimeTitle = new JLabel("Elapsed time");
                GridBagConstraints gbc_lblElapsedTimeTitle = new GridBagConstraints();
                gbc_lblElapsedTimeTitle.insets = new Insets(0, 0, 5, 5);
                gbc_lblElapsedTimeTitle.gridx = 2;
                gbc_lblElapsedTimeTitle.gridy = 0;
                panel.add(lblElapsedTimeTitle, gbc_lblElapsedTimeTitle);
            }
            {
                JLabel lblEstimatedRemainingTitle = new JLabel("Estimated remaining");
                GridBagConstraints gbc_lblEstimatedRemainingTitle = new GridBagConstraints();
                gbc_lblEstimatedRemainingTitle.insets = new Insets(0, 0, 5, 0);
                gbc_lblEstimatedRemainingTitle.gridx = 4;
                gbc_lblEstimatedRemainingTitle.gridy = 0;
                panel.add(lblEstimatedRemainingTitle, gbc_lblEstimatedRemainingTitle);
            }
            {
                lblAvgTimePerImage = new JLabel("Unknwon");
                GridBagConstraints gbc_lblAvgTimePerImage = new GridBagConstraints();
                gbc_lblAvgTimePerImage.insets = new Insets(0, 0, 0, 5);
                gbc_lblAvgTimePerImage.gridx = 0;
                gbc_lblAvgTimePerImage.gridy = 1;
                panel.add(lblAvgTimePerImage, gbc_lblAvgTimePerImage);
            }
            {
                lblElapsedTime = new JLabel("Unknown");
                GridBagConstraints gbc_lblElapsedTime = new GridBagConstraints();
                gbc_lblElapsedTime.insets = new Insets(0, 0, 0, 5);
                gbc_lblElapsedTime.gridx = 2;
                gbc_lblElapsedTime.gridy = 1;
                panel.add(lblElapsedTime, gbc_lblElapsedTime);
            }
            {
                lblRemainingTime = new JLabel("Unknown");
                GridBagConstraints gbc_lblRemainingTime = new GridBagConstraints();
                gbc_lblRemainingTime.gridx = 4;
                gbc_lblRemainingTime.gridy = 1;
                panel.add(lblRemainingTime, gbc_lblRemainingTime);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton disconnetButton = new JButton("Disconnect");
                disconnetButton.setEnabled(false);
                disconnetButton.setActionCommand("OK");
                buttonPane.add(disconnetButton);
                getRootPane().setDefaultButton(disconnetButton);
            }
            {
                cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        closeOrCancelPressed();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        progressBar.setMinimum(0);
        progressBar.getUI();
        UIDefaults defaults = new UIDefaults();
        painter = new ProgressPainter(Color.WHITE, Color.ORANGE);
        defaults.put("ProgressBar[Enabled].foregroundPainter", painter);
        defaults.put("ProgressBar[Enabled+Finished].foregroundPainter", painter);

        progressBar.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
        progressBar.putClientProperty("Nimbus.Overrides", defaults);        
        WindowListener wndCloser = new WindowAdapter() {            
            public void windowClosing(WindowEvent e) {
                
            }
        };
        addWindowListener(wndCloser);
    }

    protected JProgressBar getProgressBar() {
        return progressBar;
    }
    
    protected JLabel getLblAvgTimePerImage() {
        return lblAvgTimePerImage;
    }
    
    protected JLabel getLblElapsedTime() {
        return lblElapsedTime;
    }
    
    protected JLabel getLblRemainingTime() {
        return lblRemainingTime;
    }
    
    protected JLabel getLblExecutionDetails() {
        return lblExecutionDetails;
    }
    
    public void updateWithReport(ProgressReport report) {
        progressBar.setMaximum(report.getTotalImages());
        progressBar.setValue(report.getProcessedImages());
        updateOutputFileIfNeeded(report.getResultOutputPathAndFilename());
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(getLocale());
        dfs.setInfinity("Unknown");
        dfs.setNaN("Unknown");
        NumberFormat timeFormat = new DecimalFormat("######0.000 s", dfs);
        timeFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        timeFormat.setMaximumFractionDigits(3);
        timeFormat.setMinimumFractionDigits(3);
        timeFormat.setMinimumIntegerDigits(1);
        timeFormat.setMaximumIntegerDigits(7);
        lblAvgTimePerImage.setText(timeFormat.format(report.getTimePerImage()));
        lblRemainingTime.setText(timeFormat.format(report.getRemainingTime()));
        lblElapsedTime.setText(timeFormat.format(report.getElapsedTime()));
        lblExecutionDetails.setText(String.valueOf(report.getProcessedImages()) + "/" + String.valueOf(report.getTotalImages()));
    }

	private void updateOutputFileIfNeeded(String newOutputPathAndFilename) {
	    if (!outputPathAndFilename.equals(newOutputPathAndFilename)) {
	        lblDestination.setText("Output file: " + newOutputPathAndFilename);
	        outputPathAndFilename  = newOutputPathAndFilename;
	        invalidate();
	    }
    }

    public void startTrackingThread(ExecuteLocalPIVWorker localWorker) {
	    monitorWorker = new LocalPIVExecutionMonitorWorker(this, localWorker);
	    monitorWorker.execute();
	}
	
	private void closeOrCancelPressed() {
	    if (!finished) {
	        int status = Utils.showOptionDialog(this, "PIV Processing cancellation confirmation", 
	                "All data will be lost. Do you wish to cancel the PIV processing?");
	        if (status == JOptionPane.YES_OPTION) {
	            monitorWorker.requestCancellation();
	        } else {
	            return;
	        }
	    }
	    
        try {
            monitorWorker.get();
        } catch (InterruptedException e) {
            //Ignored on purpose
        } catch (ExecutionException e) {               
            //Ignored on purpose
        }
        
        setVisible(false);
	}

    public void updateWithCompletionStatus(PIVCompletionStatus completionStatus) {
        if (completionStatus.getCompleted()) {
            lblExecutionDetails.setText("Processing complete (" + progressBar.getMaximum() + " images), results exported.");
            painter.changeColors(Color.WHITE, Color.GREEN);
        } else {            
            lblExecutionDetails.setText("Processing failed after image: " + progressBar.getValue());
            UIException ex = completionStatus.getException();
            JOptionPane.showMessageDialog(this, ex.getMessage(), ex.getTitleMessage(), JOptionPane.ERROR_MESSAGE);
            painter.changeColors(Color.WHITE, Color.RED);
        }
        
        cancelButton.setText("Close");
        finished = true;
    }
    protected JButton getCancelButton() {
        return cancelButton;
    }
    protected JLabel getLblDestination() {
        return lblDestination;
    }
}
