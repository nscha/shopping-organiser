package edu.nadisoft.shopping;

import edu.nadisoft.shopping.entities.ShoppingItem;
import edu.nadisoft.shopping.entities.ShoppingLists;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ShoppingListActivity extends MenuedListActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String listName = getIntent().getStringExtra(EXTRA_LIST_NAME);
        setShoppingList(ShoppingLists.getList(this, listName));
        setContentView(R.layout.shopping_list);
		setListAdapter(new ArrayAdapter<ShoppingItem>(this, R.layout.list_item, getShoppingList().getFilteredItems()));
		
		TextView listSelector = (TextView) this.findViewById(R.id.listSelector);
        listSelector.setText(getShoppingList().getName());
        
		ListView lv = getListView();
		registerForContextMenu(lv);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		reviewCheckedItems();
    }
    
    private void reviewCheckedItems() {
    	ListView lv = getListView();
    	for (int i = 0; i < lv.getCount(); i++){
    		ShoppingItem item = (ShoppingItem) lv.getItemAtPosition(i);
    		lv.setItemChecked(i, getShoppingList().filterByNeed() ? item.isBought() : item.isNeeded() );
    	}
	}

	@Override
	protected void onPause() {
		for (int i = 0; i < getListView().getAdapter().getCount(); i++) {
			Object item = getListView().getAdapter().getItem(i);
			if ( item instanceof ShoppingItem ){
				ShoppingItem shopItem = (ShoppingItem) item;
				if ( getShoppingList().filterByNeed() ) {
					shopItem.setBought(getListView().isItemChecked(i));
				} else {
					shopItem.setNeeded(getListView().isItemChecked(i));
				}
			}
		}
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
		return R.menu.main_menu;
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
		case R.id.reorder:
			Intent intent = new Intent(getApplicationContext(), OrderingListActivity.class);
	        intent.putExtra(EXTRA_LIST_NAME, getShoppingList().getName());
	        startActivity(intent);
			return true;
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
		default:
			return false;
		}
	}

}
