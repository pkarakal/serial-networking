# Serial Network Programming in Java
This is a project developed for the [035] - Networking I class of ECE department in AUTH. 

## Prerequisites
1.  OpenJDK 11 or higher (it may work with an earlier version as well)
2.  Maven

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
