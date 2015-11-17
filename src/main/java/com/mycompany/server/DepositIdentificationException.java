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
public class DepositIdentificationException extends RuntimeException {
    public DepositIdentificationException() { super(); }
    public DepositIdentificationException(String err) { super(err); }
    public DepositIdentificationException(String s, Throwable throwable) { super(s, throwable); }
    public DepositIdentificationException(Throwable throwable) { super(throwable); }
}
