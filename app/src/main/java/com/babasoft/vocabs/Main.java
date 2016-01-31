package com.babasoft.vocabs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;

public class Main extends ActionBarActivity{
	// menu IDs
	private static final int LISTS = Menu.FIRST+1; // Wortlisten auswaehlen/anlegen/loeschen
	private static final int STATS = Menu.FIRST+4; // Wortlisten auswaehlen/anlegen/loeschen
	private static final int PREFS = Menu.FIRST+5; // Einstellung fuer Sound
	private static final int HELP  = Menu.FIRST+6; // Einstellung fuer Sound
	ActionBar actionBar;
	Tab tabMemoryCard, tabMultipleChoice, tabWordLists;
	Fragment fragMemoryCard, fragMultipleChoice, fragWordLists;
	public Boolean savedInstance;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // Notice that setContentView() is not used, because we use the root
	    // android.R.id.content as the container for each fragment
	    actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowHomeEnabled(false);
	    actionBar.setDisplayShowTitleEnabled(true);
	    
	    if(tabMemoryCard==null){
	        //SpannableString s = new SpannableString(getString(R.string.MemoryCard));
	        //s.setSpan(new TypefaceSpan("monospace"), 0, s.length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
	        tabMemoryCard = actionBar.newTab()
	                      .setText(R.string.MemoryCard)
	                      .setTabListener(new TabListener<MemoryCard>(this, "MemoryCard", MemoryCard.class));
	        actionBar.addTab(tabMemoryCard);
	    }
	    
	    if(tabMultipleChoice==null){
	        tabMultipleChoice  = actionBar.newTab()
                                .setText(getString(R.string.MultipleChoice))
                                .setTabListener(new TabListener<MultipleChoice>(this, "MultipleChoice", MultipleChoice.class));
	        actionBar.addTab(tabMultipleChoice);
	    }
	    if(tabWordLists==null){
	        tabWordLists = actionBar.newTab()
                          .setText(getString(R.string.WordLists))
                          .setTabListener(new TabListener<WordLists>(this, "WordLists", WordLists.class));
	        actionBar.addTab(tabWordLists);
	    }
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
	    
   	@Override
    public void onStop() {
        super.onStop();
    }
   	
   	@Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int ret = prefs.getInt("SelectedTab", 0);
        switch (ret) {
        case 0:
            actionBar.selectTab(tabMemoryCard);
            break;
        case 1:
            actionBar.selectTab(tabMultipleChoice);
            break;
        case 2:
            actionBar.selectTab(tabWordLists);
            break;
        default:
            actionBar.selectTab(tabMemoryCard);
            break;
        }
        
    }

    @Override
    protected void onPause() {
        Prefs.setLastTime(this, Prefs.getDateTime());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("SelectedTab", actionBar.getSelectedTab().getPosition());
        editor.commit();
        super.onPause();
    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        mFragment=((FragmentActivity) mActivity).getSupportFragmentManager().findFragmentByTag(mTag);
	        if (mFragment == null) {
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.add(android.R.id.content, mFragment, mTag);
	        }      
	        else {
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            ft.detach(mFragment);
	        }
	    }
	    public void onTabReselected(Tab tab, FragmentTransaction ft) {}
	    
	}

    /**
     * Auswahl eines Menue-Eintrags
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case PREFS:
            startActivity(new Intent(this, PrefsActivity.class));
            break;
        case STATS:
            startActivity(new Intent(this, Stats.class));
            break;
        case HELP:
            startActivity(new Intent(this, Help.class));
            break;
        }

        return super.onOptionsItemSelected(item);
    }

/**
 * Wird nach Beenden einer per <code>startActivityForResult</code> gestarteten 
 * Activity gerufen	
 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LISTS && resultCode == RESULT_OK) {
			// eine andere Liste wurde ausgewaehlt
			long id = data.getExtras().getLong(WordLists.ID);
			Prefs.setID(this, id);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	 public boolean onSearchRequested() {
	    Intent intent = new Intent(this, EditWordList.class);
        intent.putExtra(EditWordList.ID, -1); // übergebe aktuelle ID
        startActivity(intent); 
	    return false;  // don't go ahead and show the search box
	 }
}