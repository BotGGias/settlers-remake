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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.ConnectionNotice;
import jsettlers.common.menu.EPlayerConnectionState;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.menu.PlayerStatusColors;
import jsettlers.common.menu.messages.EMessageType;
import jsettlers.common.menu.messages.IMessage;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.player.EWinState;
import jsettlers.common.player.IPlayer;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.common.packets.PlayerStatusPacket;
import jsettlers.network.common.packets.PlayerStatusesPacket;

public class PlayerStatusControllerTest {
	private static final byte LOCAL = 0;
	private static final byte OTHER = 1;
	private static final byte AI = 2;
	private static final byte THIRD = 3;

	private final List<IMessage> messages = new ArrayList<>();
	private final TestPlayer[] players = { new TestPlayer(LOCAL, 0), new TestPlayer(OTHER, 1), new TestPlayer(AI, 1), new TestPlayer(THIRD, 0), null };

	private long now = 100000;
	private boolean pausing = false;
	private long millisSinceLastProgress = 0;
	private boolean connected = true;

	private PlayerStatusController controller;

	@Before
	public void setUp() {
		controller = createController(true, new EPlayerType[] { EPlayerType.HUMAN, EPlayerType.HUMAN, EPlayerType.HUMAN, EPlayerType.HUMAN, null });
	}

	private PlayerStatusController createController(boolean multiplayer, EPlayerType[] types) {
		IGameClock clock = (IGameClock) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { IGameClock.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "isPausing":
						return pausing;
					case "getMillisSinceLastProgress":
						return millisSinceLastProgress;
					default:
						throw new UnsupportedOperationException(method.getName());
					}
				});
		INetworkConnector connector = (INetworkConnector) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { INetworkConnector.class }, (proxy, method, args) -> {
					if (method.getName().equals("isConnected")) {
						return connected;
					}
					throw new UnsupportedOperationException(method.getName());
				});
		return new PlayerStatusController(clock, connector, multiplayer, LOCAL, types, players, messages::add, () -> now);
	}

	private void receive(PlayerStatusPacket... statuses) {
		controller.playerStatusReceived(new PlayerStatusesPacket(statuses));
	}

	private void receiveAllFine() {
		receive(new PlayerStatusPacket(LOCAL, "host", true, 1, 10000, 50), new PlayerStatusPacket(OTHER, "other", true, 42, 10000, 50),
				new PlayerStatusPacket(THIRD, "third", true, 80, 10000, 50));
	}

	private void checkAfter(long millis) {
		now += millis;
		controller.check();
	}

	private InGamePlayerStatus status(byte playerId) {
		for (InGamePlayerStatus status : controller.getPlayerStatuses()) {
			if (status.getPlayerId() == playerId) {
				return status;
			}
		}
		return null;
	}

	private List<String> labels() {
		List<String> labels = new ArrayList<>();
		for (IMessage message : messages) {
			labels.add(message.getMessageLabel() + (message.getSender() >= 0 ? "@" + message.getSender() : ""));
		}
		return labels;
	}

	@Test
	public void testPlayersAreSortedByTeamWithNamesAndPing() {
		receiveAllFine();

		List<InGamePlayerStatus> statuses = controller.getPlayerStatuses();
		assertEquals(4, statuses.size());
		assertEquals(LOCAL, statuses.get(0).getPlayerId());
		assertEquals(THIRD, statuses.get(1).getPlayerId());
		assertEquals(OTHER, statuses.get(2).getPlayerId());
		assertEquals(AI, statuses.get(3).getPlayerId());

		assertTrue(status(LOCAL).isLocalPlayer());
		assertEquals("other", status(OTHER).getName());
		assertEquals("other", controller.getPlayerName(OTHER));
		assertEquals(42, status(OTHER).getPingMs());
		assertEquals(EPlayerConnectionState.CONNECTED, status(OTHER).getConnectionState());
	}

	@Test
	public void testPlayersNotReportedByTheServerAreComputerPlayers() {
		assertFalse("the clients do not know the computer players before the server reports the humans", status(AI).isAi());
		assertEquals(EPlayerConnectionState.UNKNOWN, status(OTHER).getConnectionState());

		receiveAllFine();

		assertTrue(status(AI).isAi());
		assertNull(status(AI).getConnectionState());
		assertNull(status(AI).getName());
		assertEquals(-1, status(AI).getPingMs());
		assertFalse(status(OTHER).isAi());
	}

	@Test
	public void testSinglePlayerUsesPlayerTypes() {
		controller = createController(false, new EPlayerType[] { EPlayerType.HUMAN, EPlayerType.AI_HARD, EPlayerType.AI_EASY, EPlayerType.AI_VERY_EASY, null });
		millisSinceLastProgress = 10000;

		checkAfter(500);

		assertFalse(status(LOCAL).isAi());
		assertTrue(status(OTHER).isAi());
		assertNull(status(LOCAL).getConnectionState());
		assertTrue(messages.isEmpty());
	}

	@Test
	public void testLeftPlayerIsReportedOnce() {
		receiveAllFine();
		receive(new PlayerStatusPacket(OTHER, "other", false, -1, 10000, 0));
		receive(new PlayerStatusPacket(OTHER, "other", false, -1, 10000, 0));

		assertEquals(List.of("network_player_left@1"), labels());
		assertEquals(EPlayerStatusColor.ERROR, EPlayerStatusColor.of(messages.get(0)));
		assertEquals(EMessageType.PLAYER_STATUS, messages.get(0).getType());
		assertEquals(EPlayerConnectionState.DISCONNECTED, status(OTHER).getConnectionState());
		assertEquals(-1, status(OTHER).getPingMs());
	}

	@Test
	public void testWaitingForLaggingPlayer() {
		receiveAllFine();
		checkAfter(500);
		assertTrue(messages.isEmpty());

		millisSinceLastProgress = 2000;
		receive(new PlayerStatusPacket(LOCAL, "host", true, 1, 20000, 50), new PlayerStatusPacket(OTHER, "other", true, 42, 20000, 50),
				new PlayerStatusPacket(THIRD, "third", true, 80, 17000, 60));
		checkAfter(500);
		checkAfter(500);

		assertEquals(List.of("network_waiting_for_player@3"), labels());
		assertEquals(EPlayerStatusColor.WARNING, EPlayerStatusColor.of(messages.get(0)));
		assertEquals(EPlayerConnectionState.WAITING, status(THIRD).getConnectionState());
		assertEquals(EPlayerConnectionState.CONNECTED, status(OTHER).getConnectionState());

		millisSinceLastProgress = 0;
		receiveAllFine();
		checkAfter(500);
		assertEquals("the player is only fine again after some time", EPlayerConnectionState.WAITING, status(THIRD).getConnectionState());
		checkAfter(PlayerStatusController.RECOVERY_MS);

		assertEquals(List.of("network_waiting_for_player@3", "network_player_responding@3"), labels());
		assertEquals(EPlayerStatusColor.OK, EPlayerStatusColor.of(messages.get(1)));
		assertEquals(EPlayerConnectionState.CONNECTED, status(THIRD).getConnectionState());
	}

	@Test
	public void testLocalPlayerIsNeverWaitedFor() {
		millisSinceLastProgress = 2000;
		receive(new PlayerStatusPacket(LOCAL, "host", true, 1, 5000, 50), new PlayerStatusPacket(OTHER, "other", true, 42, 20000, 50));
		checkAfter(500);

		assertFalse(labels().contains("network_waiting_for_player@0"));
		assertEquals(EPlayerConnectionState.CONNECTED, status(LOCAL).getConnectionState());
	}

	@Test
	public void testNoWaitingWhilePaused() {
		pausing = true;
		millisSinceLastProgress = 10000;
		for (int i = 0; i < 10; i++) { // the server keeps sending the status while the game is paused
			receive(new PlayerStatusPacket(LOCAL, "host", true, 1, 20000, 50), new PlayerStatusPacket(OTHER, "other", true, 42, 15000, 50));
			checkAfter(500);
		}

		assertTrue(labels().toString(), messages.isEmpty());
	}

	@Test
	public void testNotRespondingPlayerIsReportedWithoutStall() {
		receive(new PlayerStatusPacket(LOCAL, "host", true, 1, 20000, 50), new PlayerStatusPacket(OTHER, "other", true, 42, 15000, 5000));
		checkAfter(500);

		assertEquals(List.of("network_player_not_responding@1"), labels());
		assertEquals(EPlayerConnectionState.WAITING, status(OTHER).getConnectionState());
	}

	@Test
	public void testGeneralStallWithoutCulprit() {
		receiveAllFine();
		millisSinceLastProgress = 2000;
		checkAfter(500);
		assertTrue(messages.isEmpty());

		millisSinceLastProgress = 3500;
		receiveAllFine();
		checkAfter(500);
		checkAfter(500);
		assertEquals(List.of("network_waiting_for_players"), labels());

		millisSinceLastProgress = 0;
		checkAfter(500);
		assertEquals(List.of("network_waiting_for_players", "network_game_continues"), labels());
	}

	@Test
	public void testLostConnection() {
		receiveAllFine();
		connected = false;
		checkAfter(500);
		checkAfter(500);

		assertEquals(List.of("network_connection_lost"), labels());
		assertEquals(EPlayerStatusColor.ERROR, EPlayerStatusColor.of(messages.get(0)));
		assertEquals(EPlayerConnectionState.DISCONNECTED, status(LOCAL).getConnectionState());
		assertEquals(EPlayerConnectionState.UNKNOWN, status(OTHER).getConnectionState());
	}

	@Test
	public void testMissingStatusUpdates() {
		receiveAllFine();
		checkAfter(PlayerStatusController.STATUS_TIMEOUT_MS + 1);
		checkAfter(500);
		assertEquals(List.of("network_connection_problems"), labels());

		receiveAllFine();
		checkAfter(500);
		assertEquals(List.of("network_connection_problems", "network_connection_restored"), labels());
	}

	@Test
	public void testDefeatedPlayerIsReportedOnce() {
		receiveAllFine();
		players[AI].winState = EWinState.LOST;
		players[LOCAL].winState = EWinState.LOST;
		checkAfter(500);
		checkAfter(500);

		assertEquals(List.of("player_defeated@2"), labels());
		assertTrue(status(AI).isDefeated());
	}

	private enum EPlayerStatusColor {
		OK, WARNING, ERROR, OTHER;

		static EPlayerStatusColor of(IMessage message) {
			if (message.getIndicatorColor() == PlayerStatusColors.OK) {
				return OK;
			} else if (message.getIndicatorColor() == PlayerStatusColors.WARNING) {
				return WARNING;
			} else if (message.getIndicatorColor() == PlayerStatusColors.ERROR) {
				return ERROR;
			}
			return OTHER;
		}
	}

	@Test
	public void testConnectionNotice() {
		receiveAllFine();
		checkAfter(500);
		assertEquals(ConnectionNotice.Type.NONE, controller.getConnectionNotice().getType());

		// The game has been waiting for a lagging player for a while: interrupted, waiting for him.
		millisSinceLastProgress = PlayerStatusController.GENERAL_STALL_MESSAGE_MS + 500;
		receive(new PlayerStatusPacket(LOCAL, "host", true, 1, 20000, 50), new PlayerStatusPacket(OTHER, "other", true, 42, 20000, 50),
				new PlayerStatusPacket(THIRD, "third", true, 80, 17000, 60));
		checkAfter(500);
		ConnectionNotice notice = controller.getConnectionNotice();
		assertEquals(ConnectionNotice.Type.INTERRUPTED, notice.getType());
		assertEquals(List.of("third"), notice.getWaitingFor());

		// No status for longer than the timeout (own connection gone for the moment): interrupted, nobody named.
		now += PlayerStatusController.STATUS_TIMEOUT_MS + 1;
		notice = controller.getConnectionNotice();
		assertEquals(ConnectionNotice.Type.INTERRUPTED, notice.getType());
		assertTrue(notice.getWaitingFor().isEmpty());

		// Statuses flow again and the game advances: nothing to report.
		millisSinceLastProgress = 0;
		receiveAllFine();
		assertEquals(ConnectionNotice.Type.NONE, controller.getConnectionNotice().getType());

		// Connection to the server closed: lost.
		connected = false;
		assertEquals(ConnectionNotice.Type.LOST, controller.getConnectionNotice().getType());

		// Single player: never a notice.
		controller = createController(false, new EPlayerType[] { EPlayerType.HUMAN, EPlayerType.AI_HARD, null, null, null });
		assertEquals(ConnectionNotice.Type.NONE, controller.getConnectionNotice().getType());
	}

	private static class TestPlayer implements IPlayer {
		private final byte playerId;
		private final byte teamId;
		private EWinState winState = EWinState.UNDECIDED;

		TestPlayer(byte playerId, int teamId) {
			this.playerId = playerId;
			this.teamId = (byte) teamId;
		}

		@Override
		public byte getPlayerId() {
			return playerId;
		}

		@Override
		public byte getTeamId() {
			return teamId;
		}

		@Override
		public EWinState getWinState() {
			return winState;
		}

		@Override
		public ECivilisation getCivilisation() {
			return ECivilisation.ROMAN;
		}
	}
}
