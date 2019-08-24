package de.empire.deckenlampe;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

public abstract class AbstractClientHandler extends Thread {
	private static boolean	PROFILING	= false;

	private short[]			readBuffer	= new short[Test.LEDS_PER_ROW * Test.ROWS * 3];
	private short[]			data;

	private boolean			active		= true;

	public AbstractClientHandler(final short[] data) {
		this.data = data;
	}

	@Override
	public void run() {
		try {
			final DataInput in = getDataIn();
			while (true) {
				try {
					final int amountLeds = in.readInt();
					if (amountLeds > Test.LEDS_PER_ROW * Test.ROWS) {
						writeError("to many leds terminating");
						close();
						return;
					}
					final int rows = (int) Math.ceil(amountLeds / (float) Test.LEDS_PER_ROW);
					int j = 0;
					int rcounter = amountLeds;
					while (rcounter > 0) {
						rcounter--;
						short b = in.readShort();
						b = (short) Math.min(b, 255);
						this.readBuffer[j] = b;
						j++;
						short g = in.readShort();
						g = (short) Math.min(g, 255);
						this.readBuffer[j] = g;
						j++;
						short r = in.readShort();
						r = (short) Math.min(r, 255);
						this.readBuffer[j] = r;
						j++;
					}
					for (int row = 0; row < rows; row++) {
						for (int led = 0; led < Test.LEDS_PER_ROW; led++) {
							final short b = this.readBuffer[(row * Test.LEDS_PER_ROW + led) * 3];
							final short g = this.readBuffer[(row * Test.LEDS_PER_ROW + led) * 3 + 1];
							final short r = this.readBuffer[(row * Test.LEDS_PER_ROW + led) * 3 + 2];
							if (active) {

								if (row % 2 == 0) {
									this.data[(row * Test.LEDS_PER_ROW + led) * 4 + 4] = 255;
									this.data[(row * Test.LEDS_PER_ROW + led) * 4 + 5] = b;
									this.data[(row * Test.LEDS_PER_ROW + led) * 4 + 6] = g;
									this.data[(row * Test.LEDS_PER_ROW + led) * 4 + 7] = r;
								} else {
									this.data[(row * Test.LEDS_PER_ROW + (Test.LEDS_PER_ROW - 1 - led)) * 4 + 4] = 255;
									this.data[(row * Test.LEDS_PER_ROW + (Test.LEDS_PER_ROW - 1 - led)) * 4 + 5] = b;
									this.data[(row * Test.LEDS_PER_ROW + (Test.LEDS_PER_ROW - 1 - led)) * 4 + 6] = g;
									this.data[(row * Test.LEDS_PER_ROW + (Test.LEDS_PER_ROW - 1 - led)) * 4 + 7] = r;
								}
							}
						}
					}
				} catch (final EOFException e) {
					e.printStackTrace();
					try {
						close();
					} catch (final Exception e1) {
						e1.printStackTrace();
					}
					return;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract DataInput getDataIn() throws IOException;

	protected abstract void writeError(String string) throws IOException;

	protected abstract void close() throws IOException;

	public void setActive(boolean empty) {
		this.active = empty;
	}
}
