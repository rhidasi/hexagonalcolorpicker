/*
 * Copyright (C) 2015 Robert Hidasi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.hidasi.hexagonalcolorpicker;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;

class HexagonalColorSwatch extends ImageView {

    final public PointF mPosition;
    final public int mColor;
    final public int mAnimDelay;
    final private int mStrokeColor;
    final private GradientDrawable mDrawable;

    public HexagonalColorSwatch(final Context context, final float positionX, final float positionY, final int color, final int animDelay, final Drawable background) {
        super(context);

        mPosition = new PointF(positionX, positionY);
        mColor = color;
        mStrokeColor = HexagonalColorPicker.getStrokeColor(mColor);
        mAnimDelay = animDelay;

        mDrawable = new GradientDrawable();
        mDrawable.setShape(GradientDrawable.OVAL);
        mDrawable.setColor(mColor);
        setImageDrawable(mDrawable);
        if (background != null) {
            setBackgroundCorrect(background);
        }
    }

    public void updateStrokeWidth(final int strokeWidth) {
        mDrawable.setStroke(strokeWidth, mStrokeColor);
    }

    @SuppressWarnings("deprecation")
    private void setBackgroundCorrect(final Drawable drawable) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable);
        } else {
            setBackground(drawable);
        }
    }

}
