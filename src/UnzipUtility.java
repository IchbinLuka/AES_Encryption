import java.io.*;
import java.security.KeyStore.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UnzipUtility {
    
    private static final int BUFFER_SIZE = 4096;

    public static void unzip(String filePath, String destination) throws IOException
    {
        File file = new File(destination);



        if(!file.exists())
        {
            file.mkdir();
        }

        ZipInputStream input = new ZipInputStream(new FileInputStream(filePath));
        ZipEntry zipEntry = input.getNextEntry();

        while (zipEntry != null)
        {
            String newFilePath = destination + File.separator + zipEntry.getName();

            if(!zipEntry.isDirectory())
            {
                extractFile(input, newFilePath);
            }
            else
            {
                File dir = new File(newFilePath);
                dir.mkdirs();
            }
            input.closeEntry();
            zipEntry = input.getNextEntry();
        }
        input.close();
    }

    private static void extractFile(ZipInputStream input, String newfilePath) throws IOException
    {
        /*File file = new File(newfilePath);
        file.mkdirs();
        System.out.println(file.getPath());
        if(!file.exists()) file.createNewFile();*/

        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(newfilePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];

        int read = 0;
        while ((read = input.read(bytesIn)) != -1) output.write(bytesIn, 0, read);

        output.close();

    }
}
