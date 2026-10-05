package com.dshmobile.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public final class Ui {
    public static final int PRIMARY = Color.parseColor("#4D6BFE");
    public static final int PRIMARY_SOFT = Color.parseColor("#EEF2FF");

    private Ui() {
    }

    public static int bg(Context context) {
        return context.getColor(R.color.ds_bg);
    }

    public static int bgSoft(Context context) {
        return context.getColor(R.color.ds_bg_soft);
    }

    public static int text(Context context) {
        return context.getColor(R.color.ds_text);
    }

    public static int textSecondary(Context context) {
        return context.getColor(R.color.ds_text_secondary);
    }

    public static int border(Context context) {
        return context.getColor(R.color.ds_border);
    }

    public static int dp(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    public static TextView title(Context context, String str) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(text(context));
        textView.setTextSize(22.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        return textView;
    }

    public static TextView body(Context context, String str) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(text(context));
        textView.setTextSize(15.0f);
        return textView;
    }

    public static TextView hint(Context context, String str) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(textSecondary(context));
        textView.setTextSize(13.0f);
        return textView;
    }

    public static TextView sectionHeader(Context context, String str) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextColor(PRIMARY);
        textView.setTextSize(13.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLetterSpacing(0.05f);
        return textView;
    }

    public static Button primaryButton(Context context, String str) {
        Button button = new Button(context);
        button.setText(str);
        button.setTextColor(-1);
        button.setTextSize(15.0f);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackgroundResource(R.drawable.btn_primary);
        button.setMinHeight(dp(context, 48.0f));
        return button;
    }

    public static Button outlineButton(Context context, String str) {
        Button button = new Button(context);
        button.setText(str);
        button.setTextColor(PRIMARY);
        button.setTextSize(15.0f);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.btn_outline);
        button.setMinHeight(dp(context, 48.0f));
        return button;
    }

    public static LinearLayout card(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundResource(R.drawable.card_bg);
        int iDp = dp(context, 16.0f);
        linearLayout.setPadding(iDp, iDp, iDp, iDp);
        return linearLayout;
    }

    public static GradientDrawable softBg(Context context) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(bgSoft(context));
        gradientDrawable.setCornerRadius(dp(context, 12.0f));
        return gradientDrawable;
    }

    public static View divider(Context context) {
        View view = new View(context);
        view.setBackgroundColor(border(context));
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(context, 0.5f)));
        return view;
    }
}
