package com.babasoft.vocabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.memetix.mst.language.Language;

public class SupportedLanguage{
    private static Language languages[];
    private static SupportedLanguage INSTANCE = new SupportedLanguage();
    
    public static SupportedLanguage getInstance() {
        return INSTANCE;
    }
    
    /**
    * Enum constructor.
    */
    private SupportedLanguage() {
        languages=Language.values();
    }
    
    /**
     * @return Language display name corresponding to user local settings
     */
    final private static String getDisplayLanguage(final String languageCode){
        if(languageCode.length()==0)
            return "AUTODETECT";
        Locale l = new Locale(languageCode);
        return l.getDisplayName() + " (" + languageCode +")";
    }
    
    public static List<String> getLanguages() {
        List<String> l = new ArrayList<String>();
        for (Language language : languages) {
            l.add(getDisplayLanguage(language.toString()));
        }
        return l;
    }
    
    public static Language getSupportedLanguage(final String l){
        for(Language lang:languages){
            if (lang.toString().equals(l))
                return lang;
        }
        return null;
    }
    
    /**
    * Returns language string from display language.
    * @param DisplayLanguage string.
    */
    public static String parseLanguage(final String dl){
        String split[]=dl.split("[(]");
        if(split.length>1)
        {
          return split[1].replace(")", "").trim();  
        }
        return "";
    }
}
