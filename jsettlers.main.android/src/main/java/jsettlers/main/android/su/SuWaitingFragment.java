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

package jsettlers.main.android.su;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import jsettlers.main.android.R;
import jsettlers.main.android.core.GameStarter;
import jsettlers.main.android.core.ui.FragmentUtil;
import jsettlers.main.android.mainmenu.navigation.MainMenuNavigator;

/**
 * Launcher start contract v2: waiting screen instead of map picker and game setup, until the match starts.
 */
@EFragment(R.layout.fragment_su_waiting)
public class SuWaitingFragment extends Fragment {

	public static SuWaitingFragment create() {
		return new SuWaitingFragment_();
	}

	@ViewById(R.id.toolbar)
	Toolbar toolbar;
	@ViewById(R.id.text_view_status)
	TextView statusView;

	private SuWaitingViewModel viewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SuSession session = ((GameStarter) getActivity().getApplication()).getSuSession();
		if (session == null || !session.launch.isDirect()) { // e.g. restored after the process was killed
			((MainMenuNavigator) getActivity()).popToMenuRoot();
			return;
		}
		viewModel = ViewModelProviders.of(this, new SuWaitingViewModel.Factory(getActivity(), session.launch)).get(SuWaitingViewModel.class);
	}

	@AfterViews
	void setupToolbar() {
		FragmentUtil.setActionBar(this, toolbar);
		toolbar.setTitle(R.string.su_wait_title);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (viewModel == null) {
			return;
		}
		viewModel.getStatus().observe(this, statusView::setText);
		viewModel.getStartedEvent().observe(this, started -> ((MainMenuNavigator) getActivity()).showGame());
		viewModel.getFailedEvent().observe(this, message -> {
			if (message != null) {
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
			((MainMenuNavigator) getActivity()).popToMenuRoot();
		});
	}

	@Click(R.id.button_cancel)
	void cancelClicked() {
		viewModel.cancel();
	}
}
