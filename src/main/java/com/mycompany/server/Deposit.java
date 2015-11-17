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
    public Deposit(String customer, String id, BigDecimal initialBalance, BigDecimal upperBound) {
        setCustomerName(customer);
        setId(id);
        setUpperBound(upperBound);
        setBalance(initialBalance);
    }
    
    public Deposit(String customer, String id, String initialBalance, String upperBound) {
        setCustomerName(customer);
        setId(id);
        setUpperBound(upperBound);
        setBalance(initialBalance);
    }
    
    private void setCustomerName(String name) {
        if(name != null && !"".equals(name))
            customer = name;
        else
            throw new DepositIdentificationException("Customer name is invalid!");
    }
    
    public String getCustomerName() {
        if(customer != null)
            return customer;
        else
            throw new DepositIdentificationException("Customer name is not set!");
    }
    
    private void setId(String id) {
        if(id != null && !"".equals(id))
            this.id = id;
        else
            throw new DepositIdentificationException("The ID give is not valid!");
    }
    
    public String getId() {
        if(id != null)
            return id;
        else
            throw new DepositIdentificationException("ID is not initiated!");
    }
    
        
    private void setUpperBound(BigDecimal bound) {
        if(bound != null && bound.intValue() >= 0)
            upperBound = bound;
        else if(bound.intValue() < 0)
            throw new UpperBoundIsNotValidException("Upperbound value is negative!");
        else
            throw new UpperBoundIsNotValidException("Upperbound value in the file is not given!");
    }
    
    private void setUpperBound(String bound) {
        if(bound != null){
            BigDecimal intBound = new BigDecimal(bound.replace(",", ""));
            setUpperBound(intBound);
        }
        else
            throw new UpperBoundIsNotValidException("Upperbound value in the file is not given!");
    }
    
    public BigDecimal getUpperBound() {
        if(upperBound != null)
            return upperBound;
        else
            throw new UpperBoundIsNotValidException("Uperbound is not initiated!");
    }
    
    public String getUpperBoundInString() {
        BigDecimal value = getUpperBound();
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        return myFormatter.format(value);
    }
    
    private void setBalance(BigDecimal balance) {
        if(balance == null)
            throw new BalanceIsNotValidException("The balance is not given in the file or is negative");
        else if(balance.intValue() < 0)
            throw new BalanceIsNotEnoughException();
        else if(balance.intValue() > getUpperBound().intValue())
            throw new UpperBoundExceedException("The balance is bigger than upperbound!");
        else
            this.balance = balance;
    }   
    
    private void setBalance(String balance) {
        if(balance == null)
            throw new BalanceIsNotValidException("The initial balance is not given in the file");
        else{ 
            BigDecimal newBalance = new BigDecimal(balance.replace(",", ""));
            setBalance(newBalance);
        }
    } 
    
    public BigDecimal getBalance() {
        if(this.balance != null)
            return this.balance;
        else
            throw new BalanceIsNotValidException("The initial balance is not set!");
    }
    
    public String getBalanceInString() {
        BigDecimal value = getBalance();
        DecimalFormat myFormatter = new DecimalFormat("###,###");
        return myFormatter.format(value);
    }
    
    public synchronized void addBalanceToDeposit(BigDecimal newCash) {
        BigDecimal newBalance = getBalance().add(newCash);
        setBalance(newBalance);
    }
    
    public synchronized void addBalanceToDeposit(String newCash) {
        BigDecimal cash = new BigDecimal(newCash.replace(",", ""));
        BigDecimal newBalance = getBalance().add(cash);
        try{
            setBalance(newBalance);
        } catch (UpperBoundExceedException ex) {
            throw new UpperBoundExceedException(ex.getMessage() + "\nThe request was " + newCash + " the balance is " + getBalanceInString() + " and the upperbound is " + getUpperBoundInString());
        }
    }
    
    public synchronized void withdraw(String withdrawAmount) {
        BigDecimal amount = new BigDecimal(withdrawAmount.replace(",", ""));
        BigDecimal newBalance = getBalance().subtract(amount);
        try{
            setBalance(newBalance);
        } catch (BalanceIsNotEnoughException ex) {
            throw new BalanceIsNotEnoughException("The balance amount is " + getBalanceInString() + " and is not enough to withdraw up to " + withdrawAmount + "!");
        }
    }
}
