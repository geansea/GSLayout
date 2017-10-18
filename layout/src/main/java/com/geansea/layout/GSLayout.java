package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.TextPaint;

import java.util.List;

public abstract class GSLayout {
    public CharSequence getText() {
        return text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getUsedWidth() {
        return usedWidth;
    }

    public float getUsedHeight() {
        return usedHeight;
    }

    public RectF getUsedRect() {
        if (vertical) {
            return new RectF(width - usedWidth, 0, width, usedHeight);
        } else {
            return new RectF(0, 0, usedWidth, usedHeight);
        }
    }

    public int getLineCount() {
        if (lines != null) {
            return lines.size();
        } else {
            return 0;
        }
    }

    public GSLayoutLine getLine(int index) {
        if (lines != null && 0 <= index && index < lines.size()) {
            return lines.get(index);
        } else {
            return null;
        }
    }

    public void draw(Canvas canvas) {
        if (lines == null || lines.size() == 0) {
            return;
        }
        for (GSLayoutLine line : lines) {
            line.draw(canvas);
        }
    }

    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        ALIGN_JUSTIFY,
    }

    GSLayout() {
    }

    protected CharSequence text;
    protected int start;
    protected int end;
    protected TextPaint paint;
    protected int width;
    protected int height;
    protected float usedWidth;
    protected float usedHeight;
    protected float indent;
    protected float punctuationCompressRate;
    protected Alignment alignment = Alignment.ALIGN_NORMAL;
    protected float lineSpacing;
    protected float paragraphSpacing;
    protected boolean vertical;
    protected List<GSLayoutLine> lines;
}
