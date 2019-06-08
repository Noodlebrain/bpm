package noodlebrain.bpm;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class PackageManager
{
    public static void main(String[] args)
    {
        parseArgs(args);
    }

    private static void parseArgs(String[] args) {
        if (args.length < 1)
        {
            System.err.println("bpm: Insufficient arguments\n" +
                "For usage, try \"bpm help\".");
            System.exit(1);
        }

        if (args[0].equals("init"))
        {
            // init - sets up a package folder using args[1] as name
            // default case (no args[1]): create in current directory
            if (args.length < 2)
            {
                init(".");
            }
            else
            {
                init(args[1]);
            }
        }
        else if (args[0].equals("beat"))
        {
            // beat - checks heartbeat vs installed packages and installs necessary packages
        }
        else if (args[0].equals("pump"))
        {
            // pump - upload current package to cdvs

        }
        else if (args[0].equals("help"))
        {

        }
        else
        {
            System.err.println("bpm: Unknown command\n" +
                "For usage, use \"bpm help\".");
            System.exit(127);
        }
    }

    private static void init(String path)
    {
        /* if the path isn't the current directory, make a new one
         * (if it's not there already)  */
        File dir = new File(path);
        if (!path.equals("."))
        {
            makeNewDirs(dir);
        }
        // create directories for init, if they don't already exist: packages and source
        File packageDir = new File(path + "/Packages");
        File sourceDir = new File(path + "/Source");
        makeNewDirs(packageDir);
        makeNewDirs(sourceDir);

        /* create a new YAML heartbeat file
         * if one already exists, prompt user if they would like to overwrite it
         */
        File heartbeat = new File(path + "/heartbeat.yaml");


        if (heartbeat.exists())
        {
            String response = "";
            Scanner sc = new Scanner(System.in);
            while (!response.equals("y") && !response.equals("n"))
            {
                System.out.println(
                        "bpm: WARNING: Heartbeat file already exists, would you like to overwrite it? [y/n]");
                response = sc.next().toLowerCase();
            }

            // if they don't want to overwrite the heartbeat file, return
            if (response.equals("n"))
            {
                return;
            }
        }

        String dirName = dir.getName();
        if (dirName.equals("."))
        {
            dirName = dir.getAbsoluteFile().getParentFile().getName();
        }

        try
        {
            Heartbeat.create(heartbeat, dirName);
        }
        catch (IOException e)
        {
            System.err.println("bpm: init: Unable to create heartbeat file");
            System.exit(1);
        }
    }


    // creates a new directory (if it doesn't already exist)
    private static void makeNewDirs(File file)
    {
        if (file.exists())
        {
            /* if the file exists, check if it is a directory or not
             * if it's a directory, return - we don't need to do anything
             * if it's a file, remove it
             */
            if (file.isDirectory())
            {
                return;
            }
        }
        // create directory
        if (!file.mkdirs())
        {
            System.err.println("bpm: Unable to create directory " + file.getName());
            System.exit(1);
        }
    }

}
