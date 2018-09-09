package com.babasoft.vocabs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class Main extends AppCompatActivity {
    int mTabSelection = 0;
    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MemoryCard(), getString(R.string.MemoryCard));
        adapter.addFrag(new MultipleChoice(), getString(R.string.MultipleChoice));
        adapter.addFrag(new RecyclerViewFragment(), getString(R.string.WordLists));
        adapter.addFrag(new TranslateFragment(), getString(R.string.Translate));
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);//setting tab over viewpager
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabSelection = tab.getPosition();
                mViewPager.setCurrentItem(mTabSelection);
                ViewPagerAdapter viewPagerAdapter = (ViewPagerAdapter) mViewPager.getAdapter();
                viewPagerAdapter.updateTitleBar(mTabSelection);
                Log.d("onTabSelected", "tab:" + mTabSelection);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    @Override
    protected void onPause() {
        Prefs.setLastTime(this, Prefs.getDateTime());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("SelectedTab", mTabSelection);
        editor.commit();
        Log.d("onPause", "tab:" + mTabSelection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int selectedTab = prefs.getInt("SelectedTab", 0);
        Log.d("onResume", "tab:" + selectedTab);
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setCurrentItem(selectedTab);
    }
}