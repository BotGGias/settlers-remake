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
package jsettlers.common.statistics;

import java.io.Serializable;
import java.util.Arrays;

/**
 * An immutable series of samples of all {@link EStatisticType}s of one player. The samples are sorted by their time.
 */
public final class StatisticsSeries implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final StatisticsSeries EMPTY = new StatisticsSeries(new int[0], new int[EStatisticType.NUMBER_OF_TYPES][0]);

	private final int[] times;
	private final int[][] values;

	/**
	 * @param times
	 *            The game time in milliseconds of each sample.
	 * @param values
	 *            For each {@link EStatisticType} (by ordinal) the value of each sample. Every array needs the length of times.
	 */
	public StatisticsSeries(int[] times, int[][] values) {
		if (values.length != EStatisticType.NUMBER_OF_TYPES) {
			throw new IllegalArgumentException("Expected values for " + EStatisticType.NUMBER_OF_TYPES + " types but got " + values.length);
		}
		this.times = times.clone();
		this.values = new int[values.length][];
		for (int type = 0; type < values.length; type++) {
			if (values[type].length != times.length) {
				throw new IllegalArgumentException("Expected " + times.length + " values for " + EStatisticType.VALUES[type]);
			}
			this.values[type] = values[type].clone();
		}
	}

	public int getSampleCount() {
		return times.length;
	}

	/**
	 * @return The game time of the sample in milliseconds.
	 */
	public int getTime(int sample) {
		return times[sample];
	}

	public int getValue(EStatisticType type, int sample) {
		return values[type.ordinal()][sample];
	}

	/**
	 * @return The value of the last sample or 0 if there is none.
	 */
	public int getLastValue(EStatisticType type) {
		int[] typeValues = values[type.ordinal()];
		return typeValues.length > 0 ? typeValues[typeValues.length - 1] : 0;
	}

	/**
	 * @return The biggest value of all samples or 0 if there is none.
	 */
	public int getMaxValue(EStatisticType type) {
		int max = 0;
		for (int value : values[type.ordinal()]) {
			max = Math.max(max, value);
		}
		return max;
	}

	/**
	 * @return The time of the last sample in milliseconds or 0 if there is none.
	 */
	public int getLastTime() {
		return times.length > 0 ? times[times.length - 1] : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StatisticsSeries)) {
			return false;
		}
		StatisticsSeries that = (StatisticsSeries) o;
		return Arrays.equals(times, that.times) && Arrays.deepEquals(values, that.values);
	}

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(times) + Arrays.deepHashCode(values);
	}

	@Override
	public String toString() {
		return "StatisticsSeries{samples=" + times.length + "}";
	}
}
