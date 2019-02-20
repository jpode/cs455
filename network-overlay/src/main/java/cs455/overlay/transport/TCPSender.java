package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import cs455.overlay.wireformats.Event;

public class TCPSender {
	private DataOutputStream dout;
	
	public TCPSender(Socket socket){
		try {
			dout = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("DEBUG:TCPSender: error getting DataOutputStream from socket ");
			e.printStackTrace();
		}
	}
	
	public void sendEvent(Event e) {
		try {
			sendData(e.getBytes());
		} catch (IOException e1) {
			System.out.println("DEBUG:TCPSender: error sending data ");
			e1.printStackTrace();
		}
	}
	
	private void sendData(byte[] dataToSend) throws IOException {
		System.out.println("TCPSender: sending new message...");
		int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}
}
