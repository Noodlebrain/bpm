package noodlebrain.bpm;

import java.util.LinkedHashMap;
import java.util.Map;

public class Dependencies {
    private Map<String, Package> dev;
    private Map<String, Package> test;
    private Map<String, Package> prod;

    public Dependencies()
    {

    }

    public void initEnvs()
    {
        this.dev = new LinkedHashMap<>();
        this.test = new LinkedHashMap<>();
        this.prod = new LinkedHashMap<>();
    }

    public Map<String, Package> getDev()
    {
        return this.dev;
    }
    public void setDev(Map<String, Package> dev)
    {
        this.dev = dev;
    }
    public Map<String, Package> getTest()
    {
        return this.test;
    }
    public void setTest(Map<String, Package> test)
    {
        this.test = test;
    }
    public Map<String, Package> getProd()
    {
        return this.prod;
    }
    public void setProd(Map<String, Package> prod)
    {
        this.prod = prod;
    }

    public Map<String, Package> getEnv(String env)
    {
        if (env.equals("dev"))
        {
            return this.dev;
        }
        else if (env.equals("prod"))
        {
            return this.prod;
        }
        else if (env.equals("test"))
        {
            return this.test;
        }
        else
        {
            return null;
        }
    }
    public void setEnv(String env, Map<String, Package> map)
    {
        if (env.equals("dev"))
        {
            this.dev = map;
        }
        else if (env.equals("prod"))
        {
            this.prod = map;
        }
        else if (env.equals("test"))
        {
            this.test = map;
        }
    }

    public void putPackageIntoEnv(String env, String name, String version)
    {
        Package pkg = new Package(version);
        this.getEnv(env).put(name, pkg);
    }

}
