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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jsettlers.common.CommonConstants;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.input.IGuiInputGrid;
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
 * Tests converting bearers into specialists and back without selecting them, as it is done by the settlers menu on Android.
 */
public class ConvertAtPositionIT {
	private static final byte PLAYER = 0;
	private static final byte OTHER_PLAYER = 1;

	static {
		CommonConstants.ENABLE_CONSOLE_LOGGING = true;
		Constants.FOG_OF_WAR_DEFAULT_ENABLED = false;

		TestUtils.setupTempResourceManager();
	}

	@Test
	public void testConvertBearersToSpecialistsAndBack() throws MapLoadException {
		MapLoader map = MapUtils.getMountainlake();
		PlayerSetting[] playerSettings = AiTestUtils.getDefaultPlayerSettings(map.getMaxPlayers());
		playerSettings[PLAYER] = new PlayerSetting(EPlayerType.HUMAN, ECivilisation.ROMAN, PLAYER);

		JSettlersGame.GameRunner game = AiTestUtils.createStartingGame(playerSettings, map);
		IStartedGame startedGame = ReplayUtils.waitForGameStartup(game);
		try {
			MatchConstants.clock().setPausing(true); // the test thread executes the game
			MatchConstants.clock().fastForwardTo(10 * 1000); // let the bearers register as jobless

			IGuiInputGrid grid = game.getMainGrid().getGuiInputGrid();
			int bearers = count(EMovableType.BEARER);
			assertTrue("not enough bearers on the map: " + bearers, bearers >= 10);
			ShortPoint2D position = MovableManager.getAllMovables().stream()
					.filter(movable -> movable.isAlive() && movable.getPlayer().getPlayerId() == PLAYER && movable.getMovableType() == EMovableType.BEARER)
					.findFirst().get().getPosition();

			grid.convertAtPosition(PLAYER, position, EMovableType.BEARER, EMovableType.PIONEER, 3);
			grid.convertAtPosition(PLAYER, position, EMovableType.BEARER, EMovableType.GEOLOGIST, 2);
			grid.convertAtPosition(PLAYER, position, EMovableType.BEARER, EMovableType.THIEF, 1);

			assertEquals(3, count(EMovableType.PIONEER));
			assertEquals(2, count(EMovableType.GEOLOGIST));
			assertEquals(1, count(EMovableType.THIEF));
			assertEquals(bearers - 6, count(EMovableType.BEARER));

			// the area at the position belongs to another player
			grid.convertAtPosition(OTHER_PLAYER, position, EMovableType.PIONEER, EMovableType.BEARER, Short.MAX_VALUE);
			assertEquals(3, count(EMovableType.PIONEER));

			grid.convertAtPosition(PLAYER, position, EMovableType.PIONEER, EMovableType.BEARER, Short.MAX_VALUE);
			grid.convertAtPosition(PLAYER, position, EMovableType.GEOLOGIST, EMovableType.BEARER, 1);
			grid.convertAtPosition(PLAYER, position, EMovableType.THIEF, EMovableType.BEARER, Short.MAX_VALUE);

			assertEquals(0, count(EMovableType.PIONEER));
			assertEquals(1, count(EMovableType.GEOLOGIST));
			assertEquals(0, count(EMovableType.THIEF));
			assertEquals(bearers - 1, count(EMovableType.BEARER));
		} finally {
			ReplayUtils.awaitShutdown(startedGame);
		}
	}

	private static int count(EMovableType type) {
		return (int) MovableManager.getAllMovables().stream()
				.filter(ILogicMovable::isAlive)
				.filter(movable -> movable.getPlayer().getPlayerId() == PLAYER && movable.getMovableType() == type)
				.count();
	}
}
