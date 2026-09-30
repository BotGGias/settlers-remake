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
package jsettlers.main.android.gameplay.controlsmenu.selection.features;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import jsettlers.common.action.Action;
import jsettlers.common.action.EActionType;
import jsettlers.common.action.SetAmmoProductionAction;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.images.ImageLink;
import jsettlers.common.movable.ESiegeWeaponType;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.controls.original.panel.selection.BuildingState;
import jsettlers.graphics.map.draw.ECommonLinkType;
import jsettlers.graphics.map.draw.ImageLinkMap;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ActionControls;
import jsettlers.main.android.core.controls.DrawControls;
import jsettlers.main.android.core.controls.DrawListener;
import jsettlers.main.android.core.resources.OriginalImageProvider;
import jsettlers.main.android.gameplay.navigation.MenuNavigator;

/**
 * Lets the player order siege weapons in a siege workshop and switch its ammunition production on or off.
 */
public class SiegeWorkshopFeature extends SelectionFeature implements DrawListener {
	private final DrawControls drawControls;

	private final ImageView orderImageView;
	private final TextView orderedTextView;
	private final boolean usesAmmo;
	private final TextView ammoTextView;
	private final CheckBox ammoProductionCheckBox;

	public SiegeWorkshopFeature(View view, IBuilding building, MenuNavigator menuNavigator, DrawControls drawControls, ActionControls actionControls) {
		super(view, building, menuNavigator);
		this.drawControls = drawControls;

		IBuilding.ISiegeWorkshop workshop = (IBuilding.ISiegeWorkshop) building;
		ImageLink weaponIcon = ImageLinkMap.get(building.getPlayer().getCivilisation(), ECommonLinkType.SETTLER_GUI, workshop.getSiegeWeaponType().movableType);
		if (weaponIcon == null) {
			weaponIcon = building.getBuildingVariant().getGuiImage();
		}

		orderImageView = (ImageView) getView().findViewById(R.id.imageView_orderSiegeWeapon);
		orderImageView.setOnClickListener(v -> actionControls.fireAction(new Action(EActionType.ORDER_SIEGE_WEAPON)));
		OriginalImageProvider.get(weaponIcon).setAsImage(orderImageView);

		orderedTextView = (TextView) getView().findViewById(R.id.text_view_ordered_siege_weapons);

		usesAmmo = workshop.getSiegeWeaponType().usesAmmo();
		ammoTextView = (TextView) getView().findViewById(R.id.text_view_siege_ammo);
		ammoProductionCheckBox = (CheckBox) getView().findViewById(R.id.checkbox_ammo_production);
		ammoProductionCheckBox.setText(Labels.getString("siege_ammo_production"));
		// the check box has already changed its state when the click listener is called
		ammoProductionCheckBox.setOnClickListener(v -> actionControls.fireAction(new SetAmmoProductionAction(ammoProductionCheckBox.isChecked())));
	}

	@Override
	public void initialize(BuildingState buildingState) {
		super.initialize(buildingState);
		drawControls.addInfrequentDrawListener(this);

		update();
	}

	@Override
	public void finish() {
		super.finish();
		drawControls.removeInfrequentDrawListener(this);
	}

	@Override
	public void draw() {
		if (hasNewState()) {
			getView().post(this::update);
		}
	}

	private void update() {
		if (getBuildingState().isConstruction()) {
			return;
		}

		orderImageView.setVisibility(View.VISIBLE);
		orderedTextView.setVisibility(View.VISIBLE);
		orderedTextView.setText(Labels.getString("siege_weapons_ordered", getBuildingState().getOrderedSiegeWeapons()));

		if (usesAmmo) {
			ammoTextView.setVisibility(View.VISIBLE);
			ammoTextView.setText(Labels.getString("siege_weapon_ammo", getBuildingState().getAmmoStackSize(), ESiegeWeaponType.AMMO_STACK_CAPACITY));
			ammoProductionCheckBox.setVisibility(View.VISIBLE);
			ammoProductionCheckBox.setChecked(getBuildingState().isAmmoProductionEnabled());
		}
	}
}
