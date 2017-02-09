/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import de.vanita5.twittnuker.R;

public class BadgeView extends View {

    private final TextPaint mTextPaint;
    private String mText;
    private float mTextX, mTextY;
    private Rect mTextBounds;

    public BadgeView(Context context) {
        this(context, null);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgeView);
        setColor(a.getColor(R.styleable.BadgeView_android_textColor, Color.WHITE));
        setText(a.getString(R.styleable.BadgeView_android_text));
        a.recycle();
        mTextPaint.setTextAlign(Align.CENTER);
        mTextBounds = new Rect();
    }


    public void setColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setText(String text) {
        mText = text;
        updateTextPosition();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        final int hPadding = (int) (Math.round(w * (Math.pow(2, 0.5f) - 1)) / 2);
        final int vPadding = (int) (Math.round(h * (Math.pow(2, 0.5f) - 1)) / 2);
        setPadding(hPadding, vPadding, hPadding, vPadding);
        updateTextPosition();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mTextBounds.isEmpty()) {
            canvas.drawText(mText, mTextX, mTextY, mTextPaint);
        }
    }

    private void updateTextPosition() {
        final int width = getWidth(), height = getHeight();
        if (width == 0 || height == 0) return;
        final float contentWidth = width - getPaddingLeft() - getPaddingRight();
        final float contentHeight = height - getPaddingTop() - getPaddingBottom();

        if (mText != null) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            final float scale = Math.min(contentWidth / mTextBounds.width(), contentHeight / mTextBounds.height());
            mTextPaint.setTextSize(Math.min(height / 2, mTextPaint.getTextSize() * scale));
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
            mTextX = contentWidth / 2 + getPaddingLeft();
            mTextY = contentHeight / 2 + getPaddingTop() + mTextBounds.height() / 2;
        } else {
            mTextBounds.setEmpty();
        }
    }
}