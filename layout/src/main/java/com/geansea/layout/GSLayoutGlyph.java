package com.geansea.layout;

import android.graphics.RectF;
import android.text.TextPaint;

public class GSLayoutGlyph {
    int start;
    int end;
    String text;
    TextPaint paint;
    float x;
    float y;
    float ascent;
    float descent;
    float width;
    float compressLeft;
    float compressRight;
    boolean vertical;
    boolean rotateForVertical;

    GSLayoutGlyph() {
    }

    char code() {
        // Use UTF-16 code to handle attributes
        return text.charAt(0);
    }

    boolean isFullWidth() {
        return (width > paint.getTextSize() * 0.9);
    }

    RectF getRect() {
        if (rotateForVertical) {
            return new RectF(x - descent, y, x + ascent, y + width);
        } else {
            return new RectF(x, y - ascent, x + width, y + descent);
        }
    }

    public RectF getUsedRect() {
        if (rotateForVertical) {
            return new RectF(x - descent, y + compressLeft, x + ascent, y + width - compressRight);
        } else if (vertical) {
            return new RectF(x, y - ascent + compressLeft, x + width, y + descent - compressRight);
        } else {
            return new RectF(x + compressLeft, y - ascent, x + width - compressRight, y + descent);
        }
    }
}
