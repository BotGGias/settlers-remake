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

import java.io.Serializable;

import jsettlers.common.movable.EMovableType;
import jsettlers.common.selectable.ESelectionType;
import jsettlers.common.statistics.EStatisticType;
import jsettlers.logic.buildings.Building;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.partition.PartitionsGrid;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.movable.interfaces.ILogicMovable;
import jsettlers.logic.player.Player;
import jsettlers.logic.timer.IScheduledTimerable;
import jsettlers.logic.timer.RescheduleTimer;

/**
 * Records the {@link EStatisticType} values of all players once every game minute into their {@link jsettlers.logic.player.StatisticsHistory}.
 * <p>
 * The recorder only reads the game state, so it does not affect the synchronization of multiplayer games.
 */
public class StatisticsRecorder implements IScheduledTimerable, Serializable {
	private static final long serialVersionUID = 1L;

	static final int SAMPLE_INTERVAL_MS = 60 * 1000;

	private final PartitionsGrid partitionsGrid;

	public StatisticsRecorder(PartitionsGrid partitionsGrid) {
		this.partitionsGrid = partitionsGrid;
	}

	/**
	 * Records the current values and schedules the recording of the next ones.
	 */
	public void start() {
		recordSample(false);
		RescheduleTimer.add(this, SAMPLE_INTERVAL_MS);
	}

	@Override
	public int timerEvent() {
		recordSample(false);
		return SAMPLE_INTERVAL_MS;
	}

	@Override
	public void kill() {
	}

	/**
	 * Records the current values of all players.
	 *
	 * @param force
	 *            true to record the values even if the last sample is very recent, e.g. when the game ends.
	 */
	public void recordSample(boolean force) {
		Player[] players = partitionsGrid.getPlayers();
		int[][] values = new int[players.length][EStatisticType.NUMBER_OF_TYPES];

		countMovables(values);
		countBuildings(values);
		countLand(values);

		int time = MatchConstants.clock().getTime();
		for (int playerId = 0; playerId < players.length; playerId++) {
			Player player = players[playerId];
			if (player == null) {
				continue;
			}
			int[] playerValues = values[playerId];
			playerValues[EStatisticType.RECRUITED_SOLDIERS.ordinal()] = player.getEndgameStatistic().getAmountOfProducedSoldiers();
			playerValues[EStatisticType.PRODUCED_GOODS.ordinal()] = player.getProductionStatistic().getTotalAmountProduced();
			playerValues[EStatisticType.GOLD.ordinal()] = player.getEndgameStatistic().getAmountOfProducedGold();
			playerValues[EStatisticType.MANNA.ordinal()] = player.getMannaInformation().getAmountOfProducedManna();

			player.getStatisticsHistory().addSample(time, playerValues, force);
		}
	}

	private static void countMovables(int[][] values) {
		for (ILogicMovable movable : MovableManager.getAllMovables()) {
			Player player = movable.getPlayer();
			if (player == null || !movable.isAlive() || player.getPlayerId() < 0 || player.getPlayerId() >= values.length) {
				continue;
			}

			EMovableType type = movable.getMovableType();
			if (!isSettler(type)) {
				continue;
			}

			int[] playerValues = values[player.getPlayerId()];
			playerValues[EStatisticType.SETTLERS.ordinal()]++;
			if (EMovableType.SOLDIERS.contains(type)) {
				playerValues[EStatisticType.SOLDIERS.ordinal()]++;
			}
			playerValues[EStatisticType.MILITARY_STRENGTH.ordinal()] += getMilitaryStrength(type);
		}
	}

	private static boolean isSettler(EMovableType type) {
		return type.selectionType != ESelectionType.SHIPS && type != EMovableType.DONKEY && type != EMovableType.WHITEFLAGGED_DONKEY;
	}

	/**
	 * @return The contribution of a movable of the given type to the military strength of its player.
	 */
	static int getMilitaryStrength(EMovableType type) {
		switch (type) {
		case SWORDSMAN_L1:
		case PIKEMAN_L1:
		case BOWMAN_L1:
			return 1;
		case SWORDSMAN_L2:
		case PIKEMAN_L2:
		case BOWMAN_L2:
		case MAGE:
			return 2;
		case SWORDSMAN_L3:
		case PIKEMAN_L3:
		case BOWMAN_L3:
			return 3;
		default:
			return 0;
		}
	}

	private static void countBuildings(int[][] values) {
		for (Building building : Building.getAllBuildings()) {
			Player player = building.getPlayer();
			if (player == null || building.isDestroyed() || !building.isConstructionFinished()
					|| player.getPlayerId() < 0 || player.getPlayerId() >= values.length) {
				continue;
			}
			values[player.getPlayerId()][EStatisticType.BUILDINGS.ordinal()]++;
		}
	}

	private void countLand(int[][] values) {
		int[] positions = partitionsGrid.countPositionsPerPlayer();
		for (int playerId = 0; playerId < values.length && playerId < positions.length; playerId++) {
			values[playerId][EStatisticType.LAND.ordinal()] = positions[playerId];
		}
	}
}
