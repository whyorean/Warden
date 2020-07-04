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

import android.app.Service;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aurora.warden.R;
import com.aurora.warden.ui.custom.layout.AnalyticsLayout;
import com.aurora.warden.utils.ViewUtil;
import com.aurora.warden.viewmodel.ExodusViewModel;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFragment extends Fragment {

    @BindView(R.id.txt_packagename)
    TextInputEditText inputEditText;
    @BindView(R.id.btn_search)
    ImageButton btnSearch;
    @BindView(R.id.view_flipper)
    ViewFlipper viewFlipper;
    @BindView(R.id.layout_shit)
    AnalyticsLayout analyticsLayout;
    @BindView(R.id.layout_search)
    RelativeLayout layoutSearch;

    private ExodusViewModel viewModel;
    private InputMethodManager inputMethodManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Object object = requireContext().getSystemService(Service.INPUT_METHOD_SERVICE);
        inputMethodManager = (InputMethodManager) object;

        final int backGroundColor = ViewUtil.getStyledAttribute(requireContext(), android.R.attr.colorBackground);
        layoutSearch.setBackgroundColor(ColorUtils.setAlphaComponent(backGroundColor, 245));
        analyticsLayout.setType(3);
        viewModel = new ViewModelProvider(requireActivity()).get(ExodusViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), reports -> {
            if (reports.isEmpty()) {
                analyticsLayout.switchState(AnalyticsLayout.State.EMPTY);
            } else {
                analyticsLayout.setTrackerData(reports);
                analyticsLayout.switchState(AnalyticsLayout.State.RESULT);
            }
            btnSearch.setEnabled(true);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            viewFlipper.setDisplayedChild(0);
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

        inputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(inputEditText);
                fetchReport();
            }
            return false;
        });
    }

    @OnClick(R.id.btn_search)
    public void search() {
        hideKeyboard(btnSearch);
        fetchReport();
    }

    @OnClick(R.id.layout_bottom_action)
    public void backToSearch() {
        viewFlipper.setDisplayedChild(0);
    }

    private void fetchReport() {
        Editable query = inputEditText.getText();
        if (StringUtils.isNotEmpty(query)) {
            String packageName = query.toString();
            viewModel.fetchReport(packageName);
            Toast.makeText(requireContext(), R.string.txt_exodus_searching, Toast.LENGTH_SHORT).show();
            viewFlipper.setDisplayedChild(1);
        }
    }

    private void hideKeyboard(View view) {
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}