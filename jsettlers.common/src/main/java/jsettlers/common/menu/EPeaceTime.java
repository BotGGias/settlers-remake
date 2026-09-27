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
package jsettlers.common.menu;

/**
 * The possible durations of the peace time at the beginning of a game. During the peace time, no player can attack another player.
 */
public enum EPeaceTime {
	WITHOUT(0),
	MIN_10(10),
	MIN_20(20),
	MIN_30(30),
	MIN_45(45),
	MIN_60(60),
	MIN_90(90),
	MIN_120(120);

	public static final EPeaceTime[] VALUES = values();

	public final int minutes;

	EPeaceTime(int minutes) {
		this.minutes = minutes;
	}

	/**
	 * @return The duration of the peace time in milliseconds of game time.
	 */
	public int getDurationMs() {
		return minutes * 60 * 1000;
	}

	/**
	 * @param minutes
	 *            The duration in minutes.
	 * @return The {@link EPeaceTime} with the given duration or {@link #WITHOUT} if there is none.
	 */
	public static EPeaceTime fromMinutes(int minutes) {
		for (EPeaceTime peaceTime : VALUES) {
			if (peaceTime.minutes == minutes) {
				return peaceTime;
			}
		}
		return WITHOUT;
	}
}
