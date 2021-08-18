import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

public class InfoFileSelector extends Frame {
    private static final int FRAME_HEIGHT = 170;
    private static final int FRAME_WIDTH = 350;
    private static final String TITLE = "Select Info-file";
    private static final String INFO_STRING = 
        "<html><body><p>An info-file is necessary for decryption.</p>" +
                "<p>Select it to proceed.</p></body></html>";

    private static final int TEXT_MARGIN = 15;
    private static final int LABEL_HEIGHT = 30;

    private static final String BUTTON_LABEL = "Select File";
    private static final int BUTTON_WIDTH = FRAME_WIDTH - 50;
    private static final int BUTTON_HEIGHT = 50;
    private static final int BUTTON_MARGIN = 0;

    private static final String FILE_CHOOSER_TITLE = "Select info-file";

    private JLabel  label;
    private Button button;

    public InfoFileSelector(File infoFile)
    {
        super(TITLE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) { dispose(); }
            });
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;

        setLocation(x, y);

        Panel cp = new Panel(null);
        add(cp);

        label = new JLabel(INFO_STRING, JLabel.CENTER);
        label.setBounds(0, TEXT_MARGIN,
                            FRAME_WIDTH - TEXT_MARGIN * 2, 
                            LABEL_HEIGHT);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        cp.add(label);
        
        button = new Button();
        button.setLabel(BUTTON_LABEL);

        button.setBounds((FRAME_WIDTH - BUTTON_WIDTH) / 2, 
            TEXT_MARGIN + BUTTON_MARGIN + BUTTON_HEIGHT,
            BUTTON_WIDTH, BUTTON_HEIGHT);
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectFile(infoFile);
                    dispose();
                }
            });
        cp.add(button);
        setVisible(true);
    }

    private void selectFile(File infoFile)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(FILE_CHOOSER_TITLE);

        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            infoFile = fileChooser.getSelectedFile();
    }

    public static void main(String[] args) {
        new InfoFileSelector(new File(""));
    }
}
