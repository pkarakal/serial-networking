# Serial Network Programming in Java
This is a project developed for the [035] - Networking I class of ECE department in AUTH. 

## Prerequisites
1.  OpenJDK 11 or higher (it may work with an earlier version as well)
2.  Maven
3.  Python 3.6 + (Optional)

## Instructions
1. Clone the repository
    
   ```shell
    $ git clone git@github.com:pkarakal/serial-networking.git
    ```
2. Go to that directory
     ```shell
    $ cd serial-networking
     ```
3. Download [Java Communication API](https://www.oracle.com/java/technologies/java-archive-misc-downloads.html).
   Extract the jar and place it in the root directory of the project
4. Download ithakimodem.jar and place it under the project root directory
5. Build it using Maven
    ```shell
   $ mvn package
    ```
6. Run it by using the following instruction
    ```shell
   $ java -jar ./target/serialNetworking-1.0.jar

_Note_: The above libraries are not tracked in this project due to licensing restrictions. Make sure you
have an Oracle developer account to gain access to Java Communication API.

## Message Dispatcher
The Message Dispatcher is the base class for sending requests to server. This is used for the message requests.
For this job, it runs for 4 minutes, it sends a requests and receives an answer and logs it
to the `echo+new Date().csv` file which is created in the directory the job started.
To run this job make sure you specify 
*  `-i ithaki`
*  `-j echo`
*  `-r <request_code_from_ithaki>`


## Image Receiver
The image receiver extend the message dispatcher. It sends one request and receives many packets which 
it searches for certain key bits that signal the start and end of file for jpg files. It can also take
multiple parameters depending on the job you want to run which are listed below:
1. CAM: It takes an image from a different camera. The accepted values are `PTZ` and `FIX`
2. DIR: When CAM=PTZ is defined, the user can control the camera by moving it on XY axes.
   The accepted values are:
*  U-> Up,
*  D-> Down,
*  L-> Left,
*  R-> Right,
*  C-> Center
*  M -> Remember
3. SIZE: When size is defined it requests a certain image from the server. The accepted values are
*  L : for large photos of 640x480 resolution
*  S : for small photos of 320x240 resolution

Once the complete image gets obtained, a new file is created with .jpg format.
To run this job make sure you specify
*  `-i ithaki`
*  `-j image`
*  `-r <request_code_from_ithaki>`
*  `-m <camera_to_use>`
*  `-d <direction>`
*  `-s <size_of_picture>`

## GPS Info Receiver
GPS Info Receiver extends Message Receiver. It can either obtain text data which after send the necessary code,
it receives packets which it uses to write to the output to a csv file, or it can also receive images
which it writes to .jpg files It can also take multiple parameters depending on the job you want to run 
which are listed below:
1. R: It is used to return a pre-saved route. Should be of format `ΧPPPPLL` where X is the route,
   `PPPP` is the start index and LL is the number of nodes to send.
2. T: it can be repeated up to 9 times and is used to fetch up to 9 traces on a Google Maps image 
   based on the coordinates defined. as the param. They should be of format `AABBCCDDEEFF`

To run this job make sure you specify
*  `-i ithaki`
*  `-j gps`
*  `-r <request_code_from_ithaki>`
*  `-p <parameters_to_R>`
*  `-g <google_maps_image_coords>`
*  `-f` if you want to parse the gps csv file to get the coordinates from there instead of passing
   them as a param. Make sure a gps csv file exists
   
## Echo Acknowledgement
Echo Acknowledgement extends Message Dispatcher and only differs from it, in the part that it 
checks the XOR result of the message inside <> against the 3-digit number next to it. If they match
it sends the acknowledgement code to receive a message again. If they don't, it sends the 
non-acknowledgement code to receive that image again. This also increases the value of the errors 
variable which is also written to the csv file.
To run this job make sure you specify
*  `-i ithaki`
*  `-j ack`
*  `-r <request_code_from_ithaki>`
*  `-a <ack_code>`
*  `-n <nack_code>`

## Visualization
To visualize all the data received from the server there is also a python package created to parse
and extract data from those file and output it a graphs to the user. 
It plots the duration of each request and calculates the mean duration, it also calculates the BER
in the case of ack job, which are also displayed in the plots. Finally it also shows the images 
received. To cycle through them, when opened, press right arrow on keyboard. 

To run the visualization package
*  create a new virtual environment: 
   ```shell
   $ virtualenv ./serial-networking
   ```
*  Activate virtualenv if not activated on creation
   ```shell
   $ source serial-networking/bin/activate
   ```
*  install the dependencies
   ```shell
   $ pip install -r requirements.txt
   ```
*  run the package from project root directory
   ```shell
   $ python -m visualization
   ```
