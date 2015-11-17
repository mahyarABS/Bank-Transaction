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
public class BalanceIsNotEnoughException extends RuntimeException{
    public BalanceIsNotEnoughException(){ super(); }
    public BalanceIsNotEnoughException(String err){ super(err); }
    public BalanceIsNotEnoughException(String s, Throwable throwable) { super(s, throwable); }
    public BalanceIsNotEnoughException(Throwable throwable) { super(throwable); }
}
