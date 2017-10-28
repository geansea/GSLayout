package com.geansea.layout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
final class GSCharacterUtils {
    private HashSet<Character> compressLeftSet;
    private HashSet<Character> compressRightSet;
    private HashSet<Character> notLineBeginSet;
    private HashSet<Character> notLineEndSet;
    private HashSet<Character> rotateForVerticalSet;
    private HashMap<Character, Character> replaceForVerticalMap;

    GSCharacterUtils() {
        compressLeftSet = new HashSet<>(Arrays.asList(new Character[]{
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
        compressRightSet = new HashSet<>(Arrays.asList(new Character[]{
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
        notLineBeginSet = new HashSet<>(Arrays.asList(new Character[]{
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
        notLineEndSet = new HashSet<>(Arrays.asList(new Character[]{
                '(',
                '<',
                '[',
                '{',
        }));
        rotateForVerticalSet = new HashSet<>(Arrays.asList(new Character[]{
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
        replaceForVerticalMap = new HashMap<Character, Character>() {{
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
    }

    boolean isNewline(char code) {
        return code == '\r' || code == '\n';
    }

    boolean isNewline(GSLayoutGlyph glyph) {
        return glyph != null && isNewline(glyph.code());
    }

    boolean shouldAddGap(char prevCode, char code) {
        return (isAlphaDigit(prevCode) && isCjk(code)) ||
                (isCjk(prevCode) && isAlphaDigit(code)) ||
                ('%' == prevCode && isCjk(code));
    }

    boolean shouldAddGap(GSLayoutGlyph glyph0, GSLayoutGlyph glyph1) {
        if (glyph0 == null || glyph1 == null) {
            return false;
        }
        char code0 = glyph0.code();
        char code1 = glyph1.code();
        if (glyph0.isItalic() && !glyph1.isItalic() && code1 != ' ') {
            return true;
        }
        if (isCjk(code0)) {
            return isAlphaDigit(code1);
        }
        if (isCjk(code1)) {
            return isAlphaDigit(code0) || code0 == '%';
        }
        return false;
    }

    boolean canCompressLeft(char code) {
        return compressLeftSet.contains(code);
    }

    boolean canCompress(GSLayoutGlyph glyph) {
        if (glyph == null) {
            return false;
        }
        if (!glyph.isFullWidth()) {
            return false;
        }
        char code = glyph.code();
        if ('\uFE13' <= code && code <= '\uFE16') {
            return false;
        }
        return true;
    }

    boolean shouldCompressStart(GSLayoutGlyph glyph) {
        if (glyph == null) {
            return false;
        }
        return compressLeftSet.contains(glyph.code());
    }

    boolean shouldCompressEnd(GSLayoutGlyph glyph) {
        if (glyph == null) {
            return false;
        }
        return compressRightSet.contains(glyph.code());
    }

    boolean canCompressRight(char code) {
        return compressRightSet.contains(code);
    }

    boolean canBreak(char prevCode, char code) {
        if (0 == prevCode) {
            return false;
        }
        // Always can break after space
        if (' ' == prevCode) {
            return true;
        }
        // No Break SPace
        if ('\u00A0' == prevCode) {
            return false;
        }
        // Space follow prev
        if (' ' == code || '\u00A0' == code) {
            return false;
        }
        if (isAlphaDigit(prevCode)) {
            if (isAlphaDigit(code)) {
                return false;
            }
            if ('\'' == code || '\"' == code || '-' == code || '_' == code || '%' == code) {
                return false;
            }
        }
        if (isAlphaDigit(code)) {
            if ('\'' == prevCode || '\"' == prevCode || '\u2019' == prevCode) {
                return false;
            }
        }
        return !cannotLineEnd(prevCode) && !cannotLineBegin(code);
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

    boolean shouldRotateForVertical(char code) {
        return code < 0x2600 || rotateForVerticalSet.contains(code);
    }

    String replaceTextForVertical(String text) {
        StringBuilder textBuilder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ++i) {
            char code = text.charAt(i);
            code = replaceForVertical(code);
            textBuilder.append(code);
        }
        return textBuilder.toString();
    }

    private boolean isAlphaDigit(char code) {
        return ('a' <= code && code <= 'z') || ('A' <= code && code <= 'Z') || ('0' <= code && code <= '9');
    }

    private boolean isCjk(char code) {
        return (0x4E00 <= code && code < 0xD800) || (0xE000 <= code && code < 0xFB00);
    }

    private boolean cannotLineBegin(char code) {
        return canCompressRight(code) || notLineBeginSet.contains(code);
    }

    private boolean cannotLineEnd(char code) {
        return canCompressLeft(code) || notLineEndSet.contains(code);
    }

    private char replaceForVertical(char code) {
        Character value = replaceForVerticalMap.get(code);
        return (value != null ? value : code);
    }
}
