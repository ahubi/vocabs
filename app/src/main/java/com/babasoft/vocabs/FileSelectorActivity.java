package com.babasoft.vocabs;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.babasoft.vocabs.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * ListActivity zur Dateiauswahl, basierend auf der Endung, 
 * sucht im Hauptverzeichnis der Erweiterungskarte und der ersten Ebene
 * von Unterverzeichnissen.
 * <p>
 * Verwendung:<br>
 * <code>
 *         	Intent i=new Intent(this,FileSelectorActivity.class);<br>
 *        	i.putExtra(FileSelectorActivity.EXTENSIONS, "txt");<br>
 *        	startActivityForResult(i, FILESELECTOR_REQUEST);<br>
 *          ...<br>
 *          onActivityResult(int requestCode, int resultCode, Intent data) {<br>
 *   	    if(requestCode==FILESELECTOR_REQUEST && resultCode==RESULT_OK) {<br>
 *   		&nbsp;&nbsp;File file=new File(data.getExtras().getString(FileSelectorActivity.PATH));<br>
 *          &nbsp;&nbsp;...<br>
 * </code>
 * @author Andreas Linke
 */
public class FileSelectorActivity extends ListActivity {

	private static final int DELETE_ID = Menu.FIRST + 1;
	public static final String PATH = "PATH"; // im Intent zur�ckgegebener ausgew�hlter Pfad
	public static final String EXTENSIONS = "EXTENSIONS"; // im Intent �bergebene Liste von Dateierweiterungen (mit space getrennt)
	private String[] extensions;
	private ArrayAdapter<String> listadapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    registerForContextMenu(getListView());
	    String strext=null;
		if (savedInstanceState != null) {
			strext = savedInstanceState.getString(EXTENSIONS);
		}
        if(strext==null && getIntent()!=null && getIntent().getExtras()!=null)
        	strext=getIntent().getExtras().getString(EXTENSIONS);
        if(strext!=null)
        	extensions=strext.split(" "); 
        else
        	extensions=new String[]{};
        setTitle(R.string.SelectFile);
	    fillFileList();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// gib ausgew�hlte Datei mit vollst�ndigem Pfad zur�ck
		String path=listadapter.getItem((int) id);
		Intent i=new Intent();
		i.putExtra(PATH, toAbsolutePath(path));
		setResult(RESULT_OK,i);
		finish();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        if(v.equals(getListView()))
        	{
            AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        	String path=listadapter.getItem((int) info.id);
        	menu.setHeaderTitle(path);
        	menu.add(0, DELETE_ID, 0, R.string.Delete);
        	}
	}
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    		String path=listadapter.getItem((int) info.id);
    		File f=new File(toAbsolutePath(path));
    		f.delete();
	        fillFileList();
	        return true;
		}
		return super.onContextItemSelected(item);
	}
	/**
	 * Entferne Pfad zur Erweiterungskarte f�r die Anzeige der Dateinamen, 
	 * @param absolutepath vollst�ndiger Pfad
	 * @return Pfad f�r die Anzeige
	 */
	public static String toDisplayPath(String absolutepath)
	{
	String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
	if(absolutepath.startsWith(sdcard))
		return absolutepath.substring(sdcard.length());
	else
		return absolutepath;
	}
	/**
	 * F�ge Pfad zur Erweiterungskarte hinzu
	 * @param displaypath Pfad aus der Anzeige
	 * @return vollst�ndiger Pfad
	 */
	public static String toAbsolutePath(String displaypath)
	{
		String sdcard=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
		if(!displaypath.startsWith(sdcard))
			return sdcard+displaypath;
		else
			return displaypath;
	}

	private void fillFileList() {
		File sdDir = Environment.getExternalStorageDirectory();
		List<File> dirs = new ArrayList<File>();
		dirs.add(sdDir);
		// alle Unterverzeichnisse des Wurzelverzeichnisses der
		// Erweiterungskarte
		dirs.addAll(Arrays.asList(sdDir.listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})));
		List<String> files = new ArrayList<String>();
		for (File dir : dirs) {
			String[] paths = dir.list(new GenericFileFilter(extensions));
			if (paths == null)
				continue; // kein g�ltiges Verzeichnis
			for (String path : paths) {
				files.add(toDisplayPath(dir.getPath() + File.separator + path));
			}
		}
		// sortiere Dateiliste alphabetisch
		Collections.sort(files, new Comparator<String>() {
			
			public int compare(String file1, String file2) {
				return file1.compareToIgnoreCase(file2);
			}
		});
		listadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);
		setListAdapter(listadapter);
	}
	/**
	 * Einfacher Dateifilter nach Erweiterung
	 */
	private static class GenericFileFilter implements FilenameFilter {
		private final TreeSet<String> exts = new TreeSet<String>() ;
		public GenericFileFilter(String[] extensions) {
		  Iterator<String> extList = Arrays.asList(extensions).iterator();
		  while (extList.hasNext()) { 
		    exts.add("." + extList.next().toLowerCase().trim()); 
		  }
		  exts.remove(""); 
		} 
		public boolean accept(File dir, String name) {
		  if(exts.size()==0)
			  return true; // alle Dateien
		  final Iterator<String> extList = exts.iterator();
		  while (extList.hasNext()) {
		    if (name.toLowerCase().endsWith(extList.next())) {
		      return true;
		    }
		  }
		  return false;
		}
	}

}
