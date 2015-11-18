/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

/**
 *
 * @author mahyar
 */
public class ClientAttributesInvalidException extends RuntimeException {
    public ClientAttributesInvalidException() { super(); }
    public ClientAttributesInvalidException(String err) { super(err); }
    public ClientAttributesInvalidException(String s, Throwable throwable) { super(s, throwable); }
    public ClientAttributesInvalidException(Throwable throwable) { super(throwable); }
}
