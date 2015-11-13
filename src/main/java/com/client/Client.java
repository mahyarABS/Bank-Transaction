/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import java.io.IOException;
import java.net.Socket;


/**
 *
 * @author mahyar
 */
public class Client {
    private String serverAddr;
    private int portNumber;
    private Socket socket;
    public Client(){
        connect();
    }
    
    private void connect(){
        try{
            readConfig();
            socket = new Socket(getServerAddr(), getPortNumber());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void readConfig() throws IOException{
        setServerAddr("127.0.0.1");
        setPortNumber(9091);
    }
    
    private void setServerAddr(String address) throws IOException{
        if (address.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
            serverAddr = address;
        else
            throw new IOException("The IP address is wrong!");
    }
    
    private String getServerAddr(){
        return serverAddr;
    }
    
    private void setPortNumber(int port) throws IOException{
        if(port >= 0 && port <= 65536)
            portNumber = port;
        else
            throw new IOException("The port number is out or range!");
    }
    
    private int getPortNumber(){
        return portNumber;
    }
}
