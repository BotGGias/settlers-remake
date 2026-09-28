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

/**
 * The values that are recorded for every player over the course of a game.
 */
public enum EStatisticType {
	/**
	 * All settlers of the player, including the soldiers.
	 */
	SETTLERS,
	/**
	 * The soldiers the player currently has.
	 */
	SOLDIERS,
	/**
	 * The soldiers the player has recruited since the start of the game.
	 */
	RECRUITED_SOLDIERS,
	/**
	 * The finished buildings of the player.
	 */
	BUILDINGS,
	/**
	 * The number of map positions the player owns.
	 */
	LAND,
	/**
	 * The goods the player has produced since the start of the game.
	 */
	PRODUCED_GOODS,
	/**
	 * The gold the player has produced since the start of the game.
	 */
	GOLD,
	/**
	 * The manna the player has produced since the start of the game.
	 */
	MANNA,
	/**
	 * The soldiers of the player weighted by their level.
	 */
	MILITARY_STRENGTH;

	public static final EStatisticType[] VALUES = values();
	public static final int NUMBER_OF_TYPES = VALUES.length;

	/**
	 * @return The key of the label with the name of this value.
	 */
	public String getLabelKey() {
		return "statistic-type-" + name();
	}
}
