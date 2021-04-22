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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EchoAcknowledgement extends MessageDispatcher{
    String ackCode;
    String nackCode;
    int errors = 0;
    
    EchoAcknowledgement(String code, Logger logger, String destination, String ack, String nack) {
        super(code, logger, destination);
        this.ackCode = ack;
        this.nackCode = nack;
    }
    
    @Override
    public void sendRequest() {
        boolean isOpen = this.modem.open(this.destination);
        long totalMs = 4 * 60000;
        long echoStartTime = System.currentTimeMillis();
        long totalElapsedMs;
        int packetCount = 0;
        this.echo = new File("ack_echo_" + new Date() +".csv");
        FileWriter outputFile;
        try {
            outputFile = new FileWriter(echo);
            // create CSVWriter object fileWriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);
            String[] headers = new String[5];
            headers[0] = "Packet";
            headers[1] = "CurrentTime";
            headers[2] = "Value";
            headers[3] = "Duration";
            headers[4] = "Time sent";
            logger.info("Writing table headers to csv file");
            writer.writeNext(headers);
            if (isOpen) {
                boolean res = this.modem.write(this.code.getBytes());
                while ((totalElapsedMs = System.currentTimeMillis() - echoStartTime) < totalMs) {
                    long startTime = System.currentTimeMillis();
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
                                if(!checkForErrors(response)) {
                                    long endTime = System.currentTimeMillis();
                                    long responseTime = endTime - startTime;
                                    String[] values = new String[5];
                                    values[0] = String.valueOf(packetCount);
                                    values[1] = String.valueOf(endTime);
                                    values[2] = response;
                                    values[3] = String.valueOf(responseTime);
                                    values[4] = String.valueOf(this.errors+1);
                                    writer.writeNext(values);
                                    ++packetCount;
                                    this.errors=0;
                                    this.modem.write(this.ackCode.getBytes());
                                } else {
                                    ++this.errors;
                                    this.modem.write(this.nackCode.getBytes());
                                }
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
    
    private boolean checkForErrors(String response){
        Pattern messagePattern = Pattern.compile("<.*?>");
        Matcher messageMatcher = messagePattern.matcher(response);
        String message = messageMatcher.find() ?
                                 messageMatcher.group().replaceAll("[^A-Za-z0-9]", ""): "";
        Pattern codePattern = Pattern.compile("> \\d{3}");
        Matcher codeMatcher = codePattern.matcher(response);
        int code = codeMatcher.find() ? Integer.parseInt(codeMatcher.group().replaceAll("[^\\d.]", "")) : 0;
        return !(code == XOR(message));
    }
    
    private int XOR(String message) {
        if (message.length() > 0) {
            int ans = message.charAt(0);
            for (int i = 1; i < message.length(); i++)
                ans = (ans ^ (message.charAt(i)));
            return ans;
        }
        return 0;
    }
}
