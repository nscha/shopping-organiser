package com.nadisoft.shopping.organiser;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nadisoft.shopping.organiser.provider.ShoppingContract;

public class ShoppingOrganiserActivity extends SherlockListActivity implements ActionBar.OnNavigationListener {
    //private TextView mSelected;
	SimpleCursorAdapter navListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Context context = getSupportActionBar().getThemedContext();

        setUpActionBar();
        setUpList();
    }

    private void setUpList() {
        long listId = getListId();
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, null, null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter itemsAdapter = new SimpleCursorAdapter(this,
				R.layout.item, cursor,
				new String[] { ShoppingContract.Items.ITEM_NAME },
				new int[] { R.id.itemText });
		setListAdapter(itemsAdapter);
	}

    private void changeList(long listId){
    	SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
    	@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, null, null, null);
    	adapter.changeCursor(cursor);
    }

	private void setUpActionBar() {
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Lists.buildListsUri(), 
				null, null, null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter navListAdapter = new SimpleCursorAdapter(this,
				R.layout.sherlock_spinner_item, cursor,
				new String[] { ShoppingContract.Lists.LIST_NAME },
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
		this.navListAdapter = navListAdapter;

        navListAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setListNavigationCallbacks(navListAdapter, this);
	}

	@Override
    public boolean onNavigationItemSelected(int itemPosition, long listId) {
		changeList(listId);
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
    	Intent intent;
    	switch (item.getItemId()) {
		case R.id.menu_item_edit:
			intent = new Intent(this, EditItemsActivity.class);
			intent.putExtra(EditItemsActivity.EXTRA_LIST_ID, getListId());
			startActivity(intent);
			break;
		case R.id.menu_item_editLists:
			intent = new Intent(this, EditListsActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
    
	private long getListId() {
		int position = getSupportActionBar().getSelectedNavigationIndex();
        return navListAdapter.getItemId(position);
	}
    
}
