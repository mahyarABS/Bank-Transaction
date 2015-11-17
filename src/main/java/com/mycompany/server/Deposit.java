/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import javax.imageio.IIOException;

/**
 *
 * @author mahyar
 */
public class Deposit {
    private String customer = null;
    private String id = null;
    private BigDecimal balance = null;
    private BigDecimal upperBound = null;
    public Deposit(String customer, String id, BigDecimal initialBalance, BigDecimal upperBound) throws IOException{
        setCustomerName(customer);
        setId(id);
        setUpperBound(upperBound);
        setBalance(initialBalance);
    }
    
    public Deposit(String customer, String id, String initialBalance, String upperBound) throws IOException{
        setCustomerName(customer);
        setId(id);
        setUpperBound(upperBound);
        setBalance(initialBalance);
    }
    
    private void setCustomerName(String name) throws IOException{
        if(name != null && !"".equals(name))
            customer = name;
        else
            throw new IOException("Customer name is invalid!");
    }
    
    public String getCustomerName() throws IOException{
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
    
    public String getId() throws IOException{
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
    
    public BigDecimal getUpperBound() throws IOException{
        if(upperBound != null)
            return upperBound;
        else
            throw new IOException("Uperbound is not initiated!");
    }
    
    public String getUpperBoundInString() throws IOException{
        BigDecimal value = getUpperBound();
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        return myFormatter.format(value);
    }
    
    private void setBalance(BigDecimal balance) throws IOException{
        if(balance == null)
            throw new IOException("The balance is not given in the file or is negative");
        else if(balance.intValue() < 0)
            throw new BalanceIsNotEnoughException();
        else if(balance.intValue() > getUpperBound().intValue())
            throw new UpperBoundExceedException("The balance is bigger than upperbound!");
        else
            this.balance = balance;
    }   
    
    private void setBalance(String balance) throws IOException{
        if(balance == null)
            throw new IOException("The initial balance is not given in the file");
        else{ 
            BigDecimal newBalance = new BigDecimal(balance.replace(",", ""));
            setBalance(newBalance);
        }
    } 
    
    public BigDecimal getBalance() throws IOException{
        if(this.balance != null)
            return this.balance;
        else
            throw new IOException("The initial balance is not set!");
    }
    
    public String getBalanceInString() throws IOException{
        BigDecimal value = getBalance();
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        return myFormatter.format(value);
    }
    
    public synchronized void addBalanceToDeposit(BigDecimal newCash) throws IOException{
        BigDecimal newBalance = getBalance().add(newCash);
        setBalance(newBalance);
    }
    
    public synchronized void addBalanceToDeposit(String newCash) throws IOException{
        BigDecimal cash = new BigDecimal(newCash.replace(",", ""));
        BigDecimal newBalance = getBalance().add(cash);
        try{
            setBalance(newBalance);
        } catch (UpperBoundExceedException ex){
            throw new UpperBoundExceedException(ex.getMessage() + "\nThe request was " + newCash + " and upperbound is " + getUpperBoundInString());
        }
    }
    
    public synchronized void withdraw(String withdrawAmount) throws IOException{
        BigDecimal amount = new BigDecimal(withdrawAmount.replace(",", ""));
        BigDecimal newBalance = getBalance().subtract(amount);
        try{
            setBalance(newBalance);
        } catch (BalanceIsNotEnoughException e){
            throw new BalanceIsNotEnoughException("The balance amount is " + getBalanceInString() + " and is not enough to withdraw up to " + withdrawAmount + "!");
        }
    }
}
