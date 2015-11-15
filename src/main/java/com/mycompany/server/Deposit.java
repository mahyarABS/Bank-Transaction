/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.IOException;
import java.math.BigDecimal;
import javax.imageio.IIOException;

/**
 *
 * @author mahyar
 */
public class Deposit {
    private String customer = null;
    private String id = null;
    private BigDecimal initialBalance = null;
    private BigDecimal upperBound = null;
    public Deposit(String customer, String id, BigDecimal initialBalance, BigDecimal upperBound) throws IOException{
        setCustomer(customer);
        setId(id);
        setUpperBound(upperBound);
        setInitialBalance(initialBalance);
    }
    
    public Deposit(String customer, String id, String initialBalance, String upperBound) throws IOException{
        setCustomer(customer);
        setId(id);
        setUpperBound(upperBound);
        setInitialBalance(initialBalance);
    }
    
    private void setCustomer(String name) throws IOException{
        if(name != null && !"".equals(name))
            customer = name;
        else
            throw new IOException("Customer name is invalid!");
    }
    
    private String getCustomer() throws IOException{
        if(customer != null)
            return customer;
        else
            throw new IOException("Customer name is not set!");
    }
    
    private void setId(String id) throws IOException{
        if(id != null && !"".equals(id))
            this.id = id;
        else
            throw new IOException("The ID give is not valid!");
    }
    
    private String getId() throws IOException{
        if(id != null)
            return id;
        else
            throw new IOException("ID is not initiated!");
    }
    
        
    private void setUpperBound(BigDecimal bound) throws IOException{
        if(bound != null && bound.intValue() >= 0)
            upperBound = bound;
        else
            throw new IOException("Upperbound value in the file is not given or is negative!");
    }
    
    private void setUpperBound(String bound) throws IOException{
        if(bound != null){
            BigDecimal intBound = new BigDecimal(bound.replace(",", ""));
            if(intBound.intValue() >= 0)
                upperBound = intBound;
            else
                throw new IOException("Upperbound value is negative!");
        }
        else
            throw new IOException("Upperbound value in the file is not given!");
    }
    
    private BigDecimal getUpperBound() throws IOException{
        if(upperBound != null)
            return upperBound;
        else
            throw new IOException("Uperbound is not initiated!");
    }
    
    private void setInitialBalance(BigDecimal balance) throws IOException{
        if(balance == null || balance.intValue() < 0)
            throw new IOException("The initial balance is not given in the file or is negative");
        else if(balance.intValue() > getUpperBound().intValue())
            throw new UpperBoundExceedException("The initial balance is bigger than upperbound!");
        else
            initialBalance = balance;
    }   
    
    private void setInitialBalance(String balance) throws IOException{
        if(balance == null)
            throw new IOException("The initial balance is not given in the file");
        else{ 
            BigDecimal intBalance = new BigDecimal(balance.replace(",", ""));
            if(intBalance.intValue() > getUpperBound().intValue())
                throw new UpperBoundExceedException("The initial balance is bigger than upperbound!");
            else if(intBalance.intValue() < 0)
                throw new IOException("The initial balance given is negative");
            else
                initialBalance = intBalance;
        }
    } 
    
    private BigDecimal getInitialBalance() throws IOException{
        if(initialBalance != null)
            return initialBalance;
        else
            throw new IOException("The initial balance is not set!");
    }
    
    public void addBalanceToDeposit(BigDecimal newBalance){
        
    }
    
    public void withdraw(BigDecimal amount){
        
    }
}
