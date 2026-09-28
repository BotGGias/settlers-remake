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
package jsettlers.graphics.map.draw;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;

public class ImageLinkMapTest {

	@Test
	public void testStatisticSoldierIconExistsForAllCivilisations() {
		for (ECivilisation civilisation : ECivilisation.VALUES) {
			assertNotNull(civilisation.toString(), ImageLinkMap.get(civilisation, ECommonLinkType.STATISTIC_SOLDIERS, EMovableType.SWORDSMAN_L1));
		}
	}

	@Test
	public void testSiegeWeaponIconsExistForAllCivilisations() {
		for (EMovableType weapon : EMovableType.SIEGE_WEAPONS) {
			for (ECivilisation civilisation : ECivilisation.VALUES) {
				assertNotNull(weapon + " " + civilisation, ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, weapon));
			}
		}
	}
}
