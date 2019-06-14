package noodlebrain.bpm;

import org.apache.commons.lang.StringUtils;

public class Package
{
    // instance variables
    private String min;
    private String max;

    // JavaBeans constructor
    public Package()
    {
    }

    // constructor to set min and max to the same version
    public Package(String ver)
    {
        this.min = ver;
        this.max = ver;
    }

    // instance methods

    public String getMin()
    {
        return this.min;
    }
    public void setMin(String min)
    {
        this.min = min;
    }

    public String getMax()
    {
        return this.max;
    }
    public void setMax(String max)
    {
        this.max = max;
    }

    // compares another lock Package to this package;
    // returns true if the other package's versions are within this one's
    public boolean inVersionRange(String other)
    {
        // case if this package's max version are "latest" or ""
        if (this.max.equals("latest") || this.max.equals(""))
        {
            if (this.min.equals(""))
            {
                return true;
            }
            else
            {
                int minComp = compareVersions(this.min, other);
                return minComp < 1;
            }
        }
        // case if this package's min version is "" and max is not "latest" or empty
        else if (this.min.equals(""))
        {
            int maxComp = compareVersions(this.max, other);
            return maxComp > -1;
        }

        /* otherwise, check that our max >= other version's max,
         * and our min <= other version's min
         */
        else {
            int minComp = compareVersions(this.min, other);
            int maxComp = compareVersions(this.max, other);
            return (minComp < 1 && maxComp > -1);
        }
    }

    // static methods

    /* compare versions between two version strings:
     * returns -1 if a is less than b,
     * returns 0 if a and b are equal,
     * returns 1 if a is greater than b.
     */
    static int compareVersions(String a, String b)
    {
        if (b == null)
        {
            return 1;
        }

        // separate the two version strings into arrays containing their sub-versions
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");

        int aLen = aParts.length;
        int bLen = bParts.length;


        // iterate through the version string arrays
        int i = 0;
        while (i < aLen && i < bLen)
        {
            /* edge cases for non-numeric versions:
             * if only one version is non-numeric, the other (numeric) one is greater
             */
            if (!StringUtils.isNumeric(aParts[i]))
            {
                // if both are non-numeric, compare strings
                if (!StringUtils.isNumeric(bParts[i]))
                {
                    return aParts[i].compareTo(bParts[i]);
                }
                else
                {
                    return -1;
                }
            }
            else if (!StringUtils.isNumeric(bParts[i]))
            {
                return 1;
            }
            int aPart = Integer.parseInt(aParts[i]);
            int bPart = Integer.parseInt(bParts[i]);

            if (aPart < bPart)
            {
                return -1;
            }
            else if (aPart > bPart)
            {
                return 1;
            }
            i++;
        }
        /* if we reach the end of one, the longer version is considered "greater"
         * e.g. "4.2.0" > "4.2"
         */
        if (aLen < bLen)
        {
            return -1;
        }
        else if (aLen > bLen)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
}
