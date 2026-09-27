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
package jsettlers.main.android.gameplay.controlsmenu.goods;

import jsettlers.common.material.EMaterialType;

/**
 * One material in the transport priority list.
 */
public class PriorityMaterialState {
	private final EMaterialType materialType;
	private final int rank;
	private final boolean stocked;
	private final boolean selected;

	public PriorityMaterialState(EMaterialType materialType, int rank, boolean stocked, boolean selected) {
		this.materialType = materialType;
		this.rank = rank;
		this.stocked = stocked;
		this.selected = selected;
	}

	public EMaterialType getMaterialType() {
		return materialType;
	}

	/**
	 * @return The position in the priority list, starting with 1 for the highest priority.
	 */
	public int getRank() {
		return rank;
	}

	public boolean isStocked() {
		return stocked;
	}

	public boolean isSelected() {
		return selected;
	}
}
