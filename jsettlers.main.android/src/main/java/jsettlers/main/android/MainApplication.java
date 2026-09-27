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

package jsettlers.main.android;

import jsettlers.network.infrastructure.log.ConsoleLogger;
import org.androidannotations.annotations.EApplication;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.support.multidex.MultiDexApplication;

import java.io.IOException;
import java.net.InetAddress;

import android.os.SystemClock;

import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IJoiningGame;
import jsettlers.common.menu.IMapInterfaceConnector;
import jsettlers.common.menu.IMultiplayerConnector;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.IStartingGame;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.main.MultiplayerConnector;
import jsettlers.main.android.core.AndroidPreferences;
import jsettlers.main.android.core.GameManager;
import jsettlers.main.android.core.GameService_;
import jsettlers.main.android.core.GameStarter;
import jsettlers.main.android.core.controls.ControlsAdapter;
import jsettlers.main.android.core.controls.GameMenu;
import jsettlers.main.android.core.resources.scanner.AndroidResourcesLoader;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.server.GameServerThread;
import jsettlers.main.android.su.SuLaunch;
import jsettlers.main.android.su.SuSession;

@EApplication
public class MainApplication extends MultiDexApplication implements GameStarter, GameManager {
	static { // configure game to be better usable on Android
		Constants.BUILDING_PLACEMENT_MAX_SEARCH_RADIUS = 10;
	}

	private MapList mapList;
	private IMultiplayerConnector multiplayerConnector;
	private IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector;
	private IStartingGame startingGame;
	private IJoiningGame joiningGame;

	private ControlsAdapter controlsAdapter;

	@Override
	public void onCreate() {
		super.onCreate();
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
		new AndroidResourcesLoader(this).setup();
	}

	/**
	 * GameStarter implementation
	 */
	@Override
	public MapList getMapList() {
		if (mapList == null) {
			mapList = MapList.getDefaultList();
		}
		return mapList;
	}

	private GameServerThread serverHandle = null;

	@Override
	public void toggleServer() {
		closeMultiPlayerConnector();
		if(serverHandle == null) {
			try {
				serverHandle = new GameServerThread(true);
				serverHandle.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			serverHandle.shutdown();
			serverHandle = null;
		}
	}

	@Override
	public boolean isServerRunning() {
		return serverHandle!=null;
	}

	@Override
	public IMultiplayerConnector getMultiPlayerConnector() {
		if (multiplayerConnector == null) {
			AndroidPreferences androidPreferences = new AndroidPreferences(this);
			String server = androidPreferences.getServer();
			String playerId = androidPreferences.getPlayerId();
			String playerName = androidPreferences.getPlayerName();
			if(serverHandle != null) server = "localhost";
			SuSession session = suSession;
			if (session != null) { // launcher: tunnel or own loopback server, session identity; the settings stay untouched
				server = session.launch.serverAddress();
				playerId = session.launch.playerId;
				playerName = session.launch.playerName;
			}
			MultiplayerConnector connector = new MultiplayerConnector(server, playerId, playerName, new ConsoleLogger("jsettlers.main.android-network"));
			if (session != null) {
				connector.setStartResources(session.launch.startResources); // v2: same value for everyone, from the launcher lobby
			}
			multiplayerConnector = connector;
		}
		return multiplayerConnector;
	}

	/**
	 * Settlers United launcher session (contract {@link SuLaunch}).
	 */
	private volatile SuSession suSession;
	/** Loopback server started for a launcher host session (not the LAN server of the main menu). */
	private GameServerThread suServer;
	/** App to return to after a launcher game (outlives the session: it ends with the game, the quit dialog asks afterwards). */
	private volatile String suReturnPackage;

	@Override
	public String takeSuReturnPackage() {
		String pkg = suReturnPackage;
		suReturnPackage = null;
		return pkg;
	}

	@Override
	public SuSession getSuSession() {
		return suSession;
	}

	@Override
	public synchronized String startSuSession(SuLaunch launch) {
		endSuSession();
		closeMultiPlayerConnector();
		if (launch.role == SuLaunch.Role.HOST && serverHandle == null) {
			try {
				suServer = new GameServerThread(false, InetAddress.getByName("127.0.0.1"), launch.port, new ConsoleLogger("jsettlers.main.android-su-server"));
				suServer.start();
			} catch (IOException e) {
				suServer = null;
				return e.getMessage();
			}
		}
		suSession = new SuSession(launch, SystemClock.uptimeMillis());
		suReturnPackage = launch.returnPackage;
		return null;
	}

	@Override
	public synchronized void endSuSession() {
		if (suSession == null && suServer == null) {
			return;
		}
		suSession = null;
		if (suServer != null) {
			closeMultiPlayerConnector();
			suServer.shutdown();
			suServer = null;
		}
	}

	@Override
	public String getPlayerId() {
		SuSession session = suSession;
		return session != null ? session.launch.playerId : new AndroidPreferences(this).getPlayerId();
	}

	@Override
	public String getNewMatchName() {
		SuSession session = suSession;
		return session != null ? session.launch.matchName : new AndroidPreferences(this).getPlayerName();
	}

	@Override
	public void refreshMapList() {
		new AndroidResourcesLoader(this).setup(); // new map list factory: rescans the map folders
		mapList = null;
	}

	@Override
	public void closeMultiPlayerConnector() {
		if (multiplayerConnector != null) {
			((IClientConnection)multiplayerConnector).action(IClientConnection.EClientAction.CLOSE, null);
			multiplayerConnector = null;
		}
	}

	@Override
	public IStartingGame getStartingGame() {
		return startingGame;
	}

	@Override
	public void setStartingGame(IStartingGame startingGame) {
		if (startingGame != null && suSession == null) {
			suReturnPackage = null; // a game started in JSettlers itself ends in the main menu
		}
		this.startingGame = startingGame;
	}

	@Override
	public IJoiningGame getJoiningGame() {
		return joiningGame;
	}

	@Override
	public void setJoiningGame(IJoiningGame joiningGame) {
		this.joiningGame = joiningGame;
	}

	@Override
	public IJoinPhaseMultiplayerGameConnector getJoinPhaseMultiplayerConnector() {
		return joinPhaseMultiplayerGameConnector;
	}

	@Override
	public void setJoinPhaseMultiPlayerConnector(IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector) {
		this.joinPhaseMultiplayerGameConnector = joinPhaseMultiplayerGameConnector;
	}

	@Override
	public IMapInterfaceConnector gameStarted(IStartedGame game) {
		controlsAdapter = new ControlsAdapter(getApplicationContext(), game, MatchConstants.clock());
		game.setGameExitListener(controlsAdapter.getGameMenu());
		controlsAdapter.getGameMenu().getGameState().observeForever(gameStateObserver);

		GameService_.intent(this).start();

		return controlsAdapter.getMapContent().getInterfaceConnector();
	}

	/**
	 * GameManager implementation
	 */
	@Override
	public ControlsAdapter getControlsAdapter() {
		return controlsAdapter;
	}

	@Override
	public GameMenu getGameMenu() {
		if (controlsAdapter == null) {
			return null;
		}

		return controlsAdapter.getGameMenu();
	}

	@Override
	public boolean isGameInProgress() {
		return controlsAdapter != null;
	}

	private Observer<GameMenu.GameState> gameStateObserver = gameState -> {
		if (gameState == GameMenu.GameState.QUITTED) {
			controlsAdapter.getGameMenu().getGameState().removeObserver(this.gameStateObserver);

			controlsAdapter = null;
			startingGame = null;
			joiningGame = null;
			joinPhaseMultiplayerGameConnector = null;
			mapList = null; // Nulling this means that any new saved games will be available next time mapList is set

			closeMultiPlayerConnector();
			endSuSession();
		}
	};
}
