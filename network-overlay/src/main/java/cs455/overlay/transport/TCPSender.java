package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import cs455.overlay.wireformats.Event;

public class TCPSender {
	private DataOutputStream dout;
	
	public TCPSender(Socket socket) throws IOException {
		dout = new DataOutputStream(socket.getOutputStream());
	}
	
	public void sendEvent(Event e) {
		try {
			sendData(e.getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void sendData(byte[] dataToSend) throws IOException {
		int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}
}
