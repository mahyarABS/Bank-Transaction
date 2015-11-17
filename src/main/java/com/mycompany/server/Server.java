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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
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
import java.util.logging.SimpleFormatter;
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
        readAndSetConfig();
        ServerSocket listener = null;
        try {
            (new Thread(new InputHandler())).start();
            listener = new ServerSocket(getPortNumber());
            while (true) {
                Socket socket = listener.accept();
                Thread newClient = new Thread(new RequestHandler(socket));
                newClient.start();
                logFile.log(Level.FINE, "Client with Thread ID : {0} connected.", newClient.getId());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (listener != null) {
                    listener.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void readAndSetConfig() {
        try {
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
        } catch (FileNotFoundException ex) {
            System.out.println("The file does not exist!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (UpperBoundExceedException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String readFileToString(String filename) throws FileNotFoundException {
        String json;
        try (Scanner data = new Scanner(new File(filename)).useDelimiter("\\Z")) {
            json = data.next();
        }
        return json;
    }

    private void addInitialDepositsToHashMap(JSONArray depositJsonArray) throws IOException {
        for (int i = 0; i < depositJsonArray.size(); i++) {
            JSONObject deposit = depositJsonArray.getJSONObject(i);
            String customer = deposit.getString("customer");
            String id = deposit.getString("id");
            String initialBalance = deposit.getString("initialBalance");
            String upperBound = deposit.getString("upperBound");
            deposits.put(id, new Deposit(customer, id, initialBalance, upperBound));
        }
    }

    private void setPortNumber(int port) throws IOException {
        if (port >= 0 && port <= 65536) {
            portNumber = port;
        } else {
            throw new IOException("The port number is out or range!");
        }
    }

    private int getPortNumber() throws IOException {
        if (portNumber == -1) {
            throw new IOException("Port number is not set");
        }
        return portNumber;
    }

    private class RequestHandler implements Runnable {

        private Socket clientSocket;
        private BufferedReader clientRequestsReader;
        private PrintWriter responsesSender;

        public RequestHandler(Socket socket) throws IOException {
            try {
                clientSocket = socket;
                clientRequestsReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                responsesSender = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                throw new IOException("Client reader or writer cannot be created!" + socket.getPort());
            }
        }

        private void handleRequests() {
            String request = null;
            while (true) {
                try {
                    if ((request = clientRequestsReader.readLine()) != null) {
                        System.err.println(request);
                        JSONObject newRequest = (JSONObject) JSONSerializer.toJSON(request);
                        String id = newRequest.getString("deposit");
                        Deposit deposit = (Deposit) deposits.get(id);
                        if ("deposit".equals(newRequest.getString("type"))) {
                            deposit.addBalanceToDeposit(newRequest.getString("amount"));
                        } else if ("withdraw".equals(newRequest.getString("type"))) {
                            deposit.withdraw(newRequest.getString("amount"));
                        }
                        responsesSender.println("ok");
                    }
                } catch (UpperBoundExceedException ex){
                    System.err.println(ex.getMessage());
                } catch (BalanceIsNotEnoughException ex){
                    System.err.println(ex.getMessage());
                } catch (IOException ex) {
                    responsesSender.println("error");
                    System.err.println("error");
                } finally {

                }
            }
        }

        @Override
        public void run() {
            handleRequests();
        }
    }

    private void sync() throws IOException {
        try (PrintWriter coreJSON = new PrintWriter("core.json")) {
            coreJSON.print("{ \n  \"port\":8080,\n  \"deposits\":[\n");
            System.err.println(logFileName);
            Iterator<String> depositsIterator = deposits.keySet().iterator();
            while (depositsIterator.hasNext()) {
                String id = depositsIterator.next();
                Deposit deposit = (Deposit) deposits.get(id);
                coreJSON.print("{\n  \"customer\":\"" + deposit.getCustomerName() + "\",\n  \"id\":\""
                        + deposit.getId() + "\",\n  \"initialBalance\":\"" + deposit.getBalanceInString()
                        + "\",\n  \"upperBound\":\"" + deposit.getUpperBoundInString() + "\"\n}");
                if (depositsIterator.hasNext()) {
                    coreJSON.print(",");
                }
                coreJSON.print("\n");
            }
            coreJSON.print("],\n  \"outLog\":\"" + logFileName + "\"\n}");
            coreJSON.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
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
                        }
                    }
                } catch (IOException io) {
                    io.printStackTrace();
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
