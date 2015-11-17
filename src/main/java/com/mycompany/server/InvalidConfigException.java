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
public class InvalidConfigException extends RuntimeException{
    public InvalidConfigException() { super(); }
    public InvalidConfigException(String err) { super(err); }
    public InvalidConfigException(String s, Throwable throwable) { super(s, throwable); }
    public InvalidConfigException(Throwable throwable) { super(throwable); }
}
