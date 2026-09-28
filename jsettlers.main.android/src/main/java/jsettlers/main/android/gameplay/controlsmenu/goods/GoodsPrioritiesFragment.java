/*
 * Copyright (c) 2017
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

import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import jsettlers.common.material.EMaterialType;
import jsettlers.graphics.localization.Labels;
import jsettlers.main.android.R;
import jsettlers.main.android.core.resources.OriginalImageProvider;

/**
 * Page of the goods menu to change the order in which the bearers transport the materials.
 */
@EFragment(R.layout.menu_goods_priorities)
public class GoodsPrioritiesFragment extends Fragment {
	public static GoodsPrioritiesFragment newInstance() {
		return new GoodsPrioritiesFragment_();
	}

	private PrioritiesViewModel viewModel;
	private PrioritiesAdapter adapter;
	private EMaterialType lastSelectedMaterial;
	private int lastSelectedIndex = -1;

	@ViewById(R.id.textView_title)
	TextView titleTextView;
	@ViewById(R.id.recyclerView)
	RecyclerView recyclerView;
	@ViewById(R.id.textView_message)
	TextView messageTextView;
	@ViewById(R.id.layout_buttons)
	ViewGroup buttonsLayout;
	@ViewById(R.id.button_to_top)
	ImageButton toTopButton;
	@ViewById(R.id.button_up)
	ImageButton upButton;
	@ViewById(R.id.button_down)
	ImageButton downButton;
	@ViewById(R.id.button_to_bottom)
	ImageButton toBottomButton;

	@AfterViews
	void setupViews() {
		titleTextView.setText(Labels.getString("controlpanel_transportation_title"));
		setupButton(toTopButton, "original_3_GUI_219", "priority-to-top");
		setupButton(upButton, "original_3_GUI_225", "priority-one-up");
		setupButton(downButton, "original_3_GUI_228", "priority-one-down");
		setupButton(toBottomButton, "original_3_GUI_222", "priority-to-bottom");
	}

	private static void setupButton(ImageButton button, String imageName, String descriptionLabel) {
		OriginalImageProvider.get(imageName).setAsImage(button);
		button.setContentDescription(Labels.getString(descriptionLabel));
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		viewModel = ViewModelProviders.of(this, new PrioritiesViewModel.Factory(getActivity())).get(PrioritiesViewModel.class);

		adapter = new PrioritiesAdapter(getActivity());
		recyclerView.setAdapter(adapter);

		viewModel.getState().observe(this, this::showState);
	}

	@Click(R.id.button_to_top)
	void toTopClicked() {
		viewModel.moveSelectedMaterial(PrioritiesViewModel.MOVE_TO_TOP);
	}

	@Click(R.id.button_up)
	void upClicked() {
		viewModel.moveSelectedMaterial(PrioritiesViewModel.MOVE_UP);
	}

	@Click(R.id.button_down)
	void downClicked() {
		viewModel.moveSelectedMaterial(PrioritiesViewModel.MOVE_DOWN);
	}

	@Click(R.id.button_to_bottom)
	void toBottomClicked() {
		viewModel.moveSelectedMaterial(PrioritiesViewModel.MOVE_TO_BOTTOM);
	}

	private void showState(PrioritiesViewModel.PrioritiesState state) {
		adapter.setMaterialStates(state.materials);

		recyclerView.setVisibility(state.inPlayerPartition ? View.VISIBLE : View.INVISIBLE);
		messageTextView.setVisibility(state.inPlayerPartition ? View.INVISIBLE : View.VISIBLE);

		boolean buttonsEnabled = state.inPlayerPartition && state.selectedMaterial != null;
		for (int i = 0; i < buttonsLayout.getChildCount(); i++) {
			View button = buttonsLayout.getChildAt(i);
			button.setEnabled(buttonsEnabled);
			button.setAlpha(buttonsEnabled ? 1f : 0.4f);
		}

		scrollToMovedMaterial(state);
	}

	/**
	 * Keeps the selected material visible when it was moved.
	 */
	private void scrollToMovedMaterial(PrioritiesViewModel.PrioritiesState state) {
		int selectedIndex = -1;
		for (int i = 0; i < state.materials.length; i++) {
			if (state.materials[i].isSelected()) {
				selectedIndex = i;
				break;
			}
		}

		if (selectedIndex >= 0 && state.selectedMaterial == lastSelectedMaterial && selectedIndex != lastSelectedIndex) {
			recyclerView.scrollToPosition(selectedIndex);
		}
		lastSelectedMaterial = state.selectedMaterial;
		lastSelectedIndex = selectedIndex;
	}

	/**
	 * RecyclerView adapter
	 */
	private class PrioritiesAdapter extends RecyclerView.Adapter<PriorityViewHolder> {
		private final LayoutInflater layoutInflater;
		private PriorityMaterialState[] materialStates = new PriorityMaterialState[0];

		PrioritiesAdapter(Activity activity) {
			this.layoutInflater = LayoutInflater.from(activity);
		}

		@Override
		public int getItemCount() {
			return materialStates.length;
		}

		@Override
		public PriorityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = layoutInflater.inflate(R.layout.vh_priority_material, parent, false);
			return new PriorityViewHolder(view);
		}

		@Override
		public void onBindViewHolder(PriorityViewHolder holder, int position) {
			holder.bind(materialStates[position]);
		}

		@Override
		public void onBindViewHolder(PriorityViewHolder holder, int position, List<Object> payloads) {
			if (payloads == null || payloads.size() == 0) {
				onBindViewHolder(holder, position);
			} else {
				holder.update(materialStates[position]);
			}
		}

		void setMaterialStates(PriorityMaterialState[] materialStates) {
			DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(this.materialStates, materialStates));
			this.materialStates = materialStates;
			diffResult.dispatchUpdatesTo(this);
		}

		/**
		 * Diff callback
		 */
		private class DiffCallback extends DiffUtil.Callback {
			private final PriorityMaterialState[] oldStates;
			private final PriorityMaterialState[] newStates;

			DiffCallback(PriorityMaterialState[] oldStates, PriorityMaterialState[] newStates) {
				this.oldStates = oldStates;
				this.newStates = newStates;
			}

			@Override
			public int getOldListSize() {
				return oldStates.length;
			}

			@Override
			public int getNewListSize() {
				return newStates.length;
			}

			@Override
			public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
				return oldStates[oldItemPosition].getMaterialType() == newStates[newItemPosition].getMaterialType();
			}

			@Override
			public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
				PriorityMaterialState oldState = oldStates[oldItemPosition];
				PriorityMaterialState newState = newStates[newItemPosition];
				return oldState.getRank() == newState.getRank()
						&& oldState.isStocked() == newState.isStocked()
						&& oldState.isSelected() == newState.isSelected();
			}

			@Nullable
			@Override
			public Object getChangePayload(int oldItemPosition, int newItemPosition) {
				return Boolean.TRUE;
			}
		}
	}

	/**
	 * RecyclerView ViewHolder
	 */
	private class PriorityViewHolder extends RecyclerView.ViewHolder {
		private final TextView rankTextView;
		private final ImageView imageView;
		private final TextView nameTextView;
		private final View stockedView;

		private EMaterialType materialType;

		PriorityViewHolder(View itemView) {
			super(itemView);
			rankTextView = itemView.findViewById(R.id.textView_rank);
			imageView = itemView.findViewById(R.id.imageView_material);
			nameTextView = itemView.findViewById(R.id.textView_name);
			stockedView = itemView.findViewById(R.id.view_stocked);

			itemView.setOnClickListener(view -> viewModel.selectMaterial(materialType));
		}

		void bind(PriorityMaterialState state) {
			materialType = state.getMaterialType();
			OriginalImageProvider.get(materialType).setAsImage(imageView);
			nameTextView.setText(Labels.getName(materialType, false));
			update(state);
		}

		void update(PriorityMaterialState state) {
			rankTextView.setText(String.valueOf(state.getRank()));
			stockedView.setSelected(state.isStocked());
			itemView.setActivated(state.isSelected());
		}
	}
}
