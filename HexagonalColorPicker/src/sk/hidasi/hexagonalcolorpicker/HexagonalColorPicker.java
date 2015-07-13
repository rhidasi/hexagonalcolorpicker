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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class HexagonalColorPicker extends View {

	private static final float VIEW_ASPECT_RATIO = (float) Math.sqrt(4.0/3.0);
	private static final int FRAMES_PER_SECOND = 50;

	private static final int ANIM_TIME_VIEW  = 400;
	private static final int ANIM_TIME_SWATCH = 200;
	private static final int DEFAULT_PALETTE_RADIUS = 3;

	public interface OnColorSelectedListener {
		/**
		 * Called when a specific color swatch has been selected
		 */
		public void onColorSelected(final int color);
	}

	class ColorSwatch extends GradientDrawable {
		final public float mCoordX;
		final public float mCoordY;
		public int mColor;
		public int mAnimStart;
		public int mAnimEnd;

		public ColorSwatch( final int xCoord, final int yCoord, final int strokeWidth, final int animStart) {
			mCoordX = getItemCoordX(xCoord);
			mCoordY = getItemCoordY(yCoord);
			mAnimStart = animStart;
			mAnimEnd = animStart + ANIM_TIME_SWATCH;
			setShape(GradientDrawable.OVAL);
			final float x = (float)xCoord / (float)(mPaletteRadius*2);
			final float y = (float)yCoord / (float)(mPaletteRadius*2);
			final float[] hsv = { 360.0f * (float) (0.5 + 0.5 * Math.atan2(y, x) / Math.PI), (float) Math.sqrt(x*x + y*y), 1.0f };
			mColor = isInEditMode() ? Color.CYAN : Color.HSVToColor(hsv);
			setColor( mColor );
			// stroke color is the same but darker 
			hsv[2] = 0.5f;
			setStroke(strokeWidth, isInEditMode() ? Color.BLUE : Color.HSVToColor(hsv));
		}
	}

	private ColorSwatch[] mSwatches;
	private Drawable mChecker;
	private RectF mBounds;
	private int mPaletteRadius;
	private GradientDrawable mShadowDrawable;
	private int mShadowDistance;
	private int mShadowColor;
	private int mSelectedColor;
	private long mAnimStartMilis;
	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
	private OnColorSelectedListener mListener;

	public HexagonalColorPicker(Context context) {
		super(context);
		initAttrs(null, 0);
	}

	public HexagonalColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(attrs, 0);
	}

	public HexagonalColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttrs(attrs, defStyleAttr);
	}

	private void initAttrs(AttributeSet attrs, int defStyle) {

		final TypedArray a = getContext().getTheme().obtainStyledAttributes(
			attrs, R.styleable.HexagonalColorPicker, defStyle, defStyle);
	
		mPaletteRadius = a.getInteger(R.styleable.HexagonalColorPicker_paletteRadius, DEFAULT_PALETTE_RADIUS);
		mShadowDistance = a.getDimensionPixelSize(R.styleable.HexagonalColorPicker_shadowDistance, 0);
		mShadowColor = a.getColor(R.styleable.HexagonalColorPicker_shadowColor, Color.DKGRAY);
		a.recycle();
		
		mSelectedColor = Color.TRANSPARENT;
		mListener = null;
	}

	public void setAttrs(final int paletteRadius, final int selectedColor, final OnColorSelectedListener listener ) {

		mPaletteRadius = paletteRadius;
		mSelectedColor = selectedColor;
		mListener = listener;
		init();
	}
	
	public void setShadowParams(final int shadowDistance, final int shadowColor) {
		mShadowDistance = shadowDistance;
		mShadowColor = shadowColor;
	}

	public void setListener( final OnColorSelectedListener listener ) {

		mListener = listener;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			init();
		}
	}
	
	private void init() {

		mSwatches = new ColorSwatch[swatchCount(mPaletteRadius)];

		final int strokeWidth = (int)(0.05f * getItemRadius());
		int indx = 0;
		for ( int y = -mPaletteRadius*2; y <= mPaletteRadius*2; y += 2 ) {
			final int rowSize = mPaletteRadius*2 - Math.abs(y/2);
			for ( int x = -rowSize; x <= rowSize; x += 2) {
				final int animTimeStart = (ANIM_TIME_VIEW - ANIM_TIME_SWATCH) * indx / mSwatches.length;
				mSwatches[indx++] = new ColorSwatch(x, y, strokeWidth, animTimeStart);
			}
		}
		
		if ( indx != mSwatches.length ) {
			throw new IllegalStateException();
		}
		
		mShadowDrawable = new GradientDrawable();
		mShadowDrawable.setShape(GradientDrawable.OVAL);
		mShadowDrawable.setColor(mShadowColor);

		mAnimStartMilis = 0;

		if ( !isInEditMode() ) {
			mChecker = getResources().getDrawable(R.drawable.ic_colorpicker_swatch_selected);
		}
	}
	
	private static int swatchCount(final int radius) {
		return 3*radius*(radius + 1) + 1;
	}
	
	private int getSwatchRadius(final ColorSwatch swatch, final int time, final int fullRadius) {

		if ( isInEditMode() ) {
			return fullRadius; 
		}
		
		if ( time < swatch.mAnimStart ) {
			return 0;
		} else if ( time < swatch.mAnimEnd ) {
			final float delta = (float)(time - swatch.mAnimStart) / ANIM_TIME_SWATCH;
			return (int)(fullRadius * mInterpolator.getInterpolation(delta));
		} else {
			return fullRadius;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if ( mBounds == null )
			return;
		
		if ( mAnimStartMilis == 0 ) {
			mAnimStartMilis = System.currentTimeMillis() + 1000 / FRAMES_PER_SECOND;
		}
		
		final int time = (int)(System.currentTimeMillis() - mAnimStartMilis);
		final int fullRadius = (int)(getItemRadius() * 0.9f);

		for ( final ColorSwatch item : mSwatches ) {

			final int r = getSwatchRadius(item, time, fullRadius);
			final int x = (int) (mBounds.left + item.mCoordX * mBounds.width());
			final int y = (int) (mBounds.top + item.mCoordY * mBounds.height());

			if ( mShadowDistance > 0 ) {
				mShadowDrawable.setBounds(x-r+mShadowDistance/2, y-r+mShadowDistance, x+r+mShadowDistance/2, y+r+mShadowDistance);
				mShadowDrawable.draw(canvas);
			}

			item.setBounds(x-r, y-r, x+r, y+r);
			item.draw(canvas);

			if ( item.mColor == mSelectedColor && time >= item.mAnimEnd ) {
				mChecker.setBounds(x-r, y-r, x+r, y+r);
				mChecker.draw(canvas);
			}
		}

		if ( time <= ANIM_TIME_VIEW ) {
			postInvalidateDelayed(1000 / FRAMES_PER_SECOND);
		}
	}

	public int getSelectedColor() {
		return mSelectedColor;
	}

	private float getItemRadius() {
		if (mBounds == null) {
			return 0.0f;
		}
		return 0.5f * mBounds.width() / (mPaletteRadius * 2 + 1);
	}

	private float getItemCoordX(final int localX) {
		return 0.5f + 0.5f * localX / (mPaletteRadius * 2 + 1);
	}

	private float getItemCoordY(final int localY) {
		return 0.5f + 0.5f * localY / (mPaletteRadius * 2 + 1);
	}

	private ColorSwatch getTouchItem(final float touchX, final float touchY) {
		final float r = getItemRadius();

		for ( final ColorSwatch item : mSwatches ) {
			final float x = mBounds.left + item.mCoordX * mBounds.width();
			final float y = mBounds.top + item.mCoordY * mBounds.height();
			if ( x-r < touchX && touchX < x+r && y-r < touchY && touchY < y+r ) {
				return item;
			}
		}

		return null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		final ColorSwatch item = getTouchItem(event.getX(), event.getY());

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (item != null) {
					mSelectedColor = item.mColor;
					invalidate();
				}
				break;

			case MotionEvent.ACTION_UP:
				if ( mListener != null && item != null ) {
					if ( mSelectedColor == item.mColor ) {
						mListener.onColorSelected(mSelectedColor);
					}
				}
				break;
		}

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		mBounds = new RectF(0, 0, w, h);

		mBounds.left += getPaddingLeft();
		mBounds.right -= getPaddingRight();
		mBounds.top += getPaddingTop();
		mBounds.bottom -= getPaddingBottom();

		final float width = mBounds.width();
		final float height = mBounds.height();

		if ( width > height * VIEW_ASPECT_RATIO ) {
			final float halfDif = (width - height * VIEW_ASPECT_RATIO) * 0.5f;
			mBounds.left += halfDif;
			mBounds.right -= halfDif;
		} else if ( height > width / VIEW_ASPECT_RATIO ) {
			final float halfDif = (height - width / VIEW_ASPECT_RATIO) * 0.5f;
			mBounds.top += halfDif;
			mBounds.bottom -= halfDif;
		}
		init();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if ( widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.UNSPECIFIED ) {
			heightSize = (int)(widthSize / VIEW_ASPECT_RATIO);
		}
		if ( widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.EXACTLY ) {
			widthSize = (int)(heightSize * VIEW_ASPECT_RATIO);
		}

		setMeasuredDimension(widthSize, heightSize);
	}
}
