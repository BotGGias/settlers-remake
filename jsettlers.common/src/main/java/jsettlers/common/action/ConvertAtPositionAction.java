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
package jsettlers.common.action;

import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;

/**
 * Converts settlers of the area (partition) at the given position without the need to select them, e.g. bearers into pioneers or pioneers back
 * into bearers.
 */
public class ConvertAtPositionAction extends PointAction {

	private final EMovableType sourceType;
	private final EMovableType targetType;
	private final short amount;

	/**
	 * @param sourceType
	 *            The type of the settlers that shall be converted.
	 * @param targetType
	 *            The type they shall be converted to.
	 * @param amount
	 *            The maximum number of settlers to convert. Use {@link Short#MAX_VALUE} to convert all.
	 * @param position
	 *            A position in the area where the settlers shall be converted.
	 */
	public ConvertAtPositionAction(EMovableType sourceType, EMovableType targetType, short amount, ShortPoint2D position) {
		super(EActionType.CONVERT_AT_POSITION, position);
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.amount = amount;
	}

	public EMovableType getSourceType() {
		return sourceType;
	}

	public EMovableType getTargetType() {
		return targetType;
	}

	public short getAmount() {
		return amount;
	}
}
