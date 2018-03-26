package project1;

import java.io.*;
import java.util.*;

public class Wallet{

	float balance;	//balance of wallet
	String wid; 	//wallet id
	ArrayList <Wallettransaction> transactionlist = new ArrayList <Wallettransaction>(); //Arraylist to strore past transactions

	public Wallet(float accbal, String accid){	//constructor 
		this.balance = accbal;					//set starting account/wallet balance
		this.wid = accid;						//set account/wallet id
		this.transactionlist=null;				//wallets begin with no transactions

	}

	public void newtransaction(String wid2, String value, String transtype)
	{
		Wallettransaction a = new Wallettransaction(wid2,value,transtype);	//instantiate new transaction object
		this.transactionlist.add(a);							//add it to wallet's list
	}

	public float getbal()	
	{
		return(this.balance);	//method to check wallet's balance
	}

	public void addfunds(String amount)
	{
		this.balance+=Float.parseFloat(amount);
	}
	public void subtractfunds(String amount)
	{
		this.balance-=Float.parseFloat(amount);
	}
	public String getwid()
	{	
		return(this.wid);		//method to check wallet's id
	}

}