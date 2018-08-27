package com.babasoft.vocabs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.GestureStore;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.babasoft.vocabs.WordDB.WordList;
import com.babasoft.vocabs.WordDB.WordRecord;

public class MultipleChoice extends Fragment implements Observer{
    private Context context;

    // menu IDs
    private static final int EDIT = Menu.FIRST; // akt. Wortliste bearbeiten
    private static final int LISTS = Menu.FIRST+1; // Wortlisten auswaehlen/anlegen/loeschen
    
    public static final int LINEARLAYOUTID    =   0xff00;
    public static final int ID2               =   0xff02;
    public static final int QUESTID           =   0xff01;
    private SoundPool mSoundPool; // fuer Erfolgsfanfare
    protected List<WordRecord> mCurrentWords;                         //Words to be picked up for questions
    protected List<WordRecord> mShuffledWords;                        //Words to be set on buttons
    protected int mCurrentIndex=-1;
    protected Button mAnswerButton;
    protected int fanfareSoundID; // ID der geladenen Sound-Resource
    protected long mStartTime=0L;
    protected WordDB mDB;
    
    int XSIZE = 2;
    int YSIZE = 2;
    
    public MultipleChoice(){};

    QuestSession mSession = new QuestSession();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d(getClass().getName(), "onCreateView");

        XSIZE = Prefs.getXButtons(getActivity());
        YSIZE = Prefs.getYButtons(getActivity());
        
        mShuffledWords = new ArrayList<WordDB.WordRecord>();
        mCurrentWords = new ArrayList<WordDB.WordRecord>();
        
        View view = createUIDynamically(inflater, container); // dynamisch

        // Create start point
        if (Prefs.getFirstTime(getActivity()).length()==0)
            Prefs.setFirstTime(getActivity(), Prefs.getDateTime());


        // rechts/links Wischen zum Wechsel der Wortliste
        //registerGestures(view);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDB = new WordDB(getActivity());
        setupListeners(view);
        loadAction(view,XSIZE, YSIZE);
    }
    private View createUIDynamically(LayoutInflater inflater, ViewGroup container) {
        // wichtig: für LayoutParams immer die passende Layout-Klasse verwenden
        int textSizeButtonOffset = Prefs.getButtonTextSize(getActivity());
        //LinearLayout retView = new LinearLayout(getActivity());
        LinearLayout retView = (LinearLayout)inflater.inflate(R.layout.multiplechoice, container, false);
        retView.setId(ID2);
        LinearLayout main = new LinearLayout(getActivity());
        main.setId(LINEARLAYOUTID);
        main.setOrientation(LinearLayout.VERTICAL);

        retView.addView(main, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));

        LinearLayout rowTextView = new LinearLayout(getActivity());

        TextView tQuest = new TextView(getActivity());

        tQuest.setId(QUESTID);
        tQuest.setGravity(Gravity.CENTER_HORIZONTAL);
        //tQuest.setGravity(Gravity.CENTER_VERTICAL);
        tQuest.setTextColor(Color.GREEN);
        tQuest.setTextSize(tQuest.getTextSize() + Prefs.getQuestionTextSize(getActivity()));

        rowTextView.setGravity(Gravity.CENTER_VERTICAL);
        rowTextView.addView(tQuest, new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.35f));

        main.addView(rowTextView, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 0.35f));

        Drawable shape = getResources().getDrawable(Prefs.getButtonsForm(getActivity()));
        shape.clearColorFilter();
        shape.setColorFilter(Prefs.getButtoncolor(getActivity()), PorterDuff.Mode.SRC);

        for (int y = 0; y < YSIZE; y++) {
            LinearLayout row = new LinearLayout(getActivity());
            for (int x = 0; x < XSIZE; x++) {
                Button button = new Button(getActivity());
                // die ID wird hier der Einfachheit halber aus den generierten
                // R-Konstanten
                // gesetzt, damit das restliche Coding weiter funktioniert.
                button.setId(getButtonID(x, y));
                button.setWidth(0);
                button.setHeight(0);
                button.setTextSize(button.getTextSize()+textSizeButtonOffset);
                //button.setEllipsize(TextUtils.TruncateAt.END);
                button.setBackgroundDrawable(shape);
                button.setTextColor(Prefs.getTextcolor(getActivity()));
                row.addView(button, new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1)); // weight
                // 1
            }
            main.addView(row, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
        }
        return retView;
    }

    @Override
    public void onResume() {
        loadAction(getView(),XSIZE, YSIZE);
        mStartTime = System.currentTimeMillis();
        //updateTitle();
        super.onResume();
    }
    
    @Override
    public void onPause() {
        Prefs.setIncorrectAnswers(getActivity(), Prefs.getIncorrectAnswers(getActivity())+mSession.wrongAnswers);
        Prefs.setCorrectAnswers(getActivity(), Prefs.getCorrectAnswers(getActivity())+mSession.correctAnswers);
        Prefs.setLastWords(getActivity(), mSession.wordlistName);
        Prefs.setDuration(getActivity(), Prefs.getDuration(getActivity()) + System.currentTimeMillis() - mStartTime);
        updateWordsScore();
        super.onPause();
    }
    
    @Override
    public void onStop(){
        mDB.close();
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    /**
     * Gesten-Erkennung: Finger nach rechts bzw. links wischen laedt vorherige bzw. naechste Wortliste. 
     */
    private void registerGestures(View view) {
        // Gesten-Erkennung
        final GestureLibrary gesturelib = GestureLibraries.fromRawResource(getActivity(), R.raw.gestures);
        gesturelib.setSequenceType(GestureStore.SEQUENCE_INVARIANT);
        gesturelib.load();
        LinearLayout layout = (LinearLayout) view.findViewById(ID2);
        GestureOverlayView gestureOverlay = new GestureOverlayView(getActivity());
        gestureOverlay.setUncertainGestureColor(Color.TRANSPARENT); // nicht erkannte Gesten werden nicht auf den Screen gemalt
        gestureOverlay.setGestureColor(Color.TRANSPARENT);
        layout.addView(gestureOverlay, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
        // haenge den LayoutView mit den Buttons unterhalb des Overlays
        ViewGroup mainViewGroup = (ViewGroup) view.findViewById(LINEARLAYOUTID);
        layout.removeView(mainViewGroup);
        gestureOverlay.addView(mainViewGroup);
        gestureOverlay.addOnGesturePerformedListener(new OnGesturePerformedListener() {         
            
            public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
                ArrayList<Prediction> predictions = gesturelib.recognize(gesture);
                for (Prediction prediction : predictions) {
                    if (prediction.score > 1.0) {
                        if (prediction.name.equals("up")){
                            previousAction(); // schiebe nach rechts aus dem Bild und hole den Vorgaenger
                        }
                        else if (prediction.name.equals("down")){
                            nextAction();
                        }
                        else if (prediction.name.equals("Shuffle")){
                            shuffleAction(getView(),true);
                        }
                        else if (prediction.name.equals("right")){
                            shuffleAction(getView(),true);
                        }
                        else if (prediction.name.equals("left")){
                            shuffleAction(getView(),true);
                        }
                        else{}
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * Spiele vorher im mSoundPool geladenen Sound ab.
     */
    protected void playFanfare() {
        if (Prefs.getSound(getActivity())) {
            AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            // berechne relative Lautstaerke, basierend auf vom Benutzer
            // eingestellter Lautstaerke und Maximum (int-Werte!)
            float volume = (float) audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC)
                    / (float) audioManager
                            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // spiele den in onCreate geladenen Sound
            mSoundPool.play(fanfareSoundID, volume, volume, 1, 0, 1f);
        }
    }
    

    final View.OnClickListener onButtonClick=new View.OnClickListener() {
        
        public void onClick(View v) {
            clickAction((Button)v);
        }
    };
    
    private void setupListeners(View view){
        // verknuepfe Buttons mit Aktionen  
        for (int y = 0; y < YSIZE; y++){
            
            for (int x = 0; x < XSIZE; x++) {
                Button button = (Button) view.findViewById(getButtonID(x, y));               
                button.setOnClickListener(onButtonClick); 
            }
        }
    }

    public void clickAction(Button b) {
        if(mCurrentIndex<0)
            return;
        checkAnswer(b);
        shuffleAction(getView(),false);
    }

    public void nextAction() {
        long nextid = mDB.getAdjacentWordList(Prefs.getID(getActivity()),true); // naechste Wortliste
      
      if (nextid == 0)
          nextid = mDB.getFirstWordListID();
      
      Prefs.setID(getActivity(),nextid); // neu gesetzte Wortliste wird am Ende der Animation geladen (s.u.)
      loadAction(getView(),XSIZE, YSIZE);
    }

    public void previousAction() {
        long nextid = mDB.getAdjacentWordList(Prefs.getID(getActivity()), false); // vorhergehende
        if (nextid == 0)
            nextid = mDB.getLastWordListID();

        Prefs.setID(getActivity(), nextid);
        loadAction(getView(),XSIZE, YSIZE);
    }

    private void shuffleAction(View view,boolean animate) {
        int i=0;
                
        /*Make a copy of current active words
         * To be able to ask linearly
         */
        Collections.shuffle(mShuffledWords);
        
        //Generate randow button coordinates
        Random rnd = new Random();
        int rndX = rnd.nextInt(XSIZE);
        int rndY = rnd.nextInt(YSIZE);
        Button button;
        
        mCurrentIndex   = getNextQuestionPair();
        if(mCurrentIndex<0)
            return;
        
        String qA = getAnswerWord(mCurrentWords.get(mCurrentIndex).w1,mCurrentWords.get(mCurrentIndex).w2);
        
        mAnswerButton=(Button) view.findViewById(getButtonID(rndX, rndY));
        mAnswerButton.setText(qA);
        mAnswerButton.setTag(mCurrentWords.get(mCurrentIndex));
        
        TextView tQuestion = (TextView)view.findViewById(QUESTID);
        tQuestion.setText(getQuestionWord(mCurrentWords.get(mCurrentIndex).w1,mCurrentWords.get(mCurrentIndex).w2));
        
        for (int x = 0; x < XSIZE; x++)
        {
            
            for (int y = 0; y < YSIZE; y++)
            {
                if(rndX==x && rndY==y)
                    continue;
                
                button=(Button) view.findViewById(getButtonID(x, y));
                
                if(i>=mShuffledWords.size()){
                    // overflow
                    Collections.shuffle(mShuffledWords);
                    i=0;
                }
                
                //Get words on buttons and check for duplicates
                if(qA.compareTo(getAnswerWord(mShuffledWords.get(i).w1,mShuffledWords.get(i).w2))==0)
                    i++;
                
                i=i%mShuffledWords.size();
                button.setText(getAnswerWord(mShuffledWords.get(i).w1,mShuffledWords.get(i).w2));
                button.setTag(R.id.Tag1,getAnswerWord(mShuffledWords.get(i).w1,mShuffledWords.get(i).w2));
                button.setTag(R.id.Tag2,getQuestionWord(mShuffledWords.get(i).w1,mShuffledWords.get(i).w2));
                
                i++;
            }
        }
        
        if(animate) {
            ViewGroup mainViewGroup = (ViewGroup) view.findViewById(LINEARLAYOUTID);
            for (int j = 0; j < mainViewGroup.getChildCount(); j++) {
                ViewGroup rowViewGroup = (ViewGroup) mainViewGroup.getChildAt(j);
                rowViewGroup.startLayoutAnimation();
            }   
        }
        mSession.wordIndex++;
        
        if(mSession.wordIndex>mCurrentWords.size())
            mSession.wordIndex=0;   

    }

    private void loadAction(View view, int x, int y) {
        XSIZE = x;
        YSIZE = y;
        Button bt;
        //Clear previous content
        TextView tQuestion = (TextView)view.findViewById(QUESTID);
        tQuestion.setText("");
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                bt=(Button) view.findViewById(getButtonID(i, j));
                bt.setText("");
            }
        }
        List<Long> lst = mDB.getSelectedWordLists();
        
        mShuffledWords.clear();
        mCurrentWords.clear();
        
        // If no list is selected get first list
        if (lst.isEmpty()) {
            // keine Wortliste zur ID gefunden, nimm erste
            long id = mDB.getFirstWordListID();
            WordList wl = mDB.getWordList(id);
            if (wl != null) {
                Prefs.setID(getActivity(), id);
                wl = mDB.getWordList(id);
                mShuffledWords = mDB.getWords(wl.id);
                mCurrentWords = mDB.getWords(wl.id);
                mSession.wordlistName = wl.title;
            }else{
                Toast.makeText(getActivity(), R.string.NoMoreWordlists, Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            for (int i = 0; i < lst.size(); i++) {
                mShuffledWords.addAll(mDB.getWords(lst.get(i)));
                mCurrentWords.addAll(mDB.getWords(lst.get(i)));
            }
            mSession.wordlistName = mDB.getWordList(lst.get(0)).title + "*";
        }
        // delete asked word counter
        mCurrentIndex = 0;
        mSession.wordsToGo = mCurrentWords.size();
        mSession.wordsDone = 0;
        mSession.correctInSession = 0;
        shuffleAction(view,false);
        //updateTitle();
    }
    
    protected void playNotification(int answer) {

        switch (answer) {
        case 0:
            Uri rTone = Uri.parse(Prefs.getSoundF(getActivity()));
            if (!rTone.toString().equals("Silent"))
                RingtoneManager.getRingtone(getActivity(), rTone).play();

            break;

        case 1:
            rTone = Uri.parse(Prefs.getSoundR(getActivity()));
            if (!rTone.toString().equals("Silent"))
                RingtoneManager.getRingtone(getActivity(), rTone).play();

            break;
        case 3:
            if (Prefs.getSound(getActivity()))
                playFanfare();

            break;

        default:
            break;
        }

    }

    protected void vibrate(boolean vibrate) {

        if (vibrate) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    protected boolean isButtonSet(Button button) {
        return button.getTag() != null;
    }

    /**
     * Liefere numerische Button-ID per Reflection aus den Konstanten der
     * R-Klasse
     * 
     * @param x
     *            Spaltennummer 0..4
     * @param y
     *            Zeilennummer 0..4
     * @return
     */
    protected int getButtonID(int x, int y) {
        try {
            String buttonid = (y +1) + "" + (x + 1);
            return Integer.parseInt(buttonid);
        } catch (Exception e) {
            // reflection lookup could throw exceptions e.g. if the button is
            // not found
            throw new RuntimeException("Internal error", e);
        }
    }
    
    protected void updateTitle(){
        String title = mSession.wordlistName + " " + "[" +
                Long.toString(mSession.wordsDone) + "/" +
                Long.toString(mSession.wordsToGo) + "]" + " " +
                getActivity().getText(R.string.False)  +
                Long.toString(mSession.wrongAnswers) + " " +
                getActivity().getText(R.string.Right) +
                Long.toString(mSession.correctAnswers);
        Log.d(getClass().getName(), "setting title:" + title);
        getActivity().setTitle(title);
    }
    
    protected void updateTimeInfo(long timeVal){
        updateTitle();
        getActivity().setTitle(getActivity().getTitle() + " " + "[" + Long.toString(timeVal) + "]");
    }
    
    //Return question word depending on inverse language settings
    protected String getQuestionWord(String w1, String w2)
    {
        if(Prefs.getInverseLang(getActivity()))
            return w1;
        else
            return w2;
    }
    
    //Return answer word depending on inverse language settings
    protected String getAnswerWord(String w1, String w2)
    {
        if(Prefs.getInverseLang(getActivity()))
            return w2;
        else
            return w1;
    }
    
    protected int getNextQuestionPair() {
        //stay beyond list size
        if (!mCurrentWords.isEmpty()) {
            mCurrentIndex++;
            if (mCurrentIndex >= mCurrentWords.size()) {
                /*
                 * Shuffle words to be asked to have a different order on a wrap
                 * around
                 */
                Collections.shuffle(mCurrentWords);
                mCurrentIndex = 0;
            }
        }else
            mCurrentIndex=-1;
        
        return mCurrentIndex;
    }
    protected void checkAnswer(Button v){
        
        if(getAnswerWord(mCurrentWords.get(mCurrentIndex).w1,
                         mCurrentWords.get(mCurrentIndex).w2).compareTo(v.getText().toString())==0){
            mSession.correctAnswers++;
            mSession.correctInSession++;
            getActivity().setTitleColor(Color.GREEN);
            vibrate(Prefs.getVibrateR(getActivity()));
            playNotification(1);
            mCurrentWords.get(mCurrentIndex).dirty=1;
            mCurrentWords.get(mCurrentIndex).score++;
        }
        else{
            mSession.wrongAnswers++;
            getActivity().setTitleColor(Color.WHITE);
            vibrate(Prefs.getVibrateF(getActivity()));
            playNotification(0);
            showHint();
            mCurrentWords.get(mCurrentIndex).dirty=1;
            mCurrentWords.get(mCurrentIndex).score--;
        }
        
        mSession.wordsDone++;
        updateTitle();
    }
    
    protected void updateWordsScore() {
        if (mCurrentWords != null)
            mDB.updateWordsScore(mCurrentWords, Prefs.getListAutoSort(getActivity()));
    }
    
    private void showHint(){
        Toast.makeText(getActivity(), getQuestionWord(mCurrentWords.get(mCurrentIndex).w1,
                                             mCurrentWords.get(mCurrentIndex).w2) + 
                       " = " + getAnswerWord(mCurrentWords.get(mCurrentIndex).w1,
                                             mCurrentWords.get(mCurrentIndex).w2) , 
                       Toast.LENGTH_LONG).show();
    }
    
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.memorycard_activity_actions, menu);
    }

    /**
     * Auswahl eines Menue-Eintrags
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_compose: {
            if (mAnswerButton != null) {
                WordRecord wr = (WordRecord) mAnswerButton.getTag();
                Intent intent = new Intent(getActivity(), EditWordRecord.class);
                intent.putExtra("id", wr.id);
                startActivity(intent);
            }
        }
        break;
        }
        return super.onOptionsItemSelected(item);
    }

/**
 * Wird nach Beenden einer per <code>startActivityForResult</code> gestarteten 
 * Activity gerufen 
 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LISTS) {
            loadAction(getView(), XSIZE, YSIZE);
            updateTitle();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d(getClass().getName(), "update from observer called");
        updateTitle();
    }
}
