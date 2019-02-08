package cs455.overlay.registry;

//This is the server program.
import java.io.*;
import java.net.*;

public class Server {

	private boolean open;
	private ServerSocket ss;
	private Socket test_socket; //Used to check if a port is available
	
	public Server(Integer port, Integer num_connections) {
		//Cycle through ports until an empty one is found
		while(port < 66000) {
			try {
				test_socket = new Socket("localhost", port);
				port++;

			} catch(IOException e) {
				//Create the server socket. Number of connections is specified as the max connection queue size
				try {
					this.ss = new ServerSocket(port, num_connections);
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		this.open = false;
	}
	
	public void startServer() {
		if(ss != null) {
			
			//Print IP address for test purposes.
			//TODO: remove 
			try{
			 ss.getInetAddress();
			 System.out.println(InetAddress.getLocalHost().getHostAddress() + ":" + ss.getLocalPort()); 
			}catch(UnknownHostException e){ e.printStackTrace(); }
			
			
			//This thread runs the server socket, so that the entire program does not block while listening for connections
			Thread ss_thread = new Thread(new Runnable(){
				public void run(){
					try{
						while(open) {
							System.out.println("Listening for connections...");
							
							//Block on accepting connections. Once it has received a connection it will return a socket for us to use.
							Socket cs = ss.accept();
							
							//If we get here we are no longer blocking, so we accepted a new connection
							System.out.println("Connection received: " + cs.getRemoteSocketAddress().toString());
							
							//We have yet to block again, so we can handle this connection however we would like to.
							DataInputStream inputStream = new DataInputStream(cs.getInputStream());
							DataOutputStream outputStream = new DataOutputStream(cs.getOutputStream());
							
							//Wait for a message
							Integer msgLength = 0;
							//Try to read an integer from our input stream. This will block if there is nothing.
							msgLength = inputStream.readInt();
			
							//If we got here that means there was an integer to 
							// read and we have the length of the rest of the next message.			
							//Now try to read the incoming message.
							byte[] incomingMessage = new byte[msgLength];
							inputStream.readFully(incomingMessage, 0, msgLength);
			
							//You could have used .read(byte[] incomingMessage), however this will read 
							// *potentially* incomingMessage.length bytes, maybe less.
							//Whereas .readFully(...) will read exactly msgLength number of bytes. 
							
							System.out.println("Received Message: ");
							System.out.println(new String(incomingMessage));
							System.out.println();
							
							byte[] response = Registry.getInstance().handlePacket(incomingMessage, cs);
							
							if(response != null) {
								//Send response indicating registration status 
								Integer response_length = response.length;
				
								//Our self-inflicted protocol says we send the length first
								outputStream.writeInt(response_length);
								//Then we can send the message
								outputStream.write(response, 0, response_length);
							} else {
								System.out.println("Message handling failure.");
							}
							
							/*
							//Let's send a message to our new friend
							byte[] msgToClient = "What class is this video for?".getBytes();
							Integer msgToClientLength = msgToClient.length;
			
							//Our self-inflicted protocol says we send the length first
							outputStream.writeInt(msgToClientLength);
							//Then we can send the message
							outputStream.write(msgToClient, 0, msgToClientLength);
							*/
							//Close streams and then socket
							inputStream.close();
							outputStream.close();
							cs.close();
						}
						
						if(!open) {
							System.out.println("Server received kill command");
						}
						
					} catch (IOException e) {
			            System.out.println("Server::main::accepting_connections:: " + e);
			            System.exit(1);
			        }
				}
			});
			
			this.open = true;
			ss_thread.start();
		}
	}

	public void killServer() {
		open = false;
		
		try {
			ss.close();
		} catch (IOException e) {
			System.out.println("ERROR: Server was not killed gracefully");
			e.printStackTrace();
		}
	}

	public int serverStatus() {
		//Returns an int instead of boolean for probable future use, where the server will return a more detailed status
		if(open) {
			return 0;
		} else {
			return 1;
		}
	}
}