package edu.nadisoft.shopping;

import java.util.List;

import edu.nadisoft.shopping.entities.ShoppingItem;
import edu.nadisoft.shopping.entities.ShoppingLists;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class OrderingListActivity extends MenuedListActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String listName = getIntent().getStringExtra(EXTRA_LIST_NAME);
        setShoppingList(ShoppingLists.getList(this, listName));
        setContentView(R.layout.ordering_list);
		setListAdapter(new ShoppingItemArrayAdapter(this, R.layout.ordering_list_item, R.id.ordering_list_item_text, getShoppingList().getItems()));
		
		setTitle(getString(R.string.reordering) + " " + getShoppingList().getName());
		
		ListView lv = getListView();
		registerForContextMenu(lv);
    }

	@Override
	protected void onPause() {
		ShoppingLists.save(this);
		super.onPause();
	}

	@SuppressWarnings("unchecked") /** TODO MEJORAR */
	public void addItemToList(View view) {
    	EditText editText = (EditText) findViewById(R.id.editText);
    	String text = editText.getText().toString();
    	if ( text.length() > 0 ) {
	    	ListView lv = getListView();

	    	ArrayAdapter<ShoppingItem> adapter = (ArrayAdapter<ShoppingItem>) lv.getAdapter();
	    	ShoppingItem newItem = new ShoppingItem(text);
	    	//adapter.add(newItem); // parece que no hace falta D:
	    	ShoppingLists.addShoppingItem(this, newItem);
	    	adapter.notifyDataSetChanged();
	    	editText.setText("");
    	}
    }
    
    @Override
	protected Integer getMenuLayout() {
		return R.menu.ordering_main_menu;
	}

	@Override
	protected Integer getContextMenuLayout() {
		return R.menu.context_menu;
	}
	
	@SuppressWarnings("unchecked") /** TODO MEJORAR */
	private void removeListItem(int position) {
		ArrayAdapter<ShoppingItem> adapter = (ArrayAdapter<ShoppingItem>) getListAdapter();
		adapter.remove(adapter.getItem(position));
	}

	private void markAll(boolean value) {
		ListView lv = getListView();
		for (int i = 0; i < lv.getAdapter().getCount(); i++) {
			lv.setItemChecked(i, value);
			((ShoppingItem) lv.getItemAtPosition(i)).setNeeded(value);
		}
	}

	@Override
	public boolean handleMenuSelection (int itemId) {
		// Handle item selection
		switch (itemId) {
		case R.id.select_all:
			markAll(true);
			return true;
		case R.id.select_none:
			markAll(false);
			return true;
		case R.id.quit_reorder:
			Intent intent = new Intent(getApplicationContext(), ShoppingListActivity.class);
	        intent.putExtra(EXTRA_LIST_NAME, getShoppingList().getName());
	        startActivity(intent);
		default:
			return false;
		}
	}

	@Override
	public boolean handleContextMenuSelection(int itemId, int position) {
		switch (itemId) {
		case R.id.remove_item:
			removeListItem(position);
			return true;
		/*
		case R.id.move_up:
			list.moveItemUp(position);
			((ArrayAdapter<ShoppingItem>)getListAdapter()).notifyDataSetChanged();
			reviewCheckedItems();
			return true;
		case R.id.move_down:
			list.moveItemDown(position);
			((ArrayAdapter<ShoppingItem>)getListAdapter()).notifyDataSetChanged();
			reviewCheckedItems();
			return true;
		*/
		default:
			return false;
		}
	}

	
	public void moveItemDown(View view) {
		Object tag = view.getTag();
		if ( tag instanceof ShoppingItem){
			ShoppingItem item = (ShoppingItem) tag;
			getShoppingList().moveItemDown(item);
			ListAdapter adapter = getListAdapter();
			if ( adapter instanceof ShoppingItemArrayAdapter){
				ShoppingItemArrayAdapter siaAdaptor = (ShoppingItemArrayAdapter)adapter; 
				siaAdaptor.notifyDataSetChanged();
			}
		}
    }
	
	public void moveItemUp(View view) {
		Object tag = view.getTag();
		if ( tag instanceof ShoppingItem){
			ShoppingItem item = (ShoppingItem) tag;
			getShoppingList().moveItemUp(item);
			ListAdapter adapter = getListAdapter();
			if ( adapter instanceof ShoppingItemArrayAdapter){
				ShoppingItemArrayAdapter siaAdaptor = (ShoppingItemArrayAdapter)adapter; 
				siaAdaptor.notifyDataSetChanged();
			}
		}
    }
	
	private class ShoppingItemArrayAdapter extends ArrayAdapter<ShoppingItem>{

		public ShoppingItemArrayAdapter(Context context, int resource,
				int textViewResourceId, List<ShoppingItem> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			ShoppingItem item = getItem(position);
			view.findViewById(R.id.moveItemDownButton).setTag(item);
			view.findViewById(R.id.moveItemUpButton).setTag(item);
			return view;
		}
		
	}

}
