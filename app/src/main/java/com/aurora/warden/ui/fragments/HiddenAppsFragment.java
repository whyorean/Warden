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

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.warden.R;
import com.aurora.warden.data.model.HiddenApp;
import com.aurora.warden.data.model.items.HiddenAppItem;
import com.aurora.warden.manager.HiddenAppsManager;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.ui.custom.view.ViewFlipper2;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.utils.callback.HiddenAppDiffCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HiddenAppsFragment extends Fragment {


    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    private CamtonoManager camtonoManager;
    private HiddenAppsManager hiddenAppsManager;
    private FastAdapter<HiddenAppItem> fastAdapter;
    private ItemAdapter<HiddenAppItem> itemAdapter;

    private CompositeDisposable disposable = new CompositeDisposable();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_hidden_apps, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        camtonoManager = CamtonoManager.getInstance(requireContext());
        hiddenAppsManager = new HiddenAppsManager(requireContext());
        swipeContainer.setOnRefreshListener(() -> fetchHiddenApps());
        fetchHiddenApps();
    }

    private void fetchHiddenApps() {
        disposable.add(Observable.fromCallable(() -> hiddenAppsManager
                .getAll())
                .flatMap(hiddenApps -> Observable.fromIterable(hiddenApps)
                        .sorted((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()))
                        .map(HiddenAppItem::new))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((hiddenAppItems, throwable) -> {
                    dispatchAppsToAdapter(hiddenAppItems);
                }));
    }

    private void dispatchAppsToAdapter(List<HiddenAppItem> items) {
        final FastAdapterDiffUtil fastAdapterDiffUtil = FastAdapterDiffUtil.INSTANCE;
        final HiddenAppDiffCallback diffCallback = new HiddenAppDiffCallback();
        final DiffUtil.DiffResult diffResult = fastAdapterDiffUtil.calculateDiff(itemAdapter, items, diffCallback);
        fastAdapterDiffUtil.set(itemAdapter, diffResult);
        swipeContainer.setRefreshing(false);
        recycler.scheduleLayoutAnimation();

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
            showUnhideAppDialog(item.getApp());
            return false;
        });

        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        recycler.setAdapter(fastAdapter);
    }

    private void showUnhideAppDialog(HiddenApp hiddenApp) {
        int backGroundColor = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorBackground);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), ViewUtil.getBitmapFromRawString(hiddenApp.getRawBitmap()));
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setIcon(bitmapDrawable)
                .setTitle(hiddenApp.getDisplayName())
                .setMessage(R.string.string_unhide_app)
                .setBackground(new ColorDrawable(backGroundColor))
                .setPositiveButton(R.string.action_unhide, (dialog, which) -> {
                    boolean result = camtonoManager.camtono().unhide(hiddenApp.getPackageName());
                    if (result)
                        hiddenAppsManager.remove(hiddenApp.getPackageName());
                })
                .setNegativeButton(R.string.action_later, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}