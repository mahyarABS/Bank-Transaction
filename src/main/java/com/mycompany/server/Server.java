/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
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
    private PrintWriter logfile = null;
    private Map deposits = null;
    private Server(){
        start();
    }
    
    public static Server getInstance(){
        return server;
    }
    
    private void start(){  
       readAndSetConfig();
       ServerSocket listener = null;
       try{ 
            listener = new ServerSocket(getPortNumber());
            while (true) {
                Socket socket = listener.accept();
                new RequestHandler(socket).run();   
            }
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        finally{
           try {
            if(listener != null)
                listener.close();
           } catch (IOException ex) {
               Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
    }
    
    public void readAndSetConfig(){
        try {
            String json = readFileToString("core.json");
            JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( json ); 
            setPortNumber(jsonObject.getInt("port"));
            JSONArray jsonArray = jsonObject.getJSONArray("deposits");
            deposits = new HashMap();
            for(int i = 0 ; i < jsonArray.size() ; i++){
                JSONObject deposit = jsonArray.getJSONObject(i);
                String customer = deposit.getString("customer");
                String id = deposit.getString("id");
                String initialBalance = deposit.getString("initialBalance");
                String upperBound = deposit.getString("upperBound");
                System.out.println("data = " + customer + " " + id + " " + initialBalance + " " + upperBound);
                deposits.put(id, new Deposit(customer, id, initialBalance, upperBound));
            }
            logfile = new PrintWriter(jsonObject.getString("outLog"), "UTF-8");
          
        } catch (FileNotFoundException ex) {
            System.out.println("The file does not exist!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public String readFileToString(String filename) throws FileNotFoundException{
        Scanner data = new Scanner(new File(filename)).useDelimiter("\\Z");
        String json = data.next();
        data.close();
        return json;
    }

     private void setPortNumber(int port) throws IOException{
        if(port >= 0 && port <= 65536)
            portNumber = port;
        else
            throw new IOException("The port number is out or range!");
    }
    
    private int getPortNumber() throws IOException{
        if(portNumber == -1)
            throw new IOException("Port number is not set");
        return portNumber;
    }
    
    private class RequestHandler implements Runnable{
        private Socket clientSocket;
        public RequestHandler(Socket socket){
            clientSocket = socket;
        }
        
        private void handleRequests(){
            PrintWriter out = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(new Date().toString());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                out.close();
            }
        }
        
        @Override
        public void run(){
            handleRequests();
        }
    }
}
