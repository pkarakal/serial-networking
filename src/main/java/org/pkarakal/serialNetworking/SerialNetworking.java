package org.pkarakal.serialNetworking;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SerialNetworking {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("networking");
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("./networking.log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.info("Application started");
    }
}
