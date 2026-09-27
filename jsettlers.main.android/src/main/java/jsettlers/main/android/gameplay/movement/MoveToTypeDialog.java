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

package jsettlers.main.android.gameplay.movement;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import jsettlers.common.action.EMoveToType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ChosenMoveToAction;
import jsettlers.main.android.core.controls.ControlsResolver;

/**
 * Lets the user choose how the selected units should move to a position. Replaces the modifier keys of the desktop version (Ctrl, Shift, Alt).
 */
public class MoveToTypeDialog extends DialogFragment {
	private static final String ARG_X = "x";
	private static final String ARG_Y = "y";

	private static final EMoveToType[] MOVE_TO_TYPES = { EMoveToType.DEFAULT, EMoveToType.FORCED, EMoveToType.WAYPOINT, EMoveToType.PATROL };
	private static final int[] MOVE_TO_TYPE_LABELS = { R.string.move_to_normal, R.string.move_to_forced, R.string.move_to_waypoint, R.string.move_to_patrol };

	public static MoveToTypeDialog create(ShortPoint2D position) {
		Bundle arguments = new Bundle();
		arguments.putShort(ARG_X, position.x);
		arguments.putShort(ARG_Y, position.y);

		MoveToTypeDialog dialog = new MoveToTypeDialog();
		dialog.setArguments(arguments);
		return dialog;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();
		ShortPoint2D position = new ShortPoint2D(arguments.getShort(ARG_X), arguments.getShort(ARG_Y));

		String[] labels = new String[MOVE_TO_TYPE_LABELS.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = getString(MOVE_TO_TYPE_LABELS[i]);
		}

		AlertDialog dialog = new AlertDialog.Builder(requireActivity(), R.style.GameMenuDialogTheme)
				.setTitle(R.string.move_to_title)
				.setItems(labels, (x, which) -> moveTo(MOVE_TO_TYPES[which], position))
				.create();

		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		applyFullscreenWorkaround(dialog);

		return dialog;
	}

	private void moveTo(EMoveToType moveToType, ShortPoint2D position) {
		new ControlsResolver(requireActivity()).getActionControls().fireAction(new ChosenMoveToAction(moveToType, position));
	}

	/**
	 * Stops the system bars from showing when this dialog appears.
	 */
	private void applyFullscreenWorkaround(AlertDialog dialog) {
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		dialog.setOnShowListener(x -> dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE));
		int activitySystemUiVisibility = requireActivity().getWindow().getDecorView().getSystemUiVisibility();
		dialog.getWindow().getDecorView().setSystemUiVisibility(activitySystemUiVisibility);
	}
}
