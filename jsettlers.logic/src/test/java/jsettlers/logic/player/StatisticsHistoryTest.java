/*******************************************************************************
 * Copyright (c) 2026
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.StatisticsSeries;

public class StatisticsHistoryTest {
	private static final int MINUTE = 60 * 1000;

	private static int[] values(int value) {
		int[] values = new int[EStatisticType.NUMBER_OF_TYPES];
		for (int i = 0; i < values.length; i++) {
			values[i] = value + i;
		}
		return values;
	}

	@Test
	public void testAddSamples() {
		StatisticsHistory history = new StatisticsHistory();
		for (int i = 0; i < 20; i++) {
			history.addSample(i * MINUTE, values(i * 10), false);
		}

		StatisticsSeries series = history.copySeries();
		assertEquals(20, series.getSampleCount());
		assertEquals(7 * MINUTE, series.getTime(7));
		assertEquals(70, series.getValue(EStatisticType.SETTLERS, 7));
		assertEquals(70 + EStatisticType.GOLD.ordinal(), series.getValue(EStatisticType.GOLD, 7));
		assertEquals(190 + EStatisticType.LAND.ordinal(), series.getLastValue(EStatisticType.LAND));
		assertEquals(19 * MINUTE, series.getLastTime());
	}

	@Test
	public void testCopyIsNotChangedByLaterSamples() {
		StatisticsHistory history = new StatisticsHistory();
		history.addSample(0, values(1), false);
		StatisticsSeries series = history.copySeries();

		history.addSample(MINUTE, values(2), false);

		assertEquals(1, series.getSampleCount());
		assertEquals(2, history.copySeries().getSampleCount());
	}

	@Test
	public void testEmptyHistory() {
		StatisticsSeries series = new StatisticsHistory().copySeries();
		assertEquals(0, series.getSampleCount());
		assertEquals(0, series.getLastValue(EStatisticType.SETTLERS));
		assertEquals(0, series.getMaxValue(EStatisticType.SETTLERS));
		assertEquals(0, series.getLastTime());
	}

	@Test
	public void testForcedSampleReplacesSampleWithSameTime() {
		StatisticsHistory history = new StatisticsHistory();
		history.addSample(0, values(1), false);
		history.addSample(MINUTE, values(2), false);
		history.addSample(MINUTE, values(3), false);
		assertEquals(2, history.copySeries().getLastValue(EStatisticType.SETTLERS));

		history.addSample(MINUTE, values(4), true);
		StatisticsSeries series = history.copySeries();
		assertEquals(2, series.getSampleCount());
		assertEquals(4, series.getLastValue(EStatisticType.SETTLERS));
	}

	@Test
	public void testSamplesBeforeLastSampleAreIgnored() {
		StatisticsHistory history = new StatisticsHistory();
		history.addSample(2 * MINUTE, values(1), false);
		history.addSample(MINUTE, values(2), true);

		StatisticsSeries series = history.copySeries();
		assertEquals(1, series.getSampleCount());
		assertEquals(2 * MINUTE, series.getTime(0));
	}

	@Test
	public void testLongGamesAreThinnedOut() {
		StatisticsHistory history = new StatisticsHistory();
		int minutes = StatisticsHistory.MAX_SAMPLES * 3;
		for (int i = 0; i <= minutes; i++) {
			history.addSample(i * MINUTE, values(i), false);
		}

		StatisticsSeries series = history.copySeries();
		assertTrue(series.getSampleCount() <= StatisticsHistory.MAX_SAMPLES);
		assertTrue(series.getSampleCount() >= StatisticsHistory.MAX_SAMPLES / 2);
		assertEquals(0, series.getTime(0));
		assertEquals(minutes * MINUTE, series.getLastTime());
		assertEquals(minutes, series.getLastValue(EStatisticType.SETTLERS));

		// the samples are spread evenly over the game
		int distance = series.getTime(1) - series.getTime(0);
		for (int i = 1; i < series.getSampleCount(); i++) {
			assertEquals(distance, series.getTime(i) - series.getTime(i - 1));
		}
	}

	@Test
	public void testForcedSampleIsStoredAfterThinningOut() {
		StatisticsHistory history = new StatisticsHistory();
		for (int i = 0; i <= StatisticsHistory.MAX_SAMPLES; i++) {
			history.addSample(i * MINUTE, values(i), false);
		}
		int lastTime = history.copySeries().getLastTime();

		history.addSample(lastTime + MINUTE / 2, values(4711), true);

		StatisticsSeries series = history.copySeries();
		assertEquals(lastTime + MINUTE / 2, series.getLastTime());
		assertEquals(4711, series.getLastValue(EStatisticType.SETTLERS));
	}

	@Test
	public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
		StatisticsHistory history = new StatisticsHistory();
		for (int i = 0; i < 5; i++) {
			history.addSample(i * MINUTE, values(i), false);
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
			oos.writeObject(history);
		}
		StatisticsHistory read;
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			read = (StatisticsHistory) ois.readObject();
		}

		assertEquals(history.copySeries(), read.copySeries());
		read.addSample(5 * MINUTE, values(5), false);
		assertEquals(6, read.getSampleCount());
	}
}
