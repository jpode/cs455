package cs455.overlay.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.Deregister;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.Register;

public class MessagingNode implements Node{
	
	private Socket socket;
	private DataInputStream input_stream;
	private DataOutputStream output_stream;
	private boolean listening;
	private boolean connected;
	private TCPReceiverThread server_listener;
	private TCPSender server_sender;
	
	public MessagingNode() {
		listening = false;
		connected = false;
	}
	
	public void onEvent(Event e) {

	}
	
	public boolean connectToServer(String addr, Integer port) {
		boolean connected = false;
		socket = attemptConnection(addr, port);

		if (socket != null && socket.isConnected()) { 
			//Node is connected to the server at this point
			connected = true;
			
			try {
				//Create data streams that will be used for all server communications
				output_stream = new DataOutputStream(socket.getOutputStream());
				input_stream = new DataInputStream(socket.getInputStream());
				
				//Start a thread to listen to the server
				server_listener = new TCPReceiverThread(socket);
				
				//Instantiate TCP sender object to send messages to the server
				server_sender = new TCPSender(socket);
				
				//Attempt to register with the server
				server_sender.sendEvent(EventFactory.getInstance().createEvent(new String("1" + "\n" + socket.getInetAddress().toString().substring(1) + "\n" + socket.getLocalPort())));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			port++;
		}

		if (!connected) {
			System.out.println("Node connection to "+ addr + ":" + port +" failed");
		}
		
		return connected;
	}
	
	public Socket attemptConnection(String addr, Integer port) {
		// Cycle through ports until an empty one is found
		System.out.println("MessagingNode attempting to connect...");
		while (port < 66000) {
			try {
				// We create the socket AND try to connect to the address and port we are
				// running the server on
				Socket result = new Socket(addr, port);
				
				// We assume that if we get here we have connected to the server.
				System.out.println("MessagingNode " + result.getInetAddress().toString().substring(1) + ":" + result.getLocalPort() + " successfully connected to the server at " + addr + ":" + port);

				return result;
			} catch (Exception e) {
				port++;
				continue;
			}
		}
		return null;
		
	}

	public void disconnect() {
		//Deregister with the server
		server_sender.sendEvent(EventFactory.getInstance().createEvent(new String("2" + "\n" + socket.getInetAddress().toString().substring(1) + "\n" + socket.getLocalPort())));
		
		// Close streams and then sockets
		try {
			input_stream.close();
			output_stream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
