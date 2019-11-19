package com.nadisoft.shopping.organiser;

import java.lang.reflect.Field;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nadisoft.app.HelpDialogBuilder;
import com.nadisoft.app.HelpDialogBuilder.HelpType;
import com.nadisoft.shopping.organiser.entities.ShoppingItem;
import com.nadisoft.shopping.organiser.provider.ShoppingContract;

public class ShoppingOrganiserActivity extends SherlockListActivity implements ActionBar.OnNavigationListener, ViewBinder {
	SimpleCursorAdapter navListAdapter;
	boolean currentListSetsFilter;
	View footer;

	static final int DIALOG_HELP_FIRST_TIME = 0;
	static final int DIALOG_SHOPPING_HELP = 1;

	private static final String TAG = "ShoppingOrganiserActivity";
	private static final String BUNDLE_NAVBAR_INDEX = "bundleActionBarNavIndex";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

    	int actionBarNavigationIndex = 0;
        if ( savedInstanceState != null ){
        	actionBarNavigationIndex = savedInstanceState.getInt(BUNDLE_NAVBAR_INDEX);
        }

        setUpActionBar(actionBarNavigationIndex);
        currentListSetsFilter = listSetsFilter(getListId());
        setUpList();
        showFirstTimeHelp();
        forceMenuOverflow();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_NAVBAR_INDEX, getSupportActionBar().getSelectedNavigationIndex());
	}

	// Horrible hack, that will even stop working with new ActionBarSherlock
	private void forceMenuOverflow() {
		try {
		    ViewConfiguration config = ViewConfiguration.get(this);
		    Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		    if (menuKeyField != null) {
		        menuKeyField.setAccessible(true);
		        menuKeyField.setBoolean(config, false);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_HELP_FIRST_TIME:
            dialog = createHelpFirstTimeDialog();
            break;
        case DIALOG_SHOPPING_HELP:
        	dialog = createShoppingHelpDialog();
        	break;
        default:
            dialog = null;
        }
        return dialog;
    }

	private Dialog createHelpFirstTimeDialog() {
		HelpDialogBuilder builder = new HelpDialogBuilder(this, HelpDialogBuilder.HelpType.FIRST_TIME);
		return builder.create();
	}

	private Dialog createShoppingHelpDialog() {
		HelpDialogBuilder builder = new HelpDialogBuilder(this, HelpDialogBuilder.HelpType.SHOPPING);
		return builder.create();
	}

	private void setUpList() {
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        footer = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shopping_list_item_reminder, null, false);
		Log.v(TAG, "changeList adding footer to get the right adapter");
		lv.addFooterView(footer);
		SimpleCursorAdapter itemsAdapter = createListItemsAdapter(getListId());
		itemsAdapter.setViewBinder(this);
		setListAdapter(itemsAdapter);
	}

	@SuppressWarnings("deprecation")
	private SimpleCursorAdapter createListItemsAdapter(long listId) {
		Cursor cursor = getListItemsCursor(listId);
		return new SimpleCursorAdapter(getSupportActionBar().getThemedContext(),
				R.layout.shopping_list_item, cursor,
				new String[] { ShoppingContract.Items.ITEM_NAME },
				new int[] { R.id.itemText });
	}

	@SuppressWarnings("deprecation")
	private void changeList(long listId){
		currentListSetsFilter = listSetsFilter(listId);
		boolean needsReminderFooter = currentListSetsFilter;
		Cursor cursor = getListItemsCursor(listId);
		SimpleCursorAdapter itemsAdapter = (SimpleCursorAdapter) getListAdapter();
		Cursor oldCursor = itemsAdapter.getCursor();
		itemsAdapter.changeCursor(cursor);
		stopManagingCursor(oldCursor); // fix for SDK >= 3
		itemsAdapter.notifyDataSetChanged();

		ListView lv = getListView();
		Log.v(TAG, "changeList removing footer just in case (can we?)");
		lv.removeFooterView(footer);
		if (needsReminderFooter) {
			Log.v(TAG, "changeList adding footer cause we need it");
			lv.addFooterView(footer);
		}
    }

	@SuppressWarnings("deprecation")
	private Cursor getListItemsCursor(long listId) {
		return managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, getSelection(), null, null);
	}

	/*
	 * commented on 13 07 13 con los problemas de footer, borrar si no hubo mas probs
	private void setUpList() {
        long listId = getListId();
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, getSelection(), null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter itemsAdapter = new SimpleCursorAdapter(getSupportActionBar().getThemedContext(),
				R.layout.shopping_list_item, cursor,
				new String[] { ShoppingContract.Items.ITEM_NAME },
				new int[] { R.id.itemText });
		itemsAdapter.setViewBinder(this);
		if ( currentListSetsFilter ){
			Log.v(TAG, "setUpList needsFooter");
			if ( footer == null ) {
				Log.v(TAG, "setUpList footer was null");
				footer = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shopping_list_item_reminder, null, false);
			}
			Log.v(TAG, "setUpList adding Footer");
			lv.addFooterView(footer);
		}
		setListAdapter(itemsAdapter);
	}

	@SuppressWarnings("deprecation")
	private void changeList(long listId){
    	SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
    	currentListSetsFilter = listSetsFilter(listId);
		Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, getSelection(), null, null);
    	Cursor oldCursor = adapter.getCursor();
    	adapter.changeCursor(cursor);
    	stopManagingCursor(oldCursor); // fix for SDK >= 3

    	ListView lv = getListView();
		try {
			Log.v(TAG, "changeList removing Footer");
			lv.removeFooterView(footer);
		} catch (Exception e) {
			Log.e(TAG, "changeList could not remove Footer");
			// different bugs in ListView could throw NPE or ClassCast
			// let's assume that in this case there was no footer
		}
		if (currentListSetsFilter) {
			Log.v(TAG, "changeList adding Footer");
			lv.addFooterView(footer);
		}
		else {Log.v(TAG, "changeList not adding Footer");}
    	adapter.notifyDataSetChanged();
    }
	 */

	private String getSelection() {
		if ( currentListSetsFilter )
			return null;
		else
			return ShoppingContract.Items.ITEM_NEEDED + "=1";
	}

	@SuppressWarnings("deprecation")
	private void showFirstTimeHelp() {
		if ( HelpDialogBuilder.showAutoHelpDialog(this, HelpType.SHOPPING) ){
			showDialog(DIALOG_SHOPPING_HELP);
		}
		if ( HelpDialogBuilder.showAutoHelpDialog(this, HelpType.FIRST_TIME) ){
			showDialog(DIALOG_HELP_FIRST_TIME);
		}
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
		ListView listView = getListView();
		long itemId = getListAdapter().getItemId(position);
		boolean checked = listView.isItemChecked(position);
		editItem(itemId,currentListSetsFilter,checked);
		for (int i = 0; i < getListView().getCount(); i++){
			if ( !listView.isItemChecked(i) )
				return;
		}
		Toast.makeText(this, getString(R.string.info_all_items_bought), Toast.LENGTH_LONG).show();
	}

	private void setUpActionBar(int actionBarNavigationIndex) {
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
				TextView view = (TextView)super.getView(position, convertView, parent);
				int resource = R.drawable.ab_spinner_shopicon;
				if ( position == 0 ){
					resource = R.drawable.ab_spinner_homeicon;
				}
				view.setCompoundDrawablesWithIntrinsicBounds(resource,0,0,0);
				view.setGravity(Gravity.CENTER_VERTICAL);
				return view;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View view = super.getDropDownView(position, convertView, parent);
				int resource = R.drawable.ab_spinner_shopicon;
				if ( position == 0 ){
					resource = R.drawable.ab_spinner_homeicon;
				}
				((TextView)view).setCompoundDrawablesWithIntrinsicBounds(resource,0,0,0);
				return view;
			}
		};
		this.navListAdapter = navListAdapter;

        navListAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setListNavigationCallbacks(navListAdapter, this);
		getSupportActionBar().setSelectedNavigationItem(actionBarNavigationIndex);
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

    @SuppressWarnings("deprecation")
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
		case R.id.menu_item_continue:
			continueShopping();
			break;
		case R.id.menu_item_restart:
			restartShopping();
			break;
		case R.id.menu_item_settings:
			intent = new Intent(this,SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_item_help:
			showDialog(DIALOG_SHOPPING_HELP);
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
		getSupportActionBar().setSelectedNavigationItem(0);
	}

	private void continueShopping() {
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Items.buildShoppedItemsUri();
		contentResolver.update(url, null, null, null);
	}

}
