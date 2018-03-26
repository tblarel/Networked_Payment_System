package project1;

import java.io.BufferedReader;
import java.io.*;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class TcpServer {

	
	public static void handleclient(DataOutputStream outToClient, BufferedReader inFromClient)
	{
		int readinput = 0;
		boolean readwid = true;
		String clientid="";
		try
		{
			for(String clientline = inFromClient.readLine(); clientline != null; clientline = inFromClient.readLine()) 	
       		{
       			  	ArrayList <ServerLogInstance> serverlog = new ArrayList<ServerLogInstance>();
					serverlog = ReadLog(serverlog);
					String parts[] = clientline.split("_");
					System.out.println("From Client: ");
					for(int i = 0 ; i<parts.length; i++)
					{
						System.out.println(parts[i]);
					}
					if(parts[1].equals("hello"))
					{
						clientid = parts[0];		//Store client id upon connection
						System.out.println("Connected to client with walletid: " + clientid +". Checking for pending transactions...");
						checkpending(clientid,outToClient,inFromClient);
						continue;
					}
				
		    		if(parts[0].equals("quit"))
		    		{    
		    			System.out.println("Saving log to file and quitting...");
						System.exit(0);
		    		}

					else if (parts[0].equals("send") || parts[0].equals("bill"))
					{	System.out.println("Adding new "+parts[0]+" transaction to serverlog");
						serverlog.add(new ServerLogInstance(parts[0],clientid,parts[1],parts[2],"pending"));
					}        			
		    		for(int i =0; i< parts.length; i++)
		    		{	
		    			System.out.println(parts[i]);
		    		}
		    		
		    		System.out.println("Saving log to file...");
		    		SaveLog(serverlog);
		    		System.out.println("next msg!");		    		
		    	} 
		    	
		    	System.out.println("read input");
	        	
        }

		
		catch(IOException e) 
		{
	    	System.err.println("Caught IOException: " + e.getMessage());
		}


	}

  	
	public static void SaveLog(ArrayList<ServerLogInstance> loginstance)
	{
		File old = new File("project1/log.txt");
		old.delete();
		File fnew = new File("project1/log.txt");
		PrintWriter out = null;
		System.out.println("Saving log of size "+loginstance.size()+" to file");
		try 
		{ out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("project1/log.txt")), "UTF-8"));
		  for(int i = 0; i<loginstance.size(); i++) 
		  {	
		    out.println(String.format(loginstance.get(i).gettype()+"_"+loginstance.get(i).getwid1()+"_"+loginstance.get(i).getwid2()+"_"+loginstance.get(i).getamount()+"_"+loginstance.get(i).getstatus()+"_"));
		  }      
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

	public static ArrayList<ServerLogInstance> ReadLog(ArrayList<ServerLogInstance> loginstance)
	{
		try
		{	
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("project1/log.txt"), "UTF8"));	        
			String str;		      
			while ((str = in.readLine()) != null) 
			{
				String parts [] = str.split("_");
				System.out.println("Reading in transaction: " + parts[0]+parts[1]+parts[2]+parts[3]+parts[4]);
				loginstance.add(new ServerLogInstance(parts[0],parts[1],parts[2],parts[3],parts[4]));			   
			}
			in.close();
		} 
	    catch (UnsupportedEncodingException e) 
	    {
			System.out.println(e.getMessage());
	    } 
	    catch (IOException e) 
	    {
			System.out.println(e.getMessage());
	    }
	    catch (Exception e)
	    {
			System.out.println(e.getMessage());
	    } 
	    return loginstance;
	}

  	public static void main(String[] args) 
  	{
  		ArrayList <ServerLogInstance> serverlog = new ArrayList<ServerLogInstance>();
  		System.out.println("Reading log from file...");
		serverlog = ReadLog(serverlog);
	    try 
	    {
	    	System.out.println("size of serverlog is " +serverlog.size());
  			//serverlog.add(new ServerLogInstance("bill","2000","1001","1300","pending"));
	    	//create welcoming socket at port 5000
     		ServerSocket welcomeSocket = new ServerSocket(5000);
     		System.out.println("Creating connection on localhost at port 5000");
     		//wait, on welcoming socket for contact by client
      		Socket connectionSocket = welcomeSocket.accept();
	  	 	//create input stream, attached to socket
      		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			System.out.println("Connection received from " + connectionSocket.getInetAddress().getHostName());
      		//create output stream, attached to socket
     		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 		
    		
    		while (true) 
    		{
    			handleclient(outToClient,inFromClient);
			}
		}
	
		catch(IOException e) 
		{
	    	System.err.println("Caught IOException: " + e.getMessage());
		}
		

	}
	public static void checkpending(String clientid, DataOutputStream outToClient, BufferedReader inFromClient)
	{
		ArrayList <ServerLogInstance> serverlog = new ArrayList<ServerLogInstance>();
		serverlog = ReadLog(serverlog);
		String token = "_";
		Integer logpos = null;
		boolean ismatch = false;
		try
		{
			System.out.println("server log size is "+serverlog.size());
			for(int i = 0; i<serverlog.size(); i++)
			{
				logpos=null;
				ServerLogInstance templog = serverlog.get(i);
				System.out.println("Checking if " +templog.getwid2() +" is equal to "+clientid);
				if(templog.gettype().equals("bill") && templog.getstatus().equals("accepted")&& templog.getwid1().equals(clientid))
				{
					System.out.println("Found accepted bill for user!");
					outToClient.writeBytes("billreturn");
					outToClient.writeBytes(token);			
					outToClient.writeBytes(templog.getwid2());
					outToClient.writeBytes(token);		
					outToClient.writeBytes(templog.getamount());	
					outToClient.writeBytes("\r\n");	//Send client End of Line	
					System.out.println("Sending completed bill transaction to user and deleting from serverlog");
					serverlog.remove(i);
					ismatch = true;
					SaveLog(serverlog);		
				}

				if(templog.getwid2().equals(clientid))
				{	
					logpos = i;
					System.out.println("found one at position "+logpos+" in log.");
					ismatch = true;
				}
				if(templog.getstatus().equals("return") && templog.getwid1().equals(clientid))
				{
					System.out.println("Found return transaction for user!");
					outToClient.writeBytes(templog.getstatus());
					outToClient.writeBytes(token);			
					outToClient.writeBytes(templog.getwid1());
					outToClient.writeBytes(token);		
					outToClient.writeBytes(templog.getamount());	
					outToClient.writeBytes("\r\n");	//Send client End of Line	
					System.out.println("Sending return transaction to user and deleting from serverlog");
					serverlog.remove(i);
					ismatch = true;
					SaveLog(serverlog);		
				}
			
			}			
			if (!ismatch)
			{
				outToClient.writeBytes("none");	
				outToClient.writeBytes(token);	
				outToClient.writeBytes("\r\n");	//Send client End of Line	
				System.out.println("No transactions found, returning");
			}
		    else if(logpos != null)		
		    {	
				System.out.println("Found pending transaction for user!");
				//Check if client connecting is recipient of pending transactions
				if(serverlog.get(logpos).getstatus().equals("pending"))
				{	//System.out.println("Found pending transaction for user!");
					outToClient.writeBytes(serverlog.get(logpos).gettype());
					outToClient.writeBytes(token);			
					outToClient.writeBytes(serverlog.get(logpos).getwid1());
					outToClient.writeBytes(token);		
					outToClient.writeBytes(serverlog.get(logpos).getamount());	
					outToClient.writeBytes("\r\n");	//Send client End of Line	
					System.out.println("Sending pending transaction to user");
					String response = inFromClient.readLine();
					if (response.equals("yes")&& serverlog.get(logpos).gettype().equals("bill"))
					{
						// Update transaction status to accepted
						serverlog.get(logpos).UpdateStatus("accepted");
    					int temppos = Integer.parseInt(logpos.toString());	
						System.out.println("updating log item at position " +temppos);
						SaveLog(serverlog);
					}
					else if (response.equals("yes"))
					{
						// Update transaction status to accepted
						//serverlog.get(logpos).UpdateStatus("accepted");
    					int temppos = Integer.parseInt(logpos.toString());	
						System.out.println("removing log item at position " +temppos);
						serverlog.remove(temppos);
						SaveLog(serverlog);	
					}
					if (response.equals("no") && serverlog.get(logpos).gettype().equals("bill"))
					{
						// If reponse is no & type is bill, delete transaction
						int temppos = Integer.parseInt(logpos.toString());	
						System.out.println("Bill declined by user, deleting transaction at position "+temppos);
						serverlog.remove(temppos);
						SaveLog(serverlog);
						return;	
					}
					if (response.equals("no") && serverlog.get(logpos).gettype().equals("send"))
					{
						// If reponse is no & type is send, return funds to wid1 
						System.out.println("recipient declined funds sent to them, returning funds to sender.");
						serverlog.get(logpos).UpdateStatus("return");	
					}

					System.out.println("Response from user: " + response);
				}
			
				SaveLog(serverlog);		
		    }
		}

		catch (NumberFormatException e) 
	    {
			System.out.println(e.getMessage());
	    }

		catch (IOException e) 
	    {
			System.out.println(e.getMessage());
	    }
	
	} 


}