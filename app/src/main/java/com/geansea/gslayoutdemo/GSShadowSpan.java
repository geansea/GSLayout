package com.geansea.gslayoutdemo;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

class GSShadowSpan extends CharacterStyle {
    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setShadowLayer(2,2,2, Color.RED);
    }
}
