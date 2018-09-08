package com.babasoft.vocabs;

import android.support.v4.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.app.Activity.RESULT_OK;

public class TranslateFragment extends Fragment implements TranslationListener {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.editwordrecord, container, false);

        //setContentView(R.layout.editwordrecord);
        mDB = new WordDB(getActivity()); // siehe onDestroy
        mTvLang1 = (TextView) retView.findViewById(R.id.edit_word_lang1);
        mTvLang2 = (TextView) retView.findViewById(R.id.edit_word_lang2);
        mEtw1 = (EditText) retView.findViewById(R.id.edit_word_word1);
        mEtw2 = (EditText) retView.findViewById(R.id.edit_word_word2);
        mEtw1.addTextChangedListener(textWatcher);
        mEtw2.addTextChangedListener(textWatcher);
        mSp1 = (Spinner)retView.findViewById(R.id.trans_word_lang1);
        mSp2 = (Spinner)retView.findViewById(R.id.trans_word_lang2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,
                android.R.id.text1,
                SupportedLanguage.getLanguages());
        mSp1.setAdapter(adapter);
        mSp2.setAdapter(adapter);
        Button b = (Button) retView.findViewById(R.id.edit_word_button1);
        b.setOnClickListener(onTranslate1);
        b = (Button) retView.findViewById(R.id.AddItem);
        b.setOnClickListener(onAddItem);
        return retView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSp2.getSelectedItem().toString().contains("auto")){
            int sl = SupportedLanguage.getLanguagePosition(Locale.getDefault().getLanguage());
            if(sl!=-1)
                mSp2.setSelection(sl);
        }
        mEditTextChanged = 1;
    }

    @Override
    public void onPause() {
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
    public void onDestroy() {
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
            if(getActivity().getCurrentFocus()== mEtw1)
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
                        req.callback = TranslateFragment.this;
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
                startActivityForResult(new Intent(getActivity(), WordListsSimple.class), LISTS);
            else
                onAddNewItem();
        }
    };

    public void onComplete(String result) {
        if (result == null)
            Toast.makeText(getActivity(), "Translation failed", Toast.LENGTH_LONG).show();
        else {
            if (mEditTextChanged == 1)
                mEtw2.setText(result);
            else
                mEtw1.setText(result);
        }
    }

    public void onError(int code, String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LISTS && resultCode == RESULT_OK) {
            // eine andere Liste wurde ausgewaehlt
            long id = data.getExtras().getLong(WordLists.ID);
            mWordLst = mDB.getWordList(id);
            onAddNewItem();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
