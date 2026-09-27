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

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.draw.ECommonLinkType;
import jsettlers.graphics.map.draw.ImageLinkMap;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.resources.OriginalImageProvider;

/**
 * Page of the settlers menu to set how many bearers, diggers and builders the area on the screen should have.
 */
@EFragment(R.layout.menu_settlers_professions)
public class SettlersProfessionsFragment extends Fragment {
	public static SettlersProfessionsFragment newInstance() {
		return new SettlersProfessionsFragment_();
	}

	private ProfessionsViewModel viewModel;
	private ECivilisation civilisation;

	@ViewById(R.id.textView_title)
	TextView titleTextView;
	@ViewById(R.id.recyclerView)
	RecyclerView recyclerView;
	@ViewById(R.id.textView_message)
	TextView messageTextView;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		titleTextView.setText(Labels.getString("settler_profession_title"));
		civilisation = new ControlsResolver(getActivity()).getPlayer().getCivilisation();

		viewModel = ViewModelProviders.of(this, new ProfessionsViewModel.Factory(getActivity())).get(ProfessionsViewModel.class);

		ProfessionsAdapter adapter = new ProfessionsAdapter(getActivity());
		recyclerView.setAdapter(adapter);

		viewModel.getState().observe(this, state -> {
			adapter.setProfessionStates(state.professions);
			recyclerView.setVisibility(state.inPlayerPartition ? View.VISIBLE : View.INVISIBLE);
			messageTextView.setVisibility(state.inPlayerPartition ? View.INVISIBLE : View.VISIBLE);
		});
	}

	/**
	 * RecyclerView adapter
	 */
	private class ProfessionsAdapter extends RecyclerView.Adapter<ProfessionViewHolder> {
		private final LayoutInflater layoutInflater;
		private ProfessionsViewModel.ProfessionState[] professionStates = new ProfessionsViewModel.ProfessionState[0];

		ProfessionsAdapter(Activity activity) {
			this.layoutInflater = LayoutInflater.from(activity);
		}

		@Override
		public int getItemCount() {
			return professionStates.length;
		}

		@Override
		public ProfessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ProfessionViewHolder(layoutInflater.inflate(R.layout.vh_settler_profession, parent, false));
		}

		@Override
		public void onBindViewHolder(ProfessionViewHolder holder, int position) {
			holder.bind(professionStates[position]);
		}

		void setProfessionStates(ProfessionsViewModel.ProfessionState[] professionStates) {
			boolean sameItems = this.professionStates.length == professionStates.length;
			this.professionStates = professionStates;
			if (sameItems) {
				notifyItemRangeChanged(0, professionStates.length);
			} else {
				notifyDataSetChanged();
			}
		}
	}

	/**
	 * RecyclerView ViewHolder
	 */
	private class ProfessionViewHolder extends RecyclerView.ViewHolder {
		private final ImageView imageView;
		private final TextView nameTextView;
		private final TextView targetTextView;
		private final TextView currentTextView;
		private final CheckBox relativeCheckBox;

		private EMovableType movableType;

		ProfessionViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView_settler);
			nameTextView = itemView.findViewById(R.id.textView_name);
			targetTextView = itemView.findViewById(R.id.textView_target);
			currentTextView = itemView.findViewById(R.id.textView_current);
			relativeCheckBox = itemView.findViewById(R.id.checkBox_relative);
			relativeCheckBox.setText(Labels.getString("settler_profession_relative"));

			itemView.findViewById(R.id.textView_increment).setOnClickListener(view -> viewModel.increase(movableType));
			itemView.findViewById(R.id.textView_decrement).setOnClickListener(view -> viewModel.decrease(movableType));
			relativeCheckBox.setOnClickListener(view -> viewModel.setRelative(movableType, relativeCheckBox.isChecked()));
		}

		void bind(ProfessionsViewModel.ProfessionState state) {
			if (movableType != state.movableType) {
				movableType = state.movableType;
				OriginalImageProvider.get(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, movableType)).setAsImage(imageView);
				nameTextView.setText(Labels.getName(movableType));
			}
			targetTextView.setText(state.target);
			currentTextView.setText(state.current);
			relativeCheckBox.setChecked(state.relative);
		}
	}
}
