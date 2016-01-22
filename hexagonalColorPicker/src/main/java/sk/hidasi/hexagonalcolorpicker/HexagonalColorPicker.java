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

public class HexagonalColorPicker extends FrameLayout implements View.OnTouchListener {

	private static final float VIEW_ASPECT_RATIO = (float) Math.sqrt(4.0/3.0);

	private static final int ANIM_TIME_VIEW  = 500;
	private static final int ANIM_TIME_SWATCH = 200;
	private static final int DEFAULT_PALETTE_RADIUS = 3;

	private HexagonalColorSwatch[] mSwatches;
	private ImageView mChecker;
	private GradientDrawable mShadowDrawable;
	private PointF mRenderOffset;
	private PointF mRenderSize;
	private int mPaletteRadius;
	private int mSelectedColor;
	private final Interpolator mInterpolator = new OvershootInterpolator();
	private OnColorSelectedListener mListener;

	public interface OnColorSelectedListener {
		/**
		 * Called when a specific color swatch has been selected
		 */
		void onColorSelected(final int color);
	}

    public HexagonalColorPicker(Context context) {
		this(context, null, 0);
	}

	public HexagonalColorPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HexagonalColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

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

	public void setAttrs(final int paletteRadius, final int selectedColor, final OnColorSelectedListener listener) {

		mPaletteRadius = paletteRadius;
		mSelectedColor = selectedColor;
		mListener = listener;

        initSwatches();
	}

    private void initSwatches() {

        removeAllViews();
        mSwatches = new HexagonalColorSwatch[swatchCount(mPaletteRadius)];

        int index = 0;
        for (int y = -mPaletteRadius*2; y <= mPaletteRadius*2; y += 2) {
            final int rowSize = mPaletteRadius*2 - Math.abs(y/2);
            for (int x = -rowSize; x <= rowSize; x += 2) {
                final float positionX = (float) x / (mPaletteRadius*2 + 1);
                final float positionY = (float) y / (mPaletteRadius*2 + 1);
                final int color = calculateColor(x, y);
                final int animDelay = (ANIM_TIME_VIEW - ANIM_TIME_SWATCH) * index / mSwatches.length;
                HexagonalColorSwatch swatch = new HexagonalColorSwatch(getContext(), positionX, positionY, color, animDelay, mShadowDrawable);
                addView(swatch);
                mSwatches[index++] = swatch;
                swatch.setOnTouchListener(this);
            }
        }

        if (index != mSwatches.length) {
            throw new IllegalStateException();
        }

        addView(mChecker);

        updateSwatchesPosition();
    }

    public void setShadowColor(final int shadowColor) {

		mShadowDrawable.setColor(shadowColor);
	}

	public void setListener(final OnColorSelectedListener listener) {
		mListener = listener;
	}

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

	private void updateCheckerPosition(final HexagonalColorSwatch item) {
		mChecker.setLayoutParams(item.getLayoutParams());
		mChecker.setPadding(item.getPaddingLeft(), item.getPaddingTop(), item.getPaddingRight(), item.getPaddingBottom());
	}

	private void updateSwatchesPosition() {
		if (mRenderSize == null || mRenderOffset == null) {
			return;
		}

		final int swatchRadius = (int) getItemRadius();
		final int padding = (int) (0.075f * getItemRadius());
		final int strokeWidth = (int) (0.05f * getItemRadius());

		for (HexagonalColorSwatch item : mSwatches) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(swatchRadius * 2 - strokeWidth, swatchRadius * 2 - strokeWidth);
			params.leftMargin = (int) getItemPositionX(item) - swatchRadius;
			params.topMargin = (int) getItemPositionY(item) - swatchRadius;
			params.gravity = Gravity.TOP | Gravity.LEFT;
			item.setLayoutParams(params);
			item.setPadding(0, 0, padding, padding);
			item.updateStrokeWidth(strokeWidth);

			final Animation itemAnim = getItemAnimation(swatchRadius, item.mAnimDelay);
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

    private Animation getItemAnimation(final int swatchRadius, final int delay) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return null;
        }
        ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, swatchRadius, swatchRadius);
        scaleAnim.setDuration(ANIM_TIME_SWATCH);
        scaleAnim.setStartOffset(delay);
        scaleAnim.setInterpolator(mInterpolator);
        return scaleAnim;
    }

	private static int swatchCount(final int radius) {
		return 3*radius*(radius + 1) + 1;
	}
	
	private float getItemRadius() {
		if (mRenderSize == null) {
			return 0.0f;
		}
		return 0.5f * mRenderSize.x / (mPaletteRadius * 2 + 1);
	}

	private float getItemPositionX(final HexagonalColorSwatch item) {
        return mRenderOffset.x + (1.0f + item.mPosition.x) * 0.5f * mRenderSize.x;
	}

	private float getItemPositionY(final HexagonalColorSwatch item) {
        return mRenderOffset.y + (1.0f + item.mPosition.y) * 0.5f * mRenderSize.y;
	}

    private int calculateColor(final int x, final int y) {
        if (isInEditMode()) {
            return Color.CYAN;
        }
        final float radius = (float) (mPaletteRadius*2);
        final float[] hsv = {
                360.0f * (float) (0.5 + 0.5 * Math.atan2(y, x) / Math.PI),  // hue
                (float) Math.sqrt(x*x + y*y) / radius,                      // saturation
                1.0f                                                        // value
        };
        return Color.HSVToColor(hsv);
    }

    public static int getStrokeColor(final int color) {
		// set stroke color slightly darker than fill
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

    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		final float strokePadding = Math.min(w, h) * 0.025f;
		final float width = w - getPaddingLeft() - getPaddingRight() - 2.0f * strokePadding;
		final float height = h - getPaddingTop() - getPaddingBottom() - 2.0f * strokePadding;
		mRenderOffset = new PointF(strokePadding, strokePadding);
		mRenderSize = new PointF(width, height);

		if (width > height * VIEW_ASPECT_RATIO) {
			final float halfDif = (width - height * VIEW_ASPECT_RATIO) * 0.5f;
			mRenderOffset.x += halfDif;
			mRenderSize.x -= 2.0f * halfDif;
		} else if (height > width / VIEW_ASPECT_RATIO) {
			final float halfDif = (height - width / VIEW_ASPECT_RATIO) * 0.5f;
			mRenderOffset.y += halfDif;
			mRenderSize.y -= 2.0f * halfDif;
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
			heightSize = (int)(widthSize / VIEW_ASPECT_RATIO);
		}
		if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.EXACTLY) {
			widthSize = (int)(heightSize * VIEW_ASPECT_RATIO);
		}

		setMeasuredDimension(widthSize, heightSize);
	}
}
