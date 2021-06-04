import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;

public class InfoFileSelector extends Frame {
    private static final int FRAME_HEIGHT = 200;
    private static final int FRAME_WIDTH = 350;
    private static final String TITLE = "Select Info-file";
    private static final String INFO_STRING = 
        "An info-file is necessary for decryption. Select it to proceed.";

    private static final int TEXT_MARGIN = 5;
    private static final int LABEL_HEIGHT = 10;

    private static final String BUTTON_LABEL = "Select File";
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_MARGIN = 20;

    private static final String FILE_CHOOSER_TITLE = "Select info-file";

    private Label  label;
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
        setResizable(false);

        Panel cp = new Panel(null);
        add(cp);

        label = new Label();
        label.setText(INFO_STRING);
        label.setBounds(TEXT_MARGIN, TEXT_MARGIN, 
                            FRAME_WIDTH - TEXT_MARGIN * 2, 
                            LABEL_HEIGHT);
        add(label);
        
        button = new Button();
        button.setLabel(BUTTON_LABEL);
        button.setBounds((FRAME_WIDTH - BUTTON_WIDTH) / 2, 
            TEXT_MARGIN + LABEL_HEIGHT + BUTTON_MARGIN + BUTTON_HEIGHT, 
            BUTTON_WIDTH, BUTTON_HEIGHT);
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectFile(infoFile);
                    dispose();
                }
            });
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
