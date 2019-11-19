package com.nadisoft.shopping.organiser;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nadisoft.shopping.organiser.entities.ShoppingItem;
import com.nadisoft.shopping.organiser.provider.ShoppingContract;

public class ShoppingOrganiserActivity extends SherlockListActivity implements ActionBar.OnNavigationListener, ViewBinder {
    //private TextView mSelected;
	SimpleCursorAdapter navListAdapter;
	boolean currentListSetsFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        currentListSetsFilter = true;
        setUpActionBar();
        setUpList();
    }

    private void setUpList() {
        long listId = getListId();
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, getSelection(), null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter itemsAdapter = new SimpleCursorAdapter(getSupportActionBar().getThemedContext(),
				R.layout.checked_item, cursor,
				new String[] { ShoppingContract.Items.ITEM_NAME },
				new int[] { R.id.itemText });
		itemsAdapter.setViewBinder(this);
		setListAdapter(itemsAdapter);
	}

	private String getSelection() {
		if ( currentListSetsFilter )
			return null;
		else
			return ShoppingContract.Items.ITEM_NEEDED + "=1";
	}

	private void changeList(long listId){
    	SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
    	currentListSetsFilter = listSetsFilter(listId);
    	@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, getSelection(), null, null);
    	adapter.changeCursor(cursor);
    	adapter.notifyDataSetChanged();
    }

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		switch (view.getId()) {
			case R.id.itemText:
				ShoppingItem item = getShoppingItem(cursor);
				if ( currentListSetsFilter ){
					getListView().setItemChecked(cursor.getPosition(), item.isNeeded());
				} else {
					getListView().setItemChecked(cursor.getPosition(), item.isBought());
				}
				break;
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		long itemId = getListAdapter().getItemId(position);
		boolean checked = getListView().isItemChecked(position);
		editItem(itemId,currentListSetsFilter,checked);
	}

	private void setUpActionBar() {
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Lists.buildListsUri(), 
				null, null, null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter navListAdapter = new SimpleCursorAdapter(getSupportActionBar().getThemedContext(),
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
		case R.id.menu_item_restart:
			restartShopping();
			break;
		case R.id.menu_item_help:
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

	private boolean listSetsFilter(long listId) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = ShoppingContract.Lists.buildListUri(listId);
		Cursor cursor = contentResolver.query(uri, new String[]{ShoppingContract.Lists.LIST_SETS_FILTER}, null, null, null);
		cursor.moveToFirst();
		return cursor.getInt(0) == 1;
	}

	private ShoppingItem getShoppingItem(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(ShoppingContract.Items._ID)); //OJO
		String name = cursor.getString(cursor.getColumnIndex(ShoppingContract.Items.ITEM_NAME));
		boolean needed = cursor.getInt(cursor.getColumnIndex(ShoppingContract.Items.ITEM_NEEDED)) == 1;
		boolean bought = cursor.getInt(cursor.getColumnIndex(ShoppingContract.Items.ITEM_BOUGHT)) == 1;

		ShoppingItem shoppingItem = new ShoppingItem(name);
		shoppingItem.setId(id);
		shoppingItem.setNeeded(needed);
		shoppingItem.setBought(bought);
		return shoppingItem;
	}

	private void editItem(long id, boolean setsNeeded, boolean checked)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Items.buildItemUri(id);
		ContentValues values = new ContentValues();
		if ( setsNeeded ) {
			values.put(ShoppingContract.Items.ITEM_NEEDED, checked);
		} else {
			values.put(ShoppingContract.Items.ITEM_BOUGHT, checked);
		}
		contentResolver.update(url, values, null, null);
	}

	private void restartShopping() {
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Items.buildItemsUri();
		ContentValues values = new ContentValues();
		values.put(ShoppingContract.Items.ITEM_NEEDED, false);
		values.put(ShoppingContract.Items.ITEM_BOUGHT, false);
		contentResolver.update(url, values, null, null);
	}

}
