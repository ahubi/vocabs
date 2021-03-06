package com.babasoft.vocabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by sonu on 08/02/17.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private Observable mObservers = new FragmentObserver();
    private final List<Fragment> mFragmentList = new ArrayList<>();//fragment array list
    private final List<String> mFragmentTitleList = new ArrayList<>();//title array list

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void updateTitleBar(int position) {
        Log.d(getClass().getName(), "getItem:" + position);
        mObservers.deleteObservers(); // Clear existing observers.
        Fragment fragment = mFragmentList.get(position);
        if(fragment instanceof Observer)
            mObservers.addObserver((Observer) fragment);

        mObservers.notifyObservers();
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    //adding fragments and title method
    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
