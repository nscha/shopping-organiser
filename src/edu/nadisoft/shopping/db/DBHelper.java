package edu.nadisoft.shopping.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.nadisoft.shopping.R;
import edu.nadisoft.shopping.entities.ShoppingItem;
import edu.nadisoft.shopping.entities.ShoppingList;
import edu.nadisoft.shopping.entities.ShoppingLists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shopping_list";
    
    private static final String TABLE_NAME_ITEMS = "items";
    private static final String ITEMS_COL_ID = "id";
    private static final String ITEMS_COL_NAME = "name";
    private static final String ITEMS_COL_NEEDED = "needed";
    private static final String ITEMS_COL_BOUGHT = "bought";
    
    private static final String TABLE_NAME_POSITIONS = "positions";
    private static final String POSITIONS_COL_ITEM_ID = "item_id";
    private static final String POSITIONS_COL_LIST_ID = "list_id";
    private static final String POSITIONS_COL_POSITION = "position";

    private static final String TABLE_NAME_LISTS = "lists";
    private static final String LISTS_COL_ID = "id";
    private static final String LISTS_COL_NAME = "name";
    private static final String LISTS_COL_NEED_FILTER = "need_filter";

    private Context context;
    
    private static final String ITEMS_TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME_ITEMS + " (" +
                ITEMS_COL_ID + " INT, " +
                ITEMS_COL_NAME + " TEXT, " +
                ITEMS_COL_NEEDED + " BOOLEAN, " +
                ITEMS_COL_BOUGHT + " BOOLEAN);";

    private static final String POSITIONS_TABLE_CREATE =
        "CREATE TABLE " + TABLE_NAME_POSITIONS + " (" +
        POSITIONS_COL_ITEM_ID + " INT, " +
        POSITIONS_COL_LIST_ID + " INT, " +
        POSITIONS_COL_POSITION + " INT);";

    private static final String LISTS_TABLE_CREATE =
        "CREATE TABLE " + TABLE_NAME_LISTS + " (" +
        LISTS_COL_ID + " INT, " +
        LISTS_COL_NAME + " TEXT, " +
        LISTS_COL_NEED_FILTER + " BOOLEAN);";
	

    public DBHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    	this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.w(DBHelper.class.getSimpleName(), "onCreate debug me with: sqlite3 "+db.getPath());
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_POSITIONS);
        db.execSQL(ITEMS_TABLE_CREATE);
        db.execSQL(POSITIONS_TABLE_CREATE);
        db.execSQL(LISTS_TABLE_CREATE);
        createDefaultLists(db);
        populateHCTables(db);
        Log.w(DBHelper.class.getSimpleName(), "/onCreate");
    }
    
    private void createDefaultLists(SQLiteDatabase db) {
    	ContentValues values = new ContentValues();
		values.put(LISTS_COL_ID, ShoppingLists.HOME_LIST);
		values.put(LISTS_COL_NAME, context.getString(R.string.list_name_home));
		values.put(LISTS_COL_NEED_FILTER, false);
		db.insert(TABLE_NAME_LISTS, null, values);
		
		values = new ContentValues();
		values.put(LISTS_COL_ID, ShoppingLists.SHOP_LIST);
		values.put(LISTS_COL_NAME, context.getString(R.string.list_name_shop));
		values.put(LISTS_COL_NEED_FILTER, true);
		db.insert(TABLE_NAME_LISTS, null, values);
	}

	private void populateHCTables(SQLiteDatabase db) {
    	List<ShoppingItem> items = getHCItems();
    	saveHCItems(items, db);
	}

	@Override
    public void onOpen(SQLiteDatabase db){
		/*
		Log.w(DBHelper.class.getSimpleName(), "FORCE calling onCreate");
    	onCreate(db);
    	*/
    	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getSimpleName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_POSITIONS);
        onCreate(db);
	}

	public List<ShoppingList> getLists() {
		SQLiteDatabase db = getReadableDatabase();
		
		String sqlLists = "SELECT "+LISTS_COL_ID+", "+LISTS_COL_NAME+", "+LISTS_COL_NEED_FILTER+" FROM "+TABLE_NAME_LISTS;
		Cursor cursorLists = db.rawQuery(sqlLists, null);
		if (cursorLists == null || !cursorLists.moveToFirst()) {
			if ( cursorLists != null ) cursorLists.close(); 
			Log.e(DBHelper.class.getSimpleName(), "no lists found");
            return null;
		}
		
		int listCount = cursorLists.getCount();
		List<ShoppingList> shoppingLists = new ArrayList<ShoppingList>(listCount);
		for (int i = 0; i < listCount; i++){
			int col = 0;
			int listId = cursorLists.getInt(col++);
			String listName = cursorLists.getString(col++);
			boolean needFilter = cursorLists.getInt(col++) == 1;
			ShoppingList list = new ShoppingList(listId, listName, needFilter);
			shoppingLists.add(list);
			cursorLists.moveToNext();
		}
		cursorLists.close();
		
		String sql = "SELECT "+ITEMS_COL_ID+", "+ITEMS_COL_NAME+", "+ITEMS_COL_NEEDED+", "+ITEMS_COL_BOUGHT;
		for (ShoppingList list: shoppingLists) {
			sql += ", "+list.getId();
			sql += ", ( SELECT "+POSITIONS_COL_POSITION+" FROM "+TABLE_NAME_POSITIONS+
			" WHERE "+POSITIONS_COL_ITEM_ID+" = "+ITEMS_COL_ID+" AND "+ POSITIONS_COL_LIST_ID+"="+list.getId()+")";
		}
		sql += " FROM items";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
		int itemCount = cursor.getCount();
		
		List<ShoppingItem[]> listsVectors = new ArrayList<ShoppingItem[]>(listCount);
		for (int j = 0; j < listCount; j++){
			listsVectors.add(new ShoppingItem[itemCount]);
		}
		
		for (int i = 0; i < itemCount; i++){
			int col = 0;
			@SuppressWarnings("unused")
			int itemId = cursor.getInt(col++);
			String name = cursor.getString(col++);
			boolean needed = cursor.getInt(col++) == 1;
			boolean bought = cursor.getInt(col++) == 1;
			ShoppingItem item = new ShoppingItem(name, needed, bought);
			//There will be two virtual columns for each list, thanks to the nested SELECTs 
			for (int j = 0; j < listCount; j++){
				int listId = cursor.getInt(col++);
				int position = cursor.getInt(col++);
				listsVectors.get(listId)[position] = item;
			}
			cursor.moveToNext();
		}
		cursor.close();
		
		db.close();
		
		for (ShoppingList list : shoppingLists) {
			List<ShoppingItem> items = new ArrayList<ShoppingItem>(itemCount);
			items.addAll(Arrays.asList(listsVectors.get(list.getId())));
			list.addAll(items);
		}
		
		return shoppingLists;
	}

	/*
	public List<ShoppingItem> getHomeItems(){
		SQLiteDatabase db = getReadableDatabase();
		db.close();
		return getHCItems();
		int listId = ShoppingList.HOME_LIST;
		SQLiteDatabase db = getReadableDatabase();
		String sql = "SELECT " + ITEMS_COL_NAME + ", " + ITEMS_COL_NEEDED + ", " + ITEMS_COL_BOUGHT + ", " + ORDERINGS_COL_ORDERING +
			" FROM " + TABLE_NAME_ITEMS + " LEFT OUTER JOIN " + TABLE_NAME_ORDERINGS +
			" ON " + TABLE_NAME_ITEMS+"."+ITEMS_COL_ID + " = " + TABLE_NAME_ORDERINGS+"."+ORDERINGS_COL_ITEM_ID +
			" WHERE " + TABLE_NAME_ORDERINGS+"."+ORDERINGS_COL_LIST_ID + " = " + listId + 
			" OR " + TABLE_NAME_ORDERINGS+"."+ORDERINGS_COL_LIST_ID + " IS NULL) ORDER BY " + ORDERINGS_COL_ORDERING;
		
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
		List<ShoppingItem> items = new ArrayList<ShoppingItem>(cursor.getCount()); 
		for (int i = 0; i < cursor.getCount(); i++){
			int col = 0;
			String name = cursor.getString(col++);
			boolean needed = cursor.getInt(col++) == 1;
			boolean bought = cursor.getInt(col++) == 1;
			//int index = cursor.getInt(col++);
			ShoppingItem item = new ShoppingItem(name, needed, bought);
			items.add(item);
			cursor.moveToNext();
		}
		db.close();
		return items;

	}
*/

	public void save(List<ShoppingList> lists) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NAME_ITEMS);
		db.execSQL("DELETE FROM " + TABLE_NAME_POSITIONS);
		/** TODO borrar tambien lists ? */
		
		ShoppingList aList = lists.get(0); //Actually all lists have the same items
		int item_id = 1;
		for (ShoppingItem item : aList.getItems()) {
			ContentValues values = new ContentValues();
			values.put(ITEMS_COL_ID, item_id);
			values.put(ITEMS_COL_NAME, item.getName());
			values.put(ITEMS_COL_NEEDED, item.isNeeded());
			values.put(ITEMS_COL_BOUGHT, item.isBought());
			
			for (ShoppingList list : lists) {
				int position = list.indexOf(item);
				ContentValues positionsValues = new ContentValues();
				positionsValues.put(POSITIONS_COL_ITEM_ID, item_id);
				positionsValues.put(POSITIONS_COL_LIST_ID, list.getId());
				positionsValues.put(POSITIONS_COL_POSITION, position);
				db.insert(TABLE_NAME_POSITIONS, null, positionsValues);
			}
			
			db.insert(TABLE_NAME_ITEMS, null, values);
			item_id++;
		}
		db.close();
	}

	private void saveHCItems(List<ShoppingItem> items, SQLiteDatabase db){
		int i = 1;
		for (ShoppingItem item : items) {
			ContentValues values = new ContentValues();
			values.put(ITEMS_COL_ID, i);
			values.put(ITEMS_COL_NAME, item.getName());
			values.put(ITEMS_COL_NEEDED, item.isNeeded());
			values.put(ITEMS_COL_BOUGHT, item.isBought());
			
			ContentValues homeListValues = new ContentValues();
			homeListValues.put(POSITIONS_COL_ITEM_ID, i);
			homeListValues.put(POSITIONS_COL_LIST_ID, ShoppingLists.HOME_LIST);
			homeListValues.put(POSITIONS_COL_POSITION, i-1);
			
			ContentValues shopListValues = new ContentValues();
			shopListValues.put(POSITIONS_COL_ITEM_ID, i);
			shopListValues.put(POSITIONS_COL_LIST_ID, ShoppingLists.SHOP_LIST);
			shopListValues.put(POSITIONS_COL_POSITION, i-1);
			
			i++;
			db.insert(TABLE_NAME_ITEMS, null, values);
			db.insert(TABLE_NAME_POSITIONS, null, homeListValues);
			db.insert(TABLE_NAME_POSITIONS, null, shopListValues);
		}
	}
	
	public List<ShoppingItem> getHCItems(){
		List<ShoppingItem> items = new ArrayList<ShoppingItem>();
		ShoppingItem item = new ShoppingItem("Leche");
		item.setNeeded(true);
		items.add(item);
		item = new ShoppingItem("Marcador Indeleble");
		item.setNeeded(true);
		items.add(item);
		item = new ShoppingItem("Huevos");
		items.add(item);
		item = new ShoppingItem("Manteca");
		items.add(item);
		item = new ShoppingItem("Papas");
		items.add(item);
		item = new ShoppingItem("Fideos");
		items.add(item);
		item = new ShoppingItem("Queso Blanco");
		items.add(item);
		item = new ShoppingItem("Manteca Untable");
		items.add(item);
		item = new ShoppingItem("Papel Transparente");
		items.add(item);
		item = new ShoppingItem("Papel Metalico");
		items.add(item);
		item = new ShoppingItem("Carne Picada");
		items.add(item);
		item = new ShoppingItem("Pechugas de Pollo");
		items.add(item);
		item = new ShoppingItem("Tomate");
		item.setNeeded(true);
		items.add(item);
		item = new ShoppingItem("Lechuga");
		item.setNeeded(true);
		items.add(item);
		item = new ShoppingItem("Jamon y Queso");
		items.add(item);
		item = new ShoppingItem("Pan Lactal");
		items.add(item);
		return items;
	}

}
