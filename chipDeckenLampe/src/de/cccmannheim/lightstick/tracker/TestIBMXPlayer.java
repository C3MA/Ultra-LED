/*
 * Decompiled with CFR 0_114.
 */
package de.cccmannheim.lightstick.tracker;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import ibxm.INoteListener;
import ibxm.jme.IBXMAdvancedLoader;
import ibxm.jme.NoteInfo;

public class TestIBMXPlayer {
	public static final int												LED_PER_STICK		= 41;
	public static final int												STICK_COUNT			= 24;
	private static final float											TICKS_PER_SECOND	= 20.0f;
	private static final int											REPEATLED			= 20;
	private static float												BRIGHTNESS			= 255.0f;
	static File															folder				= new File("./music");
	static float[]														packet				= new float[2952];
	private static long													startTime			= 0;
	protected static ConcurrentHashMap<Float, Map<NoteInfo, NoteInfo>>	todispatch			= new ConcurrentHashMap();
	private static Color[]												colorMap;
	static DatagramSocket												datagramSocket;
	private static List<File>											fileList;
	static int															fileId;
	private static IBXMAdvancedLoader									anode;
	private static int													tracks;
	private static File													currentFile;
	private static byte[]												buffer;
	private static int													bufferIndex			= 0;
	private static InetSocketAddress address;

	static int blocks = 6;
	static int rows_block = 4;
	
	static {
		TestIBMXPlayer.fileId = -1;
		TestIBMXPlayer.tracks = 1;
	}

	public static void main(final String[] args) throws IOException {
		if (args.length > 0) {
			TestIBMXPlayer.BRIGHTNESS = Float.parseFloat(args[0]);
		}
		if (args.length == 0) {
			System.out.println("offline mode, no birhtness and ip given");
		} else {
			buffer = new byte[1+rows_block*41*3];
			address = new InetSocketAddress("192.168.4.1", 10000);
			datagramSocket = new DatagramSocket();

		}
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
					TestIBMXPlayer.processInput(line);
				} while (true);
			}
		}.start();
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					TestIBMXPlayer.update();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, Math.round(15.0f), Math.round(15.0f));
	}

	private static void refreshFilelist() {
		final File[] files = TestIBMXPlayer.folder.listFiles();
		TestIBMXPlayer.fileList = new ArrayList<>(Arrays.asList(files));
		Collections.shuffle(TestIBMXPlayer.fileList);
		System.out.println("Using " + TestIBMXPlayer.fileList.size() + " trackerfiles");
	}

	private static void processInput(final String line) {
		if (line.contains("rm")) {
			try {
				System.out.println("Deleting current file " + TestIBMXPlayer.currentFile);
				TestIBMXPlayer.currentFile.delete();
				refreshFilelist();
				Thread.sleep(2000);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			TestIBMXPlayer.processInput("");
		} else if (line.isEmpty()) {
			try {
				if (TestIBMXPlayer.anode != null) {
					TestIBMXPlayer.anode.terminate();
				}
				anode = null;
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
		if (TestIBMXPlayer.anode != null) {
			TestIBMXPlayer.anode.terminate();
		}
		anode = null;

	}

	protected static void update() throws IOException, InterruptedException {
		if (TestIBMXPlayer.anode == null || TestIBMXPlayer.anode.isFinsihed()) {
			try {
				TestIBMXPlayer.playRandom();
			} catch (final Exception e) {
				TestIBMXPlayer.processInput("rm");
			}
		}
		final ArrayList<Float> removeKeys = new ArrayList<Float>();
		final float playtime = (System.currentTimeMillis() - TestIBMXPlayer.startTime) / 1000.0f;
		for (final Map.Entry<Float, Map<NoteInfo, NoteInfo>> ee : TestIBMXPlayer.todispatch.entrySet()) {
			if (ee.getKey().floatValue() >= playtime) {
				continue;
			}
			removeKeys.add(ee.getKey());
			for (final NoteInfo note : ee.getValue().keySet()) {
				final int stickid = Math.abs(note.id) % 24;
				final Color color = TestIBMXPlayer.colorMap[note.instrumentid % TestIBMXPlayer.colorMap.length];
				final int ledid = Math.abs(note.noteKey + note.panning / 20) % 20 * 3;
				float ncolor = note.volume / 64.0f * note.globalVolume / 64.0f * 125.0f / 128.0f;
				ncolor = Math.max(0.01f, ncolor);
				ncolor = Math.min(1.0f, ncolor);
				int x = ledid;
				while (x < 123) {
					final float newValueR = ncolor * (color.getRed() / 255.0f + TestIBMXPlayer.packet[stickid * 41 * 3 + x]);
					final float newValueG = ncolor * (color.getGreen() / 255.0f + TestIBMXPlayer.packet[stickid * 41 * 3 + x + 1]);
					final float newValueB = ncolor * (color.getBlue() / 255.0f + TestIBMXPlayer.packet[stickid * 41 * 3 + x + 2]);
					TestIBMXPlayer.packet[stickid * 41 * 3 + x] = Math.min(newValueR, 1.0f);
					TestIBMXPlayer.packet[stickid * 41 * 3 + x + 1] = Math.min(newValueG, 1.0f);
					TestIBMXPlayer.packet[stickid * 41 * 3 + x + 2] = Math.min(newValueB, 1.0f);
					x += 60;
				}
			}
		}
		sendPacket();
		for (final Float r : removeKeys) {
			TestIBMXPlayer.todispatch.remove(r);
		}
		int i = 0;
		while (i < TestIBMXPlayer.packet.length) {
			float fader = 0.05f;
			if (TestIBMXPlayer.packet[i] > fader) {
				final float[] arrf = TestIBMXPlayer.packet;
				final int n = i;
				arrf[n] = (float) (arrf[n] - fader);
			} else {
				TestIBMXPlayer.packet[i] = 0.0f;
			}
			++i;
		}


	}

	static int curblock = 0;
	private static void sendPacket() throws IOException, InterruptedException {
		int i = curblock;
		int ledOffset = i * rows_block * 41;
		int ledEnd = rows_block * 41 + ledOffset;
		buffer[0] = (byte) (i * rows_block);
		for (int led = ledOffset; led < ledEnd; led++) {
			int row = led/41;
			int subl = led%41;
			row = row%tracks;
			int rled = row*41+subl;
			
			writeColorByte(buffer, (led - ledOffset) * 3 + 1, packet[rled*3]);
			writeColorByte(buffer, (led - ledOffset) * 3 + 2, packet[rled*3+1]);
			writeColorByte(buffer, (led - ledOffset) * 3 + 3, packet[rled*3+2]);
		}
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address);
		datagramSocket.send(packet);
		curblock++;
		if(curblock == blocks){
			curblock = 0;
		}
	}
	
	private static void writeColorByte(byte[] buffer, int i, float red) {
		buffer[i] = (byte) (int) (red*25f);
	}

	protected static synchronized void playRandom() {
		File file;
		int i = 0;
		while (i < 100) {
			System.out.println();
			++i;
		}
		final ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(Color.BLUE);
		colors.add(Color.red);
		colors.add(Color.YELLOW);
		colors.add(Color.ORANGE);
		colors.add(Color.MAGENTA);
		colors.add(Color.green);
		TestIBMXPlayer.colorMap = colors.toArray(new Color[colors.size()]);
		if (++TestIBMXPlayer.fileId > TestIBMXPlayer.fileList.size() - 1) {
			TestIBMXPlayer.fileId = 0;
		}
		if ((file = TestIBMXPlayer.fileList.get(TestIBMXPlayer.fileId)).getName().toLowerCase().endsWith(".xm") || file.getName().toLowerCase().endsWith(".mod")
				|| file.getName().toLowerCase().endsWith(".s3m")) {
			TestIBMXPlayer.play(file);
		}
	}

	private static synchronized void play(final File fname) {
		TestIBMXPlayer.currentFile = fname;
		System.out.println(fname + " trackerLight");
		if (TestIBMXPlayer.anode != null) {
			TestIBMXPlayer.anode.terminate();
		}
		try {
			TestIBMXPlayer.anode = new IBXMAdvancedLoader(fname, new INoteListener() {

				@Override
				public void onNote(final float posInSec, final int id, final int volume, final int noteKey, final int fadeoutVol, final int instrumentid, final int panning, final int freq) {
					Map<NoteInfo, NoteInfo> notelist = TestIBMXPlayer.todispatch.get(Float.valueOf(posInSec));
					if (notelist == null) {
						notelist = new ConcurrentHashMap<NoteInfo, NoteInfo>();
						TestIBMXPlayer.todispatch.put(Float.valueOf(posInSec), notelist);
					}
					final NoteInfo vv = new NoteInfo(id, volume, noteKey, fadeoutVol, instrumentid, panning, freq);
					notelist.put(vv, vv);
				}
			});
			TestIBMXPlayer.tracks = Math.max(TestIBMXPlayer.anode.determineTracks(), 5);
			System.out.println("Trackcount is " + TestIBMXPlayer.tracks);
			System.out.println("Press enter for next song, input name and enter to search for songs");
			TestIBMXPlayer.startTime = System.currentTimeMillis();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
