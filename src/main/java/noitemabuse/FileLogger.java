/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import java.io.*;

/**
 * PrintWriter wrapper class
 * @author ruan
 */
public class FileLogger {
    private final PrintWriter writer;

    public FileLogger(File file) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        writer = new PrintWriter(new FileWriter(file, true));
    }

    public FileLogger(String filename) throws IOException {
        this(new File(filename));
    }

    public void close() {
        writer.close();
    }

    public void log(String string) {
        println(string);
    }

    private void println(String str) {
        writer.println(str);
        writer.flush();
    }
}
