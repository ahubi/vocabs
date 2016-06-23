package com.babasoft.vocabs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.babasoft.vocabs.DictCCScraper.DictRecord;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Datenbankhilfsklasse, kapselt alle DB-Zugriffe
 */
public class WordDB extends SQLiteOpenHelper {

    public final static String MAIN_TABLE = "wordlists"; // Table with words
                                                         // lists
    public final static String WORDS_TABLE = "words"; // Table with words
    // Datenbankfelder
    public final static String ID = "_id";
    public final static String TITLE = "title";
    public final static String SELECION = "selection";
    public final static String LANG1 = "lang1";
    public final static String LANG2 = "lang2";
    public final static String DESC = "descr";
    public final static String WORDS = "words";
    public final static String W1 = "w1";
    public final static String W2 = "w2";
    public final static String SCORE = "score";
    public final static String LIST_ID = "_list";
    public final static String TYPE = "type";


    protected Context context;

    /**
     * Java-Objekt für unsere Daten. Aus Performance-Gründen wird auf
     * getter/setter verzichtet
     */
    public static class WordList {
        public long id;
        public String title;
        public String lang1;
        public String lang2;
        public String desc;
        public int selection;
        public long count;
    }

    public static class WordRecord {
        public long id;
        public long lstID;
        public String w1;
        public String w2;
        public int score;
        public int dirty;
    }

    public WordDB(Context ctx) {
        super(ctx, MAIN_TABLE, null, 2); // DB version 1
        this.context = ctx;
    }

    /**
     * Creates tables in the database required for application
     * @param db to creates tables in
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String s;
        try {
            InputStream in = context.getResources()
                    .openRawResource(R.raw.words);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in, null);
            NodeList statements = doc.getElementsByTagName("statement");
            for (int i = 0; i < statements.getLength(); i++) {
                s = statements.item(i).getChildNodes().item(0).getNodeValue();
                db.execSQL(s);
            }
            in.close();
        } catch (Throwable t) {
            Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show();
        }
        importInternalWordLists(db);
    }
    
    
    /**
     * Import default lists which are part of application
     * @param: databas to import into
     */
    public void importInternalWordLists(SQLiteDatabase db) {
        String s;
        long lstId = -1;
        ContentValues cv= new ContentValues();
        try {
            InputStream in = context.getResources().openRawResource(R.raw.words);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in, null);
            NodeList items = doc.getElementsByTagName("wordlist");
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                NodeList properties = item.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String name = property.getNodeName();
                    
                    if (name.equalsIgnoreCase("list")) {
                        s = property.getFirstChild().getNodeValue();
                        String[] listParts = s.split(",");
                        if (!doesWordListExist(db, listParts[1].trim())) {
                            cv.put(TITLE, listParts[1].trim());
                            cv.put(LANG1, listParts[2].trim());
                            cv.put(LANG2, listParts[3].trim());
                            cv.put(DESC, listParts[4].trim());
                            cv.put(TYPE,0);
                            cv.put(SELECION,Integer.parseInt(listParts[5].trim()));
                            lstId = db.insert(MAIN_TABLE, null, cv);
                        } else {
                            Log.d(this.getClass().getName(), "WordList " + listParts[1].trim() + " already exists");
                            lstId = -1;
                        }
                    }else if (name.equalsIgnoreCase("words")) {
                        if (lstId != -1) {
                            List<String> strList = Arrays.asList(property.getFirstChild().getNodeValue().split("\n"));
                            Iterator<String> iter = strList.iterator();

                            while (iter.hasNext()) {
                                s = wordRecSqlInsertString(iter.next().trim(),lstId);
                                db.execSQL(s);
                            }
                        }
                    }
                }
            }
            in.close();
        } catch (Throwable t) {
            Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("ALTER TABLE " + MAIN_TABLE + " ADD COLUMN selection INTEGER");
        } catch (SQLiteException e) {
            Log.w(getClass().getName(), e.getMessage());
        }
        //Now import internal default lists
        //new ImportTask().execute(db);
        importInternalWordLists(db);
    }

    // Wichtig: vor dem endgültigen Verlassen der Activity immer close()
    // aufrufen
    // Methode ist hier nur zu Dokumentationszwecken überschrieben
    @Override
    public synchronized void close() {
        super.close();
    }

    /**
     * Gibt Wortliste zur id zurück
     * 
     * @param id
     *            Schlüsselfeld in der Datenbank
     * @return Wortliste oder null falls nicht gefunden
     */
    public WordList getWordList(long id) {
        Cursor c = null;
        //Cursor wc = null;
        try {
            c = getReadableDatabase().query(MAIN_TABLE,
                    new String[] { ID, TITLE, LANG1, LANG2, DESC, SELECION },
                    ID + "=?", new String[] { String.valueOf(id) }, null, null,
                    null);
            if (!c.moveToFirst())
                return null; // keine Wortliste zur ID gefunden
            WordList wordlist = new WordList();
            wordlist.id = id;
            wordlist.title = c.getString(c.getColumnIndex(TITLE));
            wordlist.lang1 = c.getString(c.getColumnIndex(LANG1));
            wordlist.lang2 = c.getString(c.getColumnIndex(LANG2));
            wordlist.desc = c.getString(c.getColumnIndex(DESC));
            wordlist.selection = c.getInt(c.getColumnIndex(SELECION));
            //wc = getWordsCursor(id);
            wordlist.count = getWordsCursorCount(id); //wc.getCount();
            //wc.close();
            return wordlist;
        } finally {
            if (c != null)
                c.close(); // Cursor sollte stets in einem finally-Block
                           // geschlossen werden
        }
    }
    
    /**
     * Checks wether a word list name exists in database
     * 
     * @param name of list
     * @return true list exists in database, false otherwise
     */
    public boolean doesWordListExist(SQLiteDatabase db, String name) {
        Cursor c = null;
        try {
            c = db.query(MAIN_TABLE, new String[] { ID, TITLE}, TITLE + "=?", new String[] {name}, null, null,null);
            return c.moveToFirst();
        } finally {
            if (c != null)
                c.close(); //always close cursor
        }
    }
    /**
     * Returns all avaialble word lists
     * 
     * @return word lists or null
     */
    public List<WordList> getWordLists() {
        Cursor c = getWordListsCursor();
        List<WordList> lst = new ArrayList<WordList>();
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                WordList wl = getWordList(c
                        .getLong(c.getColumnIndexOrThrow(ID)));
                lst.add(wl);
                c.moveToNext();
            }
            c.close();
        }
        return lst;
    }
    
    /**
     * Returns ids of all selected word lists
     * 
     * @return vector of selected lists
     */
    public List<Long> getSelectedWordLists() {
        Cursor c = null;
        try {
            c = getSelectedWordListsCursor();
            List<Long> lst = new ArrayList<Long>();
            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    lst.add(c.getLong(c.getColumnIndexOrThrow(ID)));
                    c.moveToNext();
                }
            }
            return lst;    
        
        } finally {
            if(c!=null)
                c.close();
        }
        
    }

    /**
     * Gibt Wortliste zur id zurück
     * 
     * @param id
     *            Schlüsselfeld in der Datenbank
     * @return Wortliste oder null falls nicht gefunden
     */
    public List<WordRecord> getWords(long id) {
        Cursor c = null;
        try {
            // Get words for this list
            c = getReadableDatabase().query(WORDS_TABLE,
                    new String[]{ID, LIST_ID, W1, W2, SCORE},
                    LIST_ID + "=?", new String[]{String.valueOf(id)}, null,
                    null, SCORE);

            List<WordRecord> lst = new ArrayList<WordRecord>();
            while (c.moveToNext()) {
                WordRecord rec = new WordRecord();
                rec.id = c.getInt(c.getColumnIndex(ID));
                rec.lstID = c.getInt(c.getColumnIndex(LIST_ID));
                rec.w1 = c.getString(c.getColumnIndex(W1));
                rec.w2 = c.getString(c.getColumnIndex(W2));
                rec.score = c.getInt(c.getColumnIndex(SCORE));
                lst.add(rec);
            }
            return lst;
        } finally {
            if (c != null)
                c.close(); // Cursor sollte stets in einem finally-Block
                           // geschlossen werden
        }
    }

    /**
     * Gibt Wortliste zur id zurück
     * 
     * @param id
     *            Schlüsselfeld in der Datenbank
     * @return Wortliste oder null falls nicht gefunden
     */
    public WordRecord getWordRecord(long id) {
        Cursor c = null;
        try {
            // Get words for this list
            c = getReadableDatabase().query(WORDS_TABLE,
                    new String[] { ID, LIST_ID, W1, W2, SCORE }, ID + "=?",
                    new String[] { String.valueOf(id) }, null, null, SCORE);
            if (c.moveToFirst()) {
                WordRecord rec = new WordRecord();
                rec.id = c.getInt(c.getColumnIndex(ID));
                rec.lstID = c.getInt(c.getColumnIndex(LIST_ID));
                rec.w1 = c.getString(c.getColumnIndex(W1));
                rec.w2 = c.getString(c.getColumnIndex(W2));
                rec.score = c.getInt(c.getColumnIndex(SCORE));
                return rec;
            } else
                return null;
        } finally {
            if (c != null)
                c.close(); // Cursor sollte stets in einem finally-Block
                           // geschlossen werden
        }
    }

    public Cursor getWordsCursor(long id, String s) {
        String query = "Select " + ID + "," + LIST_ID + "," + W1 + "," + W2
                + "," + SCORE + " from " + WORDS_TABLE + " WHERE " + LIST_ID
                + "=" + String.valueOf(id) + " AND (" + W1 + " LIKE " + "'%"
                + s + "%'" + " or " + W2 + " like " + "'%" + s + "%')"
                + " ORDER BY " + W1 + " COLLATE LOCALIZED";
        return getReadableDatabase().rawQuery(query, null);
    }

    public Cursor getWordsCursor(String s) {
        String query = "Select " + ID + "," + LIST_ID + "," + W1 + "," + W2
                + "," + SCORE + " from " + WORDS_TABLE + " WHERE " + W1
                + " LIKE " + "'%" + s + "%'" + " or " + W2 + " like " + "'%"
                + s + "%'" + " ORDER BY " + W1 + " COLLATE LOCALIZED";
        return getReadableDatabase().rawQuery(query, null);
    }

    public Cursor getWordsCursor(long id) {
        return getReadableDatabase().query(WORDS_TABLE,
                new String[]{ID, LIST_ID, W1, W2, SCORE}, LIST_ID + "=?",
                new String[]{String.valueOf(id)}, null, null,
                W1 + " COLLATE LOCALIZED");
    }
    
    public int getWordsCursorCount(long id) {
        Cursor c = null;
        try {
            c =getReadableDatabase().query(WORDS_TABLE,
                    new String[] {LIST_ID}, LIST_ID + "=?",
                    new String[] { String.valueOf(id) }, null, null,null);
            if (c!=null)
                return c.getCount();
            else
                return 0;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    public Cursor getWordsCursor() {
        return getReadableDatabase().query(WORDS_TABLE,
                new String[] { ID, LIST_ID, W1, W2, SCORE }, null, null, null,
                null, W1 + " COLLATE LOCALIZED");
    }

    /**
     * Speichere Wortliste. Falls id 0, wird ein neuer Eintrag angelegt,
     * anderenfalls wird der Eintrag aktualisiert.
     * 
     * @param wordlist
     *            zu speichernde Wortlist
     * @return id der gespeicherten Wortliste
     */
    public long setWordList(WordList wordlist) {
        ContentValues values = new ContentValues();

        if (wordlist.id != 0)
            values.put(ID, wordlist.id);

        values.put(TITLE, wordlist.title);
        values.put(LANG1, wordlist.lang1);
        values.put(LANG2, wordlist.lang2);
        values.put(DESC, wordlist.desc);
        values.put(SELECION, wordlist.selection);

        if (wordlist.id == 0) {
            wordlist.id = getWritableDatabase()
                    .insert(MAIN_TABLE, null, values);
        } else {
            getWritableDatabase().update(MAIN_TABLE, values, ID + "=?",
                    new String[]{String.valueOf(wordlist.id)});
        }
        return wordlist.id;
    }
    
    /**
     * insterts a wordlist into db
     * anderenfalls wird der Eintrag aktualisiert.
     * @param db in wich inserted 
     * @param wordlist to be inserted
     * @return id der gespeicherten Wortliste
     */
    public long insertWordList(SQLiteDatabase db, WordList wordlist) {
        ContentValues values = new ContentValues();

        if (wordlist.id != 0)
            values.put(ID, wordlist.id);

        values.put(TITLE, wordlist.title);
        values.put(LANG1, wordlist.lang1);
        values.put(LANG2, wordlist.lang2);
        values.put(DESC, wordlist.desc);
        values.put(SELECION, wordlist.selection);

        if (wordlist.id == 0) {
            wordlist.id = db.insert(MAIN_TABLE, null, values);
        } else {
            db.update(MAIN_TABLE, values, ID + "=?", new String[]{String.valueOf(wordlist.id)});
        }
        return wordlist.id;
    }

    public void updateWordListSelection(long id, boolean selection) {
        ContentValues values = new ContentValues();

        if (id != 0) {
            values.put(SELECION, selection ? 1:0);
            getWritableDatabase().update(MAIN_TABLE, values, ID + "=?",
                    new String[]{String.valueOf(id)});
        }
    }

    public long setWordRecord(WordRecord rec) {
        ContentValues values = new ContentValues();

        if (rec.id != 0)
            values.put(ID, rec.id);

        values.put(LIST_ID, rec.lstID);
        values.put(W1, rec.w1);
        values.put(W2, rec.w2);
        values.put(SCORE, rec.score);

        if (rec.id == 0) {
            rec.id = (int) getWritableDatabase().insert(WORDS, null, values);
        } else {
            getWritableDatabase().update(WORDS, values, ID + "=?",
                    new String[]{String.valueOf(rec.id)});
        }
        return rec.id;
    }

    public long insertWordRecord(SQLiteDatabase db, WordRecord rec) {
        ContentValues values = new ContentValues();

        if (rec.id != 0)
            values.put(ID, rec.id);

        values.put(LIST_ID, rec.lstID);
        values.put(W1, rec.w1);
        values.put(W2, rec.w2);
        values.put(SCORE, rec.score);

        if (rec.id == 0) {
            rec.id = (int) db.insert(WORDS, null, values);
        } else {
            db.update(WORDS, values, ID + "=?", new String[]{String.valueOf(rec.id)});
        }
        return rec.id;
    }
    public long importRecords(WordList wordlist, List<DictRecord> l) {
        long id = setWordList(wordlist);
        SQLiteDatabase db = getWritableDatabase();
        for (DictRecord e : l) {
            ContentValues values = new ContentValues();
            values.put(LIST_ID, id);
            values.put(W1, e.l);
            values.put(W2, e.r);
            values.put(SCORE, 0);
            db.insert(WORDS, null, values);
        }
        db.close();
        return id;
    }

    public void removeWordRecord(long id) {
        getWritableDatabase().delete(WORDS, "_id=" + id, null);
    }

    private String wordRecSqlInsertString(String rec, Long lstId) {
        String[] listParts = rec.split("\t");
        return String.format("INSERT INTO " + WORDS_TABLE
                        + " VALUES(NULL,%d,'%s','%s',%d)",
                lstId, listParts[0].trim(), listParts[1].trim(), 0);
    }

    /**
     * Lege Standard-(Beispiel)Wortliste an
     * 
     * @return id der gespeicherten Wortliste
     */
    public long createDefaultWordList() {
        return setWordList(getDefaultWordList());
    }

    private WordList getDefaultWordList() {
        WordList wordlist = new WordList();
        wordlist.title = "Standard";
        return wordlist;
    }

    /**
     * Liefere ID der ersten gespeicherten Wortliste zurück, als Fallback, falls
     * die aktuelle Wortliste nicht mehr existiert
     * 
     * @return ID der Wortliste oder 0 falls Tabelle leer
     */
    public long getFirstWordListID() {
        Cursor c = null;
        try {
            c =getReadableDatabase().query(MAIN_TABLE,new String[] { ID, TITLE }, null, null, null, null,
                                           TITLE + " COLLATE LOCALIZED");
            if (c.moveToFirst())
                return c.getLong(c.getColumnIndexOrThrow(ID));
            else
                return 0;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    /**
     * Liefere ID der ersten gespeicherten Wortliste zurück, als Fallback, falls
     * die aktuelle Wortliste nicht mehr existiert
     * 
     * @return ID der Wortliste oder 0 falls Tabelle leer
     */
    public long getLastWordListID() {
        Cursor c = null;
        try {
            c = getReadableDatabase().query(MAIN_TABLE, new String[] { ID },
                    null, null, null, null, null, null);
            if (c.moveToLast())
                return c.getLong(c.getColumnIndexOrThrow(ID));
            else
                return 0;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    /**
     * Zugriff auf alle Wortlisten, sortiert nach Titel
     * 
     * @return Cursor über die Daten, Aufrufer muss Cursor selbst schliesseen
     */
    public Cursor getSelectedWordListsCursor() {
        return getReadableDatabase().query(MAIN_TABLE,
                new String[] { ID, TITLE, SELECION }, SELECION +"=1", null, null, null,
                TITLE + " COLLATE LOCALIZED"); // sortiere nach Titel,
                                               // Sortierreihenfolge der
                                               // aktuellen Sprache
    }
    
    /**
     * Zugriff auf alle Wortlisten, sortiert nach Titel
     * 
     * @return Cursor über die Daten, Aufrufer muss Cursor selbst schliesseen
     */
    public Cursor getWordListsCursor() {
        return getReadableDatabase().query(MAIN_TABLE,
                new String[] { ID, TITLE }, null, null, null, null,
                TITLE + " COLLATE LOCALIZED");    
    }
    
    /**
     * Finde benachbarte Wortliste, basierend auf der alphabetisch sortierten
     * Liste aller Wortlisten
     * 
     * @param id
     *            ID der aktuellen Wortliste
     * @param next
     *            true für nächste, false für vorhergehende
     * @return ID der nächsten/vorhergehenden Wortliste, 0 falls keine gefunden
     */
    public long getAdjacentWordList(long id, boolean next) {
        Cursor c = null;
        try {
            c = getWordListsCursor(); // sortiert
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                if (c.getLong(c.getColumnIndexOrThrow(ID)) == id) {
                    // Wortliste mit gegebener ID wurde gefunden
                    if (next && c.moveToNext())
                        return c.getLong(c.getColumnIndexOrThrow(ID));
                    else if (!next && c.moveToPrevious())
                        return c.getLong(c.getColumnIndexOrThrow(ID));
                    else
                        return 0;
                }
            }
            return 0; // Wortliste nicht gefunden
        } finally {
            if (c != null)
                c.close();
        }
    }

    /**
     * Importiere neue Wortliste aus dem Dateisystem. Titel ist Dateiname (ohne
     * .txt), Wortliste ist Inhalt der Textdatei
     * @param db database to import into
     * @param file Pfad zur Wortlistendatei
     * @return true wenn Datei gelesen wurde
     */
    public boolean importWordList(SQLiteDatabase db, File file) {
        String title = URLDecoder.decode(file.getName()); // s.u. encoding
        int extind = title.lastIndexOf('.');

        if (extind >= 0)
            title = title.substring(0, extind);

        try {
            // liest standardmäßig UTF-8
            WordList wl = new WordList();
            wl.title = title;
            wl.lang1 = wl.lang2 = "";
            wl.desc = "Imported from: " + file.getPath();
            String str;
            long listId = 0;
            CSVReader csvReader = new CSVReader(new FileReader(file), '\t');
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                if (listId == 0) {
                    str = row[0];
                    if (str.contains("-lang-")) {
                        wl.lang1 = str.split("-lang-")[0].trim();
                        wl.lang2 = str.split("-lang-")[1].trim();
                        listId = insertWordList(db, wl);
                        continue;//skip -lang- line
                    } else
                        listId = insertWordList(db, wl);
                }
                //Import pairs only
                if (row.length>1) {
                    //Import non empty words only
                    if(row[0].length()>0 && row[1].length()>0) {
                        WordRecord wr = new WordRecord();
                        wr.w1 = row[0];
                        wr.w2 = row[1];
                        wr.lstID = listId;
                        insertWordRecord(db, wr);
                    }
                }
            }
            csvReader.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Import all lists from a directory
     * @param dir directory to import from
     */
    public void importAllWordLists(SQLiteDatabase db, File dir) {
        if(dir.exists()){
            String files[] = dir.list();
            for (int i = 0; i < files.length; i++) {
                File file = new File(files[i]);
                if(!importWordList(db,file))
                    Log.e(this.getClass().getName(), "Failed to import word list from " + file.toString());
            }
        }else
            Log.e(this.getClass().getName(), "directory for import " + dir.toString() + " doesn't exist");
    }

    /**
     * Exportiere Wortliste ins Dateisystem. Der Dateiname ergibt sich aus dem
     * Titel, eine existierende Datei wird überschrieben.
     * 
     * @param id
     *            ID der Wortliste
     * @param dir
     *            Verzeichnis in das geschrieben werden soll (muss existieren)
     * @return true falls erfolgreich
     */
    public String exportWordList(long id, File dir) {
        WordList wordlist = getWordList(id);
        // Dateipfad ergibt sich aus Name, wir quoten alle Sonderzeichen
        File file = new File(dir, URLEncoder.encode(wordlist.title) + ".txt");
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file),'\t');
            List<WordRecord> words = getWords(id);
            String entries[]= new String[2];
            entries[0]= wordlist.lang1 + "-lang-" + wordlist.lang2;
            writer.writeNext(entries,false);
            //Write all word records
            for (WordRecord wr : words) {
                entries[0]= wr.w1;
                entries[1]= wr.w2;
                writer.writeNext(entries,false);
            }
            writer.close();
        } catch (IOException e) {
            // Fehler wird hier einfach ignoriert
            return null;
        }
        return file.getAbsolutePath();
    }

    public List<String> exportSelectedWordLists(File dir) {
        Cursor c = getSelectedWordListsCursor();
        List<String> lst = new ArrayList<String>();
        String ret;
        if (c.moveToNext()) {
            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    ret = exportWordList(c.getLong(c.getColumnIndexOrThrow(ID)), dir);
                    if (ret!=null)
                        lst.add(ret);
                    c.moveToNext();
                }

            }
            c.close();
        }
        return lst;
    }
    
    /**
     * Exportiere Wortliste ins Dateisystem. Der Dateiname ergibt sich aus dem
     * Titel, eine existierende Datei wird überschrieben.
     * 
     * @param db
     *            database
     * @param dir
     *            Verzeichnis in das geschrieben werden soll (muss existieren)
     * @return true falls erfolgreich
     */
    public List<String> exportAllWordLists(SQLiteDatabase db, File dir) {
        // hard coded name for export of all words
        Cursor c = db.query(MAIN_TABLE,new String[] {ID}, null, null, null, null,null);
        List<String> lst = new ArrayList<String>();
        String ret;
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                ret=exportWordList(c.getLong(c.getColumnIndexOrThrow(ID)), dir);
                if (ret!=null)
                    lst.add(ret);
                c.moveToNext();
            }
            
        }
        c.close();
        return lst;
    }

    /**
     * Lösche Wortliste
     * 
     * @param id
     *            ID der Wortliste
     */
    public void removeWordList(long id) {
        getWritableDatabase().delete(MAIN_TABLE, "_id=" + id, null);
    }

    /**
     * Removes all lists from database
     */
    public void removeAllList() {
        Cursor c = getWordListsCursor();
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                getWritableDatabase().delete(MAIN_TABLE,
                        "_id=" + c.getLong(c.getColumnIndex(ID)), null);
                c.moveToNext();
            }
            c.close();
        }
    }
    
    /**
     * Removes all lists from database
     */
    public void removeSelectedLists() {
        Cursor c = getSelectedWordListsCursor();
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                getWritableDatabase().delete(MAIN_TABLE,
                        "_id=" + c.getLong(c.getColumnIndex(ID)), null);
                c.moveToNext();
            }
            c.close();
        }
    }

    public int getNumberOfLists() {
        return getWordListsCursor().getCount();
    }

    // Hilfsfunktionen zur Konvertierung von Java-Listen in \n-getrennte Strings
    // und umgekehrt
    public static String listToString(List<String> list) {
        Iterator<String> iter = list.iterator();
        StringBuffer words = new StringBuffer(iter.next());
        while (iter.hasNext())
            words.append("\n").append(iter.next());
        return words.toString();
    }

    public static List<String> stringToList(String string) {
        return Arrays.asList(string.split("\n"));
    }

}
