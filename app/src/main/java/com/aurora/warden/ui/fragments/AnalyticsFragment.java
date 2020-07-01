/*
 * Warden
 * Copyright (C) 2020, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.aurora.warden.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.data.model.exodus.Report;
import com.aurora.warden.data.model.items.LoggerItem;
import com.aurora.warden.data.model.items.TrackerItem;
import com.aurora.warden.data.model.items.base.ListItem;
import com.aurora.warden.manager.StaticDataProvider;
import com.aurora.warden.ui.custom.view.ViewFlipper2;
import com.aurora.warden.ui.sheets.TrackerFullSheet;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AnalyticsFragment extends Fragment {

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    private FastAdapter<ListItem> fastAdapter;
    private ItemAdapter<ListItem> itemAdapter;

    private CompositeDisposable disposable = new CompositeDisposable();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shit, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        if (getArguments() != null) {
            final Bundle bundle = getArguments();
            final int intExtra = bundle.getInt(Constants.INT_EXTRA, 0);
            final String stringExtra = bundle.getString(Constants.STRING_EXTRA, null);
            if (StringUtils.isEmpty(stringExtra))
                populateData(intExtra);
            else
                populateData(stringExtra);
        }
    }

    private void populateData(String stringExtra) {
        Report report = new Gson().fromJson(stringExtra, Report.class);
        if (report != null) {
            Set<Integer> trackerIdSet = new HashSet<>(report.getTrackers());
            disposable.add(Observable.fromCallable(() -> StaticDataProvider.getInstance(requireContext())
                    .getKnownTrackersList().values())
                    .delay(200, TimeUnit.MILLISECONDS)
                    .flatMap(objects -> Observable.fromIterable(objects)
                            .filter(tracker -> trackerIdSet.contains(tracker.getId()))
                            .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                            .map(TrackerItem::new))
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::dispatchAppsToAdapter, Throwable::printStackTrace));
        } else {
            requireActivity().finishAfterTransition();
        }
    }

    private void populateData(int listType) {
        disposable.add(Observable.fromCallable(() -> listType == 0
                ? StaticDataProvider.getInstance(requireContext()).getKnownTrackersList().values()
                : StaticDataProvider.getInstance(requireContext()).getKnownLoggerList().values())
                .flatMap(objects -> Observable.fromIterable(objects)
                        .sorted((o1, o2) -> {
                            if (o1 instanceof Tracker && o2 instanceof Tracker) {
                                return ((Tracker) o1).getName()
                                        .compareToIgnoreCase(((Tracker) o2).getName());
                            } else
                                return ((Logger) o1).getName()
                                        .compareToIgnoreCase(((Logger) o2).getName());
                        })
                        .map(obj -> {
                            if (obj instanceof Tracker)
                                return new TrackerItem((Tracker) obj);
                            else
                                return new LoggerItem((Logger) obj);
                        }))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::dispatchAppsToAdapter, Throwable::printStackTrace));
    }

    private void dispatchAppsToAdapter(List<? extends ListItem> appItems) {
        itemAdapter.add(appItems);
        if (itemAdapter != null && itemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();

        fastAdapter.addAdapter(0, itemAdapter);
        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            if (item instanceof TrackerItem) {
                openTrackerFullSheet(((TrackerItem) item).getTracker());
            }
            return false;
        });

        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        recycler.setAdapter(fastAdapter);

        new FastScrollerBuilder(recycler)
                .useMd2Style()
                .build();
    }

    private void openTrackerFullSheet(Tracker tracker) {
        final FragmentManager fragmentManager = getChildFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(TrackerFullSheet.TAG);
        final TrackerFullSheet bottomSheet = new TrackerFullSheet();
        final Bundle bundle = new Bundle();

        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        bundle.putString(Constants.STRING_EXTRA, new Gson().toJson(tracker));
        bottomSheet.setArguments(bundle);
        fragmentManager.beginTransaction()
                .add(bottomSheet, TrackerFullSheet.TAG)
                .commitAllowingStateLoss();
    }
}