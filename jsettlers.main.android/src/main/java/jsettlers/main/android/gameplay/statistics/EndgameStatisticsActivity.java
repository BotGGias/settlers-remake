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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import jsettlers.common.player.EWinState;
import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.GameStatistics;
import jsettlers.common.statistics.GameStatistics.PlayerStatistics;
import jsettlers.graphics.localization.Labels;
import jsettlers.main.android.R;
import jsettlers.main.android.core.GameManager;
import jsettlers.main.android.mainmenu.MainActivity_;

/**
 * Shown after a game has been quit: the result of the local player and the statistics of all players. Continues to the main menu.
 */
@EActivity(R.layout.activity_endgame_statistics)
public class EndgameStatisticsActivity extends AppCompatActivity {
	private static final String KEY_SELECTED_TYPE = "selected_type";
	private static final int COLOR_WON = Color.rgb(0x66, 0xbb, 0x6a);
	private static final int COLOR_LOST = Color.rgb(0xef, 0x53, 0x50);

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@ViewById(R.id.text_result)
	TextView resultText;

	@ViewById(R.id.progress_loading)
	ProgressBar loadingProgress;

	@ViewById(R.id.panel_statistics)
	GameStatisticsPanel statisticsPanel;

	private GameManager gameManager;
	private EStatisticType restoredType;
	private ColorStateList defaultResultColors;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gameManager = (GameManager) getApplication();
		if (savedInstanceState != null && savedInstanceState.getSerializable(KEY_SELECTED_TYPE) instanceof EStatisticType) {
			restoredType = (EStatisticType) savedInstanceState.getSerializable(KEY_SELECTED_TYPE);
		}
	}

	@AfterViews
	void setupViews() {
		setSupportActionBar(toolbar);
		setTitle(R.string.statistics_endgame_title);
		defaultResultColors = resultText.getTextColors();

		if (restoredType != null) {
			statisticsPanel.setSelectedType(restoredType);
		}
		gameManager.getEndgameStatistics().observe(this, this::showStatistics);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (statisticsPanel != null) {
			outState.putSerializable(KEY_SELECTED_TYPE, statisticsPanel.getSelectedType());
		}
	}

	private void showStatistics(GameStatistics statistics) {
		// while the game is shutting down, the statistics are not available yet
		boolean loading = statistics == null && gameManager.isGameInProgress();
		loadingProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
		statisticsPanel.setVisibility(loading ? View.GONE : View.VISIBLE);
		statisticsPanel.setStatistics(statistics);
		showResult(statistics);
	}

	private void showResult(GameStatistics statistics) {
		PlayerStatistics localPlayer = statistics != null ? statistics.getLocalPlayer() : null;
		if (localPlayer != null && localPlayer.getWinState() == EWinState.WON) {
			resultText.setText(Labels.getString("winstate_WON"));
			resultText.setTextColor(COLOR_WON);
		} else if (localPlayer != null && localPlayer.getWinState() == EWinState.LOST) {
			resultText.setText(Labels.getString("winstate_LOST"));
			resultText.setTextColor(COLOR_LOST);
		} else if (statistics != null && statistics.hasSamples()) {
			resultText.setText(getString(R.string.statistics_game_ended, StatisticsChartView.formatTime(statistics.getLastTime())));
			resultText.setTextColor(defaultResultColors);
		} else {
			resultText.setText(R.string.statistics_game_ended_no_time);
			resultText.setTextColor(defaultResultColors);
		}
	}

	@Click(R.id.button_continue)
	void continueClicked() {
		showMainMenu();
	}

	@Override
	public void onBackPressed() {
		showMainMenu();
	}

	private void showMainMenu() {
		gameManager.clearEndgameStatistics();
		MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
		finish();
	}
}
