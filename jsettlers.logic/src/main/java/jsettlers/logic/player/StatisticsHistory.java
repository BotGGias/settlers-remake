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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.IStatisticsHistory;
import jsettlers.common.statistics.StatisticsSeries;

/**
 * Stores the values of {@link EStatisticType} of a player over the course of a game.
 * <p>
 * To keep long games small, every second sample is dropped as soon as {@link #MAX_SAMPLES} samples are stored. From then on, samples are only
 * stored if they are at least twice as far apart as before.
 */
public class StatisticsHistory implements IStatisticsHistory, Serializable {
	private static final long serialVersionUID = 1L;

	static final int MAX_SAMPLES = 400;
	private static final int INITIAL_CAPACITY = 16;

	private int sampleCount = 0;
	private int minSampleDistanceMs = 0;
	private int[] times = new int[INITIAL_CAPACITY];
	private int[][] values = new int[EStatisticType.NUMBER_OF_TYPES][INITIAL_CAPACITY];

	/**
	 * Adds a sample to the history.
	 *
	 * @param timeMs
	 *            The game time of the sample. It must not be before the time of the last sample.
	 * @param sampleValues
	 *            The value of each {@link EStatisticType} (by ordinal).
	 * @param force
	 *            If true, the sample is stored even if it is closer to the last sample than required. A sample with the same time as the last
	 *            one replaces it.
	 */
	public synchronized void addSample(int timeMs, int[] sampleValues, boolean force) {
		if (sampleValues.length != EStatisticType.NUMBER_OF_TYPES) {
			throw new IllegalArgumentException("Expected " + EStatisticType.NUMBER_OF_TYPES + " values but got " + sampleValues.length);
		}

		if (sampleCount > 0) {
			int lastTime = times[sampleCount - 1];
			if (timeMs < lastTime) {
				return;
			}
			// a little tolerance, because the timer does not fire exactly on time
			if (timeMs == lastTime || (timeMs - lastTime < minSampleDistanceMs * 9 / 10 && !force)) {
				if (force) {
					setSample(sampleCount - 1, timeMs, sampleValues);
				}
				return;
			}
		}

		if (sampleCount >= MAX_SAMPLES) {
			dropEverySecondSample(timeMs);
		}
		ensureCapacity(sampleCount + 1);
		setSample(sampleCount, timeMs, sampleValues);
		sampleCount++;
	}

	private void setSample(int index, int timeMs, int[] sampleValues) {
		times[index] = timeMs;
		for (int type = 0; type < sampleValues.length; type++) {
			values[type][index] = sampleValues[type];
		}
	}

	private void dropEverySecondSample(int newTimeMs) {
		int newCount = 0;
		for (int i = 0; i < sampleCount; i += 2) {
			times[newCount] = times[i];
			for (int[] typeValues : values) {
				typeValues[newCount] = typeValues[i];
			}
			newCount++;
		}
		// the distance of the remaining samples; the first two samples may be closer if the game was saved and loaded
		int distance = newCount > 1 ? (times[newCount - 1] - times[0]) / (newCount - 1) : newTimeMs - times[0];
		minSampleDistanceMs = Math.max(minSampleDistanceMs * 2, distance);
		sampleCount = newCount;
	}

	private void ensureCapacity(int capacity) {
		if (times.length < capacity) {
			int newCapacity = Math.max(capacity, times.length * 2);
			times = Arrays.copyOf(times, newCapacity);
			for (int type = 0; type < values.length; type++) {
				values[type] = Arrays.copyOf(values[type], newCapacity);
			}
		}
	}

	public synchronized int getSampleCount() {
		return sampleCount;
	}

	@Override
	public synchronized StatisticsSeries copySeries() {
		int[][] copiedValues = new int[values.length][];
		for (int type = 0; type < values.length; type++) {
			copiedValues[type] = Arrays.copyOf(values[type], sampleCount);
		}
		return new StatisticsSeries(Arrays.copyOf(times, sampleCount), copiedValues);
	}

	private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}
}
