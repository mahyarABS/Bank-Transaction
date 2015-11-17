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
public class UpperBoundIsNotValidException extends RuntimeException{
    public UpperBoundIsNotValidException() { super(); }
    public UpperBoundIsNotValidException(String err) { super(err); }
    public UpperBoundIsNotValidException(String s, Throwable throwable) { super(s, throwable); }
    public UpperBoundIsNotValidException(Throwable throwable) { super(throwable); }
}
