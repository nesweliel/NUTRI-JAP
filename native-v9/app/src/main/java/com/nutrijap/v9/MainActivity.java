package com.nutrijap.v9;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private NutriGameView gameView;
    private EditText chatInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.rgb(2,5,11));
        getWindow().setNavigationBarColor(Color.rgb(2,5,11));
        hideSystemUi();

        tts = new TextToSpeech(this, this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(2,5,11));

        gameView = new NutriGameView(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout chatBar = new LinearLayout(this);
        chatBar.setOrientation(LinearLayout.HORIZONTAL);
        chatBar.setGravity(Gravity.CENTER_VERTICAL);
        chatBar.setPadding(dp(7), dp(6), dp(7), dp(6));
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(Color.argb(238, 5, 14, 27));
        barBg.setStroke(dp(1), Color.rgb(28, 66, 96));
        barBg.setCornerRadius(dp(18));
        chatBar.setBackground(barBg);

        chatInput = new EditText(this);
        chatInput.setSingleLine(true);
        chatInput.setTextColor(Color.WHITE);
        chatInput.setHintTextColor(Color.rgb(103, 132, 153));
        chatInput.setHint("דבר עם REI…");
        chatInput.setTextSize(13);
        chatInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        chatInput.setPadding(dp(12), 0, dp(12), 0);
        chatInput.setBackgroundColor(Color.TRANSPARENT);
        chatInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
        chatInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        TextView send = new TextView(this);
        send.setText("שלח");
        send.setTextColor(Color.WHITE);
        send.setTextSize(12);
        send.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        send.setGravity(Gravity.CENTER);
        GradientDrawable sendBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(14, 108, 240), Color.rgb(34, 185, 255)});
        sendBg.setCornerRadius(dp(13));
        send.setBackground(sendBg);

        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(dp(62), dp(44));
        sendLp.setMarginStart(dp(6));
        chatBar.addView(chatInput, inputLp);
        chatBar.addView(send, sendLp);

        FrameLayout.LayoutParams chatLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, dp(60), Gravity.BOTTOM);
        chatLp.leftMargin = dp(10);
        chatLp.rightMargin = dp(10);
        chatLp.bottomMargin = dp(9);
        root.addView(chatBar, chatLp);
        gameView.setBottomOverlayInset(dp(82));

        View.OnClickListener submit = v -> submitChat();
        send.setOnClickListener(submit);
        chatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitChat();
                return true;
            }
            return false;
        });

        setContentView(root);
    }

    private void submitChat() {
        String text = chatInput.getText().toString().trim();
        if (text.isEmpty()) return;
        chatInput.setText("");
        gameView.userMessage(text);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("he", "IL"));
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (!ttsReady) {
                tts.setLanguage(Locale.getDefault());
                ttsReady = true;
            }
            tts.setSpeechRate(1.03f);
            tts.setPitch(1.06f);
        }
    }

    public void speak(String text) {
        if (ttsReady && text != null && !text.isEmpty()) {
            tts.stop();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nutri_" + System.nanoTime());
            gameView.onSpeechStarted(text);
        }
    }

    public void updateChatHint(String characterName) {
        chatInput.setHint("דבר עם " + characterName + "…");
    }

    private void hideSystemUi() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                getWindow().getInsetsController().setSystemBarsBehavior(
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
