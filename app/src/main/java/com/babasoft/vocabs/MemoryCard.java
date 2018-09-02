package com.babasoft.vocabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.babasoft.vocabs.WordDB.WordList;
import com.babasoft.vocabs.WordDB.WordRecord;


public class MemoryCard extends Fragment implements Observer{
    private Context context;

    private static final int EDIT = Menu.FIRST; // akt. Wortliste bearbeiten
    private static final int LISTS = Menu.FIRST+1; // Wortlisten auswaehlen/anlegen/loeschen
    
    int XSIZE = 1;
    int YSIZE = 1;
    private WordDB mDB;
    protected SoundPool mSoundPool; // fuer Erfolgsfanfare
    protected int fanfareSoundID; // ID der geladenen Sound-Resource
    protected String mCurrentWordlist=null;
    protected List<WordRecord> mShuffledWords;                        //Words to be set on buttons
    protected String mCurrentPair;
    protected long mStartTime=0L;

    public MemoryCard(){};

    QuestSession mSession = new QuestSession();

    @Override
    public void onAttach(Context context) {
        Log.d(getClass().getName(), "onAttach called");
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(getClass().getName(), "onCreateView called");
        setHasOptionsMenu(true);

        mShuffledWords = new ArrayList<WordDB.WordRecord>();

        // Create start point
        if (Prefs.getFirstTime(getActivity()).length() == 0)
            Prefs.setFirstTime(getActivity(), Prefs.getDateTime());
        mDB = new WordDB(getActivity());

        // Inflate the layout for this fragment
        View retView = inflater.inflate(R.layout.memory, container, false);
        retView.setBackgroundColor(0x00000000); //black background
        int textSizeButtonOffset = Prefs.getButtonTextSize(getActivity())-16;
        //Add all buttons to list to traverse with a loop
        List<Button> buttonList = new ArrayList<Button>();
        buttonList.add((Button)retView.findViewById(R.id.button_main));
        buttonList.add((Button)retView.findViewById(R.id.button_ohoh));
        buttonList.add((Button)retView.findViewById(R.id.button_easy));
        buttonList.add((Button)retView.findViewById(R.id.button_solala));

        for (Button b:buttonList) {
            b.setTextSize(b.getTextSize()+textSizeButtonOffset);
            b.setTextColor(Prefs.getTextcolor(getActivity()));
            b.setBackgroundColor(Prefs.getButtoncolor(getActivity()));
            b.setBackgroundResource(Prefs.getButtonsForm(getActivity()));
        }
        setupListeners(retView);
        registerGestures(retView);
        return retView;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mShuffledWords = new ArrayList<WordDB.WordRecord>();
//
//        // Create start point
//        if (Prefs.getFirstTime(getActivity()).length() == 0)
//            Prefs.setFirstTime(getActivity(), Prefs.getDateTime());
//        mDB = new WordDB(getActivity());
//    }
    
    @Override
    public void onPause() {
        Log.d(getClass().getName(), "onPause called");
        Prefs.setLastWords(getActivity(), mSession.wordlistName);
        Prefs.setDuration(getActivity(), Prefs.getDuration(getActivity()) + System.currentTimeMillis() - mStartTime);
        //To be at the same place as before
        Prefs.setLastIndex(getActivity(), mSession.wordIndex);
        updateWordsScore();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(getClass().getName(), "onStop called");
        mDB.close();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "onDestroy called");
        super.onDestroy();
    }
    /**
     * Wird nach onCreate und nach dem Beenden einer anderen Activity aufgerufen.
     * Lade die aktuelle Wortliste. Wenn sie nicht mit der aktuellen Liste uebereinstimmt,
     * mische und setze alle Buttons neu.
     * Registriere fuer Accelerometer-Events. 
     */
    @Override
    public void onResume() {
        Log.d(getClass().getName(), "onResume called");
        loadAction(getView(),XSIZE, YSIZE);
        //updateTitle();
        mStartTime = System.currentTimeMillis();
        super.onResume();
    }
    
    /**
     * Gesten-Erkennung: Finger nach rechts bzw. links wischen laedt vorherige bzw. naechste Wortliste. 
     */
    private void registerGestures(View view) {
        // Gesten-Erkennung
        final GestureLibrary gesturelib = GestureLibraries.fromRawResource(getActivity(), R.raw.gestures);
        gesturelib.load();
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.Root);
        GestureOverlayView gestureOverlay = new GestureOverlayView(getActivity());
        gestureOverlay.setUncertainGestureColor(Color.TRANSPARENT); // nicht erkannte Gesten werden nicht auf den Screen gemalt
        gestureOverlay.setGestureColor(Color.TRANSPARENT);
        //layout.addView(gestureOverlay, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
        // haenge den LayoutView mit den Buttons unterhalb des Overlays
        //ViewGroup mainViewGroup = (ViewGroup) findViewById(R.id.LLMainButton);
        //layout.removeView(mainViewGroup);
        //gestureOverlay.addView(mainViewGroup);
        gestureOverlay.addOnGesturePerformedListener(new OnGesturePerformedListener() {         
            
            public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
                ArrayList<Prediction> predictions = gesturelib.recognize(gesture);
                for (Prediction prediction : predictions) {
                    if (prediction.score > 1.0) {
                        if (prediction.name.equals("right")){
                            previousAction(); // schiebe nach rechts aus dem Bild und hole den Vorgaenger
                            updateTitle();
                            break;
                        }
                        else if (prediction.name.equals("left")){
                            nextAction();
                            updateTitle();
                            break;
                        }
                        else
                        break;
                    }
                }
            }
        });
    }
   
    void skipList(boolean direction){
        long wordId = mDB.getAdjacentWordList(Prefs.getID(getActivity()), direction);
        Prefs.setID(getActivity(), wordId);
        Prefs.setLastIndex(getActivity(), 0);   
    }
    
    public void setupListeners(View view){
        Button button = (Button)view.findViewById(R.id.button_main);
        button.setOnClickListener(onButtonClick);
        
        button = (Button)view.findViewById(R.id.button_easy);
        button.setOnClickListener(onButtonClick);
        
        button = (Button)view.findViewById(R.id.button_ohoh);
        button.setOnClickListener(onButtonClick);
        
        button = (Button)view.findViewById(R.id.button_solala);
        button.setOnClickListener(onButtonClick);
    }
    
    final View.OnClickListener onButtonClick = new View.OnClickListener() {
        public void onClick(View v) {
            if (mShuffledWords.size() > 0) {
                Button b = (Button) v;
                switch (b.getId()) {
                case R.id.button_main:
                    toggleButtonState(b);
                    break;
                case R.id.button_easy:
                    updateButtonScore(3);
                    nextAction();
                    updateTitle();
                    break;
                case R.id.button_solala:
                    updateButtonScore(-1);
                    nextAction();
                    updateTitle();
                    break;
                case R.id.button_ohoh:
                    updateButtonScore(-3);
                    nextAction();
                    updateTitle();
                    break;
                default:
                    break;
                }
            }
        }
    };
    
    /**
     * aendere Zustand eines Buttons von nicht gesetzt auf gesetzt und
     * umgekehrt. Dabei wird die Button-Farbe entsprechend angepasst.
     * 
     * @param button
     *            der zu aendernde Button
     */
    public void toggleButtonState(Button button) {
        if (button.getText().equals(button.getTag(R.id.Tag1))) {
            applyRotation(button,true, 0.0f, 90.0f);
        } else {
            applyRotation(button,false, 0.0f, -90.0f);
        }
    }

    void updateButtonScore(int score) {
        Button b = (Button)getView().findViewById(R.id.button_main);
        int i = (Integer)b.getTag(R.id.Tag3);
        mShuffledWords.get(i).score += score;
        mShuffledWords.get(i).dirty = 1;
    }
    private void applyRotation(Button b, boolean firstView, float start, float end) {
        // Find the center of image
        final float centerX = b.getWidth() / 2.0f;
        final float centerY = b.getHeight() / 2.0f;
         
        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(300);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(firstView,b));
        b.startAnimation(rotation);
    }

    public void nextAction() {
        if (mShuffledWords.size() > 0) {
            Button button;
            if (mSession.wordIndex >= mSession.wordsToGo) {
                mSession.wordIndex = 0;
                mSession.wordsDone = 0;
            }
            String w1 = mShuffledWords.get(mSession.wordIndex).w1;
            String w2 = mShuffledWords.get(mSession.wordIndex).w2;
            button = (Button) getView().findViewById(R.id.button_main);
            button.setText(getAnswerWord(w1, w2));
            button.setTag(R.id.Tag1, getAnswerWord(w1, w2));
            button.setTag(R.id.Tag2, getQuestionWord(w1, w2));
            button.setTag(R.id.Tag3, mSession.wordIndex);
            button.setTag(mShuffledWords.get(mSession.wordIndex));
            mSession.wordIndex++;
            mSession.wordsDone = mSession.wordIndex;
            mSession.lastDirection = QuestSession.LAST_DIRECTION.FORWARD;
        }
    }

    public void previousAction() {
        Button button;
        mSession.wordIndex--;
        if (mSession.wordIndex < 0) {
            long wordId = mDB.getAdjacentWordList(Prefs.getID(getActivity()), false);
            WordList wordlist = mDB.getWordList(wordId);

            if (wordlist != null) {
                updateWordsScore();
                mShuffledWords = mDB.getWords(wordId);
                mSession.wordlistName = wordlist.title;
                mSession.wordsToGo = mShuffledWords.size();
                mSession.wordsDone = mShuffledWords.size();
                mSession.wordIndex = mShuffledWords.size() - 1;
                mSession.correctInSession = 0;
                Prefs.setID(getActivity(), wordId);
            } else
                return; // no more lists
        }
        String w1 = mShuffledWords.get(mSession.wordIndex).w1;
        String w2 = mShuffledWords.get(mSession.wordIndex).w2;
        button = (Button) getView().findViewById(R.id.button_main);
        button.setText(getAnswerWord(w1, w2));
        button.setTag(R.id.Tag1, getAnswerWord(w1, w2));
        button.setTag(R.id.Tag2, getQuestionWord(w1, w2));
        button.setTag(R.id.Tag3, mSession.wordIndex);
        button.setTag(mShuffledWords.get(mSession.wordIndex));
        if (mSession.wordIndex < 0)
            mSession.wordIndex = 0;
        mSession.wordsDone = mSession.wordIndex;
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

    public void loadAction(View view,int xs, int ys) {
        List<Long> lst = mDB.getSelectedWordLists();
        mShuffledWords.clear();
        if (lst.size() <= 0){ 
            long id = mDB.getFirstWordListID();
            mShuffledWords = mDB.getWords(mDB.getFirstWordListID());
            WordList wl = mDB.getWordList(id);
            if(wl!=null)
                mSession.wordlistName = wl.title;
            else{
                Toast.makeText(getActivity(), R.string.NoMoreWordlists, Toast.LENGTH_LONG).show();
                return;
            }
        }
        else{
            for (int i = 0; i < lst.size(); i++)
                mShuffledWords.addAll(mDB.getWords(lst.get(i)));
            
            mSession.wordlistName = mDB.getWordList(lst.get(0)).title + "*";
        }
        mSession.wordsToGo = mShuffledWords.size();
        mSession.correctInSession = 0;
        mSession.wordIndex = 0;
        Button button = (Button) view.findViewById(R.id.button_main);
        button.setText("");
        if (mShuffledWords.size() > 0) {
            String w1 = mShuffledWords.get(mSession.wordIndex).w1;
            String w2 = mShuffledWords.get(mSession.wordIndex).w2;
            button = (Button) view.findViewById(R.id.button_main);
            button.setText(getAnswerWord(w1, w2));
            button.setTag(R.id.Tag1, getAnswerWord(w1, w2));
            button.setTag(R.id.Tag2, getQuestionWord(w1, w2));
            button.setTag(R.id.Tag3, mSession.wordIndex);
            button.setTag(mShuffledWords.get(mSession.wordIndex));
            mSession.wordIndex++;
            mSession.wordsDone = mSession.wordIndex;
        }
    }
    protected void updateWordsScore(){
        mDB.updateWordsScore(mShuffledWords, Prefs.getListAutoSort(getActivity()));
    }
    protected void updateTitle(){
        getActivity().setTitle(mSession.wordlistName + " " + "[" +
                Long.toString(mSession.wordsDone) + "/" + 
                Integer.toString(mShuffledWords.size()) + "]");
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(getClass().getName(), "onCreateOptionsMenu called");
        inflater.inflate(R.menu.memorycard_activity_actions, menu);
        updateTitle();
    }

    /**
     * Auswahl eines Menue-Eintrags
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(getClass().getName(), "onOptionsItemSelected called");

        switch (item.getItemId()) {
        case R.id.action_compose: {
            Button button = (Button) getView().findViewById(R.id.button_main);
            if (button != null) {
                WordRecord wr = (WordRecord) button.getTag();
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
        Log.d(getClass().getName(), "onActivityResult called");
        if (requestCode == LISTS && resultCode == Activity.RESULT_OK) {
            // eine andere Liste wurde ausgewaehlt
            long id = data.getExtras().getLong(WordLists.ID);
            Prefs.setID(getActivity(), id);
            Prefs.setLastIndex(getActivity(), 0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void update(Observable o, Object arg) {
        Log.d(getClass().getName(), "update from observer called");
        updateTitle();
    }
}
