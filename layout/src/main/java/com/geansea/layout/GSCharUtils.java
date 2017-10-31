package com.geansea.layout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
final class GSCharUtils {
    static boolean isNewline(char code) {
        return code == '\r' || code == '\n';
    }

    static boolean isVerticalPunctuation(char code) {
        return ('\uFE10' <= code && code <= '\uFE16');
    }

    static boolean shouldAddGap(GSLayoutGlyph glyph0, GSLayoutGlyph glyph1) {
        if (glyph0 == null || glyph1 == null) {
            return false;
        }
        if (glyph0.paint.baselineShift != glyph1.paint.baselineShift) {
            return false;
        }
        if (glyph0.isItalic() && !glyph1.isItalic()) {
            return true;
        }
        char code0 = glyph0.code();
        char code1 = glyph1.code();
        if (isCjk(code0)) {
            return isAlphaDigit(code1);
        }
        if (isCjk(code1)) {
            return isAlphaDigit(code0) || code0 == '%';
        }
        return false;
    }

    static boolean canCompress(GSLayoutGlyph glyph) {
        if (glyph == null) {
            return false;
        }
        if (!glyph.isFullSize()) {
            return false;
        }
        char code = glyph.code();
        if (isVerticalFullSizePunctuation(code)) {
            return false;
        }
        return true;
    }

    static boolean shouldCompressStart(GSLayoutGlyph glyph) {
        if (glyph == null) {
            return false;
        }
        return compressStartSet.contains(glyph.code());
    }

    static boolean shouldCompressEnd(GSLayoutGlyph glyph) {
        if (glyph == null) {
            return false;
        }
        return compressEndSet.contains(glyph.code());
    }

    static boolean canBreak(GSLayoutGlyph glyph0, GSLayoutGlyph glyph1) {
        if (glyph0 == null || glyph1 == null) {
            return false;
        }
        if (cannotLineEnd(glyph0)) {
            return false;
        }
        if (cannotLineBegin(glyph1)) {
            return false;
        }
        // Not break sup/sub
        if (glyph1.paint.baselineShift != 0) {
            return false;
        }
        char code0 = glyph0.code();
        char code1 = glyph1.code();
        // Always can break after space
        if (code0 == ' ') {
            return true;
        }
        // No Break SPace
        if (code0 == '\u00A0') {
            return false;
        }
        // Space follow prev
        if (code1 == ' ' || code1 == '\u00A0') {
            return false;
        }
        if (isAlphaDigit(code0)) {
            if (isAlphaDigit(code1)) {
                return false;
            }
            if (code1 == '\'' || code1 == '\"' || code1 == '-' || code1 == '_' || code1 == '%') {
                return false;
            }
        }
        if (isAlphaDigit(code1)) {
            if (code0 == '\'' || code0 == '\"' || code0 == '\\' || code0 == '\u2019') {
                return false;
            }
        }
        return true;
    }

    static boolean canStretch(GSLayoutGlyph glyph0, GSLayoutGlyph glyph1) {
        if (!canBreak(glyph0, glyph1)) {
            return false;
        }
        char code0 = glyph0.code();
        char code1 = glyph1.code();
        if ('/' == code0 && isAlphaDigit(code1)) {
            return false;
        }
        if (isAlphaDigit(code0) && '/' == code1) {
            return false;
        }
        return true;
    }

    static boolean shouldRotateForVertical(char code) {
        if (code < '\u2600') {
            return true;
        }
        return rotateForVerticalSet.contains(code);
    }

    static String replaceTextForVertical(String text) {
        StringBuilder textBuilder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ++i) {
            char code = text.charAt(i);
            code = replaceForVertical(code);
            textBuilder.append(code);
        }
        return textBuilder.toString();
    }

    private static final HashSet<Character> compressStartSet
            = new HashSet<>(Arrays.asList(new Character[]{
            '\u2018', // ‘
            '\u201C', // “
            '\u3008', // 〈
            '\u300A', // 《
            '\u300C', // 「
            '\u300E', // 『
            '\u3010', // 【
            '\u3014', // 〔
            '\u3016', // 〖
            '\uFF08', // （
            '\uFF3B', // ［
            '\uFF5B', // ｛
    }));

    private static final HashSet<Character> compressEndSet
            = new HashSet<>(Arrays.asList(new Character[]{
            '\u2019', // ’
            '\u201D', // ”
            '\u3001', // 、
            '\u3002', // 。
            '\u3009', // 〉
            '\u300B', // 》
            '\u300D', // 」
            '\u300F', // 』
            '\u3011', // 】
            '\u3015', // 〕
            '\u3017', // 〗
            '\uFF01', // ！
            '\uFF09', // ）
            '\uFF0C', // ，
            '\uFF1A', // ：
            '\uFF1B', // ；
            '\uFF1F', // ？
            '\uFF3D', // ］
            '\uFF5D', // ｝
            // For vertical
            '\uFE10', // ，
            '\uFE11', // 、
            '\uFE12', // 。
            '\uFE13', // ：
            '\uFE14', // ；
            '\uFE15', // ！
            '\uFE16', // ？
    }));

    private static final HashSet<Character> notLineBeginSet
            = new HashSet<>(Arrays.asList(new Character[]{
            '!',
            ')',
            ',',
            '.',
            ':',
            ';',
            '>',
            '?',
            ']',
            '}',
    }));

    private static final HashSet<Character> notLineEndSet
            = new HashSet<>(Arrays.asList(new Character[]{
            '(',
            '<',
            '[',
            '{',
    }));

    private static final HashSet<Character> rotateForVerticalSet
            = new HashSet<>(Arrays.asList(new Character[]{
            '\u2014', // —
            '\u2026', // …
            '\u3008', // 〈
            '\u3009', // 〉
            '\u300A', // 《
            '\u300B', // 》
            '\u300C', // 「
            '\u300D', // 」
            '\u300E', // 『
            '\u300F', // 』
            '\u3010', // 【
            '\u3011', // 】
            '\u3014', // 〔
            '\u3015', // 〕
            '\u3016', // 〖
            '\u3017', // 〗
            '\uFF08', // （
            '\uFF09', // ）
            '\uFF3B', // ［
            '\uFF3D', // ］
            '\uFF5B', // ｛
            '\uFF5D', // ｝
    }));

    private static final HashMap<Character, Character> replaceForVerticalMap
            = new HashMap<Character, Character>() {{
        put('\uFF0C', '\uFE10'); // ，
        put('\u3001', '\uFE11'); // 、
        put('\u3002', '\uFE12'); // 。
        put('\uFF1A', '\uFE13'); // ：
        put('\uFF1B', '\uFE14'); // ；
        put('\uFF01', '\uFE15'); // ！
        put('\uFF1F', '\uFE16'); // ？
        //put('\u3016', '\uFE17'); // 〖
        //put('\u3017', '\uFE18'); // 〗
        //put('\u2026', '\uFE19'); // …
        // For quotes
        put('\u2018', '\u300C'); // ‘ -> 「
        put('\u2019', '\u300D'); // ’ -> 」
        put('\u201C', '\u300E'); // “ -> 『
        put('\u201D', '\u300F'); // ” -> 』
    }};

    private static boolean isAlphaDigit(char code) {
        return ('a' <= code && code <= 'z') || ('A' <= code && code <= 'Z') || ('0' <= code && code <= '9');
    }

    private static boolean isCjk(char code) {
        return (0x4E00 <= code && code < 0xD800) || (0xE000 <= code && code < 0xFB00);
    }

    private static boolean isVerticalFullSizePunctuation(char code) {
        return ('\uFE13' <= code && code <= '\uFE16');
    }

    private static boolean cannotLineBegin(GSLayoutGlyph glyph) {
        if (shouldCompressEnd(glyph)) {
            return true;
        }
        return notLineBeginSet.contains(glyph.code());
    }

    private static boolean cannotLineEnd(GSLayoutGlyph glyph) {
        if (shouldCompressStart(glyph)) {
            return true;
        }
        return notLineEndSet.contains(glyph.code());
    }

    private static char replaceForVertical(char code) {
        Character value = replaceForVerticalMap.get(code);
        return (value != null ? value : code);
    }
}
