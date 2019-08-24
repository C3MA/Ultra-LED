package de.empire.deckenlampe;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import com.pi4j.wiringpi.Spi;

public class Test {
	public static int							LEDS_PER_ROW	= 41;
	public static int							ROWS			= 24;

	private static List<AbstractClientHandler>	clients			= new CopyOnWriteArrayList<>();
	private static volatile boolean				active			= true;
	static AbstractClientHandler				idleClient;

	private static volatile boolean				overHeat		= true;

	public static void main(final String[] args) throws InterruptedException, UnknownHostException, IOException {
		final int fd = Spi.wiringPiSPISetup(Spi.CHANNEL_0, 5_000_000);
		if (fd == -1) {
			throw new RuntimeException("Could not open SPI");
		}

		final short[] data = new short[Test.ROWS * Test.LEDS_PER_ROW * 4 + 4];
		data[0] = 0;
		data[1] = 0;
		data[2] = 0;
		data[3] = 0;

		for (int i = 4; i < data.length; i++) {
			if (i % 4 == 0) {
				data[i] = 255;
			} else {
				data[i] = 1;
			}
		}

		idleClient = new IdleClient(data);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutdown init");
				Test.active = false;
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				for (int i = 4; i < data.length; i++) {
					if (i % 4 == 0) {
						data[i] = 255;
					} else {
						data[i] = 1;
					}
				}
				Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, data);
				try {
					Thread.sleep(200);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Shutdown finish");
			}
		});

		final short[] transActionBuffer = new short[data.length];

		new Thread() {
			@Override
			public void run() {
				final File oneWires = new File("/sys/bus/w1/devices");
				while (Test.active) {
					try {
						final File[] devices = oneWires.listFiles();
						boolean oh = false;
						for (final File device : devices) {
							if (!device.getName().contains("master")) {
								try (Scanner read = new Scanner(new File(device, "w1_slave"))) {
									read.nextLine();
									final String line2 = read.nextLine();
									final int split = line2.indexOf("t=") + 2;
									final String temp = line2.substring(split);
									final float tempf = Float.parseFloat(temp) / 1000;
									if (tempf > 50) {
										oh = true;
									}
									System.out.println("temp " + tempf);
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}
						}

						Test.overHeat = oh;
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();

		new Thread() {
			@Override
			public void run() {
				while (Test.active) {
					for (final AbstractClientHandler client : Test.clients) {
						if (!client.isAlive()) {
							Test.clients.remove(client);
						}
					}

					idleClient.setActive(Test.clients.isEmpty());
					if (Test.overHeat) {
						final short[] ohBuffer = data.clone();
						this.middleDot(ohBuffer, 3);
						System.arraycopy(ohBuffer, 0, transActionBuffer, 0, data.length);
					} else {
						System.arraycopy(data, 0, transActionBuffer, 0, data.length);
					}

					Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, transActionBuffer);
					try {
						Thread.sleep(50);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Finsihed spi writer");
			}

			private void middleDot(final short[] data, final int offset) {
				final float gen = System.nanoTime() / 1000000000f;
				final double target1 = (Math.sin(gen) + 1) * 20;
				for (int i = 4; i < data.length; i++) {
					final int left = i % 4;
					if (left == 0) {
						data[i] = 255;
					} else {
						data[i] = 0;
					}
					data[(data.length - 4) / 2 + offset + Test.LEDS_PER_ROW / 2 * 4 - Test.LEDS_PER_ROW * 4 + 4] = (short) target1;
					data[(data.length - 4) / 2 + offset + Test.LEDS_PER_ROW / 2 * 4 + 4] = (short) target1;
				}
			};
		}.start();

		final ServerSocket serverSocket = new ServerSocket(10000);
		final Thread acceptor = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						final Socket clientsocker = serverSocket.accept();
						final AbstractClientHandler client = new NetworkClientHandler(clientsocker, data);
						client.start();
						clients.add(client);
					} catch (final InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
			};
		};
		acceptor.setDaemon(true);
		acceptor.start();
		
		idleClient.start();
	}

	protected static void idleClient() {
		// TODO Auto-generated method stub

	}
}
