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
import cm.ben.pulltorefresh.sample.R;
import cm.ben.pulltorefresh.widget.PullToRefreshView;

public class LayoutExample extends BaseActivity implements PullToRefreshView.OnPullToRefreshListener {

	private PullToRefreshView mPullToRefreshView;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.content_with_layout_view);

		mPullToRefreshView = (PullToRefreshView) super.findViewById(R.id.ptr);

		mAttacher = mPullToRefreshView.getAttacher().attach(this, R.id.scrollview);
		mAttacher.setOnPullToRefreshListener(this);
	}
}
