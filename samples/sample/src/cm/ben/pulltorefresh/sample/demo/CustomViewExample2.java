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

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import cm.ben.pulltorefresh.sample.R;
import cm.ben.pulltorefresh.widget.PullToRefreshView;
import com.nineoldandroids.view.animation.AnimatorProxy;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static cm.ben.pulltorefresh.widget.PullToRefreshView.HeaderView;

public class CustomViewExample2 extends ListViewExample implements PullToRefreshView.HeaderViewHandler {

	private ImageView mImageView;
	private Animation mAnimation;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAttacher.setHeaderView(R.layout.header_view, this);
		mAttacher.setOnPullToRefreshListener(this);
	}

	@Override
	public void onViewCreated (HeaderView headerView) {
		mImageView = (ImageView) headerView.findViewById(R.id.image);
		mImageView.setVisibility(GONE);
	}

	@Override
	public void onReset (HeaderView headerView) {
		mImageView.setVisibility(GONE);
		mImageView.clearAnimation();
	}

	@Override
	public void onPulled (HeaderView headerView, float scale) {
		mImageView.setVisibility(VISIBLE);
		AnimatorProxy.wrap(mImageView).setRotation(360 * scale);
	}

	@Override
	public void onRefreshStarted (HeaderView headerView) {
		if (mAnimation == null)
			mAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
		mImageView.startAnimation(mAnimation);
	}
}
