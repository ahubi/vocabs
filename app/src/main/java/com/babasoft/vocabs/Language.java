package com.babasoft.vocabs;

public enum Language {
    AUTO_DETECT("auto"),
    AFRIKAANS("af"),
    ALBANIAN("sq"),
    AMHARIC("am"),
    ARABIC("ar"),
    ARMENIAN("hy"),
    AZEERBAIJANI("az"),
    BASQUE("eu"),
    BELARUSIAN("be"),
    BENGALI("bn"),
    BOSNIAN("bs"),
    BULGARIAN("bg"),
    CATALAN("ca"),
    CEBUANO("ceb"),
    CHICHEWA("ny"),
    CHINESESIMPLIFIED("zh-CN"),
    CHINESETRADITIONAL("zh-TW"),
    CORSICAN("co"),
    CROATIAN("hr"),
    CZECH("cs"),
    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    ESPERANTO("eo"),
    ESTONIAN("et"),
    FILIPINO("tl"),
    FINNISH("fi"),
    FRENCH("fr"),
    FRISIAN("fy"),
    GALICIAN("gl"),
    GEORGIAN("ka"),
    GERMAN("de"),
    GREEK("el"),
    GUJARATI("gu"),
    HAITIANCREOLE("ht"),
    HAUSA("ha"),
    HAWAIIAN("haw"),
    HEBREW("iw"),
    HINDI("hi"),
    HMONG("hmn"),
    HUNGARIAN("hu"),
    ICELANDIC("is"),
    IGBO("ig"),
    INDONESIAN("id"),
    IRISH("ga"),
    ITALIAN("it"),
    JAPANESE("ja"),
    JAVANESE("jw"),
    KANNADA("kn"),
    KAZAKH("kk"),
    KHMER("km"),
    KOREAN("ko"),
    KURDISH("ku"),
    KYRGYZ("ky"),
    LAO("lo"),
    LATIN("la"),
    LATVIAN("lv"),
    LITHUANIAN("lt"),
    LUXEMBOURGISH("lb"),
    MACEDONIAN("mk"),
    MALAGASY("mg"),
    MALAY("ms"),
    MALAYALAM("ml"),
    MALTESE("mt"),
    MAORI("mi"),
    MARATHI("mr"),
    MONGOLIAN("mn"),
    BURMESE("my"),
    NEPALI("ne"),
    NORWEGIAN("no"),
    PASHTO("ps"),
    PERSIAN("fa"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    PUNJABI("ma"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SAMOAN("sm"),
    SCOTSGAELIC("gd"),
    SERBIAN("sr"),
    SESOTHO("st"),
    SHONA("sn"),
    SINDHI("sd"),
    SINHALA("si"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SOMALI("so"),
    SPANISH("es"),
    SUNDANESE("su"),
    SWAHILI("sw"),
    SWEDISH("sv"),
    TAJIK("tg"),
    TAMIL("ta"),
    TELUGU("te"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("uk"),
    URDU("ur"),
    UZBEK("uz"),
    VIETNAMESE("vi"),
    WELSH("cy"),
    XHOSA("xh"),
    YIDDISH("yi"),
    YORUBA("yo"),
    ZULU("zu");

    private final String language;

    Language(String pLanguage) {
        this.language = pLanguage;
    }

    public static Language fromString(String pLanguage) {
        Language[] arr = values();

        for (Language l : arr) {
            if (l.toString().equals(pLanguage)) {
                return l;
            }
        }
        return null;
    }
    public String toString() {
        return this.language;
    }

}
