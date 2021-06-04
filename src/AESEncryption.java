import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;
import java.nio.file.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;

public class AESEncryption {

    public static final byte COMPATIBILITY_VERSION = 1;
    public static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) throws Exception {
        Key key = new SecretKeySpec("aaaaaaaaaaaaaaaa".getBytes(), "AES");
        encryption(new File("C:/Users/Luka/Documents/CM/AES/Encryption/Test.txt"), key, Cipher.ENCRYPT_MODE, "C:/Users/Luka/Documents/CM/AES/Encryption/Test2.txt");
        encryption(new File("C:/Users/Luka/Documents/CM/AES/Encryption/Test2.txt"), key, Cipher.DECRYPT_MODE, "C:/Users/Luka/Documents/CM/AES/Encryption/Test3.txt");
    }

    public static void encryption(File file, Key key, int ciphermode, String destination) throws  FileNotFoundException, NoSuchAlgorithmException,
    NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
    {
        File outFile = new File(destination);
        outFile.createNewFile();

        FileInputStream input = new FileInputStream(file);
        BufferedInputStream bInput = new BufferedInputStream(input);

        FileOutputStream output = new FileOutputStream(outFile);
        BufferedOutputStream bOutput = new BufferedOutputStream(output);

        try {
            if(ciphermode == Cipher.ENCRYPT_MODE)
            {
                bOutput.write(COMPATIBILITY_VERSION);
            }
            else if(ciphermode == Cipher.DECRYPT_MODE)
            {
                byte[] versionBytes = new byte[1];
                bInput.read(versionBytes);
                if(versionBytes[0] != COMPATIBILITY_VERSION)
                {
                    new ErrorMessage("Wrong Version!");
                }
            }
            
            Cipher cipher;

            SecretKeySpec sKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(ciphermode, sKeySpec, new IvParameterSpec(new byte[16]));
            
            byte[] inBytes = input.readAllBytes();

            byte[] outBytes = cipher.doFinal(inBytes);

            /**
             * Buffered Encryption (not working yet)
             * 
             * while(bInput.available() > 0)
            {
                byte inBytes = (byte)bInput.read();
                byte[] outBytes = cipher.doFinal(inBytes);
                output.write(outBytes);
            } **/
            if(ciphermode == Cipher.ENCRYPT_MODE) output.write(COMPATIBILITY_VERSION);
            output.write(outBytes);
        } 
        finally
        {
            input.close();
            output.close();
        }
    }

    public void encrypt(File file, String key, String destination)
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        byte[] iv   = new byte[16];

        random.nextBytes(salt);
        random.nextBytes(iv);

        File infoFile = new File(destination + ".info");
        FileOutputStream out;
        try {
            infoFile.createNewFile();
            out = new FileOutputStream(infoFile);

            out.write(salt);
            out.write(iv);

            out.close();

            encryption2(file, key, destination, Cipher.ENCRYPT_MODE, salt, iv);
        } catch (Exception e) {
            new ErrorMessage("Unable to create info file");
        }

    }

    public void decrypt(File file, File infoFile, String key, String destination)
        throws Exception
    {
        FileInputStream in = new FileInputStream(infoFile);

        byte[] salt = new byte[16];
        byte[] iv   = new byte[16];

        in.read(salt);
        in.read(iv);

        encryption2(file, key, destination, Cipher.ENCRYPT_MODE, salt, iv);
    }

    private void encryption2(   File file, String key, 
                                String destination, 
                                int ciphermode, 
                                byte[] salt, byte[] iv) throws Exception
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(ciphermode, secretKey, new IvParameterSpec(iv));

        FileInputStream in = new FileInputStream(file);
        
        File outFile = new File(destination);
        outFile.createNewFile();
        FileOutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[128];

        while(in.read(buffer) != -1) 
        {
            byte[] bytes = cipher.update(buffer);
            out.write(bytes);
        }
        byte[] bytes = in.readAllBytes();
        out.write(cipher.doFinal(bytes));

        out.close();
        in.close();

        
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
}
