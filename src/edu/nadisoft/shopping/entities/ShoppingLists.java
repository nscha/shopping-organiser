package edu.nadisoft.shopping.entities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;
import edu.nadisoft.shopping.ShoppingListApplication;
import edu.nadisoft.shopping.db.DBHelper;

public class ShoppingLists {
	public final static int HOME_LIST = 0;
	public final static int SHOP_LIST = 1;
	
	private static List<ShoppingList> lists;

	public static List<ShoppingList> getLists(){
		if ( lists == null ) {
			Context context = ShoppingListApplication.getContext();
			DBHelper db = new DBHelper(context);
			try {
			lists = db.getLists();
			} catch (Exception e) {
				Toast.makeText(context, "UNRECOVERABLE ERROR, RESETTING ALL DATA D:", 5).show();
				db.resetToFactory();
				lists = db.getLists();
			}
		}
		return lists;
	}

	public static ShoppingList getList(String listName) {
		List<ShoppingList> lists = getLists();
		if ( listName == null ) return lists.get(0);
		for (ShoppingList list : lists) {
			if (list.getName().equals(listName)){
				return list;
			}
		}
		return null;
	}
	
	public static void addShoppingItem(ShoppingItem item){
		for (ShoppingList list : getLists()) {
			list.addShoppingItem(item);	
		}
	}

	public static void addShoppingList(String name) {
		int id = 0;
		boolean ok;
		List<ShoppingList> lists = getLists();
		do {
			ok = true;
			for(ShoppingList list: lists){
				if (list.getId() == id) {
					ok = false;
				}
			}
			if (!ok) id++;
		} while (!ok);
		ShoppingList list = new ShoppingList(id, name, true);
		list.addAll(lists.get(0).getItems());
		lists.add(list);
	}
	
	public static void setAsHomeList(ShoppingList list){
		for (ShoppingList shoppingList : getLists()) {
			shoppingList.setFilterByNeed(true);
		}
		list.setFilterByNeed(false);
	}

	@SuppressWarnings("unchecked") //TODO EH?
	public static void save() {
		List<ShoppingList> listsCopy = new ArrayList<ShoppingList>();
		for (ShoppingList shoppingList : lists) {
			listsCopy.add(new ShoppingList(shoppingList));
		}
		new SaveAllDBTask().execute(listsCopy);
	}

	public static void resetListsToFactory() {
		DBHelper db = new DBHelper(ShoppingListApplication.getContext());
		db.resetToFactory();
		lists = null;
		getLists();
	}
}
