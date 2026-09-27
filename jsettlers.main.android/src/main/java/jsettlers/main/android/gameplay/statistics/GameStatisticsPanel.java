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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jsettlers.common.player.EWinState;
import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.GameStatistics;
import jsettlers.common.statistics.GameStatistics.PlayerStatistics;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.main.android.R;

/**
 * Shows the statistics of a game: a tab for every {@link EStatisticType}, a chart of the selected value over the course of the game and a
 * ranking of the players by their last value.
 */
public class GameStatisticsPanel extends LinearLayout {
	private final TabLayout tabLayout;
	private final StatisticsChartView chartView;
	private final LinearLayout playersContainer;

	private GameStatistics statistics;
	private EStatisticType selectedType = EStatisticType.SETTLERS;

	public GameStatisticsPanel(Context context) {
		this(context, null);
	}

	public GameStatisticsPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		LayoutInflater.from(context).inflate(R.layout.view_game_statistics, this, true);

		tabLayout = findViewById(R.id.tab_layout_statistic_type);
		chartView = findViewById(R.id.chart_statistics);
		playersContainer = findViewById(R.id.players_container);

		for (EStatisticType type : EStatisticType.VALUES) {
			tabLayout.addTab(tabLayout.newTab().setText(Labels.getString(type.getLabelKey())).setTag(type));
		}
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				selectedType = (EStatisticType) tab.getTag();
				update();
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
		});
	}

	/**
	 * Shows the given statistics. The selected value is kept.
	 */
	public void setStatistics(GameStatistics statistics) {
		this.statistics = statistics;
		update();
	}

	public EStatisticType getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(EStatisticType type) {
		TabLayout.Tab tab = tabLayout.getTabAt(type.ordinal());
		if (tab != null) {
			tab.select();
		}
	}

	private void update() {
		chartView.setStatistics(statistics, selectedType);
		updatePlayers();
	}

	private void updatePlayers() {
		playersContainer.removeAllViews();
		if (statistics == null) {
			return;
		}

		EStatisticType type = selectedType;
		List<PlayerStatistics> players = new ArrayList<>(statistics.getPlayers());
		Collections.sort(players, Comparator.comparingInt((PlayerStatistics player) -> player.getSeries().getLastValue(type)).reversed()
				.thenComparingInt(PlayerStatistics::getPlayerId));

		LayoutInflater inflater = LayoutInflater.from(getContext());
		int rank = 0;
		int lastValue = Integer.MIN_VALUE;
		for (int i = 0; i < players.size(); i++) {
			PlayerStatistics player = players.get(i);
			int value = player.getSeries().getLastValue(type);
			if (value != lastValue) {
				rank = i + 1; // players with the same value share their rank
				lastValue = value;
			}

			View row = inflater.inflate(R.layout.vh_statistics_player, playersContainer, false);
			TextView rankView = row.findViewById(R.id.text_rank);
			TextView colorDot = row.findViewById(R.id.text_color_dot);
			TextView name = row.findViewById(R.id.text_name);
			TextView details = row.findViewById(R.id.text_details);
			TextView valueView = row.findViewById(R.id.text_value);

			rankView.setText(getContext().getString(R.string.statistics_rank, rank));
			colorDot.setTextColor(MapDrawContext.getPlayerColor(player.getPlayerId()).getARGB());
			name.setText(getName(player));
			name.setTypeface(null, player.isLocalPlayer() ? Typeface.BOLD : Typeface.NORMAL);
			details.setText(getDetails(player));
			valueView.setText(String.valueOf(value));

			playersContainer.addView(row);
		}
	}

	static String getName(PlayerStatistics player) {
		String name = player.getName();
		if (name == null) {
			name = Labels.getString(player.isAi() ? "players-computer-name" : "players-player-name", player.getPlayerId() + 1);
		}
		return player.isLocalPlayer() ? name + " " + Labels.getString("players-you") : name;
	}

	private static String getDetails(PlayerStatistics player) {
		String team = Labels.getString("players-team", player.getTeamId() + 1);
		String status;
		if (player.getWinState() == EWinState.WON) {
			status = Labels.getString("players-status-WON");
		} else if (player.getWinState() == EWinState.LOST) {
			status = Labels.getString("players-status-DEFEATED");
		} else {
			status = Labels.getString("players-status-ACTIVE");
		}
		return team + " - " + status;
	}
}
