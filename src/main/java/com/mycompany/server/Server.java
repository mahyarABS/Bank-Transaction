/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
/**
 *
 * @author mahyar
 */
public class Server {
    private ServerSocket listener;
    private static Server server = new Server();
    private Server(){
        start();
    }
    
    public static Server getInstance(){
        return server;
    }
    
    private void start(){    
       try{ 
            listener = new ServerSocket(9091);
            while (true) {
                Socket socket = listener.accept();
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        while(true){
                            handleRequests();
                        }
                    }
                }); 
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                    out.println(new Date().toString());
            }
        }
        catch(Exception e){
            
        }
    }
    
    private void handleRequests(){
        
        
    }
    
    public void close() throws IOException{
        listener.close();
    }
}
