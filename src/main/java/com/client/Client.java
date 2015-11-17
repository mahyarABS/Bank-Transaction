/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import com.mycompany.server.Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import net.sf.json.JSONObject;
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
    private static final Logger logFile = Logger.getLogger(Server.class.getName());
    private NodeList requests;
    public Client(){
    }
    
    public void configAndConnectAndSendRequests(){
        try{
            readAndSetConfig();
            connect();
            serverResponsesReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestsSender = new PrintWriter(socket.getOutputStream(), true);
            sendRequestsAndHandleResponses();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private void connect() throws Exception{
        socket = new Socket(getServerAddr(), getPortNumber());
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
    
    private void sendRequestsAndHandleResponses() throws Exception{
        for (int temp = 0; temp < requests.getLength(); temp++){
            parseAndSendRequest(temp);
            handleResponse();
        }
    }
    
    private void setServerAttributes(Element server) throws IOException{
        setServerAddr(server.getAttribute("ip"));
        setPortNumber(Integer.parseInt(server.getAttribute("port")));
    }
    
    private void openLogFile(Element outLog) throws IOException{
        String logFileName = outLog.getAttribute("path");
        FileHandler logFileHandler = new FileHandler(logFileName, true);
        logFileHandler.setFormatter(new ClientFormatter());
        logFile.addHandler(logFileHandler);
        logFile.setLevel(Level.ALL);
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
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("id", id);
        jsonMessage.put("type", type);
        jsonMessage.put("amount", amount);
        jsonMessage.put("deposit", deposit);
        requestsSender.println(jsonMessage.toString());
    }
    
    private void handleResponse(){
        String response = null;
        try {
            if((response = serverResponsesReader.readLine()) != null){
                System.err.println(response);
                logFile.log(Level.FINE, "transaction completed successfully");
            }
        } catch (IOException ex) {
            System.err.println("error");
        }
    }
    
    private class ClientFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            Date date = new Date();
            SimpleDateFormat dateFromatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss:SSS a zzz");
            String message = dateFromatter.format(date) + " Thread ID :" + Thread.currentThread().getId()
                    + "\n" + record.getMessage() + "\n";
            return message;
        }

    }
}
