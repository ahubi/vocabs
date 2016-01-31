package com.babasoft.vocabs;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;

public class DictCCScraper {
    //"http://my.dict.cc/lists/?f=blabla-inboth-DEEN"
    private final static String mBaseUri  = "http://my.dict.cc/lists/?f=";
    private final static String mSearchIn = "inboth";
    private static Map<String, List<ListRecord>> mCache=new HashMap<String, List<ListRecord>>();
    private static Map<String, List<DictRecord>> mCacheWords=new HashMap<String, List<DictRecord>>();
    private static List<String> mLangFilters = new ArrayList<String>();
    private static String mNextListLink="";
    private static String mPrevListLink="";
    
    public static class ListRecord {
        public String   langFrom;
        public String   langTo;
        public String   list;
        public String   listLink;
        public String   user;
        public String   userLink;
        public long     count;
    }

    public static class DictRecord implements Serializable{
        ///Left site word
        public String l;
        ///Right site word
        public String r;
    }

    public static List<String> getLangFilters() {
        return mLangFilters;
    }
    
    public static String getNextListLink(){
        return mNextListLink;
    }
    
    public static String getPrevListLink(){
        return mPrevListLink;
    }
    
    public static List<ListRecord> getLists(String reqURI, 
                                            String langFrom, 
                                            String langTo,
                                            String filter){
        if(!reqURI.contains("http"))
            reqURI = mBaseUri + filter + "-" + mSearchIn + "-" + langFrom + langTo;

        //Check the cache first
        if(mCache.containsKey(reqURI)){
            return mCache.get(reqURI);
        }else{
            Document doc;
            try {
                doc = Jsoup.connect(reqURI).get();
                
                long lStartTime = new Date().getTime(); //start time
                //language, list link, user link, count, copy to;
                Elements langtds = doc.select("td.td4nl,td.td3nl,td.td3");
                Elements prevnexttds = doc.select("td.td2nl");
                
                //Parse lang filter option box
                if(mLangFilters.size()<=0){
                    Elements options = doc.select("[name=slang] option");
                    for (int i = 0; i < options.size(); i++) {
                        print("Option value %s text %s",options.get(i).attr("value"),options.get(i).text());
                        mLangFilters.add(options.get(i).attr("value") + "|" + options.get(i).text());
                    }
                }
                
                long lEndTime = new Date().getTime(); //start time
                
                print("\nLangugage tds: (%d) ,time diff (%d) ms", 
                        langtds.size(),lEndTime-lStartTime);
                
                if (prevnexttds.size() > 0) {
                    Elements links = prevnexttds.get(0).select("a[href]");
                    for (int i = 0; i < links.size(); i++) {
                        if(i==0)
                            mPrevListLink = links.get(i).attr("abs:href");
                        else if(i==links.size()-1)
                            mNextListLink = links.get(i).attr("abs:href");
                    }
                    
                }
                
                if(langtds.size()>0){
                    List<ListRecord> lstRec = new ArrayList<ListRecord>();
                    
                    for (int i = 0; i+2 < langtds.size(); i=i+5) {
                        ListRecord rec = new ListRecord();
                        Elements links = langtds.get(i).select("a[href]");
                        if(links.size()>0){
                            rec.langFrom = links.get(0).text().trim().split("-", 10)[0];
                            rec.langTo = links.get(0).text().trim().split("-", 10)[1];
                        }
                        
                        links = langtds.get(i+1).select("a[href]");
                        if(links.size()>0){
                            rec.listLink    = links.get(0).attr("abs:href");
                            rec.list        = links.get(0).text();
                        }
                        
                        links = langtds.get(i+2).select("a[href]");
                        if(links.size()>0){
                            rec.userLink    = links.get(0).attr("abs:href");
                            rec.user        = links.get(0).text();
                            rec.count       = Integer.parseInt(langtds.get(i+3).text());
                        }
                        
                        lstRec.add(rec);
                    }
                    //mCache.put(reqURI, lstRec);
                    return lstRec;
                }
               
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static List<DictRecord> getWords(String reqURI) {
        // Check the cache first
        if (mCacheWords.containsKey(reqURI)) {
            return mCacheWords.get(reqURI);
        } else {
            Document doc;
            try {
                doc = Jsoup.connect(reqURI).get();
                long lStartTime = new Date().getTime(); //start time
                //get table trs
                Elements trs = doc.select("table.vokabelheft").select("tr");
                
                long lEndTime = new Date().getTime(); //end time

                print("\nVokabelheft trs: (%d) ,time diff (%d) ms",
                        trs.size(), lEndTime - lStartTime);
                
                if (trs.size() > 0) {
                    List<DictRecord> lstRec = new ArrayList<DictRecord>();

                    for (int i = 1; i < trs.size(); i = i+1) {
                        Elements tds = trs.get(i).select("td");
                        //at least 2 tds
                        if(tds.size()>1){
                            DictRecord rec = new DictRecord();
                            rec.l = tds.get(0).text();
                            rec.r = tds.get(1).text();
                            lstRec.add(rec);
                        }
                    }
                    mCacheWords.put(reqURI, lstRec);
                    return lstRec;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    private static void print(String msg, Object... args) {
        Log.i("WorTrainer",String.format(msg, args));
    }
}
