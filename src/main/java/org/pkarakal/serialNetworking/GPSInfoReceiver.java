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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class GPSInfoReceiver extends MessageDispatcher{
    boolean receiveImage;
    HashMap<Boolean, String> fileName;
    boolean readFromFile;
    public GPSInfoReceiver(String code, Logger logger, String destination, boolean isImage, boolean fromFile) {
        super(code, logger, destination);
        this.receiveImage = isImage;
        this.readFromFile = fromFile;
        this._createImageHashMap();
    }
    
    @Override
    public void sendRequest() {
        if (!this.receiveImage) {
            textReply();
        } else {
            try{
                imageReply();
            } catch (IOException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }
    
    private void _createImageHashMap() {
        this.fileName = new HashMap<>(2);
        this.fileName.put(true, "image_gps" + new Date() + ".jpg");
        this.fileName.put(false, "gps.csv");
    }
    
    private void textReply() {
        File gps = new File(fileName.get(this.receiveImage));
        FileWriter outputFile;
        FileReader inputFile = null;
        CSVWriter writer = null;
        try {
            outputFile = new FileWriter(gps);
            inputFile = new FileReader(gps);
            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(outputFile);
        } catch ( IOException e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        boolean isOpen = this.modem.open(this.destination);
        if(isOpen) {
            StringBuilder response = new StringBuilder();
            boolean res = this.modem.write(this.code.getBytes());
            if (!res) {
                logger.severe("Couldn't send message to server");
                System.exit(-1);
            }
            Vector<String> line = new Vector<>();
            while(true){
                int currentValue = modem.read();
                if (currentValue == -1) {
                    logger.severe("Connection Closed");
                    break;
                }
                response.append((char) currentValue);
                if(response.toString().contains("START ITHAKI GPS TRACKING\r\n")){
                    assert writer != null;
                    line.add(String.valueOf((char) currentValue));
                    if(line.contains("\r") && line.contains("\n")){
                        String finResult = "";
                        for(String i: line)
                            finResult = finResult.concat(i);
                        finResult = finResult.replaceAll("[\\r\\n]+", "");
                        writer.writeNext(finResult.split(","));
                        line.clear();
                    }
                }
                if(response.toString().contains("STOP ITHAKI GPS TRACKING\r\n")) {
                    assert inputFile != null;
                    try {
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    CSVReader reader = new CSVReader(inputFile);
                    List<String[]> allElements = null;
                    try {
                        allElements = reader.readAll();
                        allElements.remove(allElements.size()-1);
                        FileWriter w = new FileWriter(gps);
                        CSVWriter writer1 = new CSVWriter(w);
                        writer1.writeAll(allElements);
                        writer1.flush();
                        writer1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void imageReply() throws IOException {
        File gps = new File(fileName.get(this.receiveImage));
        FileOutputStream stream = new FileOutputStream(gps);
        if(this.readFromFile)
            this.handleCoordsFromFile();
        boolean isOpen = this.modem.open(this.destination);
        if(isOpen){
            boolean res = this.modem.write(this.code.getBytes());
            if(!res) {
                logger.severe("Couldn't send message to server");
                System.exit(-1);
            }
            boolean firstValue = true;
            int previousValue;
            int currentValue =0;
            while(true) {
                previousValue = currentValue;
                currentValue= modem.read();
                if (currentValue == -1) {
                    logger.severe("Connection Closed");
                    break;
                }
                if (currentValue == 0xD8 && previousValue == 0xFF){
                    stream.write((char)previousValue);
                    stream.write((char)currentValue);
                    firstValue = false;
                }else if (currentValue == 0xD9 && previousValue == 0xFF){
                    stream.write((char)previousValue);
                    stream.write((char)currentValue);
                    break;
                } else if(!firstValue) {
                    stream.write((char)currentValue);
                }
            }
            stream.flush();
            stream.close();
            modem.close();
        }
    }
    
    private void handleCoordsFromFile(){
        System.out.println("Here");
        FileReader inputFile = null;
        CSVReader reader = null;
        File gps_csv = new File(fileName.get(false));
        try {
            inputFile = new FileReader(gps_csv);
            reader = new CSVReader(inputFile);
            List<String[]> allElements = reader.readAll();
            List<String[]> filteredItems = this.searchForCoordinates(allElements);
            ArrayList<String> finResult = this.createCoordsFromFile(filteredItems);
            System.out.println(filteredItems);
            this.createCode(finResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private List<String[]> searchForCoordinates(List<String[]> elements) {
        Predicate<String[]> filteredElements = element -> element[0].toUpperCase(Locale.ROOT).equals("$GPRMC".toUpperCase(Locale.ROOT));
        return elements.stream().filter(filteredElements).collect(Collectors.toList());
    }
    
    private ArrayList<String> createCoordsFromFile(List<String[]> elements){
        ArrayList<String> coords = new ArrayList<>();
        for (String[] elem: elements) {
            String latitude = String.join("", elem[3].split("\\."));
            latitude = latitude.length() > 6 ? latitude.substring(0, 6) : latitude;
            String longitude = String.join("", elem[5].split("\\."));
            longitude = longitude.length() > 6 ? longitude.substring(0, 6) : longitude;
            coords.add(latitude.concat(longitude));
        }
        return coords;
    }
    
    private void createCode(ArrayList<String> elements) {
        String code ="";
        code = this.code.split("\\r")[0];
        for (String item: elements) {
            code = code.concat("T=".concat(item));
        }
        this.code = code.concat("\r");
    }
}
