package com.geansea.layout;

import android.graphics.Color;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;

import java.util.LinkedList;

final class GSLayoutUtils {
    static int breakText(CharSequence text, TextPaint paint, int start, int end, float size) {
        int count = 0;
        if (text instanceof Spanned) {
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
                int spanCount = spanPaint.breakText(spanned.toString(), spanStart, spanEnd, true, size - measured, spanMeasured);
                count += spanCount;
                if (spanStart + spanCount < spanEnd) {
                    break;
                }
                measured += spanMeasured[0];
                spanStart = spanEnd;
            }
        } else {
            count = paint.breakText(text.toString(), start, end, true, size, null);
        }
        for (int index = start; index < Math.min(start + count + 1, end); ++index) {
            if (GSCharUtils.isNewline(text.charAt(index))) {
                count = index - start + 1;
                break;
            }
        }
        return count;
    }

    static LinkedList<GSLayoutGlyph> getGlyphs(CharSequence text, TextPaint paint, int start, int count, boolean vertical, float pos) {
        String string = text.toString();
        if (!(text instanceof Spanned)) {
            if (vertical) {
                return getVerticalGlyphs(string, paint, start, count, pos);
            } else {
                return getHorizontalGlyphs(string, paint, start, count, pos);
            }
        }
        Spanned spanned = (Spanned) text;
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        int spanStart = start;
        while (spanStart < start + count) {
            int spanEnd = spanned.nextSpanTransition(spanStart, start + count, CharacterStyle.class);
            TextPaint spanPaint = paint;
            CharacterStyle[] spans = spanned.getSpans(spanStart, spanEnd, CharacterStyle.class);
            if (spans != null && spans.length > 0) {
                spanPaint = new TextPaint();
                spanPaint.set(paint);
                for (CharacterStyle span : spans) {
                    span.updateDrawState(spanPaint);
                }
                // Handle by self
                spanPaint.bgColor = Color.TRANSPARENT;
                spanPaint.setUnderlineText(false);
                spanPaint.setStrikeThruText(false);
            }
            LinkedList<GSLayoutGlyph> spanGlyphs;
            if (vertical) {
                spanGlyphs = getVerticalGlyphs(string, spanPaint, spanStart, spanEnd - spanStart, pos);
            } else {
                spanGlyphs = getHorizontalGlyphs(string, spanPaint, spanStart, spanEnd - spanStart, pos);
            }
            glyphs.addAll(spanGlyphs);
            spanStart = spanEnd;
            pos = glyphs.getLast().getEndPos();
        }
        return glyphs;
    }

    private static LinkedList<GSLayoutGlyph> getHorizontalGlyphs(String text, TextPaint paint, int start, int count, float x) {
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

    private static LinkedList<GSLayoutGlyph> getVerticalGlyphs(String text, TextPaint paint, int start, int count, float y) {
        text = GSCharUtils.replaceTextForVertical(text);
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
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
                if (GSCharUtils.shouldRotateForVertical(glyph.code()) || glyphSize < fontSize * 0.9) {
                    float glyphAscent = fontSize * 0.98f;
                    float glyphDescent = fontSize * 0.22f;
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
                    if (GSCharUtils.isVerticalPunctuation(glyph.code())) {
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
}
