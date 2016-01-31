package com.babasoft.vocabs;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Bearbeite eine Wortliste, bestehend aus zwei EditText-Steuerelementen für
 * Titel und Buzzwort-Liste (durch \n getrennt)
 */
public class EditWordList extends ListActivity {
    public final static String ID       = "id"; // Intent parameter
    public final static String LIST_ID  = "list_id"; // Intent parameter
    private final static int EDIT_RECORD=0;
    private final static int ADD_RECORD=1;
    
    private WordDB db;
    private EditText filterText = null;
    SimpleCursorAdapter adapter = null;
    private Long mListId;
    private String mTitle;
    private static final int EDIT = Menu.FIRST;
    private static final int DELETE = Menu.FIRST+1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editwordlist);
        db = new WordDB(this); // siehe onDestroy
        filterText = (EditText) findViewById(R.id.filter);
        filterText.addTextChangedListener(filterTextWatcher);
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        Button b = (Button)findViewById(R.id.add_word_record);
        b.setOnClickListener(onButtonClickAddItem);
        b = (Button)findViewById(R.id.edit_list);
        b.setOnClickListener(onEditList);
        registerForContextMenu(getListView());
        
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(ID)) {
            mListId = extras.getLong(ID);
            Cursor cursor;
            
            if(mListId==0){
                cursor = db.getWordsCursor();
                b = (Button)findViewById(R.id.add_word_record);
                b.setVisibility(Button.GONE);
                b = (Button)findViewById(R.id.edit_list);
                b.setVisibility(Button.GONE);
                mTitle="*";
            }
            else{
                cursor = db.getWordsCursor(mListId); 
                mTitle= db.getWordList(mListId).title;
            }
            setTitle(mTitle);
            adapter = new SimpleCursorAdapter(this, R.layout.wordsrow, 
                                              cursor, new String[] {WordDB.ID,WordDB.LIST_ID, WordDB.W1, WordDB.W2 }, 
                                              new int[] {R.id.id, R.id.listId, R.id.word1, R.id.word2 });
            adapter.setFilterQueryProvider(new FilterQueryProvider() {
                
                public Cursor runQuery(CharSequence s) {
                    Cursor cur;
                    if(mListId==0)
                        cur = db.getWordsCursor(s.toString());
                    else
                        cur = db.getWordsCursor(mListId,s.toString());
                    
                    startManagingCursor(cur);
                    
                    return cur;
                }
            });
            
            setListAdapter(adapter);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        db.close();
        ((CursorAdapter)getListAdapter()).getCursor().close();
        super.onDestroy();
    }
    
    private void onAddItemClick(){
        //Toast.makeText(this, s, Toast.LENGTH_LONG).show(); 
        Intent intent = new Intent(this, EditWordRecord.class);
        intent.putExtra(EditWordList.LIST_ID, mListId);
        startActivityForResult(intent, ADD_RECORD); 
    }
    
    private void onEditListClick(){
        //Toast.makeText(this, s, Toast.LENGTH_LONG).show(); 
        Intent intent = new Intent(this, EditList.class);
        intent.putExtra(EditWordList.ID, mListId);
        startActivity(intent); 
    }
    
    /**
     * Benutzer klickt auf einen Eintrag in der Liste
     */
    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        this.openContextMenu(v);
    }

    /**
     * Erzeuge Context(Popup)-Menü, durch langen Tap auf einen Listeintrag
     * ausgewählt
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v.equals(getListView())) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            if (info.id >= 0) {
                menu.setHeaderTitle(mTitle);
                menu.add(0, DELETE, 0, R.string.Delete);
                menu.add(0, EDIT, 0, R.string.Edit);
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * Context-Menü für List-Eintrag wurde durch Tap-and-Hold ausgewählt
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
        case DELETE:
            db.removeWordRecord(info.id);
            ((CursorAdapter) getListAdapter()).getCursor().requery(); // update list
            break;
        case EDIT:
            Intent intent = new Intent(this, EditWordRecord.class);
            intent.putExtra(ID, info.id);
            startActivityForResult(intent, EDIT_RECORD);
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
    
    final View.OnClickListener onButtonClickAddItem = new View.OnClickListener() {

        public void onClick(View v) {
            onAddItemClick();
        }
    };
    final View.OnClickListener onEditList = new View.OnClickListener() {

        public void onClick(View v) {
            onEditListClick();
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((CursorAdapter) getListAdapter()).changeCursor(db.getWordsCursor(mListId));
    }
}
