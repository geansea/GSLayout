package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.ArrayList;
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

    public ArrayList<GSLayoutLine> getLines() {
        if (lines == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(lines);
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

    GSLayout(@NonNull CharSequence text,
             int start,
             int end,
             @NonNull TextPaint paint,
             int width,
             int height,
             float indent,
             float punctuationCompressRate,
             Alignment alignment,
             float lineSpacing,
             float paragraphSpacing,
             boolean vertical) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.paint = paint;
        this.width = width;
        this.height = height;
        this.indent = indent;
        this.punctuationCompressRate = punctuationCompressRate;
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
        this.paragraphSpacing = paragraphSpacing;
        this.vertical = vertical;
    }

    void setEnd(int end) {
        this.end = end;
    }

    TextPaint getPaint() {
        return paint;
    }

    void setUsedWidth(float usedWidth) {
        this.usedWidth = usedWidth;
    }

    void setUsedHeight(float usedHeight) {
        this.usedHeight = usedHeight;
    }

    float getIndent() {
        return indent;
    }

    float getPunctuationCompressRate() {
        return punctuationCompressRate;
    }

    Alignment getAlignment() {
        return alignment;
    }

    float getLineSpacing() {
        return lineSpacing;
    }

    float getParagraphSpacing() {
        return paragraphSpacing;
    }

    boolean getVertical() {
        return vertical;
    }

    void setLines(List<GSLayoutLine> lines) {
        this.lines = lines;
    }

    private CharSequence text;
    private int start;
    private int end;
    private TextPaint paint;
    private int width;
    private int height;
    private float usedWidth;
    private float usedHeight;
    private float indent;
    private float punctuationCompressRate;
    private Alignment alignment;
    private float lineSpacing;
    private float paragraphSpacing;
    private boolean vertical;
    private List<GSLayoutLine> lines;
}
