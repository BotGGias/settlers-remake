/*
 * Copyright (c) 2017 - 2018
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

package jsettlers.main.android.core.controls;

import go.graphics.GLDrawContext;
import go.graphics.UIPoint;
import go.graphics.event.GOEvent;
import go.graphics.event.GOModalEventHandler;
import go.graphics.event.mouse.GODrawEvent;
import go.graphics.event.mouse.GOPanEvent;
import java.util.Optional;
import jsettlers.common.action.Action;
import jsettlers.common.action.AskCastSpellAction;
import jsettlers.common.action.BuildAction;
import jsettlers.common.action.CastSpellAction;
import jsettlers.common.action.EActionType;
import jsettlers.common.action.EMoveToType;
import jsettlers.common.action.IAction;
import jsettlers.common.action.MoveToAction;
import jsettlers.common.action.PointAction;
import jsettlers.common.action.SetDockAction;
import jsettlers.common.action.SetTradingWaypointAction;
import jsettlers.common.action.ShowConstructionMarksAction;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.map.shapes.MapRectangle;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.selectable.ESelectionType;
import jsettlers.common.selectable.ISelectionSet;
import jsettlers.graphics.action.ActionFireable;
import jsettlers.graphics.action.AskSetTradingWaypointAction;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.graphics.map.controls.IControls;
import jsettlers.graphics.map.controls.original.MiniMapLayoutProperties;
import jsettlers.graphics.map.minimap.Minimap;
import jsettlers.graphics.map.minimap.MinimapMode;

/**
 * Created by tompr on 21/11/2016.
 */
public class AndroidControls implements IControls, ActionFireable, TaskControls {
	/*
	 * Minimap layout in dp. These have to match the overlaying Android views in fragment_map.xml / dimens.xml.
	 */
	private static final float MINIMAP_MARGIN_DP = 8;
	private static final float MINIMAP_MAX_HEIGHT_DP = 220;
	private static final float TOOLBAR_HEIGHT_DP = 56;
	/** Screens at least this wide show the bottom sheet centered with {@link #BOTTOM_SHEET_WIDTH_DP} (values-w600dp). */
	private static final float CENTERED_BOTTOM_SHEET_MIN_SCREEN_WIDTH_DP = 600;
	private static final float BOTTOM_SHEET_WIDTH_DP = 320;
	/** Minimap height relative to the screen width if it is placed at the top (portrait). */
	private static final float MINIMAP_TOP_WIDTH_FRACTION = 0.35f;
	/** Minimap height relative to the screen height if it is placed at the bottom (landscape). */
	private static final float MINIMAP_BOTTOM_HEIGHT_FRACTION = 0.3f;
	/** Smallest minimap height in dp that is still usable next to the bottom sheet. */
	private static final float MINIMAP_MIN_HEIGHT_DP = 64;

	private final ControlsAdapter controlsAdapter;
	private final float density;

	private ActionFireable actionFireable;
	private MapDrawContext context;

	private volatile boolean minimapVisible;
	/** Created lazily on the GL thread the first time it is shown. */
	private volatile Minimap minimap;
	private volatile MinimapLayout minimapLayout;
	private volatile MapRectangle mapViewport;

	private ISelectionSet selection;

	/**
	 * A task is something that requires multiple steps. E.g. show constructions markers, then choose build location. If an action is part of a task then store it so we know how to react when the next
	 * action comes in.
	 */
	private IAction taskAction;

	public AndroidControls(ControlsAdapter controlsAdapter, float density, boolean minimapVisible) {
		this.controlsAdapter = controlsAdapter;
		this.density = density;
		this.minimapVisible = minimapVisible;
	}

	public boolean isMinimapVisible() {
		return minimapVisible;
	}

	public void setMinimapVisible(boolean minimapVisible) {
		this.minimapVisible = minimapVisible;
	}

	/**
	 * The action is being sent to the game, here we just store it if it's part of a task. Depending on what's active and what's coming in we may need to start a new task, update the current task or
	 * end the current task.
	 */
	@Override
	public void action(IAction action) {
		switch (action.getActionType()) {
		case SHOW_CONSTRUCTION_MARK:
			ShowConstructionMarksAction showConstructionMarksAction = (ShowConstructionMarksAction) action;
			if (showConstructionMarksAction.getBuildingType() != null) { // null means dismissing the construction markers, so is not awaiting further actions
				if (taskAction != null && taskAction.getActionType() == EActionType.SHOW_CONSTRUCTION_MARK) {
					updateTask(action);
				} else {
					startTask(action);
				}
			}
			break;
		case ASK_SET_WORK_AREA:
		case ASK_CAST_SPELL:
		case ASK_SET_DOCK:
		case ASK_SET_TRADING_WAYPOINT:
			startTask(action);
			break;
		case SET_WORK_AREA:
		case CAST_SPELL:
		case SET_DOCK:
		case SET_TRADING_WAYPOINT:
		case BUILD:
		case ABORT:
			endTask();
			break;
		}

		controlsAdapter.onAction(action);
	}

	/**
	 * Replace the action based on the current task/selection. E.g SELECT_POINT may be choosing a build location or moving soldiers depending on what's current
	 */
	@Override
	public IAction replaceAction(IAction action) {
		if (action.getActionType() == EActionType.SELECT_POINT) {
			PointAction pointAction = (PointAction) action;

			if (taskAction != null) {
				switch (taskAction.getActionType()) {
				case SHOW_CONSTRUCTION_MARK:
					ShowConstructionMarksAction showConstructionMarksAction = (ShowConstructionMarksAction) taskAction;
					return new BuildAction(showConstructionMarksAction.getBuildingType(), pointAction.getPosition());
				case ASK_SET_WORK_AREA:
					return new PointAction(EActionType.SET_WORK_AREA, pointAction.getPosition());
				case ASK_CAST_SPELL:
					return new CastSpellAction(((AskCastSpellAction)taskAction).getSpell(), pointAction.getPosition());
				case ASK_SET_DOCK:
					return new SetDockAction(pointAction.getPosition());
				case ASK_SET_TRADING_WAYPOINT:
					AskSetTradingWaypointAction askSetTradingWaypointAction = (AskSetTradingWaypointAction) taskAction;
					return new SetTradingWaypointAction(askSetTradingWaypointAction.getWaypoint(), pointAction.getPosition());
				}
			}

			// if (selection != null && selection.getSize() > 0 && (selection.getSelectionType() == ESelectionType.SOLDIERS || selection.getSelectionType() == ESelectionType.SPECIALISTS)) {
			// return new PointAction(EActionType.MOVE_TO, pointAction.getPosition());
			// }
		} else if (action.getActionType() == EActionType.MOVE_TO) {
			PointAction pointAction = (PointAction) action;

			if (selection == null || selection.getSize() == 0) {
				return null;
			}

			// there are no modifier keys on touch screens => ask the user how the units should move
			if (!(action instanceof ChosenMoveToAction) && canChooseMoveToType(selection)) {
				controlsAdapter.requestMoveToType(pointAction.getPosition());
				return null;
			}
		}

		return action;
	}

	private static boolean canChooseMoveToType(ISelectionSet selection) {
		ESelectionType selectionType = selection.getSelectionType();
		return selectionType == ESelectionType.SOLDIERS || selectionType == ESelectionType.SPECIALISTS;
	}

	@Override
	public void displaySelection(ISelectionSet selection) {
		this.selection = selection;
		endTask();
		controlsAdapter.onSelection(selection);
	}

	@Override
	public void drawAt(GLDrawContext gl) {
		MinimapLayout layout = minimapLayout;
		if (minimapVisible && context != null && layout != null) {
			if (minimap == null) {
				Minimap newMinimap = new Minimap(context, new MinimapMode());
				newMinimap.setSize(layout.width, layout.height);
				if (mapViewport != null) {
					newMinimap.setMapViewport(mapViewport);
				}
				minimap = newMinimap;
			}
			minimap.draw(gl, layout.left, layout.bottom);
		}

		controlsAdapter.onDraw();
	}

	/**
	 * Places the minimap so that it is not hidden by the Android views drawn on top of the map: In portrait the bottom sheet uses the whole width, so the minimap goes to the top left below the
	 * toolbar. In landscape the bottom sheet is centered, so the minimap goes to the bottom left next to it.
	 */
	@Override
	public void resizeTo(float newWidth, float newHeight) {
		if (context == null) {
			return;
		}

		IGraphicsGrid map = context.getMap();
		float stride = getMinimapStride();
		// width of the minimap parallelogram's bounding box relative to its height
		float boundingBoxRatio = (1 + stride) * map.getWidth() / map.getHeight();
		float margin = MINIMAP_MARGIN_DP * density;
		float maxHeight = MINIMAP_MAX_HEIGHT_DP * density;

		MinimapLayout layout = null;
		boolean landscape = newWidth >= newHeight;
		if (landscape && newWidth / density >= CENTERED_BOTTOM_SHEET_MIN_SCREEN_WIDTH_DP) {
			float freeWidth = (newWidth - BOTTOM_SHEET_WIDTH_DP * density) / 2 - 2 * margin;
			float height = Math.min(Math.min(newHeight * MINIMAP_BOTTOM_HEIGHT_FRACTION, maxHeight), freeWidth / boundingBoxRatio);
			if (height >= MINIMAP_MIN_HEIGHT_DP * density) {
				layout = new MinimapLayout(margin, margin, height, map);
			}
		}

		if (layout == null) {
			float height = Math.min(Math.min(newWidth * MINIMAP_TOP_WIDTH_FRACTION, maxHeight), newHeight * MINIMAP_BOTTOM_HEIGHT_FRACTION);
			height = Math.min(height, (newWidth - 2 * margin) / boundingBoxRatio);
			float bottom = newHeight - TOOLBAR_HEIGHT_DP * density - margin - height;
			layout = new MinimapLayout(margin, bottom, height, map);
		}

		minimapLayout = layout;
		if (minimap != null) {
			minimap.setSize(layout.width, layout.height);
		}
	}

	@Override
	public boolean containsPoint(UIPoint position) {
		MinimapLayout layout = minimapLayout;
		if (!minimapVisible || minimap == null || layout == null) {
			return false;
		}

		float relativeY = (float) (position.getY() - layout.bottom) / layout.height;
		float relativeX = (float) (position.getX() - layout.left - getMinimapStride() * layout.width * relativeY) / layout.width;
		return relativeX >= 0 && relativeX <= 1 && relativeY >= 0 && relativeY <= 1;
	}

	@Override
	public String getDescriptionFor(UIPoint position) {
		return null;
	}

	@Override
	public void setMapViewport(MapRectangle screenArea, ShortPoint2D displayCenter) {
		mapViewport = screenArea;
		if (minimap != null) {
			minimap.setMapViewport(screenArea);
		}
		controlsAdapter.onPositionChanged(screenArea, displayCenter);
	}

	@Override
	public Optional<Action> getActionForMoveTo(UIPoint position, EMoveToType moveToType) {
		return getMinimapPosition(position).map(mapPosition -> new MoveToAction(moveToType, mapPosition));
	}

	@Override
	public Optional<Action> getActionForSelect(UIPoint position) {
		return getMinimapPosition(position).map(mapPosition -> new PointAction(EActionType.PAN_TO, mapPosition));
	}

	@Override
	public boolean handlePanEvent(GOPanEvent event) {
		if (minimap == null) {
			return false;
		}
		event.setHandler(new MinimapScrollHandler());
		return true;
	}

	@Override
	public boolean handleDrawEvent(GODrawEvent event) {
		if (!containsPoint(event.getDrawPosition())) {
			return false;
		}
		event.setHandler(new MinimapScrollHandler());
		return true;
	}

	@Override
	public void setDrawContext(ActionFireable actionFireable, MapDrawContext context) {
		this.actionFireable = actionFireable;
		this.context = context;
	}

	@Override
	public String getMapTooltip(ShortPoint2D point) {
		return null;
	}

	@Override
	public void stop() {
		if (minimap != null) {
			minimap.stop();
		}
	}

	private float getMinimapStride() {
		int mapWidth = context.getMap().getWidth();
		return MiniMapLayoutProperties.getStride(mapWidth) / mapWidth;
	}

	private Optional<ShortPoint2D> getMinimapPosition(UIPoint position) {
		Minimap currentMinimap = minimap;
		MinimapLayout layout = minimapLayout;
		if (currentMinimap == null || layout == null) {
			return Optional.empty();
		}

		float relativeX = (float) (position.getX() - layout.left) / layout.width;
		float relativeY = (float) (position.getY() - layout.bottom) / layout.height;
		return Optional.ofNullable(currentMinimap.getClickPositionIfOnMap(relativeX, relativeY));
	}

	/**
	 * Position and size of the minimap in screen pixels, origin is bottom left.
	 */
	private static final class MinimapLayout {
		final float left;
		final float bottom;
		final int width;
		final int height;

		MinimapLayout(float left, float bottom, float height, IGraphicsGrid map) {
			this.left = left;
			this.bottom = bottom;
			this.height = Math.max(1, (int) height);
			this.width = Math.max(1, (int) (height * map.getWidth() / map.getHeight()));
		}
	}

	/**
	 * Moves the map while the user drags a finger over the minimap.
	 */
	private class MinimapScrollHandler implements GOModalEventHandler {
		@Override
		public void phaseChanged(GOEvent event) {
		}

		@Override
		public void finished(GOEvent event) {
			eventDataChanged(event);
		}

		@Override
		public void aborted(GOEvent event) {
		}

		@Override
		public void eventDataChanged(GOEvent event) {
			UIPoint current;
			if (event instanceof GODrawEvent) {
				current = ((GODrawEvent) event).getDrawPosition();
			} else {
				GOPanEvent panEvent = (GOPanEvent) event;
				UIPoint center = panEvent.getPanCenter();
				UIPoint distance = panEvent.getPanDistance();
				current = new UIPoint(center.getX() + distance.getX(), center.getY() + distance.getY());
			}
			getMinimapPosition(current).ifPresent(context::scrollTo);
		}
	}

	/**
	 * ActionFireable implementation
	 */
	@Override
	public void fireAction(IAction action) {
		actionFireable.fireAction(action);
	}

	/**
	 * TaskControls implementation
	 *
	 * @return
	 */
	@Override
	public boolean isTaskActive() {
		return taskAction != null && taskAction.getActionType() != EActionType.MOVE_TO;
	}

	@Override
	public void endTask() {
		if (taskAction != null) {
			switch (taskAction.getActionType()) {
			case SHOW_CONSTRUCTION_MARK:
				fireAction(new ShowConstructionMarksAction(null));
				break;
			default:
				fireAction(new Action(EActionType.ABORT));
				break;
			}

			taskAction = null;
		}
	}

	private void startTask(IAction action) {
		endTask();
		taskAction = action;
	}

	private void updateTask(IAction action) {
		taskAction = action;
	}
}
