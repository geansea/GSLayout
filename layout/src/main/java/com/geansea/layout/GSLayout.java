package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.LinkedList;

public class GSLayout {
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
        final GSCharacterUtils charUtils;

        public static Builder obtain(TextPaint paint) {
            return new Builder(paint);
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

        public GSLayout build(CharSequence text, int start, int end) {
            start = Math.max(start, 0);
            end = Math.min(end, text.length());
            if (start >= end) {
                return null;
            }
            GSLayout layout = new GSLayout(this, text, start, end);
            if (vertical) {
                layout.doVerticalLayout();
            } else {
                layout.doHorizontalLayout();
            }
            return layout;
        }

        float getFontSize() {
            return paint.getTextSize();
        }

        private Builder(TextPaint paint) {
            this.paint = paint;
            alignment = Alignment.ALIGN_NORMAL;
            charUtils = new GSCharacterUtils();
        }
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final Builder builder;
    private final GSCharacterUtils charUtils;
    private final GSLayoutUtils layoutUtils;
    private final CharSequence text;
    private final int start;
    private int end;
    private float usedWidth;
    private float usedHeight;
    private LinkedList<GSLayoutLine> lines;

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

    GSLayout(Builder builder, CharSequence text, int start, int end) {
        this.builder = builder;
        charUtils = builder.charUtils;
        layoutUtils = new GSLayoutUtils(charUtils);
        this.text = text;
        this.start = start;
        this.end = end;
    }

    void setEnd(int end) {
        this.end = end;
    }

    Builder getBuilder() {
        return builder;
    }

    private void doHorizontalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getBuilder().getFontSize();
        int lineLocation = getStart();
        float maxWidth = 0;
        float lineTop = 0;
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            PointF lineOrigin = line.getOrigin();
            RectF lineRect = line.getUsedRect();
            lineOrigin.y = lineTop - lineRect.top;
            lineTop = lineOrigin.y + lineRect.bottom;
            if (lineTop > getHeight()) {
                break;
            }
            lines.add(line);
            lineLocation = line.getEnd();
            maxWidth = Math.max(maxWidth, lineRect.width());
            lineTop += fontSize * getBuilder().lineSpacing;
            if (charUtils.isNewline(line.getLastGlyph())) {
                lineTop += fontSize * getBuilder().paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            RectF lastRect = last.getUsedRect();
            setEnd(last.getEnd());
            setUsedWidth(maxWidth);
            setUsedHeight(lastRect.bottom);
        }
        this.lines = lines;
    }

    private void doVerticalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getBuilder().getFontSize();
        int lineLocation = getStart();
        float maxHeight = 0;
        float lineRight = getWidth();
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            PointF lineOrigin = line.getOrigin();
            RectF lineRect = line.getUsedRect();
            lineOrigin.x = lineRight - lineRect.right;
            lineRight = lineOrigin.x + lineRect.left;
            if (lineRight < 0) {
                break;
            }
            lines.add(line);
            lineLocation = line.getEnd();
            maxHeight = Math.max(maxHeight, lineRect.height());
            lineRight -= fontSize * getBuilder().lineSpacing;
            if (charUtils.isNewline(line.getLastGlyph())) {
                lineRight -= fontSize * getBuilder().paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            RectF lastRect = last.getUsedRect();
            setEnd(last.getEnd());
            setUsedWidth(getWidth() - lastRect.left);
            setUsedHeight(maxHeight);
        }
        this.lines = lines;
    }

    private GSLayoutLine layoutLine(int start) {
        String text = getText().toString();
        TextPaint paint = getBuilder().paint;
        float fontSize = getBuilder().getFontSize();
        float indent = 0;
        if (0 == start || charUtils.isNewline(text.charAt(start - 1))) {
            indent = fontSize * getBuilder().indent;
        }
        float size = getBuilder().vertical ? getHeight() : getWidth();
        int count = layoutUtils.breakText(text, paint, start, getEnd(), (size - indent) * SIZE_EXTEND_TIMES);
        LinkedList<GSLayoutGlyph> glyphs;
        if (builder.vertical) {
            glyphs = layoutUtils.getVerticalGlyphs(text, paint, start, count, indent);
        } else {
            glyphs = layoutUtils.getHorizontalGlyphs(text, paint, start, count, indent);
        }
        compressGlyphs(glyphs);
        int breakPos = breakGlyphs(glyphs, size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, text.length(), size);
        if (builder.vertical) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }

    void compressGlyphs(LinkedList<GSLayoutGlyph> glyphs) {
        float move = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            // Add gap
            if (builder.charUtils.shouldAddGap(glyph0, glyph1)) {
                move += builder.getFontSize() / 6;
            }
            // Punctuation compress
            if (builder.charUtils.shouldCompressStart(glyph1)) {
                if (glyph0 == null && builder.charUtils.canCompress(glyph1)) {
                    glyph1.compressStart = glyph1.size * builder.punctuationCompressRate;
                    move -= glyph1.compressStart;
                }
                if (builder.charUtils.shouldCompressEnd(glyph0)) {
                    if (builder.charUtils.canCompress(glyph1)) {
                        glyph1.compressStart = glyph1.size * builder.punctuationCompressRate / 2;
                        move -= glyph1.compressStart;
                    }
                    if (builder.charUtils.canCompress(glyph0)) {
                        glyph0.compressEnd = glyph0.size * builder.punctuationCompressRate / 2;
                        move -= glyph0.compressEnd;
                    }
                }
            }
            if (builder.charUtils.shouldCompressEnd(glyph1)) {
                if (builder.charUtils.shouldCompressEnd(glyph0)) {
                    if (builder.charUtils.canCompress(glyph0)) {
                        glyph0.compressEnd = glyph0.size * builder.punctuationCompressRate / 2;
                        move -= glyph0.compressEnd;
                    }
                }
            }
            // Move
            if (builder.vertical) {
                glyph1.y += move;
            } else {
                glyph1.x += move;
            }
            // Fix CRLF width
            if (builder.charUtils.isNewline(glyph1)) {
                glyph1.compressEnd = glyph1.size;
                move -= glyph1.size;
            }
            glyph0 = glyph1;
        }
    }

    int breakGlyphs(LinkedList<GSLayoutGlyph> glyphs, float size) {
        int breakPos = 0;
        int pos = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            if (builder.charUtils.canBreak(glyph0, glyph1)) {
                breakPos = pos;
            }
            float currentSize = glyph1.getUsedEndSize();
            if (currentSize > size) {
                if (builder.charUtils.shouldCompressEnd(glyph1) && builder.charUtils.canCompress(glyph1)) {
                    float compressEnd = glyph1.size * builder.punctuationCompressRate;
                    currentSize = glyph1.getEndSize() - compressEnd;
                }
            }
            if (currentSize > size) {
                break;
            }
            pos++;
            glyph0 = glyph1;
        }
        // If all glyphs can be in line
        if (pos == glyphs.size()) {
            breakPos = pos;
        }
        // If no valid break position
        if (0 == breakPos) {
            breakPos = pos;
        }
        // Add next space if possible, for latin layout
        if (breakPos < glyphs.size()) {
            if (glyphs.get(breakPos).code() == ' ') {
                ++breakPos;
            }
        }
        return breakPos;
    }

    void adjustEndGlyphs(LinkedList<GSLayoutGlyph> glyphs) {
        // Compress last none CRLF glyph if possible
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        GSLayoutGlyph crlfGlyph = null;
        if (builder.charUtils.isNewline(lastGlyph)) {
            crlfGlyph = lastGlyph;
            glyphs.removeLast();
            lastGlyph = glyphs.peekLast();
        }
        if (builder.charUtils.shouldCompressEnd(lastGlyph) && builder.charUtils.canCompress(lastGlyph)) {
            lastGlyph.compressEnd = lastGlyph.size * builder.punctuationCompressRate;
        }
        if (lastGlyph.code() == ' ') {
            lastGlyph.compressEnd = lastGlyph.size;
        }
        if (crlfGlyph != null) {
            if (builder.vertical) {
                crlfGlyph.y = lastGlyph.getUsedEndSize();
            } else {
                crlfGlyph.x = lastGlyph.getUsedEndSize();
            }
            glyphs.addLast(crlfGlyph);
        }
    }

    PointF adjustGlyphs(LinkedList<GSLayoutGlyph> glyphs, int textLength, float size) {
        PointF origin = new PointF();
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        boolean lastLine = builder.charUtils.isNewline(lastGlyph) || lastGlyph.end == textLength;
        float adjustSize = size - lastGlyph.getUsedEndSize();
        if (adjustSize == 0) {
            return origin;
        }
        switch (builder.alignment) {
            case ALIGN_NORMAL:
                break;
            case ALIGN_OPPOSITE:
                if (builder.vertical) {
                    origin.y += adjustSize;
                } else {
                    origin.x += adjustSize;
                }
                break;
            case ALIGN_CENTER:
                if (builder.vertical) {
                    origin.y += adjustSize / 2;
                } else {
                    origin.x += adjustSize / 2;
                }
                break;
            case ALIGN_JUSTIFY:
                if (!lastLine) {
                    int stretchCount = 0;
                    for (int i = 1; i < glyphs.size(); ++i) {
                        GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                        GSLayoutGlyph thisGlyph = glyphs.get(i);
                        if (builder.charUtils.canStretch(prevGlyph, thisGlyph)) {
                            ++stretchCount;
                        }
                    }
                    float stretchSize = adjustSize / stretchCount;
                    float move = 0;
                    for (int i = 1; i < glyphs.size(); ++i) {
                        GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                        GSLayoutGlyph thisGlyph = glyphs.get(i);
                        if (builder.charUtils.canStretch(prevGlyph, thisGlyph)) {
                            move += stretchSize;
                        }
                        if (builder.vertical) {
                            thisGlyph.y += move;
                        } else {
                            thisGlyph.x += move;
                        }
                    }
                    break;
                }
            default:
                break;
        }
        return origin;
    }

    void setUsedWidth(float usedWidth) {
        this.usedWidth = usedWidth;
    }

    void setUsedHeight(float usedHeight) {
        this.usedHeight = usedHeight;
    }
}
