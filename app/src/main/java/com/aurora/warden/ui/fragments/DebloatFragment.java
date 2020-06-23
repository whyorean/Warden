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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.warden.AuroraApplication;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.data.model.DebloatProfile;
import com.aurora.warden.data.model.items.BloatItem;
import com.aurora.warden.services.DebloatService;
import com.aurora.warden.ui.activities.AppDetailsActivity;
import com.aurora.warden.ui.custom.view.ViewFlipper2;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.viewmodel.AppViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DebloatFragment extends Fragment {

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.fab_debloat)
    ExtendedFloatingActionButton fabDebloat;

    private AppViewModel model;
    private FastAdapter<BloatItem> fastAdapter;
    private ItemAdapter<BloatItem> itemAdapter;
    private SelectExtension<BloatItem> selectExtension;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_debloat, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();

        Bundle bundle = getArguments();
        if (bundle != null) {
            String stringExtra = bundle.getString(Constants.STRING_EXTRA, "");
            DebloatProfile debloatProfile = new Gson().fromJson(stringExtra, DebloatProfile.class);

            model = new ViewModelProvider(this).get(AppViewModel.class);
            model.getBloatAppList().observe(getViewLifecycleOwner(), this::dispatchAppsToAdapter);
            swipeContainer.setOnRefreshListener(() -> model.fetchAllApps(debloatProfile.getPackages()));

            if (debloatProfile != null) {
                model.fetchAllApps(debloatProfile.getPackages());
            }
        }

        if (!AuroraApplication.isRooted()) {
            fabDebloat.hide();
        }
    }

    @OnClick(R.id.fab_debloat)
    public void debloat() {
        fabDebloat.setEnabled(false);

        final Set<BloatItem> selectedItems = selectExtension.getSelectedItems();
        final Set<String> packageNameSet = new HashSet<>();

        for (BloatItem bloatItem : selectedItems) {
            packageNameSet.add(bloatItem.getApp().getPackageName());
        }

        if (!DebloatService.isServiceRunning()) {
            Intent intent = new Intent(requireContext(), DebloatService.class);
            intent.putExtra(Constants.STRING_EXTRA, new Gson().toJson(packageNameSet));
            requireContext().startService(intent);
        }
    }

    private void dispatchAppsToAdapter(List<BloatItem> appItems) {
        final FastAdapterDiffUtil fastAdapterDiffUtil = FastAdapterDiffUtil.INSTANCE;
        final DiffUtil.DiffResult diffResult = fastAdapterDiffUtil.calculateDiff(itemAdapter, appItems);

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
        selectExtension = new SelectExtension<>(fastAdapter);
        fastAdapter.addExtension(selectExtension);
        fastAdapter.addEventHook(new BloatItem.CheckBoxClickEvent());

        selectExtension.setSelectWithItemUpdate(true);
        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            //item.setSelected(!selected);
        });

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final Intent intent = new Intent(requireContext(), AppDetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, item.getApp().getPackageName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        fastAdapter.addAdapter(0, itemAdapter);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recycler.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down));
        recycler.setAdapter(fastAdapter);
    }
}