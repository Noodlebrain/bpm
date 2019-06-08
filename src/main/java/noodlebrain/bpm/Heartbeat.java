package noodlebrain.bpm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Heartbeat
{

    /* creates a new Heartbeat YAML file */
    static void create(File path, String name) throws IOException
    {
        String fileTemplate =
                "name: " + name + "\n" +
                "version: 0.1.0\n" +
                "deps:\n" +
                "  - Core: [min: \"\", max: latest]\n" +
                "  - dev:\n" +
                "    -\n" +
                "  - prod:\n" +
                "    -\n";

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(fileTemplate);
        writer.close();
    }
}
