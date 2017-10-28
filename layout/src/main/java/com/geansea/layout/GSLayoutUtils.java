package com.geansea.layout;

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

    LinkedList<GSLayoutGlyph> getHoriGlyphs(String text, TextPaint paint, int start, int count, float x) {
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
                glyph.width = glyphWidth;
                glyphs.add(glyph);
            }
            x += glyphWidth;
        }
        return glyphs;
    }

    LinkedList<GSLayoutGlyph> getVertGlyphs(String text, TextPaint paint, int start, int count, float y) {
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        text = characterUtils.replaceTextForVertical(text);
        float fontSize = paint.getTextSize();
        float ascent = -paint.ascent();
        float descent = paint.descent();
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
                    glyph.x = (descent - ascent) / 2;
                    glyph.y = y;
                    glyph.ascent = ascent;
                    glyph.descent = descent;
                    glyph.width = glyphSize;
                    glyph.vertical = true;
                    glyph.rotateForVertical = true;
                } else {
                    float extended = ascent + descent - glyphSize;
                    float ascentForVertical = ascent - extended / 2;
                    float descentForVertical = descent - extended / 2;
                    glyph.x = -glyphSize / 2;
                    glyph.y = y + ascentForVertical;
                    glyph.ascent = ascentForVertical;
                    glyph.descent = descentForVertical;
                    glyph.width = glyphSize;
                    glyph.vertical = true;
                    glyphSize = ascentForVertical + descentForVertical;
                }
                glyphs.add(glyph);
            }
            y += glyphSize;
        }
        return glyphs;
    }

    LinkedList<GSLayoutGlyph> getHoriGlyphs(Spanned text, TextPaint paint, int start, int count, float x) {
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
            LinkedList<GSLayoutGlyph> spanGlyphs = getHoriGlyphs(text.toString(), spanPaint, spanStart, spanEnd - spanStart, x);
            glyphs.addAll(spanGlyphs);
            spanStart = spanEnd;
            x = glyphs.getLast().getRect().right;
        }
        return glyphs;
    }

    LinkedList<GSLayoutGlyph> getVertGlyphs(Spanned text, TextPaint paint, int start, int count, float y) {
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
            LinkedList<GSLayoutGlyph> spanGlyphs = getVertGlyphs(text.toString(), spanPaint, spanStart, spanEnd - spanStart, y);
            glyphs.addAll(spanGlyphs);
            spanStart = spanEnd;
            y = glyphs.getLast().getRect().bottom;
        }
        return glyphs;
    }

    void compressGlyphs(LinkedList<GSLayoutGlyph> glyphs, GSLayout.Parameters parameters) {
        float move = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            // Add gap
            if (characterUtils.shouldAddGap(glyph0, glyph1)) {
                move += parameters.getFontSize() / 6;
            }
            // Punctuation compress
            if (characterUtils.shouldCompressStart(glyph1)) {
                if (glyph0 == null && characterUtils.canCompress(glyph1)) {
                    glyph1.compressLeft = glyph1.width * parameters.punctuationCompressRate;
                    move -= glyph1.compressLeft;
                }
                if (characterUtils.shouldCompressEnd(glyph0)) {
                    if (characterUtils.canCompress(glyph1)) {
                        glyph1.compressLeft = glyph1.width * parameters.punctuationCompressRate / 2;
                        move -= glyph1.compressLeft;
                    }
                    if (characterUtils.canCompress(glyph0)) {
                        glyph0.compressRight = glyph0.width * parameters.punctuationCompressRate / 2;
                        move -= glyph0.compressRight;
                    }
                }
            }
            if (characterUtils.shouldCompressEnd(glyph1)) {
                if (characterUtils.shouldCompressEnd(glyph0)) {
                    if (characterUtils.canCompress(glyph0)) {
                        glyph0.compressRight = glyph0.width * parameters.punctuationCompressRate / 2;
                        move -= glyph0.compressRight;
                    }
                }
            }
            // Move
            if (parameters.vertical) {
                glyph1.y += move;
            } else {
                glyph1.x += move;
            }
            // Fix CRLF width
            if (characterUtils.isNewline(glyph1)) {
                glyph1.compressRight = glyph1.width;
                move -= glyph1.width;
            }
            glyph0 = glyph1;
        }
    }
}
