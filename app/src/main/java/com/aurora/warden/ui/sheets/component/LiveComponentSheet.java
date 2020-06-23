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

package com.aurora.warden.ui.sheets.component;

import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.warden.R;
import com.aurora.warden.data.model.items.ComponentItem;
import com.aurora.warden.manager.camtono.CamtonoManager;
import com.aurora.warden.ui.sheets.BaseBottomSheet;
import com.aurora.warden.utils.Log;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Setter;

public class LiveComponentSheet extends BaseBottomSheet {

    public static final String TAG = "ACTIVITY_SHEET";

    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.subtitle)
    AppCompatTextView subtitle;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    @Setter
    private Set<ComponentInfo> componentInfoHashSet = new HashSet<>();
    private FastItemAdapter<ComponentItem> fastAdapter;
    private CamtonoManager camtonoManager;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_component_live, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        camtonoManager = CamtonoManager.getInstance(requireContext());
        title.setText(getString(R.string.title_component));
        subtitle.setText(getString(R.string.title_component_desc));
        setupRecycler();
        populateData();
    }

    @OnClick({R.id.img_close, R.id.btn_close})
    public void closeSheet() {
        dismissAllowingStateLoss();
    }

    @OnClick({R.id.btn_disable_all})
    public void disableAll() {
        CamtonoManager camtonoManager = CamtonoManager.getInstance(requireContext());
        for (ComponentInfo componentInfo : componentInfoHashSet) {
            boolean result = camtonoManager.camtono().disable(componentInfo.packageName, componentInfo.name);
            if (result)
                Log.d("Component changed -> %s : %s", componentInfo.packageName, componentInfo.name);
            else
                Log.e("Component un-changed -> %s : %s", componentInfo.packageName, componentInfo.name);
        }
        dismissAllowingStateLoss();
    }

    private void setupRecycler() {
        fastAdapter = new FastItemAdapter<>();
        recycler.setAdapter(fastAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    private void populateData() {
        disposable.add(Observable.fromIterable(componentInfoHashSet)
                .subscribeOn(Schedulers.io())
                .sorted((o1, o2) -> o1.name.compareToIgnoreCase(o2.name))
                .map(componentInfo -> {
                    boolean isEnabled = camtonoManager.camtono().isEnabled(componentInfo.packageName, componentInfo.name);
                    return new ComponentItem(componentInfo, isEnabled);
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activityItems -> fastAdapter.add(activityItems), throwable -> Log.e(throwable.getMessage())));
    }
}
