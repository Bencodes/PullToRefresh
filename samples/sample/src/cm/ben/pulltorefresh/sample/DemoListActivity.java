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

package cm.ben.pulltorefresh.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cm.ben.pulltorefresh.sample.demo.*;
import com.actionbarsherlock.app.SherlockListActivity;

public class DemoListActivity extends SherlockListActivity {

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setListAdapter(mAdapter);
	}

	@Override
	protected void onListItemClick (ListView l, View v, int position, long id) {
		startActivity(new Intent(this, ((Item) mAdapter.getItem(position)).clss));
	}

	private final BaseAdapter mAdapter = new BaseAdapter() {

		private final Item[] mData = new Item[]{new Item("Layout Demo", LayoutExample.class), new Item("ListView Demo", ListViewExample.class), new Item("WebView Demo", WebViewExample.class), new Item("ScrollView Demo", ScrollViewExample.class), new Item("Custom View Demo", CustomViewExample.class), new Item("Custom View 2 Demo", CustomViewExample2.class)};

		@Override
		public int getCount () {
			return mData.length;
		}

		@Override
		public Item getItem (int i) {
			return mData[i];
		}

		@Override
		public long getItemId (int i) {
			return i;
		}

		@Override
		public View getView (int i, View view, ViewGroup viewGroup) {
			if (view == null)
				view = LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null);

			final TextView textView = (TextView) view.findViewById(android.R.id.text1);
			textView.setTextColor(Color.BLACK);
			textView.setText(getItem(i).name);
			return view;
		}
	};

	private class Item {
		private Class clss;
		private String name;

		public Item (String name, Class clss) {
			this.name = name;
			this.clss = clss;
		}
	}
}
