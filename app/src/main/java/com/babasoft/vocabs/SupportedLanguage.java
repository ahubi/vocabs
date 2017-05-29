package com.babasoft.vocabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class SupportedLanguage{
    private static Language languages[];
    private static SupportedLanguage INSTANCE = new SupportedLanguage();
    private  static List<String> mSortedLanguages;
    public static SupportedLanguage getInstance() {
        return INSTANCE;
    }
    
    /**
    * Enum constructor.
    */
    private SupportedLanguage(){
        languages=Language.values();
        mSortedLanguages = new ArrayList<String>();
        for (Language language : languages) {
            mSortedLanguages.add(getDisplayLanguage(language.toString()));
        }
        Collections.sort(mSortedLanguages);
        //Put auto always at first place
        mSortedLanguages.remove("auto (auto)");
        mSortedLanguages.add(0,"auto (auto)");

    }
    
    /**
     * @return Language display name corresponding to user local settings
     */
    final private static String getDisplayLanguage(final String languageCode){
        if(languageCode.length()==0) //return auto as "Auto(auto)"
            return "auto(auto)";
        Locale l = new Locale(languageCode);
        return l.getDisplayName() + " (" + languageCode +")";
    }
    
    public static List<String> getLanguages() {
        return mSortedLanguages;
    }
    
    public static Language getSupportedLanguage(final String l){
        for(Language lang:languages){
            if (lang.toString().equals(l))
                return lang;
        }
        return null;
    }
    public static int getLanguagePosition(final String l){
        return mSortedLanguages.indexOf(getDisplayLanguage(l));
    }
    
    /**
    * Returns language string from display language.
    * @param dl string.
    */
    public static String parseLanguage(final String dl){
        String split[]=dl.split("[(]");
        if(split.length>1)
        {
          return split[1].replace(")", "").trim();  
        }
        return "auto";
    }
}
