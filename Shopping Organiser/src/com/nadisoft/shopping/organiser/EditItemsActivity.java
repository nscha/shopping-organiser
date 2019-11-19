package com.nadisoft.shopping.organiser;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.tlv.TouchListView;
import com.nadisoft.app.CustomAlertDialogBuilder;
import com.nadisoft.app.HelpDialogBuilder;
import com.nadisoft.app.HelpDialogBuilder.HelpType;
import com.nadisoft.shopping.organiser.entities.ShoppingItem;
import com.nadisoft.shopping.organiser.entities.ShoppingList;
import com.nadisoft.shopping.organiser.provider.ShoppingContract;

public class EditItemsActivity extends SherlockListActivity{
	public static final String EXTRA_LIST_ID = "com.nadisoft.shopping.organiser.EditItemsActivity.LISTID";

    private EditText newItemNameEditText;
	private EditText editItemNameEditText;
	private ShoppingItem itemOnEdition;

	private static final int DIALOG_EDIT_ITEMS_HELP = 0;
	private static final int DIALOG_EDIT_ITEM_NAME = 1;
	private static final int DIALOG_CONFIRM_ITEM_DELETE = 2;

	private static final String BUNDLE_ITEM_ON_EDITION = "BundleItemOnEdition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			itemOnEdition = (ShoppingItem) savedInstanceState.getSerializable(BUNDLE_ITEM_ON_EDITION);
		}

        setContentView(R.layout.edit_items);
        long listId = getIntent().getLongExtra(EXTRA_LIST_ID, 1);
        getSupportActionBar().setSubtitle(getShoppingList(listId).getName());

        newItemNameEditText = (EditText) findViewById(R.id.newItemNameEditText);
        setUpList(listId);
        showFirstTimeHelp();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BUNDLE_ITEM_ON_EDITION, itemOnEdition);
	}

	private void setUpList(long listId) {
		TouchListView tlv=(TouchListView)getListView();

        @SuppressWarnings("deprecation")
    	Cursor cursor = managedQuery(ShoppingContract.Items.buildListItemsUri(listId), 
				null, null, null, null);

        @SuppressWarnings("deprecation")
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				R.layout.edit_items_item, cursor,
				new String[] { ShoppingContract.Items._ID,
				ShoppingContract.Items.ITEM_NAME },
				new int[] { R.id.editItemButton,
				R.id.itemText });

		listAdapter.setViewBinder(new ViewBinder(){
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch (view.getId()) {
				case R.id.editItemButton:
					view.setTag(cursor.getLong(columnIndex));
					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							long id = (Long)v.getTag();
							EditItemsActivity.this.startEditingItem(id);
						}
					});
					return true;
				}
				return false;
			}
		});
		setListAdapter(listAdapter);

		tlv.setDropListener(onDrop);
		tlv.setRemoveListener(onRemove);
	}

	private TouchListView.DropListener onDrop=new TouchListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			SimpleCursorAdapter adapter = (SimpleCursorAdapter)getListAdapter();
			Cursor cursor=(Cursor)adapter.getItem(from);
			int idIdx = cursor.getColumnIndex(ShoppingContract.Items._ID);
			int listIdIdx = cursor.getColumnIndex(ShoppingContract.Orderings.ORDERING_LIST_ID);

			long itemId = cursor.getLong(idIdx);
			long listId = cursor.getLong(listIdIdx);
			moveItem(itemId, listId, from,to);
		}
	};

	private TouchListView.RemoveListener onRemove=new TouchListView.RemoveListener() {
		@Override
		public void remove(int which) {
				SimpleCursorAdapter adapter = (SimpleCursorAdapter)getListAdapter();
				Cursor cursor=(Cursor)adapter.getItem(which);
				int idIdx = cursor.getColumnIndex(ShoppingContract.Items._ID);
				startDeletingItem(cursor.getLong(idIdx));
		}
	};

	@SuppressWarnings("deprecation")
	private void showFirstTimeHelp() {
		if ( HelpDialogBuilder.showAutoHelpDialog(this, HelpType.EDIT_ITEMS) ){
			showDialog(DIALOG_EDIT_ITEMS_HELP);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_EDIT_ITEMS_HELP:
        	dialog = createEditItemsHelpDialog();
        	break;
        case DIALOG_EDIT_ITEM_NAME:
            dialog = createEditItemNameDialog();
            break;
        case DIALOG_CONFIRM_ITEM_DELETE:
        	dialog = createConfirmDeleteItemDialog();
        	break;
        default:
            dialog = null;
        }
        return dialog;
    }

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case DIALOG_EDIT_ITEMS_HELP:
			break;
        case DIALOG_EDIT_ITEM_NAME:
            dialog = prepareEditItemNameDialog(dialog);
            break;
        case DIALOG_CONFIRM_ITEM_DELETE:
        	break;
        default:
            dialog = null;
        }
	}

	private Dialog createEditItemsHelpDialog() {
		HelpDialogBuilder builder = new HelpDialogBuilder(this, HelpDialogBuilder.HelpType.EDIT_ITEMS);
		return builder.create();
	}

	private Dialog createEditItemNameDialog(){
		editItemNameEditText = new EditText(this);
		editItemNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);
		builder.setView(editItemNameEditText)
			.setMessage(R.string.item_name_prompt)
	    	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			String newName = editItemNameEditText.getText().toString();
	    			endEditingItem(newName.trim());
	    		}
	    	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			itemOnEdition = null;
	    		}
	    	});
		return builder.create();
	}

	private Dialog prepareEditItemNameDialog(Dialog dialog) {
		editItemNameEditText.setText(itemOnEdition.getName());
		return dialog;
	}

	private Dialog createConfirmDeleteItemDialog(){
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);
		builder.setMessage(R.string.item_del_prompt)
	    	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			endDeletingItem(true);
	    		}

	    	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			endDeletingItem(false);
	    		}
	    	});

		return builder.create();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.menu_item_done:
			finish();
			break;
		case R.id.menu_item_help:
			showDialog(DIALOG_EDIT_ITEMS_HELP);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void addNewItem(View view) {
		String name = newItemNameEditText.getText().toString();
    	if ( name.length() > 0 ) {
    		createItem(name);
			newItemNameEditText.setText("");
    	}
    }

    @SuppressWarnings("deprecation")
	private void startEditingItem(long id) {
		itemOnEdition = getShoppingItem(id);
		showDialog(DIALOG_EDIT_ITEM_NAME);
	}

    private void endEditingItem(String newName) {
    	long id = itemOnEdition.getId();
    	String oldName = itemOnEdition.getName();
    	itemOnEdition = null;
		editItem(id, oldName, newName);
	}

    @SuppressWarnings("deprecation")
	private void startDeletingItem(long id) {
		itemOnEdition = getShoppingItem(id);
		showDialog(DIALOG_CONFIRM_ITEM_DELETE);
	}

    private void endDeletingItem(boolean confirm) {
    	long id = itemOnEdition.getId();
    	if ( confirm ){
    		deleteItem(id);
    	}
    	itemOnEdition = null;
	}

	private ShoppingList getShoppingList(long id) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = ShoppingContract.Lists.buildListUri(id);
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex(ShoppingContract.Lists.LIST_NAME));
		boolean setsFilter = pointedListSetsFilter(cursor);
		ShoppingList shoppingList = new ShoppingList(name, setsFilter);
		shoppingList.setId(id);
		return shoppingList;
	}

	private boolean pointedListSetsFilter(Cursor cursor) {
		return 1 == cursor.getInt(cursor.getColumnIndex(ShoppingContract.Lists.LIST_SETS_FILTER));
	}

	private ShoppingItem getShoppingItem(long id) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = ShoppingContract.Items.buildItemUri(id);
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex(ShoppingContract.Items.ITEM_NAME));
		ShoppingItem shoppingItem = new ShoppingItem(name);
		shoppingItem.setId(id);
		return shoppingItem;
	}

	private void createItem(String name) {
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Items.buildItemsUri();
		ContentValues values = new ContentValues();
		values.put(ShoppingContract.Items.ITEM_NAME, name);
		values.put(ShoppingContract.Items.ITEM_NEEDED, false);
		values.put(ShoppingContract.Items.ITEM_BOUGHT, false);
		contentResolver.insert(url, values);
	}

	private void editItem(long id, String oldName, String newName)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Items.buildItemUri(id);
		ContentValues values = new ContentValues();
		if ( !oldName.equals(newName.trim()) ) {
			values.put(ShoppingContract.Items.ITEM_NAME, newName);
			contentResolver.update(url, values, null, null);
		}
	}

	private void moveItem(long itemId, long listId, int from, int to) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = ShoppingContract.Items.buildMoveListItemUri(listId, itemId, from, to);
		contentResolver.update(uri, null, null, null);
	}

	private void deleteItem(long id)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Items.buildItemUri(id);
		contentResolver.delete(url, null, null);
	}
}
