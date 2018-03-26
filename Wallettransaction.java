package project1;

import java.io.*;



public class Wallettransaction {
	String wid1;			//counterpart's wallet ID
	String amount;		//amount exchanged in transaction
	String type;			//type of transaction: 0 = sent money, 1 = received money

	public Wallettransaction(String wid2, String value, String transtype)
	{
		this.wid1=wid2;
		this.amount=value;
		this.type=transtype;
	}

}
