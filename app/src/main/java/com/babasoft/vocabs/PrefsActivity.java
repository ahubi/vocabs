package com.babasoft.vocabs;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.babasoft.vocabs.R;
import com.babasoft.vocabs.ColorPicker.OnColorChangedListener;

public class PrefsActivity extends PreferenceActivity {

    private final OnColorChangedListener colorCL = new OnColorChangedListener() {
        public void colorChanged(int c) {
            Prefs.setButtoncolor(getBaseContext(), c);
        }
    };
    private final void onButtonColorClicked(){
        ColorPicker cp = new ColorPicker(this,
                colorCL, Prefs.getButtoncolor(this));
        cp.show();
    }

    private final OnColorChangedListener txCL = new OnColorChangedListener() {
        public void colorChanged(int color) {
            Prefs.setTextcolor(getBaseContext(), color);
        }
    };
    private final void onTextColorClicked(){
        ColorPicker cp = new ColorPicker(this,
                txCL, Prefs.getTextcolor(this));
        cp.show();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        Preference customPref = findPreference("ButtonColor");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    public boolean onPreferenceClick(Preference preference) {
                        onButtonColorClicked();
                        return true;
                    }

                });
        
        Preference customPref2 = findPreference("ButtonTextColor");
        if (customPref2 != null) {
            customPref2.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {
                            onTextColorClicked();
                            return true;
                        }

                    });
        }
    }
    


}
