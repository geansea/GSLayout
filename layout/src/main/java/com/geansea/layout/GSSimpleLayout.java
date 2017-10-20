package com.geansea.layout;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GSSimpleLayout extends GSLayout {
    public static final class Builder {
        private TextPaint paint;
        private float indent;
        private float punctuationCompressRate;
        private Alignment alignment;
        private float lineSpacing;
        private float paragraphSpacing;
        private boolean vertical;

        public static Builder obtain(@NonNull TextPaint paint) {
            return new Builder(paint);
        }

        private Builder(@NonNull TextPaint paint) {
            this.paint = paint;
            alignment = Alignment.ALIGN_NORMAL;
            punctuationCompressRate = 1;
        }

        public Builder setFontSize(float fontSize) {
            paint.setTextSize(fontSize);
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

        public GSSimpleLayout build(@NonNull String text, int start, int end, int width, int height) {
            GSSimpleLayout layout = new GSSimpleLayout(
                    text,
                    start,
                    end,
                    paint,
                    width,
                    height,
                    indent,
                    punctuationCompressRate,
                    alignment,
                    lineSpacing,
                    paragraphSpacing,
                    vertical);
            layout.doLayout();
            return layout;
        }
    }

    private GSLayoutUtils utils;

    private GSSimpleLayout(@NonNull String text,
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
        super(
                text,
                start,
                end,
                paint,
                width,
                height,
                indent,
                punctuationCompressRate,
                alignment,
                lineSpacing,
                paragraphSpacing,
                vertical
        );
        utils = new GSLayoutUtils();
    }

    private void doLayout() {
        if (getVertical()) {
            doVerticalLayout();
        } else {
            doHorizontalLayout();
        }
    }

    private void doHorizontalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getPaint().getTextSize();
        int lineLocation = getStart();
        float maxWidth = 0;
        float lineTop = 0;
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            line.origin.y = lineTop + line.ascent;
            lineTop = line.origin.y + line.descent;
            if (lineTop > getHeight()) {
                break;
            }
            lines.add(line);
            lineLocation = line.end;
            maxWidth = Math.max(maxWidth, line.size);
            lineTop += fontSize * getLineSpacing();
            if (utils.isNewline(line.glyphs.get(line.glyphs.size() - 1).utf16Code())) {
                lineTop += fontSize * getParagraphSpacing();
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.end);
            setUsedWidth(maxWidth);
            setUsedHeight(last.getUsedRect().bottom);
        }
        setLines(lines);
    }

    private void doVerticalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getPaint().getTextSize();
        int lineLocation = getStart();
        float maxHeight = 0;
        float lineRight = getWidth();
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            line.origin.x = lineRight - line.ascent;
            lineRight = line.origin.x - line.descent;
            if (lineRight < 0) {
                break;
            }
            lines.add(line);
            lineLocation = line.end;
            maxHeight = Math.max(maxHeight, line.size);
            lineRight -= fontSize * getLineSpacing();
            if (utils.isNewline(line.glyphs.get(line.glyphs.size() - 1).utf16Code())) {
                lineRight -= fontSize * getParagraphSpacing();
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.end);
            setUsedWidth(getWidth() - last.getUsedRect().left);
            setUsedHeight(maxHeight);
        }
        setLines(lines);
    }

    private GSLayoutLine layoutLine(int start) {
        CharSequence text = getText();
        float fontSize = getPaint().getTextSize();
        float lineIndent = 0;
        if (0 == start || utils.isNewline(text.charAt(start - 1))) {
            lineIndent = fontSize * getIndent();
        }
        float layoutSize = getVertical() ? getHeight() : getWidth();
        float trySize = layoutSize * 1.3f;
        ArrayList<GSLayoutGlyph> tryGlyphs = utils.glyphsForSimpleLayout(text.toString(), getPaint(), start, text.length(), trySize, getVertical());
        compressGlyphs(tryGlyphs, lineIndent);
        int breakPos = breakPosForGlyphs(tryGlyphs, layoutSize);
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>(tryGlyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, layoutSize);
        if (getVertical()) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }

    private void compressGlyphs(ArrayList<GSLayoutGlyph> glyphs, float indent) {
        float fontSize = getPaint().getTextSize();
        float move = indent;
        GSLayoutGlyph prevGlyph = null;
        for (GSLayoutGlyph thisGlyph : glyphs) {
            char code = thisGlyph.utf16Code();
            char prevCode = (prevGlyph != null) ? prevGlyph.utf16Code() : 0;
            // Add gap
            if (utils.shouldAddGap(prevCode, code)) {
                move += fontSize / 6;
            }
            // Punctuation compress
            if (utils.canCompressLeft(code)) {
                if (0 == prevCode) {
                    if (thisGlyph.isFullWidth()) {
                        thisGlyph.compressLeft = thisGlyph.width * getPunctuationCompressRate();
                        move -= thisGlyph.compressLeft;
                    }
                }
                if (utils.canCompressRight(prevCode)) {
                    if (thisGlyph.isFullWidth()) {
                        thisGlyph.compressLeft = thisGlyph.width * getPunctuationCompressRate() / 2;
                        move -= thisGlyph.compressLeft;
                    }
                    if (prevGlyph != null && prevGlyph.isFullWidth()) {
                        prevGlyph.compressRight = prevGlyph.width * getPunctuationCompressRate() / 2;
                        move -= prevGlyph.compressRight;
                    }
                }
            }
            if (utils.canCompressRight(code)) {
                if (utils.canCompressRight(prevCode)) {
                    if (prevGlyph != null && prevGlyph.isFullWidth()) {
                        prevGlyph.compressRight = prevGlyph.width * getPunctuationCompressRate() / 2;
                        move -= prevGlyph.compressRight;
                    }
                }
            }
            // Move
            if (getVertical()) {
                thisGlyph.y += move;
            } else {
                thisGlyph.x += move;
            }
            // Fix CRLF width
            if (utils.isNewline(code)) {
                move -= thisGlyph.width;
                thisGlyph.compressRight = thisGlyph.width;
            }
            prevGlyph = thisGlyph;
        }
    }

    private int breakPosForGlyphs(List<GSLayoutGlyph> glyphs, float size) {
        int breakPos = 0;
        int forceBreakPos = 0;
        for (int i = 1; i < glyphs.size(); ++i) {
            GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
            GSLayoutGlyph thisGlyph = glyphs.get(i);
            if (utils.canBreak(prevGlyph.utf16Code(), thisGlyph.utf16Code())) {
                breakPos = i;
            }
            float currentSize = getVertical() ? thisGlyph.getUsedRect().bottom : thisGlyph.getUsedRect().right;
            if (currentSize > size) {
                if (utils.canCompressRight(thisGlyph.utf16Code()) && thisGlyph.isFullWidth()) {
                        float compressRight = thisGlyph.width * getPunctuationCompressRate();
                        currentSize = (getVertical() ? thisGlyph.getRect().bottom : thisGlyph.getRect().right) - compressRight;
                }
            }
            if (currentSize > size) {
                forceBreakPos = i;
                break;
            }
        }
        // If all glyphs can be in line
        if (0 == forceBreakPos) {
            breakPos = glyphs.size();
        }
        // If no valid break position
        if (0 == breakPos) {
            breakPos = forceBreakPos;
        }
        // Add next space if possible, for latin layout
        if (breakPos < glyphs.size()) {
            if (' ' == glyphs.get(breakPos).utf16Code()) {
                ++breakPos;
            }
        }
        return breakPos;
    }

    private void adjustEndGlyphs(List<GSLayoutGlyph> glyphs) {
        // Compress last none CRLF glyph if possible
        int count = glyphs.size();
        GSLayoutGlyph lastGlyph = glyphs.get(count - 1);
        GSLayoutGlyph crlfGlyph = null;
        if (utils.isNewline(lastGlyph.utf16Code())) {
            crlfGlyph = lastGlyph;
            if (count > 1) {
                lastGlyph = glyphs.get(count - 2);
            }
        }
        if (utils.canCompressRight(lastGlyph.utf16Code()) && lastGlyph.isFullWidth()) {
            lastGlyph.compressRight = lastGlyph.width * getPunctuationCompressRate();
        }
        if (' ' == lastGlyph.utf16Code()) {
            lastGlyph.compressRight = lastGlyph.width;
        }
        if (crlfGlyph != null) {
            if (getVertical()) {
                crlfGlyph.y = lastGlyph.getUsedRect().bottom;
            } else {
                crlfGlyph.x = lastGlyph.getUsedRect().right;
            }
        }
    }

    private PointF adjustGlyphs(List<GSLayoutGlyph> glyphs, float width) {
        PointF origin = new PointF();
        GSLayoutGlyph lastGlyph = glyphs.get(glyphs.size() - 1);
        boolean reachEnd = utils.isNewline(lastGlyph.utf16Code());
        if (lastGlyph.end == getText().length()) {
            reachEnd = true;
        }
        float lineSize = getVertical() ? lastGlyph.getUsedRect().bottom : lastGlyph.getUsedRect().right;
        float adjustSize = width - lineSize;
        if (adjustSize > 0) {
            switch (getAlignment()) {
                case ALIGN_NORMAL:
                    break;
                case ALIGN_OPPOSITE:
                    if (getVertical()) {
                        origin.y += adjustSize;
                    } else {
                        origin.x += adjustSize;
                    }
                    break;
                case ALIGN_CENTER:
                    if (getVertical()) {
                        origin.y += adjustSize / 2;
                    } else {
                        origin.x += adjustSize / 2;
                    }
                    break;
                case ALIGN_JUSTIFY:
                    if (!reachEnd) {
                        int stretchCount = 0;
                        for (int i = 1; i < glyphs.size(); ++i) {
                            GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                            GSLayoutGlyph thisGlyph = glyphs.get(i);
                            if (utils.canStretch(prevGlyph.utf16Code(), thisGlyph.utf16Code())) {
                                ++stretchCount;
                            }
                        }
                        float stretchSize = adjustSize / stretchCount;
                        float move = 0;
                        for (int i = 1; i < glyphs.size(); ++i) {
                            GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                            GSLayoutGlyph thisGlyph = glyphs.get(i);
                            if (utils.canStretch(prevGlyph.utf16Code(), thisGlyph.utf16Code())) {
                                move += stretchSize;
                            }
                            if (getVertical()) {
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
        }
        return origin;
    }
}
