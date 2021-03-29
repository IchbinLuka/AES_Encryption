import java.nio.file.FileVisitResult;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.*;


public class ZipDir extends SimpleFileVisitor<Path> {
    
    private ZipOutputStream outputStream;
    private Path sourcePath;

    public ZipDir(Path sourcePath, ZipOutputStream outputStream)
    {
        this.outputStream = outputStream;
        this.sourcePath = sourcePath;
    }
    

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
    {
        try {
            Path targetFile = sourcePath.relativize(file);
 
            outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
 
            byte[] bytes = Files.readAllBytes(file);
            outputStream.write(bytes, 0, bytes.length);
            outputStream.closeEntry();
        } catch (IOException e)
        {
            System.err.println(e);
        }
        return FileVisitResult.CONTINUE;
    }

}
