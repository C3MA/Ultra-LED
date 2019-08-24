import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class Cyber {
	static float pulse = 0;
	static float color = 0.1f;
	private static boolean tick;
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException, AWTException, Exception {
		LedLib led = new LedLib("192.168.4.1", 10000);

		BufferedImage image = new BufferedImage(41, 24, BufferedImage.TYPE_4BYTE_ABGR);

		while (true) {
			led.setScale(1f);
			if(tick) {
				pulse += 0.1;
				if(pulse > 1) {
					pulse = 1;
					tick = false;
				}
			}else {
				pulse -= 0.2;
			}
			if(pulse < 0 && !tick) {
				pulse = 0;
				color += 0.1f;
				tick = true;
				Thread.sleep(5000);
			}
			doImage(image);
			led.setImage(image);
			led.send(0);
			Thread.sleep(4);
			led.send(1);
			Thread.sleep(4);
			led.send(2);
			Thread.sleep(4);
			led.send(3);
			Thread.sleep(4);
			led.send(4);
			Thread.sleep(4);
			led.send(5);
			Thread.sleep(100);

			// picLabel.setIcon(new ImageIcon(smalle));

			// debug.pack();

		}

	}

	private static void doImage(BufferedImage image) {
		Graphics g = image.getGraphics();
		g.clearRect(0, 0, 41, 24);
		g.setColor(Color.getHSBColor(color, 1, pulse));		
		g.fillRect(0, 0, 41, 24);
	}
}
