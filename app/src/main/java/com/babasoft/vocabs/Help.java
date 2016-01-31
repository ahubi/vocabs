package com.babasoft.vocabs;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class Help extends Activity {
    
    public Help(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        TextView txView = (TextView) findViewById(R.id.helpTextView);
        Spanned sp = Html.fromHtml(getString(R.string.HelpString));
        txView.setText(sp);
    }
        
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
