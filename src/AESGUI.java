import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;


public class AESGUI extends Frame {
    private static final int FRAME_HEIGHT = 200;
    private static final int FRAME_WIDTH = 350;
    private static final String VERSION = "2.0 Dev";

    private static final String PASSWORD_LABEL_1 = "Key";

    private TextField currentDir, password1, password2;

    private Label passwordLabel1, passwordLabel2;

    private Button selectButton, startButton;

    private JCheckBox checkBoxEncrypt, checkBoxDecrypt;

    private JFileChooser fileChooser;

    private String path;

    private boolean encrypt = true;


    private AESGUI(String title)
    {
        super(title);
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
                try {
                    start();
                }
                catch (BadPaddingException bException) {
                    new ErrorMessage("Wrong Password/Key!");
                }
                catch(FileNotFoundException fException)
                {
                    new ErrorMessage("File/Directory not found!");
                    fException.printStackTrace();
                }
                catch (Exception e) {
                    new ErrorMessage("Something went wrong! Check console for more info.");
                    e.printStackTrace();
                }
            }
        });
        cp.add(startButton);

        currentDir = new TextField();
        currentDir.setBounds(80, 95, 250, 23);
        currentDir.setText("No directory selected");
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
        if(keyBytes.length != 16)
        {
            byte[] newKey = new byte[16];
            if(keyBytes.length < 16)
            {
                for(int i = 0; i < keyBytes.length; i++) newKey[i] = keyBytes[i];
            }
            else
            {
                for(int i = 0; i < 16; i++) newKey[i] = keyBytes[i];
            }
            keyBytes = newKey;
        }
        if(!encrypt)
        {
            File encryptedFile = new File(path);
            //AESEncryption.encryption(encryptedFile, key, Cipher.DECRYPT_MODE, path + "_decrypted.zip");
            File infoFile = new File(path.replace(".enc", ".info"));
            if(!infoFile.exists())
            {
                new InfoFileSelector(infoFile);
            }
            AESEncryption.decrypt(encryptedFile, infoFile, new String(keyBytes), path + "_decrypted.zip");
            File decryptedZip = new File(path + "_decrypted.zip");
            UnzipUtility.unzip(path + "_decrypted.zip", path + "_decrypted");
            if(!encryptedFile.delete() || !decryptedZip.delete()) 
                new ErrorMessage("Unable to clean up results!");
        }
        else
        {
            AESEncryption.compress(path, path + "_compressed.zip");
            File compressedFile = new File(path + "_compressed.zip");

            AESEncryption.encryption(new File(path + "_compressed.zip"), key, 
                Cipher.ENCRYPT_MODE, 
                path + ".enc");
            if(!compressedFile.delete()) new ErrorMessage("Unable to clean up results!");
        }
    }

    private void selectDirectory()
    {
        fileChooser = new JFileChooser();
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
    public static void main(String[] args) {
        new AESGUI("AES Encryption v" + VERSION);
    }
}