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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class ImageReceiver extends MessageDispatcher {
    boolean isLarge;
    ImageReceiver(String code, Logger logger, String destination) {
        super(code, logger, destination);
    }
    
    @Override
    public void sendRequest() {
        try {
            File file = new File("image" + new Date().toString() + ".jpg");
            FileOutputStream stream = new FileOutputStream(file);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
