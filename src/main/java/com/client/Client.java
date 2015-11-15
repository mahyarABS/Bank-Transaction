/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


/**
 *
 * @author mahyar
 */
public class Client {
    private String serverAddr;
    private int portNumber;
    private Socket socket;
    private BufferedReader serverResponsesReader;
    private PrintWriter requestsSender;
    private String id = null;
    private String type = null;
    private PrintWriter logFile = null;
    private NodeList requests;
    public Client(){
        connect();
    }
    
    private void connect(){
        try{
            readAndSetConfig();
            socket = new Socket(getServerAddr(), getPortNumber());
            serverResponsesReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestsSender = new PrintWriter(socket.getOutputStream(), true);
            for (int temp = 0; temp < requests.getLength(); temp++){
                parseAndSendRequest(temp);
                handleResponse();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void readAndSetConfig() throws IOException{
        try {	
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse("terminal.xml");
            
            Element terminal = doc.getDocumentElement();
            setId(terminal.getAttribute("id"));
            setType(terminal.getAttribute("type"));
            
            Element server = (Element) terminal.getElementsByTagName("server").item(0);
            setServerAttributes(server);
            
            Element outLog = (Element) terminal.getElementsByTagName("outLog").item(0);
            openLogFile(outLog);
            
            requests = doc.getElementsByTagName("transaction");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Running Client failed!");
        } catch (Exception e) {
           
        }
    }
    
    private void setServerAttributes(Element server) throws IOException{
        setServerAddr(server.getAttribute("ip"));
        setPortNumber(Integer.parseInt(server.getAttribute("port")));
    }
    
    private void openLogFile(Element outLog) throws IOException{
        logFile = new PrintWriter(outLog.getAttribute("path"), "UTF-8");
    }
    
    private void setServerAddr(String address) throws IOException{
        if (address.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
            serverAddr = address;
        else
            throw new IOException("The IP address is wrong!");
    }
    
    private String getServerAddr() throws IOException{
        if(serverAddr != null)
            return serverAddr;
        else
            throw new IOException("Server address is not set!");
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
    
    private void setId(String id) throws IOException{
        if(id != null && !"".equals(id))
            this.id = id;
        else
            throw new IOException("ID prameter is not given or is null!");
    }
    
    private String getId() throws IOException{
        if(id != null)
            return id;
        else
            throw new IOException("ID is not set!");
    }
    
    private void setType(String type) throws IOException{
        if(type != null && !"".equals(type))
            this.type = type;
        else
            throw new IOException("Type is not given!");
    }
    
    private String getType() throws IOException{
        if(type != null)
            return type;
        else
            throw new IOException("Type is not set!");
    }
    
    private void parseAndSendRequest(int requestNumber)throws Exception{
        Element transaction = (Element) requests.item(requestNumber);
        String id = transaction.getAttribute("id");
        String type = transaction.getAttribute("type");
        String amount = transaction.getAttribute("amount");
        String deposit = transaction.getAttribute("deposit");
        sendRequest(id, type, amount, deposit);
    }
    
    private void sendRequest(String id, String type, String amount, String deposit){
        String message = "{\"id\":\"" + id + "\", \"type\":\"" + type + "\", \"amount\":\""
                + amount + "\", \"deposit\":\"" + deposit + "\"}";
        requestsSender.println(message);
    }
    
    private void handleResponse(){
        String response = null;
        try {
            if((response = serverResponsesReader.readLine()) != null)
                System.err.println(response);
        } catch (IOException ex) {
            System.err.println("error");
        }
    }
}
