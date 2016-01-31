package com.babasoft.vocabs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.babasoft.vocabs.DictCCScraper.ListRecord;

public class DictCCList extends ListActivity {
    public final static String LIST_NAME        = "list_name"; // Intent parameter
    public final static String LIST_LINK        = "list_link"; // Intent parameter
    public final static String LIST_COUNT       = "list_count"; // Intent parameter
    public final static String LIST_LANG_FROM   = "list_lang_from"; // Intent parameter
    public final static String LIST_LANG_TO     = "list_lang_to"; // Intent parameter
    public final static String LIST_USER        = "list_user"; // Intent parameter
    
    private WordDB db;
    private EditText mFilterText = null;
    private Spinner  mLangSpinner = null;
    MyDictCCArrayAdapter adapter = null;
    private String mTitle;
    private static final int IMPORT = Menu.FIRST;
    private static final int INFO = Menu.FIRST+1;
    Button mNext,mPrev,mFilter;
    List<ListRecord> mLst = new ArrayList<ListRecord>();
    private int mLangSpinnerSelection=0;
    
    private static class ViewHolder {
        public TextView count;
        public TextView lang;
        public TextView listName;
        public TextView listLink;
        public TextView userName;
    }
    private static class DictCCReq{
        public String uri;
        public String from;
        public String to;
        public String filter;
    }
    private class DownloadWebPageTask extends AsyncTask<DictCCReq, Void, List<ListRecord>> {
        private ProgressDialog dialog;
        
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(DictCCList.this);
            dialog.setTitle(R.string.PleaseWait);
            dialog.setMessage(getString(R.string.GetLists));
            this.dialog.show();
        }
        
        @Override
        protected void onPostExecute(List<ListRecord> lst) {
            if(lst!=null){
                
                adapter = new MyDictCCArrayAdapter(DictCCList.this,lst);
                setListAdapter(adapter);
                adapter.notifyDataSetChanged();
                
                if(mLangSpinner.getCount()==0){
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(DictCCList.this,
                            android.R.layout.simple_spinner_item, android.R.id.text1,
                            DictCCScraper.getLangFilters());
                    mLangSpinner.setAdapter(adapter);
                }
            }else
                Toast.makeText(DictCCList.this, getString(R.string.ErrorFromDictCC), Toast.LENGTH_LONG).show();
            
            if (dialog.isShowing())
                dialog.dismiss();
        }

        @Override
        protected List<ListRecord> doInBackground(DictCCReq... r) {
            return DictCCScraper.getLists(r[0].uri, r[0].from, r[0].to, r[0].filter);
        }
    }

    public void readLists(String reqURI,
                          String langFrom,
                          String langTo,
                          String filter ) {
        DictCCReq r = new DictCCReq();
        r.uri       = reqURI;
        r.from      = langFrom;
        r.to        = langTo;
        r.filter    = filter;
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute(r);

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dict_cc_list);
        db = new WordDB(this); // siehe onDestroy
        mFilterText = (EditText) findViewById(R.id.filter);
        mLangSpinner = (Spinner) findViewById(R.id.spinner1);
        mNext = (Button)findViewById(R.id.next_dict_cc_list);
        mNext.setOnClickListener(onButtonClickNextList);
        mPrev = (Button)findViewById(R.id.prev_dict_cc_list);
        mPrev.setOnClickListener(onButtonClickPreviousList);
        mFilter=(Button)findViewById(R.id.bt_filter);
        mFilter.setOnClickListener(onButtonClickFilter);
        registerForContextMenu(getListView());
        setTitle(getString(R.string.ImportFromDictCC));
        readLists("read default", "", "", "");
        mLangSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            
            public void onItemSelected(AdapterView<?> parent, View view,
                    int pos, long id) {
                if (mLangSpinnerSelection != pos) {
                    String sT = parent.getItemAtPosition(pos).toString();
                    String lFrom = null, lTo = null;
                    if (sT.length() > 3) {
                        lFrom = sT.substring(0, 2);
                        if (sT.charAt(3) == '|')
                            lTo = sT.substring(2, 3);
                        else
                            lTo = sT.substring(2, 4);
                    }
                    mLangSpinnerSelection=pos;
                    readLists("I want to filter", lFrom, lTo, mFilterText.getText().toString());
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplication(), "Nothing selected",
                        Toast.LENGTH_LONG).show();
            }

        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();

    }
    public class MyDictCCArrayAdapter extends ArrayAdapter<ListRecord> {
        private final Activity context;
        private List<ListRecord> lst;

        public MyDictCCArrayAdapter(Activity context, List<ListRecord> lst) {
            super(context, R.layout.dict_cc_list_row, lst);
            this.context = context;
            this.lst = lst;
        }
        public void setList(List<ListRecord> lst){
            this.lst=lst;
            adapter.notifyDataSetChanged();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.dict_cc_list_row, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.lang = (TextView) rowView.findViewById(R.id.lang);
                viewHolder.listName = (TextView) rowView.findViewById(R.id.listName);
                viewHolder.count = (TextView) rowView.findViewById(R.id.count);
                viewHolder.userName = (TextView) rowView.findViewById(R.id.userName);
                viewHolder.listLink = (TextView) rowView.findViewById(R.id.listLink);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();
            ListRecord s = lst.get(position);
            holder.lang.setText("|" + s.langFrom.trim() + "-" + s.langTo.trim() + "|");
            holder.listName.setText(s.list);
            holder.count.setText("(" + String.valueOf(s.count) + ")");
            holder.userName.setText(s.user);
            holder.listLink.setText(s.listLink);
            return rowView;
        }
    } 
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
    
    private void onNextListClick(){
        readLists(DictCCScraper.getNextListLink(), "", "", "");
    }
    
    private void onPreviousListClick(){
        readLists(DictCCScraper.getPrevListLink(), "", "", "");
    }
    
    
    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        ViewHolder holder = (ViewHolder) v.getTag();
        String lang_from    = holder.lang.getText().toString().split("-")[0].replace("|", "");
        String lang_to      = holder.lang.getText().toString().split("-")[1].replace("|", "");
        Intent iw = new Intent(this, DictCCWords.class);
        iw.putExtra(DictCCList.LIST_NAME, holder.listName.getText().toString());
        iw.putExtra(DictCCList.LIST_LINK, holder.listLink.getText().toString());
        iw.putExtra(DictCCList.LIST_LANG_FROM, lang_from);
        iw.putExtra(DictCCList.LIST_LANG_TO, lang_to);
        iw.putExtra(DictCCList.LIST_COUNT, holder.count.getText().toString());
        iw.putExtra(DictCCList.LIST_USER, holder.userName.getText().toString());
        startActivity(iw);
        //this.openContextMenu(v);
    }
    
    private void onFilterClick() {
        String sT = mLangSpinner.getSelectedItem().toString();
        String lFrom = null, lTo = null;
        if (sT.length() > 3) {
            lFrom = sT.substring(0, 2);
            if(sT.charAt(3)=='|')
                lTo = sT.substring(2, 3);
            else
                lTo = sT.substring(2, 4);
        }
        readLists("I want to filter", lFrom, lTo, 
                mFilterText.getText().toString());
    }
  
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v.equals(getListView())) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            if (info.id >= 0) {
                menu.setHeaderTitle(mTitle);
                menu.add(0, IMPORT, 0, R.string.Import);
                //menu.add(0, INFO, 0, R.string.Edit);
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
        case IMPORT:
            ViewHolder holder = (ViewHolder) info.targetView.getTag();
            String lang_from    = holder.lang.getText().toString().split("-")[0].replace("|", "");
            String lang_to      = holder.lang.getText().toString().split("-")[1].replace("|", "");
            Intent iw = new Intent(this, DictCCWords.class);
            iw.putExtra(DictCCList.LIST_NAME, holder.listName.getText().toString());
            iw.putExtra(DictCCList.LIST_LINK, holder.listLink.getText().toString());
            iw.putExtra(DictCCList.LIST_LANG_FROM, lang_from);
            iw.putExtra(DictCCList.LIST_LANG_TO, lang_to);
            iw.putExtra(DictCCList.LIST_COUNT, holder.count.getText().toString());
            iw.putExtra(DictCCList.LIST_USER, holder.userName.getText().toString());
            startActivity(iw);
            break;
        case INFO:
            Intent intent = new Intent(this, EditWordRecord.class);
            intent.putExtra(LIST_LINK, info.id);
            startActivity(intent);
            break;
        }
        return super.onContextItemSelected(item);
    }
    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.getFilter().filter(s);
        }

    };
    
    final View.OnClickListener onButtonClickNextList = new View.OnClickListener() {

        public void onClick(View v) {
            onNextListClick();
        }
    };
    final View.OnClickListener onButtonClickPreviousList = new View.OnClickListener() {

        public void onClick(View v) {
            onPreviousListClick();
        }
    };
    final View.OnClickListener onButtonClickFilter = new View.OnClickListener() {

        public void onClick(View v) {
            onFilterClick();
        }
    };
}
