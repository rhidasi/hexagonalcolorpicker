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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Hexagonal color picker layout.
 * This is the parent layout which displays a color palette with color swatches.
 * The number of color swatches depends on mPaletteRadius (@link #swatchCount()).
 */
public class HexagonalColorPicker extends FrameLayout implements View.OnTouchListener {

    // Aspect ratio of the view (4:3).
    private static final float VIEW_ASPECT_RATIO = (float) Math.sqrt(4.0 / 3.0);
    // Duration of the animation for the whole view (all swatches).
    private static final int ANIM_TIME_VIEW = 500;
    // Duration of the animation for a single swatch.
    private static final int ANIM_TIME_SWATCH = 200;
    // Default palette radius (if not specified).
    private static final int DEFAULT_PALETTE_RADIUS = 3;

    // Radius of the palette (0 => 1 swatch, 1 => 7 swatches, ...)
    private int mPaletteRadius;
    // Selected color value
    private int mSelectedColor;
    // Swatch views pivot point (pixels)
    private PointF mSwatchPivot;
    // Swatch views scale (pixels)
    private PointF mSwatchScale;
    // Check mark (selected color swatch)
    private ImageView mChecker;
    // Swatch shadow
    private GradientDrawable mShadowDrawable;
    // Animation interpolator
    private final Interpolator mInterpolator = new OvershootInterpolator();
    // Selected color listener
    private OnColorSelectedListener mListener;

    /**
     * The interface of selected color listener.
     */
    public interface OnColorSelectedListener {
        /**
         * Called when a specific color swatch has been selected
         *
         * @param color New color
         */
        void onColorSelected(final int color);
    }

    /**
     * Constructor.
     */
    public HexagonalColorPicker(Context context) {
        super(context);
        init(null, 0);
    }

    /**
     * Constructor.
     */
    public HexagonalColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    /**
     * Constructor.
     */
    public HexagonalColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    /**
     * Read attributes and initialize members (call only from constructor).
     *
     * @param attrs        Attribute Set
     * @param defStyleAttr Default style
     */
    private void init(AttributeSet attrs, int defStyleAttr) {

        final TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.HexagonalColorPicker, defStyleAttr, defStyleAttr);

        mPaletteRadius = a.getInteger(R.styleable.HexagonalColorPicker_paletteRadius, DEFAULT_PALETTE_RADIUS);
        final int shadowColor = a.getColor(R.styleable.HexagonalColorPicker_shadowColor, Color.GRAY);
        a.recycle();

        mChecker = new ImageView(getContext());
        mChecker.setImageResource(R.drawable.ic_colorpicker_swatch_selected);

        mShadowDrawable = new GradientDrawable();
        mShadowDrawable.setShape(GradientDrawable.OVAL);
        mShadowDrawable.setColor(shadowColor);

        mSelectedColor = Color.TRANSPARENT;
        mListener = null;

        initSwatches();
    }

    /**
     * Set attributes.
     *
     * @param paletteRadius Palette radius
     * @param selectedColor Selected color
     * @param listener      Color change listener
     */
    public void setAttrs(final int paletteRadius, final int selectedColor, final OnColorSelectedListener listener) {

        mPaletteRadius = paletteRadius;
        mSelectedColor = selectedColor;
        mListener = listener;

        initSwatches();
    }

    /**
     * Initialize color swatches (child views).
     */
    private void initSwatches() {

        removeAllViews();
        final int swatchCount = getSwatchCount(mPaletteRadius);

        int index = 0;
        for (int y = -mPaletteRadius * 2; y <= mPaletteRadius * 2; y += 2) {
            final int rowSize = mPaletteRadius * 2 - Math.abs(y / 2);
            for (int x = -rowSize; x <= rowSize; x += 2) {
                final PointF position = new PointF((float) x / (mPaletteRadius * 2 + 1), (float) y / (mPaletteRadius * 2 + 1));
                final int color = calculateColor(x, y);
                final int animDelay = (ANIM_TIME_VIEW - ANIM_TIME_SWATCH) * index++ / swatchCount;
                HexagonalColorSwatch swatch = new HexagonalColorSwatch(getContext(), color, position, animDelay, mShadowDrawable);
                addView(swatch);
                swatch.setOnTouchListener(this);
            }
        }

        if (index != swatchCount) {
            throw new IllegalStateException("The number of color swatches and palette radius are inconsistent.");
        }

        addView(mChecker);

        updateSwatchesPosition();
    }

    /**
     * Sets shadow color of swatches.
     *
     * @param shadowColor New shadow color
     */
    public void setShadowColor(final int shadowColor) {
        mShadowDrawable.setColor(shadowColor);
    }

    /**
     * Sets color change listener.
     *
     * @param listener New listener
     */
    public void setListener(final OnColorSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Gets selected color.
     *
     * @return Selected color
     */
    public int getSelectedColor() {
        return mSelectedColor;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateSwatchesPosition();
        }
    }

    /**
     * Updates the position of check mark.
     * Call when selected color is changed.
     *
     * @param item New selected color swatch
     */
    private void updateCheckerPosition(final HexagonalColorSwatch item) {
        mChecker.setLayoutParams(item.getLayoutParams());
        mChecker.setPadding(item.getPaddingLeft(), item.getPaddingTop(), item.getPaddingRight(), item.getPaddingBottom());
    }

    /**
     * Update position of all color swatches (child views).
     * Called after layout change.
     */
    private void updateSwatchesPosition() {
        if (mSwatchScale == null || mSwatchPivot == null) {
            return;
        }

        final float swatchRadius = getSwatchRadius();
        final int padding = (int) (0.075f * swatchRadius);
        final int strokeWidth = (int) (0.05f * swatchRadius);

        for (int i = 0; i < getChildCount(); i++) {
            final View childView = getChildAt(i);
            if (childView instanceof HexagonalColorSwatch) {
                final HexagonalColorSwatch item = (HexagonalColorSwatch) childView;
                final int swatchSize = (int) (swatchRadius - strokeWidth) * 2;
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(swatchSize, swatchSize);
                params.leftMargin = (int) (getItemPositionX(item) - swatchRadius);
                params.topMargin = (int) (getItemPositionY(item) - swatchRadius);
                params.gravity = Gravity.TOP | Gravity.LEFT;
                item.setLayoutParams(params);
                item.setPadding(0, 0, padding, padding);
                item.updateStrokeWidth(strokeWidth);

                final Animation itemAnim = createSwatchAnimation(swatchRadius, item.mAnimDelay);
                if (itemAnim != null) {
                    item.startAnimation(itemAnim);
                }

                if (item.mColor == mSelectedColor) {
                    updateCheckerPosition(item);
                    if (itemAnim != null) {
                        mChecker.startAnimation(itemAnim);
                    }
                }
            }
        }
    }

    /**
     * Creates a animation for swatch item or returns null!
     *
     * @param swatchRadius Radius of the color swatch
     * @param delay        Animation delay
     * @return Animation object or null
     */
    private Animation createSwatchAnimation(final float swatchRadius, final int delay) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return null;
        }
        ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, swatchRadius, swatchRadius);
        scaleAnim.setDuration(ANIM_TIME_SWATCH);
        scaleAnim.setStartOffset(delay);
        scaleAnim.setInterpolator(mInterpolator);
        return scaleAnim;
    }

    /**
     * Calculates the count of color swatches per palette radius.
     *
     * @param radius Palette radius
     * @return color swatches count
     */
    private static int getSwatchCount(final int radius) {
        return 3 * radius * (radius + 1) + 1;
    }

    /**
     * Calculates the radius of a color swatch.
     *
     * @return Radius of a color swatch.
     */
    private float getSwatchRadius() {
        if (mSwatchScale == null) {
            return 0.0f;
        }
        return 0.5f * mSwatchScale.x / (mPaletteRadius * 2 + 1);
    }

    /**
     * Calculates the X coordinate of a color swatch item.
     * Item coordinates are relative (from -1.0 to 1.0)
     *
     * @param item Color swatch item
     * @return X coordinate in pixels
     */
    private float getItemPositionX(final HexagonalColorSwatch item) {
        return mSwatchPivot.x + (item.mPosition.x * 0.5f * mSwatchScale.x);
    }

    /**
     * Calculates the Y coordinate of a color swatch item.
     * Item coordinates are relative (from -1.0 to 1.0)
     *
     * @param item Color swatch item
     * @return Y coordinate in pixels
     */
    private float getItemPositionY(final HexagonalColorSwatch item) {
        return mSwatchPivot.y + (item.mPosition.y * 0.5f * mSwatchScale.y);
    }

    /**
     * Calculates the color of a color swatch according to item position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Color of the swatch
     */
    private int calculateColor(final int x, final int y) {
        if (isInEditMode()) {
            // the calculation is using native functions and they don't work in edit mode
            return Color.CYAN;
        }
        final float radius = (float) (mPaletteRadius * 2);
        final float[] hsv = {
                360.0f * (float) (0.5 + 0.5 * Math.atan2(y, x) / Math.PI),  // hue
                (float) Math.sqrt(x * x + y * y) / radius,                  // saturation
                1.0f                                                        // value
        };
        return Color.HSVToColor(hsv);
    }

    /**
     * Calculates the stroke color for a color swatch (slightly darker than color fill).
     *
     * @param color Color of the swatch
     * @return The stroke color
     */
    public static int calculateStrokeColor(final int color) {
        return Color.rgb(Color.red(color) / 2, Color.green(color) / 2, Color.blue(color) / 2);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final HexagonalColorSwatch item = (HexagonalColorSwatch) view;
                mSelectedColor = item.mColor;
                updateCheckerPosition(item);
                break;

            case MotionEvent.ACTION_UP:
                mListener.onColorSelected(mSelectedColor);
                break;
        }
        return true;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        final float width = w - getPaddingLeft() - getPaddingRight();
        final float height = h - getPaddingTop() - getPaddingBottom();
        mSwatchPivot = new PointF(width / 2.0f, height / 2.0f);
        // additional padding for swatch stroke and overshoot animation
        final float strokePadding = Math.min(w, h) * 0.05f;
        mSwatchScale = new PointF(width - strokePadding, height - strokePadding);

        if (width > height * VIEW_ASPECT_RATIO) {
            final float diff = width - height * VIEW_ASPECT_RATIO;
            mSwatchScale.x -= diff;
        } else if (height > width / VIEW_ASPECT_RATIO) {
            final float diff = height - width / VIEW_ASPECT_RATIO;
            mSwatchScale.y -= diff;
        }

        updateSwatchesPosition();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = (int) (widthSize / VIEW_ASPECT_RATIO);
        }
        if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.EXACTLY) {
            widthSize = (int) (heightSize * VIEW_ASPECT_RATIO);
        }

        setMeasuredDimension(widthSize, heightSize);
    }
}
