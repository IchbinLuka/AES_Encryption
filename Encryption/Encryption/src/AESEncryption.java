import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;
import java.nio.file.Files;

import java.io.File;

public class AESEncryption {


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
        FileOutputStream output = new FileOutputStream(outFile);

        try {
            Cipher cipher;

            SecretKeySpec sKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(ciphermode, sKeySpec, new IvParameterSpec(new byte[16]));

            byte[] inBytes = new byte[(int) file.length()];
            input.read(inBytes);

            byte[] outBytes = cipher.doFinal(inBytes);

            output.write(outBytes);
            
        } 
        finally
        {
            input.close();
            output.close();
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
}
