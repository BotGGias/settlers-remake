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
package jsettlers.common.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.player.EWinState;
import jsettlers.common.player.IInGamePlayer;

/**
 * An immutable copy of the statistics of all players of a game. It stays valid after the game has been shut down.
 */
public final class GameStatistics implements Serializable {
	private static final long serialVersionUID = 1L;

	private final List<PlayerStatistics> players;
	private final byte localPlayerId;

	public GameStatistics(List<PlayerStatistics> players, byte localPlayerId) {
		this.players = Collections.unmodifiableList(new ArrayList<>(players));
		this.localPlayerId = localPlayerId;
	}

	/**
	 * Copies the statistics of all players of the given game.
	 *
	 * @param game
	 *            A running game or a game that has just been shut down.
	 * @return The statistics of the game.
	 */
	public static GameStatistics create(IStartedGame game) {
		Map<Byte, InGamePlayerStatus> statuses = new HashMap<>();
		for (InGamePlayerStatus status : game.getPlayerStatuses()) {
			statuses.put(status.getPlayerId(), status);
		}

		IInGamePlayer localPlayer = game.getInGamePlayer();
		byte localPlayerId = localPlayer != null ? localPlayer.getPlayerId() : -1;

		List<PlayerStatistics> players = new ArrayList<>();
		IInGamePlayer[] allPlayers = game.getAllInGamePlayers();
		if (allPlayers != null) {
			for (IInGamePlayer player : allPlayers) {
				if (player == null) {
					continue;
				}
				byte playerId = player.getPlayerId();
				InGamePlayerStatus status = statuses.get(playerId);

				String name = game.getPlayerName(playerId);
				if (name == null && status != null) {
					name = status.getName();
				}
				boolean ai = status != null && status.isAi();

				IStatisticsHistory history = player.getStatisticsHistory();
				StatisticsSeries series = history != null ? history.copySeries() : StatisticsSeries.EMPTY;

				players.add(new PlayerStatistics(playerId, player.getTeamId(), name, ai, playerId == localPlayerId, player.getWinState(),
						player.getCivilisation(), series));
			}
		}
		return new GameStatistics(players, localPlayerId);
	}

	/**
	 * @return The statistics of all players, sorted by their id.
	 */
	public List<PlayerStatistics> getPlayers() {
		return players;
	}

	/**
	 * @return The statistics of the local player or null if it is not known.
	 */
	public PlayerStatistics getLocalPlayer() {
		for (PlayerStatistics player : players) {
			if (player.getPlayerId() == localPlayerId) {
				return player;
			}
		}
		return null;
	}

	/**
	 * @return The time of the last sample of all players in milliseconds.
	 */
	public int getLastTime() {
		int lastTime = 0;
		for (PlayerStatistics player : players) {
			lastTime = Math.max(lastTime, player.getSeries().getLastTime());
		}
		return lastTime;
	}

	/**
	 * @return The biggest value of the given type of all players.
	 */
	public int getMaxValue(EStatisticType type) {
		int max = 0;
		for (PlayerStatistics player : players) {
			max = Math.max(max, player.getSeries().getMaxValue(type));
		}
		return max;
	}

	/**
	 * @return true if at least one sample has been recorded.
	 */
	public boolean hasSamples() {
		for (PlayerStatistics player : players) {
			if (player.getSeries().getSampleCount() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The statistics of one player of the game.
	 */
	public static final class PlayerStatistics implements Serializable {
		private static final long serialVersionUID = 1L;

		private final byte playerId;
		private final byte teamId;
		private final String name;
		private final boolean ai;
		private final boolean localPlayer;
		private final EWinState winState;
		private final ECivilisation civilisation;
		private final StatisticsSeries series;

		public PlayerStatistics(byte playerId, byte teamId, String name, boolean ai, boolean localPlayer, EWinState winState,
				ECivilisation civilisation, StatisticsSeries series) {
			this.playerId = playerId;
			this.teamId = teamId;
			this.name = name;
			this.ai = ai;
			this.localPlayer = localPlayer;
			this.winState = winState;
			this.civilisation = civilisation;
			this.series = series;
		}

		public byte getPlayerId() {
			return playerId;
		}

		public byte getTeamId() {
			return teamId;
		}

		/**
		 * @return The name of a human player of a multiplayer game or null if the player has no name.
		 */
		public String getName() {
			return name;
		}

		public boolean isAi() {
			return ai;
		}

		public boolean isLocalPlayer() {
			return localPlayer;
		}

		public EWinState getWinState() {
			return winState;
		}

		public ECivilisation getCivilisation() {
			return civilisation;
		}

		public StatisticsSeries getSeries() {
			return series;
		}
	}
}
