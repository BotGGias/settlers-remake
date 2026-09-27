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
package jsettlers.graphics.map.controls.original.panel.content;

import java.util.List;

import go.graphics.GLDrawContext;
import go.graphics.text.EFontSize;
import jsettlers.common.action.Action;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.InGamePlayerStatus;
import jsettlers.common.menu.PlayerStatusColors;
import jsettlers.graphics.action.ActionFireable;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.localization.PlayerStatusTexts;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.graphics.ui.Label;
import jsettlers.graphics.ui.Label.EHorizontalAlignment;
import jsettlers.graphics.ui.LabeledButton;
import jsettlers.graphics.ui.UIPanel;

/**
 * Shows all players of the game: their team, whether they are active or defeated and - for human players of multiplayer games - their network
 * state and ping. The civilisations of the players are not shown.
 */
public class PlayersContent extends AbstractContentProvider {
	private static final int MAX_ROWS = 8;
	private static final long UPDATE_INTERVAL_MS = 500;

	private static final float ROWS_TOP = .9f;
	private static final float ROWS_BOTTOM = .14f;
	private static final float ROW_HEIGHT = (ROWS_TOP - ROWS_BOTTOM) / MAX_ROWS;

	private final IStartedGame game;
	private final UIPanel panel;
	private final Label[] statusDots = new Label[MAX_ROWS];
	private final Label[] names = new Label[MAX_ROWS];
	private final Label[] details = new Label[MAX_ROWS];
	private long lastUpdate = 0;

	public PlayersContent(IStartedGame game, Action backAction) {
		this.game = game;
		this.panel = new UIPanel() {
			@Override
			public void drawAt(GLDrawContext gl) {
				updateIfNeeded();
				super.drawAt(gl);
			}
		};

		panel.addChild(new Label(Labels.getString("players-title"), EFontSize.HEADLINE), .05f, .92f, .95f, 1f);

		for (int i = 0; i < MAX_ROWS; i++) {
			float top = ROWS_TOP - i * ROW_HEIGHT;
			float middle = top - ROW_HEIGHT * .55f;
			float bottom = top - ROW_HEIGHT;

			statusDots[i] = new Label("", EFontSize.NORMAL, EHorizontalAlignment.CENTER);
			names[i] = new Label("", EFontSize.NORMAL, EHorizontalAlignment.LEFT);
			details[i] = new Label("", EFontSize.SMALL, EHorizontalAlignment.LEFT);

			panel.addChild(statusDots[i], .02f, middle, .12f, top);
			panel.addChild(names[i], .12f, middle, .98f, top);
			panel.addChild(details[i], .12f, bottom, .98f, middle);
		}

		panel.addChild(new LabeledButton(Labels.getString("players-back"), backAction), .1f, .02f, .9f, .11f);
	}

	private void updateIfNeeded() {
		long now = System.currentTimeMillis();
		if (now - lastUpdate < UPDATE_INTERVAL_MS) {
			return;
		}
		lastUpdate = now;

		List<InGamePlayerStatus> statuses = game.getPlayerStatuses();
		for (int i = 0; i < MAX_ROWS; i++) {
			if (i < statuses.size()) {
				InGamePlayerStatus status = statuses.get(i);
				statusDots[i].setText(PlayerStatusTexts.STATUS_DOT);
				statusDots[i].setTextColor(PlayerStatusColors.of(status));
				names[i].setText(PlayerStatusTexts.getNameWithHint(status));
				names[i].setTextColor(MapDrawContext.getPlayerColor(status.getPlayerId()));
				details[i].setText(getDetails(status));
			} else {
				statusDots[i].setText("");
				names[i].setText("");
				details[i].setText("");
			}
		}
	}

	private static String getDetails(InGamePlayerStatus status) {
		StringBuilder text = new StringBuilder(PlayerStatusTexts.getTeam(status));
		text.append(" - ").append(PlayerStatusTexts.getStatus(status));
		String ping = PlayerStatusTexts.getPing(status);
		if (!ping.isEmpty()) {
			text.append(" - ").append(ping);
		}
		return text.toString();
	}

	@Override
	public void contentShowing(ActionFireable actionFireable) {
		lastUpdate = 0;
	}

	@Override
	public UIPanel getPanel() {
		return panel;
	}

	@Override
	public ESecondaryTabType getTabs() {
		return null;
	}
}
