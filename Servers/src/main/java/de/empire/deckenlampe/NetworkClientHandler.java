package de.empire.deckenlampe;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class NetworkClientHandler extends AbstractClientHandler {

	private Socket client;

	public NetworkClientHandler(Socket clientsocker, short[] data) throws SocketException {
		super(data);
		this.client = clientsocker;
		client.setSoTimeout(3000);
	}

	@Override
	protected void close() throws IOException {
		this.client.close();
	}

	@Override
	protected void writeError(String string) throws IOException {
		final DataOutputStream dout = new DataOutputStream(this.client.getOutputStream());
		dout.writeUTF(string);
		dout.close();
	}

	@Override
	protected DataInputStream getDataIn() throws IOException {
		return new DataInputStream(new BufferedInputStream(this.client.getInputStream()));
	}

}
