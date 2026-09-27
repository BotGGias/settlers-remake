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
package jsettlers.common.menu;

import jsettlers.common.Color;
import jsettlers.common.player.EWinState;

/**
 * The colors of the status dots shown for players and in status messages.
 */
public final class PlayerStatusColors {
	public static final Color OK = new Color(0.3f, 0.85f, 0.3f, 1);
	public static final Color WARNING = new Color(1f, 0.8f, 0.1f, 1);
	public static final Color ERROR = new Color(0.95f, 0.25f, 0.25f, 1);
	public static final Color INACTIVE = new Color(0.65f, 0.65f, 0.65f, 1);

	private PlayerStatusColors() {
	}

	/**
	 * @return The color that represents the state of the given player.
	 */
	public static Color of(InGamePlayerStatus status) {
		if (status.getWinState() == EWinState.LOST) {
			return INACTIVE;
		}
		EPlayerConnectionState connectionState = status.getConnectionState();
		if (connectionState == null) {
			return OK;
		}
		switch (connectionState) {
		case CONNECTED:
			return OK;
		case WAITING:
			return WARNING;
		case DISCONNECTED:
			return ERROR;
		default:
			return INACTIVE;
		}
	}
}
