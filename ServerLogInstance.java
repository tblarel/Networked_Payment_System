package project1;

import java.io.*;
import java.util.*;

public class ServerLogInstance{

	private String type;		//String to denote transaction type
	private String wid1;		//int for sender/biller
	private String wid2;		//int for wallet id of recipient/payee
	private String amount;
	private String status;		//0 by default, 1 for confirmed, 2 for declined

	public ServerLogInstance(String type, String wid1, String wid2,String amount, String status)	
	{	//Default Constructor
		this.type = type;
		this.wid1 = wid1;
		this.wid2 = wid2;
		this.amount = amount;
		this.status = status;
	}

	public void UpdateStatus(String newstatus)
	{
		this.status = newstatus;
	}

	public String getwid1()
	{
		return(this.wid1);
	}

	public String gettype()
	{
		return(this.type);
	}

	public String getamount()
	{
		return(this.amount);
	}

	public String getstatus()
	{
		return(this.status);
	}

	public String getwid2()
	{
		return(wid2);
	}


}