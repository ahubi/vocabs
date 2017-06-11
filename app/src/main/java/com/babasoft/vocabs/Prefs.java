package com.babasoft.vocabs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.babasoft.vocabs.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Verwaltung von Benutzereinstellungen
 *
 */
public class Prefs {
	private static final String ID="id";
	private static final String SOUND_R="ToneSelectorR";
	private static final String SOUND_F="ToneSelectorF";
	private static final String BUTTONX="ButtonsX";
	private static final String BUTTONY="ButtonsY";
	private static final String TRAINMODE="trainmode";
	private static final String INVERSELANG="inverselang";
	private static final String VIBRATE="VibrateF";
	private static final String VIBRATE_R="VibrateR";
	private static final String BUTTONFORMS = "ButtonFormKey";
	private static final String BUTTONCOLOR = "ButtonColor";
	private static final String TEXTCOLOR = "ButtonTextColor";
	private static final String TEXTSIZE_BUTTON = "TextSizeKey";
	private static final String TEXTSIZE_QUESTION = "TextSizeQuestKey";
    private static final String AUTO_SORT = "listautosort";
	
    //Statistics values, not in prefs screen used
    private final static String FIRST_TIME      ="first_time";          //first time training started
    private final static String LAST_TIME       ="last_time";           //last time trainer used
    private final static String TRAIN_DURATION  ="train_duration";      //training duration
    private final static String INCORRECT_ANS   ="incorrect_answers";   //Incorrect answers
    private final static String CORRECT_ANS     ="correct_answers";     //Correct answers
    private final static String LAST_WORDS      ="last_words";     //Correct answers
    private final static String LAST_INDEX      ="last_index";
	
    
    public static int getLastIndex(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt(LAST_INDEX,0);
    }
    
    public static void setLastIndex(Context ctx, int val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_INDEX, val);
        editor.commit();
    }
    
    public static int getButtoncolor(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt(BUTTONCOLOR, 0xFFFFFF7A/*R.color.DefaultButtonColor*/);
    }
    
    public static void setButtoncolor(Context ctx, int c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(BUTTONCOLOR, c);
        editor.commit();
    }
    
    public static int getTextcolor(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getInt(TEXTCOLOR, 0xFF000000/*R.color.DefaultTextColor*/);
    }
    
    public static void setTextcolor(Context ctx, int c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(TEXTCOLOR, c);
        editor.commit();
    }
    
    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public static void setLastWords(Context ctx,String val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_WORDS, val);
        editor.commit();
    }
    
    public static String getLastWords(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(LAST_WORDS,""); 
    }
    
    public static long getCorrectAnswers(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong(CORRECT_ANS,0); 
    }
    
    public static void setCorrectAnswers(Context ctx, long val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(CORRECT_ANS, val);
        editor.commit();
    }
    
    public static long getIncorrectAnswers(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong(INCORRECT_ANS,0);
    }
    
    public static void setIncorrectAnswers(Context ctx, long val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(INCORRECT_ANS, val);
        editor.commit();
    }
    
    public static long getDuration(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong(TRAIN_DURATION,0);
    }
    
    public static void setDuration(Context ctx, long val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(TRAIN_DURATION, val);
        editor.commit();
    }
    
    public static String getLastTime(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(LAST_TIME,getDateTime());
    }
    
    public static void setLastTime(Context ctx,String val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_TIME, val);
        editor.commit();
    }
    
    public static String getFirstTime(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(FIRST_TIME,"");
    }
    
    public static void setFirstTime(Context ctx,String val){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FIRST_TIME, val);
        editor.commit();
    }
    
	public static void setID(Context ctx,long id)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(ID, id);
		editor.commit();
	}
	public static long getID(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getLong(ID,0);	// default ist 0, d.h. keine ID	
	}
	
	public static void setSoundR(Context ctx,String soundR)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SOUND_R, soundR);
		editor.commit();
	}
	
	public static String getSoundR(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getString(SOUND_R,"Silent");	// default ist true, d.h. kein Sound
	}
	
	public static void setSoundF(Context ctx,String soundF)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SOUND_F, soundF);
        editor.commit();
    }
    
    public static String getSoundF(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        // Get the custom preference
        return prefs.getString(SOUND_F,"Silent");   // default ist true, d.h. kein Sound
    }
    
	public static void setVibrateF(Context ctx,boolean vibrate)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(VIBRATE, vibrate);
        editor.commit();
    }
    public static boolean getVibrateF(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(VIBRATE,true);  // default ist true, d.h. kein Sound    
    }
    
    public static void setVibrateR(Context ctx,boolean vibrate)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(VIBRATE_R, vibrate);
        editor.commit();
    }
    public static boolean getVibrateR(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(VIBRATE_R,false);  // default ist true, d.h. kein Sound    
    }
    
	public static void setTrainMode(Context ctx,boolean nosound)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(TRAINMODE, nosound);
		editor.commit();
	}
	
	public static boolean getTrainMode(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(TRAINMODE,true);	// default ist true, d.h. kein Sound	
	}
	
	public static void setInverseLang(Context ctx,boolean nosound)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(INVERSELANG, nosound);
		editor.commit();
	}
	
	public static boolean getListAutoSort(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getBoolean(AUTO_SORT,false);
	}

    public static void setListAutoSort(Context ctx, boolean listautosort)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(AUTO_SORT, listautosort);
        editor.commit();
    }

    public static boolean getInverseLang(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(INVERSELANG,false);	// default ist true, d.h. kein Sound
    }

	public static int getXButtons(Context ctx){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String tmpStr = prefs.getString(BUTTONX, "2");
		
		int ret = Integer.parseInt(tmpStr);
		
		if(ret>5 || ret<0)
			return 2;
		else
			return ret;
	}
	
	public static int getYButtons(Context ctx){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int ret = Integer.parseInt(prefs.getString(BUTTONY, "2"));
		if(ret>5 || ret<0)
			return 2;
		else
			return ret;
	}
	
	public static int getButtonTextSize(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        int ret = Integer.parseInt(prefs.getString(TEXTSIZE_BUTTON, "2"));
        if(ret<0)
            return 2;
        else
            return ret;
    }
	
	public static int getQuestionTextSize(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        int ret = Integer.parseInt(prefs.getString(TEXTSIZE_QUESTION, "6"));
        if(ret<0)
            return 6;
        else
            return ret;
    }
	
    public static int getButtonsForm(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String form = prefs.getString(BUTTONFORMS, "rectangle");
        if(form.compareTo("rectangle")==0)
            return R.drawable.rectangle;
        else
            return R.drawable.oval;
    }
	   
	public static boolean getSound(Context ctx)
	{
	    return true;
	}
	public static long getTimeOut(){
	    return 15000;
	}
}
