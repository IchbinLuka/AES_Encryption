import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.swing.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;


public class AESGUI extends Frame {
    private static final int FRAME_HEIGHT = 230;
    private static final int FRAME_WIDTH = 350;
    private static final String VERSION = "2.0 Beta";
    private static final String FILE_CHOOSER_TITLE = "Select info-file";

    private static final String PASSWORD_LABEL_1 = "Key";

    private static final String ENCRYPTED_FILE_ENDING = ".enc";

    private static final String DEFAULT_DIRECTORY = "C:/Users/Luka/Documents/CM/AES_Encryption/Encryption_alpha_2-0";

    private static final int KEY_LENGTH = 32;

    private static final String ERROR_NO_INFO_FILE = "Cannot decrypt without an info-file!";
    private static final String ERROR_LOOK_AND_FEEL_NOT_FOUND = "Not able to apply system default look and feel!";

    private TextField currentDir, password1, password2;

    private Label passwordLabel1, passwordLabel2;

    private Button selectButton, startButton;

    private JCheckBox checkBoxEncrypt, checkBoxDecrypt;

    private JFileChooser fileChooser;

    private JProgressBar progressBar;

    private String path;

    private boolean encrypt = true;


    private AESGUI(String title)
    {
        super(title);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) { dispose(); }
            });
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(true);
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;

        setLocation(x, y);

        Panel cp = new Panel(null);
        add(cp);

        passwordLabel1 = new Label();
        passwordLabel1.setText(PASSWORD_LABEL_1);
        passwordLabel1.setBounds(10, 15, 60, 25);
        cp.add(passwordLabel1);

        password1 = new TextField();
        password1.setBounds(80, 15, 150, 23);
        password1.setEditable(true);
        password1.setEchoChar('*');
        cp.add(password1);

        passwordLabel2 = new Label();
        passwordLabel2.setText("Confirm");
        passwordLabel2.setBounds(10, 50, 60, 25);
        cp.add(passwordLabel2);

        password2 = new TextField();
        password2.setBounds(80, 50, 150, 23);
        password2.setEditable(true);
        password2.setEchoChar('*');
        cp.add(password2);

        checkBoxEncrypt = new JCheckBox("Encrypt");
        checkBoxEncrypt.setBounds(getSize().width - 90, 15, 70, 25);
        checkBoxEncrypt.setSelected(true);
        checkBoxEncrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkBoxDecrypt.setSelected(false);
                encrypt = true;
            }
        });
        cp.add(checkBoxEncrypt);

        checkBoxDecrypt = new JCheckBox("Decrypt");
        checkBoxDecrypt.setBounds(getSize().width - 90, 50, 70, 25);
        checkBoxDecrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkBoxEncrypt.setSelected(false);
                encrypt = false;
            }
        });
        cp.add(checkBoxDecrypt);

        selectButton = new Button();
        selectButton.setBounds(10, 95, 60, 25);
        selectButton.setLabel("Select");
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectDirectory();
            }
        });
        cp.add(selectButton);

        startButton = new Button();
        startButton.setBounds(10, 130, getWidth() - 30, 25);
        startButton.setLabel("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startButton.setEnabled(false);
                try {
                    start();
                }
                catch(FileNotFoundException fException)
                {
                    new ErrorMessage("File/Directory not found!");
                    fException.printStackTrace();
                }
                catch (IOException ioException)
                {
                    new ErrorMessage("IOException");
                }
                catch (Exception e) {
                    new ErrorMessage("Something went wrong! Check console for more info.");
                    e.printStackTrace();
                }
            }
        });
        cp.add(startButton);

        progressBar = new JProgressBar();
        progressBar.setBounds(startButton.getX(), startButton.getY() + 30, startButton.getWidth(), startButton.getHeight());
        //progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        cp.add(progressBar);

        currentDir = new TextField();
        currentDir.setBounds(80, 95, 250, 23);
        currentDir.setText("No directory/file selected");
        cp.add(currentDir);

        setVisible(true);
    }

    public void start() throws Exception
    {
        if(!password1.getText().equals(password2.getText())) 
        {
            new ErrorMessage("The Passwords don't match");
            return;
        }
        else if(path == null)
        {
            new ErrorMessage("File/Directory not found");
            return;
        }

        byte[] keyBytes = password1.getText().getBytes();
        if(keyBytes.length != KEY_LENGTH)
        {
            byte[] newKey = new byte[KEY_LENGTH];
            if(keyBytes.length < KEY_LENGTH)
            {
                for(int i = 0; i < keyBytes.length; i++) newKey[i] = keyBytes[i];
            }
            else
            {
                for(int i = 0; i < KEY_LENGTH; i++) newKey[i] = keyBytes[i];
            }
            keyBytes = newKey;
        }
        if(!encrypt)
        {
            File encryptedFile = new File(path);
            File infoFile = new File(path.replace(".enc", ".info"));
            if(!infoFile.exists())
            {
                infoFile = selectInfoFile();
                if (infoFile == null || !infoFile.exists()) {
                    new ErrorMessage(ERROR_NO_INFO_FILE);
                    return;
                }
            }
            AESEncryption task = new AESEncryption(
                    encryptedFile,
                    infoFile,
                    new String(keyBytes),
                    path + "_decrypted.zip",
                    progressBar,
                    startButton);
            task.execute();
        }
        else
        {
            AESEncryption task = new AESEncryption(
                    new File(path),
                    new String(keyBytes),
                    path + ENCRYPTED_FILE_ENDING,
                    progressBar,
                    startButton);
            task.execute();
        }
    }

    private void selectDirectory()
    {
        fileChooser = new JFileChooser();
        if(DEFAULT_DIRECTORY != null)
            fileChooser.setCurrentDirectory(new File(DEFAULT_DIRECTORY));

        if(encrypt)
        {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Directory");
            fileChooser.setAcceptAllFileFilterUsed(false);
        }
        else
        {
            fileChooser.setDialogTitle("Select File");
        }

        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            path = fileChooser.getSelectedFile().getPath();
            currentDir.setText(path);
        }
    }

    private File selectInfoFile()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(FILE_CHOOSER_TITLE);

        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();
        else
            return null;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            new ErrorMessage(ERROR_LOOK_AND_FEEL_NOT_FOUND);
            e.printStackTrace();
        }

        new AESGUI("AES Encryption v" + VERSION);
    }
}