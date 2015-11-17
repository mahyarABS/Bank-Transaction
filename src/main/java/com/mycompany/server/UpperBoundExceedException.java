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
public class UpperBoundExceedException extends RuntimeException{
    public UpperBoundExceedException() { super(); }
    public UpperBoundExceedException(String err) { super(err); }
    public UpperBoundExceedException(String s, Throwable throwable) { super(s, throwable); }
    public UpperBoundExceedException(Throwable throwable) { super(throwable); }
}
