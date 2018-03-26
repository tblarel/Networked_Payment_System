package project1;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.*;
import java.util.*;

public class TcpClient1 {
	static String myfilename="wallet1.txt";

	public static float getbalfromfile(String filename)
	{
		String tempbal="";
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("project1/"+filename), "UTF8"));	        
			String str;		      
			str=in.readLine();
			String parts[] = str.split("_");
			tempbal=parts[1];
			System.out.println("User's balance is "+tempbal);
			in.close();
		}
		catch (IOException e) 
	    {
			System.out.println(e.getMessage());
	    }
	    catch (Exception e)
	    {
			System.out.println(e.getMessage());
	    } 
	    return (Float.parseFloat(tempbal));
	}

	public static void updatebalinfile(String filename, Wallet mywallet)
	{
		File old = new File("project1/"+filename);
		old.delete();
		File fnew = new File("project1/"+filename);
		PrintWriter out = null;	
		System.out.println("Saving balance of "+mywallet.getbal()+" to file");
		try 
		{ 
			out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("project1/"+filename)), "UTF-8"));	  	
		    out.println(String.format(mywallet.getwid()+"_"+mywallet.getbal()));	        
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
		  	e.printStackTrace();
		} 
		finally 
		{
		  if(out != null) 
		  {
		    out.close();
		  }
		}

	}

	public static void main(String[] args) 
	{
		TcpClient1 sp = new TcpClient1();
		String temp;
		String displayBytes;
		float balance = getbalfromfile(myfilename);
		Wallet mywallet = new Wallet(balance,"2000");
		try 
		{
			String mywid = mywallet.getwid();
			Socket clientSocket = new Socket("localhost",5000);
		  	System.out.println("Initial connection succesfull!");
		  	//create input and output streams
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			
			sp.Checkpending(mywallet,clientSocket,outToServer,mywid, inFromServer);
	     	System.out.println("My wallet id is: " + mywid);
			
 			while(true)
		  	{   
			  	System.out.print("Enter command or type 'help' for list of commands: ");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				temp = br.readLine();		      
				switch(temp)
			      {	
			      	case "help":
			      		System.out.println("Welcome to banking system. \r\n Type 'bal' to check your balance \r\n 'send' to send funds \r\n 'bill' to request funds from another user \r\n or 'quit' to exit");
			      		break;

			      	case "bal":
			      		System.out.println("$"+mywallet.getbal());	
			      		break;

			      	case "send":
			      		System.out.println("Send Funds...");
			      		sp.Sendprotocol(mywallet, clientSocket, outToServer);
			      		updatebalinfile(myfilename,mywallet);
			      		break;

			      	case "bill":
			      		System.out.println("Bill Funds...");
			      		sp.Billprotocol(mywallet,clientSocket, outToServer);
			      		break;

			      	case "quit":
			      		sp.Quitprotocol(clientSocket, outToServer);
			      		System.out.println("Quitting Program...");
			      		System.exit(0);
			      		break;

			      	default:
			      		System.out.println("Please enter a valid command. Type 'help' for a list of commands.");
			      		break;
			      }
			      //br.close();
			}
		}
		catch (IndexOutOfBoundsException e) 
		{
	    		System.err.println("IndexOutOfBoundsException: " + e.getMessage());
		}
		catch(IOException e) 
		{
	    		System.err.println("Caught IOException: " + e.getMessage());
		}

	}

	public void Checkpending(Wallet mywallet,Socket clientSocket, DataOutputStream outToServer, String mywid, BufferedReader inFromServer)
	{
		String token = "_";
		try
		{
			//Send server user's wallet id
		    outToServer.writeBytes(mywid);
			outToServer.writeBytes(token);	
		    System.out.println("Sending wallet id to server: " +mywid);	
		    //And connection welcome message
		    outToServer.writeBytes("hello");
			outToServer.writeBytes(token);
			//And connection welcome message
		    outToServer.writeBytes("server");
			outToServer.writeBytes(token);	
		    outToServer.writeBytes("\r\n");
			for(String serverline = inFromServer.readLine(); serverline != null; serverline = inFromServer.readLine()) 	
			{
		      	//System.out.println("serverline:" +serverline);
		      	//Check if server has message
		      	String parts [] = serverline.split("_");
		      	if(parts[0].equals("none"))
		      	{
					System.out.println("Done checking for transactions");	
					break;
		      	}
		      	System.out.println("*You have a pending transaction!*");
		      	if(parts[0].equals("return"))
		      	{
		      		System.out.println(parts[2] + " funds sent to user with wallet id " +parts[1]+ " have been declined and returned");
		      		mywallet.addfunds(parts[2]);
		      		updatebalinfile(myfilename,mywallet);
		      		break;
		      	}
				if(parts[0].equals("billreturn"))
		      	{
		      		System.out.println(parts[2] + " funds received from your bill request from user with wallet id " +parts[1]);
		      		mywallet.addfunds(parts[2]);
		      		updatebalinfile(myfilename,mywallet);
		      		break;
		      	}
		      	else if(parts[0].equals("send"))
		      	{	
			      	System.out.println("Do you wish to accept " + parts[2] + " funds from user with wallet id " +parts[1]+ "? Enter [y/n]");
			      	char choice = (char) System.in.read();
					if(choice == 'y' || choice == 'Y')
					{
						System.out.println("Pending transaction accepted. Funds and transaction will be added to wallet.");
						outToServer.writeBytes("yes");
						outToServer.writeBytes("\r\n");
						mywallet.addfunds(parts[2]);
						System.out.println("Updated balance of wallet is : $" + mywallet.getbal());
						updatebalinfile(myfilename,mywallet);
					}
					else
					{
						System.out.println("Funds declined.");
						outToServer.writeBytes("no");
						outToServer.writeBytes("\r\n");
					}
					System.in.read(new byte[System.in.available()]);   
			      	break;
			    }
			   
			    
			    if(parts[0].equals("bill"))
		      	{	
			      	System.out.println("You have been billed $" + parts[2] + " from user with wallet id " +parts[1]+ ". Would you like to pay this user? Enter [y/n]");
			      	char choice = (char) System.in.read();
					if(choice == 'y' || choice == 'Y')
					{
						System.out.println("Pending transaction accepted. Funds will be removed and transaction will be added to wallet.");
						outToServer.writeBytes("yes");
						outToServer.writeBytes("\r\n");
						mywallet.subtractfunds(parts[2]);
						System.out.println("Updated balance of wallet is : $" + mywallet.getbal());
						updatebalinfile(myfilename,mywallet);
					}
					else
					{
						System.out.println("Bill declined.");
						outToServer.writeBytes("no");
						outToServer.writeBytes("\r\n");
					}
					System.in.read(new byte[System.in.available()]);   
			      	break;
			    }
			}

		}

		catch(IOException e) 
		{
	    	System.err.println("Caught IOException: " + e.getMessage());
		}

	}
	
	public void Quitprotocol(Socket clientSocket, DataOutputStream outToServer)
	{

		String token = "_";

		try
		{
			String s1 = "quit";
			outToServer.writeBytes(s1);				
			outToServer.writeBytes(token);			

			String s2 = "quit";
			outToServer.writeBytes(s2);				
			outToServer.writeBytes(token);	

			String s3 = "quit";
			outToServer.writeBytes(s3);
			outToServer.writeBytes(token);				

			outToServer.writeBytes("\r\n");			//Send server End of Line	
		}
		catch(IOException e) 
		{
	    	System.err.println("Caught IOException: " + e.getMessage());
		}

	}
	

	public void Sendprotocol(Wallet mywallet, Socket clientSocket, DataOutputStream outToServer)
	{
		String tempwid;
		String tempamnt;
		String token = "_";

		try
		{	
			System.out.print("Please enter 4 digit wallet id of user: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			tempwid = br.readLine();
			System.out.println("Sending to user with wallet id: " + tempwid);
			System.out.print("Please enter amount of funds you would like to send: ");
		    tempamnt = br.readLine();
		    System.out.println("Sending " +tempamnt);
		    Float x = Float.valueOf(tempamnt);
		    if (x > mywallet.balance)
		      	System.out.println("Error. Cannot send more funds than are available in your wallet.");
		    else
		    {
		    	System.out.println("Sending "+tempamnt+" to user with wallet id " +tempwid);
		    	System.out.println("Please confirm [y/n] to proceed with the transaction");
		    	char choice = (char) System.in.read();
				if(choice == 'y' || choice == 'Y')
				{
			    	//send protocol type to server
			    	mywallet.subtractfunds(tempamnt);
			    	String s = "send";
			    	//byte[] b = s.getBytes("UTF-8");
					//int messagelength = b.length; 			//Get lenth of protocol code
					//System.out.println("sending " + messagelength + " bytes to server for protocol");
					//outToServer.writeInt(messagelength);	//Send that length to server
					outToServer.writeBytes(s);				//Send server send funds code
			    	outToServer.writeBytes(token);			


					//send wallet id of recipient
					//b = tempwid.getBytes("UTF-8");
					//messagelength = b.length;				//get byte length of wid
			    	//outToServer.writeInt(messagelength);	//send this length to server
			    	outToServer.writeBytes(tempwid);		//Send user's wallet id
			    	outToServer.writeBytes(token);			

			    	//send amnt
					//b = tempamnt.getBytes("UTF-8");
			    	//messagelength = b.length;
		    		//System.out.println("Size of amnt is " + messagelength);
		    		//System.out.println("sending " + messagelength + " bytes to server for amnt");
		    		//outToServer.writeInt(messagelength);	//send this length to server
		      		outToServer.writeBytes(tempamnt);		//Send amount to server
			    	outToServer.writeBytes(token);			

		      		outToServer.writeBytes("\r\n");			//Send server End of Line	

		      		System.in.read(new byte[System.in.available()]);   //clear input

			    }
			    else{return;}
			}
		}	
		catch(IOException e) 
		{
	    	System.err.println("Caught IOException: " + e.getMessage());
		}
	
	}
	
	public void Billprotocol(Wallet mywallet,Socket clientSocket, DataOutputStream outToServer)
	{
		String tempwid;
		String tempamnt;
		String token = "_";
		try
		{	
			System.out.print("Please enter 4 digit wallet id of the member you would like to charge: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			tempwid = br.readLine();
			System.out.println("Billing from user with wallet id " + tempwid);
			System.out.print("Please enter amount of funds you would like to request: ");
		    tempamnt = br.readLine();
		    System.out.println("Billing " +tempamnt);
		    Float x = Float.valueOf(tempamnt);
		    if (x > 5000f)
		    	System.out.println("Error. Cannot bill more than $5000 per request.");
		    else
		    {
		    	System.out.println("Billing "+tempamnt+" from user with wallet id " +tempwid);
		    	System.out.println("Please confirm [y/n] to proceed with the transaction");
		    	char choice = (char) System.in.read();
				if(choice == 'y' || choice == 'Y')
				{
				 	//send protocol type to server
		    		String s = "bill";
			    	//byte[] b = s.getBytes("UTF-8");
					//int messagelength = b.length; 			//Get lenth of protocol code
					//System.out.println("sending " + messagelength + " bytes to server for protocol");
					//outToServer.writeInt(messagelength);	//Send that length to server
					outToServer.writeBytes(s);				//Send server send funds code
			    	outToServer.writeBytes(token);			


					//send wallet id of recipient
					//b = tempwid.getBytes("UTF-8");
					///messagelength = b.length;				//get byte length of wid
			    	//outToServer.writeInt(messagelength);	//send this length to server
			    	outToServer.writeBytes(tempwid);		//Send user's wallet id
			    	outToServer.writeBytes(token);			

			    	//send amnt
					//b = tempamnt.getBytes("UTF-8");
			    	//messagelength = b.length;
		    		//System.out.println("Size of amnt is " + messagelength);
		    		//System.out.println("sending " + messagelength + " bytes to server for amnt");
		    		//outToServer.writeInt(messagelength);	//send this length to server
		      		outToServer.writeBytes(tempamnt);		//Send amount to server
			    	outToServer.writeBytes(token);			

		      		outToServer.writeBytes("\r\n");			//Send server End of Line
		      		System.in.read(new byte[System.in.available()]);   //clear input
				}
				else{return;}
		    }	
		}		
		catch(IOException e) 
		{
	    	System.err.println("Caught IOException: " + e.getMessage());
		}
	
	}
}