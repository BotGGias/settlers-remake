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
package jsettlers.common.material;

import static jsettlers.common.material.EMaterialType.AXE;
import static jsettlers.common.material.EMaterialType.BREAD;
import static jsettlers.common.material.EMaterialType.COAL;
import static jsettlers.common.material.EMaterialType.FISH;
import static jsettlers.common.material.EMaterialType.PLANK;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MaterialPriorityOrderTest {

	private static final EMaterialType[] ORDER = { PLANK, COAL, FISH, BREAD, AXE };

	@Test
	public void testMoveUp() {
		assertArrayEquals(new EMaterialType[] { PLANK, BREAD, COAL, FISH, AXE }, MaterialPriorityOrder.reorder(ORDER, BREAD, 1));
	}

	@Test
	public void testMoveDown() {
		assertArrayEquals(new EMaterialType[] { PLANK, FISH, BREAD, COAL, AXE }, MaterialPriorityOrder.reorder(ORDER, COAL, 3));
	}

	@Test
	public void testMoveToTopAndBottomIsClamped() {
		assertArrayEquals(new EMaterialType[] { FISH, PLANK, COAL, BREAD, AXE }, MaterialPriorityOrder.reorder(ORDER, FISH, -100));
		assertArrayEquals(new EMaterialType[] { PLANK, COAL, BREAD, AXE, FISH }, MaterialPriorityOrder.reorder(ORDER, FISH, 100));
	}

	@Test
	public void testMoveToSamePosition() {
		assertArrayEquals(ORDER, MaterialPriorityOrder.reorder(ORDER, FISH, 2));
	}

	@Test
	public void testUnknownMaterialKeepsOrder() {
		assertArrayEquals(ORDER, MaterialPriorityOrder.reorder(ORDER, EMaterialType.GOLD, 0));
	}

	@Test
	public void testInputIsNotModified() {
		EMaterialType[] order = ORDER.clone();
		MaterialPriorityOrder.reorder(order, AXE, 0);
		assertArrayEquals(ORDER, order);
	}

	@Test
	public void testIndexOf() {
		assertEquals(3, MaterialPriorityOrder.indexOf(ORDER, BREAD));
		assertEquals(-1, MaterialPriorityOrder.indexOf(ORDER, EMaterialType.GOLD));
	}
}
