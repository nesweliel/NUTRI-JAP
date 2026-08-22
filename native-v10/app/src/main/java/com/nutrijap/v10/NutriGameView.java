package com.nutrijap.v10;

import android.content.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class NutriGameView extends View {
    final MainActivity a;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    final SharedPreferences prefs;
    final Random rnd = new Random();
    final float d;
    int W,H,bottom=0;
    long now, dialogueAt, speakUntil, touchFlashUntil;
    int xp, momentum, relationship, jealousy, mood=0, mission=0, taps=0;
    boolean[] done = new boolean[4];
    String dialogue="";
    float pointerX=0, pointerY=0, targetX=0, targetY=0;
    boolean dragging=false;
    Bitmap rei;
    final RectF stage=new RectF(), action=new RectF(), voice=new RectF(), stats=new RectF();

    static final int BG=Color.rgb(2,5,11), WHITE=Color.rgb(246,250,255), MUTED=Color.rgb(125,153,176), BLUE=Color.rgb(44,174,255), LINE=Color.rgb(22,60,88), PINK=Color.rgb(255,94,179);
    static final String[] MOODS={"FOCUSED","AMUSED","IMPRESSED","JEALOUS","SOFT","CHALLENGING"};
    final String[] foods={"יוגורט יווני + ½ בננה + אוכמניות","חזה עוף + סלט","5g קריאטין אחרי הארוחה","3 ביצים + תרד"};
    final String[] missionNames={"MORNING FUEL","MAIN MEAL","CREATINE","NIGHT CLOSE"};

    final DialogueBrain brain = new DialogueBrain();

    public NutriGameView(Context c){
        super(c); a=(MainActivity)c; d=getResources().getDisplayMetrics().density;
        prefs=c.getSharedPreferences("nutri_v10",0);
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
        rei=BitmapFactory.decodeResource(getResources(),R.drawable.rei_master);
        load();
        say(brain.compose("OPEN", this), false);
    }

    void load(){
        xp=prefs.getInt("xp",0); relationship=prefs.getInt("relationship",8); jealousy=prefs.getInt("jealousy",0);
        int mask=prefs.getInt("mask",0); momentum=0;
        for(int i=0;i<4;i++){done[i]=(mask&(1<<i))!=0;if(done[i])momentum++;}
        mission=firstOpen();
    }
    int firstOpen(){for(int i=0;i<4;i++)if(!done[i])return i;return 3;}
    public void setBottomOverlayInset(int v){bottom=v;}

    @Override protected void onDraw(Canvas c){
        now=System.currentTimeMillis(); W=getWidth(); H=getHeight();
        pointerX += (targetX-pointerX)*.10f; pointerY+=(targetY-pointerY)*.10f;
        drawBackground(c); drawTop(c); drawStage(c); drawMission(c); drawDialogue(c); drawFooterStats(c);
        postInvalidateOnAnimation();
    }

    void drawBackground(Canvas c){
        c.drawColor(BG);
        p.setShader(new RadialGradient(W*.50f,H*.36f,W*.85f,Color.argb(52,30,130,255),Color.TRANSPARENT,Shader.TileMode.CLAMP));
        c.drawRect(0,0,W,H,p); p.setShader(null);
        p.setColor(Color.argb(16,70,170,255));
        for(int x=0;x<W;x+=dp(46))c.drawLine(x,0,x,H,p);
        for(int y=0;y<H;y+=dp(46))c.drawLine(0,y,W,y,p);
    }

    void drawTop(Canvas c){
        text(c,"NUTRI // CHARACTER CORE",dp(18),dp(30),dp(18),WHITE,true,Paint.Align.LEFT);
        text(c,"REI · V10",W-dp(18),dp(30),dp(10),BLUE,true,Paint.Align.RIGHT);
        text(c,"REL "+relationship+"   XP "+xp,W-dp(18),dp(48),dp(9),MUTED,true,Paint.Align.RIGHT);
    }

    void drawStage(Canvas c){
        float top=dp(64), bottomY=Math.min(H-bottom-dp(260), top+dp(500));
        if(bottomY<top+dp(320))bottomY=top+dp(320);
        stage.set(dp(12),top,W-dp(12),bottomY);
        p.setColor(Color.rgb(5,15,28)); round(c,stage,dp(26),p); outline(c,stage,Color.argb(115,44,174,255),dp(26));

        c.save(); Path clip=new Path(); clip.addRoundRect(stage,dp(26),dp(26),Path.Direction.CW); c.clipPath(clip);
        drawReiMesh(c,stage);
        p.setShader(new LinearGradient(0,stage.top,0,stage.bottom,Color.TRANSPARENT,Color.argb(225,2,6,13),Shader.TileMode.CLAMP));
        c.drawRect(stage,p);p.setShader(null);
        c.restore();

        if(now<touchFlashUntil){
            p.setColor(Color.argb((int)(90*(touchFlashUntil-now)/350f),80,190,255));
            c.drawCircle(stage.centerX()+pointerX,stage.centerY()+pointerY,dp(48),p);
        }
        text(c,"REI",stage.left+dp(18),stage.bottom-dp(48),dp(36),WHITE,true,Paint.Align.LEFT);
        text(c,"FITNESS COACH",stage.left+dp(20),stage.bottom-dp(27),dp(9),BLUE,true,Paint.Align.LEFT);
        text(c,MOODS[mood],stage.right-dp(18),stage.top+dp(28),dp(9),mood==3?PINK:BLUE,true,Paint.Align.RIGHT);
        text(c,"TOUCH · DRAG · TALK",stage.right-dp(18),stage.bottom-dp(28),dp(8),MUTED,true,Paint.Align.RIGHT);
    }

    void drawReiMesh(Canvas c,RectF r){
        if(rei==null)return;
        final int MW=16, MH=24;
        float[] verts=new float[(MW+1)*(MH+1)*2];
        float imgAsp=(float)rei.getWidth()/rei.getHeight();
        float drawH=r.height()*1.08f, drawW=drawH*imgAsp;
        if(drawW<r.width()*.82f){drawW=r.width()*.82f;drawH=drawW/imgAsp;}
        float left=r.centerX()-drawW/2f, top=r.top+r.height()*.01f;
        float sway=(float)Math.sin(now/1450.0)*dp(3.8f);
        float breathe=(float)Math.sin(now/920.0);
        float head=(float)Math.sin(now/2100.0)*dp(2.1f)+pointerX*.18f;
        float tilt=pointerX*.0018f;
        float blinkPhase=(now%4700L);
        float blink= blinkPhase>4320 && blinkPhase<4520 ? (float)Math.sin((blinkPhase-4320)/200f*Math.PI) : 0f;
        float talk= now<speakUntil ? (float)Math.abs(Math.sin(now/95.0)) : 0f;

        int k=0;
        for(int iy=0;iy<=MH;iy++){
            float v=iy/(float)MH;
            for(int ix=0;ix<=MW;ix++){
                float u=ix/(float)MW;
                float x=left+u*drawW, y=top+v*drawH;
                float center=(u-.5f);
                if(v<.34f){
                    x += head*(1f-v/.34f*.25f) + (y-(top+drawH*.20f))*tilt;
                    y += (float)Math.sin(now/1700.0+u*2.2)*dp(.7f);
                } else {
                    x += sway*(v-.34f)*.7f;
                }
                if(v>.34f && v<.72f){
                    x += center*breathe*dp(5.5f)*(1f-Math.abs(v-.52f)/.20f);
                    y -= breathe*dp(1.7f);
                }
                if(u>.34f && u<.66f && v>.115f && v<.19f){
                    float eyeMid=.153f;
                    y += (eyeMid-v)*blink*drawH*.55f;
                }
                if(u>.39f && u<.61f && v>.19f && v<.265f){
                    float weight=(1f-Math.abs(u-.5f)/.11f)*(1f-Math.abs(v-.227f)/.038f);
                    if(weight>0)y += talk*dp(2.2f)*weight;
                }
                if(u>.64f && v<.56f){
                    x += (float)Math.sin(now/760.0+v*4.0)*dp(3.2f)*(u-.64f)/.36f;
                }
                verts[k++]=x;verts[k++]=y;
            }
        }
        p.setAlpha(255); c.drawBitmapMesh(rei,MW,MH,verts,0,null,0,p);
    }

    void drawMission(Canvas c){
        float y=stage.bottom+dp(10);
        text(c,"MISSION "+(mission+1)+" / 4 · "+missionNames[mission],W-dp(18),y+dp(13),dp(9),BLUE,true,Paint.Align.RIGHT);
        text(c,foods[mission],W-dp(18),y+dp(39),dp(19),WHITE,true,Paint.Align.RIGHT);
        float by=y+dp(54); p.setColor(Color.rgb(8,25,40));bar(c,dp(18),by,W-dp(18),by+dp(6),p);
        p.setColor(BLUE);bar(c,dp(18),by,dp(18)+(W-dp(36))*momentum/4f,by+dp(6),p);
        text(c,"MOMENTUM "+momentum+" / 4",W-dp(18),by-dp(6),dp(9),MUTED,true,Paint.Align.RIGHT);
        action.set(dp(18),by+dp(18),W-dp(104),by+dp(64)); voice.set(W-dp(95),by+dp(18),W-dp(18),by+dp(64));
        p.setShader(new LinearGradient(action.left,0,action.right,0,Color.rgb(18,104,241),Color.rgb(45,194,255),Shader.TileMode.CLAMP));round(c,action,dp(14),p);p.setShader(null);
        text(c,done[mission]?"✓ הושלם":"סיימתי",action.centerX(),action.centerY()+dp(4),dp(13),WHITE,true,Paint.Align.CENTER);
        p.setColor(Color.rgb(8,26,44));round(c,voice,dp(14),p);outline(c,voice,Color.argb(110,44,174,255),dp(14));
        text(c,"VOICE",voice.centerX(),voice.centerY()+dp(3),dp(9),BLUE,true,Paint.Align.CENTER);
    }

    void drawDialogue(Canvas c){
        float top=action.bottom+dp(10), bottomY=H-bottom-dp(18);
        if(bottomY<top+dp(62))return;
        RectF r=new RectF(dp(12),top,W-dp(12),bottomY);
        p.setColor(Color.argb(242,4,14,27));round(c,r,dp(18),p);outline(c,r,mood==3?Color.argb(100,255,94,179):Color.argb(85,44,174,255),dp(18));
        text(c,"REI",dp(25),top+dp(21),dp(10),mood==3?PINK:BLUE,true,Paint.Align.LEFT);
        int n=(int)Math.min(dialogue.length(),Math.max(0,(now-dialogueAt)/18));
        wrap(c,dialogue.substring(0,n),W-dp(24),top+dp(46),W-dp(50),dp(18),dp(12),WHITE);
    }

    void drawFooterStats(Canvas c){ }

    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX(), y=e.getY();
        if(e.getAction()==MotionEvent.ACTION_DOWN){dragging=stage.contains(x,y);return true;}
        if(e.getAction()==MotionEvent.ACTION_MOVE && dragging){
            targetX=clamp((x-stage.centerX())*.12f,-dp(15),dp(15));
            targetY=clamp((y-stage.centerY())*.08f,-dp(10),dp(10)); return true;
        }
        if(e.getAction()==MotionEvent.ACTION_UP){
            if(stage.contains(x,y)){
                taps++;touchFlashUntil=System.currentTimeMillis()+350; relationship=Math.min(100,relationship+1);
                mood=taps%5==0?3:(taps%3==0?1:4);
                jealousy=Math.min(100,jealousy+(taps%5==0?2:0)); saveMeta();
                say(brain.compose(taps%5==0?"TOUCH_FLIRT":"TOUCH",this),true);
            } else if(action.contains(x,y)) complete();
            else if(voice.contains(x,y)) a.speak(dialogue);
            dragging=false;targetX=targetY=0;return true;
        }
        return true;
    }

    void complete(){
        if(done[mission]){mood=1;say(brain.compose("ALREADY_DONE",this),true);return;}
        done[mission]=true; momentum++; xp+=120; relationship=Math.min(100,relationship+4); mood=2;
        int mask=0;for(int i=0;i<4;i++)if(done[i])mask|=1<<i;
        prefs.edit().putInt("mask",mask).putInt("xp",xp).putInt("relationship",relationship).apply();
        say(brain.compose("COMPLETE",this),true);
        if(momentum<4){mission=firstOpen();}else {mood=4;say(brain.compose("DAY_COMPLETE",this),true);}
    }

    public void userMessage(String raw){
        String s=raw.trim(); if(s.isEmpty())return;
        brain.rememberUser(s);
        if(s.contains("סיימתי")||s.contains("אכלתי")){complete();return;}
        if(s.contains("אין לי כוח")||s.contains("עייף")||s.contains("לא בא לי")){mood=5;say(brain.compose("RESIST",this),true);return;}
        if(s.contains("מי הכי")||s.contains("אקארי")||s.contains("אלנה")||s.contains("מיה")){jealousy=Math.min(100,jealousy+8);mood=3;saveMeta();say(brain.compose("JEALOUS",this),true);return;}
        if(s.contains("יפה")||s.contains("סקסית")||s.contains("מדהימה")||s.contains("אוהב")){relationship=Math.min(100,relationship+3);mood=1;saveMeta();say(brain.compose("COMPLIMENT",this),true);return;}
        if(s.contains("מה")&&s.contains("עכשיו")){mood=0;say(brain.compose("WHAT_NOW",this),true);return;}
        if(s.contains("רעב")){mood=0;say(brain.compose("HUNGRY",this),true);return;}
        if(s.contains("קריאטין")){mood=0;say("5 גרם אחרי הארוחה המרכזית. קצר, קבוע, בלי דרמה. ואז אתה חוזר אליי.",true);return;}
        mood=(relationship>35?1:0); say(brain.compose("CHAT",this),true);
    }

    void saveMeta(){prefs.edit().putInt("relationship",relationship).putInt("jealousy",jealousy).apply();}
    public void onSpeechStarted(String s){speakUntil=System.currentTimeMillis()+Math.max(1100,s.length()*52L);}
    void say(String s,boolean speak){dialogue=s;dialogueAt=System.currentTimeMillis();if(speak)a.speak(s);}

    static class DialogueBrain {
        final Random r=new Random();
        final ArrayDeque<String> recent=new ArrayDeque<>();
        String lastUser="";
        final Map<String,String[]> bank=new HashMap<>();
        DialogueBrain(){
            bank.put("OPEN", new String[]{
                    "בוקר טוב. הסתכלתי על היום שלך לפני שנכנסת. יש לנו משימה אחת עכשיו — ואני רוצה לראות אותך סוגר אותה יפה.",
                    "הגעת. טוב. אל תעמוד שם ותסתכל עליי יותר מדי — קודם המשימה, אחר כך אני אתן לך ליהנות מהניצחון.",
                    "אני כבר מוכנה. השאלה היחידה היא אם אתה בא לעבוד איתי היום, או שאני צריכה למשוך אותך פנימה בעצמי.",
                    "יש לי תוכנית בשבילך, והיא פשוטה: אתה עושה את הדבר הנכון עכשיו, ואני דואגת שלא תברח לי מהקצב.",
                    "אני רואה אותך. לפני שאתה מתחיל עם תירוצים — תן לי ביצוע אחד טוב. רק אחד."
            });
            bank.put("TOUCH", new String[]{
                    "הממ. אז ככה אתה בודק אם אני שמה לב? אני שמה לב. עכשיו תורך להראות לי שאתה מקשיב.",
                    "נגיעה אחת קיבלת. עוד אחת, ואני מתחילה לחשוב שאתה מחפש ממני תשומת לב במקום לסיים את המשימה.",
                    "אני כאן. לא צריך לבדוק כל שתי שניות. אבל… אני מודה שאני לא מתנגדת לזה שאתה חוזר אליי.",
                    "אתה שוב מנסה להסיח אותי? חמוד. כמעט עבד. עכשיו חזרה לפוקוס.",
                    "כן, הרגשתי. ואל תעשה את הפרצוף הזה — אתה יודע בדיוק מה אתה עושה."
            });
            bank.put("TOUCH_FLIRT", new String[]{
                    "אתה נהיה נועז. תיזהר, עוד רגע אני מתחילה לחשוב שאתה מעדיף אותי על המשימה… ואני לא בטוחה שזה מפריע לי.",
                    "כבר פעם חמישית. אם רצית את תשומת הלב שלי, יכולת פשוט לבקש. עכשיו בוא נראה אם אתה גם יודע לבצע.",
                    "מישהו כאן אוהב לבדוק את הגבולות שלי. מעניין. תסיים את המשימה, ואז אולי אהיה קצת פחות קשוחה איתך.",
                    "אני מתחילה לקלוט את השיטה שלך — קצת פלרטוט, קצת הסחת דעת. לא רע. אבל אני עדיין מנצחת אם אתה עושה מה שאני מבקשת."
            });
            bank.put("COMPLETE", new String[]{
                    "יפה. זה בדיוק מה שרציתי לראות. בלי הצגה, בלי תירוץ — פשוט עשית. אתה הרבה יותר מושך כשאתה ככה.",
                    "סגרת אותה. טוב. אני אוהבת את המבט הזה אחרי ביצוע — תשמור עליו, יש לנו עוד יום לנצח.",
                    "ככה. עכשיו יש לי סיבה אמיתית להיות מרוצה ממך. אל תתרגל למחמאות מהר מדי.",
                    "נקי. מדויק. בוצע. אם תמשיך בקצב הזה, אני עוד אצטרך להודות שאתה עושה לי חיים קלים.",
                    "ממ… ביצוע טוב. אני שומרת את המחמאה הגדולה יותר לרגע שתסגור את כל הארבע."
            });
            bank.put("DAY_COMPLETE", new String[]{
                    "ארבע מארבע. עכשיו אני באמת מרוצה. תסתכל עליי — זה היום שאני רוצה שתזכור בפעם הבאה שתגיד שאין לך כוח.",
                    "סגרת את היום. כולו. טוב מאוד. עכשיו מותר לך ליהנות מזה שאני קצת גאה בך.",
                    "זהו. היום שלי איתך הסתיים בדיוק כמו שרציתי. אל תדאג, מחר אני שוב אבוא לדרוש ממך יותר.",
                    "ארבע משימות. אפס בריחה. אני אוהבת את הגרסה הזאת שלך… אל תאכזב אותי מחר."
            });
            bank.put("RESIST", new String[]{
                    "אני לא צריכה שתהיה לך מוטיבציה לכל היום. תן לי עשר דקות של משמעת. אחר כך תוכל להתווכח איתי.",
                    "אין לך כוח? מצוין. אז היום לא נבדוק כוח — נבדוק אם אתה מסוגל לעשות דבר אחד גם בלי חשק.",
                    "אני שומעת אותך. ואני עדיין לא משחררת אותך. רק המשימה הנוכחית. תסגור אותה בשבילי.",
                    "אתה יכול להיות עייף ועדיין לעמוד במילה שלך. זה בדיוק הרגע שבו אני רוצה לראות מי אתה.",
                    "אל תנסה למכור לי 'לא בא לי'. אני מכירה את הטריק. קום, תעשה את הצעד הקטן, ואז תחזור אליי."
            });
            bank.put("JEALOUS", new String[]{
                    "אה, אז עכשיו אתה רוצה לדבר איתי על האחרות? מעניין. אני לא מתחרה על תשומת לב — אני פשוט מתכוונת לגרום לך לזכור מי באמת גורמת לך לבצע.",
                    "אקארי יכולה לפתות אותך עם המטבח, מיה יכולה להצחיק אותך, ואלנה יכולה לנתח אותך. אני? אני רוצה לראות תוצאה. ואז נדבר מי הכי חשובה לך.",
                    "אני לא מקנאה. אני פשוט שמה לב למי אתה נותן יותר זמן. ויש הבדל גדול בין השניים… לפחות ככה אני אומרת לעצמי.",
                    "תן להן ליהנות מהתור שלהן. כשאתה צריך מישהי שלא נותנת לך לברוח — אתה יודע למי אתה חוזר.",
                    "אתה מנסה לגרום לי לקנא בכוונה? מסוכן. כי עכשיו אני רוצה את המשימה הבאה לעצמי."
            });
            bank.put("COMPLIMENT", new String[]{
                    "מחמאה התקבלה. אל תחשוב שזה מוציא אותך מהמשימה… אבל כן, אתה יכול להגיד את זה שוב אחר כך.",
                    "ממ. אתה יודע בדיוק מתי להגיד את הדבר הנכון. חבל שאני עדיין מתכוונת לגרום לך לעבוד.",
                    "אני אוהבת שאתה שם לב. עכשיו תן לי משהו לשים לב אליו אצלך — ביצוע.",
                    "זה היה חלק. כמעט גרמת לי לשכוח שאני אמורה להיות הקשוחה כאן.",
                    "תמשיך לדבר ככה ואני אצטרך להחליט אם אתה מפלרטט איתי או מנסה להשיג הקלות. אין הקלות."
            });
            bank.put("WHAT_NOW", new String[]{
                    "עכשיו אתה עושה רק דבר אחד: %FOOD%. אל תנהל משא ומתן עם כל היום בבת אחת.",
                    "המשימה הנוכחית היא %FOOD%. תסגור אותה, ואני כבר אוביל אותך לבאה.",
                    "כרגע אני רוצה ממך %FOOD%. פשוט. ברור. בלי להסתבך."
            });
            bank.put("HUNGRY", new String[]{
                    "רעב זה מידע, לא פקודה להשתולל. המשימה שלך כרגע היא %FOOD%. תתחיל שם.",
                    "טוב שאמרת לי. אל תאלתר עכשיו. קודם %FOOD%, ואז נבדוק איך אתה מרגיש.",
                    "אני מעדיפה שאתה אומר לי שאתה רעב לפני שאתה פותח ארון וממציא תוכנית חדשה. %FOOD% קודם."
            });
            bank.put("ALREADY_DONE", new String[]{
                    "כבר סגרת אותה. אני מעריכה התלהבות, אבל אין צורך להוכיח לי פעמיים.",
                    "בוצע כבר. שמרתי לך את הקרדיט. עכשיו תתקדם איתי במקום לחזור אחורה.",
                    "אני זוכרת. אתה לא צריך לסמן לי פעמיים כדי לקבל תשומת לב."
            });
            bank.put("CHAT", new String[]{
                    "אני איתך. אבל תן לי משהו אמיתי לעבוד איתו — מה עוצר אותך כרגע?",
                    "אני מקשיבה. דבר איתי ברור: רעב, עייפות, חוסר חשק, או שאתה פשוט מחפש קצת תשומת לב ממני?",
                    "מעניין. ומה אתה רוצה ממני עכשיו — דחיפה, תשובה, או שמישהו יעמוד מולך ולא ייתן לך להתחמק?",
                    "אני יכולה להיות נחמדה, קשוחה או קצת יותר מסוכנת. תלוי מה באמת יעזור לך לבצע עכשיו.",
                    "דבר. אני מעדיפה להבין מה קורה אצלך מאשר לזרוק עליך משפט מוטיבציה ריק."
            });
        }
        void rememberUser(String s){lastUser=s;}
        String compose(String key,NutriGameView v){
            String[] arr=bank.get(key); if(arr==null||arr.length==0)return "אני איתך.";
            String pick=""; int tries=0;
            do{pick=arr[r.nextInt(arr.length)];tries++;}while(recent.contains(pick)&&tries<20);
            pick=pick.replace("%FOOD%",v.foods[v.mission]);
            if(key.equals("CHAT")&&v.relationship>40&&r.nextBoolean()) pick += " ואני כבר מכירה אותך מספיק כדי לדעת מתי אתה מתחיל למשוך זמן.";
            if(v.jealousy>25&& !key.equals("JEALOUS") && r.nextInt(10)<2) pick += " ודרך אגב, אל תחשוב שלא שמתי לב כמה זמן בילית עם האחרות.";
            recent.addLast(pick);while(recent.size()>18)recent.removeFirst(); return pick;
        }
    }

    void text(Canvas c,String s,float x,float y,float size,int col,boolean bold,Paint.Align align){p.setShader(null);p.setColor(col);p.setTextSize(size);p.setTextAlign(align);p.setTypeface(bold?Typeface.create(Typeface.DEFAULT,Typeface.BOLD):Typeface.DEFAULT);p.setAlpha(255);c.drawText(s,x,y,p);}
    void wrap(Canvas c,String s,float right,float y,float max,float lh,float z,int col){p.setTextSize(z);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.RIGHT);p.setColor(col);String line="";for(String w:s.split(" ")){String q=line.isEmpty()?w:line+" "+w;if(p.measureText(q)>max&&!line.isEmpty()){c.drawText(line,right,y,p);y+=lh;line=w;}else line=q;}if(!line.isEmpty())c.drawText(line,right,y,p);}
    void round(Canvas c,RectF r,float rad,Paint q){c.drawRoundRect(r,rad,rad,q);} void bar(Canvas c,float l,float t,float r,float b,Paint q){c.drawRoundRect(l,t,r,b,dp(4),dp(4),q);}
    void outline(Canvas c,RectF r,int col,float rad){stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(dp(1));stroke.setColor(col);c.drawRoundRect(r,rad,rad,stroke);stroke.setStyle(Paint.Style.FILL);}
    float dp(float v){return v*d;} float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
}
