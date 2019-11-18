package edu.nadisoft.shopping.entities;

import java.util.List;

import android.content.Context;
import edu.nadisoft.shopping.db.DBHelper;

public class ShoppingLists {
	public final static int HOME_LIST = 0;
	public final static int SHOP_LIST = 1;
	
	private static List<ShoppingList> lists;

	public static List<ShoppingList> getLists(Context context){
		if ( lists == null ) {
			DBHelper db = new DBHelper(context);
			lists = db.getLists();
		}
		return lists;
	}
	
	public static ShoppingList getList(Context context, int listId){
		return getLists(context).get(listId);
	}

	public static ShoppingList getList(Context context, String listName) {
		List<ShoppingList> lists = getLists(context);
		if ( listName == null ) return lists.get(0);
		for (ShoppingList list : lists) {
			if (list.getName().equals(listName)){
				return list;
			}
		}
		return null;
	}
	
	public static void addShoppingItem(Context context, ShoppingItem item){
		for (ShoppingList list : getLists(context)) {
			list.addShoppingItem(item);	
		}
	}

	public static void save(Context context) {
		DBHelper db = new DBHelper(context);
		db.save(lists);
	}

}
