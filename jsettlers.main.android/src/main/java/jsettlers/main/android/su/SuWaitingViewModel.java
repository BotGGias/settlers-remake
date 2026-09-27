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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import jsettlers.common.menu.EProgressState;
import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IJoinableGame;
import jsettlers.common.menu.IJoiningGame;
import jsettlers.common.menu.IJoiningGameListener;
import jsettlers.common.menu.IMapDefinition;
import jsettlers.common.menu.IMultiplayerConnector;
import jsettlers.common.menu.IOpenMultiplayerGameInfo;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.utils.collections.ChangingList;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.main.android.R;
import jsettlers.main.android.core.GameStarter;
import jsettlers.main.android.core.events.SingleLiveEvent;
import jsettlers.main.su.SuMatchDirector;

/**
 * Launcher start contract v2: prepares the map, opens (host) or joins (member) the match and lets {@link SuMatchDirector} set it up
 * and start it. The user only sees a waiting screen; on failure or timeout the fragment returns to the main menu.
 */
public class SuWaitingViewModel extends ViewModel implements IJoiningGameListener, SuMatchDirector.Listener {
	private static final String TAG = "SuWaiting";
	private static final long MAX_MAP_BYTES = 32L * 1024 * 1024;

	private final Context context;
	private final GameStarter gameStarter;
	private final SuLaunch launch;
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private final Runnable timeout;

	private final MutableLiveData<String> status = new MutableLiveData<>();
	private final SingleLiveEvent<Boolean> startedEvent = new SingleLiveEvent<>();
	private final SingleLiveEvent<String> failedEvent = new SingleLiveEvent<>();

	private boolean done;
	private boolean joinRequested;
	private String mapName = "";
	private IJoiningGame joiningGame;
	private IJoinPhaseMultiplayerGameConnector connector;
	private SuMatchDirector director;

	SuWaitingViewModel(Context context, GameStarter gameStarter, SuLaunch launch) {
		this.context = context;
		this.gameStarter = gameStarter;
		this.launch = launch;
		this.timeout = () -> fail(context.getString(R.string.su_start_timeout, launch.startTimeoutSecs));

		status.setValue(context.getString(R.string.su_wait_preparing));
		mainHandler.postDelayed(timeout, launch.startTimeoutSecs * 1000L);
		new Thread(this::prepare, "su-prepare").start();
	}

	public LiveData<String> getStatus() {
		return status;
	}

	/** The match starts: show the game (loading screen). */
	public LiveData<Boolean> getStartedEvent() {
		return startedEvent;
	}

	/** Message for the user; back to the main menu. */
	public LiveData<String> getFailedEvent() {
		return failedEvent;
	}

	public void cancel() {
		fail(null);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		mainHandler.removeCallbacks(timeout);
		boolean abort;
		synchronized (this) {
			abort = !done;
			done = true;
		}
		if (abort) {
			cleanup();
		}
	}

	/** Background: map from the launcher into the own maps folder, then look it up. */
	private void prepare() {
		try {
			MapLoader map = resolveMap();
			if (map == null) {
				fail(context.getString(R.string.su_map_missing, launch.mapFile != null ? launch.mapFile : launch.mapId));
				return;
			}
			String problem = launch.slots.checkMap(map.getMaxPlayers());
			if (problem != null) {
				fail(context.getString(R.string.su_start_failed, problem));
				return;
			}
			mapName = map.getMapName();
			mainHandler.post(() -> connect(map));
		} catch (IOException | RuntimeException e) {
			Log.w(TAG, "prepare", e);
			fail(context.getString(R.string.su_start_failed, e.getMessage()));
		}
	}

	private MapLoader resolveMap() throws IOException {
		if (launch.mapUri == null) {
			return MapList.getDefaultList().getMapById(launch.mapId);
		}
		File dir = new File(context.getExternalFilesDir(null), "maps");
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("cannot create " + dir);
		}
		File target = new File(dir, launch.mapFile);
		File part = new File(dir, "." + launch.mapFile + ".part");
		try (InputStream in = context.getContentResolver().openInputStream(Uri.parse(launch.mapUri)); OutputStream out = new FileOutputStream(part)) {
			if (in == null) {
				throw new IOException("cannot open map uri");
			}
			byte[] buffer = new byte[64 * 1024];
			long total = 0;
			int n;
			while ((n = in.read(buffer)) > 0) {
				total += n;
				if (total > MAX_MAP_BYTES) {
					throw new IOException("map file too large");
				}
				out.write(buffer, 0, n);
			}
		} catch (IOException | RuntimeException e) {
			part.delete();
			throw e;
		}
		if (!part.renameTo(target)) {
			target.delete();
			if (!part.renameTo(target)) {
				part.delete();
				throw new IOException("cannot write " + target.getName());
			}
		}
		gameStarter.refreshMapList();

		// the id of original maps is checksum + file name, so the launcher refers to the file by name
		MapLoader found = null;
		for (MapLoader map : MapList.getDefaultList().getFreshMaps().getItems()) {
			if (launch.mapFile.equals(map.getListedMap().getFileName())) {
				if (found != null && !found.getMapId().equals(map.getMapId())) {
					throw new IOException("map file name " + launch.mapFile + " is ambiguous");
				}
				found = map;
			}
		}
		return found;
	}

	/** Main thread. */
	private void connect(MapLoader map) {
		synchronized (this) {
			if (done) {
				return;
			}
		}
		progress(0, 0, launch.slots.getHumanCount());
		IMultiplayerConnector multiplayerConnector = gameStarter.getMultiPlayerConnector();
		if (launch.role == SuLaunch.Role.HOST) {
			IJoiningGame joining = multiplayerConnector.openNewMultiplayerGame(new IOpenMultiplayerGameInfo() {
				@Override
				public String getMatchName() {
					return launch.matchName;
				}

				@Override
				public IMapDefinition getMapDefinition() {
					return map;
				}

				@Override
				public int getMaxPlayers() {
					return map.getMaxPlayers();
				}
			});
			setJoining(joining);
		} else {
			ChangingList<IJoinableGame> games = multiplayerConnector.getJoinableMultiplayerGames();
			games.setListener(list -> checkJoin(list.getItems(), map));
			checkJoin(games.getItems(), map);
		}
	}

	/** Member: join as soon as the host's match shows up. Any thread. */
	private void checkJoin(List<? extends IJoinableGame> games, MapLoader map) {
		for (IJoinableGame game : games) {
			if (!launch.matchName.equals(game.getName())) {
				continue;
			}
			synchronized (this) {
				if (done || joinRequested) {
					return;
				}
				if (game.getMap() == null || !map.getMapId().equals(game.getMap().getMapId())) {
					fail(context.getString(R.string.su_map_missing, mapName));
					return;
				}
				joinRequested = true;
			}
			mainHandler.post(() -> {
				synchronized (this) {
					if (done) {
						return;
					}
				}
				setJoining(gameStarter.getMultiPlayerConnector().joinMultiplayerGame(game));
			});
			return;
		}
	}

	private void setJoining(IJoiningGame joining) {
		synchronized (this) {
			joiningGame = joining;
		}
		gameStarter.setJoiningGame(joining);
		joining.setListener(this);
	}

	/**
	 * IJoiningGameListener implementation
	 */
	@Override
	public void joinProgressChanged(EProgressState state, float progress) {
	}

	@Override
	public void gameJoined(IJoinPhaseMultiplayerGameConnector joined) {
		SuMatchDirector d;
		synchronized (this) {
			if (done || connector != null) {
				return;
			}
			connector = joined;
			d = director = new SuMatchDirector(joined, launch.slots, launch.role == SuLaunch.Role.HOST, this);
		}
		gameStarter.setJoiningGame(null);
		gameStarter.setJoinPhaseMultiPlayerConnector(joined);
		d.start();
	}

	/**
	 * SuMatchDirector.Listener implementation (network thread)
	 */
	@Override
	public void progress(int joined, int ready, int total) {
		status.postValue(context.getString(R.string.su_wait_players, mapName, joined, total, ready));
	}

	@Override
	public void starting(IStartingGame game) {
		synchronized (this) {
			if (done) {
				return;
			}
			done = true;
		}
		mainHandler.removeCallbacks(timeout);
		gameStarter.setJoinPhaseMultiPlayerConnector(null);
		gameStarter.setStartingGame(game);
		startedEvent.postValue(true);
	}

	@Override
	public void failed(String reason) {
		fail(context.getString(R.string.su_start_failed, reason));
	}

	/** Ends the attempt; message null = cancelled by the user. Any thread. */
	private void fail(String message) {
		synchronized (this) {
			if (done) {
				return;
			}
			done = true;
		}
		Log.w(TAG, "launcher start failed: " + message);
		mainHandler.removeCallbacks(timeout);
		mainHandler.post(this::cleanup);
		failedEvent.postValue(message);
	}

	/** Leaves the match and closes the connection. */
	private void cleanup() {
		SuMatchDirector d;
		IJoinPhaseMultiplayerGameConnector c;
		IJoiningGame j;
		synchronized (this) {
			d = director;
			c = connector;
			j = joiningGame;
		}
		try {
			if (d != null) {
				d.stop();
			}
			if (c != null) {
				c.abort();
			} else if (j != null) {
				j.abort();
			}
		} catch (RuntimeException e) { // not connected yet
			Log.w(TAG, "abort", e);
		}
		gameStarter.setJoiningGame(null);
		gameStarter.setJoinPhaseMultiPlayerConnector(null);
		gameStarter.closeMultiPlayerConnector();
	}

	public static class Factory implements ViewModelProvider.Factory {
		private final Activity activity;
		private final SuLaunch launch;

		public Factory(Activity activity, SuLaunch launch) {
			this.activity = activity;
			this.launch = launch;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ViewModel> T create(Class<T> modelClass) {
			if (modelClass == SuWaitingViewModel.class) {
				return (T) new SuWaitingViewModel(activity.getApplicationContext(), (GameStarter) activity.getApplication(), launch);
			}
			throw new RuntimeException("SuWaitingViewModel.Factory doesn't know how to create a: " + modelClass);
		}
	}
}
