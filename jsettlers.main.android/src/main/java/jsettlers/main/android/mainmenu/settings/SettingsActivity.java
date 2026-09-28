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

package jsettlers.main.android.mainmenu.settings;

import android.widget.SeekBar;
import android.widget.Switch;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import jsettlers.main.android.R;
import jsettlers.main.android.core.ui.dialogs.EditTextDialog;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends AppCompatActivity implements SettingsView, EditTextDialog.Listener {
	private static final int REQUEST_CODE_PLAYER_NAME = 10;
	private static final int REQUEST_CODE_SERVER_ADDRESS = 11;

	@ViewById(R.id.text_view_player_name)
	TextView textViewPlayerName;

	@ViewById(R.id.text_view_server_address)
	TextView textViewServerAddress;

	@ViewById(R.id.switch_play_all_music)
	Switch switchPlayAllMusic;

	@ViewById(R.id.switch_music_enabled)
	Switch switchMusicEnabled;

	@ViewById(R.id.seek_bar_music_volume)
	SeekBar seekBarMusicVolume;

	@ViewById(R.id.text_view_music_volume)
	TextView textViewMusicVolume;

	@ViewById(R.id.seek_bar_sound_volume)
	SeekBar seekBarSoundVolume;

	@ViewById(R.id.text_view_sound_volume)
	TextView textViewSoundVolume;

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	private SettingsPresenter presenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		presenter = PresenterFactory.createSettingsPresenter(this, this);
	}

	@AfterViews
	void configureToolbar() {
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@AfterViews
	void bindPresenter() {
		presenter.bindView();
	}

	@Click(R.id.layout_player_name)
	void onClickPlayerNameLayout() {
		EditTextDialog.create(REQUEST_CODE_PLAYER_NAME, R.string.settings_player_name, R.string.settings_name, textViewPlayerName.getText()).show(getSupportFragmentManager(), null);
	}

	@Click(R.id.layout_server_address)
	void onClickServerAddressLayout() {
		EditTextDialog.create(REQUEST_CODE_SERVER_ADDRESS, R.string.settings_server_address, R.string.settings_address, textViewServerAddress.getText()).show(getSupportFragmentManager(), null);
	}

	@CheckedChange(R.id.switch_play_all_music)
	void onCheckedChangedPlayAllMusicLayout() {
		presenter.playAllMusicEdited(switchPlayAllMusic.isChecked());
	}

	@CheckedChange(R.id.switch_music_enabled)
	void onCheckedChangedMusicEnabled() {
		presenter.musicEnabledEdited(switchMusicEnabled.isChecked());
	}

	@SeekBarProgressChange(R.id.seek_bar_music_volume)
	void onMusicVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			presenter.musicVolumeEdited(progress);
		}
	}

	@SeekBarProgressChange(R.id.seek_bar_sound_volume)
	void onSoundVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			presenter.soundVolumeEdited(progress);
		}
	}

	@OptionsItem(android.R.id.home)
	void homeSelected() {
		finish();
	}

	@Override
	public void setPlayerName(String playerName) {
		textViewPlayerName.setText(playerName);
	}

	@Override
	public void setServerAddress(String serverAddress) {
		textViewServerAddress.setText(serverAddress);
	}

	@Override
	public void setPlayAllMusic(boolean playAll) {
		switchPlayAllMusic.setChecked(playAll);
	}

	@Override
	public void setMusicEnabled(boolean musicEnabled) {
		switchMusicEnabled.setChecked(musicEnabled);
		seekBarMusicVolume.setEnabled(musicEnabled);
		switchPlayAllMusic.setEnabled(musicEnabled);
	}

	@Override
	public void setMusicVolume(int percent) {
		seekBarMusicVolume.setProgress(percent);
		textViewMusicVolume.setText(getString(R.string.settings_volume_percent, percent));
	}

	@Override
	public void setSoundVolume(int percent) {
		seekBarSoundVolume.setProgress(percent);
		textViewSoundVolume.setText(getString(R.string.settings_volume_percent, percent));
	}

	@Override
	public void saveEditTextDialog(int requestCode, String text) {
		switch (requestCode) {
		case REQUEST_CODE_PLAYER_NAME:
			presenter.playerNameEdited(text);
			break;
		case REQUEST_CODE_SERVER_ADDRESS:
			presenter.serverAddressEdited(text);
			break;
		}
	}
}
