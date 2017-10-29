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
        if (rotateForVertical) {
            return new RectF(x - descent, y + compressStart, x + ascent, y + size - compressEnd);
        } else if (vertical) {
            return new RectF(x, y - ascent + compressStart, x + size, y + descent - compressEnd);
        } else {
            return new RectF(x + compressStart, y - ascent, x + size - compressEnd, y + descent);
        }
    }

    GSLayoutGlyph() {
    }

    char code() {
        // Use UTF-16 code to handle attributes
        return text.charAt(0);
    }

    boolean isFullSize() {
        return (size >= paint.getTextSize());
    }

    boolean isItalic() {
        return paint.getTypeface().isItalic();
    }

    float getEndSize() {
        return vertical ? getRect().bottom : getRect().right;
    }

    float getUsedEndSize() {
        return vertical ? getUsedRect().bottom : getUsedRect().right;
    }

    private RectF getRect() {
        if (rotateForVertical) {
            return new RectF(x - descent, y, x + ascent, y + size);
        } else {
            return new RectF(x, y - ascent, x + size, y + descent);
        }
    }
}
