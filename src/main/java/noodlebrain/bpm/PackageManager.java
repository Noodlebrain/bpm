package noodlebrain.bpm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PackageManager
{
    public static void main(String[] args)
    {
        parseArgs(args);
    }

    private static void parseArgs(String[] args)
    {
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
            if (args.length < 2)
            {
                System.err.println("Usage: bpm beat env");
                System.exit(126);
            }
            else
            {
                beat(args[1]);
            }
        }
        else if (args[0].equals("pump"))
        {
            // pump - upload current package to cdvs
            pump();

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
            Heartbeat.createHeartbeatFile(heartbeat, dirName);
        }
        catch (IOException e)
        {
            System.err.println("bpm: init: Unable to create heartbeat file");
            System.exit(1);
        }
    }


    /* beat - creates symlink to core package folder,
     * reads heartbeat file, and installs packages not already installed
     */

    private static void beat(String env)
    {
        Heartbeat heartbeatInfo = new Heartbeat();
        Heartbeat lastBeat;
        Heartbeat newLock = new Heartbeat();

        // create symbolic link to core packages folder
        try
        {
            CoreSymlink.makeCoreSymlink("Packages");
        }
        catch (IOException e)
        {
            System.err.println("bpm: beat: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.exit(1);
        }


        // create a stack to store packages to be analyzed
        Stack<String> unparsedPkgs = new Stack<>();
        unparsedPkgs.push(".");


        while (unparsedPkgs.size() > 0)
        {
            String pkgName = unparsedPkgs.pop();

            // initialize heartbeat object from file
            try
            {
                heartbeatInfo = Heartbeat.readHeartbeatFile(pkgName + "/heartbeat.yaml");

                // initialize fields for new heartbeat.lock
                newLock.setName(heartbeatInfo.getName());
                newLock.setVersion(heartbeatInfo.getVersion());
                newLock.initDeps();
                newLock.getDeps().initEnvs();
            }
            catch (FileNotFoundException e)
            {
                System.err.println("bpm: beat: ERROR: Heartbeat file not found");
                System.exit(1);
            }

            // read heartbeat.lock, if there is one
            try
            {
                lastBeat = Heartbeat.readHeartbeatFile("heartbeat.lock");
                if (lastBeat.getDeps() != null)
                {
                    Map<String, Package> enviro = lastBeat.getDeps().getEnv(env);
                    if (enviro != null)
                    {
                        newLock.getDeps().setEnv(env, enviro);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
            }

            // get the diff between the current heartbeat and heartbeat.lock file
            Map<String, Package> diff = heartbeatInfo.getDiff(newLock, env);

            // for each element in the diff: find an appropriate version
            for (String diffPkg : diff.keySet())
            {
                try
                {
                    List<PulseEntry> pulse = CdvsUtils.getPulse(diffPkg);
                    Collections.sort(pulse);

                    for (PulseEntry entry : pulse)
                    {
                        Package pkg = diff.get(diffPkg);
                        if (pkg.inVersionRange(entry.version))
                        {
                            // download package
                            CdvsUtils.downloadPackage(entry.url, diffPkg);
                            // put this package on the stack so its deps can be analyzed
                            unparsedPkgs.push("Packages/" + diffPkg);
                            // update new lock with downloaded package
                            newLock.getDeps().putPackageIntoEnv(env, diffPkg, entry.version);
                            break;
                        }
                    }
                }
                catch (IOException e)
                {
                    System.err.println("bpm: beat: WARNING: Unable to retrieve package " + diffPkg);
                }
            }

        }
        try
        {
            newLock.writeHeartbeatLock(new File("heartbeat.lock"));
        }
        catch (IOException e)
        {
            System.err.println("bpm: beat: WARNING: Unable to create new heartbeat.lock");
        }

    }

    // create tarball from package, to be uploaded to cdvs
    /* TODO: implement login prompt, and use session cookie to automatically upload tarball
     * currently infeasible due to Rails' CSRF protection
     */


    private static void pump()
    {
        try
        {
            CdvsUtils.createTarball();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("bpm: pump: Unable to create tarball for package");
        }
    }

    /*
     * helper methods
     */

    // creates a new directory (if it doesn't already exist)
    private static void makeNewDirs(File file)
    {
        if (file.exists())
        {
            /* if the file exists, check if it is a directory or not
             * if it's a directory, return - we don't need to do anything
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
