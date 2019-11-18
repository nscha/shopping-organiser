package edu.nadisoft.shopping;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import edu.nadisoft.shopping.entities.ShoppingList;
import edu.nadisoft.shopping.entities.ShoppingLists;

public abstract class MenuedListActivity extends ListActivity {

	public static String EXTRA_LIST_NAME = "extra_list_name";
	private ShoppingList list;

	public ShoppingList getShoppingList() {
		return list;
	}

	public void setShoppingList(ShoppingList list) {
		this.list = list;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Integer menuLayout = getMenuLayout();
		if ( menuLayout != null ){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(menuLayout, menu);
			return true;
		} else {
			return super.onCreateOptionsMenu(menu);
		}
	}

	protected abstract Integer getMenuLayout();

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Integer contextMenuLayout = getContextMenuLayout();
		super.onCreateContextMenu(menu, v, menuInfo);
		if ( contextMenuLayout != null ){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(getContextMenuLayout(), menu);
		}
	}

	abstract protected Integer getContextMenuLayout();

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		boolean handled = handleMenuSelection(item.getItemId());
		if (!handled) return super.onOptionsItemSelected(item);
		else return handled;
	}

	abstract protected boolean handleMenuSelection(int itemId);

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		boolean handled = handleContextMenuSelection(item.getItemId(),info.position);
		if (!handled) return super.onOptionsItemSelected(item);
		else return handled;
	}

	abstract protected boolean handleContextMenuSelection(int itemId, int position);

	public void chooseList(View view) {
		List<ShoppingList> lists = ShoppingLists.getLists(this);
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
		        startActivity(intent);
		    }
		});
		builder.show();
    }

}
