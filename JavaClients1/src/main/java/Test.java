import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class Test {
	static OpenCVFrameConverter.ToIplImage	grabberConverter	= new OpenCVFrameConverter.ToIplImage();
	static Java2DFrameConverter				paintConverter		= new Java2DFrameConverter();

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException, AWTException, Exception {
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();

		// JFrame debug = new JFrame("deb");
		// debug.setAlwaysOnTop(true);
		// JLabel picLabel = new JLabel();
		// debug.add(picLabel);

		// debug.setVisible(true);

		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(":0.0+" + 0 + "," + 0);
		grabber.setFormat("x11grab");
		grabber.setImageWidth((int) ss.getWidth());
		grabber.setImageHeight((int) ss.getHeight());
		grabber.start();

		LedLib led = new LedLib("192.168.4.1", 10000);

		led.swapShit(true);
		
		while (true) {
			led.setScale(0.05f);
			Frame grabed = grabber.grab();

			BufferedImage image = paintConverter.convert(grabed);
			image = getScaledImage(image, 41 * 8, 24 * 8);
			image = getScaledImage(image, 41 * 4, 24 * 4);
			image = getScaledImage(image, 41 * 2, 24 * 2);
			image = getScaledImage(image, 41, 24);
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
			Thread.sleep(7);
			
			// picLabel.setIcon(new ImageIcon(smalle));

			// debug.pack();

			
		}

	}

	private static BufferedImage getScaledImage(BufferedImage src, int w, int h) {
		int finalw = w;
		int finalh = h;
		BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(src, 0, 0, finalw, finalh, null);
		g2.dispose();
		return resizedImg;
	}
}
