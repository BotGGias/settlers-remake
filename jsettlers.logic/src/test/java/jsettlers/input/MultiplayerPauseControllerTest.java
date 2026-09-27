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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.input.tasks.EGuiAction;
import jsettlers.input.tasks.PauseGameGuiTask;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.IGameResumeListener;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.client.interfaces.ITaskScheduler;
import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.synchronic.timer.NetworkTimer;

public class MultiplayerPauseControllerTest {
	private static final int COUNTDOWN_MS = 50;
	private static final byte LOCAL_PLAYER = 0;
	private static final byte OTHER_PLAYER = 1;

	private final NetworkTimer clock = new NetworkTimer(true);
	private final List<TaskPacket> scheduledTasks = new ArrayList<>();
	private final List<String> messages = new ArrayList<>();
	private final LoopbackConnector connector = new LoopbackConnector();
	private MultiplayerPauseController controller;

	@Before
	public void setUp() {
		controller = createController(true);
	}

	@After
	public void tearDown() {
		controller.shutdown();
	}

	private MultiplayerPauseController createController(boolean multiplayer) {
		MultiplayerPauseController controller = new MultiplayerPauseController(clock, scheduledTasks::add, connector, multiplayer, LOCAL_PLAYER, 2,
				message -> messages.add(message.getMessageLabel()), COUNTDOWN_MS);
		connector.setGameResumeListener(controller::resumeRequested);
		return controller;
	}

	@Test
	public void testRequestPauseSchedulesTask() {
		controller.requestPause();

		assertEquals(1, scheduledTasks.size());
		PauseGameGuiTask task = (PauseGameGuiTask) scheduledTasks.get(0);
		assertEquals(EGuiAction.PAUSE_GAME, task.getGuiAction());
		assertEquals(LOCAL_PLAYER, task.getPlayerId());
		assertEquals(0, task.getPauseCount());
		assertFalse("the game must only pause when the task is executed", clock.isPausing());
	}

	@Test
	public void testExecutePausePausesTheGame() {
		controller.executePause(new PauseGameGuiTask(OTHER_PLAYER, 0));

		assertTrue(clock.isPausing());
		assertEquals(1, controller.getPausesUsed(OTHER_PLAYER));
		assertEquals(MultiplayerPauseController.MAX_PAUSES_PER_PLAYER, controller.getRemainingPauses());
		assertEquals(List.of("game_paused_by_player"), messages);
	}

	@Test
	public void testPauseWhilePausedIsIgnored() {
		controller.executePause(new PauseGameGuiTask(OTHER_PLAYER, 0));
		controller.executePause(new PauseGameGuiTask(LOCAL_PLAYER, 0));

		assertEquals(0, controller.getPausesUsed(LOCAL_PLAYER));
		assertEquals(1, controller.getPausesUsed(OTHER_PLAYER));
	}

	@Test
	public void testOutdatedPauseRequestIsIgnored() throws InterruptedException {
		pauseAndResume(OTHER_PLAYER, 0);

		// requested before the first pause was executed
		controller.executePause(new PauseGameGuiTask(LOCAL_PLAYER, 0));

		assertFalse(clock.isPausing());
		assertEquals(0, controller.getPausesUsed(LOCAL_PLAYER));
	}

	@Test
	public void testPauseLimit() throws InterruptedException {
		for (int i = 0; i < MultiplayerPauseController.MAX_PAUSES_PER_PLAYER; i++) {
			pauseAndResume(LOCAL_PLAYER, i);
		}
		assertEquals(0, controller.getRemainingPauses());

		controller.executePause(new PauseGameGuiTask(LOCAL_PLAYER, MultiplayerPauseController.MAX_PAUSES_PER_PLAYER));
		assertFalse(clock.isPausing());

		scheduledTasks.clear();
		controller.requestPause();
		assertTrue(scheduledTasks.isEmpty());
		assertEquals("pause_limit_reached", messages.get(messages.size() - 1));

		// other players can still pause
		controller.executePause(new PauseGameGuiTask(OTHER_PLAYER, MultiplayerPauseController.MAX_PAUSES_PER_PLAYER));
		assertTrue(clock.isPausing());
	}

	@Test
	public void testResumeStartsCountdown() throws InterruptedException {
		controller.executePause(new PauseGameGuiTask(OTHER_PLAYER, 0));

		controller.requestResume();
		assertTrue(clock.isPausing());
		assertTrue(controller.getResumeCountdownMs() > 0);

		waitForCountdown();
		assertFalse(clock.isPausing());
		assertEquals(0, controller.getResumeCountdownMs());
		assertEquals(List.of("game_paused_by_player", "game_resumed_by_player"), messages);
	}

	@Test
	public void testResumeReceivedBeforePauseWasExecuted() throws InterruptedException {
		controller.resumeRequested(OTHER_PLAYER, 1);
		assertEquals(0, controller.getResumeCountdownMs());

		controller.executePause(new PauseGameGuiTask(OTHER_PLAYER, 0));
		assertTrue(controller.getResumeCountdownMs() > 0);

		waitForCountdown();
		assertFalse(clock.isPausing());
	}

	@Test
	public void testResumeWhenNotPausedIsIgnored() {
		controller.requestResume();
		assertEquals(0, connector.resumeRequests);
	}

	@Test
	public void testSinglePlayerIsNotAffected() {
		controller.shutdown();
		controller = createController(false);

		controller.requestPause();
		assertTrue(scheduledTasks.isEmpty());

		// e.g. replay of a multiplayer game
		controller.executePause(new PauseGameGuiTask(OTHER_PLAYER, 0));
		assertFalse(clock.isPausing());
		assertEquals(-1, controller.getRemainingPauses());
	}

	@Test
	public void testTaskSerialization() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		new PauseGameGuiTask(OTHER_PLAYER, 3).serialize(new DataOutputStream(bytes));

		PauseGameGuiTask read = (PauseGameGuiTask) TaskPacket.DEFAULT_DESERIALIZER.deserialize(null,
				new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

		assertEquals(EGuiAction.PAUSE_GAME, read.getGuiAction());
		assertEquals(OTHER_PLAYER, read.getPlayerId());
		assertEquals(3, read.getPauseCount());
	}

	private void pauseAndResume(byte playerId, int pauseCount) throws InterruptedException {
		controller.executePause(new PauseGameGuiTask(playerId, pauseCount));
		assertTrue(clock.isPausing());
		controller.requestResume();
		waitForCountdown();
		assertFalse(clock.isPausing());
	}

	private void waitForCountdown() throws InterruptedException {
		long end = System.currentTimeMillis() + 5000;
		while (clock.isPausing() && System.currentTimeMillis() < end) {
			Thread.sleep(5);
		}
	}

	/**
	 * Sends the resume requests directly back like the server that forwards them to all players.
	 */
	private static class LoopbackConnector implements INetworkConnector {
		private IGameResumeListener listener;
		private int resumeRequests = 0;

		@Override
		public void requestGameResume(byte playerId, int pauseCount) {
			resumeRequests++;
			listener.resumeRequested(playerId, pauseCount);
		}

		@Override
		public void setGameResumeListener(IGameResumeListener listener) {
			this.listener = listener;
		}

		@Override
		public ITaskScheduler getTaskScheduler() {
			return null;
		}

		@Override
		public IGameClock getGameClock() {
			return null;
		}

		@Override
		public void shutdown() {
		}

		@Override
		public void setStartFinished(boolean startFinished) {
		}

		@Override
		public boolean haveAllPlayersStartFinished() {
			return true;
		}
	}
}
