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
package jsettlers.main.android.gameplay.controlsmenu.settlers;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.player.SettlerStatistics;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.controls.DrawControls;
import jsettlers.main.android.core.events.DrawEvents;

/**
 * Provides the number of settlers of the player.
 */
public class StatisticsViewModel extends ViewModel {

	private final IInGamePlayer player;
	private final LiveData<SettlerStatistics> statistics;

	public StatisticsViewModel(DrawControls drawControls, IInGamePlayer player) {
		this.player = player;

		statistics = Transformations.map(new DrawEvents(drawControls), x -> createStatistics());
	}

	public LiveData<SettlerStatistics> getStatistics() {
		return statistics;
	}

	private SettlerStatistics createStatistics() {
		return new SettlerStatistics(player.getSettlerInformation(), player.getBedInformation().getTotalBedAmount());
	}

	/**
	 * ViewModel factory
	 */
	public static class Factory implements ViewModelProvider.Factory {
		private final ControlsResolver controlsResolver;

		public Factory(Activity activity) {
			this.controlsResolver = new ControlsResolver(activity);
		}

		@Override
		public <T extends ViewModel> T create(Class<T> modelClass) {
			if (modelClass == StatisticsViewModel.class) {
				return (T) new StatisticsViewModel(controlsResolver.getDrawControls(), controlsResolver.getPlayer());
			}
			throw new RuntimeException("StatisticsViewModel.Factory doesn't know how to create a: " + modelClass.toString());
		}
	}
}
