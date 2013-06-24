/*
 * Copyright 2013 Benjamin lee
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package cm.ben.pulltorefresh.sample.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import cm.ben.pulltorefresh.sample.R;
import cm.ben.pulltorefresh.widget.PullToRefreshView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static cm.ben.pulltorefresh.widget.PullToRefreshView.HeaderView;

public class CustomViewExample extends ListViewExample {

	private MyView mHeaderView;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHeaderView = new MyView(this);
		mAttacher.setHeaderView(mHeaderView, mHeaderView);
		mAttacher.setOnPullToRefreshListener(this);
	}

	public final class MyView extends View implements PullToRefreshView.HeaderViewHandler {

		private Paint mPaint;
		private Paint mBackground;
		private Paint mTextPaint;

		private String[] mSwatches;

		private NumberFormat mNumberFormat;

		private float mScale = 0F;

		public MyView (Context context) {
			super(context);
			super.setWillNotDraw(false);

			mSwatches = getResources().getStringArray(R.array.swatches);

			mNumberFormat = DecimalFormat.getPercentInstance();
			mNumberFormat.setMinimumFractionDigits(0);

			mBackground = new Paint();
			mBackground.setStyle(Paint.Style.FILL);
			mBackground.setColor(Color.WHITE);

			mPaint = new Paint();
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(mAttacher.getProgressColor());

			mTextPaint = new Paint();
			mTextPaint.setStyle(Paint.Style.FILL);
			mTextPaint.setTextSize(38);
			mTextPaint.setColor(Color.BLACK);
		}

		@Override
		public void onDraw (Canvas canvas) {
			super.onDraw(canvas);

			mPaint.setColor(mAttacher.getProgressColor());

			// Get the middle points
			final float x = (getWidth() / 2);
			final float y = (getHeight() / 2);

			// Format out message
			final String message = mNumberFormat.format(mScale);

			// Draw the background
			canvas.drawRect(0, 0, getWidth(), getHeight(), mBackground);


			int count = 1;
			while (count < mSwatches.length) {
				final int distance = ((super.getWidth() * 2) / count);
				final float widthScale = (super.getWidth() * mScale);

				if ((getWidth() - widthScale) < distance) {
					// Change the color
					mPaint.setColor(Color.parseColor(mSwatches[count]));

					// Draw the circle
					canvas.drawCircle(x, y, widthScale / count, mPaint);
				}

				count++;
			}

			// Measure out text bounds
			final Rect rect = new Rect();
			mTextPaint.getTextBounds(message, 0, message.length(), rect);

			canvas.drawText(message, (x - (rect.width() / 2)), (y + (rect.height() / 2)), mTextPaint);
		}

		@Override
		public void onViewCreated (HeaderView headerView) {

		}

		@Override
		public void onReset (HeaderView headerView) {
			// Get rid of the scale
			mScale = 0;

			// Invalidate the view
			invalidate();
		}

		@Override
		public void onPulled (HeaderView headerView, float scale) {
			// Update The Scale
			mScale = scale;

			// Invalidate the view
			invalidate();
		}

		@Override
		public void onRefreshStarted (HeaderView headerView) {
			// Show the ptr_header if needed
			headerView.showIfNecessary(true);

			// Make sure the scale knows
			mScale = 1F;

			// Invalidate the view
			invalidate();
		}
	}

}
