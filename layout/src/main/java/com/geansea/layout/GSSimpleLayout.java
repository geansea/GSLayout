package com.geansea.layout;

import android.graphics.PointF;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GSSimpleLayout extends GSLayout {
    public static final class Builder {
        private String text;
        private TextPaint paint;
        private float indent;
        private float punctuationCompressRate;
        private Alignment alignment;
        private float lineSpacing;
        private float paragraphSpacing;

        public static Builder obtain(String text, TextPaint paint) {
            return new Builder(text, paint);
        }

        private Builder(String text, TextPaint paint) {
            this.text = text;
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

        public GSSimpleLayout build(int start, int end, int width, int height, boolean vertical) {
            GSSimpleLayout layout = new GSSimpleLayout(
                    text,
                    start,
                    end,
                    paint,
                    width,
                    height);
            layout.indent = indent;
            layout.punctuationCompressRate = punctuationCompressRate;
            layout.alignment = alignment;
            layout.lineSpacing = lineSpacing;
            layout.paragraphSpacing = paragraphSpacing;
            layout.vertical = vertical;
            layout.doLayout();
            return layout;
        }
    }

    private GSLayoutUtils utils;

    private GSSimpleLayout(String text,
                           int start,
                           int end,
                           TextPaint paint,
                           int width,
                           int height) {
        super(text, start, end, paint, width, height);
        utils = new GSLayoutUtils();
    }

    private void doLayout() {
        if (vertical) {
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
            lineTop += fontSize * lineSpacing;
            if (utils.isNewline(line.glyphs.get(line.glyphs.size() - 1).utf16Code())) {
                lineTop += fontSize * paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.end);
            setUsedWidth(maxWidth);
            setUsedHeight(last.getUsedRect().bottom);
        }
        this.lines = new ArrayList<>(lines);
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
            lineRight -= fontSize * lineSpacing;
            if (utils.isNewline(line.glyphs.get(line.glyphs.size() - 1).utf16Code())) {
                lineRight -= fontSize * paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.end);
            setUsedWidth(getWidth() - last.getUsedRect().left);
            setUsedHeight(maxHeight);
        }
        this.lines = new ArrayList<>(lines);
    }

    private GSLayoutLine layoutLine(int start) {
        CharSequence text = getText();
        float fontSize = getPaint().getTextSize();
        float lineIndent = 0;
        if (0 == start || utils.isNewline(text.charAt(start - 1))) {
            lineIndent = fontSize * indent;
        }
        float layoutSize = vertical ? getHeight() - lineIndent : getWidth() - lineIndent;
        float trySize = layoutSize * 1.3f;
        ArrayList<GSLayoutGlyph> tryGlyphs = utils.glyphsForSimpleLayout(text.toString(), getPaint(), start, text.length(), trySize, vertical);
        compressGlyphs(tryGlyphs);
        int breakPos = breakPosForGlyphs(tryGlyphs, layoutSize);
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>(tryGlyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, layoutSize, lineIndent);
        if (vertical) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }

    private void compressGlyphs(ArrayList<GSLayoutGlyph> glyphs) {
        float fontSize = getPaint().getTextSize();
        float move = 0;
        GSLayoutGlyph prevGlyph = null;
        for (GSLayoutGlyph thisGlyph : glyphs) {
            char code = thisGlyph.utf16Code();
            char prevCode = (prevGlyph != null) ? prevGlyph.utf16Code() : 0;
            // Add gap
            if (utils.shouldAddGap(prevCode, code)) {
                move += fontSize / 6;
            }
            // Punctuation compress
            if (utils.canGlyphCompressLeft(thisGlyph)) {
                if (0 == prevCode) {
                    thisGlyph.compressLeft = thisGlyph.width * punctuationCompressRate;
                    move -= thisGlyph.compressLeft;
                }
                if (prevGlyph != null && utils.canGlyphCompressRight(prevGlyph)) {
                    thisGlyph.compressLeft = thisGlyph.width * punctuationCompressRate / 2;
                    move -= thisGlyph.compressLeft;
                    prevGlyph.compressRight = prevGlyph.width * punctuationCompressRate / 2;
                    move -= prevGlyph.compressRight;
                }
            }
            if (utils.canGlyphCompressRight(thisGlyph)) {
                if (prevGlyph != null && utils.canGlyphCompressRight(prevGlyph)) {
                    prevGlyph.compressRight = prevGlyph.width * punctuationCompressRate / 2;
                    move -= prevGlyph.compressRight;
                }
            }
            // Move
            if (vertical) {
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
            float currentSize = vertical ? thisGlyph.getUsedRect().bottom : thisGlyph.getUsedRect().right;
            if (currentSize > size) {
                if (utils.canGlyphCompressRight(thisGlyph)) {
                    float compressRight = thisGlyph.width * punctuationCompressRate;
                    currentSize = (vertical ? thisGlyph.getRect().bottom : thisGlyph.getRect().right) - compressRight;
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
        if (utils.canGlyphCompressRight(lastGlyph)) {
            lastGlyph.compressRight = lastGlyph.width * punctuationCompressRate;
        }
        if (' ' == lastGlyph.utf16Code()) {
            lastGlyph.compressRight = lastGlyph.width;
        }
        if (crlfGlyph != null) {
            crlfGlyph.x = lastGlyph.getUsedRect().right;
        }
    }

    private PointF adjustGlyphs(List<GSLayoutGlyph> glyphs, float width, float indent) {
        PointF origin = new PointF();
        if (vertical) {
            origin.y = indent;
        } else {
            origin.x = indent;
        }
        GSLayoutGlyph lastGlyph = glyphs.get(glyphs.size() - 1);
        boolean reachEnd = utils.isNewline(lastGlyph.utf16Code());
        if (lastGlyph.end == getText().length()) {
            reachEnd = true;
        }
        float lineSize = vertical ? lastGlyph.getUsedRect().bottom : lastGlyph.getUsedRect().right;
        float adjustSize = width - lineSize;
        if (adjustSize > 0) {
            switch (alignment) {
                case ALIGN_NORMAL:
                    break;
                case ALIGN_OPPOSITE:
                    if (vertical) {
                        origin.y += adjustSize;
                    } else {
                        origin.x += adjustSize;
                    }
                    break;
                case ALIGN_CENTER:
                    if (vertical) {
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
                            if (vertical) {
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
