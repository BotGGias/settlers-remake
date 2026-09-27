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
package jsettlers.logic.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.player.EWinState;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.GameStatistics;
import jsettlers.common.statistics.GameStatistics.PlayerStatistics;
import jsettlers.common.statistics.IGameTimeProvider;
import jsettlers.logic.player.Player;
import jsettlers.logic.player.Team;

public class GameStatisticsTest {
	private static final int MINUTE = 60 * 1000;

	private static Player createPlayer(int playerId, int teamId) {
		return new Player((byte) playerId, new Team((byte) teamId), (byte) 3, EPlayerType.HUMAN, ECivilisation.ROMAN);
	}

	private static void addSample(Player player, int time, int settlers) {
		int[] values = new int[EStatisticType.NUMBER_OF_TYPES];
		values[EStatisticType.SETTLERS.ordinal()] = settlers;
		player.getStatisticsHistory().addSample(time, values, false);
	}

	@Test
	public void testCreate() {
		Player player0 = createPlayer(0, 0);
		Player player2 = createPlayer(2, 1);
		player2.setWinState(EWinState.LOST);
		addSample(player0, 0, 10);
		addSample(player0, MINUTE, 25);
		addSample(player2, 0, 10);
		addSample(player2, 2 * MINUTE, 5);

		List<InGamePlayerStatus> statuses = Arrays.asList(
				new InGamePlayerStatus((byte) 0, (byte) 0, "Alice", false, true, EWinState.UNDECIDED, null, -1),
				new InGamePlayerStatus((byte) 2, (byte) 1, null, true, false, EWinState.LOST, null, -1));
		GameStatistics statistics = GameStatistics.create(new FakeGame(new Player[] { player0, null, player2 }, player0, statuses));

		List<PlayerStatistics> players = statistics.getPlayers();
		assertEquals(2, players.size());

		PlayerStatistics statistics0 = players.get(0);
		assertEquals(0, statistics0.getPlayerId());
		assertEquals("Alice", statistics0.getName());
		assertTrue(statistics0.isLocalPlayer());
		assertFalse(statistics0.isAi());
		assertEquals(25, statistics0.getSeries().getLastValue(EStatisticType.SETTLERS));

		PlayerStatistics statistics2 = players.get(1);
		assertEquals(2, statistics2.getPlayerId());
		assertEquals(1, statistics2.getTeamId());
		assertNull(statistics2.getName());
		assertTrue(statistics2.isAi());
		assertFalse(statistics2.isLocalPlayer());
		assertEquals(EWinState.LOST, statistics2.getWinState());

		assertEquals(statistics0, statistics.getLocalPlayer());
		assertEquals(2 * MINUTE, statistics.getLastTime());
		assertEquals(25, statistics.getMaxValue(EStatisticType.SETTLERS));
		assertTrue(statistics.hasSamples());
	}

	@Test
	public void testStatisticsAreNotChangedByLaterSamples() {
		Player player = createPlayer(0, 0);
		addSample(player, 0, 10);
		GameStatistics statistics = GameStatistics.create(new FakeGame(new Player[] { player }, player, Arrays.asList()));

		addSample(player, MINUTE, 20);

		assertEquals(1, statistics.getPlayers().get(0).getSeries().getSampleCount());
		assertEquals(10, statistics.getMaxValue(EStatisticType.SETTLERS));
	}

	@Test
	public void testWithoutSamples() {
		Player player = createPlayer(0, 0);
		GameStatistics statistics = GameStatistics.create(new FakeGame(new Player[] { player }, null, Arrays.asList()));

		assertFalse(statistics.hasSamples());
		assertNull(statistics.getLocalPlayer());
		assertEquals(0, statistics.getLastTime());
	}

	@Test
	public void testMilitaryStrength() {
		assertEquals(1, StatisticsRecorder.getMilitaryStrength(EMovableType.SWORDSMAN_L1));
		assertEquals(2, StatisticsRecorder.getMilitaryStrength(EMovableType.BOWMAN_L2));
		assertEquals(3, StatisticsRecorder.getMilitaryStrength(EMovableType.PIKEMAN_L3));
		assertEquals(2, StatisticsRecorder.getMilitaryStrength(EMovableType.MAGE));
		assertEquals(0, StatisticsRecorder.getMilitaryStrength(EMovableType.BEARER));
		for (EMovableType soldier : EMovableType.SOLDIERS) {
			assertTrue(StatisticsRecorder.getMilitaryStrength(soldier) > 0);
		}
	}

	private static class FakeGame implements IStartedGame {
		private final Player[] players;
		private final Player localPlayer;
		private final List<InGamePlayerStatus> statuses;

		FakeGame(Player[] players, Player localPlayer, List<InGamePlayerStatus> statuses) {
			this.players = players;
			this.localPlayer = localPlayer;
			this.statuses = statuses;
		}

		@Override
		public IGraphicsGrid getMap() {
			return null;
		}

		@Override
		public IGameTimeProvider getGameTimeProvider() {
			return null;
		}

		@Override
		public IInGamePlayer[] getAllInGamePlayers() {
			return players;
		}

		@Override
		public IInGamePlayer getInGamePlayer() {
			return localPlayer;
		}

		@Override
		public void setGameExitListener(Consumer<IStartedGame> exitListener) {
		}

		@Override
		public boolean isShutdownFinished() {
			return true;
		}

		@Override
		public boolean isMultiplayerGame() {
			return false;
		}

		@Override
		public List<InGamePlayerStatus> getPlayerStatuses() {
			return statuses;
		}
	}
}
