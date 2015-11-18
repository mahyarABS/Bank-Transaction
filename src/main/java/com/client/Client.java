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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 *
 * @author mahyar
 */
public class Client {
    private String serverAddr;
    private int portNumber = -1;
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
        } catch (ClientAttributesInvalidException ex) {
            logFile.log(Level.FINE, ex.getMessage());
        } catch (SAXException | ParserConfigurationException ex) {
            logFile.log(Level.FINE, "XML file has errors!");
        } catch (IOException ex) {
            logFile.log(Level.FINE, "Server connection failed!");
        }
    }
    
    private void connect() throws IOException{
        socket = new Socket(getServerAddr(), getPortNumber());
    }
    
    public void readAndSetConfig() throws IOException, SAXException, ParserConfigurationException{	
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse("terminal.xml");
            
            Element terminal = doc.getDocumentElement();
            Element outLog = (Element) terminal.getElementsByTagName("outLog").item(0);
            openLogFile(outLog);
            
            setId(terminal.getAttribute("id"));
            setType(terminal.getAttribute("type"));
            
            Element server = (Element) terminal.getElementsByTagName("server").item(0);
            setServerAttributes(server);
            
            requests = doc.getElementsByTagName("transaction");
    }
    
    private void sendRequestsAndHandleResponses() {
        for (int temp = 0; temp < requests.getLength(); temp++){
            parseAndSendRequest(temp);
            handleResponse(temp);
        }
    }
    
    private void setServerAttributes(Element server) {
        setServerAddr(server.getAttribute("ip"));
        setPortNumber(Integer.parseInt(server.getAttribute("port")));
    }
    
    private void openLogFile(Element outLog) throws IOException {
        String logFileName = outLog.getAttribute("path");
        FileHandler logFileHandler = new FileHandler(logFileName, true);
        logFileHandler.setFormatter(new ClientFormatter());
        logFile.addHandler(logFileHandler);
        logFile.setLevel(Level.ALL);
    }
    
    private void setServerAddr(String address) {
        if (address.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
            serverAddr = address;
        else
            throw new ClientAttributesInvalidException("The IP address is wrong!");
    }
    
    private String getServerAddr() {
        if(serverAddr != null)
            return serverAddr;
        else
            throw new ClientAttributesInvalidException("Server address is not set!");
    }
    
    private void setPortNumber(int port) {
        if(port >= 0 && port <= 65536)
            portNumber = port;
        else
            throw new ClientAttributesInvalidException("The port number is out or range!");
    }
    
    private int getPortNumber() {
        if(portNumber != -1)
            return portNumber;
        else
            throw new ClientAttributesInvalidException("The port number is not set!");
    }
    
    private void setId(String id) {
        if(id != null && !"".equals(id))
            this.id = id;
        else
            throw new ClientAttributesInvalidException("ID prameter is not given or is null!");
    }
    
    private String getId() {
        if(id != null)
            return id;
        else
            throw new ClientAttributesInvalidException("ID is not set!");
    }
    
    private void setType(String type) {
        if(type != null && !"".equals(type))
            this.type = type;
        else
            throw new ClientAttributesInvalidException("Type is not given!");
    }
    
    private String getType() {
        if(type != null)
            return type;
        else
            throw new ClientAttributesInvalidException("Type is not set!");
    }
    
    private void parseAndSendRequest(int requestNumber) {
        Element transaction = (Element) requests.item(requestNumber);
        String id = transaction.getAttribute("id");
        String type = transaction.getAttribute("type");
        String amount = transaction.getAttribute("amount");
        String deposit = transaction.getAttribute("deposit");
        sendRequest(id, type, amount, deposit);
    }
    
    private void sendRequest(String id, String type, String amount, String deposit) {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("id", id);
        jsonMessage.put("type", type);
        jsonMessage.put("amount", amount);
        jsonMessage.put("deposit", deposit);
        requestsSender.println(jsonMessage.toString());
    }
    
    private void handleResponse(int requestNumber) {
        String response = null;
        try {
            if((response = serverResponsesReader.readLine()) != null) {
                writeResponseToXMLFile(response, requestNumber);
                logFile.log(Level.FINE, "transaction with id : " + ((Element)requests.item(requestNumber)).getAttribute("id") + " completed successfully");
            }
        } catch (TransformerException | ParserConfigurationException | SAXException | IOException ex) {
            logFile.log(Level.FINE, "response.xml file curropted!");
        }
    }
    
    private void writeResponseToXMLFile(String response, int requestNumber) throws TransformerException, ParserConfigurationException, SAXException, IOException{
        JSONObject newResponse = (JSONObject) JSONSerializer.toJSON(response);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        File xmlFile = new File("response.xml");
        Document doc;
        Element rootElement;
        if(xmlFile.exists()) {
            doc = docBuilder.parse(xmlFile);
            rootElement = doc.getDocumentElement();
        }
        else {
            doc = docBuilder.newDocument();
            rootElement = doc.createElement("transactions");
            doc.appendChild(rootElement);
        }
        
        Element transaction = (Element) requests.item(requestNumber);      

        Element status = transaction.getOwnerDocument().createElement("status");        
        status.appendChild(transaction.getOwnerDocument().createTextNode(newResponse.getString("status")));
        transaction.appendChild(status);

        Element message = transaction.getOwnerDocument().createElement("message");
        message.appendChild(transaction.getOwnerDocument().createTextNode(newResponse.getString("message")));
        transaction.appendChild(message);
        rootElement.appendChild(doc.importNode(transaction, true));
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(xmlFile);

        transformer.transform(source, result);
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
