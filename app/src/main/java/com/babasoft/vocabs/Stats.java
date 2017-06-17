package com.babasoft.vocabs;

import com.babasoft.vocabs.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class Stats extends Fragment {

    public Stats() {
        // TODO Auto-generated constructor stub
    }
    
    private EditText first_time;
    private EditText last_time;
    private EditText train_duration;
    //private EditText incorrect_answers;
    private EditText correct_answers;
    private EditText last_words;
    private EditText ratio_edittext;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.stats, container, false);
        first_time = (EditText) view.findViewById(R.id.FirstTime);
        last_time = (EditText) view.findViewById(R.id.LastTime);
        train_duration= (EditText) view.findViewById(R.id.Duration);
        last_words= (EditText) view.findViewById(R.id.LastWords);
        correct_answers= (EditText) view.findViewById(R.id.CorrectAnswers);
        ratio_edittext= (EditText) view.findViewById(R.id.Ratio);
        return view;
    }
        
    @Override
    public void onResume() {
        super.onResume();
        long correct = Prefs.getCorrectAnswers(getActivity());
        long incorrect = Prefs.getIncorrectAnswers(getActivity());
        long ratio =0;
        
        if(incorrect>0 || correct>0)
            ratio = correct * 100 / (correct +incorrect);
        
        first_time.setText(getString(R.string.FirstTime) + ": " + Prefs.getFirstTime(getActivity()));
        last_time.setText(getString(R.string.LastTime) + ": " + Prefs.getLastTime(getActivity()));
        last_words.setText(getString(R.string.LastWords) + ": " + Prefs.getLastWords(getActivity()));
        train_duration.setText(getString(R.string.TotalDuration) + ": " + Long.toString(Prefs.getDuration(getActivity())/(1000*60)) + " min");
        
        correct_answers.setText(getString(R.string.TotalCorrect) + " " +
                                Long.toString(correct) + " " +
                                getString(R.string.TotalWrong) + " " +
                                Long.toString(incorrect));
        
        ratio_edittext.setText(getString(R.string.Ratio) + ": " +
                Long.toString(ratio) + "%");
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
