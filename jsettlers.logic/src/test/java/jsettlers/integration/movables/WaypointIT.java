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
 * Tests the waypoints of soldiers ({@link EMoveToType#WAYPOINT}, Shift+click).
 */
public class WaypointIT {
	private static final byte PLAYER = 0;
	private static final int STEP_MS = 100;
	private static final int TIMEOUT_MS = 60 * 1000;
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

			ILogicMovable soldier = MovableManager.getAllMovables().stream()
					.filter(movable -> movable.isAlive() && movable.getPlayer().getPlayerId() == PLAYER && movable.getMovableType().isSoldier())
					.findFirst().orElseThrow(() -> new AssertionError("no soldier on the map"));
			ShortPoint2D start = soldier.getPosition();
			ShortPoint2D other = MovableManager.getAllMovables().stream()
					.filter(movable -> movable.isAlive() && movable.getPlayer().getPlayerId() == PLAYER && movable.getMovableType() == EMovableType.BEARER)
					.map(ILogicMovable::getPosition)
					.filter(position -> position.getOnGridDistTo(start) >= 6)
					.findFirst().orElseThrow(() -> new AssertionError("no target position found"));

			// the soldier walks to the target and then back to the waypoint
			soldier.moveTo(other, EMoveToType.DEFAULT);
			soldier.moveTo(start, EMoveToType.WAYPOINT);
			assertTrue("soldier did not reach the first target", runUntilReached(soldier, other));
			assertTrue("soldier did not continue to the waypoint", runUntilReached(soldier, start));

			// an idle soldier directly walks to a waypoint
			soldier.moveTo(other, EMoveToType.WAYPOINT);
			assertTrue("idle soldier did not walk to the waypoint", runUntilReached(soldier, other));

			// a normal move order discards the waypoints
			soldier.moveTo(start, EMoveToType.DEFAULT);
			soldier.moveTo(other, EMoveToType.WAYPOINT);
			soldier.moveTo(start, EMoveToType.DEFAULT);
			assertTrue("soldier did not reach the new target", runUntilReached(soldier, start));
			runFor(10 * 1000);
			assertTrue("soldier did not discard its waypoints", soldier.getPosition().getOnGridDistTo(start) <= REACHED_DISTANCE);
		} finally {
			ReplayUtils.awaitShutdown(startedGame);
		}
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
