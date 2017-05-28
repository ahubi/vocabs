package com.babasoft.vocabs;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.babasoft.vocabs.DictCCScraper.DictRecord;
import com.babasoft.vocabs.WordDB.WordList;

/**
 * Bearbeite eine Wortliste, bestehend aus zwei EditText-Steuerelementen für
 * Titel und Buzzwort-Liste (durch \n getrennt)
 */
@SuppressLint("ParserError")
public class DictCCWords extends ListActivity {
    private String mLink;
    private String mName;
    private String mCount;
    private String mLangFrom;
    private String mLangTo;
    private String mUser;
    ArrayList<DictRecord> mLst;
    WordDB db;
    ArrayAdapter<DictRecord> adapter;
    private static class ViewHolder {
        public TextView l;
        public TextView r;
    }
    
    private class DownloadWebPageTask extends
            AsyncTask<String, Void, ArrayList<DictRecord>> {
        
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(DictCCWords.this);
            dialog.setTitle(R.string.PleaseWait);
            dialog.setMessage(getString(R.string.GetWords));
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<DictRecord> lst) {
            if(lst!=null)
            {
                adapter = new MyDictCCArrayAdapter(DictCCWords.this, lst);
                setListAdapter(adapter);
                adapter.notifyDataSetChanged();
                mLst = lst;
            }else
                Toast.makeText(DictCCWords.this, getString(R.string.ErrorGettingWords), Toast.LENGTH_LONG).show();
            
            if (dialog.isShowing())
                dialog.dismiss();
        }

        @Override
        protected ArrayList<DictRecord> doInBackground(String... params) {
            return (ArrayList<DictRecord>) DictCCScraper.getWords(params[0]);
        }
    }
    
    private class ImportTask extends AsyncTask<String, Void, Long> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(DictCCWords.this);
            dialog.setTitle(R.string.PleaseWait);
            dialog.setMessage(getString(R.string.ImportProgress));
            dialog.show();
        }

        @Override
        protected void onPostExecute(Long listId) {
            if (dialog.isShowing())
                dialog.dismiss();
            if(listId>0)
                Toast.makeText(DictCCWords.this, "List imported", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Long doInBackground(String... s) {
            WordList wl = new WordList();
            Language langFrom   = SupportedLanguage.getSupportedLanguage(mLangFrom.toLowerCase());
            Language langTo     = SupportedLanguage.getSupportedLanguage(mLangTo.toLowerCase());
            
            if(langFrom!=null)
                wl.lang1 = langFrom.toString();
            else
                wl.lang1 = mLangFrom.toLowerCase();
            
            if(langTo!=null)
                wl.lang2 = langTo.toString();
            else
                wl.lang2 = mLangTo.toLowerCase();
            wl.desc = s[0];
            wl.title = mName;
            return db.importRecords(wl, mLst);
        }
    }

    public void readWords(String reqURI) {
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute(reqURI);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dict_cc_words);
        db = new WordDB(this); // siehe onDestroy
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        Button b = (Button)findViewById(R.id.add_word_record);
        b.setOnClickListener(onButtonClickImport);
        registerForContextMenu(getListView());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCount = (String)extras.get(DictCCList.LIST_COUNT);
            mName = (String)extras.get(DictCCList.LIST_NAME);
            mLink = (String)extras.get(DictCCList.LIST_LINK);
            mLangFrom = (String)extras.get(DictCCList.LIST_LANG_FROM);
            mLangTo = (String)extras.get(DictCCList.LIST_LANG_TO);
            mUser   = (String)extras.get(DictCCList.LIST_USER);
            setTitle(mCount + "|" + mLangFrom + mLangTo + "|" + mName + " by " + mUser);
            readWords(mLink);
        }
    }
    
    public class MyDictCCArrayAdapter extends ArrayAdapter<DictRecord> {
        private final Activity context;
        private final List<DictRecord> lst;

        public MyDictCCArrayAdapter(Activity context, ArrayList<DictRecord> lst) {
            super(context, R.layout.dict_cc_word_row, lst);
            this.context = context;
            this.lst = lst;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.dict_cc_word_row, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.l = (TextView) rowView.findViewById(R.id.leftWord);
                viewHolder.r = (TextView) rowView.findViewById(R.id.rightWord);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();
            DictRecord s = lst.get(position);
            holder.l.setText(s.l);
            holder.r.setText(s.r);
            return rowView;
        }
    } 
    @Override
    protected void onPause() {
        super.onPause();
        // Daten speichern
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
    
    private void onImportClick(){
        new ImportTask().execute("Provided by" + mUser + "on dict.cc");
    }
    
    final View.OnClickListener onButtonClickImport = new View.OnClickListener() {

        public void onClick(View v) {
            onImportClick();
        }
    };
}
