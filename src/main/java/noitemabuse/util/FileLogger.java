/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PrintWriter wrapper class
 * @author ruan
 */
public class FileLogger {
    private SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
    private Date date = new Date();
    private final PrintWriter writer;
    private Thread thread = new Thread(new Flusher());
    private final Object lock = new Object();

    public FileLogger(File file) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        writer = new PrintWriter(new FileWriter(file, true));
        thread.start();
    }

    public FileLogger(String filename) throws IOException {
        this(new File(filename));
    }

    public void close() {
        writer.close();
        thread.interrupt();
    }

    public void log(String string) {
        date.setTime(System.currentTimeMillis());
        String timestamp = format.format(date);
        println(timestamp + string);
    }

    private void println(String str) {
        writer.println(str);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    class Flusher implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (lock) {
                        lock.wait();
                    }
                    writer.flush();
                }
            } catch (InterruptedException ex) {} finally {
                writer.close();
            }
        }
    }
}
