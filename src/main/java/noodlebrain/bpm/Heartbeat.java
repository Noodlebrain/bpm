package noodlebrain.bpm;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Heartbeat
{
    // instance variables
    private String name;
    private String version;
    private Dependencies deps;

    // JavaBeans constructor
    public Heartbeat()
    {
    }

    // instance methods

    // get name of this heartbeat
    public String getName()
    {
        return this.name;
    }
    // set name of this heartbeat
    public void setName(String name)
    {
        this.name = name;
    }

    // get version of this heartbeat
    public String getVersion()
    {
        return this.version;
    }
    // set version of this heartbeat
    public void setVersion(String version)
    {
        this.version = version;
    }

    // get map for dependencies
    public Dependencies getDeps()
    {
        return this.deps;
    }
    // set map for dependencies
    public void setDeps(Dependencies deps)
    {
        this.deps = deps;
    }

    // initialize dependencies
    public void initDeps()
    {
        this.deps = new Dependencies();
    }

    // get diff between this object and another Heartbeat object (lock file), given a certain env
    public Map<String, Package> getDiff(Heartbeat other, String env)
    {
        Map<String, Package> envDeps = this.getDeps().getEnv(env);

        Map<String, Package> diff = new HashMap<>();

        if (envDeps == null)
        {
            return diff;
        }

        // if the other heartbeat is null, return the entire list of packages
        if (other == null)
        {
            return envDeps;
        }

        // otherwise, get the dependencies of the other file
        Dependencies otherDeps = other.getDeps();
        // if the other file's dependencies are null or empty, return entire list of packages
        if (otherDeps == null)
        {
            return envDeps;
        }

        Map<String, Package> otherEnv = otherDeps.getEnv(env);
        if (otherEnv == null)
        {
            return envDeps;
        }
        if (otherEnv.size() < 1)
        {
            return envDeps;
        }

        // otherwise, check each heartbeat's dependencies against each other
        for (String dep: envDeps.keySet())
        {
            if (otherEnv.containsKey(dep))
            {
                Package pkg = envDeps.get(dep);
                Package otherPkg = otherEnv.get(dep);
                if (!pkg.inVersionRange(otherPkg.getMin()))
                {
                    /* currently, no elegant way to handle needing multiple versions of the same
                     * dependency - use the newer version if the situation arises
                     */
                    if (Package.compareVersions(pkg.getMax(), otherPkg.getMax()) > 0)
                    {
                        diff.put(dep, pkg);
                    }
                }
                }
        }

        return diff;
    }


    /* writes a Heartbeat object to disk as a YAML file
     * Unfortunately, SnakeYAML doesn't do a great job at dumping data, so we're writing it
     */
    void writeHeartbeatLock(File path) throws IOException
    {
        String fileHeader =
                "name: " + this.name + "\n" +
                "version: 0.1.0\n" +
                "deps:\n";

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(fileHeader);

        String devTemplate = "  dev:\n";
        writer.write(devTemplate);

        Map<String, Package> devDeps = this.deps.getDev();
        for (String devDep : devDeps.keySet())
        {
            Package pkg = devDeps.get(devDep);
            String devLine = "    " + devDep + ": {min: " + pkg.getMin() + ", max: " + pkg.getMax() + " }\n";
            writer.write(devLine);
        }


        String testTemplate = "  test:\n";
        writer.write(testTemplate);

        Map<String, Package> testDeps = this.deps.getTest();
        for (String testDep : testDeps.keySet())
        {
            Package pkg = testDeps.get(testDep);
            String testLine = "    " + testDep + ": {min: " + pkg.getMin() + ", max: " + pkg.getMax() + " }\n";
            writer.write(testLine);
        }


        String prodTemplate = "  prod:\n";
        writer.write(prodTemplate);

        Map<String, Package> prodDeps = this.deps.getProd();
        for (String prodDep : prodDeps.keySet())
        {
            Package pkg = testDeps.get(prodDep);
            String prodLine = "    " + prodDep + ": {min: " + pkg.getMin() + ", max: " + pkg.getMax() + " }\n";
            writer.write(prodLine);
        }


        writer.close();
    }


    // static methods

    // creates a new Heartbeat YAML file
    static void createHeartbeatFile(File path, String name) throws IOException
    {
        String fileTemplate =
                "name: " + name + "\n" +
                "version: 0.1.0\n" +
                "deps:\n" +
                "  dev:\n" +
                "#    devPackage: {min: 0.1.0, max: latest}\n" +
                "  test:\n" +
                "#    testPackage: {min: 0.1.0, max: latest}\n" +
                "  prod:\n" +
                "#    example: {min: \"\", max: 2.0}\n";

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(fileTemplate);
        writer.close();
    }

    /* read a heartbeat YAML file, and get an Heartbeat object -
     * in it, is defined a name, version, and dependencies, split up by branch name
     * Each package in a branch has name, minimum version, and maximum version
     */

    static Heartbeat readHeartbeatFile(String path) throws FileNotFoundException
    {
        Constructor constructor = new Constructor(Heartbeat.class);
        TypeDescription depsDesc = new TypeDescription(Dependencies.class);
        depsDesc.putMapPropertyType("dev", Object.class, Package.class);
        depsDesc.putMapPropertyType("test", Object.class, Package.class);
        depsDesc.putMapPropertyType("prod", Object.class, Package.class);
        constructor.addTypeDescription(depsDesc);

        Yaml yaml = new Yaml(constructor);
        InputStream input = new FileInputStream(path);

        Heartbeat beat = yaml.load(input);
        return beat;
    }

}

