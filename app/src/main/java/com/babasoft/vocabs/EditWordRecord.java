package com.babasoft.vocabs;

import java.util.Locale;
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
import com.memetix.mst.language.Language;

public class EditWordRecord extends Activity implements TranslationListener {
    private WordDB db;
    private long wordRecId = 0;
    WordRecord wordRec = null;
    WordList wordLst = null;
    SimpleCursorAdapter adapter = null;
    TextView tv_lang1, tv_lang2;
    Spinner sp1, sp2;
    EditText et_w1, et_w2;
    private boolean textChanged = false;
    private int mEditTextChanged = 1;
    private int LISTS=0xff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editwordrecord);
        db = new WordDB(this); // siehe onDestroy
        tv_lang1 = (TextView) findViewById(R.id.edit_word_lang1);
        tv_lang2 = (TextView) findViewById(R.id.edit_word_lang2);
        et_w1 = (EditText) findViewById(R.id.edit_word_word1);
        et_w2 = (EditText) findViewById(R.id.edit_word_word2);
        et_w1.addTextChangedListener(textWatcher);
        et_w2.addTextChangedListener(textWatcher);
        sp1 = (Spinner)findViewById(R.id.trans_word_lang1);
        sp2 = (Spinner)findViewById(R.id.trans_word_lang2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, android.R.id.text1,
                SupportedLanguage.getLanguages());
        sp1.setAdapter(adapter);
        sp2.setAdapter(adapter);
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
                wordRecId = extras.getLong(EditWordList.ID);
                wordRec = db.getWordRecord(wordRecId);
                wordLst = db.getWordList(wordRec.lstID);

            } else if (extras.containsKey(EditWordList.LIST_ID)) {
                wordLst = db.getWordList(extras.getLong(EditWordList.LIST_ID));
            }

            if (wordRec != null) {
                et_w1.setText(wordRec.w1);
                et_w2.setText(wordRec.w2);
            }
            if (wordLst != null) {
                tv_lang1.setText(wordLst.lang1);
                tv_lang2.setText(wordLst.lang2);
                Language sl1 = SupportedLanguage.getSupportedLanguage(wordLst.lang1);
                Language sl2 = SupportedLanguage.getSupportedLanguage(wordLst.lang2);
                
                if(sl1!=null)
                    sp1.setSelection(sl1.ordinal());
                
                if(sl2!=null)
                    sp2.setSelection(sl2.ordinal());
                
                setTitle(wordLst.title);
            }
        }else{
            Language sl = SupportedLanguage.getSupportedLanguage(Locale.getDefault().getLanguage());
            if(sl!=null)
                sp2.setSelection(sl.ordinal()); 
        }
        mEditTextChanged = 1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save data if changed
        saveRecord();
        db.close();
    }
    
    void saveRecord() {
        if (textChanged && wordLst != null) {
            // In case of new record
            if (wordRec == null) {
                wordRec = new WordRecord();
                wordRec.id = 0;
            }

            wordRec.w1 = et_w1.getText().toString();
            wordRec.w2 = et_w2.getText().toString();
            wordRec.lstID = wordLst.id;
            if (et_w1.getText().length() > 0 && et_w2.getText().length() > 0) {
                db.setWordRecord(wordRec);
                onError(0, getString(R.string.RecordSaved));
            } else
                onError(0, getString(R.string.RecordSaveFail));
        }
    }
    
    void onAddNewItem() {
        saveRecord();
        et_w1.getText().clear();
        et_w2.getText().clear();
        wordRec = new WordRecord();
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
        	if(getCurrentFocus()==et_w1)
        	    mEditTextChanged = 1;
            else
                mEditTextChanged = 2;
            
            textChanged = true;
        }
    };

    final View.OnClickListener onTranslate1 = new View.OnClickListener() {

        public void onClick(View v) {
            String toBeTranslated;
            Language from,to;
            try {
                
                if(mEditTextChanged==1){
                    from = Language.fromString(SupportedLanguage.parseLanguage(sp1.getSelectedItem().toString()));
                    to = Language.fromString(SupportedLanguage.parseLanguage(sp2.getSelectedItem().toString()));
                    toBeTranslated = et_w1.getText().toString();
                }else{
                    from = Language.fromString(SupportedLanguage.parseLanguage(sp2.getSelectedItem().toString()));
                    to = Language.fromString(SupportedLanguage.parseLanguage(sp1.getSelectedItem().toString()));
                    toBeTranslated = et_w2.getText().toString();
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
            if (wordLst == null)
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
                et_w2.setText(result);
            else
                et_w1.setText(result);
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
            wordLst = db.getWordList(id);
            onAddNewItem();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
