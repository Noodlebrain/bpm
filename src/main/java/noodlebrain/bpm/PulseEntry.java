package noodlebrain.bpm;

public class PulseEntry implements Comparable<PulseEntry>
{
    String version;
    String url;

    public int compareTo(PulseEntry o)
    {
        return Package.compareVersions(this.version, o.version);
    }
}
