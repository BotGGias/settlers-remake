/*
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
 */
package jsettlers.common.player;

import static org.junit.Assert.assertEquals;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;

import jsettlers.common.movable.EMovableType;

public class SettlerStatisticsTest {

	@Test
	public void testCounts() {
		Map<EMovableType, Integer> counts = new EnumMap<>(EMovableType.class);
		counts.put(EMovableType.BEARER, 10);
		counts.put(EMovableType.DIGGER, 2);
		counts.put(EMovableType.BRICKLAYER, 3);
		counts.put(EMovableType.LUMBERJACK, 4);
		counts.put(EMovableType.SMITH, 1);
		counts.put(EMovableType.SWORDSMAN_L1, 5);
		counts.put(EMovableType.SWORDSMAN_L3, 1);
		counts.put(EMovableType.BOWMAN_L2, 2);
		counts.put(EMovableType.PIKEMAN_L1, 3);
		counts.put(EMovableType.MAGE, 1);
		counts.put(EMovableType.GEOLOGIST, 2);
		counts.put(EMovableType.THIEF, 1);
		counts.put(EMovableType.PIONEER, 4);
		counts.put(EMovableType.DONKEY, 6);

		SettlerStatistics statistics = new SettlerStatistics(type -> counts.getOrDefault(type, 0), 42);

		assertEquals(42, statistics.getBeds());
		assertEquals(10, statistics.getBearers());
		assertEquals(2, statistics.getDiggers());
		assertEquals(3, statistics.getBricklayers());
		assertEquals(5, statistics.getWorkers());
		assertEquals(20, statistics.getCivilians());
		assertEquals(6, statistics.getSwordsmen());
		assertEquals(2, statistics.getBowmen());
		assertEquals(3, statistics.getPikemen());
		assertEquals(1, statistics.getMages());
		assertEquals(12, statistics.getSoldiers());
		assertEquals(32, statistics.getTotal());
		assertEquals(2, statistics.getGeologists());
		assertEquals(1, statistics.getThieves());
		assertEquals(4, statistics.getPioneers());
		assertEquals(6, statistics.getDonkeys());
	}
}
