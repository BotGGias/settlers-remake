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
package jsettlers.logic.player;

import jsettlers.common.menu.messages.SimpleMessage;
import jsettlers.network.synchronic.timer.INetworkTimerable;

/**
 * Informs the local player about the remaining peace time. This only shows messages and does not change the game state.
 */
public class PeaceTimeNotifier implements INetworkTimerable {
	private static final int[] WARNING_MINUTES = { 10, 5, 1 };
	private static final int MINUTE_MS = 60 * 1000;

	private final Player player;
	private boolean startMessageShown = false;
	private boolean finished = false;
	private int nextWarningIndex = 0;

	public PeaceTimeNotifier(Player player) {
		this.player = player;
	}

	@Override
	public void timerEvent() {
		if (finished) {
			return;
		}

		int remainingMs = player.getPeaceTimeRemainingMs();
		if (remainingMs <= 0) {
			finished = true;
			if (startMessageShown) {
				player.showMessage(SimpleMessage.info("peace_time_over"));
			}
			return;
		}

		if (!startMessageShown) {
			startMessageShown = true;
			player.showMessage(SimpleMessage.info("peace_time_active"));

			// skip warnings that are already due, e.g. for a short peace time or when loading a savegame
			while (nextWarningIndex < WARNING_MINUTES.length && remainingMs <= WARNING_MINUTES[nextWarningIndex] * MINUTE_MS) {
				nextWarningIndex++;
			}
		} else if (nextWarningIndex < WARNING_MINUTES.length && remainingMs <= WARNING_MINUTES[nextWarningIndex] * MINUTE_MS) {
			player.showMessage(SimpleMessage.info("peace_time_ends_" + WARNING_MINUTES[nextWarningIndex] + "min"));
			nextWarningIndex++;
		}
	}
}
