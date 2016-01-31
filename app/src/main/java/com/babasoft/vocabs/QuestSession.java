package com.babasoft.vocabs;

public class QuestSession{
        public int wordIndex;
        public long correctAnswers;
        public long wrongAnswers;
        public String wordlistName;
        public long wordsToGo;
        public long wordsDone;
        public long correctInSession;
        public LAST_DIRECTION lastDirection;
        public enum LAST_DIRECTION{
            UNKNOWN,
            FORWARD,
            BACKWARD
        }
}
