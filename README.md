PullToRefresh
=================

This project aims to provide a simple yet customizable pull to refresh implementation as seen in the latest Gmail app by Google.

![PullToRefresh](https://raw.github.com/bencodes/PullToRefresh/master/downloads/screenshots/demo-01.png)
![PullToRefresh](https://raw.github.com/bencodes/PullToRefresh/master/downloads/screenshots/demo-02.png)
![PullToRefresh](https://raw.github.com/bencodes/PullToRefresh/master/downloads/screenshots/demo-03.png)


![PullToRefresh](https://raw.github.com/bencodes/PullToRefresh/master/downloads/screenshots/demo-04.png)
![PullToRefresh](https://raw.github.com/bencodes/PullToRefresh/master/downloads/screenshots/demo-05.png)
![PullToRefresh](https://raw.github.com/bencodes/PullToRefresh/master/downloads/screenshots/demo-06.png)

Supported Views
============
* AbsListView
* ScrollView
* WebView
* HorizontalScrollView

You can also support your own views by providing a custom `ViewHandler`.

```
public class MyActivity extends ListActivity {

	private PullToRefreshView.Attacher mAttacher;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAttacher = new PullToRefreshView.Attacher(this);
		mAttacher.attach(getListView(), new PullToRefreshView.ViewHandler<ListView>() {
			@Override
			public boolean isScrolledToTop (ListView listView) {
				// Decide if my view is scrolled up
				return false;
			}
		});
	}
}
```

Usage
============

Using `PullToRefreshView.Attacher`


```
public class MyActivity extends Activity implements OnPullToRefreshListener {

	private ScrollView mScrollView;
	private PullToRefreshView mPullToRefreshView;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.my_view);

		// Find The ScrolView
		mScrollView = (ScrollView) findViewById(R.id.scrollview);

		// Attach It
		mAttacher = new PullToRefreshView.Attacher(mScrollView);
		
		// Set the pull to refresh listener
		mAttacher.setOnPullToRefreshListener(this);
	}
}
```

Using `PullToRefreshView`


```
public class MyActivity extends Activity implements OnPullToRefreshListener {

	private Attacher mAttacher;
	private ScrollView mScrollView;
	private PullToRefreshView mPullToRefreshView;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.my_pull_to_refresh_view);
		
		// Find out pull to refresh view
		mPullToRefreshView = (PullToRefreshView) super.findViewById(R.id.ptr);
		
		// Option 1: Attach our view with the resource id
		mAttacher = mPullToRefreshView.getAttacher().attach(this, R.id.view);
		
		// Option 2: Attach with our view
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		mAttacher = mPullToRefreshView.getAttacher().attach(mScrollView);
		
		// Set the pull to refresh listener
		mAttacher.setOnPullToRefreshListener(this);
	}
}
```

Dependencies
============

* [NineOldAndroids](https://github.com/JakeWharton/NineOldAndroids)


Developed By
============

* Ben Lee - <ben@ben.cm>



License
=======

    Copyright 2012 Benjamin Lee

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.