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
package jsettlers.integration.movables;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jsettlers.common.CommonConstants;
import jsettlers.common.action.EMoveToType;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.integration.ai.AiTestUtils;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.movable.interfaces.ILogicMovable;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.replay.ReplayUtils;
import jsettlers.testutils.TestUtils;
import jsettlers.testutils.map.MapUtils;

/**
 * Tests the waypoints of player controlled movables ({@link EMoveToType#WAYPOINT}, Shift+click).
 */
public class WaypointIT {
	private static final byte PLAYER = 0;
	private static final int STEP_MS = 100;
	private static final int TIMEOUT_MS = 30 * 1000;
	private static final int REACHED_DISTANCE = 2;

	static {
		CommonConstants.ENABLE_CONSOLE_LOGGING = true;
		Constants.FOG_OF_WAR_DEFAULT_ENABLED = false;

		TestUtils.setupTempResourceManager();
	}

	private int time;

	@Test
	public void testWaypoints() throws MapLoadException {
		MapLoader map = MapUtils.getMountainlake();
		PlayerSetting[] playerSettings = AiTestUtils.getDefaultPlayerSettings(map.getMaxPlayers());
		playerSettings[PLAYER] = new PlayerSetting(EPlayerType.HUMAN, ECivilisation.ROMAN, PLAYER);

		JSettlersGame.GameRunner game = AiTestUtils.createStartingGame(playerSettings, map);
		IStartedGame startedGame = ReplayUtils.waitForGameStartup(game);
		try {
			MatchConstants.clock().setPausing(true); // the test thread executes the game
			time = 1000;
			MatchConstants.clock().fastForwardTo(time);

			// the soldiers on the map are all in towers => use a thief (without foreign materials around, it just walks to its targets)
			ShortPoint2D bearerPosition = MovableManager.getAllMovables().stream()
					.filter(movable -> movable.isAlive() && movable.getPlayer().getPlayerId() == PLAYER && movable.getMovableType() == EMovableType.BEARER)
					.findFirst().get().getPosition();
			game.getMainGrid().getGuiInputGrid().convertAtPosition(PLAYER, bearerPosition, EMovableType.BEARER, EMovableType.THIEF, 1);
			ILogicMovable unit = MovableManager.getAllMovables().stream()
					.filter(movable -> movable.isAlive() && movable.getPlayer().getPlayerId() == PLAYER && movable.getMovableType() == EMovableType.THIEF)
					.findFirst().orElseThrow(() -> new AssertionError("no thief created"));
			ShortPoint2D start = unit.getPosition();
			ShortPoint2D other = findReachablePosition(unit, start);

			// the unit walks to the target and then on to the waypoint
			unit.moveTo(start, EMoveToType.DEFAULT);
			unit.moveTo(other, EMoveToType.WAYPOINT);
			assertTrue("unit did not reach the first target", runUntilReached(unit, start));
			assertTrue("unit did not continue to the waypoint", runUntilReached(unit, other));

			// an idle unit directly walks to a waypoint
			unit.moveTo(start, EMoveToType.WAYPOINT);
			assertTrue("idle unit did not walk to the waypoint", runUntilReached(unit, start));

			// a normal move order discards the waypoints
			unit.moveTo(other, EMoveToType.DEFAULT);
			unit.moveTo(start, EMoveToType.WAYPOINT);
			unit.moveTo(other, EMoveToType.DEFAULT);
			assertTrue("unit did not reach the new target", runUntilReached(unit, other));
			runFor(10 * 1000);
			assertTrue("unit did not discard its waypoints", unit.getPosition().getOnGridDistTo(other) <= REACHED_DISTANCE);
		} finally {
			ReplayUtils.awaitShutdown(startedGame);
		}
	}

	/**
	 * Sends the movable to positions around the start until it reaches one of them with a normal move order.
	 */
	private ShortPoint2D findReachablePosition(ILogicMovable movable, ShortPoint2D start) {
		int[][] offsets = { { 8, 0 }, { -8, 0 }, { 0, 8 }, { 0, -8 }, { 8, 8 }, { -8, -8 } };
		for (int[] offset : offsets) {
			ShortPoint2D candidate = new ShortPoint2D(start.x + offset[0], start.y + offset[1]);
			movable.moveTo(candidate, EMoveToType.DEFAULT);
			if (runUntilReached(movable, candidate)) {
				return candidate;
			}
		}
		throw new AssertionError("no reachable position found around " + start);
	}

	private boolean runUntilReached(ILogicMovable movable, ShortPoint2D target) {
		for (int passed = 0; passed < TIMEOUT_MS; passed += STEP_MS) {
			runFor(STEP_MS);
			if (movable.getPosition().getOnGridDistTo(target) <= REACHED_DISTANCE) {
				return true;
			}
		}
		return false;
	}

	private void runFor(int ms) {
		time += ms;
		MatchConstants.clock().fastForwardTo(time);
	}
}
