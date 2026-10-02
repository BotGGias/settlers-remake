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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import jsettlers.main.android.R;
import jsettlers.main.android.core.GameManager;
import jsettlers.main.android.gameplay.GameActivity_;
import jsettlers.main.android.mainmenu.MainActivity_;
import jsettlers.main.android.mainmenu.navigation.Actions;

/**
 * Entry point for the Settlers United launcher ({@link SuLaunch#ACTION}). Invisible; checks the contract and forwards to the
 * main menu, which does the actual work. A running game is never interrupted.
 */
public class SuLaunchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent in = getIntent();
		SuLaunch.Result result = SuLaunch.parse(in.getExtras());

		if (((GameManager) getApplication()).isGameInProgress()) {
			Toast.makeText(this, R.string.su_launch_game_running, Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, GameActivity_.class).setAction(Actions.ACTION_RESUME_GAME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} else if (result.launch == null) {
			Toast.makeText(this, getString(R.string.su_launch_invalid, result.error), Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, MainActivity_.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} else {
			Bundle extras = new Bundle(in.getExtras());
			Uri referrer = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ? getReferrer() : null;
			if (!extras.containsKey(SuLaunch.EXTRA_RETURN_TO) && referrer != null && "android-app".equals(referrer.getScheme())) {
				extras.putString(SuLaunch.EXTRA_RETURN_TO, referrer.getHost()); // the launcher app that sent the intent
			}
			Intent out = new Intent(this, MainActivity_.class)
					.setAction(SuLaunch.ACTION)
					.putExtras(extras)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(out);
		}
		finish();
	}
}
