package name.ignat.minerva.util;

import static com.google.common.io.Files.createParentDirs;
import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Dan Ignat
 */
public final class Files
{
    public static FileOutputStream openNewOutputFile(File file) throws IOException
    {
        createNewFileSafely(file);

        return new FileOutputStream(file);
    }

    public static void createNewFileSafely(File file) throws IOException
    {
        createParentDirs(file);

        if (!file.createNewFile())
        {
            throw new FileExistsException(file);
        }
    }

    private Files() { }

    @SuppressWarnings("serial")
    public static class FileExistsException extends IOException
    {
        public FileExistsException(File file)
        {
            super(format("File '%s' already exists", file.getPath()));
        }
    }
}
