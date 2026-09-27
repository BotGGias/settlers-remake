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
package jsettlers.main.android.gameplay.controlsmenu.goods;

import java.util.Arrays;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.os.SystemClock;

import jsettlers.common.action.SetMaterialPrioritiesAction;
import jsettlers.common.map.partition.IPartitionData;
import jsettlers.common.map.partition.IPartitionSettings;
import jsettlers.common.map.partition.IStockSettings;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.material.MaterialPriorityOrder;
import jsettlers.main.android.core.controls.ActionControls;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.controls.DrawControls;
import jsettlers.main.android.core.controls.PositionControls;
import jsettlers.main.android.core.events.DrawEvents;

/**
 * Shows and changes the order in which the bearers of the area on the screen transport the materials.
 */
public class PrioritiesViewModel extends ViewModel {
	public static final int MOVE_TO_TOP = -100;
	public static final int MOVE_UP = -1;
	public static final int MOVE_DOWN = 1;
	public static final int MOVE_TO_BOTTOM = 100;

	/**
	 * A new order needs a moment until the game applies it. Until then, or until this timeout, the sent order is shown and used for further moves, so that fast
	 * clicks don't get lost.
	 */
	private static final long PENDING_ORDER_TIMEOUT_MS = 2000;

	/**
	 * Snapshot of the priority list.
	 */
	public static class PrioritiesState {
		public final boolean inPlayerPartition;
		public final PriorityMaterialState[] materials;
		public final EMaterialType selectedMaterial;

		PrioritiesState(boolean inPlayerPartition, PriorityMaterialState[] materials, EMaterialType selectedMaterial) {
			this.inPlayerPartition = inPlayerPartition;
			this.materials = materials;
			this.selectedMaterial = selectedMaterial;
		}
	}

	private final ActionControls actionControls;
	private final PositionControls positionControls;

	private final MediatorLiveData<PrioritiesState> state = new MediatorLiveData<>();

	private EMaterialType selectedMaterial;
	private EMaterialType[] displayedOrder = new EMaterialType[0];
	private EMaterialType[] pendingOrder;
	private long pendingOrderTime;

	public PrioritiesViewModel(ActionControls actionControls, PositionControls positionControls, DrawControls drawControls) {
		this.actionControls = actionControls;
		this.positionControls = positionControls;

		state.addSource(new DrawEvents(drawControls), x -> updateState());
	}

	public LiveData<PrioritiesState> getState() {
		return state;
	}

	public void selectMaterial(EMaterialType materialType) {
		selectedMaterial = materialType;
		updateState();
	}

	/**
	 * Moves the selected material in the priority list.
	 *
	 * @param offset
	 *            The number of places to move, negative values give it a higher priority. See {@link #MOVE_TO_TOP} and {@link #MOVE_TO_BOTTOM}.
	 */
	public void moveSelectedMaterial(int offset) {
		if (selectedMaterial == null || !positionControls.isInPlayerPartition()) {
			return;
		}

		int index = MaterialPriorityOrder.indexOf(displayedOrder, selectedMaterial);
		if (index < 0) {
			return;
		}

		EMaterialType[] newOrder = MaterialPriorityOrder.reorder(displayedOrder, selectedMaterial, index + offset);
		if (Arrays.equals(newOrder, displayedOrder)) {
			return;
		}

		actionControls.fireAction(new SetMaterialPrioritiesAction(positionControls.getCurrentPosition(), newOrder));
		pendingOrder = newOrder;
		pendingOrderTime = SystemClock.uptimeMillis();
		updateState();
	}

	private void updateState() {
		state.setValue(createState());
	}

	private PrioritiesState createState() {
		IPartitionData partitionData = positionControls.isInPlayerPartition() ? positionControls.getCurrentPartitionData() : null;
		if (partitionData == null) {
			displayedOrder = new EMaterialType[0];
			return new PrioritiesState(false, new PriorityMaterialState[0], selectedMaterial);
		}

		IPartitionSettings partitionSettings = partitionData.getPartitionSettings();
		IStockSettings stockSettings = partitionSettings.getStockSettings();
		displayedOrder = currentOrder(partitionSettings.getMaterialTypesForPriorities());

		PriorityMaterialState[] materials = new PriorityMaterialState[displayedOrder.length];
		for (int i = 0; i < displayedOrder.length; i++) {
			EMaterialType materialType = displayedOrder[i];
			materials[i] = new PriorityMaterialState(materialType, i + 1, stockSettings.isAccepted(materialType), materialType == selectedMaterial);
		}
		return new PrioritiesState(true, materials, selectedMaterial);
	}

	private EMaterialType[] currentOrder(EMaterialType[] gameOrder) {
		if (pendingOrder != null) {
			boolean applied = Arrays.equals(gameOrder, pendingOrder);
			boolean timedOut = SystemClock.uptimeMillis() - pendingOrderTime > PENDING_ORDER_TIMEOUT_MS;
			if (!applied && !timedOut) {
				return pendingOrder;
			}
			pendingOrder = null;
		}
		return gameOrder;
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
			if (modelClass == PrioritiesViewModel.class) {
				return (T) new PrioritiesViewModel(
						controlsResolver.getActionControls(),
						controlsResolver.getPositionControls(),
						controlsResolver.getDrawControls());
			}
			throw new RuntimeException("PrioritiesViewModel.Factory doesn't know how to create a: " + modelClass.toString());
		}
	}
}
