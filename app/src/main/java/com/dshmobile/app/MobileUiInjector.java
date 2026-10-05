package com.dshmobile.app;

import android.content.Context;
import android.webkit.WebView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/* JADX INFO: loaded from: classes.dex */
public final class MobileUiInjector {
    private final String css;
    private final String js;

    public MobileUiInjector(Context context) {
        this.css = readAsset(context, "mobile.css");
        this.js = readAsset(context, "inject.js");
    }

    public void inject(WebView webView) {
        webView.evaluateJavascript("(function(){if(!document.getElementById('dsh-mobile-style')){var st=document.createElement('style');st.id='dsh-mobile-style';st.textContent=" + toJsString(this.css) + ";document.head.appendChild(st);}var v=document.querySelector('meta[name=viewport]');if(v){v.setAttribute('content','width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover, interactive-widget=resizes-content');}else{var m=document.createElement('meta');m.name='viewport';m.content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover, interactive-widget=resizes-content';document.head.appendChild(m);}})();", null);
        webView.evaluateJavascript(this.js, null);
    }

    private static String readAsset(Context context, String str) {
        try {
            InputStream inputStreamOpen = context.getAssets().open(str);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = inputStreamOpen.read(bArr);
                    if (i == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, i);
                }
                String string = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
                return string;
            } catch (Throwable th) {
                if (inputStreamOpen != null) {
                    try {
                        inputStreamOpen.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (IOException unused) {
            return "";
        }
    }

    private static String toJsString(String str) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt == '\t') {
                sb.append("\\t");
            } else if (cCharAt == '\n') {
                sb.append("\\n");
            } else if (cCharAt == '\r') {
                sb.append("\\r");
            } else if (cCharAt == '\"') {
                sb.append("\\\"");
            } else if (cCharAt == '\\') {
                sb.append("\\\\");
            } else if (cCharAt < ' ') {
                sb.append(String.format("\\u%04x", Integer.valueOf(cCharAt)));
            } else {
                sb.append(cCharAt);
            }
        }
        return sb.append("\"").toString();
    }
}
