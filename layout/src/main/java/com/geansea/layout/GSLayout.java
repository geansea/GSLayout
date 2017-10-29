package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Typeface;
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

    public static final class Builder {
        TextPaint paint;
        int width;
        int height;
        float indent;
        float punctuationCompressRate;
        Alignment alignment;
        float lineSpacing;
        float paragraphSpacing;
        boolean vertical;

        public static Builder obtain(@NonNull TextPaint paint) {
            return new Builder(paint);
        }

        private Builder(TextPaint paint) {
            this.paint = paint;
            alignment = Alignment.ALIGN_NORMAL;
        }

        public Builder setTypeface(Typeface typeface) {
            paint.setTypeface(typeface);
            return this;
        }

        public Builder setFontSize(float fontSize) {
            paint.setTextSize(fontSize);
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setIndent(float indent) {
            this.indent = indent;
            return this;
        }

        public Builder setPunctuationCompressRate(float punctuationCompressRate) {
            this.punctuationCompressRate = punctuationCompressRate;
            return this;
        }

        public Builder setAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder setLineSpacing(float lineSpacing) {
            this.lineSpacing = lineSpacing;
            return this;
        }

        public Builder setParagraphSpacing(float paragraphSpacing) {
            this.paragraphSpacing = paragraphSpacing;
            return this;
        }

        public Builder setVertical(boolean vertical) {
            this.vertical = vertical;
            return this;
        }

        float getFontSize() {
            return paint.getTextSize();
        }
    }

    private CharSequence text;
    private int start;
    private int end;
    private Builder builder;
    private float usedWidth;
    private float usedHeight;
    private List<GSLayoutLine> lines;

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
        return builder.width;
    }

    public int getHeight() {
        return builder.height;
    }

    public float getUsedWidth() {
        return usedWidth;
    }

    public float getUsedHeight() {
        return usedHeight;
    }

    public RectF getUsedRect() {
        if (builder.vertical) {
            return new RectF(builder.width - usedWidth, 0, builder.width, usedHeight);
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

    GSLayout(CharSequence text, int start, int end, Builder builder) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.builder = builder;
    }

    void setEnd(int end) {
        this.end = end;
    }

    Builder getBuilder() {
        return builder;
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
