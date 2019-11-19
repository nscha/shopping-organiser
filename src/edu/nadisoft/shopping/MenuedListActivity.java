package edu.nadisoft.shopping;

import android.app.ListActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * MenuedListActivity is the abstract class that provides default
 * behavior to handle a ListActivity that handles menus and context menus
 * @author Nadia
 */
public abstract class MenuedListActivity extends ListActivity {

	public static String EXTRA_LIST_NAME = "extra_list_name";

	public MenuedListActivity() {
		super();
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Integer contextMenuLayout = getContextMenuLayout();
		super.onCreateContextMenu(menu, v, menuInfo);
		if ( contextMenuLayout != null ){
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(getContextMenuLayout(), menu);
		}
	}

	protected abstract Integer getContextMenuLayout();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = handleMenuSelection(item.getItemId());
		if (!handled) return super.onOptionsItemSelected(item);
		else return handled;
	}

	protected abstract boolean handleMenuSelection(int itemId);

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		boolean handled = handleContextMenuSelection(item.getItemId(),info.position);
		if (!handled) return super.onOptionsItemSelected(item);
		else return handled;
	}

	protected abstract boolean handleContextMenuSelection(int itemId, int position);

}