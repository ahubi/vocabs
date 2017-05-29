package com.babasoft.vocabs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.babasoft.vocabs.WordDB.WordList;

public class EditList extends Activity {
    private WordDB db;
    public final static String ID = "id"; // Intent parameter
    public final static String LIST_ID = "list_id"; // Intent parameter
    private Long listId = new Long(0);
    WordList mWl;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editlist);
        db = new WordDB(this); // siehe onDestroy
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(ID)) {
            listId = extras.getLong(ID);
        }
        mWl = db.getWordList(listId);
        if (mWl == null) {
            mWl = new WordList();
            mWl.lang1 = "";
            mWl.lang2 = "";
        }

        EditText name = (EditText) findViewById(R.id.et_Name);
        Spinner sp1 = (Spinner) findViewById(R.id.sp_Lang1);
        Spinner sp2 = (Spinner) findViewById(R.id.sp_Lang2);
        name.setText(mWl.title);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, android.R.id.text1,
                SupportedLanguage.getLanguages());
        sp1.setAdapter(adapter);
        sp2.setAdapter(adapter);
        OnItemSelectedListener spinnerListener1 = new myOnItemSelectedListener(
                sp1, mWl.lang1);
        OnItemSelectedListener spinnerListener2 = new myOnItemSelectedListener(
                sp2, mWl.lang2);

        sp1.setOnItemSelectedListener(spinnerListener1);
        sp2.setOnItemSelectedListener(spinnerListener2);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Spinner sp1 = (Spinner) findViewById(R.id.sp_Lang1);
        Spinner sp2 = (Spinner) findViewById(R.id.sp_Lang2);
        EditText name = (EditText) findViewById(R.id.et_Name);
        String lang1 = sp1.getSelectedItem().toString();
        String lang2 = sp2.getSelectedItem().toString();
        mWl.lang1 = (lang1=="auto") ? mWl.lang1:SupportedLanguage.parseLanguage(lang1);
        mWl.lang2 = (lang2=="auto") ? mWl.lang2:SupportedLanguage.parseLanguage(lang2);
        mWl.title = name.getText().toString();
        mWl.desc = mWl.lang1 + " | " + mWl.lang2;
        db.setWordList(mWl);
    }
    
    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
    
    /**
     * A callback listener that implements the
     * {@link android.widget.AdapterView.OnItemSelectedListener} interface For
     * views based on adapters, this interface defines the methods available
     * when the user selects an item from the View.
     * 
     */
    public class myOnItemSelectedListener implements OnItemSelectedListener {
        boolean spinnerCreated = true;
        Spinner mSp;
        String mSelection;
        public myOnItemSelectedListener(Spinner sp, String lang) {
            this.mSp    = sp;
            int sl=SupportedLanguage.getLanguagePosition(lang);
            if(sl!=-1){
                mSp.setSelection(sl);
            }          
         }

        public void onItemSelected(AdapterView<?> parent, View v, int pos,
                long row) {
            //TextView resultText = (TextView) findViewById(R.id.tv_Lang1);
            //resultText.setText(parent.getItemAtPosition(pos).toString());
        }
        
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }
}
