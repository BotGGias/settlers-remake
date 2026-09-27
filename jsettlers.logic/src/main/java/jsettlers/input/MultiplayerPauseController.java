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
package jsettlers.input;

import java.util.Timer;
import java.util.TimerTask;

import jsettlers.common.menu.messages.IMessage;
import jsettlers.common.menu.messages.IMessenger;
import jsettlers.common.menu.messages.SimpleMessage;
import jsettlers.input.tasks.PauseGameGuiTask;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.client.interfaces.ITaskScheduler;

/**
 * Pauses and resumes a multiplayer game for all players.
 * <p>
 * Pausing is done with a synchronized task, so all players pause at the same game time. Every player can pause the game at most
 * {@link #MAX_PAUSES_PER_PLAYER} times.
 * <p>
 * Resuming can not be done with a synchronized task, because a paused clock does not execute tasks. Therefore the resume requests are sent
 * directly to all players. When a player receives such a request, a countdown of {@link #RESUME_COUNTDOWN_MS} is started and the game continues
 * afterwards. As all players are paused at the same game state, it does not matter that they continue at slightly different times.
 */
public class MultiplayerPauseController {
	public static final int MAX_PAUSES_PER_PLAYER = 5;
	public static final int RESUME_COUNTDOWN_MS = 5000;

	private final IGameClock clock;
	private final ITaskScheduler taskScheduler;
	private final INetworkConnector networkConnector;
	private final boolean multiplayer;
	private final byte localPlayerId;
	private final IMessenger localMessenger;
	private final int countdownDurationMs;

	/**
	 * Number of pauses executed in this game. This is the same for all players, because it is only changed by synchronized tasks.
	 */
	private int pauseCount = 0;
	private final int[] pausesUsed;

	/**
	 * The highest pause number for which a resume request has been received.
	 */
	private int resumeRequestedForPause = 0;
	private long countdownEndMs = 0;
	private Timer countdownTimer;

	public MultiplayerPauseController(IGameClock clock, ITaskScheduler taskScheduler, INetworkConnector networkConnector, boolean multiplayer,
			byte localPlayerId, int numberOfPlayers, IMessenger localMessenger) {
		this(clock, taskScheduler, networkConnector, multiplayer, localPlayerId, numberOfPlayers, localMessenger, RESUME_COUNTDOWN_MS);
	}

	MultiplayerPauseController(IGameClock clock, ITaskScheduler taskScheduler, INetworkConnector networkConnector, boolean multiplayer,
			byte localPlayerId, int numberOfPlayers, IMessenger localMessenger, int countdownDurationMs) {
		this.clock = clock;
		this.taskScheduler = taskScheduler;
		this.networkConnector = networkConnector;
		this.multiplayer = multiplayer;
		this.localPlayerId = localPlayerId;
		this.localMessenger = localMessenger;
		this.countdownDurationMs = countdownDurationMs;
		this.pausesUsed = new int[Math.max(numberOfPlayers, localPlayerId + 1)];
	}

	public boolean isMultiplayer() {
		return multiplayer;
	}

	/**
	 * Requests to pause the game for all players. Called when the local player wants to pause the game.
	 */
	public synchronized void requestPause() {
		if (!multiplayer || clock.isPausing()) {
			return;
		}

		if (getPausesUsed(localPlayerId) >= MAX_PAUSES_PER_PLAYER) {
			showMessage(SimpleMessage.info("pause_limit_reached"));
			return;
		}

		taskScheduler.scheduleTask(new PauseGameGuiTask(localPlayerId, pauseCount));
	}

	/**
	 * Requests to resume the game for all players. Called when the local player wants to continue the game.
	 */
	public synchronized void requestResume() {
		if (!multiplayer || !clock.isPausing() || isCountdownRunning()) {
			return;
		}

		networkConnector.requestGameResume(localPlayerId, pauseCount);
	}

	/**
	 * Executes a pause task. This is called at the same game time by all players.
	 */
	public synchronized void executePause(PauseGameGuiTask task) {
		byte playerId = task.getPlayerId();
		if (!multiplayer // e.g. replay of a multiplayer game
				|| clock.isPausing()
				|| task.getPauseCount() != pauseCount // the request was made during an earlier pause
				|| playerId < 0 || playerId >= pausesUsed.length
				|| pausesUsed[playerId] >= MAX_PAUSES_PER_PLAYER) {
			return;
		}

		pauseCount++;
		pausesUsed[playerId]++;
		clock.setPausing(true);

		showMessage(SimpleMessage.playerInfo("game_paused_by_player", playerId));
		tryStartCountdown();
	}

	/**
	 * Called when any player (including the local one) requested to resume the game.
	 */
	public synchronized void resumeRequested(byte playerId, int forPause) {
		if (!multiplayer || forPause <= resumeRequestedForPause) {
			return;
		}

		resumeRequestedForPause = forPause;
		showMessage(SimpleMessage.playerInfo("game_resumed_by_player", playerId));
		tryStartCountdown();
	}

	private void tryStartCountdown() {
		// the resume request can arrive before this player executed the pause task
		if (!clock.isPausing() || resumeRequestedForPause != pauseCount || isCountdownRunning()) {
			return;
		}

		countdownEndMs = System.currentTimeMillis() + countdownDurationMs;
		if (countdownTimer == null) {
			countdownTimer = new Timer("resumeCountdownTimer", true);
		}
		countdownTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				finishCountdown();
			}
		}, countdownDurationMs);
	}

	private synchronized void finishCountdown() {
		countdownEndMs = 0;
		clock.setPausing(false);
	}

	private boolean isCountdownRunning() {
		return countdownEndMs != 0;
	}

	/**
	 * @return The remaining time in milliseconds until the game continues or 0 if no countdown is running.
	 */
	public synchronized int getResumeCountdownMs() {
		if (!isCountdownRunning()) {
			return 0;
		}
		return (int) Math.max(1, countdownEndMs - System.currentTimeMillis());
	}

	/**
	 * @return The number of pauses the local player can still use or -1 if the number is not limited (single player).
	 */
	public synchronized int getRemainingPauses() {
		if (!multiplayer) {
			return -1;
		}
		return MAX_PAUSES_PER_PLAYER - getPausesUsed(localPlayerId);
	}

	synchronized int getPausesUsed(byte playerId) {
		return playerId >= 0 && playerId < pausesUsed.length ? pausesUsed[playerId] : 0;
	}

	private void showMessage(IMessage message) {
		if (localMessenger != null) {
			localMessenger.showMessage(message);
		}
	}

	public synchronized void shutdown() {
		if (countdownTimer != null) {
			countdownTimer.cancel();
			countdownTimer = null;
		}
	}
}
