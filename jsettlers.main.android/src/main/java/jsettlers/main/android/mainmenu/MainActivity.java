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

package jsettlers.main.android.mainmenu;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import jsettlers.main.android.R;
import jsettlers.main.android.core.GameStarter;
import jsettlers.main.android.core.resources.scanner.AndroidResourcesLoader;
import jsettlers.main.android.gameplay.GameActivity_;
import jsettlers.main.android.mainmenu.gamesetup.JoinMultiPlayerSetupFragment;
import jsettlers.main.android.mainmenu.gamesetup.NewMultiPlayerSetupFragment;
import jsettlers.main.android.mainmenu.gamesetup.NewSinglePlayerSetupFragment;
import jsettlers.main.android.mainmenu.home.MainMenuFragment;
import jsettlers.main.android.mainmenu.mappicker.JoinMultiPlayerPickerFragment;
import jsettlers.main.android.mainmenu.mappicker.LoadSinglePlayerPickerFragment;
import jsettlers.main.android.mainmenu.mappicker.NewMultiPlayerPickerFragment;
import jsettlers.main.android.mainmenu.mappicker.NewSinglePlayerPickerFragment;
import jsettlers.main.android.mainmenu.navigation.Actions;
import jsettlers.main.android.mainmenu.navigation.MainMenuNavigator;
import jsettlers.main.android.su.SuLaunch;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainMenuNavigator {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().addOnBackStackChangedListener(this::setUpButton);
		getSupportFragmentManager().addOnBackStackChangedListener(this::endSuSessionAtMenuRoot);

		if (savedInstanceState != null)
			return;

		getSupportFragmentManager().beginTransaction()
				.add(R.id.frame_layout, MainMenuFragment.create())
				.commit();
		if (SuLaunch.isSuLaunch(getIntent())) {
			pendingSuLaunch = getIntent();
		}
	}

	/**
	 * Settlers United launcher (contract {@link SuLaunch}): started or brought to front by {@link jsettlers.main.android.su.SuLaunchActivity}.
	 * Handled in {@link #onResumeFragments()}, when fragment transactions are allowed.
	 */
	private Intent pendingSuLaunch;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (SuLaunch.isSuLaunch(intent)) {
			pendingSuLaunch = intent;
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		if (pendingSuLaunch != null) {
			Intent intent = pendingSuLaunch;
			pendingSuLaunch = null;
			handleSuLaunch(intent);
		}
	}

	private void handleSuLaunch(Intent intent) {
		SuLaunch.Result result = SuLaunch.parse(intent.getExtras());
		if (result.launch == null) {
			Toast.makeText(this, getString(R.string.su_launch_invalid, result.error), Toast.LENGTH_LONG).show();
			return;
		}
		if (!new AndroidResourcesLoader(this).setup()) {
			Toast.makeText(this, R.string.su_launch_no_data, Toast.LENGTH_LONG).show();
			return;
		}
		// back to the menu root first: ends an older session and closes its screens
		getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		String error = ((GameStarter) getApplication()).startSuSession(result.launch);
		if (error != null) {
			Toast.makeText(this, getString(R.string.su_launch_server_failed, error), Toast.LENGTH_LONG).show();
			return;
		}
		if (result.launch.role == SuLaunch.Role.HOST) {
			showNewMultiPlayerPicker();
		} else {
			showJoinMultiPlayerPicker();
		}
	}

	/** Back at the main menu without a starting game: the launcher session is over (the stored settings apply again). */
	private void endSuSessionAtMenuRoot() {
		GameStarter gameStarter = (GameStarter) getApplication();
		if (getSupportFragmentManager().getBackStackEntryCount() == 0 && gameStarter.getStartingGame() == null) {
			gameStarter.endSuSession();
		}
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setUpButton();
	}

	@OptionsItem(android.R.id.home)
	void homeSelected() {
		getSupportFragmentManager().popBackStack();
	}

	/**
	 * MainMenu Navigation
	 */
	@Override
	public void showNewSinglePlayerPicker() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, NewSinglePlayerPickerFragment.newInstance())
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void showLoadSinglePlayerPicker() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, LoadSinglePlayerPickerFragment.newInstance())
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void showNewSinglePlayerSetup(String mapId) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, NewSinglePlayerSetupFragment.create(mapId))
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void showNewMultiPlayerPicker() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, NewMultiPlayerPickerFragment.newInstance())
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void showJoinMultiPlayerPicker() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, JoinMultiPlayerPickerFragment.create())
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void showJoinMultiPlayerSetup(String mapId) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, JoinMultiPlayerSetupFragment.create(mapId))
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void showNewMultiPlayerSetup(String mapId) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_layout, NewMultiPlayerSetupFragment.create(mapId))
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void resumeGame() {
		GameActivity_.intent(this).action(Actions.ACTION_RESUME_GAME).start();
		finish();
	}

	@Override
	public void showGame() {
		GameActivity_.intent(this).start();
		finish();
	}

	@Override
	public void popToMenuRoot() {
		getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	private void setUpButton() {
		boolean isAtRootOfBackStack = getSupportFragmentManager().getBackStackEntryCount() == 0;
		getSupportActionBar().setDisplayHomeAsUpEnabled(!isAtRootOfBackStack);
	}
}
