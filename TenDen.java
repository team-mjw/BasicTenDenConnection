/*
Team MJW
ICS414
Spring 2019

Basic Connection to Light Bulb
1) Send an HTTP Request to light bulb using the Multi-cast Address (239.255.250.250) and port # 1982
2) Receive HTTP Response, which will contain all the light bulb device information including the IP address
3) Using the IP address obtained by the previous step, connect to the light bulb
4) Ready for any commands, such as toggle, set_power, etc.
*/

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.DataOutputStream; 
import java.util.Scanner;

public class TenDen {

    //*****STEP 1: obtain the Current IP Address of the Light Bulb
    public InetAddress initialUDPConnection() throws Exception {
 
        System.out.println("*****Searching for Device*****\n");

        //String of the HTTP Request to Light Bulb
        String requestString = "M-SEARCH * HTTP/1.1\r\n" + "HOST:239.255.255.250:1982\r\n" + "MAN:\"ssdp:discover\"\r\n" + "ST:wifi_bulb\r\n";
        
        //Convert HTTP request to byte array
        byte [] httpRequest = requestString.getBytes();
        int httpRequestLength = httpRequest.length;

        //Create a Socket for data transfer of HTTP Request and Response
        DatagramSocket socket = new DatagramSocket();

        //Change the IP address from a String to a InetAddress
        InetAddress address = InetAddress.getByName("239.255.255.250");
        //Port Number of HOST (239.255.255.250)
        int portNumber = 1982;

        //Container for the HTTP Response
        byte[] receiveData = new byte[2048];

        //Prepare the HTTP Request packet
        DatagramPacket sendPacket = new DatagramPacket(httpRequest, httpRequestLength, address, portNumber);

        //Send the HTTP request packet to the socket
        socket.send(sendPacket);

        //Setup the packet for the Response HTTP packet
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        //Store the data of the HTTP Response in the Response HTTP packet
        socket.receive(receivePacket);

        //Convert the Data in the Response Packet to String form for ip address extraction
        String receiveString = new String(receivePacket.getData());

        //Close the socket connection
        socket.close();

        //Obtain the IP address from HTTP Response Packet
        receiveString = receiveString.substring(receiveString.lastIndexOf("//") + 2);
        receiveString = receiveString.substring(0, receiveString.indexOf("\n"));
        receiveString = receiveString.substring(0, receiveString.indexOf(":"));

        //Convert string ip address to InetAddress form
        InetAddress finalIpAddress = InetAddress.getByName(receiveString);

        System.out.println("*****Device Discovered*****\n");

        //Return the ip address of discovered light bulb
        return finalIpAddress;
    }

    //Step 2: Initiate TCP Connection and Run functions with lightBulb
    public void tcpConnection (InetAddress location) throws Exception {
        //User Input
        String userInput = "";

        //Create a tcp socket for data transfer
        Socket tcpSocket = new Socket(location, 55443);

        //Transfer the Data using the socket to the light bulb
        DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());

        //loop until userInput is "Off"
        do {
            //prompt the user to input one of the commands
            System.out.println("Please type one of the available commands: on, off, toggle, brightness, temperature, flicker");
            Scanner s = new Scanner(System.in);
            userInput = s.nextLine();

            //for user inputs
            int brightValue = 0;
            int ctValue = 0;

            //toggle the power
            if(userInput.equals("toggle")) {
                out.writeBytes("{\"id\":1,\"method\":\"toggle\",\"params\":[]}\r\n");
            //set brightness level using input from user
            } else if(userInput.equals("brightness")) {
                System.out.print("Input Brightness Level (-100 to 100): ");
                brightValue = s.nextInt();
                out.writeBytes("{\"id\":1,\"method\":\"adjust_bright\",\"params\":[" + brightValue + ", 1000]}\r\n");
            //set color temperature level using input from user
            } else if(userInput.equals("temperature")) {
                System.out.print("Input Color Temperature Level (1700 to 4000): ");
                ctValue = s.nextInt();
                out.writeBytes("{\"id\":1,\"method\":\"set_ct_abx\",\"params\":[" + ctValue + ", \"smooth\", 50]}\r\n");
            //flicker the light
            } else if(userInput.equals("flicker")) { //duration (ms), 2 for color temperature, CT value, brightness 1~100
                out.writeBytes("{\"id\":1,\"method\":\"start_cf\",\"params\":[5, 0, \"500, 2, 2700, 100, 500, 2, 4000, 25, 500, 2, 1500, 100, 500, 2, 4000, 25, 500, 2, 2700, 100\"]}\r\n");
            //turn off the bulb and end the program
            } else if(userInput.equals("off")){
                out.writeBytes("{\"id\": 1, \"method\": \"set_power\", \"params\":[\"off\", \"smooth\", 500]}\r\n");
                Thread.sleep(10);
            //turn on the light bulb
            } else if(userInput.equals("on")){
                out.writeBytes("{\"id\": 1, \"method\": \"set_power\", \"params\":[\"on\", \"smooth\", 500]}\r\n");
            //command not found
            } else {
                System.out.println("********Command Not Found!*********");
            }

            System.out.println();

        } while(!userInput.equals("off"));

        tcpSocket.close();

    }

    //Driver
    public static void main(String args[]) throws Exception {
        TenDen lightBulb1 = new TenDen();

        InetAddress ipAddress = lightBulb1.initialUDPConnection();

        lightBulb1.tcpConnection(ipAddress);

        System.out.println("See you Later!");
    }

}