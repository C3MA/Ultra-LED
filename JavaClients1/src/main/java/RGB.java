import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.awt.Color;

public class RGB {

    public static void main(String[] args) throws UnknownHostException,
IOException, InterruptedException, AWTException, Exception {
        LedLib led = new LedLib("192.168.4.1", 10000);

        led.swapShit(true);

        while (true) {
            led.setScale(0.10f);
            System.out.println("Red");
            led.setImage(drawImage(Color.RED.getRGB()));
           
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

            Thread.sleep(2000);
            System.out.println("Green");
            led.setImage(drawImage(Color.GREEN.getRGB()));

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

            Thread.sleep(2000);
            System.out.println("Blue");
            led.setImage(drawImage(Color.BLUE.getRGB()));
           
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
           
            Thread.sleep(2000);
        }

    }
    
    private static BufferedImage drawImage(int colorInt) {
        BufferedImage image = new BufferedImage(41,24,BufferedImage.TYPE_3BYTE_BGR);

        for (int x = 0; x < 41; x++) {
            for (int y = 0; y < 24; y++) {
                image.setRGB( x, y, colorInt );
            }
        }

        return image;
    }

    private static BufferedImage getScaledImage(BufferedImage src, int
w, int h) {
        int finalw = w;
        int finalh = h;
        BufferedImage resizedImg = new BufferedImage(finalw, finalh,
BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
    }
}
