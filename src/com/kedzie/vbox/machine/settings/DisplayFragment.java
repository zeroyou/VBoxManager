package com.kedzie.vbox.machine.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.kedzie.vbox.R;
import com.kedzie.vbox.app.FragmentElement;
import com.kedzie.vbox.app.PagerTabHost;

public class DisplayFragment extends SherlockFragment {
    private PagerTabHost mTabHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTabHost = new PagerTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(new FragmentElement("Video", DisplayVideoFragment.class, getArguments()));
        mTabHost.addTab(new FragmentElement("Remote", DisplayRemoteFragment.class, getArguments()));
        return mTabHost;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }
}

