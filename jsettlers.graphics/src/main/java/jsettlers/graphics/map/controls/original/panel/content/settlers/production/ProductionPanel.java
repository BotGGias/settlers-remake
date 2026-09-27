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
package jsettlers.graphics.map.controls.original.panel.content.settlers.production;

import go.graphics.text.EFontSize;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.player.IProductionStatistic;
import jsettlers.graphics.action.ActionFireable;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.controls.original.panel.content.AbstractContentProvider;
import jsettlers.graphics.map.controls.original.panel.content.ESecondaryTabType;
import jsettlers.graphics.map.controls.original.panel.content.updaters.UiContentUpdater.IUiContentReceiver;
import jsettlers.graphics.map.controls.original.panel.content.updaters.UiPlayerDependingContentUpdater;
import jsettlers.graphics.ui.Button;
import jsettlers.graphics.ui.Label;
import jsettlers.graphics.ui.UIPanel;
import jsettlers.graphics.ui.layout.ProductionStatisticLayout;

/**
 * This panel displays the amount of materials the player has produced since the start of the game.
 */
public class ProductionPanel extends AbstractContentProvider {

	/**
	 * This label displays the produced amount of a material.
	 */
	public static class ProducedCount extends Label implements IUiContentReceiver<IProductionStatistic> {

		private final EMaterialType material;
		private boolean plural;

		public ProducedCount(EMaterialType material) {
			super("", EFontSize.NORMAL);
			this.material = material;
		}

		@Override
		public String getDescription(float relativeX, float relativeY) {
			return Labels.getName(material, plural);
		}

		@Override
		public void update(IProductionStatistic productionStatistic) {
			int amount = productionStatistic != null ? productionStatistic.getAmountProduced(material) : 0;
			plural = amount != 1;
			setText(amount + "");
		}
	}

	/**
	 * This is a button that displays the icon of a material.
	 */
	public static class ProducedMaterialButton extends Button implements IUiContentReceiver<IProductionStatistic> {

		private final EMaterialType material;
		private boolean plural;

		public ProducedMaterialButton(EMaterialType material) {
			super(material.getIcon());
			this.material = material;
		}

		@Override
		public String getDescription(float relativeX, float relativeY) {
			return Labels.getName(material, plural);
		}

		@Override
		public void update(IProductionStatistic productionStatistic) {
			int amount = productionStatistic != null ? productionStatistic.getAmountProduced(material) : 0;
			plural = amount != 1;
		}
	}

	private final UIPanel panel;
	private final UiPlayerDependingContentUpdater<IProductionStatistic> uiContentUpdater = new UiPlayerDependingContentUpdater<>(
			IInGamePlayer::getProductionStatistic);

	public ProductionPanel() {
		panel = new ProductionStatisticLayout()._root;

		// noinspection unchecked
		panel.getChildren().stream()
				.filter(c -> c instanceof IUiContentReceiver)
				.map(c -> (IUiContentReceiver<IProductionStatistic>) c)
				.forEach(uiContentUpdater::addListener);
	}

	public void setPlayer(IInGamePlayer player) {
		uiContentUpdater.updatePlayer(player);
	}

	@Override
	public ESecondaryTabType getTabs() {
		return ESecondaryTabType.SETTLERS;
	}

	@Override
	public UIPanel getPanel() {
		return panel;
	}

	@Override
	public void contentShowing(ActionFireable actionFireable) {
		uiContentUpdater.start();
	}

	@Override
	public void contentHiding(ActionFireable actionFireable, AbstractContentProvider nextContent) {
		uiContentUpdater.stop();
	}
}
