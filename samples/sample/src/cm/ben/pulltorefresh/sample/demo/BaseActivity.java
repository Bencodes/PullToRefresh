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

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import cm.ben.pulltorefresh.sample.R;
import cm.ben.pulltorefresh.widget.PullToRefreshView;
import com.actionbarsherlock.app.SherlockActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public abstract class BaseActivity extends SherlockActivity implements View.OnClickListener, PullToRefreshView.OnPullToRefreshListener {

	protected PullToRefreshView.Attacher mAttacher;
	private Button mToggleButton;
	private LinearLayout mPicker;
	private FrameLayout mFrame;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.color_picker_frame);

		mToggleButton = (Button) super.findViewById(R.id.enabled_button);
		mToggleButton.setOnClickListener(this);
		mToggleButton.setTag(new Boolean(true));

		mPicker = (LinearLayout) findViewById(R.id.color_swatches);
		for (String c : getResources().getStringArray(R.array.swatches)) {
			final View item = LayoutInflater.from(this).inflate(R.layout.color_item, mPicker, false);
			final Integer color = Color.parseColor(c);
			item.setBackgroundColor(color);
			item.setTag(color);
			item.setOnClickListener(this);
			mPicker.addView(item);
		}
	}

	@Override
	public void onDestroy () {
		super.onDestroy();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			mAttacher.destroy();
		}
	}

	@Override
	public void setContentView (int resId) {
		setContentView(getLayoutInflater().inflate(resId, mFrame, false));
	}

	@Override
	public void setContentView (View view) {
		if (mFrame == null)
			mFrame = (FrameLayout) findViewById(R.id.frame);
		mFrame.addView(view, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
	}

	@Override
	public void onClick (View view) {
		if (mAttacher != null) {
			if (view == mToggleButton) {
				Boolean enabled = !(Boolean) mToggleButton.getTag();

				// Update the button
				mToggleButton.setTag(enabled);
				mToggleButton.setText(!enabled ? "Enable" : "Disable");

				// Update The Attatcher
				mAttacher.setEnabled(enabled);
			} else {
				Integer color = (Integer) view.getTag();
				mAttacher.setProgressColor(color);
			}
		}
	}

	@Override
	public void onRefresh () {
		Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
		simulateWork(mAttacher);
	}

	public void simulateWork (final PullToRefreshView.Attacher attacher) {
		if (mFrame != null) {
			mFrame.postDelayed(new Runnable() {
				@Override
				public void run () {
					attacher.setRefreshComplete();
				}
			}, 1250);
		}
	}
}
