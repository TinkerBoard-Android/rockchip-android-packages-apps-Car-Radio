/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.radio;

import android.content.Context;
import android.hardware.radio.RadioManager.ProgramInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.car.widget.DayNightStyle;
import androidx.car.widget.PagedListView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.broadcastradio.support.Program;
import com.android.car.radio.storage.RadioStorage;

import java.util.List;

/**
 * Fragment that shows all browseable radio stations from background scan
 */
public class BrowseFragment extends Fragment {

    private RadioController mRadioController;
    private BrowseAdapter mBrowseAdapter;
    private RadioStorage mRadioStorage;
    private View mRootView;
    private PagedListView mBrowseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.browse_fragment, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Context context = getContext();

        mRadioStorage = RadioStorage.getInstance(context);
        mBrowseAdapter = new BrowseAdapter(this, mRadioController.getCurrentProgram(),
                mRadioStorage.getFavorites());

        mBrowseAdapter.setOnItemClickListener(mRadioController::tune);
        mBrowseAdapter.setOnItemFavoriteListener(this::handlePresetItemFavoriteChanged);

        mBrowseList = view.findViewById(R.id.browse_list);
        mBrowseList.setDayNightStyle(DayNightStyle.ALWAYS_LIGHT);
        mBrowseList.setAdapter(mBrowseAdapter);
        RecyclerView recyclerView = mBrowseList.getRecyclerView();
        recyclerView.setVerticalFadingEdgeEnabled(true);
        recyclerView.setFadingEdgeLength(getResources()
                .getDimensionPixelSize(R.dimen.car_padding_4));

        mRadioController.addServiceConnectedListener(this::updateProgramList);
        updateProgramList();
    }

    private void handlePresetItemFavoriteChanged(Program program, boolean saveAsFavorite) {
        if (saveAsFavorite) {
            mRadioStorage.addFavorite(program);
        } else {
            mRadioStorage.removeFavorite(program.getSelector());
        }
    }

    static BrowseFragment newInstance(RadioController radioController) {
        BrowseFragment fragment = new BrowseFragment();
        fragment.mRadioController = radioController;
        return fragment;
    }

    private void updateProgramList() {
        List<ProgramInfo> plist = mRadioController.getProgramList();
        if (plist != null) mBrowseAdapter.setProgramList(plist);
    }
}