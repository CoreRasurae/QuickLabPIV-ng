package pt.quickLabPIV.ui.views.panels;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class RemoteProjectPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 4854665608617311509L;
    private JTextField remoteTextField;

    /**
     * Create the panel.
     */
    public RemoteProjectPanel() {
        setBorder(new EmptyBorder(5, 2, 2, 5));
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {90, 200};
        gridBagLayout.rowHeights = new int[] {27, 28};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0};
        setLayout(gridBagLayout);
        
        JLabel lblRemoteVipivistngServer = new JLabel("Remote server");
        GridBagConstraints gbc_lblRemoteVipivistngServer = new GridBagConstraints();
        gbc_lblRemoteVipivistngServer.insets = new Insets(0, 0, 5, 5);
        gbc_lblRemoteVipivistngServer.anchor = GridBagConstraints.NORTHWEST;
        gbc_lblRemoteVipivistngServer.gridx = 0;
        gbc_lblRemoteVipivistngServer.gridy = 0;
        add(lblRemoteVipivistngServer, gbc_lblRemoteVipivistngServer);
        
        remoteTextField = new JTextField();
        lblRemoteVipivistngServer.setLabelFor(remoteTextField);
        GridBagConstraints gbc_remoteTextField = new GridBagConstraints();
        gbc_remoteTextField.insets = new Insets(0, 0, 5, 0);
        gbc_remoteTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_remoteTextField.gridx = 1;
        gbc_remoteTextField.gridy = 0;
        add(remoteTextField, gbc_remoteTextField);
        remoteTextField.setColumns(10);
        
        JButton btnConnect = new JButton("Connect");
        GridBagConstraints gbc_btnConnect = new GridBagConstraints();
        gbc_btnConnect.anchor = GridBagConstraints.EAST;
        gbc_btnConnect.gridx = 1;
        gbc_btnConnect.gridy = 1;
        add(btnConnect, gbc_btnConnect);

    }

    protected JTextField getRemoteTextField() {
        return remoteTextField;
    }
    
    public void setRemoteAddress(String remoteServerAddress) {
        remoteTextField.setText(remoteServerAddress);
    }
    
    public String getRemoteAddress() {
        return remoteTextField.getText();
    }
}
