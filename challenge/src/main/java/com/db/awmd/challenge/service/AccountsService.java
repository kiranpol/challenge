package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsuficientBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  
  private NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  public synchronized String moneyTransfer(String fromAccountId, String toAccountId, BigDecimal amount) {
	  int accountfrom = this.accountsRepository.getAccount(fromAccountId).getBalance().intValue();
	  int accountTo = this.accountsRepository.getAccount(toAccountId).getBalance().intValue();
	 
	  if(accountfrom >= amount.intValue())
		{
		  try {
			      Account withdrawAccount = this.accountsRepository.withdrawMoney(fromAccountId, amount);
			      notificationService = new EmailNotificationService();
				  notificationService.notifyAboutTransfer(withdrawAccount, "Money transpered to "+toAccountId+" amount of "+amount);
				  Account depositeAccount = this.accountsRepository.depositeMoney(toAccountId, amount);
				  notificationService.notifyAboutTransfer(withdrawAccount, "Money Deposited from "+fromAccountId+" amount of "+amount);
		  
		  }
		  catch(Exception e)
		  {
			  this.accountsRepository.getAccount(fromAccountId).setBalance(new BigDecimal(accountfrom));
			  this.accountsRepository.getAccount(toAccountId).setBalance(new BigDecimal(accountTo));
			  throw e;
		  }
		}
	  else
		{
			throw new InsuficientBalanceException("Insuficient Balance in account for account id "+fromAccountId);
		}
	 
	  return "Money Transfered successfuly";
	  
	  }
  
}
