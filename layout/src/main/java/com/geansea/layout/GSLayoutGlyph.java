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
    float size;
    float compressStart;
    float compressEnd;
    boolean vertical;
    boolean rotateForVertical;

    public RectF getUsedRect() {
        RectF rect;
        if (rotateForVertical) {
            rect = new RectF(-descent, compressStart, ascent, size - compressEnd);
        } else if (vertical) {
            rect = new RectF(0, -ascent + compressStart, size, descent - compressEnd);
        } else {
            rect = new RectF(compressStart, -ascent, size - compressEnd, descent);
        }
        rect.offset(getDrawX(), getDrawY());
        return rect;
    }

    GSLayoutGlyph() {
    }

    char code() {
        // Use UTF-16 code to handle attributes
        return text.charAt(0);
    }

    float getDrawX() {
        return vertical ? (x - paint.baselineShift) : x;
    }

    float getDrawY() {
        return vertical ? y : (y + paint.baselineShift);
    }

    boolean isFullSize() {
        return (size >= paint.getTextSize() * 0.9);
    }

    boolean isItalic() {
        return (!vertical || rotateForVertical) && paint.getTypeface().isItalic();
    }

    float getEndPos() {
        return vertical ? getRect().bottom : getRect().right;
    }

    float getUsedEndPos() {
        return vertical ? getUsedRect().bottom : getUsedRect().right;
    }

    private RectF getRect() {
        RectF rect;
        if (rotateForVertical) {
            rect = new RectF(-descent, 0, ascent, size);
        } else {
            rect = new RectF(0, -ascent, size, descent);
        }
        rect.offset(getDrawX(), getDrawY());
        return rect;
    }
}
