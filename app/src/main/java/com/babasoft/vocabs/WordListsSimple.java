package com.babasoft.vocabs;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Verwalte beliebig viele Wortlisten in einem ListView: Auswahl, Anlegen und Löschen, 
 * Importieren und Exportieren. 
 */
public class WordListsSimple extends ListActivity {

	public static final String ID="id"; // Parameter zur übergabe der ausgewählten ID 
	private WordDB db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    db = new WordDB(this);
	    Cursor cursor=db.getWordListsCursor(); // Cursor über alle Einträge, wird vom ListAdapter verwaltet und geschlossen
	    setListAdapter(
	    		new SimpleCursorAdapter(this, 
	    			android.R.layout.simple_list_item_1, // Layout für Listeneintrag, hier ein einfacher TextView
	    			cursor, 
	    			new String[]{WordDB.TITLE}, new int[]{android.R.id.text1}) // Mapping zwischen ID im Eintragslayout und Feld in der Datenbank
	    		);
	    registerForContextMenu(getListView()); // zeige popup menü für Liste
	}
	
	@Override
	protected void onResume() {
		((CursorAdapter)getListAdapter()).getCursor().requery(); // frische Liste auf    		
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		db.close();
		((CursorAdapter)getListAdapter()).getCursor().close();
		super.onDestroy();
	}

/**
 * Benutzer klickt auf einen Eintrag in der Liste
 */
	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
		intent.putExtra(ID, id);
		setResult(RESULT_OK, intent);
		db.close();
		finish(); // beende Aktivität erfolgreich
	}

}
