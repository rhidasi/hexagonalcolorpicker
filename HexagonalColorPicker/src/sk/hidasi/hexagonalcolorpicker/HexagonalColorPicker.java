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
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class HexagonalColorPicker extends View {

	private static final float VIEW_ASPECT_RATIO = (float) Math.sqrt(4.0/3.0);

	private static final int ANIM_TIME_VIEW  = 400;
	private static final int ANIM_TIME_SWATCH = 200;
	private static final int DEFAULT_PALETTE_RADIUS = 3;

	public interface OnColorSelectedListener {
		/**
		 * Called when a specific color swatch has been selected
		 */
		public void onColorSelected(final int color);
	}

	class ColorSwatch {
		final public float mCoordX;
		final public float mCoordY;
		public int mColor;
		public int mColorStroke;
		public int mAnimStart;
		public int mAnimEnd;

		public ColorSwatch( final float coordX, final float coordY, final int animStart) {
			mCoordX = coordX;
			mCoordY = coordY;
			mAnimStart = animStart;
			mAnimEnd = animStart + ANIM_TIME_SWATCH;
		}
	}

	private ColorSwatch[] mSwatches;
	private Drawable mChecker;
	private RectF mBounds;
	private Paint mPaintFill;
	private Paint mPaintStroke;
	private int mPaletteRadius;
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
		init();		
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
		
	private void init() {

		mSwatches = new ColorSwatch[swatchCount(mPaletteRadius)];

		int indx = 0;
		for ( int y = -mPaletteRadius*2; y <= mPaletteRadius*2; y += 2 ) {
			final int rowSize = mPaletteRadius*2 - Math.abs(y/2);
			for ( int x = -rowSize; x <= rowSize; x += 2) {
				final int animTimeStart = (ANIM_TIME_VIEW - ANIM_TIME_SWATCH) * indx / mSwatches.length;
				final ColorSwatch item = new ColorSwatch( getItemCoordX(x), getItemCoordY(y), animTimeStart);
				initItemColors(item, x, y);
				mSwatches[indx++] = item;
			}
		}
		
		if ( indx != mSwatches.length ) {
			throw new IllegalStateException();
		}

		mPaintFill = new Paint();
		mPaintFill.setStyle( Paint.Style.FILL );
		mPaintFill.setAntiAlias(true);

		mPaintStroke = new Paint();
		mPaintStroke.setStyle( Paint.Style.STROKE );
		mPaintStroke.setAntiAlias(true);

		mAnimStartMilis = 0;

		if ( !isInEditMode() ) {
			mChecker = getResources().getDrawable(R.drawable.ic_colorpicker_swatch_selected);
		}
	}
	
	private static int swatchCount(final int radius) {
		return 3*radius*(radius + 1) + 1;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if ( mBounds == null )
			return;
		
		if ( mAnimStartMilis == 0 ) {
			mAnimStartMilis = System.currentTimeMillis();
		}
		
		final int time = (int)(System.currentTimeMillis() - mAnimStartMilis);

		final float fullSize = getItemRadius() * 0.9f;
		mPaintStroke.setStrokeWidth(0.1f * fullSize);
		final float aspect = 1.0f / ANIM_TIME_SWATCH;

		for ( final ColorSwatch item : mSwatches ) {

			float r;
			if ( time < item.mAnimStart ) {
				r = 0.0f;
			} else if ( time < item.mAnimEnd ) {
				final float delta = (time - item.mAnimStart) * aspect;
				r = fullSize * mInterpolator.getInterpolation(delta);
			} else {
				r = fullSize;
			}

			if ( isInEditMode() ) {
				r = fullSize;
			}			

			final float x = mBounds.left + item.mCoordX * mBounds.width();
			final float y = mBounds.top + item.mCoordY * mBounds.height();

			if (mShadowDistance > 0) {
				mPaintFill.setColor(mShadowColor);
				canvas.drawCircle(x+mShadowDistance/2, y+mShadowDistance, r*1.05f, mPaintFill);
			}

			mPaintStroke.setColor(item.mColorStroke);
			canvas.drawCircle(x, y, r, mPaintStroke);

			mPaintFill.setColor(item.mColor);
			canvas.drawCircle(x, y, r, mPaintFill);

			if ( item.mColor == mSelectedColor && time >= item.mAnimEnd ) {
				mChecker.setBounds((int)(x-r), (int)(y-r), (int)(x+r), (int)(y+r));
				mChecker.draw(canvas);
			}
		}


		if ( time <= ANIM_TIME_VIEW ) {
			postInvalidateDelayed(20);
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

	private void initItemColors(ColorSwatch item, final int localX, final int localY) {

		if ( isInEditMode() ) {
			item.mColor = Color.CYAN;
			item.mColorStroke = Color.BLUE;
		} else {
			final float x = (float)localX / (float)(mPaletteRadius*2);
			final float y = (float)localY / (float)(mPaletteRadius*2);
			final float[] hsv = { 360.0f * (float) (0.5 + 0.5 * Math.atan2(y, x) / Math.PI), (float) Math.sqrt(x*x + y*y), 1.0f };

			item.mColor = Color.HSVToColor(hsv);
			hsv[2] = 0.5f;
			item.mColorStroke = Color.HSVToColor(hsv);
		}
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
