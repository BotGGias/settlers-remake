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
package jsettlers.graphics.localization;

import java.util.List;

import jsettlers.common.menu.EPlayerConnectionState;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.player.EWinState;

/**
 * Creates the texts shown for the players of a running game. They are used by the players list on all platforms and by the map messages.
 */
public final class PlayerStatusTexts {
	/**
	 * The status dot in front of a player or a status message.
	 */
	public static final String STATUS_DOT = "●";

	private PlayerStatusTexts() {
	}

	/**
	 * @return The name of the player: the name of a human player in a multiplayer game or a generic name otherwise.
	 */
	public static String getName(InGamePlayerStatus status) {
		if (status.getName() != null) {
			return status.getName();
		}
		return getGenericName(status.getPlayerId(), status.isAi());
	}

	/**
	 * @return The name of the given player of the game.
	 */
	public static String getName(IStartedGame game, byte playerId) {
		String name = game.getPlayerName(playerId);
		if (name != null) {
			return name;
		}

		List<InGamePlayerStatus> statuses = game.getPlayerStatuses();
		for (InGamePlayerStatus status : statuses) {
			if (status.getPlayerId() == playerId) {
				return getName(status);
			}
		}
		return getGenericName(playerId, false);
	}

	private static String getGenericName(byte playerId, boolean ai) {
		return Labels.getString(ai ? "players-computer-name" : "players-player-name", playerId + 1);
	}

	/**
	 * @return The name with a hint if it is the local player.
	 */
	public static String getNameWithHint(InGamePlayerStatus status) {
		String name = getName(status);
		return status.isLocalPlayer() ? name + " " + Labels.getString("players-you") : name;
	}

	public static String getTeam(InGamePlayerStatus status) {
		return Labels.getString("players-team", status.getTeamId() + 1);
	}

	/**
	 * @return The ping of the player or an empty string if it is not known.
	 */
	public static String getPing(InGamePlayerStatus status) {
		return status.getPingMs() >= 0 ? Labels.getString("players-ping", status.getPingMs()) : "";
	}

	/**
	 * @return A text describing the state of the player: active / defeated / won for all players and the network state of human players.
	 */
	public static String getStatus(InGamePlayerStatus status) {
		if (status.getWinState() == EWinState.LOST) {
			return Labels.getString("players-status-DEFEATED");
		} else if (status.getWinState() == EWinState.WON) {
			return Labels.getString("players-status-WON");
		}

		EPlayerConnectionState connectionState = status.getConnectionState();
		if (connectionState != null) {
			return Labels.getString("players-status-" + connectionState.name());
		}
		return Labels.getString("players-status-ACTIVE");
	}
}
