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

package cm.ben.pulltorefresh.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.webkit.WebView;
import android.widget.*;
import cm.ben.pulltorefresh.R;
import com.nineoldandroids.view.animation.AnimatorProxy;

public class PullToRefreshView extends FrameLayout {

	private Attacher mAttacher;

	/**
	 * Constructor
	 *
	 * @param context The Context the view is running in, through which it can
	 *                access the current theme, resources, etc.
	 */
	public PullToRefreshView (Context context) {
		super(context);
		init(context, null, -1);
	}

	/**
	 * Constructor
	 *
	 * @param context The Context the view is running in, through which it can
	 *                access the current theme, resources, etc.
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 * @see #View(Context, AttributeSet, int)
	 */
	public PullToRefreshView (Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, -1);
	}

	/**
	 * Constructor
	 *
	 * @param context  The Context the view is running in, through which it can
	 *                 access the current theme, resources, etc.
	 * @param attrs    The attributes of the XML tag that is inflating the view.
	 * @param defStyle The default style to apply to this view. If 0, no style
	 *                 will be applied (beyond what is included in the theme). This may
	 *                 either be an attribute resource, whose value will be retrieved
	 *                 from the current theme, or an explicit style resource.
	 * @see #View(Context, AttributeSet)
	 */
	public PullToRefreshView (Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init (final Context context, final AttributeSet attrs, final int defStyle) {
		mAttacher = new Attacher((Activity) context);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshView);

		Drawable headerBackgroundDrawable = a.getDrawable(R.styleable.PullToRefreshView_headerBackgroundColor);
		if (headerBackgroundDrawable != null) {
			mAttacher.setHeaderBackgroundDrawable(headerBackgroundDrawable);
		}

		// Swiping Distance
		mAttacher.setRefreshSwipeDistance(a.getFloat(R.styleable.PullToRefreshView_refreshSwipeDistance, 150F));

		// Swiping Cancel Distance
		mAttacher.setRefreshSwipeCancelDistance(a.getFloat(R.styleable.PullToRefreshView_refreshSwipeCancelDistance, 15F));

		// Progress Color
		int color = a.getColor(R.styleable.PullToRefreshView_progressColor, -1);
		if (color != -1) {
			mAttacher.setProgressColor(color);
		}

		String refreshingTextResId = a.getString(R.styleable.PullToRefreshView_headerTextRefreshing);
		if (refreshingTextResId != null) {
			mAttacher.setRefreshingText(refreshingTextResId);
		}


		String draggingTextResId = a.getString(R.styleable.PullToRefreshView_headerTextDragging);
		if (draggingTextResId != null) {
			mAttacher.setDraggingText(draggingTextResId);
		}

		// Header Layout Resource
		int headerLayout = a.getResourceId(R.styleable.PullToRefreshView_headerLayout, R.layout.ptr_header);
		mAttacher.setHeaderView(headerLayout, headerLayout == R.layout.ptr_header ? mAttacher.mDefaultHandler : null);

		a.recycle();
	}

	/**
	 * Get the created {@link Attacher} crated by the view
	 *
	 * @return The generated {@link Attacher}
	 */
	public Attacher getAttacher () {
		return mAttacher;
	}


	public static class Attacher {

		// Comp. packages
		private static final String[] PACKAGES = {"com.actionbarsherlock", "android"};

		private Activity mActivity;
		private DisplayMetrics mDisplayMetrics;
		private View mProgressContainer;
		private ProgressBar mRefreshingProgress;
		private ProgressBar mSwipeProgress;
		private HeaderView mHeaderContainer;
		private View mHeaderView = null;

		private int mHeaderLayoutResId = -1;

		private int mRefreshingTextResId = -1;
		private int mDraggingTextResId = -1;
		private CharSequence mRefreshingText;
		private CharSequence mDraggingText;

		// Our current
		private boolean mIsEnabled = true;
		private boolean mIsDragging = false;
		private boolean mIsRefreshing = false;

		private float mRefreshSwipeDistance = -1F;
		private float mRefreshSwipeCancelDistance = -1F;

		private int mProgressColor = -1;
		private Drawable mHeaderBackgroundDrawable;

		// Interfaces
		private ViewHandler mViewHandler;
		private OnTouchListener mOnTouchListener;
		private HeaderViewHandler mHeaderViewHandler;
		private OnPullToRefreshListener mOnPullToRefreshListener;

		/**
		 * Constructor
		 *
		 * @param activity Current activity
		 */
		public Attacher (Activity activity) {
			// Activity
			mActivity = activity;

			// Display Metrics
			mDisplayMetrics = mActivity.getResources().getDisplayMetrics();
		}

		/**
		 * Constructor
		 *
		 * @param activity Current activity
		 * @param resId    The identified of the layout that will be attached
		 */
		public Attacher (Activity activity, int resId) {
			attach(activity, resId);
		}

		/**
		 * Constructor
		 *
		 * @param view The view that will be attached
		 */
		public Attacher (View view) {
			attach(view);
		}

		/**
		 * Attaches the provided layout identifier
		 *
		 * @param context Current context
		 * @param resId   The identified of the layout that will be attached
		 * @return The current {@link Attacher} instance
		 */
		public Attacher attach (Context context, int resId) {
			if (context == null) {
				throw new IllegalArgumentException("The provided Activity is null!");
			} else {
				attach(((Activity) context).findViewById(resId));
				return this;
			}
		}

		/**
		 * Attaches the provided view and binds it with your own {@link cm.ben.pulltorefresh.widget.PullToRefreshView.ViewHandler}
		 *
		 * @param view        The view that should be attached
		 * @param viewHandler The {@link ViewHandler} that decides when the user can start pulling to refresh
		 * @return The current {@link Attacher} instance
		 */
		public Attacher attach (View view, ViewHandler viewHandler) {
			init(view, viewHandler);
			return this;
		}

		/**
		 * Attaches the provided view and binds it with the appropriate {@link cm.ben.pulltorefresh.widget.PullToRefreshView.ViewHandler}
		 *
		 * @param view The view that should be attached
		 * @return The current {@link Attacher} instance
		 */
		public <V extends View> Attacher attach (V view) {
			if (view instanceof AbsListView) {
				init(view, new ViewHandler<AbsListView>() {
					@Override
					public boolean isScrolledToTop (AbsListView absListView) {
						// Check if top or first row is at the top
						return (absListView.getChildCount() == 0 || absListView.getFirstVisiblePosition() == 0 && absListView.getScrollY() <= 0);
					}
				});
			} else if (view instanceof ScrollView) {
				init(view, new ViewHandler<ScrollView>() {
					@Override
					public boolean isScrolledToTop (ScrollView scrollView) {
						// See if scrolled to the top
						return (scrollView.getScrollY() <= 0);
					}
				});
			} else if (view instanceof WebView) {
				init(view, new ViewHandler<WebView>() {
					@Override
					public boolean isScrolledToTop (WebView webView) {
						// See if scrolled to the top
						return (webView.getScrollY() <= 0);
					}
				});
			} else if (view instanceof HorizontalScrollView) {
				init(view, new ViewHandler<HorizontalScrollView>() {
					@Override
					public boolean isScrolledToTop (HorizontalScrollView horizontalScrollView) {
						return (horizontalScrollView.getScrollX() <= 0);
					}
				});
			} else {
				init(view, new ViewHandler() {
					@Override
					public boolean isScrolledToTop (View view) {
						// Just return true
						// Use setEnabled to enable/disable PullToRefresh
						return true;
					}
				});
			}

			return this;
		}

		private void init (final View view, final ViewHandler viewHandler) {
			if (view == null) {
				// Can't have a null view!
				throw new IllegalArgumentException("The provided View is null!");
			}

			if (viewHandler == null) {
				// Can't have a null view handler
				throw new IllegalArgumentException("The provided ViewHandler is null!");
			}

			// Activity
			mActivity = (Activity) view.getContext();

			// Display netrics
			mDisplayMetrics = view.getResources().getDisplayMetrics();

			// Set the handler
			mViewHandler = viewHandler;

			// Progress Container
			mProgressContainer = LayoutInflater.from(mActivity).inflate(R.layout.ptr_sync, null, false);
			mRefreshingProgress = (ProgressBar) mProgressContainer.findViewById(R.id.progress_refresh);
			mSwipeProgress = (ProgressBar) mProgressContainer.findViewById(R.id.progress_swipe);

			// Change the colors
			setProgressColor(mProgressColor != -1 ? mProgressColor : Color.parseColor("#0099cc"));

			// Add It In
			ViewGroup content = (ViewGroup) mActivity.getWindow().findViewById(android.R.id.content);
			content.addView(mProgressContainer);

			// Header Container
			mHeaderContainer = new HeaderView(mActivity);

			if (mHeaderLayoutResId != -1) {
				// Apply the ptr_header view using the resource id
				mHeaderContainer.setHeaderView(mHeaderLayoutResId);
			} else if (mHeaderView != null) {
				// Apply the ptr_header view using the created view
				mHeaderContainer.setHeaderView(mHeaderView);
			} else {
				// Use the default internal view
				mHeaderContainer.setHeaderView(R.layout.ptr_header);
				mHeaderViewHandler = mDefaultHandler;
			}

			// Set the header background
			mHeaderContainer.setBackgroundDrawable(mHeaderBackgroundDrawable != null ? mHeaderBackgroundDrawable : getActionBarBackground(PACKAGES));

			// Phone home
			mInternalHeaderViewHandler.onViewCreated(mHeaderContainer);

			if (mRefreshSwipeDistance == -1F) {
				// Set the swipe distance if needed
				setRefreshSwipeDistance(150F);
			}

			if (mRefreshSwipeCancelDistance == -1F) {
				// Set the swipe cancel distance if needed
				setRefreshSwipeCancelDistance(15F);
			}

			// Add It In!
			view.post(new Runnable() {
				@Override
				public void run () {
					int size = getActionBarDimen(PACKAGES);

					Rect rect = new Rect();
					mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

					WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size, 1000, 8, -3);
					params.gravity = Gravity.TOP;
					params.y = rect.top;
					params.x = 0;

					// Add it in
					mActivity.getWindowManager().addView(mHeaderContainer, params);

					// Set The Touch Listener
					view.setOnTouchListener(mInternalOnTouchListener);

					// View.addOnAttachStateChangeListener was not added until HONEYCOMB_MR1
					// Removing the header view can automatically handled here
					// destroy must be called on pre HONEYCOMB_MR1 devices
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
						mActivity.getWindow().getDecorView().addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
							@Override
							public void onViewAttachedToWindow (View v) {
							}

							@Override
							public void onViewDetachedFromWindow (View v) {
								destroy();
							}
						});
					}
				}
			});
		}

		/**
		 * A hackish way to allow ActionBar compatibility libraries without having to include them as a dependency.
		 *
		 * @param packageList List of packages to look at. Example: com.actionbarsherlock and android
		 * @return the dimension pixel size of the ActionBar or 0 if one doesn't exist.
		 */
		private int getActionBarDimen (final String[] packageList) {
			for (String pkg : packageList) {
				TypedArray a = null;
				try {
					a = mActivity.getTheme().obtainStyledAttributes(new int[]{Class.forName(pkg + ".R$attr").getField("actionBarSize").getInt(null)});
					if (a != null) {
						final int size = a.getDimensionPixelSize(0, 0);
						if (size != 0) {
							// Found our size
							return size;
						}
					}
				} catch (Exception e) {
				} finally {
					if (a != null) {
						a.recycle();
						a = null;
					}
				}
			}

			return 0;
		}

		/**
		 * A hackish way to allow ActionBar compatibility libraries without having to include them as a dependency.
		 *
		 * @param packageList List of packages to look at. Example: com.actionbarsherlock and android
		 * @return the background drawable of the ActionBar or null if one doesn't exist.
		 */
		private Drawable getActionBarBackground (final String[] packageList) {
			for (String pkg : packageList) {
				TypedArray a = null;
				try {
					final Class clss = Class.forName(pkg + ".R$attr");
					TypedValue typedValue = new TypedValue();
					mActivity.getTheme().resolveAttribute(clss.getField("actionBarStyle").getInt(null), typedValue, true);
					a = mActivity.getTheme().obtainStyledAttributes(typedValue.resourceId, new int[]{clss.getField("background").getInt(null)});
					if (a != null) {
						Drawable d = a.getDrawable(0);
						if (d != null) {
							// Found our drawable
							return d;
						}
					}
				} catch (Exception e) {
				} finally {
					if (a != null) {
						a.recycle();
						a = null;
					}
				}
			}

			return null;
		}


		/**
		 * Internal {@link OnTouchListener}
		 */
		private final OnTouchListener mInternalOnTouchListener = new OnTouchListener() {

			private Interpolator mInterpolator = new AccelerateInterpolator(1.5F);
			private float mDy;
			private float mLastY;
			private float mMaxDy;

			@Override
			public boolean onTouch (View v, MotionEvent event) {

				final float dY = event.getY();

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: {
						if (mViewHandler.isScrolledToTop(v) && !mIsRefreshing && mIsEnabled) {
							mLastY = dY;
							mMaxDy = 0;
							mDy = 0;
							mIsDragging = true;
							mIsRefreshing = false;
						}
						break;
					}

					case MotionEvent.ACTION_MOVE: {
						if (mIsDragging && !mIsRefreshing) {

							// Find the current distance
							mDy += (dY - mLastY) / mDisplayMetrics.density;

							// So we can add a small buffer so we don't accidentally trigger it
							final boolean isTriggered = (mDy > 10F);

							// Check if we are moving forward
							final boolean isMovingForward = (dY > mLastY);

							mLastY = dY;

							// We only want to register the max distance if we are moving forward
							if (isMovingForward)
								mMaxDy = mDy;

							if (isTriggered) {

								// Current Scale
								final float scale = mInterpolator.getInterpolation(mDy / mRefreshSwipeDistance);

								if (isMovingForward) {
									// Moving Forward!
									// Check if the refreshing state has been hit
									if (mDy >= mRefreshSwipeDistance) {
										// Refresh trigger hit
										mIsRefreshing = true;
										mIsDragging = false;
										mInternalHeaderViewHandler.onRefreshStarted(mHeaderContainer);
										break;
									} else {
										// User is dragging
										mIsRefreshing = false;
										mInternalHeaderViewHandler.onPulled(mHeaderContainer, scale);
										break;
									}
								} else {
									// Moving Back!
									if ((mMaxDy - mDy) / mDisplayMetrics.density >= mRefreshSwipeCancelDistance) {
										// Our back tolerance was hit
										// Don't break and let ACTION_CANCEL handle this
										// Exiting here
									} else {
										// Moving Backwards
										// Still valid until cancel distance is it
										mInternalHeaderViewHandler.onPulled(mHeaderContainer, scale);
										break;
									}
								}
							} else {
								break;
							}
						}
					}

					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP: {
						if (mIsDragging || !mIsEnabled) {
							if (mDy != 0 && mLastY != 0 && mMaxDy != 0) {
								// Disabled and we are dragging
								if (mDy < mRefreshSwipeDistance) {
									// Phone home!
									mInternalHeaderViewHandler.onReset(mHeaderContainer);
								}

								mDy = 0;
								mMaxDy = 0;
								mLastY = 0;
								mIsDragging = false;
							}
						}
						break;
					}
				}

				// Return false or the parent OnTouchListener
				return (mOnTouchListener != null && mOnTouchListener.onTouch(v, event));
			}
		};

		/**
		 * Sets the current resource id and {@link HeaderViewHandler}
		 *
		 * @param resId             The identified of the layout that will be attached
		 * @param headerViewHandler The {@link HeaderViewHandler} that controls the provided view
		 */

		public void setHeaderView (int resId, HeaderViewHandler headerViewHandler) {
			mHeaderLayoutResId = resId;
			mHeaderView = null;
			mHeaderViewHandler = headerViewHandler;

			if (mHeaderContainer != null) {
				// Add the ptr_header view
				mHeaderContainer.setHeaderView(resId);

				// Register with onViewCreated
				mInternalHeaderViewHandler.onViewCreated(mHeaderContainer);
			}
		}

		/**
		 * Sets the current ptr_header view and {@link HeaderViewHandler}
		 *
		 * @param view              View that will be added into the ptr_header view
		 * @param headerViewHandler The {@link HeaderViewHandler} that controls the provided view
		 */
		public void setHeaderView (View view, HeaderViewHandler headerViewHandler) {
			mHeaderLayoutResId = -1;
			mHeaderView = view;
			mHeaderViewHandler = headerViewHandler;

			if (mHeaderContainer != null) {
				// Add the ptr_header view
				mHeaderContainer.setHeaderView(view);

				// Register with onViewCreated
				mInternalHeaderViewHandler.onViewCreated(mHeaderContainer);
			}
		}

		/**
		 * Get the current {@link HeaderView}
		 *
		 * @return The {@link HeaderView} being used
		 */
		public HeaderView getHeaderView () {
			return mHeaderContainer;
		}

		/**
		 * Set the background color for the {@link HeaderView}
		 *
		 * @param color The color of the background
		 */
		public void setHeaderBackgroundColor (int color) {
			setHeaderBackgroundDrawable(new ColorDrawable(color));
		}

		/**
		 * Set the background drawable for the {@link HeaderView}
		 *
		 * @param drawable The {@link Drawable} that will be used as the background
		 */
		public void setHeaderBackgroundDrawable (Drawable drawable) {
			mHeaderBackgroundDrawable = drawable;
			if (mHeaderContainer != null) {
				// Set the ptr_header drawable
				mHeaderContainer.setBackgroundDrawable(mHeaderBackgroundDrawable);
			}
		}

		/**
		 * Deconstructs the current {@link Attacher}. Note, this really
		 * only needs to be called on devices with SDK's below {@link Build.VERSION_CODES.HONEYCOMB_MR1} (
		 */
		public void destroy () {
			if (mHeaderContainer != null && mActivity != null) {
				// Disable just in case
				mIsEnabled = false;

				// Destroy the view handlers
				mHeaderViewHandler = null;
				mViewHandler = null;

				// Trash the header view
				mActivity.getWindowManager().removeView(mHeaderContainer);
				mHeaderContainer = null;

				// Trash the progress view
				ViewGroup content = (ViewGroup) mActivity.getWindow().findViewById(android.R.id.content);
				content.removeView(mProgressContainer);
				mProgressContainer = null;
				mSwipeProgress = null;
				mRefreshingProgress = null;
			}
		}

		/**
		 * Get the current {@link HeaderView} background drawable.
		 *
		 * @return Returns the current {@link Drawable} being used by the {@link HeaderView}
		 */
		public Drawable getHeaderBackgroundDrawable () {
			return mHeaderBackgroundDrawable;
		}

		/**
		 * Set the color of the dragging and refreshing progress bars
		 *
		 * @param color The color of the progress bars
		 */
		public void setProgressColor (int color) {
			mProgressColor = color;

			if (mSwipeProgress != null) {
				// Change the background
				mSwipeProgress.getProgressDrawable().setColorFilter(mProgressColor, PorterDuff.Mode.SRC_IN);
			}

			if (mRefreshingProgress != null) {
				// Change the background
				mRefreshingProgress.getIndeterminateDrawable().setColorFilter(mProgressColor, PorterDuff.Mode.SRC_IN);
			}
		}

		/**
		 * Returns the current progress bar color
		 *
		 * @return The color used by the progress bars
		 */
		public int getProgressColor () {
			return mProgressColor;
		}

		/**
		 * Set the distance the user must drag in order to trigger a refresh
		 *
		 * @param distance Distance required to refresh
		 */
		public void setRefreshSwipeDistance (float distance) {
			mRefreshSwipeDistance = Math.max(Math.min(mDisplayMetrics.heightPixels / mDisplayMetrics.density / 2.5F, 300F), distance);
		}

		/**
		 * Set the distance you can scroll backwards without exiting the exiting pull to refresh
		 *
		 * @param distance Distance one can scroll backwards before an exit is triggered
		 */
		public void setRefreshSwipeCancelDistance (float distance) {
			mRefreshSwipeCancelDistance = distance;
		}

		/**
		 * Registers a callback to be invoked when a refresh has been triggered.
		 *
		 * @param onPullToRefreshListener The callback that will be used
		 */
		public void setOnPullToRefreshListener (OnPullToRefreshListener onPullToRefreshListener) {
			mOnPullToRefreshListener = onPullToRefreshListener;
		}

		/**
		 * End the refresh and calls {@link HeaderViewHandler#onReset(cm.ben.pulltorefresh.widget.PullToRefreshView.HeaderView)}
		 */
		public void setRefreshComplete () {
			// Update the current states
			mIsRefreshing = false;

			// Dismiss the header
			if (mHeaderContainer != null)
				mHeaderContainer.dismissIfNecessary(true);

			// Update the views
			if (mProgressContainer != null)
				mProgressContainer.setVisibility(GONE);

			if (mRefreshingProgress != null)
				mRefreshingProgress.setVisibility(GONE);

			if (mSwipeProgress != null) {
				mSwipeProgress.setVisibility(GONE);
				// Set the scale
				AnimatorProxy.wrap(mSwipeProgress).setScaleX(0F);
			}

			if (mHeaderViewHandler != null) {
				// Pass the message along
				mHeaderViewHandler.onReset(mHeaderContainer);
			}
		}

		/**
		 * Starts the refresh and calls {@link HeaderViewHandler#onRefreshStarted(cm.ben.pulltorefresh.widget.PullToRefreshView.HeaderView)}
		 */
		public void setRefreshing () {
			// Update the current states
			mIsRefreshing = true;

			// Dismiss the header
			mHeaderContainer.showIfNecessary(true);

			// Update the views
			if (mProgressContainer != null)
				mProgressContainer.setVisibility(VISIBLE);

			if (mRefreshingProgress != null)
				mRefreshingProgress.setVisibility(VISIBLE);

			if (mSwipeProgress != null) {
				mSwipeProgress.setVisibility(GONE);
				// Set the scale
				AnimatorProxy.wrap(mSwipeProgress).setScaleX(0F);
			}

			if (mHeaderViewHandler != null) {
				// Pass the message along
				mHeaderViewHandler.onRefreshStarted(mHeaderContainer);
			}
		}

		/**
		 * Starts the dragging state and calls {@link HeaderViewHandler#onPulled(cm.ben.pulltorefresh.widget.PullToRefreshView.HeaderView, float)}
		 */
		public void setDragging () {
			setDragging(0F);
		}

		/**
		 * Starts the dragging state and calls {@link HeaderViewHandler#onPulled(cm.ben.pulltorefresh.widget.PullToRefreshView.HeaderView, float)}
		 *
		 * @param scale - value between 0f and 1f that defines how far the user has pulled down to refresh
		 */
		public void setDragging (float scale) {
			// Update the current states
			mIsRefreshing = false;

			// Dismiss the header
			if (mHeaderContainer != null)
				mHeaderContainer.showIfNecessary(true);

			// Update the views
			if (mProgressContainer != null)
				mProgressContainer.setVisibility(VISIBLE);

			if (mRefreshingProgress != null)
				mRefreshingProgress.setVisibility(GONE);

			if (mSwipeProgress != null) {
				mSwipeProgress.setVisibility(VISIBLE);
				// Set the scale
				AnimatorProxy.wrap(mSwipeProgress).setScaleX(scale);
			}

			if (mHeaderViewHandler != null) {
				if (mHeaderContainer != null) {
					// Pass the message along
					mHeaderViewHandler.onPulled(mHeaderContainer, scale);
				}
			}
		}

		/**
		 * Enables pull to refresh
		 */
		public void setEnabled () {
			setEnabled(true);
		}

		/**
		 * Disables pulling to refresh
		 */
		public void setDisabled () {
			setEnabled(false);
		}

		/**
		 * Enables or disabled pulling to refresh
		 *
		 * @param enabled True if the library is enabled, false otherwise.
		 */
		public void setEnabled (boolean enabled) {
			mIsEnabled = enabled;
			if (!enabled) {
				mIsRefreshing = false;
				mIsDragging = false;
			}
		}

		/**
		 * Gets the current enabled/disabled state
		 *
		 * @return Returns True if enabled, otherwise False
		 */
		public boolean isEnabled () {
			return mIsEnabled;
		}

		/**
		 * Get the current refreshing state
		 *
		 * @return Returns true if the view is in refreshing mode, otherwise false
		 */
		public boolean isRefreshing () {
			return mIsRefreshing;
		}

		/**
		 * Get the current dragging state
		 *
		 * @return Returns true if the user is dragging, otherwise false
		 */
		public boolean isDragging () {
			return mIsDragging;
		}

		/**
		 * Get the current state of the {@link HeaderView}
		 *
		 * @return True if {@link cm.ben.pulltorefresh.R.layout.header} is being used, otherwise false
		 */
		public boolean isUsingInternalHeaderView () {
			return (mHeaderViewHandler == mDefaultHandler);
		}


		/**
		 * Register a callback to be invoked by the internal {@link OnTouchListener}
		 *
		 * @param onTouchListener The touch listener to be attached
		 */
		public void setOnTouchListener (OnTouchListener onTouchListener) {
			mOnTouchListener = onTouchListener;
		}

		/**
		 * Sets the header text with the provided string resource identifier
		 *
		 * @param resId The resource identifier of the string to be used while dragging
		 */
		public void setDraggingText (int resId) {
			mDraggingText = null;
			mDraggingTextResId = resId;
		}

		/**
		 * Sets the refreshing header text with the provided string value
		 *
		 * @param text The text to be used while dragging
		 */
		public void setDraggingText (CharSequence text) {
			mDraggingText = text;
			mDraggingTextResId = -1;
		}

		/**
		 * Sets the refreshing header text with the provided string resource identifier
		 *
		 * @param resId The resource identifier of the string to be used while refreshing
		 */
		public void setRefreshingText (int resId) {
			mRefreshingText = null;
			mRefreshingTextResId = resId;
		}


		/**
		 * Sets the refreshing text
		 *
		 * @param text The text to be used while refreshing
		 */
		public void setRefreshingText (CharSequence text) {
			mRefreshingText = text;
			mRefreshingTextResId = -1;
		}

		/**
		 * Gets the header {@link TextView}
		 *
		 * @return The current {@link TextView} being used by the {@link HeaderView}
		 */
		public TextView getHeaderTextView () {
			return mDefaultHandler.mText;
		}

		/**
		 * Internal {@link HeaderViewHandler}
		 */
		private final HeaderViewHandler mInternalHeaderViewHandler = new HeaderViewHandler() {

			@Override
			public void onViewCreated (HeaderView headerView) {
				// Dismiss The Header
				mHeaderContainer.dismiss(false);

				if (mHeaderViewHandler != null) {
					// Pass the message along
					mHeaderViewHandler.onViewCreated(headerView);
				}
			}

			@Override
			public void onReset (HeaderView headerView) {
				// Dismiss The Header
				Attacher.this.setRefreshComplete();
			}

			@Override
			public void onPulled (HeaderView headerView, float scale) {
				// Show The Header
				Attacher.this.setDragging(scale);
			}

			@Override
			public void onRefreshStarted (HeaderView headerView) {
				// Start Refreshing
				Attacher.this.setRefreshing();

				if (mOnPullToRefreshListener != null) {
					// Pass the message along
					mOnPullToRefreshListener.onRefresh();
				}
			}
		};


		/**
		 * Default {@link HeaderViewHandler} used when {@link cm.ben.pulltorefresh.R.layout.header} has been provided
		 */
		private final DefaultHandler mDefaultHandler = new DefaultHandler();

		private class DefaultHandler implements HeaderViewHandler {

			private TextView mText;

			@Override
			public void onViewCreated (HeaderView headerView) {
				mText = (TextView) headerView.findViewById(R.id.message);
			}

			@Override
			public void onReset (HeaderView headerView) {

			}

			@Override
			public void onPulled (HeaderView headerView, float scale) {
				if (mDraggingTextResId != -1) {
					mText.setText(mDraggingTextResId);
				} else if (mDraggingText != null) {
					mText.setText(mDraggingText);
				} else {
					mText.setText(R.string.pull_to_refresh_dragging);
				}
			}

			@Override
			public void onRefreshStarted (HeaderView headerView) {
				if (mRefreshingTextResId != -1) {
					mText.setText(mRefreshingTextResId);
				} else if (mRefreshingText != null) {
					mText.setText(mRefreshingText);
				} else {
					// Use the internal text
					mText.setText(R.string.pull_to_refresh_refreshing);
				}
			}
		}
	}

	public static class HeaderView extends LinearLayout {

		private View mView;
		private final Interpolator mInterpolatorOut = new AccelerateInterpolator(1.5F);
		private final Interpolator mInterpolatorIn = new DecelerateInterpolator(1.5F);

		public HeaderView (Context context) {
			super(context);

		}

		/**
		 * Returns the visibility of the view
		 *
		 * @return Returns True if the view is showing, false otherwise
		 */
		public boolean isShowing () {
			return (getVisibility() == View.VISIBLE);
		}

		/**
		 * Returns the visibility of the view
		 *
		 * @return Returns True if the view is hidden, false otherwise
		 */
		public boolean isHidden () {
			return (getVisibility() == View.GONE);
		}

		/**
		 * Dismisses the current view and optionally animates it
		 *
		 * @param animate True if the view should be animated, False otherwise
		 */
		public void dismiss (boolean animate) {
			if (mView == null)
				return;

			final long animTime = animate ? 200L : 0L;

			com.nineoldandroids.view.ViewPropertyAnimator.animate(mView).y(-mView.getHeight()).setInterpolator(mInterpolatorOut).setDuration(animTime).start();

			com.nineoldandroids.view.ViewPropertyAnimator.animate(this).alpha(0.0F).setDuration(animTime).start();

			postDelayed(new Runnable() {
				@Override
				public void run () {
					setVisibility(GONE);
				}
			}, animTime);
		}

		/**
		 * Dismisses the {@link HeaderView} only if the view is showing.
		 *
		 * @param animate True if the view should be animated, False otherwise
		 */
		public void dismissIfNecessary (boolean animate) {
			if (isShowing()) {
				dismiss(animate);
			}
		}

		/**
		 * Shows the current view and optionally animates it
		 *
		 * @param animate True if the view should be animated, False s
		 */
		public void show (boolean animate) {
			if (mView == null)
				return;

			final long animTime = animate ? 200L : 0L;

			setVisibility(VISIBLE);
			com.nineoldandroids.view.ViewPropertyAnimator.animate(this).alpha(1.0F).start();

			com.nineoldandroids.view.ViewPropertyAnimator.animate(mView).y(0.0F).setInterpolator(mInterpolatorIn).setDuration(animTime).start();
		}

		/**
		 * Shows the {@link HeaderView} only if the view is dismissed.
		 *
		 * @param animate True if the view should be animated, False otherwise
		 */
		public void showIfNecessary (boolean animate) {
			if (isHidden()) {
				show(animate);
			}
		}

		/**
		 * Sets the current resource id of the ptr_header view
		 *
		 * @param resId The identified of the layout that will be attached
		 */
		public void setHeaderView (int resId) {
			setHeaderView(LayoutInflater.from(getContext()).inflate(resId, this, false));
		}

		/**
		 * Sets the current ptr_header view
		 *
		 * @param view View that will be added into the ptr_header view
		 */
		public void setHeaderView (View view) {
			mView = view;
			super.removeAllViews();
			super.addView(mView);
		}

		/**
		 * Get the current view attached to the ptr_header view
		 *
		 * @return The currently attached view
		 */
		public View getHeaderView () {
			return mView;
		}
	}


	public static interface HeaderViewHandler {

		/**
		 * Called immediately after the view is inflated.
		 * This gives any custom ptr_header implementation a chance to initialize themselves
		 * once their view has been inflated.
		 *
		 * @param headerView - wrapper around the user provided view
		 */
		void onViewCreated (HeaderView headerView);

		/**
		 * Called when the view should be reset.
		 *
		 * @param headerView - wrapper around the user provided view
		 */
		void onReset (HeaderView headerView);

		/**
		 * Called when the user is pulling down to refresh.
		 *
		 * @param headerView - wrapper around the user provided view
		 * @param scale      - value between 0f and 1f that defines how far the user has pulled down to refresh
		 */
		void onPulled (HeaderView headerView, float scale);

		/**
		 * Called when the refresh distance has been hit
		 *
		 * @param headerView - wrapper around the user provided view
		 */
		void onRefreshStarted (HeaderView headerView);
	}


	public static interface OnPullToRefreshListener {

		/**
		 * Called when the refresh distance has been hit.
		 */
		void onRefresh ();
	}

	public static interface ViewHandler<V extends View> {

		/**
		 * Used to decide when a view is currently scrolled to the top.
		 *
		 * @param view view that is being accessed
		 * @return true is the view is scrolled to the top
		 */
		boolean isScrolledToTop (V view);
	}

}