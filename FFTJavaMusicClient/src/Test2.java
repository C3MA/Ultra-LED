import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthScrollBarUI;

public class Test2 {

	private static float roller;

	public static void main(String[] args) throws IOException, InterruptedException {
		LedLib led = new LedLib("192.168.4.1", 10000);
		led.setScale(0.025f);
		String data = "";
		Scanner in = new Scanner(new File("./test.txt"));
		while (in.hasNextLine()) {
			data += in.nextLine();
		}
		int offset = 41;
		Font font = new Font("monospaced", Font.PLAIN, 20);
		Color color = randomColor();
		double number = 0;
		while (true) {
			Graphics g = led.data.getGraphics();
			g.setFont(font);
			color = randomColor();

			int width = g.getFontMetrics().stringWidth(data);
			g.clearRect(0, 0, 41, 24);
			g.setColor(color);
			g.drawString(data, offset, (int) (Math.sin(number*0.01f)*6f+18));
			offset--;
			if (offset < -width - 100) {
				number++;
				offset = 41;
			}
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

	}

	private static Color randomColor() {
		roller += 0.01f;
		return	new Color(Color.HSBtoRGB(roller, 1, 1));
	}

}
