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
package jsettlers.network.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.network.NetworkConstants;
import jsettlers.network.TestUtils;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.common.packets.PlayerStatusPacket;
import jsettlers.network.common.packets.PlayerStatusesPacket;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.server.ServerManager;
import jsettlers.network.server.db.inMemory.InMemoryDB;
import jsettlers.network.server.match.EPlayerState;

/**
 * Tests that the server sends the network status of all players of a running match to the clients.
 */
public class PlayerStatusIT {
	private static final long TIMEOUT_MS = 5000;

	private final InMemoryDB db = new InMemoryDB();
	private final ServerManager manager = new ServerManager(db);

	private Channel server1Channel;
	private NetworkClient client1;
	private Channel server2Channel;
	private NetworkClient client2;

	private final List<PlayerStatusesPacket> statuses1 = new CopyOnWriteArrayList<>();
	private final List<PlayerStatusesPacket> statuses2 = new CopyOnWriteArrayList<>();

	@Before
	public void setUp() throws IOException {
		manager.start();

		AsyncChannel[] channels = TestUtils.setUpAsyncLoopbackChannels();
		server1Channel = channels[1];
		client1 = new NetworkClient(channels[0], null, new NetworkClientClockMock());
		manager.identifyNewChannel(server1Channel);

		channels = TestUtils.setUpAsyncLoopbackChannels();
		server2Channel = channels[1];
		client2 = new NetworkClient(channels[0], null, new NetworkClientClockMock());
		manager.identifyNewChannel(server2Channel);

		client1.setPlayerStatusListener(statuses1::add);
		client2.setPlayerStatusListener(statuses2::add);
	}

	@After
	public void tearDown() {
		client1.close();
		server1Channel.close();
		client2.close();
		server2Channel.close();
		manager.shutdown();
	}

	@Test
	public void testStatusOfAllPlayersIsSentDuringTheMatch() throws InterruptedException {
		startMatchWithTwoPlayers();

		PlayerStatusesPacket packet = waitForStatus(statuses2, p -> p.getStatuses().length == 2);
		PlayerStatusPacket host = find(packet, "host");
		PlayerStatusPacket guest = find(packet, "guest");

		assertTrue(host.isConnected());
		assertTrue(guest.isConnected());
		assertEquals(0, host.getInGamePlayerId());
		assertEquals(1, guest.getInGamePlayerId());
		assertTrue(host.getPingMs() >= 0);

		waitForStatus(statuses1, p -> p.getStatuses().length == 2);
	}

	@Test
	public void testLeftPlayerIsReportedAsDisconnected() throws InterruptedException {
		startMatchWithTwoPlayers();
		waitForStatus(statuses1, p -> p.getStatuses().length == 2);

		client2.leaveMatch();

		PlayerStatusesPacket packet = waitForStatus(statuses1, p -> {
			PlayerStatusPacket guest = find(p, "guest");
			return guest != null && !guest.isConnected();
		});
		assertTrue(find(packet, "host").isConnected());
		assertEquals(2, packet.getStatuses().length);
	}

	@Test
	public void testNoStatusBeforeTheMatchStarts() throws InterruptedException {
		logIn(client1, "id1", "host");
		client1.openNewMatch("TestMatch", 2, new MapInfoPacket("mapId", "mapName", "authorId", "authorName", 2), 4711L, null, null, null);
		waitFor(() -> client1.getState() == EPlayerState.IN_MATCH);

		Thread.sleep(NetworkConstants.Server.PLAYER_STATUS_SEND_INTERVAL_MS + 500);
		assertTrue(statuses1.isEmpty());
		assertFalse(statuses1.stream().anyMatch(p -> p.getStatuses().length > 0));
	}

	private void startMatchWithTwoPlayers() throws InterruptedException {
		logIn(client1, "id1", "host");
		logIn(client2, "id2", "guest");

		client1.openNewMatch("TestMatch", 2, new MapInfoPacket("mapId", "mapName", "authorId", "authorName", 2), 4711L, null, null, null);
		waitFor(() -> client1.getState() == EPlayerState.IN_MATCH && client1.getMatchInfo() != null);

		client2.joinMatch(client1.getMatchInfo().getId(), null, null, null);
		waitFor(() -> client2.getState() == EPlayerState.IN_MATCH);

		client1.setReadyState(true);
		client2.setReadyState(true);
		waitFor(() -> client1.getMatchInfo().getPlayers().length == 2 && client1.getMatchInfo().getPlayers()[0].isReady()
				&& client1.getMatchInfo().getPlayers()[1].isReady());

		client1.startMatch();
		waitFor(() -> client1.getState() == EPlayerState.IN_RUNNING_MATCH && client2.getState() == EPlayerState.IN_RUNNING_MATCH);
	}

	private static void logIn(NetworkClient client, String id, String name) throws InterruptedException {
		client.logIn(id, name, null);
		waitFor(() -> client.getState() == EPlayerState.LOGGED_IN);
	}

	private static PlayerStatusPacket find(PlayerStatusesPacket packet, String name) {
		for (PlayerStatusPacket status : packet.getStatuses()) {
			if (status.getName().equals(name)) {
				return status;
			}
		}
		return null;
	}

	private static PlayerStatusesPacket waitForStatus(List<PlayerStatusesPacket> received, Predicate<PlayerStatusesPacket> condition)
			throws InterruptedException {
		PlayerStatusesPacket[] result = new PlayerStatusesPacket[1];
		waitFor(() -> {
			for (PlayerStatusesPacket packet : received) {
				if (condition.test(packet)) {
					result[0] = packet;
					return true;
				}
			}
			return false;
		});
		assertNotNull(result[0]);
		return result[0];
	}

	private static void waitFor(BooleanSupplier condition) throws InterruptedException {
		long end = System.currentTimeMillis() + TIMEOUT_MS;
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() > end) {
				throw new AssertionError("condition not reached within " + TIMEOUT_MS + " ms");
			}
			Thread.sleep(10);
		}
	}
}
