package com.babasoft.vocabs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class RecyclerViewFragment extends Fragment implements Observer{

    private Context context;
    private WordListAdapter mAdapter;
    private ArrayList<String> arrayList;
    private Button selectButton;
    private WordDB mDB;

    public RecyclerViewFragment() {
    }

    @Override
    public void onAttach(Context context) {
        Log.d(getClass().getName(), "onAttach called");
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(getClass().getName(), "onCreateView called");
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.recycler_view_fragment, container, false);
    }

    public void updateTitle(){
        Log.d(getClass().getName(), "updateTitle called");
        FragmentActivity activity = getActivity();
        if(activity!=null) {
            String title = getString(R.string.Select2Train);
            activity.setTitle(R.string.Select2Train);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(getClass().getName(), "onViewCreated called");
        super.onViewCreated(view, savedInstanceState);
        mDB = new WordDB(getActivity());
        populateRecyclerView(view);
    }

    @Override
    public void onStop() {
        Log.d(getClass().getName(), "onStop called");
        mDB.close();
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.d(getClass().getName(), "onResume called");
        super.onResume();
        mAdapter.refreshList();
    }

    private void populateRecyclerView(View view) {
        Log.d(getClass().getName(), "populateRecyclerView called");
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new WordListAdapter(context, mDB);
        recyclerView.setAdapter(mAdapter);
        mAdapter.refreshList();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(getClass().getName(), "onCreateOptionsMenu called");
        inflater.inflate(R.menu.wordlists_activity_actions, menu);
        updateTitle();
        // Set up ShareActionProvider's default share intent
        //MenuItem shareItem = menu.findItem(R.id.action_share);
        //mShareActionProvider = (android.support.v7.widget.ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
        //mShareActionProvider.setShareIntent(getShareListsIntent());
    }
    /** Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getShareListsIntent() {
        File dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        dir.mkdir();
        ArrayList<Uri> imageUris = (ArrayList<Uri>) mDB.exportSelectedWordLists(dir);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
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
        Log.d(getClass().getName(), "onOptionsItemSelected called");
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), PrefsActivity.class)); // keine ID übergeben = neue Wortliste
                break;
            case R.id.action_new:
                startActivity(new Intent(getActivity(), EditList.class)); // keine ID übergeben = neue Wortliste
                break;
            case R.id.action_edit:
                List<Long> lst = mDB.getSelectedWordLists();
                if (lst.size() > 0) {
                    Intent editInt = new Intent(getActivity(), EditWordList.class);
                    editInt.putExtra(EditWordList.ID, lst.get(0));
                    startActivity(editInt);
                } else
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
                requestStorageRWPermission();
                File mPath = new File(Environment.getExternalStorageDirectory() + "//Vocabs//");
                FileDialog fileDialog = new FileDialog(getActivity(), mPath, "");
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    public void fileSelected(File file) {
                        Log.d(getClass().getName(), "selected file " + file.toString());
                        if (!mDB.importWordList(mDB.getWritableDatabase(),file)){
                            String msg = getString(R.string.ImportError, file.getPath());
                            new AlertDialog.Builder(getActivity()).setTitle(R.string.Error).setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
                        }else
                            mAdapter.refreshList();
                    }
                });
                fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                    public void directorySelected(File directory) {
                        Log.d(getClass().getName(), "selected dir " + directory.toString());
                        new ImportFilesTask().execute(directory);
                    }
                });
                fileDialog.setSelectDirectoryOption(true);
                fileDialog.showDialog();
                break;

            case R.id.action_export:
                requestStorageRWPermission();
                // speichere Datei auf der SD-Karte, ins Verzeichnis
                File dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                dir.mkdir(); // lege Verzeichnis an, ignoriere Fehler
                if (mDB.exportSelectedWordLists(dir).size() > 0) {
                    String msg = getString(R.string.Exported, dir.getPath());
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                } else {
                    String msg = getString(R.string.ExportError, dir.getPath());
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.Error).setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
                }
                break;
            case R.id.action_export_all:
                requestStorageRWPermission();
                //speichere Datei auf der SD-Karte, ins Verzeichnis
                File dir2 = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                if(dir2.mkdir()) {
                    mDB.exportAllWordLists(mDB.getReadableDatabase(), dir2);
                    String msg = getString(R.string.Exported, dir2.getPath());
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                }else {
                    String msg = getString(R.string.ExportError, dir2.getPath());
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.Error).setMessage(msg).setPositiveButton(android.R.string.ok, null).show();
                }
                break;

            case R.id.action_delete:
                mDB.removeSelectedLists();
                mAdapter.refreshList();
                break;
            case R.id.action_delete_all:
                AlertDialog alertDlg = new AlertDialog.Builder(getActivity()).create();
                alertDlg.setTitle(R.string.DeleteAll);
                alertDlg.setMessage(getString(R.string.DeleteAllMessage));
                alertDlg.setCancelable(true);
                alertDlg.setButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mDB.removeAllList();
                        mAdapter.refreshList();
                    }
                });
                alertDlg.setButton2("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDlg.show();
                break;
            case R.id.action_resort_to_parents:
                AlertDialog alDlg = new AlertDialog.Builder(getActivity()).create();
                alDlg.setTitle(R.string.resort2parents);
                alDlg.setMessage(getString(R.string.resort2ParentsAlertMessage));
                alDlg.setCancelable(true);
                alDlg.setButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mDB.resort2ParentList();
                        mAdapter.refreshList();
                    }
                });
                alDlg.setButton2("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alDlg.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d(getClass().getName(), "update from observer called");
        updateTitle();
    }

    private class ImportFilesTask extends AsyncTask<File, Integer, Long> {
        private ProgressDialog dialog;

        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(R.string.PleaseWait);
            dialog.setMessage(getString(R.string.GetLists));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setIndeterminate(false);
            dialog.setProgress(0);
            this.dialog.show();
        }

        protected Long doInBackground(File... dir) {
            long count = 0;
            if(dir[0].exists()){
                File files[] = dir[0].listFiles();
                if(files != null) {
                    count = files.length;
                    for (int i = 0; i < count; i++) {
                        if (!mDB.importWordList(mDB.getWritableDatabase(), files[i]))
                            Log.e(this.getClass().getName(), "Failed to import word list from " + files[i].toString());
                        publishProgress((int) ((i / (float) count) * 100));
                        if (isCancelled()) break;
                    }
                }
            }else
                Log.e(this.getClass().getName(), "directory for import " + dir[0].toString() + " doesn't exist");
            return count;
        }

        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress(progress[0]);

        }

        protected void onPostExecute(Long result) {
            mAdapter.refreshList();
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
    void requestStorageRWPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 777);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), "onActivityResult called: " + requestCode + "," + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
