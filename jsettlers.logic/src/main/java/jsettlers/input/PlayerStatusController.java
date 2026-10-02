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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.LongSupplier;

import jsettlers.common.Color;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.ConnectionNotice;
import jsettlers.common.menu.EPlayerConnectionState;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.menu.PlayerStatusColors;
import jsettlers.common.menu.messages.IMessenger;
import jsettlers.common.menu.messages.SimpleMessage;
import jsettlers.common.player.EWinState;
import jsettlers.common.player.IPlayer;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.common.packets.PlayerStatusPacket;
import jsettlers.network.common.packets.PlayerStatusesPacket;

/**
 * Keeps track of the state of all players of a running game: whether they are computer players, have been defeated and - in multiplayer games -
 * their network state and ping.
 * <p>
 * The network state is sent regularly by the server. Additionally, the local game clock is observed: if it does not advance for some time, the
 * game waits for other players. Changes of the state are shown to the local player as short messages with a colored status dot.
 */
public class PlayerStatusController {
	static final int CHECK_INTERVAL_MS = 500;
	/**
	 * If the game clock does not advance for this time, the game waits for other players.
	 */
	static final int STALLED_THRESHOLD_MS = 1500;
	/**
	 * If the game waits this long and no player can be blamed, a general message is shown.
	 */
	static final int GENERAL_STALL_MESSAGE_MS = 3000;
	/**
	 * A player that did not report his game time to the server for this time does not respond.
	 */
	static final int NOT_RESPONDING_MS = 3000;
	/**
	 * While the game waits, a player that did not report his game time for this time or is behind the others for this time is waited for.
	 */
	static final int LAGGING_MS = 1000;
	/**
	 * A player is only considered to be fine again after he has not been waited for during this time. This prevents flickering messages.
	 */
	static final int RECOVERY_MS = 2000;
	/**
	 * If no status is received from the server for this time, the connection to the server has problems.
	 */
	static final int STATUS_TIMEOUT_MS = 4000;

	private final IGameClock clock;
	private final INetworkConnector networkConnector;
	private final boolean multiplayer;
	private final byte localPlayerId;
	private final EPlayerType[] playerTypes;
	private final IPlayer[] players;
	private final IMessenger localMessenger;
	private final LongSupplier currentTime;

	private final Map<Byte, PlayerStatusPacket> networkStatuses = new HashMap<>();
	private long lastStatusReceivedMs = 0;

	private final Set<Byte> leftPlayers = new HashSet<>();
	private final Set<Byte> defeatedPlayers = new HashSet<>();
	private final Map<Byte, Long> waitingPlayers = new HashMap<>(); // player id => last time he was waited for
	private boolean generalWaitingShown = false;
	private boolean connectionProblemShown = false;
	private boolean connectionLostShown = false;

	private Timer timer;

	/**
	 * @param playerTypes
	 *            The type of every player of the game or null for unavailable players. In multiplayer games, only the host knows which players
	 *            are computer players. On the other clients, the players the server reports are the human ones.
	 * @param players
	 *            The players of the game or null for unavailable players.
	 */
	public PlayerStatusController(IGameClock clock, INetworkConnector networkConnector, boolean multiplayer, byte localPlayerId,
			EPlayerType[] playerTypes, IPlayer[] players, IMessenger localMessenger) {
		this(clock, networkConnector, multiplayer, localPlayerId, playerTypes, players, localMessenger, System::currentTimeMillis);
	}

	PlayerStatusController(IGameClock clock, INetworkConnector networkConnector, boolean multiplayer, byte localPlayerId, EPlayerType[] playerTypes,
			IPlayer[] players, IMessenger localMessenger, LongSupplier currentTime) {
		this.clock = clock;
		this.networkConnector = networkConnector;
		this.multiplayer = multiplayer;
		this.localPlayerId = localPlayerId;
		this.playerTypes = playerTypes;
		this.players = players;
		this.localMessenger = localMessenger;
		this.currentTime = currentTime;
	}

	/**
	 * Starts to observe the game. Must be called after the game has started.
	 */
	public synchronized void start() {
		if (timer == null) {
			timer = new Timer("PlayerStatusController", true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						check();
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
			}, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS);
		}
	}

	public synchronized void shutdown() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	/**
	 * Called by the network thread whenever the server sends the status of the players.
	 */
	public synchronized void playerStatusReceived(PlayerStatusesPacket packet) {
		lastStatusReceivedMs = currentTime.getAsLong();
		for (PlayerStatusPacket status : packet.getStatuses()) {
			byte playerId = status.getInGamePlayerId();
			if (!isValidPlayerId(playerId)) {
				continue;
			}

			networkStatuses.put(playerId, status);
			if (!status.isConnected() && leftPlayers.add(playerId)) {
				waitingPlayers.remove(playerId);
				if (playerId != localPlayerId) {
					showMessage("network_player_left", playerId, PlayerStatusColors.ERROR);
				}
			}
		}
	}

	/**
	 * Checks the state of all players and shows messages about changes. It is called regularly.
	 */
	synchronized void check() {
		checkDefeatedPlayers();

		if (!multiplayer) {
			return;
		}

		if (!networkConnector.isConnected()) {
			if (!connectionLostShown) {
				connectionLostShown = true;
				showMessage("network_connection_lost", (byte) -1, PlayerStatusColors.ERROR);
			}
			return;
		}

		long now = currentTime.getAsLong();
		boolean stalled = !clock.isPausing() && clock.getMillisSinceLastProgress() > STALLED_THRESHOLD_MS;
		boolean statusTimedOut = lastStatusReceivedMs != 0 && now - lastStatusReceivedMs > STATUS_TIMEOUT_MS;

		if (statusTimedOut != connectionProblemShown) {
			connectionProblemShown = statusTimedOut;
			showMessage(statusTimedOut ? "network_connection_problems" : "network_connection_restored", (byte) -1,
					statusTimedOut ? PlayerStatusColors.WARNING : PlayerStatusColors.OK);
		}

		Set<Byte> currentlyWaitingFor = statusTimedOut ? new HashSet<>() : determinePlayersToWaitFor(stalled);
		for (Byte playerId : currentlyWaitingFor) {
			if (waitingPlayers.put(playerId, now) == null) {
				showMessage(stalled ? "network_waiting_for_player" : "network_player_not_responding", playerId, PlayerStatusColors.WARNING);
			}
		}
		waitingPlayers.entrySet().removeIf(entry -> {
			if (!currentlyWaitingFor.contains(entry.getKey()) && now - entry.getValue() >= RECOVERY_MS) {
				showMessage("network_player_responding", entry.getKey(), PlayerStatusColors.OK);
				return true;
			}
			return false;
		});

		boolean generalStall = stalled && waitingPlayers.isEmpty() && !statusTimedOut
				&& clock.getMillisSinceLastProgress() > GENERAL_STALL_MESSAGE_MS;
		if (generalStall && !generalWaitingShown) {
			generalWaitingShown = true;
			showMessage("network_waiting_for_players", (byte) -1, PlayerStatusColors.WARNING);
		} else if (!stalled && generalWaitingShown) {
			generalWaitingShown = false;
			showMessage("network_game_continues", (byte) -1, PlayerStatusColors.OK);
		}
	}

	private Set<Byte> determinePlayersToWaitFor(boolean stalled) {
		int maxGameTime = Integer.MIN_VALUE;
		for (PlayerStatusPacket status : networkStatuses.values()) {
			if (status.isConnected()) {
				maxGameTime = Math.max(maxGameTime, status.getGameTime());
			}
		}

		Set<Byte> result = new HashSet<>();
		for (PlayerStatusPacket status : networkStatuses.values()) {
			if (!status.isConnected() || status.getInGamePlayerId() == localPlayerId) {
				continue;
			}

			boolean notResponding = status.getMillisSinceLastSync() > NOT_RESPONDING_MS;
			boolean lagging = stalled && (status.getMillisSinceLastSync() > LAGGING_MS || maxGameTime - status.getGameTime() > LAGGING_MS);
			if (notResponding || lagging) {
				result.add(status.getInGamePlayerId());
			}
		}
		return result;
	}

	private void checkDefeatedPlayers() {
		for (byte playerId = 0; playerId < players.length; playerId++) {
			IPlayer player = players[playerId];
			if (player != null && player.getWinState() == EWinState.LOST && defeatedPlayers.add(playerId) && playerId != localPlayerId) {
				showMessage("player_defeated", playerId, PlayerStatusColors.INACTIVE);
			}
		}
	}

	/**
	 * @return The network condition in one sentence (for a prominent notice): lost when the connection to the server is closed,
	 *         interrupted when no status arrives any more (the own connection is gone for the moment – with the Settlers United
	 *         launcher it is restored within minutes) or the game has been waiting for players for a while, otherwise none.
	 */
	public synchronized ConnectionNotice getConnectionNotice() {
		if (!multiplayer) {
			return ConnectionNotice.NONE;
		}
		if (!networkConnector.isConnected()) {
			return new ConnectionNotice(ConnectionNotice.Type.LOST, Collections.emptyList());
		}
		long now = currentTime.getAsLong();
		if (lastStatusReceivedMs != 0 && now - lastStatusReceivedMs > STATUS_TIMEOUT_MS) {
			return new ConnectionNotice(ConnectionNotice.Type.INTERRUPTED, Collections.emptyList());
		}
		boolean stalled = !clock.isPausing() && clock.getMillisSinceLastProgress() > GENERAL_STALL_MESSAGE_MS;
		if (!stalled) {
			return ConnectionNotice.NONE;
		}
		List<String> names = new ArrayList<>();
		for (Byte playerId : waitingPlayers.keySet()) {
			if (playerId != localPlayerId && !leftPlayers.contains(playerId)) {
				String name = getPlayerName(playerId);
				names.add(name != null ? name : "#" + (playerId + 1));
			}
		}
		return new ConnectionNotice(ConnectionNotice.Type.INTERRUPTED, names);
	}

	/**
	 * @return The current state of all players, sorted by team and player id.
	 */
	public synchronized List<InGamePlayerStatus> getPlayerStatuses() {
		boolean connected = networkConnector.isConnected();
		List<InGamePlayerStatus> result = new ArrayList<>();

		for (byte playerId = 0; playerId < players.length; playerId++) {
			IPlayer player = players[playerId];
			if (player == null) {
				continue;
			}

			PlayerStatusPacket networkStatus = networkStatuses.get(playerId);
			boolean local = playerId == localPlayerId;
			boolean ai = isAi(playerId);

			EPlayerConnectionState connectionState = null;
			int ping = -1;
			if (multiplayer && !ai) {
				if (networkStatus != null && networkStatus.isConnected()) {
					ping = networkStatus.getPingMs();
				}
				connectionState = getConnectionState(playerId, local, connected, networkStatus);
			}

			String name = networkStatus != null ? networkStatus.getName() : null;
			result.add(new InGamePlayerStatus(playerId, player.getTeamId(), name, ai, local, player.getWinState(), connectionState, ping));
		}

		result.sort(Comparator.comparingInt(InGamePlayerStatus::getTeamId).thenComparingInt(InGamePlayerStatus::getPlayerId));
		return result;
	}

	private EPlayerConnectionState getConnectionState(byte playerId, boolean local, boolean connected, PlayerStatusPacket networkStatus) {
		if (local) {
			return connected ? EPlayerConnectionState.CONNECTED : EPlayerConnectionState.DISCONNECTED;
		} else if (!connected || networkStatus == null) {
			return EPlayerConnectionState.UNKNOWN;
		} else if (!networkStatus.isConnected()) {
			return EPlayerConnectionState.DISCONNECTED;
		} else if (waitingPlayers.containsKey(playerId)) {
			return EPlayerConnectionState.WAITING;
		} else {
			return EPlayerConnectionState.CONNECTED;
		}
	}

	private boolean isAi(byte playerId) {
		if (multiplayer && !networkStatuses.isEmpty()) {
			return !networkStatuses.containsKey(playerId); // the server reports all human players
		}
		EPlayerType type = playerTypes[playerId];
		return type != null && type.isAi();
	}

	/**
	 * @return The name of a human player of a multiplayer game or null if it is not known.
	 */
	public synchronized String getPlayerName(byte playerId) {
		PlayerStatusPacket status = networkStatuses.get(playerId);
		return status != null ? status.getName() : null;
	}

	private boolean isValidPlayerId(byte playerId) {
		return playerId >= 0 && playerId < players.length;
	}

	private void showMessage(String label, byte playerId, Color color) {
		if (localMessenger != null) {
			localMessenger.showMessage(SimpleMessage.playerStatus(label, playerId, color));
		}
	}
}
