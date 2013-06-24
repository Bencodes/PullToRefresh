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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cm.ben.pulltorefresh.sample.R;
import cm.ben.pulltorefresh.widget.PullToRefreshView;

public class WebViewExample extends BaseActivity implements PullToRefreshView.OnPullToRefreshListener {

	private WebView mWebView;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.content_web_view);

		// Find The WebView
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setWebViewClient(mClient);
		mWebView.setVisibility(View.VISIBLE);

		// Attach It
		mAttacher = new PullToRefreshView.Attacher(mWebView);
		mAttacher.setRefreshingText("Loading");
		mAttacher.setHeaderBackgroundColor(Color.BLACK);
		mAttacher.getHeaderTextView().setTextColor(Color.WHITE);
		mAttacher.setOnPullToRefreshListener(this);
		mWebView.loadUrl(getString(R.string.demo_url));
	}

	@Override
	public void onRefresh () {
		mAttacher.setRefreshingText("Refreshing");
		mWebView.setVisibility(View.GONE);
		mWebView.reload();
	}

	private WebViewClient mClient = new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading (WebView webView, String url) {
			webView.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted (WebView view, String url, Bitmap favIcon) {
			mWebView.setVisibility(View.GONE);
			mAttacher.setRefreshing();
		}

		@Override
		public void onPageFinished (WebView view, String url) {
			mWebView.setVisibility(View.VISIBLE);
			mAttacher.setRefreshComplete();
		}
	};

}
