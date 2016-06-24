package com.babasoft.vocabs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.babasoft.vocabs.WordDB.WordList;

 
/**
 * Verwalte beliebig viele Wortlisten in einem ListView: Auswahl, Anlegen und Löschen, 
 * Importieren und Exportieren. 
 */
public class WordLists extends ListFragment {

	public static final String ID="id"; // Parameter zur übergabe der ausgewählten ID 
	private static final int DELETE = Menu.FIRST + 1;
	private static final int EXPORT = Menu.FIRST + 3;
	private static final int EDIT = Menu.FIRST + 4;
	private static final int EDIT_LIST_NAME  = Menu.FIRST + 7;
	private static final int FILESELECTOR_REQUEST = 0;
    private WordDB db;
    private ShareActionProvider mShareActionProvider;

    private static class ViewHolder {
        public TextView listId;
        public CheckBox checkBox;
        public TextView listName;
    }
    private class GetListsTask extends AsyncTask<WordDB, Void, List<WordList>> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(R.string.PleaseWait);
            dialog.setMessage(getString(R.string.GetLists));
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(List<WordList> l) {
            super.onPostExecute(l);
            if (dialog.isShowing())
                dialog.dismiss();
            ((MyListArrayAdapter) getListAdapter()).clear();
            for (Iterator<WordList> iterator = l.iterator(); iterator.hasNext();) {
                ((MyListArrayAdapter) getListAdapter()).add(iterator.next());
            }
            ((MyListArrayAdapter) getListAdapter()).notifyDataSetChanged();
        }

        @Override
        protected List<WordList> doInBackground(WordDB... db) {
            return db[0].getWordLists();
        }
    }
    
    public class MyListArrayAdapter extends ArrayAdapter<WordList> {
        private final Activity context;
        private List<WordList> mLst;

        public MyListArrayAdapter(Activity context, ArrayList<WordList> lst) {
            super(context, R.layout.word_list_row, lst);
            this.context = context;
            this.mLst = lst;
        }

        public List<WordList> getLst() {
            return this.mLst;
        }
        
        public void setLst(List<WordList> l) {
            this.mLst=l;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                view = inflater.inflate(R.layout.word_list_row, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.listId   = (TextView) view.findViewById(R.id.listId);
                viewHolder.checkBox = (CheckBox) view.findViewById(R.id.listSelected);
                viewHolder.listName = (TextView) view.findViewById(R.id.listName);
                viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                  public void onCheckedChanged(CompoundButton buttonView,
                      boolean isChecked) {
                      long id = Long.parseLong(viewHolder.listId.getText().toString());
                      db.updateWordListSelection(id, isChecked);
                      WordList element = (WordList) viewHolder.checkBox.getTag();
                      element.selection = buttonView.isChecked() ? 1:0;
                  }
                });
                view.setTag(viewHolder);
                viewHolder.checkBox.setTag(mLst.get(position));
            } else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkBox.setTag(mLst.get(position));
              }
            ViewHolder holder = (ViewHolder) view.getTag();
            WordList l = mLst.get(position);
            holder.listId.setText(String.valueOf(l.id));
            holder.checkBox.setChecked(l.selection>0);
            holder.listName.setText(l.title + " " + "[" + l.lang1 + "-" + l.lang2 + "]" + " " + "[" + l.count + "]");
            
            view.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    registerForContextMenu(v);
                    return true;
                }
            });
            return view;
        }
    }
    
    public void refreshList(){
        //new GetListsTask().execute(db);
        ((MyListArrayAdapter) getListAdapter()).clear();
        List<WordList> l = db.getWordLists();
        for (Iterator<WordList> iterator = l.iterator(); iterator.hasNext();) {
            ((MyListArrayAdapter) getListAdapter()).add(iterator.next());
        }
        ((MyListArrayAdapter) getListAdapter()).notifyDataSetChanged();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      db = new WordDB(getActivity());
      setListAdapter(new MyListArrayAdapter(getActivity(),new ArrayList<WordDB.WordList>()));
    }
    
    @Override
    public void onResume() {
		super.onResume();
		if(getActivity()!=null)
		    getActivity().setTitle(getString(R.string.Select2Train));
		refreshList();
	}

	@Override
    public void onStop() {
	    ((MyListArrayAdapter) getListAdapter()).clear();
	    db.close();
        super.onStop();
    }

	@Override
    public void onDestroy() {
	   super.onDestroy();
	}

/**
 * Benutzer klickt auf einen Eintrag in der Liste
 */
	@Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		getActivity().openContextMenu(v);
	}

    /**
     * Erzeuge Context(Popup)-Menü, durch langen Tap auf einen Listeintrag ausgewählt
     */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.equals(getListView())) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			if (info.id >= 0) {
			    WordList wl = (WordList)getListAdapter().getItem((int) info.id);
			    menu.setHeaderTitle(wl.title);
				menu.add(0, DELETE, 0, R.string.Delete);
				menu.add(0, EDIT, 0, R.string.Edit);
				menu.add(0, EDIT_LIST_NAME,0,R.string.EditList);
				MenuItem add = menu.add(0, EXPORT, 0, R.string.Export);
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					add.setEnabled(false); // s.u.
			}
		}
	}

/**
 * Context-Menü für List-Eintrag wurde durch Tap-and-Hold ausgewählt
 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		WordList wl = (WordList)getListAdapter().getItem((int) info.id);
		Intent intent;
		switch(item.getItemId()) {
    	case DELETE:
    		// hier ggf. noch Bestätigungs-Popup per AlertBuilder zeige
    		db.removeWordList(wl.id); // lösche Wortliste
    		//mAdapter.remove(wl);
    		//mAdapter.notifyDataSetChanged();
    		refreshList();
            break;
    	case EDIT:
			intent = new Intent(getActivity(), EditWordList.class);
			intent.putExtra(EditWordList.ID, wl.id); // übergebe aktuelle ID
			startActivity(intent);

    		break;
    	case EDIT_LIST_NAME:
            intent = new Intent(getActivity(), EditList.class);
            intent.putExtra(EditWordList.ID, wl.id); // übergebe aktuelle ID
            startActivity(intent);
            break;
            
    	case EXPORT:
			// speichere Datei auf der SD-Karte, ins Verzeichnis
			File dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
			dir.mkdir(); // lege Verzeichnis an, ignoriere Fehler
            Uri fpath = db.exportWordList(wl.id, dir);
			if (fpath!=null) {
				String msg = getString(R.string.Exported, fpath);
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			} else {
				// Fehlermeldung mit Dateipfad als Argument (Platzhalter %s im
				// Format-String)
				String msg = getString(R.string.ExportError, dir.getPath());
				new AlertDialog.Builder(getActivity()).setTitle(R.string.Error).setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
			}
			break;		
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.wordlists_activity_actions, menu);
	    // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());
	}

	/** Defines a default (dummy) share intent to initialize the action provider.
	  * However, as soon as the actual content to be used in the intent
	  * is known or changes, you must update the share intent by again calling
	  * mShareActionProvider.setShareIntent()
	  */
	private Intent getDefaultIntent() {

        File dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        dir.mkdir(); // lege Verzeichnis an, ignoriere Fehler

        ArrayList<Uri> imageUris = (ArrayList<Uri>) db.exportSelectedWordLists(dir);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        //startActivity(Intent.createChooser(shareIntent, "Share images to.."));
//	    Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//        intent.setType("text/plain");
        return shareIntent;
	}
	
	//@Override
//	public void onPrepareOptionsMenu(Menu menu) {
//		// Menü-Eintrag deaktivieren wenn keine SD-Karte verfügbar ist, 
//		// oder das Dateisystem z.B. wegen USB-Verbindung nicht lesbar ist
//		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//			menu.findItem(IMPORT).setEnabled(false); 
//		getActivity().onPrepareOptionsMenu(menu);
//	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
            startActivity(new Intent(getActivity(),PrefsActivity.class)); // keine ID übergeben = neue Wortliste
            break;
    	case R.id.action_new:
    		startActivity(new Intent(getActivity(),EditList.class)); // keine ID übergeben = neue Wortliste
    		break;
    	case R.id.action_edit:
    	    List<Long> lst = db.getSelectedWordLists();
            if(lst.size()>0){
                Intent editInt = new Intent(getActivity(), EditWordList.class);
                editInt.putExtra(EditWordList.ID, lst.get(0));
                startActivity(editInt);
            }else
                Toast.makeText(getActivity(), "Select a list to edit", Toast.LENGTH_LONG).show();
            break;
    	case R.id.action_search:
    	    Intent intent = new Intent(getActivity(), EditWordList.class);
            intent.putExtra(EditWordList.ID, -1);
            startActivity(intent); 
    	    break;
    	
    	case R.id.action_download:
            startActivity(new Intent(getActivity(), DictCCList.class)); 
            break;
            
    	case R.id.action_import:
            Intent i = new Intent(getActivity(), FileSelectorActivity.class);
            // starte eigene Dateiauswahl für *.txt Dateien,
            // Ergebnis in onActivityResult unten
            i.putExtra(FileSelectorActivity.EXTENSIONS, "txt"); //$NON-NLS-1$
            startActivityForResult(i, FILESELECTOR_REQUEST);
            break;
            
    	case R.id.action_export:
            // speichere Datei auf der SD-Karte, ins Verzeichnis
            File dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
            dir.mkdir(); // lege Verzeichnis an, ignoriere Fehler
            if (db.exportSelectedWordLists(dir).size()>0) {
                String msg = getString(R.string.Exported, dir.getPath());
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            } else {
                String msg = getString(R.string.ExportError, dir.getPath());
                new AlertDialog.Builder(getActivity()).setTitle(R.string.Error).setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
            }
            break;      
    	case R.id.action_export_all:
    	    //speichere Datei auf der SD-Karte, ins Verzeichnis
            File dir2 = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
            dir2.mkdir(); // lege Verzeichnis an, ignoriere Fehler
            db.exportAllWordLists(db.getReadableDatabase(),dir2);
            String msg = getString(R.string.Exported, dir2.getPath());
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    	    break;
    	
    	case R.id.action_delete:
            db.removeSelectedLists();
            refreshList();
            break;
    	case R.id.action_delete_all:
    	    AlertDialog alertDlg = new AlertDialog.Builder(getActivity()).create();
    	    alertDlg.setTitle(R.string.DeleteAll);
    	    alertDlg.setMessage(getString(R.string.DeleteAllMessage));
    	    alertDlg.setCancelable(true);
    	    alertDlg.setButton("OK", new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    db.removeAllList();
                    refreshList();
                }
            });
    	    alertDlg.setButton2("Cancel", new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    
                }
            });
    	    alertDlg.show();
    	    break;
        
		}
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILESELECTOR_REQUEST && resultCode == Activity.RESULT_OK) {
			// Benutzer hat eine Datei zum Import ausgewählt
			File file = new File(data.getExtras().getString(FileSelectorActivity.PATH));
			if (db.importWordList(db.getWritableDatabase(),file)){
			    //refreshList();
			}
			else {
				// Fehlermeldung mit Dateipfad als Argument (Platzhalter %s im
				// Format-String)
				String msg = getString(R.string.ImportError, file.getPath());
				new AlertDialog.Builder(getActivity()).setTitle(R.string.Error).setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
			}

		} // FILESELECTOR_REQUEST
	} // onActivityResult	
}
