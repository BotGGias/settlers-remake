/*
 * Copyright (c) 2015 - 2017
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
package jsettlers.graphics.map.controls.original.panel.content.settlers.statistics;

import go.graphics.text.EFontSize;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.player.ISettlerInformation;
import jsettlers.common.player.SettlerStatistics;
import jsettlers.graphics.action.ActionFireable;
import jsettlers.graphics.map.controls.original.panel.content.AbstractContentProvider;
import jsettlers.graphics.map.controls.original.panel.content.ESecondaryTabType;
import jsettlers.graphics.map.controls.original.panel.content.updaters.UiContentUpdater.IUiContentReceiver;
import jsettlers.graphics.map.controls.original.panel.content.updaters.UiPlayerDependingContentUpdater;
import jsettlers.graphics.ui.Label;
import jsettlers.graphics.ui.UIElement;
import jsettlers.graphics.ui.UIPanel;
import jsettlers.graphics.ui.layout.StatisticLayout;

/**
 * The ingame settler statistics panel
 *
 * @author codingberlin
 * @author nptr
 * @author Andreas Eberle
 */
public class SettlersStatisticsPanel extends AbstractContentProvider implements IUiContentReceiver<ISettlerInformation> {

	private UIPanel panel;
	private final UiPlayerDependingContentUpdater<ISettlerInformation> uiContentUpdater = new UiPlayerDependingContentUpdater<>(IInGamePlayer::getSettlerInformation);
	private IInGamePlayer player;

	public SettlersStatisticsPanel() {
		uiContentUpdater.addListener(this);
	}

	public void setPlayer(IInGamePlayer player) {
		panel = new StatisticLayout(null, player.getCivilisation())._root;
		this.player = player;
		uiContentUpdater.updatePlayer(player);
	}

	@Override
	public UIPanel getPanel() {
		return panel;
	}

	@Override
	public ESecondaryTabType getTabs() {
		return ESecondaryTabType.SETTLERS;
	}

	@Override
	public void update(ISettlerInformation settlerInformation) {
		SettlerStatistics statistics = new SettlerStatistics(settlerInformation, player.getBedInformation().getTotalBedAmount());

		for (UIElement element : panel.getChildren()) {
			if (element instanceof NamedLabel) {
				NamedLabel label = (NamedLabel) element;
				Integer value = getValue(statistics, label.getName());
				if (value != null) {
					label.setText(String.valueOf(value));
				}
			}
		}
	}

	private static Integer getValue(SettlerStatistics statistics, String name) {
		switch (name) {
		case "stat_beds":
			return statistics.getBeds();
		case "stat_civilian":
			return statistics.getCivilians();
		case "stat_total":
			return statistics.getTotal();
		case "stat_soldier":
			return statistics.getSoldiers();
		case "stat_bearer":
			return statistics.getBearers();
		case "stat_digger":
			return statistics.getDiggers();
		case "stat_builder":
			return statistics.getBricklayers();
		case "stat_other":
			return statistics.getWorkers();
		case "stat_swordsman":
			return statistics.getSwordsmen();
		case "stat_bowman":
			return statistics.getBowmen();
		case "stat_pikeman":
			return statistics.getPikemen();
		case "stat_mage":
			return statistics.getMages();
		case "stat_geo":
			return statistics.getGeologists();
		case "stat_thief":
			return statistics.getThieves();
		case "stat_pioneer":
			return statistics.getPioneers();
		case "stat_animals":
			return statistics.getDonkeys();
		default:
			return null;
		}
	}

	@Override
	public void contentShowing(ActionFireable actionFireable) {
		uiContentUpdater.start();
	}

	@Override
	public void contentHiding(ActionFireable actionFireable, AbstractContentProvider nextContent) {
		uiContentUpdater.stop();
	}

	public static class NamedLabel extends Label {
		private String name;

		public NamedLabel(String name) {
			super("0", EFontSize.NORMAL);
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}
}
