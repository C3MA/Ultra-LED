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

public class WelcomeFraMa {
	static OpenCVFrameConverter.ToIplImage	grabberConverter	= new OpenCVFrameConverter.ToIplImage();
	static Java2DFrameConverter				paintConverter		= new Java2DFrameConverter();
	private static ArrayList<String>		texts;
	private static LedLib					led;
	private static BufferedImage			image;

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException, AWTException, Exception {
		led = new LedLib("192.168.4.1", 10000);

		texts = new ArrayList<>();
		texts.add("FraMa");
		texts.add("C3MA");
		texts.add("CCCFFM");
		led.swapShit(true);

		image = new BufferedImage(41, 24, BufferedImage.TYPE_4BYTE_ABGR);

		while (true) {
			doIter(true, false);
			doIter(false, false);
			doIter(true, false);
			doIter(false, false);
			doIter(false, true);
			doIter(false, false);
			doIter(false, true);
			doIter(false, false);
		}

	}

	private static void doIter(boolean b, boolean c) throws InterruptedException, IOException {
		led.setScale(0.2f);
		doImage(image, b, c);
		led.setImage(image);
		Thread.sleep(5);

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
		Thread.sleep(15);

	}

	private static void doImage(BufferedImage image, boolean blue, boolean red) {
		Graphics g = image.getGraphics();
		g.clearRect(0, 0, 41, 24);
//		if (blue) {
			g.setColor(Color.white);
			g.fillRect(0, 0, 42, 24);
//		}
//		if (red) {
//			g.setColor(Color.blue);
//			g.fillXRect(0, 0, 42, 24);
//		}
	}
}
