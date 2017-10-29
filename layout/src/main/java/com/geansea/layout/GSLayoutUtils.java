package com.geansea.layout;

import android.graphics.PointF;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;

import java.util.LinkedList;

final class GSLayoutUtils {
    final private GSCharacterUtils characterUtils;

    GSLayoutUtils(GSCharacterUtils characterUtils) {
        this.characterUtils = characterUtils;
    }

    int breakText(CharSequence text, TextPaint paint, int start, int end, float size) {
        int count = 0;
        if (text instanceof String) {
            count = paint.breakText(text, start, end, true, size, null);
        } else if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            float measured = 0;
            int spanStart = start;
            while (spanStart < end) {
                int spanEnd = spanned.nextSpanTransition(spanStart, end, MetricAffectingSpan.class);
                float[] spanMeasured = new float[1];
                TextPaint spanPaint = paint;
                MetricAffectingSpan[] spans = spanned.getSpans(spanStart, spanEnd, MetricAffectingSpan.class);
                if (spans != null && spans.length > 0) {
                    spanPaint = new TextPaint(paint);
                    for (MetricAffectingSpan span : spans) {
                        span.updateMeasureState(spanPaint);
                    }
                }
                int spanCount = spanPaint.breakText(spanned, spanStart, spanEnd, true, size - measured, spanMeasured);
                count += spanCount;
                if (spanStart + spanCount < spanEnd) {
                    break;
                }
                measured += spanMeasured[0];
                spanStart = spanEnd;
            }
        }
        for (int pos = start; pos < Math.min(start + count + 1, end); ++pos) {
            if (characterUtils.isNewline(text.charAt(pos))) {
                count = pos - start + 1;
                break;
            }
        }
        return count;
    }

    LinkedList<GSLayoutGlyph> getHorizontalGlyphs(String text, TextPaint paint, int start, int count, float x) {
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        float ascent = -paint.ascent();
        float descent = paint.descent();
        float widths[] = new float[count];
        paint.getTextWidths(text, start, start + count, widths);
        for (int i = 0; i < count; ++i) {
            float glyphWidth = widths[i];
            if (glyphWidth == 0 && glyphs.size() > 0) {
                GSLayoutGlyph glyph = glyphs.getLast();
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                GSLayoutGlyph glyph = new GSLayoutGlyph();
                glyph.start = start + i;
                glyph.end = glyph.start + 1;
                glyph.text = text.substring(glyph.start, glyph.end);
                glyph.paint = paint;
                glyph.x = x;
                glyph.y = 0;
                glyph.ascent = ascent;
                glyph.descent = descent;
                glyph.size = glyphWidth;
                glyphs.add(glyph);
            }
            x += glyphWidth;
        }
        return glyphs;
    }

    LinkedList<GSLayoutGlyph> getVerticalGlyphs(String text, TextPaint paint, int start, int count, float y) {
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        text = characterUtils.replaceTextForVertical(text);
        float fontSize = paint.getTextSize();
        //float ascent = -paint.ascent();
        //float descent = paint.descent();
        float widths[] = new float[count];
        paint.getTextWidths(text, start, start + count, widths);
        for (int i = 0; i < count; ++i) {
            float glyphSize = widths[i];
            if (glyphSize == 0 && glyphs.size() > 0) {
                GSLayoutGlyph glyph = glyphs.getLast();
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                GSLayoutGlyph glyph = new GSLayoutGlyph();
                glyph.start = start + i;
                glyph.end = glyph.start + 1;
                glyph.text = text.substring(glyph.start, glyph.end);
                glyph.paint = paint;
                if (characterUtils.shouldRotateForVertical(glyph.code()) || glyphSize < fontSize) {
                    float glyphAscent = fontSize * 0.88f;
                    float glyphDescent = fontSize * 0.12f;
                    glyph.x = (glyphDescent - glyphAscent) / 2;
                    glyph.y = y;
                    glyph.ascent = glyphAscent;
                    glyph.descent = glyphDescent;
                    glyph.size = glyphSize;
                    glyph.vertical = true;
                    glyph.rotateForVertical = true;
                } else {
                    float glyphAscent = glyphSize * 0.88f;
                    float glyphDescent = glyphSize * 0.12f;
                    if (characterUtils.isVerticalPunctuation(glyph.code())) {
                        glyphAscent = glyphSize;
                        glyphDescent = 0;
                    }
                    glyph.x = -glyphSize / 2;
                    glyph.y = y + glyphAscent;
                    glyph.ascent = glyphAscent;
                    glyph.descent = glyphDescent;
                    glyph.size = glyphSize;
                    glyph.vertical = true;
                }
                glyphs.add(glyph);
            }
            y += glyphSize;
        }
        return glyphs;
    }

    LinkedList<GSLayoutGlyph> getHorizontalGlyphs(Spanned text, TextPaint paint, int start, int count, float x) {
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        int spanStart = start;
        while (spanStart < start + count) {
            int spanEnd = text.nextSpanTransition(spanStart, start + count, CharacterStyle.class);
            TextPaint spanPaint = paint;
            CharacterStyle[] spans = text.getSpans(spanStart, spanEnd, CharacterStyle.class);
            if (spans != null && spans.length > 0) {
                spanPaint = new TextPaint(paint);
                for (CharacterStyle span : spans) {
                    span.updateDrawState(spanPaint);
                    if (span instanceof MetricAffectingSpan) {
                        ((MetricAffectingSpan) span).updateMeasureState(spanPaint);
                    }
                }
            }
            LinkedList<GSLayoutGlyph> spanGlyphs = getHorizontalGlyphs(text.toString(), spanPaint, spanStart, spanEnd - spanStart, x);
            glyphs.addAll(spanGlyphs);
            spanStart = spanEnd;
            x = glyphs.getLast().getEndSize();
        }
        return glyphs;
    }

    LinkedList<GSLayoutGlyph> getVerticalGlyphs(Spanned text, TextPaint paint, int start, int count, float y) {
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        int spanStart = start;
        while (spanStart < start + count) {
            int spanEnd = text.nextSpanTransition(spanStart, start + count, CharacterStyle.class);
            TextPaint spanPaint = paint;
            CharacterStyle[] spans = text.getSpans(spanStart, spanEnd, CharacterStyle.class);
            if (spans != null && spans.length > 0) {
                spanPaint = new TextPaint(paint);
                for (CharacterStyle span : spans) {
                    span.updateDrawState(spanPaint);
                    if (span instanceof MetricAffectingSpan) {
                        ((MetricAffectingSpan) span).updateMeasureState(spanPaint);
                    }
                }
            }
            LinkedList<GSLayoutGlyph> spanGlyphs = getVerticalGlyphs(text.toString(), spanPaint, spanStart, spanEnd - spanStart, y);
            glyphs.addAll(spanGlyphs);
            spanStart = spanEnd;
            y = glyphs.getLast().getEndSize();
        }
        return glyphs;
    }

    void compressGlyphs(LinkedList<GSLayoutGlyph> glyphs, GSLayout.Builder builder) {
        float move = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            // Add gap
            if (characterUtils.shouldAddGap(glyph0, glyph1)) {
                move += builder.getFontSize() / 6;
            }
            // Punctuation compress
            if (characterUtils.shouldCompressStart(glyph1)) {
                if (glyph0 == null && characterUtils.canCompress(glyph1)) {
                    glyph1.compressStart = glyph1.size * builder.punctuationCompressRate;
                    move -= glyph1.compressStart;
                }
                if (characterUtils.shouldCompressEnd(glyph0)) {
                    if (characterUtils.canCompress(glyph1)) {
                        glyph1.compressStart = glyph1.size * builder.punctuationCompressRate / 2;
                        move -= glyph1.compressStart;
                    }
                    if (characterUtils.canCompress(glyph0)) {
                        glyph0.compressEnd = glyph0.size * builder.punctuationCompressRate / 2;
                        move -= glyph0.compressEnd;
                    }
                }
            }
            if (characterUtils.shouldCompressEnd(glyph1)) {
                if (characterUtils.shouldCompressEnd(glyph0)) {
                    if (characterUtils.canCompress(glyph0)) {
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
            if (characterUtils.isNewline(glyph1)) {
                glyph1.compressEnd = glyph1.size;
                move -= glyph1.size;
            }
            glyph0 = glyph1;
        }
    }

    int breakGlyphs(LinkedList<GSLayoutGlyph> glyphs, GSLayout.Builder builder, float size) {
        int breakPos = 0;
        int pos = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            if (characterUtils.canBreak(glyph0, glyph1)) {
                breakPos = pos;
            }
            float currentSize = glyph1.getUsedEndSize();
            if (currentSize > size) {
                if (characterUtils.shouldCompressEnd(glyph1) && characterUtils.canCompress(glyph1)) {
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

    void adjustEndGlyphs(LinkedList<GSLayoutGlyph> glyphs, GSLayout.Builder builder) {
        // Compress last none CRLF glyph if possible
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        GSLayoutGlyph crlfGlyph = null;
        if (characterUtils.isNewline(lastGlyph)) {
            crlfGlyph = lastGlyph;
            glyphs.removeLast();
            lastGlyph = glyphs.peekLast();
        }
        if (characterUtils.shouldCompressEnd(lastGlyph) && characterUtils.canCompress(lastGlyph)) {
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

    PointF adjustGlyphs(LinkedList<GSLayoutGlyph> glyphs, GSLayout.Builder builder, int textLength, float size) {
        PointF origin = new PointF();
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        boolean lastLine = characterUtils.isNewline(lastGlyph) || lastGlyph.end == textLength;
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
                        if (characterUtils.canStretch(prevGlyph, thisGlyph)) {
                            ++stretchCount;
                        }
                    }
                    float stretchSize = adjustSize / stretchCount;
                    float move = 0;
                    for (int i = 1; i < glyphs.size(); ++i) {
                        GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                        GSLayoutGlyph thisGlyph = glyphs.get(i);
                        if (characterUtils.canStretch(prevGlyph, thisGlyph)) {
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
}
