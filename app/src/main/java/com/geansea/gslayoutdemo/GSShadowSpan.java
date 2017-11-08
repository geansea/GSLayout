package com.geansea.gslayoutdemo;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

class GSShadowSpan extends CharacterStyle {
    private final float radius;
    private final float dx;
    private final float dy;
    private final int color;

    GSShadowSpan(float radius, float dx, float dy, int color) {
        this.radius = radius;
        this.dx = dx;
        this.dy = dy;
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setShadowLayer(radius, dx, dy, color);
    }
}
