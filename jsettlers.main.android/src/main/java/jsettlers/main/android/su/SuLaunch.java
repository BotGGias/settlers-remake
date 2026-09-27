/*
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
 */

package jsettlers.main.android.su;

import android.content.Intent;
import android.os.Bundle;

/**
 * Settlers United launcher contract v1 (Launcher PROTOKOLLE §16.7): the launcher app starts JSettlers with action
 * {@link #ACTION} and string extras. Host: start a local server and open "new multiplayer game" with the lobby id as match
 * name. Join: connect to the launcher's tunnel on loopback, wait for the match with that name and join it. Player id and name
 * are only used for this session; the stored settings stay untouched.
 */
public final class SuLaunch {
	public static final String ACTION = "jsettlers.main.android.action.SU_LAUNCH";
	/** Contract version this build understands; also published as manifest meta-data {@code su.launcher.contract}. */
	public static final int CONTRACT_VERSION = 1;

	public static final String EXTRA_VERSION = "su.version";
	public static final String EXTRA_ROLE = "su.role";
	public static final String EXTRA_SERVER = "su.server";
	public static final String EXTRA_PORT = "su.port";
	public static final String EXTRA_PLAYER_ID = "su.playerId";
	public static final String EXTRA_PLAYER_NAME = "su.playerName";
	public static final String EXTRA_MATCH_NAME = "su.matchName";
	public static final String EXTRA_WAIT_SECS = "su.waitSecs";

	public static final int DEFAULT_WAIT_SECS = 30;
	private static final int MAX_WAIT_SECS = 600;
	private static final int MAX_TEXT = 128;

	public enum Role {
		HOST, JOIN
	}

	public final Role role;
	public final String server;
	public final int port;
	public final String playerId;
	public final String playerName;
	public final String matchName;
	public final int waitSecs;

	private SuLaunch(Role role, String server, int port, String playerId, String playerName, String matchName, int waitSecs) {
		this.role = role;
		this.server = server;
		this.port = port;
		this.playerId = playerId;
		this.playerName = playerName;
		this.matchName = matchName;
		this.waitSecs = waitSecs;
	}

	/** Server address for the multiplayer connector ({@code host:port}). */
	public String serverAddress() {
		return server + ":" + port;
	}

	/** Result of {@link #parse}: either a launch or an error text for the user (English, technical). */
	public static final class Result {
		public final SuLaunch launch;
		public final String error;

		private Result(SuLaunch launch, String error) {
			this.launch = launch;
			this.error = error;
		}
	}

	/** Is this intent meant for the launcher mode (action set)? Without it JSettlers behaves as before. */
	public static boolean isSuLaunch(Intent intent) {
		return intent != null && ACTION.equals(intent.getAction());
	}

	/** Reads and validates the extras. Values arrive as strings (the launcher shell sends all extras as strings). */
	public static Result parse(Bundle extras) {
		if (extras == null) {
			return error("no extras");
		}
		String version = text(extras, EXTRA_VERSION);
		if (!String.valueOf(CONTRACT_VERSION).equals(version)) {
			return error("unsupported su.version " + version);
		}
		Role role;
		String r = text(extras, EXTRA_ROLE);
		if ("host".equals(r)) {
			role = Role.HOST;
		} else if ("join".equals(r)) {
			role = Role.JOIN;
		} else {
			return error("invalid su.role " + r);
		}
		String server = text(extras, EXTRA_SERVER);
		if (!"127.0.0.1".equals(server) && !"localhost".equals(server)) {
			return error("su.server must be loopback, not " + server);
		}
		int port = number(extras, EXTRA_PORT, -1);
		if (port < 1 || port > 65535) {
			return error("invalid su.port");
		}
		String playerId = text(extras, EXTRA_PLAYER_ID);
		String playerName = text(extras, EXTRA_PLAYER_NAME);
		String matchName = text(extras, EXTRA_MATCH_NAME);
		if (empty(playerId) || empty(playerName) || empty(matchName)) {
			return error("su.playerId, su.playerName and su.matchName are required");
		}
		if (playerId.length() > MAX_TEXT || playerName.length() > MAX_TEXT || matchName.length() > MAX_TEXT) {
			return error("value too long");
		}
		int waitSecs = number(extras, EXTRA_WAIT_SECS, DEFAULT_WAIT_SECS);
		if (waitSecs < 1 || waitSecs > MAX_WAIT_SECS) {
			waitSecs = DEFAULT_WAIT_SECS;
		}
		return new Result(new SuLaunch(role, "127.0.0.1", port, playerId, playerName, matchName, waitSecs), null);
	}

	private static Result error(String message) {
		return new Result(null, message);
	}

	/** Extras may be strings (launcher shell) or numbers (adb {@code --ei}). */
	private static String text(Bundle extras, String key) {
		Object v = extras.get(key);
		return v == null ? null : v.toString().trim();
	}

	private static int number(Bundle extras, String key, int fallback) {
		String v = text(extras, key);
		if (v == null) {
			return fallback;
		}
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static boolean empty(String s) {
		return s == null || s.isEmpty();
	}
}
