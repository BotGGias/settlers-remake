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

package jsettlers.main.android.gameplay.statistics;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.ScrollView;

import jsettlers.common.menu.IStartedGame;
import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.GameStatistics;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ControlsResolver;

/**
 * Shows the statistics of the running game. The values are recorded once every game minute.
 */
public class StatisticsDialog extends DialogFragment {
	private static final long UPDATE_INTERVAL_MS = 5000;
	private static final String KEY_SELECTED_TYPE = "selected_type";

	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			update();
			handler.postDelayed(this, UPDATE_INTERVAL_MS);
		}
	};

	private IStartedGame game;
	private GameStatisticsPanel statisticsPanel;

	public static StatisticsDialog create() {
		return new StatisticsDialog();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		game = new ControlsResolver(requireActivity().getApplication()).getGame();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		statisticsPanel = new GameStatisticsPanel(requireActivity());
		if (savedInstanceState != null && savedInstanceState.getSerializable(KEY_SELECTED_TYPE) instanceof EStatisticType) {
			statisticsPanel.setSelectedType((EStatisticType) savedInstanceState.getSerializable(KEY_SELECTED_TYPE));
		}
		ScrollView scrollView = new ScrollView(requireActivity());
		scrollView.addView(statisticsPanel);

		AlertDialog dialog = new AlertDialog.Builder(requireActivity(), R.style.GameMenuDialogTheme)
				.setTitle(R.string.statistics_title)
				.setView(scrollView)
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

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (statisticsPanel != null) {
			outState.putSerializable(KEY_SELECTED_TYPE, statisticsPanel.getSelectedType());
		}
	}

	private void update() {
		if (statisticsPanel != null && game != null) {
			statisticsPanel.setStatistics(GameStatistics.create(game));
		}
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
