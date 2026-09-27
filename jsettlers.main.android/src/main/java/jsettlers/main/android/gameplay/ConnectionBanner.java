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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.android.gameplay;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import jsettlers.common.menu.ConnectionNotice;
import jsettlers.common.menu.IStartedGame;
import jsettlers.main.android.R;

/**
 * A prominent notice on top of the map about the network condition of a multiplayer game ({@link IStartedGame#getConnectionNotice()}):
 * interrupted (orange, "reconnecting", naming the players the game waits for), restored (green, for a few seconds) and lost (red).
 * The map messages alone are easy to miss on a phone; with the Settlers United launcher an interruption is bridged for minutes, so
 * the players need to see that the game waits and will continue.
 */
class ConnectionBanner {
	private static final long POLL_MS = 500;
	private static final long RESTORED_MS = 3000;
	private static final int COLOR_INTERRUPTED = 0xE6B45309;
	private static final int COLOR_RESTORED = 0xE62E7D32;
	private static final int COLOR_LOST = 0xE6C62828;

	private final TextView view;
	private final IStartedGame game;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable poll = this::update;

	private boolean running;
	private boolean wasInterrupted;
	private long restoredUntil;

	ConnectionBanner(TextView view, IStartedGame game) {
		this.view = view;
		this.game = game;
	}

	void start() {
		if (!running && game != null && game.isMultiplayerGame()) {
			running = true;
			handler.post(poll);
		}
	}

	void stop() {
		running = false;
		handler.removeCallbacks(poll);
	}

	private void update() {
		if (!running) {
			return;
		}
		ConnectionNotice notice = game.getConnectionNotice();
		long now = System.currentTimeMillis();
		switch (notice.getType()) {
		case LOST:
			wasInterrupted = false;
			show(view.getContext().getString(R.string.connection_lost), COLOR_LOST);
			break;
		case INTERRUPTED:
			wasInterrupted = true;
			if (notice.getWaitingFor().isEmpty()) {
				show(view.getContext().getString(R.string.connection_interrupted), COLOR_INTERRUPTED);
			} else {
				show(view.getContext().getString(R.string.connection_waiting_for, TextUtils.join(", ", notice.getWaitingFor())), COLOR_INTERRUPTED);
			}
			break;
		default:
			if (wasInterrupted) {
				wasInterrupted = false;
				restoredUntil = now + RESTORED_MS;
			}
			if (now < restoredUntil) {
				show(view.getContext().getString(R.string.connection_restored), COLOR_RESTORED);
			} else {
				view.setVisibility(View.GONE);
			}
		}
		handler.postDelayed(poll, POLL_MS);
	}

	private void show(String text, int color) {
		if (!text.contentEquals(view.getText())) {
			view.setText(text);
		}
		view.setBackgroundColor(color);
		view.setVisibility(View.VISIBLE);
	}
}
