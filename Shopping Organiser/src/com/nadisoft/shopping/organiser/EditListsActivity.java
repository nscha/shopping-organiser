package com.nadisoft.shopping.organiser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.actionbarsherlock.app.SherlockListActivity;
import com.nadisoft.shopping.organiser.entities.ShoppingList;
import com.nadisoft.shopping.organiser.provider.ShoppingOrganiserContract;

public class EditListsActivity extends SherlockListActivity{
	private EditText editText;
	private EditText editListNameEditText;
	private ShoppingList listOnEdition;

	static final int DIALOG_LIST_NAME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists);

        editText = (EditText) findViewById(R.id.newListNameEditText);
        ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(ShoppingOrganiserContract.Lists.buildListsUri(), 
				null, null, null, null);
		@SuppressWarnings("deprecation")
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
				R.layout.list_item, cursor,
				new String[] { ShoppingOrganiserContract.Lists.LIST_NAME,
				ShoppingOrganiserContract.Lists._ID,
				ShoppingOrganiserContract.Lists._ID },
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
							Object tag = v.getTag();
							if (tag != null){
								EditListsActivity.this.removeList((Long) tag);
							}
						}
					});
					return true;
				}
				return false;
			}
		});
		setListAdapter(listAdapter);
    }

	@Override
	protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_LIST_NAME:
            dialog = createEditListNameDialog();
            break;
        default:
            dialog = null;
        }
        return dialog;
    }

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
        case DIALOG_LIST_NAME:
            dialog = prepareEditListNameDialog(dialog);
            break;
        default:
            dialog = null;
        }
	}

	private Dialog createEditListNameDialog(){
		editListNameEditText = new EditText(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.list_name_prompt)
	    	.setView(editListNameEditText)
	    	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			String newName = editListNameEditText.getText().toString();
	    			endEditingList(newName.trim());
	    		}
	    	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		@Override
				public void onClick(DialogInterface dialog, int whichButton) {
	    			listOnEdition = null;
	    		}
	    	});
		
		return builder.show();
	}

	private Dialog prepareEditListNameDialog(Dialog dialog) {
		editListNameEditText.setText(listOnEdition.getName());
		return dialog;
	}

	public void addNewList(View view) {
		String name = editText.getText().toString();
    	if ( name.length() > 0 ) {
    		createList(name);
			editText.setText("");
    	}
    }

	private void createList(String name) {
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingOrganiserContract.Lists.buildListsUri();
		ContentValues values = new ContentValues();
		values.put(ShoppingOrganiserContract.Lists.LIST_NAME, name);
		values.put(ShoppingOrganiserContract.Lists.LIST_SETS_FILTER, false);
		contentResolver.insert(url, values);
	}

    @SuppressWarnings("deprecation")
	private void startEditingList(long id) {
		listOnEdition = getShoppingList(id);
		showDialog(DIALOG_LIST_NAME);
	}

    private void endEditingList(String newName) {
    	long id = listOnEdition.getId();
    	String oldName = listOnEdition.getName();
    	listOnEdition = null;
		editList(id, oldName, newName);
	}

	private ShoppingList getShoppingList(long id) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = ShoppingOrganiserContract.Lists.buildListUri(id);
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex(ShoppingOrganiserContract.Lists.LIST_NAME));
		boolean setsFilter = pointedListSetsFilter(cursor);
		ShoppingList shoppingList = new ShoppingList(name, setsFilter);
		shoppingList.setId(id);
		return shoppingList;
	}

	private boolean pointedListSetsFilter(Cursor cursor) {
		return 1 == cursor.getInt(cursor.getColumnIndex(ShoppingOrganiserContract.Lists.LIST_SETS_FILTER));
	}

	private void editList(long id, String oldName, String newName)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingOrganiserContract.Lists.buildListUri(id);
		ContentValues values = new ContentValues();
		if ( !oldName.equals(newName.trim()) ) {
			values.put(ShoppingOrganiserContract.Lists.LIST_NAME, newName);
			contentResolver.update(url, values, null, null);
		}
	}

	private void removeList(long id)
	{
		ContentResolver contentResolver = getContentResolver();
		Uri url = ShoppingOrganiserContract.Lists.buildListUri(id);
		contentResolver.delete(url, null, null);
	}
}
