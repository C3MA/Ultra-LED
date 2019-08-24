package de.empire.deckenlampe;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IdleClient extends AbstractClientHandler {
	private static BlockingQueue<Short>	dataQueue	= new LinkedBlockingQueue<Short>(41 * 24 * 3 * 3);
	private static float				i;
	private static ArrayList<String>	address		= new ArrayList<>();
	static final short					ledAmount	= 984;

	public IdleClient(short[] data) {
		super(data);
	}

	@Override
	protected DataInput getDataIn() throws IOException {

		return new DataInput() {

			@Override
			public void readFully(byte[] b) throws IOException {
			}

			@Override
			public void readFully(byte[] b, int off, int len) throws IOException {
			}

			@Override
			public int skipBytes(int n) throws IOException {
				return 0;
			}

			@Override
			public boolean readBoolean() throws IOException {
				return false;
			}

			@Override
			public byte readByte() throws IOException {
				return 0;
			}

			@Override
			public int readUnsignedByte() throws IOException {
				return 0;
			}

			@Override
			public short readShort() throws IOException {
				try {
					return IdleClient.readShort();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public int readUnsignedShort() throws IOException {
				return 0;
			}

			@Override
			public char readChar() throws IOException {
				return 0;
			}

			@Override
			public int readInt() throws IOException {
				return ledAmount;
			}

			@Override
			public long readLong() throws IOException {
				return 0;
			}

			@Override
			public float readFloat() throws IOException {
				return 0;
			}

			@Override
			public double readDouble() throws IOException {
				return 0;
			}

			@Override
			public String readLine() throws IOException {
				return null;
			}

			@Override
			public String readUTF() throws IOException {
				return null;
			}

		};
	}

	protected static short readShort() throws InterruptedException {
		if (dataQueue.isEmpty()) {
			generateData();
		}
		return dataQueue.take();
	}

	private static void generateData() throws InterruptedException {
		i++;
		BufferedImage img = new BufferedImage(41, 24, BufferedImage.TRANSLUCENT);
		Graphics2D graf = img.createGraphics();
		graf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		draw(graf);


		for (int i = 0; i < ledAmount; i++) {
			int x = i % 41;
			int y = i / 41;
			int argb = img.getRGB(x, y);
			Color co = new Color(argb);

			if(co.getBlue()==0 && co.getGreen()==0 && co.getRed()==0){
				dataQueue.add((short) 10);
				dataQueue.add((short) 5);
				dataQueue.add((short) 0);
			}else{
				dataQueue.add((short) co.getBlue());
				dataQueue.add((short) co.getGreen());
				dataQueue.add((short) co.getRed());
			}

		}

	}

	private static void draw(Graphics2D graf) {
		try {
			address.clear();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress current_addr = addresses.nextElement();
					if (!current_addr.isLoopbackAddress()) {
						String add = current_addr.getHostAddress();
						address.add(add);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		int j = 0;
		for (String ad : address) {
			double br = (Math.sin(i / 40) + 1) * 0.5 * 0.2;
			double gr = (Math.sin(i / 30) + 1) * 0.5 * 0.2;
			double rr = (Math.sin(i / 20) + 1) * 0.5 * 0.2;

			graf.setColor(new Color((float) br, (float) gr, (float) rr));
			graf.setFont(new Font("Arial", Font.PLAIN, 7));
			graf.drawString(ad, (int) (Math.sin(i /10)*5), 7 + j * 8);
			j++;
		}
	}

	@Override
	protected void writeError(String string) throws IOException {
		System.err.println(string);
	}

	@Override
	protected void close() throws IOException {

	}

}
