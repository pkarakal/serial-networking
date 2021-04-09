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

import com.opencsv.CSVWriter;
import ithakimodem.Modem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class MessageDispatcher implements Request {
    Modem modem;
    String code;
    File echo;
    Logger logger;
    String destination;
    
    MessageDispatcher(String code, Logger logger, String destination){
        this.code = code;
        this.logger = logger;
        this.destination = destination;
        this.initModem();
    }
    
    private void initModem(){
        this.modem = new Modem();
        this.modem.setSpeed(80000);
        this.modem.setTimeout(8000);
    }
    
    @Override
    public void sendRequest() {
        boolean isOpen = this.modem.open(this.destination);
        long totalMs = (long) (4 * 60000);
        long echoStartTime = System.currentTimeMillis();
        long totalElapsedMs = 0;
        int packetCount = 0;
        this.echo = new File("echo.csv");
        FileWriter outputFile = null;
        try {
            outputFile = new FileWriter(echo);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);
            String[] headers = new String[4];
            headers[0] = "Packet";
            headers[1] = "CurrentTime";
            headers[2] = "Value";
            headers[3] = "Duration";
            logger.info("Writing table headers to csv file");
            writer.writeNext(headers);
            if (isOpen) {
                while ((totalElapsedMs = System.currentTimeMillis() - echoStartTime) < totalMs) {
                    long startTime = System.currentTimeMillis();
                    boolean res = this.modem.write(this.code.getBytes());
                    String response = "";
                    if (!res) {
                        logger.severe("Couldn't send message to server");
                        break;
                    }
                    while (true) {
                        try {
                            int k = modem.read();
                            if (k == -1) {
                                logger.severe("Connection Closed");
                                break;
                            }
                            response = response.concat(String.valueOf((char) k));
                            if (response.contains("PSTOP")) {
                                long endTime = System.currentTimeMillis();
                                long responseTime = endTime - startTime;
                                String[] values = new String[4];
                                values[0]= String.valueOf(packetCount);
                                values[1]= String.valueOf(endTime);
                                values[2]= response;
                                values[3]= String.valueOf(responseTime);
                                writer.writeNext(values);
                                ++packetCount;
                                break;
                            }
                        } catch (Exception ex) {
                            this.logger.severe(Arrays.toString(ex.getStackTrace()));
                            ex.printStackTrace();
                        }
                    }
                }
                logger.info("Took " + totalElapsedMs + " ms to get " + packetCount + "packets from server");
                logger.info("Received packets. Closing modem and file descriptors");
                modem.close();
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
