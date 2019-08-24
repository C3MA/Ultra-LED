import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class LedLib {
	static int blocks = 6;
	static int rows_block = 4;
	
	byte[] buffer = new byte[1+rows_block*41*3];
	BufferedImage data = new BufferedImage(41,24,BufferedImage.TYPE_3BYTE_BGR);

	private InetSocketAddress address;
	private DatagramSocket datagramSocket;
	private float scale = 1;
	private boolean swapGreenRed;
	
	public LedLib(String address,int port) throws SocketException {
		this.address = new InetSocketAddress(address,port);
		datagramSocket = new DatagramSocket();
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public void setRGB(int x, int y, Color rgb){
		int[] col = new int[3];
		col[0] = rgb.getRed();
		col[1] = rgb.getGreen();
		col[2] = rgb.getBlue();
		data.getRaster().setPixel(x, y, col);
	}

	public void send(int part) throws IOException, InterruptedException {
		int i = part;
		int ledOffset = i * rows_block * 41;
		int ledEnd = rows_block * 41 + ledOffset;
		buffer[0] = (byte) (i * rows_block);
		for (int led = ledOffset; led < ledEnd; led++) {
			int row = led/41;
			int subl = led%41;
			
			int data = this.data.getRGB(subl,row);
			Color col = new Color(data);
			if(swapGreenRed){
				writeColorByte(buffer, (led - ledOffset) * 3 + 1, (int) (col.getBlue()*scale));
				writeColorByte(buffer, (led - ledOffset) * 3 + 2, (int) (col.getGreen()*scale));
				writeColorByte(buffer, (led - ledOffset) * 3 + 3, (int) (col.getRed()*scale));

			}else{
				writeColorByte(buffer, (led - ledOffset) * 3 + 1, (int) (col.getGreen()*scale));
				writeColorByte(buffer, (led - ledOffset) * 3 + 2, (int) (col.getBlue()*scale));
				writeColorByte(buffer, (led - ledOffset) * 3 + 3, (int) (col.getRed()*scale));
			}
		}
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address);
		try {
			datagramSocket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		i++;
		if(i == blocks){
			i = 0;
		}
	}
	
	private static void writeColorByte(byte[] buffer, int i, int red) {
		buffer[i] = (byte) (red);
	}

	public void setImage(BufferedImage image) {
		data = image;
	}

	public void swapShit(boolean b) {
		this.swapGreenRed = b;
	}
}
