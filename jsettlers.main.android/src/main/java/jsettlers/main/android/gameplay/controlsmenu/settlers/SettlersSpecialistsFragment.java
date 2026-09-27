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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.map.draw.ECommonLinkType;
import jsettlers.graphics.map.draw.ImageLinkMap;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.resources.OriginalImageProvider;

/**
 * Page of the settlers menu to convert bearers into pioneers, geologists and thieves and back.
 */
@EFragment(R.layout.menu_settlers_specialists)
public class SettlersSpecialistsFragment extends Fragment {
	private static final short FIVE = 5;

	public static SettlersSpecialistsFragment newInstance() {
		return new SettlersSpecialistsFragment_();
	}

	private SpecialistsViewModel viewModel;

	@ViewById(R.id.text_view_carriers)
	TextView carriersTextView;
	@ViewById(R.id.text_view_pioneers)
	TextView pioneersTextView;
	@ViewById(R.id.text_view_geologists)
	TextView geologistsTextView;
	@ViewById(R.id.text_view_thieves)
	TextView thievesTextView;
	@ViewById(R.id.image_view_pioneer)
	ImageView pioneerImageView;
	@ViewById(R.id.image_view_geologist)
	ImageView geologistImageView;
	@ViewById(R.id.image_view_thief)
	ImageView thiefImageView;
	@ViewById(R.id.layout_specialists)
	ViewGroup specialistsLayout;

	@AfterViews
	void setupImages() {
		ECivilisation civilisation = new ControlsResolver(getActivity()).getPlayer().getCivilisation();
		OriginalImageProvider.get(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.PIONEER)).setAsImage(pioneerImageView);
		OriginalImageProvider.get(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.GEOLOGIST)).setAsImage(geologistImageView);
		OriginalImageProvider.get(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.THIEF)).setAsImage(thiefImageView);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		viewModel = ViewModelProviders.of(this, new SpecialistsViewModel.Factory(getActivity())).get(SpecialistsViewModel.class);

		viewModel.getState().observe(this, state -> {
			carriersTextView.setText(getString(R.string.specialists_carriers, state.carriers));
			pioneersTextView.setText(String.valueOf(state.pioneers));
			geologistsTextView.setText(String.valueOf(state.geologists));
			thievesTextView.setText(String.valueOf(state.thieves));
			setButtonsEnabled(specialistsLayout, state.convertingPossible);
		});
	}

	private static void setButtonsEnabled(ViewGroup viewGroup, boolean enabled) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View child = viewGroup.getChildAt(i);
			if (child instanceof ViewGroup) {
				setButtonsEnabled((ViewGroup) child, enabled);
			} else if (child instanceof Button) {
				child.setEnabled(enabled);
			}
		}
	}

	@Click(R.id.button_pioneer_add_one)
	void addOnePioneer() {
		viewModel.createSpecialists(EMovableType.PIONEER, (short) 1);
	}

	@Click(R.id.button_pioneer_add_five)
	void addFivePioneers() {
		viewModel.createSpecialists(EMovableType.PIONEER, FIVE);
	}

	@Click(R.id.button_pioneer_add_all)
	void addAllPioneers() {
		viewModel.createSpecialists(EMovableType.PIONEER, Short.MAX_VALUE);
	}

	@Click(R.id.button_pioneer_remove_one)
	void removeOnePioneer() {
		viewModel.removeSpecialists(EMovableType.PIONEER, (short) 1);
	}

	@Click(R.id.button_pioneer_remove_all)
	void removeAllPioneers() {
		viewModel.removeSpecialists(EMovableType.PIONEER, Short.MAX_VALUE);
	}

	@Click(R.id.button_geologist_add_one)
	void addOneGeologist() {
		viewModel.createSpecialists(EMovableType.GEOLOGIST, (short) 1);
	}

	@Click(R.id.button_geologist_add_five)
	void addFiveGeologists() {
		viewModel.createSpecialists(EMovableType.GEOLOGIST, FIVE);
	}

	@Click(R.id.button_geologist_add_all)
	void addAllGeologists() {
		viewModel.createSpecialists(EMovableType.GEOLOGIST, Short.MAX_VALUE);
	}

	@Click(R.id.button_geologist_remove_one)
	void removeOneGeologist() {
		viewModel.removeSpecialists(EMovableType.GEOLOGIST, (short) 1);
	}

	@Click(R.id.button_geologist_remove_all)
	void removeAllGeologists() {
		viewModel.removeSpecialists(EMovableType.GEOLOGIST, Short.MAX_VALUE);
	}

	@Click(R.id.button_thief_add_one)
	void addOneThief() {
		viewModel.createSpecialists(EMovableType.THIEF, (short) 1);
	}

	@Click(R.id.button_thief_add_five)
	void addFiveThieves() {
		viewModel.createSpecialists(EMovableType.THIEF, FIVE);
	}

	@Click(R.id.button_thief_add_all)
	void addAllThieves() {
		viewModel.createSpecialists(EMovableType.THIEF, Short.MAX_VALUE);
	}

	@Click(R.id.button_thief_remove_one)
	void removeOneThief() {
		viewModel.removeSpecialists(EMovableType.THIEF, (short) 1);
	}

	@Click(R.id.button_thief_remove_all)
	void removeAllThieves() {
		viewModel.removeSpecialists(EMovableType.THIEF, Short.MAX_VALUE);
	}
}
