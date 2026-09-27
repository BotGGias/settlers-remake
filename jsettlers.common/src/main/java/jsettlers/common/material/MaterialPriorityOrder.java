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

/**
 * Helper to change the transport priority order of materials.
 */
public final class MaterialPriorityOrder {

	private MaterialPriorityOrder() {
	}

	/**
	 * Moves a material to a new position in the priority order.
	 *
	 * @param order
	 *            The current order, index 0 has the highest priority. It is not modified.
	 * @param type
	 *            The material to move.
	 * @param desiredNewPosition
	 *            The new position of that material. It is clamped to the valid range.
	 * @return The new order. If the material is not contained in the order, a copy of the old order.
	 */
	public static EMaterialType[] reorder(EMaterialType[] order, EMaterialType type, int desiredNewPosition) {
		int oldPos = indexOf(order, type);
		EMaterialType[] newOrder = order.clone();
		if (oldPos < 0) {
			return newOrder;
		}
		int newPos = Math.max(Math.min(desiredNewPosition, order.length - 1), 0);

		if (newPos > oldPos) {
			System.arraycopy(newOrder, oldPos + 1, newOrder, oldPos, newPos - oldPos);
		} else {
			System.arraycopy(newOrder, newPos, newOrder, newPos + 1, oldPos - newPos);
		}
		newOrder[newPos] = type;
		return newOrder;
	}

	/**
	 * @return The position of the material in the order or -1 if it is not contained.
	 */
	public static int indexOf(EMaterialType[] order, EMaterialType type) {
		for (int i = 0; i < order.length; i++) {
			if (order[i] == type) {
				return i;
			}
		}
		return -1;
	}
}
