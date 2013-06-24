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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cm.ben.pulltorefresh.sample.R;
import cm.ben.pulltorefresh.widget.PullToRefreshView;

public class ListViewExample extends BaseActivity implements PullToRefreshView.OnPullToRefreshListener {

	private static final String[] DATA;

	static {
		DATA = new String("Lorem ipsum dolor sit amet, " +
				"consectetuer adipiscing elit, " +
				"sed diam nonummy nibh euismod tincidunt ut " +
				"laoreet dolore magna aliquam erat volutpat. " +
				"Ut wisi enim ad minim veniam, quis nostrud exerci tation " +
				"ullamcorper suscipit lobortis nisl ut " +
				"aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in " +
				"hendrerit in vulputate velit esse molestie consequat, vel il lum dolore eu " +
				"feugiat nulla facilisis at vero eros et accumsan et " +
				"Eodem modo typi, qui nunc nobis videntur parum clari, " +
				"fiant sollemnes in futurum").replaceAll("\\.", "").replaceAll(",", "").split(" ");
	}

	private ListView mList;

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.content_list_view);
		mList = (ListView) super.findViewById(android.R.id.list);
		mList.setAdapter(mAdapter);

		mAttacher = new PullToRefreshView.Attacher(mList);
		mAttacher.setOnPullToRefreshListener(this);

		if (mAttacher.getHeaderTextView() != null) {
			// Change the text color
			mAttacher.getHeaderTextView().setTextColor(Color.WHITE);
		}
	}

	private final ListAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount () {
			return DATA.length;
		}

		@Override
		public String getItem (int i) {
			final String item = DATA[i];
			return (item.substring(0, 1).toUpperCase() + item.substring(1));
		}

		@Override
		public long getItemId (int i) {
			return i;
		}

		@Override
		public View getView (int i, View view, ViewGroup viewGroup) {
			if (view == null)
				view = LayoutInflater.from(ListViewExample.this).inflate(android.R.layout.simple_list_item_1, null);
			((TextView) view.findViewById(android.R.id.text1)).setText(getItem(i));
			return view;
		}
	};

}
