package com.babasoft.vocabs;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

class GoogleTranslate extends AsyncTask<TranslateReq, Void, String> {
    private static final int TIMEOUT = 3000;
    private static final String ENCODING = "UTF-8";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String BASE_URL = "https://translate.googleapis.com/translate_a/single?";
    private TranslationListener mCallback;
    private boolean mFailed = false;
    protected String doInBackground(TranslateReq... req) {
        mCallback=req[0].callback;
        try {
            return callUrlAndParseResult(req[0].from, req[0].to, req[0].toTranslate);
        } catch (Exception e) {
            mFailed = true;
            return e.getMessage();
        }
    }

    protected void onPostExecute(String res) {
        if(mCallback!=null)
            if(mFailed)
                mCallback.onError(-1,res);
            else
                mCallback.onComplete(res);
    }

    /*  src code taken from this post
        http://archana-testing.blogspot.de/2016/02/calling-google-translation-api-in-java.html
    */
    private String callUrlAndParseResult(String langFrom, String langTo,
                                         String word) throws Exception {
        String url = BASE_URL +
                    "client=gtx&" +
                    "sl=" + langFrom +
                    "&tl=" + langTo +
                    "&dt=t&q=" +
                    URLEncoder.encode(word, ENCODING);
        Log.i("RetrieveTranslationTask", "callUrlAndParseResult url: " + url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setReadTimeout(TIMEOUT);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        Log.i("EditWordRecord", "callUrlAndParseResult: " + response.toString());
        return parseResult(response.toString());
    }
    private String parseResult(String inputJson) throws Exception {
        /*
        * inputJson for word 'hello' translated to language Hindi from English-
        * [[["नमस्ते","hello",,,1]],,"en"]
        * We have to get 'नमस्ते ' from this json.
        */
        JSONArray jsArr = new JSONArray(inputJson);
        JSONArray jsArr2 = (JSONArray) jsArr.get(0);
        JSONArray jsArr3 = (JSONArray) jsArr2.get(0);
        return jsArr3.get(0).toString();
    }
}
