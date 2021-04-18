/*
 * MIT License
 *
 * Copyright (c) 2021 Pavlos Karakalidis <pavloc.kara@outlook.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.pkarakal.serialNetworking;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SerialNetworking {
    private final static Options options = new Options();
    
    static {
        options.addRequiredOption("i", "ithaki-destination", true, "Define the destination of the modem. Accepted values are ithaki, ithakicopter");
        options.addRequiredOption("r", "request-code", true, "Define the request code");
        options.addRequiredOption("j", "job", true, "Define the job to execute. The valid parameters are echo, image, gps, ack");
        options.addOption("m", "CAM", true, "Define one of two cameras: FIX, PTZ, or number like XX");
        options.addOption("d", "DIR", true, "Define the direction of the camera. Accepted values are U,D,L,R,C,M");
        options.addOption("s", "SIZE", true, "Define the size of the picture. Accepted values are S, L");
        options.addOption("p", "pre-saved-route", true, "Define a predefined route.");
        options.addOption("g", "google-maps-image", true, "Define longitude and latitude for the location");
        options.addOption("f", "file-input", false, "Define if you want to take the coordinates from file. This is a flag and shouldn't be given arguments");
        options.addOption("a", "ack", true, "Define the ack code to send to server on correct packet receiving.");
        options.addOption("n", "nack", true, "Define the ack code to send to server on incorrect packet receiving.");
    }
    
    public static void main(String[] args) throws Exception {
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
        if (args.length > 1) {
            final CommandLineParser parser = new DefaultParser();
            try {
                final CommandLine cmd = parser.parse(options, args);
                String code = cmd.getOptionValue("r");
                String job = cmd.getOptionValue("j");
                if (cmd.getOptionValue("j").equals("image")) {
                    if (cmd.hasOption("m")) {
                        code = code.concat(" CAM=").concat(cmd.getOptionValue("m"));
                    }
                    if (cmd.hasOption("m") && cmd.getOptionValue("m").equals("PTZ")
                                && cmd.hasOption("d")) {
                        code = code.concat(" DIR=").concat(cmd.getOptionValue("d"));
                    }
                    if (cmd.hasOption("f") && cmd.getOptionValue("f").equals("ON")) {
                        code = code.concat(" FLOW=").concat(cmd.getOptionValue("f"));
                    }
                    if (cmd.hasOption("s")) {
                        code = code.concat(" SIZE=").concat(cmd.getOptionValue("s"));
                    }
                }
                if (cmd.hasOption("p") && cmd.getOptionValue("j").toUpperCase(Locale.ROOT).equals("gps".toUpperCase(Locale.ROOT))) {
                    code = code.concat("R=").concat(cmd.getOptionValue("p"));
                }
                if (cmd.hasOption("g") && cmd.getOptionValue("j").toUpperCase(Locale.ROOT).equals("gps".toUpperCase(Locale.ROOT))) {
                    code = code.concat("T=").concat(cmd.getOptionValue("g"));
                }
                code = code.concat("\r");
                Request receiver = null;
                switch (job) {
                    case "echo":
                        logger.info("Starting echo functionality");
                        receiver = new MessageDispatcher(code, logger, cmd.getOptionValue("i"));
                        break;
                    case "image":
                        logger.info("Starting image receiving functionality");
                        receiver = new ImageReceiver(code, logger, cmd.getOptionValue("i"));
                        break;
                    case "gps":
                        logger.info("Starting gps info receiving functionality");
                        receiver = new GPSInfoReceiver(code, logger, cmd.getOptionValue("i"), cmd.hasOption("g") || cmd.hasOption("f"), cmd.hasOption("f"));
                        break;
                    case "ack":
                        logger.info("Starting echo acknowledgment receiving functionality");
                        receiver = new EchoAcknowledgement(code, logger, cmd.getOptionValue("i"), cmd.getOptionValue("a").concat("\r"), cmd.getOptionValue("n").concat("\r"));
                    default:
                        break;
                }
                assert receiver != null;
                receiver.sendRequest();
                logger.info("Job completed. Closing application");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            logger.severe("Wrong number of application parameters. Exiting...");
            throw new Exception("Wrong number of application parameters");
        }
    }
}
