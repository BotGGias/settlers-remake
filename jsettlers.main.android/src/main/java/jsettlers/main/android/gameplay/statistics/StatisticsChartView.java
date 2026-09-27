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

package jsettlers.main.android.gameplay.statistics;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import jsettlers.common.statistics.EStatisticType;
import jsettlers.common.statistics.GameStatistics;
import jsettlers.common.statistics.GameStatistics.PlayerStatistics;
import jsettlers.common.statistics.StatisticsSeries;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.main.android.R;

/**
 * A line chart showing one {@link EStatisticType} of all players over the course of the game. Every player gets a line in its player color.
 */
public class StatisticsChartView extends View {
	private static final int HORIZONTAL_GRID_LINES = 4;
	private static final int TIME_LABELS = 4;
	private static final int MINUTE_MS = 60 * 1000;

	private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint gridPaint = new Paint();
	private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path path = new Path();

	private final float lineWidth;
	private final float localLineWidth;
	private final float labelPadding;

	private List<PlayerStatistics> players = Collections.emptyList();
	private EStatisticType type = EStatisticType.SETTLERS;
	private int maxValue;
	private int maxTime;

	public StatisticsChartView(Context context) {
		this(context, null);
	}

	public StatisticsChartView(Context context, AttributeSet attrs) {
		super(context, attrs);

		lineWidth = dp(2);
		localLineWidth = dp(3.5f);
		labelPadding = dp(4);

		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeJoin(Paint.Join.ROUND);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		pointPaint.setStyle(Paint.Style.FILL);

		gridPaint.setColor(0x33ffffff);
		gridPaint.setStrokeWidth(dp(1));

		labelPaint.setColor(0xaaffffff);
		labelPaint.setTextSize(sp(11));

		emptyPaint.setColor(0xaaffffff);
		emptyPaint.setTextSize(sp(14));
		emptyPaint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * Shows the given value of the given statistics.
	 */
	public void setStatistics(GameStatistics statistics, EStatisticType type) {
		this.players = statistics != null ? statistics.getPlayers() : Collections.emptyList();
		this.type = type;
		this.maxValue = statistics != null ? niceMaximum(statistics.getMaxValue(type)) : 0;
		this.maxTime = statistics != null ? statistics.getLastTime() : 0;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!hasSamples()) {
			float y = getHeight() / 2f - (emptyPaint.descent() + emptyPaint.ascent()) / 2;
			canvas.drawText(getContext().getString(R.string.statistics_no_data), getWidth() / 2f, y, emptyPaint);
			return;
		}

		float labelHeight = labelPaint.descent() - labelPaint.ascent();
		float left = getPaddingLeft() + labelPaint.measureText(formatValue(maxValue)) + labelPadding;
		float top = getPaddingTop() + labelHeight / 2;
		float right = getWidth() - getPaddingRight() - labelPaint.measureText("0:00") / 2;
		float bottom = getHeight() - getPaddingBottom() - labelHeight - labelPadding;
		if (right <= left || bottom <= top) {
			return;
		}

		drawValueGrid(canvas, left, top, right, bottom);
		drawTimeLabels(canvas, left, right, bottom, labelHeight);

		// draw the local player last, so that its line is on top
		for (PlayerStatistics player : players) {
			if (!player.isLocalPlayer()) {
				drawPlayer(canvas, player, left, top, right, bottom);
			}
		}
		for (PlayerStatistics player : players) {
			if (player.isLocalPlayer()) {
				drawPlayer(canvas, player, left, top, right, bottom);
			}
		}
	}

	private boolean hasSamples() {
		for (PlayerStatistics player : players) {
			if (player.getSeries().getSampleCount() > 0) {
				return true;
			}
		}
		return false;
	}

	private void drawValueGrid(Canvas canvas, float left, float top, float right, float bottom) {
		labelPaint.setTextAlign(Paint.Align.RIGHT);
		float textOffset = -(labelPaint.descent() + labelPaint.ascent()) / 2;
		for (int i = 0; i <= HORIZONTAL_GRID_LINES; i++) {
			int value = maxValue * i / HORIZONTAL_GRID_LINES;
			float y = toY(value, top, bottom);
			canvas.drawLine(left, y, right, y, gridPaint);
			canvas.drawText(formatValue(value), left - labelPadding, y + textOffset, labelPaint);
		}
	}

	private void drawTimeLabels(Canvas canvas, float left, float right, float bottom, float labelHeight) {
		labelPaint.setTextAlign(Paint.Align.CENTER);
		float y = bottom + labelPadding - labelPaint.ascent();
		int labels = Math.min(TIME_LABELS, maxTime / MINUTE_MS);
		for (int i = 0; i <= labels; i++) {
			int time = labels > 0 ? (int) ((long) maxTime * i / labels) : 0;
			float x = toX(time, left, right);
			canvas.drawText(formatTime(time), x, y, labelPaint);
		}
	}

	private void drawPlayer(Canvas canvas, PlayerStatistics player, float left, float top, float right, float bottom) {
		StatisticsSeries series = player.getSeries();
		int samples = series.getSampleCount();
		if (samples == 0) {
			return;
		}

		int color = MapDrawContext.getPlayerColor(player.getPlayerId()).getARGB();
		float width = player.isLocalPlayer() ? localLineWidth : lineWidth;

		if (samples == 1) {
			pointPaint.setColor(color);
			canvas.drawCircle(toX(series.getTime(0), left, right), toY(series.getValue(type, 0), top, bottom), width, pointPaint);
			return;
		}

		path.reset();
		for (int i = 0; i < samples; i++) {
			float x = toX(series.getTime(i), left, right);
			float y = toY(series.getValue(type, i), top, bottom);
			if (i == 0) {
				path.moveTo(x, y);
			} else {
				path.lineTo(x, y);
			}
		}
		linePaint.setColor(color);
		linePaint.setStrokeWidth(width);
		canvas.drawPath(path, linePaint);
	}

	private float toX(int time, float left, float right) {
		if (maxTime <= 0) {
			return left;
		}
		return left + (right - left) * time / maxTime;
	}

	private float toY(int value, float top, float bottom) {
		if (maxValue <= 0) {
			return bottom;
		}
		return bottom - (bottom - top) * value / maxValue;
	}

	/**
	 * @return A round number that is at least the given value and can be divided into {@link #HORIZONTAL_GRID_LINES} round steps.
	 */
	static int niceMaximum(int value) {
		if (value <= HORIZONTAL_GRID_LINES) {
			return HORIZONTAL_GRID_LINES;
		}
		double rawStep = (double) value / HORIZONTAL_GRID_LINES;
		double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
		double[] factors = { 1, 2, 2.5, 5, 10 };
		for (double factor : factors) {
			double step = factor * magnitude;
			if (step >= rawStep && step == Math.rint(step)) {
				return (int) (step * HORIZONTAL_GRID_LINES);
			}
		}
		return (int) Math.ceil(10 * magnitude) * HORIZONTAL_GRID_LINES;
	}

	static String formatValue(int value) {
		if (value >= 1000000) {
			return trimZero(value / 100000) + "M";
		} else if (value >= 10000) {
			return trimZero(value / 100) + "k";
		}
		return Integer.toString(value);
	}

	private static String trimZero(int tenths) {
		return tenths % 10 == 0 ? Integer.toString(tenths / 10) : (tenths / 10) + "." + (tenths % 10);
	}

	/**
	 * @return The game time as hours:minutes.
	 */
	static String formatTime(int timeMs) {
		int minutes = timeMs / MINUTE_MS;
		return String.format(java.util.Locale.ROOT, "%d:%02d", minutes / 60, minutes % 60);
	}

	private float dp(float value) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}

	private float sp(float value) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
	}
}
