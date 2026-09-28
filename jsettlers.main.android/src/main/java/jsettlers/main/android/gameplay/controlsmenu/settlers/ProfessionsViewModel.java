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

import jsettlers.common.action.ChangeMovableSettingsAction;
import jsettlers.common.action.SetMovableLimitTypeAction;
import jsettlers.common.map.partition.IPartitionData;
import jsettlers.common.map.partition.IProfessionSettings;
import jsettlers.common.map.partition.ISingleProfessionLimit;
import jsettlers.common.movable.EMovableType;
import jsettlers.graphics.localization.Labels;
import jsettlers.main.android.core.controls.ActionControls;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.controls.DrawControls;
import jsettlers.main.android.core.controls.PositionControls;
import jsettlers.main.android.core.events.DrawEvents;

/**
 * Shows and changes the wanted number of bearers, diggers and builders in the area on the screen.
 */
public class ProfessionsViewModel extends ViewModel {
	private static final int CHANGE_STEP = 5;

	/**
	 * Settings of one profession.
	 */
	public static class ProfessionState {
		public final EMovableType movableType;
		public final String target;
		public final String current;
		public final boolean relative;

		ProfessionState(EMovableType movableType, String target, String current, boolean relative) {
			this.movableType = movableType;
			this.target = target;
			this.current = current;
			this.relative = relative;
		}
	}

	/**
	 * Snapshot of all professions.
	 */
	public static class ProfessionsState {
		public final boolean inPlayerPartition;
		public final ProfessionState[] professions;

		ProfessionsState(boolean inPlayerPartition, ProfessionState[] professions) {
			this.inPlayerPartition = inPlayerPartition;
			this.professions = professions;
		}
	}

	private final ActionControls actionControls;
	private final PositionControls positionControls;
	private final LiveData<ProfessionsState> state;

	public ProfessionsViewModel(ActionControls actionControls, PositionControls positionControls, DrawControls drawControls) {
		this.actionControls = actionControls;
		this.positionControls = positionControls;

		state = Transformations.map(new DrawEvents(drawControls), x -> createState());
	}

	public LiveData<ProfessionsState> getState() {
		return state;
	}

	public void increase(EMovableType movableType) {
		changeTarget(movableType, CHANGE_STEP);
	}

	public void decrease(EMovableType movableType) {
		changeTarget(movableType, -CHANGE_STEP);
	}

	private void changeTarget(EMovableType movableType, int amount) {
		if (positionControls.isInPlayerPartition()) {
			actionControls.fireAction(new ChangeMovableSettingsAction(movableType, true, amount, positionControls.getCurrentPosition()));
		}
	}

	/**
	 * Switches between a target in percent of all settlers and an absolute number of settlers.
	 */
	public void setRelative(EMovableType movableType, boolean relative) {
		if (positionControls.isInPlayerPartition()) {
			actionControls.fireAction(new SetMovableLimitTypeAction(positionControls.getCurrentPosition(), movableType, relative));
		}
	}

	private ProfessionsState createState() {
		IPartitionData partitionData = positionControls.isInPlayerPartition() ? positionControls.getCurrentPartitionData() : null;
		if (partitionData == null) {
			return new ProfessionsState(false, new ProfessionState[0]);
		}

		IProfessionSettings professionSettings = partitionData.getPartitionSettings().getProfessionSettings();
		return new ProfessionsState(true, new ProfessionState[] {
				createProfessionState(professionSettings, EMovableType.BEARER, true),
				createProfessionState(professionSettings, EMovableType.DIGGER, false),
				createProfessionState(professionSettings, EMovableType.BRICKLAYER, false)
		});
	}

	/**
	 * @param minimum
	 *            true if the target is the minimum number of settlers, false if it is the maximum.
	 */
	private static ProfessionState createProfessionState(IProfessionSettings professionSettings, EMovableType movableType, boolean minimum) {
		ISingleProfessionLimit limit = professionSettings.getSettings(movableType);
		String targetValue = limit.isRelative() ? Math.round(limit.getTargetRatio() * 100) + "%" : String.valueOf(limit.getTargetCount());
		String target = (minimum ? "> " : "< ") + targetValue;
		String current = Labels.getString("settler_profession_currently", limit.getCurrentCount(), limit.getCurrentRatio() * 100);
		return new ProfessionState(movableType, target, current, limit.isRelative());
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
			if (modelClass == ProfessionsViewModel.class) {
				return (T) new ProfessionsViewModel(
						controlsResolver.getActionControls(),
						controlsResolver.getPositionControls(),
						controlsResolver.getDrawControls());
			}
			throw new RuntimeException("ProfessionsViewModel.Factory doesn't know how to create a: " + modelClass.toString());
		}
	}
}
