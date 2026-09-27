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
package jsettlers.common.menu;

import jsettlers.common.player.EWinState;

/**
 * An immutable snapshot of the state of one player of a running game, as it is shown in the players list.
 * <p>
 * The civilisation of the player is intentionally not contained.
 */
public final class InGamePlayerStatus {
	private final byte playerId;
	private final byte teamId;
	private final String name;
	private final boolean ai;
	private final boolean localPlayer;
	private final EWinState winState;
	private final EPlayerConnectionState connectionState;
	private final int pingMs;

	public InGamePlayerStatus(byte playerId, byte teamId, String name, boolean ai, boolean localPlayer, EWinState winState,
			EPlayerConnectionState connectionState, int pingMs) {
		this.playerId = playerId;
		this.teamId = teamId;
		this.name = name;
		this.ai = ai;
		this.localPlayer = localPlayer;
		this.winState = winState;
		this.connectionState = connectionState;
		this.pingMs = pingMs;
	}

	public byte getPlayerId() {
		return playerId;
	}

	public byte getTeamId() {
		return teamId;
	}

	/**
	 * @return The name of a human player in a multiplayer game or null if it is not known.
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

	public boolean isDefeated() {
		return winState == EWinState.LOST;
	}

	/**
	 * @return The network state of a human player in a multiplayer game or null for computer players and single player games.
	 */
	public EPlayerConnectionState getConnectionState() {
		return connectionState;
	}

	/**
	 * @return The round trip time between the player and the server in milliseconds or -1 if it is not known.
	 */
	public int getPingMs() {
		return pingMs;
	}

	@Override
	public String toString() {
		return "InGamePlayerStatus [playerId=" + playerId + ", teamId=" + teamId + ", name=" + name + ", ai=" + ai + ", localPlayer=" + localPlayer
				+ ", winState=" + winState + ", connectionState=" + connectionState + ", pingMs=" + pingMs + "]";
	}
}
