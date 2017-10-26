package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

public abstract class GSLayout {
    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        ALIGN_JUSTIFY,
    }

    public static final class Parameters {
        TextPaint paint;
        int width;
        int height;
        float indent;
        float punctuationCompressRate;
        Alignment alignment;
        float lineSpacing;
        float paragraphSpacing;
        boolean vertical;

        public static Parameters obtain(@NonNull TextPaint paint) {
            return new Parameters(paint);
        }

        private Parameters(TextPaint paint) {
            this.paint = paint;
            alignment = Alignment.ALIGN_NORMAL;
            punctuationCompressRate = 1;
        }

        public Parameters setFontSize(float fontSize) {
            paint.setTextSize(fontSize);
            return this;
        }

        public Parameters setWidth(int width) {
            this.width = width;
            return this;
        }

        public Parameters setHeight(int height) {
            this.height = height;
            return this;
        }

        public Parameters setIndent(float indent) {
            this.indent = indent;
            return this;
        }

        public Parameters setPunctuationCompressRate(float punctuationCompressRate) {
            this.punctuationCompressRate = punctuationCompressRate;
            return this;
        }

        public Parameters setAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Parameters setLineSpacing(float lineSpacing) {
            this.lineSpacing = lineSpacing;
            return this;
        }

        public Parameters setParagraphSpacing(float paragraphSpacing) {
            this.paragraphSpacing = paragraphSpacing;
            return this;
        }

        public Parameters setVertical(boolean vertical) {
            this.vertical = vertical;
            return this;
        }

        float getFontSize() {
            return paint.getTextSize();
        }
    }

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
        return parameters.width;
    }

    public int getHeight() {
        return parameters.height;
    }

    public float getUsedWidth() {
        return usedWidth;
    }

    public float getUsedHeight() {
        return usedHeight;
    }

    public RectF getUsedRect() {
        if (parameters.vertical) {
            return new RectF(parameters.width - usedWidth, 0, parameters.width, usedHeight);
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

    private CharSequence text;
    private int start;
    private int end;
    private Parameters parameters;
    private float usedWidth;
    private float usedHeight;
    private List<GSLayoutLine> lines;

    GSLayout(CharSequence text, int start, int end, Parameters parameters) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.parameters = parameters;
    }

    void setEnd(int end) {
        this.end = end;
    }

    Parameters getParameters() {
        return parameters;
    }

    void setUsedWidth(float usedWidth) {
        this.usedWidth = usedWidth;
    }

    void setUsedHeight(float usedHeight) {
        this.usedHeight = usedHeight;
    }

    void setLines(List<GSLayoutLine> lines) {
        this.lines = lines;
    }
}
