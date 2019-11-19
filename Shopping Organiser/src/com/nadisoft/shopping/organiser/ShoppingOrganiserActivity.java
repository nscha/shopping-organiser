package com.nadisoft.shopping.organiser;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nadisoft.shopping.organiser.provider.ShoppingOrganiserContract;

public class ShoppingOrganiserActivity extends SherlockActivity implements ActionBar.OnNavigationListener {
    //private TextView mSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Context context = getSupportActionBar().getThemedContext();

		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingOrganiserContract.Lists.buildListsUri(), 
				null, null, null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter navListAdapter = new SimpleCursorAdapter(this,
				R.layout.sherlock_spinner_item, cursor,
				new String[] { ShoppingOrganiserContract.Lists.LIST_NAME },
				new int[] { android.R.id.text1 }){

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				switch (position) {
				case 0:
					((TextView)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ab_spinner_homeicon,0,0,0);
					break;
				default:
					((TextView)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ab_spinner_shopicon,0,0,0);
					break;
				}
				return view;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View view = super.getDropDownView(position, convertView, parent);
				switch (position) {
				case 0:
					((TextView)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ab_spinner_homeicon,0,0,0);
					break;
				default:
					((TextView)view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ab_spinner_shopicon,0,0,0);
					break;
				}
				return view;
			}
		};

        navListAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setListNavigationCallbacks(navListAdapter, this);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        //mSelected.setText("Selected: " + mLocations[itemPosition]);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (R.id.menu_item_editLists == item.getItemId()) {
			Intent intent = new Intent(this, EditListsActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
    
}
