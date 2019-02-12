package cs455.overlay.node;

//This is the client program.
import java.io.*;
import java.net.*;

import cs455.overlay.wireformats.Register;

public class Client {

	public static Socket connect(String addr, Integer port) {
		// Cycle through ports until an empty one is found
		while (port < 90000) {
			try {
				// Create the server socket
				Socket result = new Socket(addr, port);
				return result;
			} catch (Exception e) {
				port++;
				continue;
			}
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		String SERVER_ADDRESS = "192.168.2.14";
		Integer port = 5001; // Starting port to search for connections
		boolean connected = false;
		Socket cs;

		System.out.println("Attempting to connect...");
		while (port < 66000 && !connected) {
			cs = connect(SERVER_ADDRESS, port);

			if (cs != null && cs.isConnected()) {
				connected = true;
				System.out.println("Connection established");
				messageServer(cs);
			} else {
				port++;
			}
		}

		if (!connected) {
			System.out.println("Connection failed");
		}
	}

	private static void messageServer(Socket cs) {
		DataInputStream inputStream;
		DataOutputStream outputStream;
		boolean listening = true;

		// We create the socket AND try to connect to the address and port we are
		// running the server on

		// We assume that if we get here we have connected to the server.
		System.out.println("Connected to the server.");
		System.out.println(cs.getRemoteSocketAddress());

		try {

			System.out.println("Creating output stream");
			outputStream = new DataOutputStream(cs.getOutputStream());
			System.out.println("Creating input stream");
			inputStream = new DataInputStream(cs.getInputStream());

			// Attempt to register
			Register rq = new Register(cs.getInetAddress().toString().substring(1), cs.getLocalPort());

			byte[] packet = rq.getPacket();
			Integer packet_length = packet.length;
			// Our self-inflicted protocol says we send the length first
			outputStream.writeInt(packet_length);
			// Then we can send the message
			outputStream.write(packet, 0, packet_length);

			while (listening) {
				Integer msgLength = 0;
				// Try to read an integer from our input stream. This will block if there is
				// nothing.
				System.out.println("Waiting for message...");
				msgLength = inputStream.readInt();

				// If we got here that means there was an integer to read and we have the
				// length of the rest of the next message.
				System.out.println("Received a message length of: " + msgLength);

				// Try to read the incoming message.
				byte[] incomingMessage = new byte[msgLength];
				inputStream.readFully(incomingMessage, 0, msgLength);

				// You could have used .read(byte[] incomingMessage), however this will read
				// *potentially* incomingMessage.length bytes, maybe less.
				// Whereas .readFully(...) will read exactly msgLength number of bytes.

				System.out.println("Received Message: " + new String(incomingMessage));
			}

			// Close streams and then sockets
			inputStream.close();
			outputStream.close();
			cs.close();
			return;

		} catch (IOException e) {
			System.out.println("Client::main::talking_to_the_server:: " + e);
		}
	}
}