import java.awt.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipOutputStream;
import java.nio.file.Files;

public class AESEncryption extends SwingWorker<Void, Integer> {

    public static final byte COMPATIBILITY_VERSION = 1;
    public static final int BUFFER_SIZE = 2048;

    private static final String PROGRESS_UNZIPPING = "Extracting Results";
    private static final String PROGRESS_ZIPPING   = "Zipping Folder";
    private static final String PROGRESS_ENCRYPTING = "Encrypting";
    private static final String PROGRESS_DECRYPTING = "Decrypting";
    private static final String PROGRESS_FINISHED = "Finished";
    private static final String COMPRESSED_NAME = "_compressed.zip";

    private boolean encrypt;
    private File file;
    private String key;
    private String destination;
    private File infoFile;
    private JProgressBar pb;
    private Button button;

    private byte[] salt, iv;

    public AESEncryption(File file, String key, String destination, JProgressBar pb, Button button)
    {
        encrypt = true;
        init(file, key, destination, pb, button);
    }

    public AESEncryption(File file, File infoFile, String key, String destination, JProgressBar pb, Button button)
    {
        encrypt = false;
        this.infoFile = infoFile;
        init(file, key, destination, pb, button);
    }

    private void init(File file, String key, String destination, JProgressBar pb, Button button)
    {
        this.destination = destination;
        this.file = file;
        this.key = key;
        this.pb = pb;
        this.button = button;
    }

    public void encrypt()
    {
        SecureRandom random = new SecureRandom();
        salt = new byte[16];
        iv   = new byte[16];

        random.nextBytes(salt);
        random.nextBytes(iv);

        infoFile = new File(destination.replace(".enc", ".info"));
        FileOutputStream out;
        try {
            infoFile.createNewFile();
            out = new FileOutputStream(infoFile);

            out.write(salt);
            out.write(iv);

            out.close();
        } catch (Exception e) {
            new ErrorMessage("Unable to create info file");
        }
    }

    public void decrypt()
        throws Exception
    {
        FileInputStream in = new FileInputStream(infoFile);

        salt = new byte[16];
        iv   = new byte[16];

        in.read(salt);
        in.read(iv);
        in.close();
    }

    private void encryption() throws Exception
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        System.out.println(byteToHex(secretKey.getEncoded()));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        int cipherMode;
        if (encrypt)
            cipherMode = Cipher.ENCRYPT_MODE;
        else
            cipherMode = Cipher.DECRYPT_MODE;

        cipher.init(cipherMode, secretKey, new IvParameterSpec(iv));

        File outFile = new File(destination);
        if (!outFile.createNewFile())
            new ErrorMessage("Unable to create File");

        FileInputStream in = new FileInputStream(file);

        FileOutputStream out = new FileOutputStream(outFile);

        int progress = 0;

        pb.setMaximum((int)((float)file.length() / BUFFER_SIZE));
        pb.setIndeterminate(false);

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;

            while ((read = in.read(buffer)) != -1) {
                byte[] bytes = cipher.update(buffer, 0, read);
                out.write(bytes);
                progress++;
                publish(progress);
            }
            byte[] bytes = in.readAllBytes();
            out.write(cipher.doFinal(bytes));
        }
        catch (BadPaddingException e) {}
        finally {
            out.flush();
            out.close();
            in.close();
        }
    }

    public static void compress(String source, String destination)
    {
        Path sourcePath = Paths.get(source);

        try {
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(destination));

            Files.walkFileTree(sourcePath, new ZipDir(sourcePath, outputStream));

            outputStream.close();
        }
        catch (IOException exception)
        {
            System.err.println("An Error has occured while compressing the files: " + exception);
        }
    }

    private static String byteToHex(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder();

        for(byte b : bytes)
            builder.append(String.format("%02X ", b));

        return builder.toString();
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size()-1);
        pb.setValue(i); // The last value in this array is all we care about.
        System.out.println(i);
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (encrypt)
        {
            pb.setIndeterminate(true);
            pb.setString(PROGRESS_ZIPPING);
            AESEncryption.compress(file.getPath(), file.getPath() + COMPRESSED_NAME);
            file = new File(file.getPath() + COMPRESSED_NAME);

            pb.setIndeterminate(false);
            pb.setString(PROGRESS_ENCRYPTING);

            encrypt();
            encryption();

            if(!file.delete()) new ErrorMessage("Unable to clean up results!");
        }
        else
        {
            pb.setIndeterminate(false);
            pb.setString(PROGRESS_DECRYPTING);
            File decryptedZip = new File(destination);
            decrypt();
            try {
                encryption();

                pb.setIndeterminate(true);
                pb.setString(PROGRESS_UNZIPPING);
                if (isArchive(decryptedZip)) {
                    UnzipUtility.unzip(destination, destination.replace(".zip", ""));

                    if (!file.delete() || !decryptedZip.delete() || !infoFile.delete())
                        new ErrorMessage("Unable to clean up results!");
                }
                else {
                    new ErrorMessage("Wrong Key/Password");
                    decryptedZip.delete();
                }
            }
            catch (BadPaddingException bpe)
            {
                new ErrorMessage("Wrong Password/Key");
                decryptedZip.delete();
            }

            pb.setIndeterminate(false);
        }
        pb.setString(PROGRESS_FINISHED);
        button.setEnabled(true);
        return null;
    }

    private static boolean isArchive(File file)
    {
        int fileSignature = 0;
        try(RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            fileSignature = raf.readInt();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
    }
}
