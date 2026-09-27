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
package jsettlers.input.tasks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;

/**
 * Converts settlers of the area (partition) at a position without the need to select them.
 */
public class ConvertAtPositionGuiTask extends SimpleGuiTask {

	private ShortPoint2D position;
	private EMovableType sourceType;
	private EMovableType targetType;
	private int amount;

	public ConvertAtPositionGuiTask() {
	}

	public ConvertAtPositionGuiTask(byte playerId, ShortPoint2D position, EMovableType sourceType, EMovableType targetType, int amount) {
		super(EGuiAction.CONVERT_AT_POSITION, playerId);
		this.position = position;
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.amount = amount;
	}

	public ShortPoint2D getPosition() {
		return position;
	}

	public EMovableType getSourceType() {
		return sourceType;
	}

	public EMovableType getTargetType() {
		return targetType;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	protected void serializeTask(DataOutputStream dos) throws IOException {
		super.serializeTask(dos);
		SimpleGuiTask.serializePosition(dos, position);
		dos.writeInt(sourceType.ordinal());
		dos.writeInt(targetType.ordinal());
		dos.writeInt(amount);
	}

	@Override
	protected void deserializeTask(DataInputStream dis) throws IOException {
		super.deserializeTask(dis);
		position = SimpleGuiTask.deserializePosition(dis);
		sourceType = EMovableType.VALUES[dis.readInt()];
		targetType = EMovableType.VALUES[dis.readInt()];
		amount = dis.readInt();
	}

	@Override
	public String toString() {
		return "ConvertAtPositionGuiTask{position=" + position + ", sourceType=" + sourceType + ", targetType=" + targetType + ", amount=" + amount + '}';
	}
}
