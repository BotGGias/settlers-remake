/*
 * Copyright (c) 2017
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

package jsettlers.main.android.core;

import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IJoiningGame;
import jsettlers.common.menu.IMapInterfaceConnector;
import jsettlers.common.menu.IMultiplayerConnector;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.IStartingGame;
import jsettlers.main.android.su.SuLaunch;
import jsettlers.main.android.su.SuSession;
import jsettlers.logic.map.loading.list.MapList;

/**
 * Created by tompr on 21/01/2017.
 */
public interface GameStarter {
	MapList getMapList();

	IMultiplayerConnector getMultiPlayerConnector();

	void closeMultiPlayerConnector();

	IStartingGame getStartingGame();

	void setStartingGame(IStartingGame startingGame);

	IJoiningGame getJoiningGame();

	void setJoiningGame(IJoiningGame joiningGame);

	IJoinPhaseMultiplayerGameConnector getJoinPhaseMultiplayerConnector();

	void setJoinPhaseMultiPlayerConnector(IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector);

	IMapInterfaceConnector gameStarted(IStartedGame game);

	void toggleServer();

	boolean isServerRunning();

	/** Player id for multiplayer: from the launcher session if any, otherwise from the settings. */
	String getPlayerId();

	/** Name of a new multiplayer match: the lobby id in a launcher session, otherwise the player name. */
	String getNewMatchName();

	/** Rescans the map folders (e.g. after a map file was added). Blocking. */
	void refreshMapList();

	/** Current launcher session (Settlers United), or null. */
	SuSession getSuSession();

	/**
	 * Starts a launcher session (replacing an old one). Host: starts a loopback server unless a server is already running.
	 * 
	 * @return error text for the user, or null
	 */
	String startSuSession(SuLaunch launch);

	/** Ends the launcher session and shuts down its server; nothing happens without a session. */
	void endSuSession();
}
