package com.geansea.layout;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class GSLayoutUtils {
    final private GSCharacterUtils characterUtils;

    GSLayoutUtils(GSCharacterUtils characterUtils) {
        this.characterUtils = characterUtils;
    }

    ArrayList<GSLayoutGlyph> glyphsForSimpleLayout(
            String text,
            TextPaint paint,
            int start,
            int end,
            float size,
            boolean vertical) {
        int length = paint.breakText(text, start, end, true, size, null);
        for (int pos = start; pos < Math.min(start + length + 1, end); ++pos) {
            if (characterUtils.isNewline(text.charAt(pos))) {
                length = pos - start + 1;
                break;
            }
        }
        if (vertical) {
            return glyphsForSimpleVerticalLayout(text, paint, start, length);
        } else {
            return glyphsForSimpleHorizontalLayout(text, paint, start, length);
        }
    }

    private ArrayList<GSLayoutGlyph> glyphsForSimpleHorizontalLayout(
            String text,
            TextPaint paint,
            int start,
            int length) {
        float x = 0;
        float ascent = -paint.ascent();
        float descent = paint.descent();
        float widths[] = new float[length];
        paint.getTextWidths(text, start, start + length, widths);
        ArrayList<GSLayoutGlyph> glyphs = new ArrayList<>(length);
        GSLayoutGlyph glyph = null;
        for (int i = 0; i < length; ++i) {
            float glyphWidth = widths[i];
            if (glyphWidth == 0 && glyph != null) {
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                glyph = new GSLayoutGlyph();
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

    private ArrayList<GSLayoutGlyph> glyphsForSimpleVerticalLayout(
            String text,
            TextPaint paint,
            int start,
            int length) {
        text = characterUtils.replaceTextForVertical(text);
        float y = 0;
        float fontSize = paint.getTextSize();
        float ascent = -paint.ascent();
        float descent = paint.descent();
        float widths[] = new float[length];
        paint.getTextWidths(text, start, start + length, widths);
        ArrayList<GSLayoutGlyph> glyphs = new ArrayList<>(length);
        GSLayoutGlyph glyph = null;
        for (int i = 0; i < length; ++i) {
            float glyphSize = widths[i];
            if (glyphSize == 0 && glyph != null) {
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                glyph = new GSLayoutGlyph();
                glyph.start = start + i;
                glyph.end = glyph.start + 1;
                glyph.text = text.substring(glyph.start, glyph.end);
                glyph.paint = paint;
                if (characterUtils.shouldRotateForVertical(glyph.utf16Code()) || glyphSize < fontSize * 0.9) {
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

    private LinkedList<GSLayoutGlyph> glyphsForSpannedHorizontalLayout(
            Spanned text,
            TextPaint paint,
            int start,
            int length) {
        float x = 0;
        LinkedList<GSLayoutGlyph> glyphs = new LinkedList<>();
        int spanStart = start;
        while (spanStart < start + length) {
            int spanEnd = text.nextSpanTransition(spanStart, start + length, Object.class);
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
            List<GSLayoutGlyph> spanGlyphs = glyphsForSimpleHorizontalLayout(text.toString(), spanPaint, spanStart, spanEnd - spanStart);
            glyphs.addAll(spanGlyphs);
            spanStart = spanEnd;
        }
        return glyphs;
    }

    private ArrayList<GSLayoutGlyph> glyphsForSpannedVerticalLayout(
            Spanned text,
            TextPaint paint,
            int start,
            int length) {
        return null;
    }
}
