package com.example.eventbooking.view.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventbooking.R;
import com.example.eventbooking.controller.adapter.MoreAdapter;
import com.example.eventbooking.model.MoreModel;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends Fragment {

    private View mMainView;
    private RecyclerView mMenusList;
    private GridLayoutManager mManager;
    private List<MoreModel> mDataList;
    private MoreAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_more, container, false);

        mMenusList = mMainView.findViewById(R.id.menusList);
        mManager = new GridLayoutManager(container.getContext(), 2);
        mMenusList.setHasFixedSize(true);
        mMenusList.setLayoutManager(mManager);

        mDataList = new ArrayList<>();
        mDataList.add(new MoreModel("Booking", R.drawable.book_icon));
        mDataList.add(new MoreModel("Record Details", R.drawable.record_icon));
        mDataList.add(new MoreModel("Employee Details", R.drawable.employee_icon));
        mDataList.add(new MoreModel("Feebacks", R.drawable.feedback_icon));
        mDataList.add(new MoreModel("Help & Support", R.drawable.help_icon));
        mDataList.add(new MoreModel("Log Out", R.drawable.logout_icon));

        mAdapter = new MoreAdapter(mDataList, container.getContext());
        mMenusList.setAdapter(mAdapter);

        return mMainView;
    }

}
