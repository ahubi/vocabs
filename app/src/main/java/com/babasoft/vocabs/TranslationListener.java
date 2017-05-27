package com.babasoft.vocabs;

interface TranslationListener {
    void onComplete(String result);
    void onError(int code, String error);
}
