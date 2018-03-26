# Networked_Payment_System

To run the program: 
	
	1. Run the server: "java TcpServer"
	2. In another terminal window, run the client: "java TcpClient" or "java TcpClient1"

Upon running the client, users are prompted for input and can enter one of the following options:

	1. "bal" to view current blance
	2. "help" to view available commands
	3. "send" to send funds to another user
	4. "bill" to request funds from another user
	5. "quit" to disconnect and exit the program.


Note: Currently the server is not multithreaded and can only be connected to one client at a time. The server must be re-run after disconnecting with one client and before connecting with the second. 

	TcpClient has a wallet id of 1001 and a starting balance of 400.0
	TcpClient1 had a wallet id of 2000 and a starting balance of 3100.0


Known Bugs: 
	
	1. Upon connecting to the server, the server checks for any pending transactions and prompts the user for approval of these incoming transactions. The user is currently only able to address one incoming transaction each time they run the programs. 

