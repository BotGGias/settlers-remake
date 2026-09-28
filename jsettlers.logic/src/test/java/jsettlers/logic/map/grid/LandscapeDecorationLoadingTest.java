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
package jsettlers.logic.map.grid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.flags.FlagsGrid;
import jsettlers.logic.map.grid.objects.LandscapeDecorationMapObject;
import jsettlers.logic.map.grid.objects.ObjectsGrid;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.network.synchronic.timer.NetworkTimer;
import jsettlers.testutils.map.MapUtils;

public class LandscapeDecorationLoadingTest {

	@After
	public void tearDown() {
		MatchConstants.clearState();
	}

	@Test
	public void testDecorationsOfOriginalMapAreLoaded() throws MapLoadException {
		MatchConstants.init(new NetworkTimer(true), 0);
		MainGrid grid = MapUtils.getSpezialSumpf().loadMainGrid(null).getMainGrid();
		ObjectsGrid objectsGrid = grid.getObjectsGrid();
		FlagsGrid flagsGrid = grid.getFlagsGrid();

		int decorations = 0;
		int blocking = 0;
		for (int y = 0; y < grid.getHeight(); y++) {
			for (int x = 0; x < grid.getWidth(); x++) {
				LandscapeDecorationMapObject decoration = (LandscapeDecorationMapObject) objectsGrid.getMapObjectAt(x, y, EMapObjectType.LANDSCAPE_DECORATION);
				if (decoration != null) {
					decorations++;
					assertFalse("decorations must not be placed below buildings", objectsGrid.isBuildingAt(x, y));
					if (decoration.getDecorationType().blocking) {
						blocking++;
						assertTrue(flagsGrid.isBlocked(x, y));
						assertTrue(flagsGrid.isProtected(x, y));
					}
				}
			}
		}

		assertTrue("the original map should contain decorations", decorations > 0);
	}
}
