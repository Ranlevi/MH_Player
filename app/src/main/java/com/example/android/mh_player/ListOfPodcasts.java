package com.example.android.mh_player;

import java.util.ArrayList;
import java.util.Arrays;

class ListOfPodcasts {
    //Creates the list of podcasts displayed in the main screen.
    private ArrayList<Podcast> arrayOfPodcasts;

    //Constructor, Create the List of Podcasts
    ListOfPodcasts(){

        Podcast makingHistory = new Podcast("עושים היסטוריה עם רן לוי",
                                            "פודקאסט על מדע, טכנולוגיה והיסטוריה",
                                            "making_history_logo_60px",
                                            "http://www.ranlevi.com/feed/podcast/" );

        Podcast osimAsakim = new Podcast("עושים עסקים עם גיל רוזנפלד",
                                        "מאחורי הקלעים עם המנהלים המובילים במשק",
                                        "osim_asakim_logo_60px",
                                        "http://www.ranlevi.com/feed/bizpod/" );

        Podcast osimPolitica = new Podcast("עושים פוליטיקה עם דפנה ליאל",
                                            "מאחורי הקלעים של הפוליטיקה הישראלית",
                                            "osim_politica_logo_60px",
                                            "http://www.ranlevi.com/feed/osimpolitica/" );

        Podcast osimTiyul = new Podcast("עושים טיול עם עינב לנדאו",
                                        "טיולים מרתקים בעולם ובישראל",
                                        "osim_tiyul_logo_60px",
                                        "http://www.ranlevi.com/feed/osim_tiuol/" );

        Podcast osimTanach = new Podcast("עושים תנ\"ך עם ד״ר ליאורה רביד ויותם שטיינמן",
                                        "מסע היסטורי אל תקופת המקרא",
                                        "osim_tanach_logo_60px",
                                        "http://www.ranlevi.com/feed/osimtanach" );

        Podcast osimShivuk = new Podcast("עושים שיווק עם קובי גור",
                                        "שיווק באינטרנט ליזמים, אנשי עסקים ויוצרי תוכן",
                                        "osim_shivuk_logo_60px",
                                        "http://www.ranlevi.com/feed/osim_shivuk/" );

        Podcast osimRefua = new Podcast("עושים רפואה עם דר' יובל בלוך ועידן כהן",
                                        "סיפורים מרתקים וראיונות ייחודיים סביב המכונה המופלאה ביותר ביקום: אנחנו",
                                        "osim_refua_logo_60px",
                                        "http://www.ranlevi.com/feed/osim_refua/" );

        Podcast familySoundsHeb = new Podcast("סיפור משפחתי",
                                            "תוכניות רדיו דוקומנטריות על משפחות וגיבורים אמיתיים",
                                            "family_sounds_logo_60px",
                                            "http://www.familysounds.co.il/feed/podcast/" );

        arrayOfPodcasts = new ArrayList<Podcast>(
                    Arrays.asList(makingHistory, osimAsakim, osimPolitica, osimTiyul,
                            osimTanach, osimShivuk, osimRefua, familySoundsHeb)
        );
    }

    ArrayList<Podcast> getList(){
        return arrayOfPodcasts;
    }


}
