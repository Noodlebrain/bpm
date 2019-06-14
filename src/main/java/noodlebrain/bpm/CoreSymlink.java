package noodlebrain.bpm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

class CoreSymlink
{
    static void makeCoreSymlink(String pathName) throws IOException
    {
        // create symbolic link to core packages directory in "Packages" directory

        // core packages location is set by environment variable when BLZ is installed
        String blzVar = "BLZPACKAGES";

        // read the environment variable
        String packagesPathName = System.getenv(blzVar);

        // if the path name is null, the variable is not defined
        if (packagesPathName == null)
        {
            System.err.println(
                    "bpm: beat: ERROR: Environment variable " + blzVar + " not set. Is BLZ installed on this machine?");
            System.exit(1);
        }

        // create Path objects for the core path and the destination path
        Path corePath = Paths.get(packagesPathName, "Core");
        Path destPath = Paths.get(pathName,"Core").toAbsolutePath();


        /* Windows requires admin rights to create symbolic links -
         * if the user is running Windows, copy the core packages directory instead
         */
        String os = System.getProperty("os.name");

        if (!os.contains("Windows"))
        {
            // Non-Windows OSes - create symbolic link
            Files.createSymbolicLink(corePath, destPath);
        }
        else
        {
            // Windows - copy core packages directory and its contents
            FileUtils.copyDirectory(corePath.toFile(), destPath.toFile());
        }
    }
}
