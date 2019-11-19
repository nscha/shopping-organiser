package edu.nadisoft.shopping;

import java.util.List;

import edu.nadisoft.shopping.entities.ShoppingItem;
import edu.nadisoft.shopping.entities.ShoppingLists;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * OrderingListActivity is the Activity used to allow reording of items in Shopping Lists
 * @author Nadia
 */
public class OrderingListActivity extends MenuedShoppingListActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String listName = getIntent().getStringExtra(EXTRA_LIST_NAME);
        setShoppingList(ShoppingLists.getList(listName));
        setContentView(R.layout.ordering_list);
		setListAdapter(new ShoppingItemArrayAdapter(this, R.layout.ordering_list_item, R.id.ordering_list_item_text, getShoppingList().getItems()));
		
		setTitle(getString(R.string.reordering) + " " + getShoppingList().getName());
		
		ListView lv = getListView();
		registerForContextMenu(lv);
    }

	@Override
	protected void onDestroy() {
		Log.i(this.getClass().getSimpleName(), "onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		Log.i(this.getClass().getSimpleName(), "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.i(this.getClass().getSimpleName(), "onResume");
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.i(this.getClass().getSimpleName(), "onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.i(this.getClass().getSimpleName(), "onStop");
		super.onStop();
	}

	@Override
	protected void onPause() {
		Log.i(this.getClass().getSimpleName(), "onPause (saving!)");
		ShoppingLists.save();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		Log.i(this.getClass().getSimpleName(), "onBackPressed");
		Log.i(this.getClass().getSimpleName(), "calling finish from onBackPressed");
		finish();
		super.onBackPressed();
	}

	@SuppressWarnings("unchecked") /** TODO MEJORAR */
	public void addItemToList(View view) {
    	EditText editText = (EditText) findViewById(R.id.editText);
    	String text = editText.getText().toString();
    	if ( text.length() > 0 ) {
	    	ListView lv = getListView();

	    	ArrayAdapter<ShoppingItem> adapter = (ArrayAdapter<ShoppingItem>) lv.getAdapter();
	    	ShoppingItem newItem = new ShoppingItem(text);
	    	if ( getShoppingList().filterByNeed() ){
	    		newItem.setNeeded(true);
	    	}
	    	
	    	ShoppingLists.addShoppingItem(newItem);
	    	if ( adapter.getPosition(newItem) < 0 ){
	    		adapter.add(newItem); //The list the adapter has could be the same pointer or not
	    	}
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

	@Override
	public boolean handleMenuSelection (int itemId) {
		// Handle item selection
		switch (itemId) {
		case R.id.quit_reorder:
			Intent intent = new Intent(getApplicationContext(), ShoppingListActivity.class);
	        intent.putExtra(EXTRA_LIST_NAME, getShoppingList().getName());
	        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	        startActivity(intent);
	        finish();
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
