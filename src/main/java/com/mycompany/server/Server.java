/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author mahyar
 */
public class Server {
    private static Server server = new Server();
    private int portNumber = -1;
    private static final Logger logFile = Logger.getLogger(Server.class.getName());
    private String logFileName = null;
    private Map deposits = new ConcurrentHashMap();

    private Server() {
    }

    public static Server getInstance() {
        return server;
    }

    public void start() {
        try{
            readAndSetConfig();
            try (ServerSocket listener = new ServerSocket(getPortNumber())) {
                (new Thread(new InputHandler())).start();
                while (true) {
                    Socket socket = listener.accept();
                    Thread newClient = new Thread(new RequestHandler(socket));
                    newClient.start();
                    logFile.log(Level.FINE, "Client with Thread ID : " + newClient.getId() + " connected.");
                }
            } catch (IOException ex) {
                logFile.log(Level.FINE, "Server failed to start!\nlistener failed.");
            }
        } catch (ServerRequestHandlerException ex) {
            logFile.log(Level.FINE, ex.getMessage());
        } catch (InvalidConfigException ex) {
            logFile.log(Level.FINE, "Server failed to start!\nConfig file has errors.\n" + ex.getMessage() + ".");
        } catch (IOException ex) {
            logFile.log(Level.FINE, "Server failed to start!\nJSON file has errors.");
        }
    }

    public void readAndSetConfig() throws IOException {
        String json = readFileToString("core.json");
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(json);
        setPortNumber(jsonObject.getInt("port"));
        JSONArray depositJsonArray = jsonObject.getJSONArray("deposits");
        addInitialDepositsToHashMap(depositJsonArray);
        logFileName = jsonObject.getString("outLog");
        FileHandler logFileHandler = new FileHandler(logFileName, true);
        logFileHandler.setFormatter(new ServerFormatter());
        logFile.addHandler(logFileHandler);
        logFile.setLevel(Level.ALL);
    }

    private String readFileToString(String filename) throws FileNotFoundException {
        String json;
        try (Scanner data = new Scanner(new File(filename)).useDelimiter("\\Z")) {
            json = data.next();
        }
        return json;
    }

    private void addInitialDepositsToHashMap(JSONArray depositJsonArray) {
        for (int i = 0; i < depositJsonArray.size(); i++) {
            JSONObject deposit = depositJsonArray.getJSONObject(i);
            String customer = deposit.getString("customer");
            String id = deposit.getString("id");
            String initialBalance = deposit.getString("initialBalance");
            String upperBound = deposit.getString("upperBound");
            try {
                deposits.put(id, new Deposit(customer, id, initialBalance, upperBound));
            } catch (UpperBoundExceedException | UpperBoundIsNotValidException | BalanceIsNotEnoughException | BalanceIsNotValidException | DepositIdentificationException ex) {
                logFile.log(Level.FINE, "Deposit with id " + id + " could not be added input variables are invalid or not set.\n" + ex.getMessage());
            }
        }
    }

    private void setPortNumber(int port) {
        if (port >= 0 && port <= 65536)
            portNumber = port;
        else 
            throw new InvalidConfigException("The port number is out or range!");
    }

    private int getPortNumber() {
        if (portNumber == -1) {
            throw new InvalidConfigException("Port number is not set");
        }
        return portNumber;
    }

    private class RequestHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader clientRequestsReader;
        private PrintWriter responsesSender;

        public RequestHandler(Socket socket) {
            try {
                clientSocket = socket;
                clientRequestsReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                responsesSender = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                throw new ServerRequestHandlerException("Client reader or writer cannot be created!" + socket.getPort());
            }
        }

        private void handleRequests() {
            String request = null;
            String transactionId = null;
            String id = null;
            String type = null;
            while (true) {
                try {
                    if ((request = clientRequestsReader.readLine()) != null) {
                        System.err.println(request);
                        JSONObject newRequest = (JSONObject) JSONSerializer.toJSON(request);
                        transactionId = newRequest.getString("id");
                        id = newRequest.getString("deposit");
                        Deposit deposit = (Deposit) deposits.get(id);
                        type = newRequest.getString("type");
                        if ("deposit".equals(type)) {
                            deposit.addBalanceToDeposit(newRequest.getString("amount"));
                        } else if ("withdraw".equals(type)) {
                            deposit.withdraw(newRequest.getString("amount"));
                        }
                        sendSuccessResponse("Deposit successfully updated.\nNew balance amount is : " + deposit.getBalanceInString());
                        logFile.log(Level.FINE, "The transaction with id : " + transactionId + " succeeded for deposit id : " + id + " request type : " + type + " client IP : " + clientSocket.getInetAddress() + ". \n" + "Deposit successfully updated.\nNew balance amount is : " + deposit.getBalanceInString());
                    }
                } catch (UpperBoundExceedException | UpperBoundIsNotValidException | BalanceIsNotEnoughException | BalanceIsNotValidException | IOException ex) {
                    logFile.log(Level.FINE, "The transaction with id : " + transactionId + " failed for deposit id : " + id + " request type : " + type + " client IP : " + clientSocket.getInetAddress() + ". \n" + ex.getMessage());
                    sendFailResponse(ex.getMessage());
                } finally {

                }
            }
        }
        
        private void sendSuccessResponse(String message) {
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("message", message);
            responsesSender.println(response.toString());
        }
        
        private void sendFailResponse(String message) {
            JSONObject response = new JSONObject();
            response.put("status", "fail");
            response.put("message", message);
            responsesSender.println(response.toString());
        }
        
        @Override
        public void run() {
            handleRequests();
        }
    }

    private void sync() throws IOException {       
        JSONObject serverData = new JSONObject();
        serverData.put("port", 8080);
        JSONArray depositsData = new JSONArray();
        Iterator<String> depositsIterator = deposits.keySet().iterator();
        while (depositsIterator.hasNext()) {
            String id = depositsIterator.next();
            Deposit deposit = (Deposit) deposits.get(id);
            JSONObject depositJSON = new JSONObject();
            depositJSON.put("customer", deposit.getCustomerName());
            depositJSON.put("id", deposit.getId());
            depositJSON.put("initialBalance", deposit.getBalanceInString());
            depositJSON.put("upperBound", deposit.getUpperBoundInString());
            depositsData.add(depositJSON);   
        }
        serverData.put("deposits", depositsData);
        serverData.put("outLog", logFileName);
        try (PrintWriter coreJSON = new PrintWriter("core.json")) {
            coreJSON.println(serverData.toString(2));
            coreJSON.close();
        }
    }

    private class InputHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String input;

                    while ((input = br.readLine()) != null) {
                        if ("sync".equals(input)) {
                            sync();
                            logFile.log(Level.FINE, "Sync command executed successfully.");
                        }
                    }
                } catch (UpperBoundIsNotValidException | BalanceIsNotValidException ex) {
                    logFile.log(Level.FINE, "Input command execution failed.\n" + ex.getMessage());
                } catch (IOException ex) {
                    logFile.log(Level.FINE, "Input command execution failed.\nFile could not be used.");
                }
            }
        }

    }

    private class ServerFormatter extends Formatter {

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
