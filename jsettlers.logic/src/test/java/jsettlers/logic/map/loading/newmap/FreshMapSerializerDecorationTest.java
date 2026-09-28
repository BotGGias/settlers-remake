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
package jsettlers.logic.map.loading.newmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import jsettlers.common.mapobject.EDecorationType;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.data.IMapData;
import jsettlers.logic.map.loading.data.objects.DecorationMapDataObject;
import jsettlers.logic.map.loading.data.objects.MapDataObject;
import jsettlers.testutils.map.MapUtils;

public class FreshMapSerializerDecorationTest {

	@Test
	public void testOriginalMapDecorationsSurviveSerialization() throws MapLoadException, IOException {
		IMapData originalMap = MapUtils.getSpezialSumpf().getMapData();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FreshMapSerializer.serialize(originalMap, out);
		FreshMapData freshMap = new FreshMapData();
		FreshMapSerializer.deserialize(freshMap, new ByteArrayInputStream(out.toByteArray()));

		int decorations = 0;
		for (int y = 0; y < originalMap.getHeight(); y++) {
			for (int x = 0; x < originalMap.getWidth(); x++) {
				MapDataObject original = originalMap.getMapObject(x, y);
				if (original instanceof DecorationMapDataObject) {
					decorations++;
					MapDataObject loaded = freshMap.getMapObject(x, y);
					assertTrue(loaded instanceof DecorationMapDataObject);
					assertEquals(((DecorationMapDataObject) original).getType(), ((DecorationMapDataObject) loaded).getType());
					EDecorationType decorationType = ((DecorationMapDataObject) original).getDecorationType();
					assertEquals(decorationType, ((DecorationMapDataObject) loaded).getDecorationType());
				}
			}
		}

		assertTrue("the original map should contain decorations", decorations > 0);
	}
}
