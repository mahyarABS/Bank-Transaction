/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

/**
 *
 * @author mahyar
 */
public class ServerRequestHandlerException extends RuntimeException{
    public ServerRequestHandlerException() { super(); }
    public ServerRequestHandlerException(String err) { super(err); }
    public ServerRequestHandlerException(String s, Throwable throwable) { super(s, throwable); }
    public ServerRequestHandlerException(Throwable throwable) { super(throwable); }
}
