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
package jsettlers.logic.map.loading.original.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jsettlers.common.mapobject.EDecorationType;
import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.logic.map.loading.data.objects.DecorationMapDataObject;
import jsettlers.logic.map.loading.data.objects.MapDataObject;
import jsettlers.logic.map.loading.data.objects.MapTreeObject;
import jsettlers.logic.map.loading.data.objects.StoneMapDataObject;

public class EOriginalMapObjectTypeTest {

	@Test
	public void testAllKnownDecorationsAreLoaded() {
		for (int id = 1; id <= 67; id++) {
			assertDecoration(id);
		}
		for (int id = 111; id <= 114; id++) {
			assertDecoration(id);
		}
	}

	@Test
	public void testDecorationGraphics() {
		assertDecoration(1, EDecorationType.BIG_STONE_1, 29, 0, true);
		assertDecoration(12, EDecorationType.STONE_4, 29, 11, true);
		assertDecoration(20, EDecorationType.BOUNDARY_STONE_8, 29, 19, true);
		assertDecoration(21, EDecorationType.SMALL_STONE_1, 30, 0, false);
		assertDecoration(29, EDecorationType.WRECK_1, 28, 0, true);
		assertDecoration(34, EDecorationType.GRAVE, 28, 5, false);
		assertDecoration(35, EDecorationType.PLANT_SMALL_1, 27, 0, false);
		assertDecoration(49, EDecorationType.BONES, 27, 14, false);
		assertDecoration(67, EDecorationType.REED_BEDS_6, 27, 32, false);
		assertDecoration(111, EDecorationType.REEF_SMALL, 29, 20, false);
		assertDecoration(114, EDecorationType.REEF_XLARGE, 29, 23, false);
	}

	@Test
	public void testTreesAndStonesAreUnchanged() {
		for (int id = 68; id <= 80; id++) {
			assertTrue(EOriginalMapObjectType.getTypeByInt(id).getNewInstance() instanceof MapTreeObject);
		}
		assertTrue(EOriginalMapObjectType.getTypeByInt(84).getNewInstance() instanceof MapTreeObject);

		for (int id = 115; id <= 127; id++) {
			MapDataObject object = EOriginalMapObjectType.getTypeByInt(id).getNewInstance();
			assertTrue(object instanceof StoneMapDataObject);
			assertEquals(127 - id, ((StoneMapDataObject) object).getCapacity());
		}
	}

	@Test
	public void testUnknownObjectsAreIgnored() {
		assertNull(EOriginalMapObjectType.getTypeByInt(0).getNewInstance());
		for (int id = 81; id <= 83; id++) {
			assertNull(EOriginalMapObjectType.getTypeByInt(id).getNewInstance());
		}
		for (int id = 85; id <= 110; id++) {
			assertNull(EOriginalMapObjectType.getTypeByInt(id).getNewInstance());
		}
		assertNull(EOriginalMapObjectType.getTypeByInt(200).getNewInstance());
	}

	private static DecorationMapDataObject assertDecoration(int id) {
		MapDataObject object = EOriginalMapObjectType.getTypeByInt(id).getNewInstance();
		assertTrue("object " + id + " should be a decoration", object instanceof DecorationMapDataObject);
		DecorationMapDataObject decoration = (DecorationMapDataObject) object;
		assertEquals(EMapObjectType.LANDSCAPE_DECORATION, decoration.getType());
		assertEquals(EOriginalMapObjectType.getTypeByInt(id).name(), decoration.getDecorationType().name());
		return decoration;
	}

	private static void assertDecoration(int id, EDecorationType expected, int sequence, int imageIndex, boolean blocking) {
		EDecorationType decorationType = assertDecoration(id).getDecorationType();
		assertEquals(expected, decorationType);
		assertEquals(sequence, decorationType.sequence);
		assertEquals(imageIndex, decorationType.imageIndex);
		if (blocking) {
			assertTrue(decorationType.blocking);
		} else {
			assertFalse(decorationType.blocking);
		}
	}
}
