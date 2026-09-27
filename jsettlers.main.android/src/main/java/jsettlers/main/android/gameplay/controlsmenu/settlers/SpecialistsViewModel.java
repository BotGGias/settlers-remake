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
package jsettlers.main.android.gameplay.controlsmenu.settlers;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import jsettlers.common.action.ConvertAtPositionAction;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.player.ISettlerInformation;
import jsettlers.main.android.core.controls.ActionControls;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.controls.DrawControls;
import jsettlers.main.android.core.controls.PositionControls;
import jsettlers.main.android.core.events.DrawEvents;

/**
 * Converts bearers of the area on the screen into specialists (pioneers, geologists, thieves) and back.
 */
public class SpecialistsViewModel extends ViewModel {

	/**
	 * Snapshot of the numbers shown in the menu.
	 */
	public static class SpecialistsState {
		public final int carriers;
		public final int pioneers;
		public final int geologists;
		public final int thieves;
		public final boolean convertingPossible;

		SpecialistsState(int carriers, int pioneers, int geologists, int thieves, boolean convertingPossible) {
			this.carriers = carriers;
			this.pioneers = pioneers;
			this.geologists = geologists;
			this.thieves = thieves;
			this.convertingPossible = convertingPossible;
		}
	}

	private final ActionControls actionControls;
	private final PositionControls positionControls;
	private final IInGamePlayer player;

	private final LiveData<SpecialistsState> state;

	public SpecialistsViewModel(ActionControls actionControls, PositionControls positionControls, DrawControls drawControls, IInGamePlayer player) {
		this.actionControls = actionControls;
		this.positionControls = positionControls;
		this.player = player;

		state = Transformations.map(new DrawEvents(drawControls), x -> createState());
	}

	public LiveData<SpecialistsState> getState() {
		return state;
	}

	/**
	 * Converts bearers into the given specialist.
	 *
	 * @param amount
	 *            The number of bearers to convert or {@link Short#MAX_VALUE} for all.
	 */
	public void createSpecialists(EMovableType specialistType, short amount) {
		fireConvertAction(EMovableType.BEARER, specialistType, amount);
	}

	/**
	 * Converts the given specialists back into bearers.
	 *
	 * @param amount
	 *            The number of specialists to convert or {@link Short#MAX_VALUE} for all.
	 */
	public void removeSpecialists(EMovableType specialistType, short amount) {
		fireConvertAction(specialistType, EMovableType.BEARER, amount);
	}

	private void fireConvertAction(EMovableType sourceType, EMovableType targetType, short amount) {
		if (positionControls.isInPlayerPartition()) {
			actionControls.fireAction(new ConvertAtPositionAction(sourceType, targetType, amount, positionControls.getCurrentPosition()));
		}
	}

	private SpecialistsState createState() {
		ISettlerInformation settlerInformation = player.getSettlerInformation();
		return new SpecialistsState(
				settlerInformation.getMovableCount(EMovableType.BEARER),
				settlerInformation.getMovableCount(EMovableType.PIONEER),
				settlerInformation.getMovableCount(EMovableType.GEOLOGIST),
				settlerInformation.getMovableCount(EMovableType.THIEF),
				positionControls.isInPlayerPartition());
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
			if (modelClass == SpecialistsViewModel.class) {
				return (T) new SpecialistsViewModel(controlsResolver.getActionControls(), controlsResolver.getPositionControls(),
						controlsResolver.getDrawControls(), controlsResolver.getPlayer());
			}
			throw new RuntimeException("SpecialistsViewModel.Factory doesn't know how to create a: " + modelClass.toString());
		}
	}
}
