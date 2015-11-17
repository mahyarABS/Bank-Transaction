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
public class BalanceIsNotValidException extends RuntimeException{
    public BalanceIsNotValidException() { super(); }
    public BalanceIsNotValidException(String err) { super(err); }
    public BalanceIsNotValidException(String s, Throwable throwable) { super(s, throwable); }
    public BalanceIsNotValidException(Throwable throwable) { super(throwable); }
}
