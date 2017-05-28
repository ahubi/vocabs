package com.babasoft.vocabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.babasoft.vocabs.WordDB.WordList;
import com.babasoft.vocabs.WordDB.WordRecord;

import java.util.Locale;

public class EditWordRecord extends Activity implements TranslationListener {
    private WordDB mDB;
    private long mWordRecId = 0;
    WordRecord mWordRec = null;
    WordList mWordLst = null;
    SimpleCursorAdapter mAdapter = null;
    TextView mTvLang1, mTvLang2;
    Spinner mSp1, mSp2;
    EditText mEtw1, mEtw2;
    private boolean mTextChanged = false;
    private int mEditTextChanged = 1;
    private int LISTS=0xff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editwordrecord);
        mDB = new WordDB(this); // siehe onDestroy
        mTvLang1 = (TextView) findViewById(R.id.edit_word_lang1);
        mTvLang2 = (TextView) findViewById(R.id.edit_word_lang2);
        mEtw1 = (EditText) findViewById(R.id.edit_word_word1);
        mEtw2 = (EditText) findViewById(R.id.edit_word_word2);
        mEtw1.addTextChangedListener(textWatcher);
        mEtw2.addTextChangedListener(textWatcher);
        mSp1 = (Spinner)findViewById(R.id.trans_word_lang1);
        mSp2 = (Spinner)findViewById(R.id.trans_word_lang2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                android.R.id.text1,
                SupportedLanguage.getLanguages());
        mSp1.setAdapter(adapter);
        mSp2.setAdapter(adapter);
        Button b = (Button) findViewById(R.id.edit_word_button1);
        b.setOnClickListener(onTranslate1);
        b = (Button) findViewById(R.id.AddItem);
        b.setOnClickListener(onAddItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey(EditWordList.ID)) {
                mWordRecId = extras.getLong(EditWordList.ID);
                mWordRec = mDB.getWordRecord(mWordRecId);
                mWordLst = mDB.getWordList(mWordRec.lstID);

            } else if (extras.containsKey(EditWordList.LIST_ID)) {
                mWordLst = mDB.getWordList(extras.getLong(EditWordList.LIST_ID));
            }

            if (mWordRec != null) {
                mEtw1.setText(mWordRec.w1);
                mEtw2.setText(mWordRec.w2);
            }
            if (mWordLst != null) {
                mTvLang1.setText(mWordLst.lang1);
                mTvLang2.setText(mWordLst.lang2);
                Language sl1 = SupportedLanguage.getSupportedLanguage(mWordLst.lang1);
                Language sl2 = SupportedLanguage.getSupportedLanguage(mWordLst.lang2);
                
                if(sl1!=null)
                    mSp1.setSelection(sl1.ordinal());
                
                if(sl2!=null)
                    mSp2.setSelection(sl2.ordinal());
                
                setTitle(mWordLst.title);
            }
        }else{
            Language sl = SupportedLanguage.getSupportedLanguage(Locale.getDefault().getLanguage());
            if(sl!=null)
                mSp2.setSelection(sl.ordinal());
        }
        mEditTextChanged = 1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save data if changed
        saveRecord();
        mDB.close();
    }
    
    void saveRecord() {
        if (mTextChanged && mWordLst != null) {
            // In case of new record
            if (mWordRec == null) {
                mWordRec = new WordRecord();
                mWordRec.id = 0;
            }

            mWordRec.w1 = mEtw1.getText().toString();
            mWordRec.w2 = mEtw2.getText().toString();
            mWordRec.lstID = mWordLst.id;
            if (mEtw1.getText().length() > 0 && mEtw2.getText().length() > 0) {
                mDB.setWordRecord(mWordRec);
                onError(0, getString(R.string.RecordSaved));
            } else
                onError(0, getString(R.string.RecordSaveFail));
        }
    }
    
    void onAddNewItem() {
        saveRecord();
        mEtw1.getText().clear();
        mEtw2.getText().clear();
        mWordRec = new WordRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {

        }

        public void afterTextChanged(Editable s) {
        	if(getCurrentFocus()== mEtw1)
        	    mEditTextChanged = 1;
            else
                mEditTextChanged = 2;
            
            mTextChanged = true;
        }
    };

    final View.OnClickListener onTranslate1 = new View.OnClickListener() {

        public void onClick(View v) {
            String toBeTranslated;
            Language from,to;
            try {
                
                if(mEditTextChanged==1){
                    from = Language.fromString(SupportedLanguage.parseLanguage(mSp1.getSelectedItem().toString()));
                    to = Language.fromString(SupportedLanguage.parseLanguage(mSp2.getSelectedItem().toString()));
                    toBeTranslated = mEtw1.getText().toString();
                }else{
                    from = Language.fromString(SupportedLanguage.parseLanguage(mSp2.getSelectedItem().toString()));
                    to = Language.fromString(SupportedLanguage.parseLanguage(mSp1.getSelectedItem().toString()));
                    toBeTranslated = mEtw2.getText().toString();
                }
                    
                if (from != null && to != null) {
                    if (to != Language.AUTO_DETECT) {
                        TranslateReq req = new TranslateReq();
                        req.from = from.toString();
                        req.to = to.toString();
                        req.toTranslate = toBeTranslated;
                        req.callback = EditWordRecord.this;
                        GoogleTranslate tt = new GoogleTranslate();
                        tt.execute(req);
                    }else
                        onError(0, getString(R.string.TargetLanguageError)); 
                } else
                    onError(0, "Tranlsation not supported");
            } catch (Exception e) {
                onError(e.hashCode(), e.getMessage());
            }
        }
    };

  
    final View.OnClickListener onAddItem = new View.OnClickListener() {

        public void onClick(View v) {
            if (mWordLst == null)
                startActivityForResult(new Intent(EditWordRecord.this, WordListsSimple.class), LISTS);
            else    
                onAddNewItem();
        }
    };

    public void onComplete(String result) {
        if (result == null)
            Toast.makeText(this, "Translation failed", Toast.LENGTH_LONG).show();
        else {
            if (mEditTextChanged == 1)
                mEtw2.setText(result);
            else
                mEtw1.setText(result);
        }
    }

    public void onError(int code, String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LISTS && resultCode == RESULT_OK) {
            // eine andere Liste wurde ausgewaehlt
            long id = data.getExtras().getLong(WordLists.ID);
            mWordLst = mDB.getWordList(id);
            onAddNewItem();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
