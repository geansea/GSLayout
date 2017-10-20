package com.geansea.layout;

import android.support.annotation.NonNull;
import android.text.Spanned;
import android.text.TextPaint;

public class GSSpannedLayout extends GSLayout {
    protected GSSpannedLayout(@NonNull Spanned text,
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
    }
}
