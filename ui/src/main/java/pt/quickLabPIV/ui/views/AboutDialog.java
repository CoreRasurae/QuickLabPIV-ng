package pt.quickLabPIV.ui.views;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.quickLabPIV.release.ReleaseInfo;

public class AboutDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 8522755601133373163L;
    private static Logger logger = LoggerFactory.getLogger(AboutDialog.class);

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    AboutDialog dialog = new AboutDialog();
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    
    private Path getImage() {
        URI jarPath = null;
        try {
            jarPath = getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
        } catch (URISyntaxException e1) {
            return null;
        }
        
        try {            
            FileSystem fs = null;
            logger.info(jarPath.getPath());
            String path = "";
            if (jarPath.getPath().endsWith(".jar")) {
                logger.info("Trying to retrieve image from JAR file...");
                URI uri = URI.create("jar:file:" + jarPath.getRawPath());
                path = "/resources/QuickLabPIVng.png";
                try {
                    fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException e) {
                    fs = FileSystems.getFileSystem(uri);
                }
            } else {
                logger.info("Trying to retrieve image from non-JAR file...");
                path = jarPath.getPath() + "/../resources/QuickLabPIVng.png";
                fs = FileSystems.getDefault();
            }

            List<Path> collect  = Files.walk(fs.getPath(path))
                                  .filter(Files::isRegularFile)
                                  .collect(Collectors.toList());
       
            for (Path file : collect)
                logger.info("Found file {}", file.toString());
            if (collect.size() == 0) {
                return null;
            }
            logger.info("Found {} images.", collect.size());
            return collect.get(0);

        } catch (IOException e) {
            return null;
        }
    }
    /**
     * Create the dialog.
     */
    public AboutDialog() {
        setType(Type.UTILITY);
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModal(true);
        setTitle("About QuickLab PIV-ng");
        setBounds(100, 100, 450, 400);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 62, 0, 0};
        gridBagLayout.rowHeights = new int[]{30, 0, 75, 0, 0, 0, 0, 0, 33, 14, 30};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);

        BufferedImage backgroundImage = null;
        try {
            Path img = getImage();
            if (img != null) {
                backgroundImage = ImageIO.read(img.toUri().toURL());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        JLabel lblLogo = new JLabel("Logo");
        if (backgroundImage != null) {
            lblLogo = new JLabel(new ImageIcon(backgroundImage));
        }        
        GridBagConstraints gbc_lblLogo = new GridBagConstraints();
        gbc_lblLogo.gridwidth = 2;
        gbc_lblLogo.insets = new Insets(0, 0, 5, 5);
        gbc_lblLogo.gridx = 1;
        gbc_lblLogo.gridy = 2;
        getContentPane().add(lblLogo, gbc_lblLogo);
        
        JLabel lblVersionTilte = new JLabel("Version");
        GridBagConstraints gbc_lblVersionTilte = new GridBagConstraints();
        gbc_lblVersionTilte.anchor = GridBagConstraints.WEST;
        gbc_lblVersionTilte.insets = new Insets(0, 0, 5, 5);
        gbc_lblVersionTilte.gridx = 1;
        gbc_lblVersionTilte.gridy = 3;
        getContentPane().add(lblVersionTilte, gbc_lblVersionTilte);
        
        JLabel lblVersion = new JLabel(ReleaseInfo.BUILD_VERSION);
        GridBagConstraints gbc_lblVersion = new GridBagConstraints();
        gbc_lblVersion.anchor = GridBagConstraints.WEST;
        gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
        gbc_lblVersion.gridx = 2;
        gbc_lblVersion.gridy = 3;
        getContentPane().add(lblVersion, gbc_lblVersion);
        
        JLabel lblBuildNumberTitle = new JLabel("Build number");
        GridBagConstraints gbc_lblBuildNumberTitle = new GridBagConstraints();
        gbc_lblBuildNumberTitle.anchor = GridBagConstraints.WEST;
        gbc_lblBuildNumberTitle.insets = new Insets(0, 0, 5, 5);
        gbc_lblBuildNumberTitle.gridx = 1;
        gbc_lblBuildNumberTitle.gridy = 4;
        getContentPane().add(lblBuildNumberTitle, gbc_lblBuildNumberTitle);
        
        JLabel lblbuildNumber = new JLabel(ReleaseInfo.BUILD_NUMBER);
        GridBagConstraints gbc_lblbuildNumber = new GridBagConstraints();
        gbc_lblbuildNumber.anchor = GridBagConstraints.WEST;
        gbc_lblbuildNumber.insets = new Insets(0, 0, 5, 5);
        gbc_lblbuildNumber.gridx = 2;
        gbc_lblbuildNumber.gridy = 4;
        getContentPane().add(lblbuildNumber, gbc_lblbuildNumber);
        
        JLabel lblBuildDateTitle = new JLabel("Build date");
        GridBagConstraints gbc_lblBuildDateTitle = new GridBagConstraints();
        gbc_lblBuildDateTitle.anchor = GridBagConstraints.WEST;
        gbc_lblBuildDateTitle.insets = new Insets(0, 0, 5, 5);
        gbc_lblBuildDateTitle.gridx = 1;
        gbc_lblBuildDateTitle.gridy = 5;
        getContentPane().add(lblBuildDateTitle, gbc_lblBuildDateTitle);
        
        JLabel lblBuildDate = new JLabel(ReleaseInfo.BUILD_TIMESTAMP);
        GridBagConstraints gbc_lblBuildDate = new GridBagConstraints();
        gbc_lblBuildDate.anchor = GridBagConstraints.WEST;
        gbc_lblBuildDate.insets = new Insets(0, 0, 5, 5);
        gbc_lblBuildDate.gridx = 2;
        gbc_lblBuildDate.gridy = 5;
        getContentPane().add(lblBuildDate, gbc_lblBuildDate);
        
        JTextPane txtpnCopyrightc = new JTextPane();
        txtpnCopyrightc.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        StyledDocument doc = txtpnCopyrightc.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        txtpnCopyrightc.setText("Copyright(C) 2017 - 2023\nApplication design, implementation and testing:\n     Luís P. N. Mendes\n\nProject supervisors:\n   Rui M. L. Ferreira\n   Alexandre J. M. Bernardino");
        txtpnCopyrightc.setEditable(false);
        GridBagConstraints gbc_txtpnCopyrightc = new GridBagConstraints();
        gbc_txtpnCopyrightc.gridwidth = 2;
        gbc_txtpnCopyrightc.insets = new Insets(0, 0, 5, 5);
        gbc_txtpnCopyrightc.fill = GridBagConstraints.BOTH;
        gbc_txtpnCopyrightc.gridx = 1;
        gbc_txtpnCopyrightc.gridy = 6;
        getContentPane().add(txtpnCopyrightc, gbc_txtpnCopyrightc);       
        
        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });
        GridBagConstraints gbc_btnOk = new GridBagConstraints();
        gbc_btnOk.fill = GridBagConstraints.VERTICAL;
        gbc_btnOk.gridwidth = 2;
        gbc_btnOk.insets = new Insets(0, 0, 5, 5);
        gbc_btnOk.gridx = 1;
        gbc_btnOk.gridy = 8;
        getContentPane().add(btnOk, gbc_btnOk);
    }
}
