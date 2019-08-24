import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

public class Test {
	protected static final int		bandStoreSize	= 8;
	static Minim					minim;
	static AudioPlayer				player;
	private static FFT				fft;
	private static BeatDetect		BEAT_FREQUENCY_MODE;
	private static BeatDetect		BEAT_ENERGY_MODE;
	static File						folder			= new File("./music");
	private static ArrayList<File>	fileList;
	private static File				currentFile;
	private static int				fileId;
	protected float					flowCount;
	protected float					fb2;
	private LedLib					led;
	private float[]					bandstore;

	private boolean					onset;
	private boolean					snare;
	private boolean					kick;
	private boolean					hat;

	private static void refreshFilelist() {
		final File[] files = folder.listFiles();
		fileList = new ArrayList<>(Arrays.asList(files));
		Collections.shuffle(fileList);
		System.out.println("Using " + fileList.size() + " files");
	}

	private static void processInput(final String line) {
		if (line.contains("rm")) {
			try {
				System.out.println("Deleting current file " + currentFile);
				currentFile.delete();
				refreshFilelist();
				Thread.sleep(2000);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			processInput("");
		} else if (line.isEmpty()) {
			try {
				minim.stop();
				player.pause();
			} catch (final Exception e) {
				// empty catch block
			}
		} else {
			searchAndPlay(line);
		}

		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void searchAndPlay(String line) {
		refreshFilelist();
		List<File> matches = new ArrayList<>();
		for (File f : fileList) {
			if (f.getName().toLowerCase().contains(line.toLowerCase())) {
				matches.add(f);
			}
		}

		if (matches.isEmpty()) {
			System.out.println("No matches found for " + line);
			return;
		}
		if (matches.size() > 1) {
			System.out.println("Multiple matches found for " + line + " please specify");
			for (File f : matches) {
				System.out.println(f.getName());
			}
			return;
		}
		File toplay = matches.get(0);
		System.out.println("Switching to " + toplay.getName());
		int targetIndex = fileList.indexOf(toplay) - 1;
		fileId = targetIndex;
	}

	protected static synchronized void playRandom() {
		File file;
		int i = 0;
		while (i < 100) {
			System.out.println();
			++i;
		}
		if (++fileId > fileList.size() - 1) {
			fileId = 0;
		}
		if ((file = fileList.get(fileId)).getName().toLowerCase().endsWith(".mp3")) {
			play(file);
		}
	}

	private synchronized static void play(final File fname) {
		currentFile = fname;
		try {
			player = minim.loadFile(folder.getName() + "/" + fname.getName());
			fft = new FFT(player.bufferSize(), player.sampleRate());
			BEAT_FREQUENCY_MODE = new BeatDetect(player.bufferSize(), player.sampleRate());
			BEAT_ENERGY_MODE = new BeatDetect();
			player.play();
			System.out.println(fname + " trackerLight");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setup() throws SocketException {
		refreshFilelist();
		final Scanner cin = new Scanner(System.in);
		new Thread() {

			@Override
			public void run() {
				do {
					if (!cin.hasNextLine()) {
						continue;
					}
					final String line = cin.nextLine();
					processInput(line);
				} while (true);
			}
		}.start();

		minim = new Minim(this);
		JFrame frame = new JFrame();

		bandstore = new float[bandStoreSize];
		JPanel panel = new JPanel() {
			private float b2;

			@Override
			protected void paintComponent(Graphics g) {
				try {
					super.paintComponent(g);
					int split = 4;
					if (player == null || !player.isPlaying()) {
						playRandom();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						return;
					}
					fft.forward(player.mix);
					BEAT_FREQUENCY_MODE.detect(player.mix);
					BEAT_ENERGY_MODE.detect(player.mix);

					hat = BEAT_FREQUENCY_MODE.isHat();
					kick = BEAT_FREQUENCY_MODE.isKick();
					onset = BEAT_ENERGY_MODE.isOnset();
					snare = BEAT_FREQUENCY_MODE.isSnare();
					// System.out.println("hat " + hat);
					// System.out.println("kick " + kick);
					// System.out.println("onset " + onset);
					// System.out.println("snare " + snare);

					g.setColor(new Color(hat ? 1f : 0f, kick ? 1 : 0f, onset ? 1 : 0));
					g.fillRect(0, 0, getWidth() / split, getHeight());
					g.setColor(Color.BLACK);
					g.fillRect(getWidth() / split, 0, getWidth(), getHeight());
					for (int x = 0; x < bandStoreSize; x++) {

						float detectScaler = (((float) bandStoreSize) / (BEAT_FREQUENCY_MODE.detectSize() - 1));
						int band = Math.round(x / detectScaler);
						if (BEAT_FREQUENCY_MODE.isOnset(band)) {
							bandstore[x] = bandstore[x] + 1f;
							if (bandstore[x] > 1) {
								bandstore[x] = 1;
							}

						}
						g.setColor(new Color(1, bandstore[x], b2));
						g.drawLine(x, 0, x, (int) (fft.getBand(x) * 5));
						if (bandstore[x] > 0.01) {
							bandstore[x] = bandstore[x] - 0.01f;
						} else {
							bandstore[x] = 0.0f;
						}
					}
					doRun();
				} finally {
					repaint();
				}
			}
		};
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setSize(bandStoreSize, 500);
		panel.setBackground(Color.BLACK);
		frame.setSize(260, 510);
		frame.add(panel);
		frame.setVisible(true);

		led = new LedLib("192.168.4.1", 10000);
		led.setScale(0.01f);
	}

	public void doRun() {
		try {
			update(bandstore, led);
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
			Thread.sleep(10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void update(float[] bandstore, LedLib led) {
		System.out.println(kick);
		for (int x = 0; x < 41; x++) {
			for (int y = 0; y < 24; y++) {
				led.setRGB(x, y, new Color(kick ? 0f : 0f, snare ? 1f : 0f, hat ? 0f : 0f));
			}
		}
	}

	public static void main(String[] args) throws SocketException {
		new Test().setup();
	}

	public InputStream createInput(String fileName) {
		try {
			return new FileInputStream(new File("./" + fileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
