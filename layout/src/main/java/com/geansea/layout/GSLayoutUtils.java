package com.geansea.layout;

import android.text.TextPaint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class GSLayoutUtils {
    private Set<Character> compressLeftSet = null;
    private Set<Character> compressRightSet = null;
    private Set<Character> notLineBeginSet = null;
    private Set<Character> notLineEndSet = null;
    private Set<Character> rotateForVertical = null;

    GSLayoutUtils() {
    }

    ArrayList<GSLayoutGlyph> glyphsForSimpleLayout(
            String text,
            TextPaint paint,
            int start,
            int end,
            float width,
            boolean vertical) {
        int length = paint.breakText(text, start, end, true, width, null);
        for (int pos = start; pos < Math.min(start + length + 1, end); ++pos) {
            if (isNewline(text.charAt(pos))) {
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

    ArrayList<GSLayoutGlyph> glyphsForSimpleHorizontalLayout(
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

    ArrayList<GSLayoutGlyph> glyphsForSimpleVerticalLayout(
            String text,
            TextPaint paint,
            int start,
            int length) {
        float y = 0;
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
                if (shouldRotateForVertical(glyph.utf16Code())) {
                    glyph.x = (descent - ascent) / 2;
                    glyph.y = y;
                    glyph.ascent = ascent;
                    glyph.descent = descent;
                    glyph.width = glyphWidth;
                    glyph.rotateForVertical = true;
                } else {
                    float ascentForVertical = ascent * glyphWidth / (ascent + descent);
                    float descentForVertical = descent * glyphWidth / (ascent + descent);
                    glyph.x = -glyphWidth / 2;
                    glyph.y = y + ascentForVertical;
                    glyph.ascent = ascentForVertical;
                    glyph.descent = descentForVertical;
                    glyph.width = glyphWidth;
                    glyphWidth = ascentForVertical + descentForVertical;
                }
                glyphs.add(glyph);
            }
            y += glyphWidth;
        }
        return glyphs;
    }

    boolean isNewline(char code) {
        return ('\r' == code || '\n' == code);
    }

    boolean shouldAddGap(char prevCode, char code) {
        if (isAlphaDigit(prevCode) && isCjk(code)) {
            return true;
        }
        if (isCjk(prevCode) && isAlphaDigit(code)) {
            return true;
        }
        return false;
    }

    boolean canGlyphCompressLeft(GSLayoutGlyph glyph) {
        if (glyph.width < glyph.paint.getTextSize() * 0.9) {
            return false;
        }
        return canCompressLeft(glyph.utf16Code());
    }

    boolean canGlyphCompressRight(GSLayoutGlyph glyph) {
        if (glyph.width < glyph.paint.getTextSize() * 0.9) {
            return false;
        }
        return canCompressRight(glyph.utf16Code());
    }

    boolean canBreak(char prevCode, char code) {
        if (0 == prevCode) {
            return false;
        }
        // Always can break after space
        if (' ' == prevCode) {
            return true;
        }
        // false Break SPace
        if (0xA0 == prevCode) {
            return false;
        }
        // Space follow prev
        if (' ' == code || 0xA0 == code) {
            return false;
        }
        if (isAlphaDigit(prevCode)) {
            if (isAlphaDigit(code)) {
                return false;
            }
            if ('\'' == code || '\"' == code || '-' == code || '_' == code) {
                return false;
            }
        }
        if (isAlphaDigit(code)) {
            if ('\'' == prevCode || '\"' == prevCode || 0x2019 == prevCode) {
                return false;
            }
        }
        if (cannotLineBegin(code)) {
            return false;
        }
        if (cannotLineEnd(prevCode)) {
            return false;
        }
        return true;
    }

    boolean canStretch(char prevCode, char code) {
        if (!canBreak(prevCode, code)) {
            return false;
        }
        if ('/' == prevCode) {
            if (isAlphaDigit(code)) {
                return false;
            }
        }
        if ('/' == code) {
            if (isAlphaDigit(prevCode)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAlphaDigit(char code) {
        return ('a' <= code && code <= 'z') || ('A' <= code && code <= 'Z') || ('0' <= code && code <= '9');
    }

    private boolean isCjk(char code) {
        return (0x4E00 <= code && code < 0xD800) || (0xE000 <= code && code < 0xFB00);
    }

    private boolean canCompressLeft(char code) {
        if (compressLeftSet == null) {
            compressLeftSet = new HashSet<>();
            compressLeftSet.add('\u2018'); // ‘
            compressLeftSet.add('\u201C'); // “
            compressLeftSet.add('\u3008'); // 〈
            compressLeftSet.add('\u300A'); // 《
            compressLeftSet.add('\u300C'); // 「
            compressLeftSet.add('\u300E'); // 『
            compressLeftSet.add('\u3010'); // 【
            compressLeftSet.add('\u3014'); // 〔
            compressLeftSet.add('\u3016'); // 〖
            compressLeftSet.add('\uFF08'); // （
            compressLeftSet.add('\uFF3B'); // ［
            compressLeftSet.add('\uFF5B'); // ｛
        }
        return compressLeftSet.contains(code);
    }

    private boolean canCompressRight(char code) {
        if (compressRightSet == null) {
            compressRightSet = new HashSet<>();
            compressRightSet.add('\u2019'); // ’
            compressRightSet.add('\u201D'); // ”
            compressRightSet.add('\u3001'); // 、
            compressRightSet.add('\u3002'); // 。
            compressRightSet.add('\u3009'); // 〉
            compressRightSet.add('\u300B'); // 》
            compressRightSet.add('\u300D'); // 」
            compressRightSet.add('\u300F'); // 』
            compressRightSet.add('\u3011'); // 】
            compressRightSet.add('\u3015'); // 〕
            compressRightSet.add('\u3017'); // 〗
            compressRightSet.add('\uFF01'); // ！
            compressRightSet.add('\uFF09'); // ）
            compressRightSet.add('\uFF0C'); // ，
            compressRightSet.add('\uFF1A'); // ：
            compressRightSet.add('\uFF1B'); // ；
            compressRightSet.add('\uFF1F'); // ？
            compressRightSet.add('\uFF3D'); // ］
            compressRightSet.add('\uFF5D'); // ｝
        }
        return compressRightSet.contains(code);
    }

    private boolean cannotLineBegin(char code) {
        if (canCompressRight(code)) {
            return true;
        }
        if (notLineBeginSet == null) {
            notLineBeginSet = new HashSet<>();
            notLineBeginSet.add('!');
            notLineBeginSet.add(')');
            notLineBeginSet.add(',');
            notLineBeginSet.add('.');
            notLineBeginSet.add(':');
            notLineBeginSet.add(';');
            notLineBeginSet.add('>');
            notLineBeginSet.add('?');
            notLineBeginSet.add(']');
            notLineBeginSet.add('}');
        }
        return notLineBeginSet.contains(code);
    }

    private boolean cannotLineEnd(char code) {
        if (canCompressLeft(code)) {
            return true;
        }
        if (notLineEndSet == null) {
            notLineEndSet = new HashSet<>();
            notLineEndSet.add('(');
            notLineEndSet.add('<');
            notLineEndSet.add('[');
            notLineEndSet.add('{');
        }
        return notLineEndSet.contains(code);
    }

    private boolean shouldRotateForVertical(char code) {
        if (code < 0x2600) {
            return true;
        }
        if (rotateForVertical == null) {
            rotateForVertical = new HashSet<>();
            rotateForVertical.add('\u2014'); // —
            rotateForVertical.add('\u2026'); // …
            rotateForVertical.add('\u3008'); // 〈
            rotateForVertical.add('\u3009'); // 〉
            rotateForVertical.add('\u300A'); // 《
            rotateForVertical.add('\u300B'); // 》
            rotateForVertical.add('\u300C'); // 「
            rotateForVertical.add('\u300D'); // 」
            rotateForVertical.add('\u300E'); // 『
            rotateForVertical.add('\u300F'); // 』
            rotateForVertical.add('\u3010'); // 【
            rotateForVertical.add('\u3011'); // 】
            rotateForVertical.add('\u3014'); // 〔
            rotateForVertical.add('\u3015'); // 〕
            rotateForVertical.add('\u3016'); // 〖
            rotateForVertical.add('\u3017'); // 〗
            rotateForVertical.add('\uFF08'); // （
            rotateForVertical.add('\uFF09'); // ）
            rotateForVertical.add('\uFF3B'); // ［
            rotateForVertical.add('\uFF3D'); // ］
            rotateForVertical.add('\uFF5B'); // ｛
            rotateForVertical.add('\uFF5D'); // ｝
        }
        return rotateForVertical.contains(code);
    }
}
