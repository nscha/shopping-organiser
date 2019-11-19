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
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nadisoft.app.CustomAlertDialogBuilder;
import com.nadisoft.app.HelpDialogBuilder;
import com.nadisoft.app.HelpDialogBuilder.HelpType;
import com.nadisoft.shopping.organiser.entities.ShoppingList;
import com.nadisoft.shopping.organiser.provider.ShoppingContract;

public class EditListsActivity extends SherlockListActivity{
	private EditText newListNameEditText;
	private EditText editListNameEditText;
	private ShoppingList listOnEdition;

	private static final int DIALOG_EDIT_LISTS_HELP = 0;
	private static final int DIALOG_EDIT_LIST_NAME = 1;
	private static final int DIALOG_CONFIRM_LIST_DELETE = 2;

	private static final String BUNDLE_LIST_ON_EDITION = "BundleListOnEdition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lists);

		if (savedInstanceState != null) {
			listOnEdition = (ShoppingList) savedInstanceState.getSerializable(BUNDLE_LIST_ON_EDITION);
		}

        newListNameEditText = (EditText) findViewById(R.id.newListNameEditText);

		setUpList();
		showFirstTimeHelp();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BUNDLE_LIST_ON_EDITION, listOnEdition);
	}

	private void setUpList() {
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingContract.Lists.buildListsUri(), 
				null, null, null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				R.layout.edit_lists_item, cursor,
				new String[] { ShoppingContract.Lists.LIST_NAME,
				ShoppingContract.Lists._ID,
				ShoppingContract.Lists._ID },
				new int[] { R.id.listItemText,
				R.id.editListButton,
				R.id.removeListButton });
		listAdapter.setViewBinder(new ViewBinder(){
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				boolean setsFilter;
				switch (view.getId()) {
				case R.id.listItemText:
					setsFilter = pointedListSetsFilter(cursor);
					TextView textView = ((TextView)view);
					textView.setText(cursor.getString(columnIndex));
					if ( setsFilter ) {
						textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ab_spinner_homeicon,0,0,0);
					} else {
						textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ab_spinner_shopicon,0,0,0);
					}
					return true;
				case R.id.editListButton:
					view.setTag(cursor.getLong(columnIndex));
					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							long id = (Long)v.getTag();
							EditListsActivity.this.startEditingList(id);
						}
					});
					return true;
				case R.id.removeListButton:
					setsFilter = pointedListSetsFilter(cursor);
					if ( setsFilter ) {
						view.setVisibility(View.INVISIBLE);
						return true;
					} else {
						view.setVisibility(View.VISIBLE);
					}
					view.setTag(cursor.getLong(columnIndex));
					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							long id = (Long)v.getTag();
							EditListsActivity.this.startDeletingList(id);
						}
					});
					return true;
				}
				return false;
			}
		});
		setListAdapter(listAdapter);
	}

	@SuppressWarnings("deprecation")
	private void showFirstTimeHelp() {
		if ( HelpDialogBuilder.showAutoHelpDialog(this, HelpType.EDIT_LISTS) ){
			showDialog(DIALOG_EDIT_LISTS_HELP);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_EDIT_LISTS_HELP:
        	dialog = createEditListsHelpDialog();
        	break;
        case DIALOG_EDIT_LIST_NAME:
            dialog = createEditListNameDialog();
            break;
        case DIALOG_CONFIRM_LIST_DELETE:
            dialog = createConfirmDeleteListDialog();
            break;
        default:
            dialog = null;
        }
        return dialog;
    }

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case DIALOG_EDIT_LISTS_HELP:
			break;
        case DIALOG_EDIT_LIST_NAME:
            dialog = prepareEditListNameDialog(dialog);
            break;
        case DIALOG_CONFIRM_LIST_DELETE:
        	break;
        default:
            dialog = null;
        }
	}

	private Dialog createEditListsHelpDialog() {
		HelpDialogBuilder builder = new HelpDialogBuilder(this, HelpDialogBuilder.HelpType.EDIT_LISTS);
		return builder.create();
	}

	private Dialog createEditListNameDialog(){
		editListNameEditText = new EditText(this);
		editListNameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);
		builder.setView(editListNameEditText)
			.setMessage(R.string.list_name_prompt)
	    	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			String newName = editListNameEditText.getText().toString();
	    			endEditingList(newName.trim());
	    		}
	    	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			endEditingList(null);
	    		}
	    	});

		return builder.create();
	}

	private Dialog createConfirmDeleteListDialog(){
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);
		builder.setMessage(R.string.list_del_prompt)
	    	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			endDeletingList(true);
	    		}

	    	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			endDeletingList(false);
	    		}
	    	});

		return builder.create();
	}

	private Dialog prepareEditListNameDialog(Dialog dialog) {
		editListNameEditText.setText(listOnEdition.getName());
		return dialog;
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
			showDialog(DIALOG_EDIT_LISTS_HELP);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void addNewList(View view) {
		String name = newListNameEditText.getText().toString();
    	if ( name.length() > 0 ) {
    		createList(name);
			newListNameEditText.setText("");
    	}
    }

    @SuppressWarnings("deprecation")
	private void startEditingList(long id) {
		listOnEdition = getShoppingList(id);
		showDialog(DIALOG_EDIT_LIST_NAME);
	}

    private void endEditingList(String newName) {
    	long id = listOnEdition.getId();
    	if ( newName != null ){
        	String oldName = listOnEdition.getName();
    		editList(id, oldName, newName);    		
    	}
    	listOnEdition = null;
	}

    @SuppressWarnings("deprecation")
	private void startDeletingList(long id) {
		listOnEdition = getShoppingList(id);
		showDialog(DIALOG_CONFIRM_LIST_DELETE);
	}

    private void endDeletingList(boolean confirmed) {
    	long id = listOnEdition.getId();
    	listOnEdition = null;
    	if ( confirmed ){
    		deleteList(id);
    	}
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

	private void createList(String name) {
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Lists.buildListsUri();
		ContentValues values = new ContentValues();
		values.put(ShoppingContract.Lists.LIST_NAME, name);
		values.put(ShoppingContract.Lists.LIST_SETS_FILTER, false);
		contentResolver.insert(url, values);
	}

	private boolean pointedListSetsFilter(Cursor cursor) {
		return 1 == cursor.getInt(cursor.getColumnIndex(ShoppingContract.Lists.LIST_SETS_FILTER));
	}

	private void editList(long id, String oldName, String newName)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Lists.buildListUri(id);
		ContentValues values = new ContentValues();
		if ( !oldName.equals(newName.trim()) ) {
			values.put(ShoppingContract.Lists.LIST_NAME, newName);
			contentResolver.update(url, values, null, null);
		}
	}

	private void deleteList(long id)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingContract.Lists.buildListUri(id);
		contentResolver.delete(url, null, null);
	}
}
