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

import java.util.function.ToIntFunction;

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
import android.widget.ImageView;
import android.widget.TextView;

import jsettlers.common.images.ImageLink;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.player.SettlerStatistics;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.draw.ECommonLinkType;
import jsettlers.graphics.map.draw.ImageLinkMap;
import jsettlers.main.android.R;
import jsettlers.main.android.core.controls.ControlsResolver;
import jsettlers.main.android.core.resources.OriginalImageProvider;

/**
 * Page of the settlers menu that shows how many settlers of which kind the player has.
 */
@EFragment(R.layout.menu_settlers_statistics)
public class SettlersStatisticsFragment extends Fragment {
	public static SettlersStatisticsFragment newInstance() {
		return new SettlersStatisticsFragment_();
	}

	/**
	 * One entry of the statistics, the same as in the desktop version.
	 */
	private static class StatisticEntry {
		final ImageLink image;
		final String name;
		final ToIntFunction<SettlerStatistics> value;

		StatisticEntry(ImageLink image, String name, ToIntFunction<SettlerStatistics> value) {
			this.image = image;
			this.name = name;
			this.value = value;
		}
	}

	@ViewById(R.id.textView_title)
	TextView titleTextView;
	@ViewById(R.id.recyclerView)
	RecyclerView recyclerView;

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		titleTextView.setText(Labels.getString("settler_stats_title"));

		ECivilisation civilisation = new ControlsResolver(getActivity()).getPlayer().getCivilisation();
		StatisticsAdapter adapter = new StatisticsAdapter(getActivity(), createEntries(civilisation, getString(R.string.settlers_statistics_total)));
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(adapter);

		StatisticsViewModel viewModel = ViewModelProviders.of(this, new StatisticsViewModel.Factory(getActivity())).get(StatisticsViewModel.class);
		viewModel.getStatistics().observe(this, adapter::setStatistics);
	}

	private static StatisticEntry[] createEntries(ECivilisation civilisation, String totalName) {
		return new StatisticEntry[] {
				new StatisticEntry(ImageLink.fromName("original_3_GUI_393", 0), Labels.getString("settler_stats_bed"), SettlerStatistics::getBeds),
				new StatisticEntry(ImageLink.fromName("original_3_GUI_126", 0), Labels.getString("settler_stats_civilian"), SettlerStatistics::getCivilians),
				new StatisticEntry(ImageLinkMap.get(civilisation, ECommonLinkType.STATISTIC_SOLDIERS, EMovableType.SWORDSMAN_L1), Labels.getString("settler_stats_soldier"),
						SettlerStatistics::getSoldiers),
				new StatisticEntry(ImageLink.fromName("original_3_GUI_3", 0), totalName, SettlerStatistics::getTotal),
				settlerEntry(civilisation, EMovableType.BEARER, SettlerStatistics::getBearers),
				settlerEntry(civilisation, EMovableType.DIGGER, SettlerStatistics::getDiggers),
				settlerEntry(civilisation, EMovableType.BRICKLAYER, SettlerStatistics::getBricklayers),
				new StatisticEntry(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.SMITH), Labels.getString("settler_stats_worker"),
						SettlerStatistics::getWorkers),
				new StatisticEntry(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.SWORDSMAN_L1), Labels.getString("soldier_SWORDSMAN"),
						SettlerStatistics::getSwordsmen),
				new StatisticEntry(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.BOWMAN_L1), Labels.getString("soldier_BOWMAN"),
						SettlerStatistics::getBowmen),
				new StatisticEntry(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, EMovableType.PIKEMAN_L1), Labels.getString("soldier_PIKEMAN"),
						SettlerStatistics::getPikemen),
				settlerEntry(civilisation, EMovableType.MAGE, SettlerStatistics::getMages),
				settlerEntry(civilisation, EMovableType.GEOLOGIST, SettlerStatistics::getGeologists),
				settlerEntry(civilisation, EMovableType.THIEF, SettlerStatistics::getThieves),
				settlerEntry(civilisation, EMovableType.PIONEER, SettlerStatistics::getPioneers),
				new StatisticEntry(ImageLink.fromName("original_3_GUI_0", 0), Labels.getName(EMovableType.DONKEY), SettlerStatistics::getDonkeys)
		};
	}

	private static StatisticEntry settlerEntry(ECivilisation civilisation, EMovableType type, ToIntFunction<SettlerStatistics> value) {
		return new StatisticEntry(ImageLinkMap.get(civilisation, ECommonLinkType.SETTLER_GUI, type), Labels.getName(type), value);
	}

	/**
	 * RecyclerView adapter
	 */
	private static class StatisticsAdapter extends RecyclerView.Adapter<StatisticViewHolder> {
		private final LayoutInflater layoutInflater;
		private final StatisticEntry[] entries;
		private SettlerStatistics statistics;

		StatisticsAdapter(Activity activity, StatisticEntry[] entries) {
			this.layoutInflater = LayoutInflater.from(activity);
			this.entries = entries;
		}

		@Override
		public int getItemCount() {
			return entries.length;
		}

		@Override
		public StatisticViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new StatisticViewHolder(layoutInflater.inflate(R.layout.vh_settler_statistic, parent, false));
		}

		@Override
		public void onBindViewHolder(StatisticViewHolder holder, int position) {
			holder.bind(entries[position], statistics);
		}

		void setStatistics(SettlerStatistics statistics) {
			this.statistics = statistics;
			notifyItemRangeChanged(0, entries.length);
		}
	}

	/**
	 * RecyclerView ViewHolder
	 */
	private static class StatisticViewHolder extends RecyclerView.ViewHolder {
		private final ImageView imageView;
		private final TextView nameTextView;
		private final TextView countTextView;
		private StatisticEntry entry;

		StatisticViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView_settler);
			nameTextView = itemView.findViewById(R.id.textView_name);
			countTextView = itemView.findViewById(R.id.textView_count);
		}

		void bind(StatisticEntry entry, SettlerStatistics statistics) {
			if (this.entry != entry) {
				this.entry = entry;
				OriginalImageProvider.get(entry.image).setAsImage(imageView);
				nameTextView.setText(entry.name);
			}
			countTextView.setText(statistics == null ? "" : String.valueOf(entry.value.applyAsInt(statistics)));
		}
	}
}
