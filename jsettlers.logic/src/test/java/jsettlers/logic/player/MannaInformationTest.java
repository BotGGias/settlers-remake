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

import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.movable.ESoldierType;
import jsettlers.common.movable.ESpellType;
import jsettlers.common.player.ECivilisation;

public class MannaInformationTest {

	@Test
	public void testProducedMannaIsCumulative() {
		MannaInformation manna = new MannaInformation(ECivilisation.ROMAN);
		for (int i = 0; i < 12; i++) {
			manna.increaseManna();
		}
		manna.increaseMannaByBigTemple();

		assertEquals(20, manna.getAmountOfManna());
		assertEquals(20, manna.getAmountOfProducedManna());

		manna.upgrade(ESoldierType.SWORDSMAN); // costs 10 manna
		assertTrue(manna.useSpell(ESpellType.ROMAN_EYE)); // costs 10 manna

		assertEquals(0, manna.getAmountOfManna());
		assertEquals(20, manna.getAmountOfProducedManna());

		manna.increaseManna();
		assertEquals(1, manna.getAmountOfManna());
		assertEquals(21, manna.getAmountOfProducedManna());
	}

	@Test
	public void testBigTempleBonusCanBeStopped() {
		MannaInformation manna = new MannaInformation(ECivilisation.ROMAN);
		manna.stopFutureManaIncreasingByBigTemple();
		manna.increaseMannaByBigTemple();

		assertEquals(0, manna.getAmountOfManna());
		assertEquals(0, manna.getAmountOfProducedManna());
	}

	@Test
	public void testEndgameStatisticReportsProducedManna() {
		Player player = new Player((byte) 0, new Team((byte) 0), (byte) 1, EPlayerType.HUMAN, ECivilisation.ROMAN);
		MannaInformation manna = player.getMannaInformation();
		for (int i = 0; i < 15; i++) {
			manna.increaseManna();
		}
		manna.upgrade(ESoldierType.BOWMAN);

		assertEquals(5, manna.getAmountOfManna());
		assertEquals(15, player.getEndgameStatistic().getAmountOfProducedMana());
	}
}
