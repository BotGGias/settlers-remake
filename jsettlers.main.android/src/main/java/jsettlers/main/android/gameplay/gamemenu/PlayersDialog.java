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
package jsettlers.main.android.gameplay.gamemenu;

import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.menu.PlayerStatusColors;
import jsettlers.graphics.localization.PlayerStatusTexts;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ControlsResolver;

/**
 * Shows the players of the running game grouped by team: whether they are active or defeated and - for human players of multiplayer games -
 * their connection state and ping. The civilisations of the players are not shown.
 */
public class PlayersDialog extends DialogFragment {
	private static final long UPDATE_INTERVAL_MS = 1000;

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			update();
			handler.postDelayed(this, UPDATE_INTERVAL_MS);
		}
	};

	private IStartedGame game;
	private LinearLayout playersContainer;

	public static PlayersDialog create() {
		return new PlayersDialog();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		game = new ControlsResolver(requireActivity().getApplication()).getGame();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_players, null);
		playersContainer = view.findViewById(R.id.players_container);

		AlertDialog dialog = new AlertDialog.Builder(requireActivity(), R.style.GameMenuDialogTheme)
				.setTitle(R.string.players_title)
				.setView(view)
				.setPositiveButton(R.string.players_close, null)
				.create();

		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		applyFullscreenWorkaround(dialog);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		handler.post(updateRunnable);
	}

	@Override
	public void onStop() {
		super.onStop();
		handler.removeCallbacks(updateRunnable);
	}

	private void update() {
		if (playersContainer == null) {
			return;
		}

		List<InGamePlayerStatus> statuses = game != null ? game.getPlayerStatuses() : Collections.emptyList();
		LayoutInflater inflater = LayoutInflater.from(playersContainer.getContext());
		playersContainer.removeAllViews();

		int currentTeam = -1;
		for (InGamePlayerStatus status : statuses) { // the statuses are sorted by team
			if (status.getTeamId() != currentTeam) {
				currentTeam = status.getTeamId();
				TextView teamView = (TextView) inflater.inflate(R.layout.vh_players_team, playersContainer, false);
				teamView.setText(getString(R.string.players_team, currentTeam + 1));
				playersContainer.addView(teamView);
			}

			View playerView = inflater.inflate(R.layout.vh_player, playersContainer, false);
			TextView statusDot = playerView.findViewById(R.id.text_status_dot);
			TextView name = playerView.findViewById(R.id.text_name);
			TextView details = playerView.findViewById(R.id.text_details);

			statusDot.setTextColor(PlayerStatusColors.of(status).getARGB());
			name.setText(PlayerStatusTexts.getNameWithHint(status));
			name.setTextColor(MapDrawContext.getPlayerColor(status.getPlayerId()).getARGB());
			details.setText(getDetails(status));

			playersContainer.addView(playerView);
		}
	}

	private static String getDetails(InGamePlayerStatus status) {
		String text = PlayerStatusTexts.getStatus(status);
		String ping = PlayerStatusTexts.getPing(status);
		return ping.isEmpty() ? text : text + " - " + ping;
	}

	/**
	 * Stops the system bars from showing when this dialog appears.
	 */
	private void applyFullscreenWorkaround(AlertDialog dialog) {
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		dialog.setOnShowListener(x -> dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE));
		int activitySystemUiVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
		dialog.getWindow().getDecorView().setSystemUiVisibility(activitySystemUiVisibility);
	}
}
