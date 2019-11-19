package edu.nadisoft.shopping;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import edu.nadisoft.shopping.entities.ShoppingList;
import edu.nadisoft.shopping.entities.ShoppingLists;

/**
 * MenuedShoppingListActivity is the abstract class that provides default
 * behavior to handle an Activity that handles a ShoppingList
 * and allows to change to another ShoppingList
 * @author Nadia
 */
public abstract class MenuedShoppingListActivity extends MenuedListActivity {

	private ShoppingList list;

	public ShoppingList getShoppingList() {
		return list;
	}

	public void setShoppingList(ShoppingList list) {
		this.list = list;
	}
	
	public void chooseList(View view) {
		List<ShoppingList> lists = ShoppingLists.getLists();
		final String[] items = new String[lists.size()];
		for (int i = 0; i < lists.size(); i++) {
			items[i] = lists.get(i).getName();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.list_selector));
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        Intent intent = new Intent(getApplicationContext(), ShoppingListActivity.class);
		        intent.putExtra(EXTRA_LIST_NAME, items[item]);
		        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		        startActivity(intent);
		    }
		});
		builder.show();
    }

}
