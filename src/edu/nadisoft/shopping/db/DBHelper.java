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
	
    private static Object lock = new Object();

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

	public void resetToFactory() {
		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			onCreate(db);
		}
	}

	public List<ShoppingList> getLists() {
		Log.d(DBHelper.class.getSimpleName(), "ENTERING getLists");
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
		
		Log.d(DBHelper.class.getSimpleName(), "EXITING getLists");
		return shoppingLists;
	}

	public void save(List<ShoppingList> oLists) {
		Log.d(DBHelper.class.getSimpleName(), "->ENTERING save unsynced");
		synchronized (lock) {
			Log.d(DBHelper.class.getSimpleName(), "*->ENTERING save SYNC ZONE");
			SQLiteDatabase db = getWritableDatabase();
			db.execSQL("DELETE FROM " + TABLE_NAME_POSITIONS);
			db.execSQL("DELETE FROM " + TABLE_NAME_ITEMS);
			db.execSQL("DELETE FROM " + TABLE_NAME_LISTS);

			// Inserting lists
			List<ShoppingList> lists = new ArrayList<ShoppingList>(oLists);
			for (ShoppingList list : lists) {
				ContentValues values = new ContentValues();
				values.put(LISTS_COL_ID, list.getId());
				values.put(LISTS_COL_NAME, list.getName());
				values.put(LISTS_COL_NEED_FILTER, list.filterByNeed());
				db.insert(TABLE_NAME_LISTS, null, values);
			}

			// Inserting items
			ShoppingList aList = lists.get(0); // Actually all lists have the same items
			int item_id = 1;
			for (ShoppingItem item : new ArrayList<ShoppingItem>(aList.getItems())) {
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
			Log.d(DBHelper.class.getSimpleName(), "*<- EXITING save SYNCED ZONE");
		}
		Log.d(DBHelper.class.getSimpleName(), "<- EXITING save unsynced");
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
		items.add(new ShoppingItem("Toallitas diarias, normales, nocturnas"));
		items.add(new ShoppingItem("Dentifrico"));
		items.add(new ShoppingItem("Desodorante Nai"));
		items.add(new ShoppingItem("Desodorante Maxi"));
		items.add(new ShoppingItem("Shampoo (VO5, Sedal, Capilatis de Ortiga)"));
		items.add(new ShoppingItem("Papel Higienico"));
		items.add(new ShoppingItem("Jabon"));
		items.add(new ShoppingItem("Desodorante de ambientes"));
		items.add(new ShoppingItem("Carilinas"));
		items.add(new ShoppingItem("Gillette Match 3 Turbo"));
		items.add(new ShoppingItem("Gillette Foamy Piel Sensible"));
		items.add(new ShoppingItem("Rapiditas"));
		items.add(new ShoppingItem("Salchichas"));
		items.add(new ShoppingItem("Medallones de Pollo"));
		items.add(new ShoppingItem("Patitas de Pollo (u otro relleno)"));
		items.add(new ShoppingItem("Pechugas de Pollo"));
		items.add(new ShoppingItem("Carne picada"));
		items.add(new ShoppingItem("Churrasco de Cuadril"));
		items.add(new ShoppingItem("Colita de Cuadril"));
		items.add(new ShoppingItem("Pecetto de Vaca"));
		items.add(new ShoppingItem("Huevos"));
		items.add(new ShoppingItem("Manteca"));
		items.add(new ShoppingItem("Manteca untable"));
		items.add(new ShoppingItem("Leche"));
		items.add(new ShoppingItem("Limon Minerva"));
		items.add(new ShoppingItem("Queso Blanco"));
		items.add(new ShoppingItem("Mayonesa"));
		items.add(new ShoppingItem("Dulce de Leche"));
		items.add(new ShoppingItem("Crema"));
		items.add(new ShoppingItem("Tholem"));
		items.add(new ShoppingItem("Queso Port Salut"));
		items.add(new ShoppingItem("Dulce de Batata"));
		items.add(new ShoppingItem("Pascualina"));
		items.add(new ShoppingItem("Jamon cocido"));
		items.add(new ShoppingItem("Queso de maquina"));
		items.add(new ShoppingItem("Queso de rallar"));
		items.add(new ShoppingItem("Tomate"));
		items.add(new ShoppingItem("Lechuga"));
		items.add(new ShoppingItem("Cebolla de Verdeo"));
		items.add(new ShoppingItem("Cebollin (Ciboulette)"));
		items.add(new ShoppingItem("Papel Transparente"));
		items.add(new ShoppingItem("Papel Metalico"));
		items.add(new ShoppingItem("Bolsas de Residuo Num 3"));
		items.add(new ShoppingItem("Rolisec"));
		items.add(new ShoppingItem("Atun"));
		items.add(new ShoppingItem("Choclo cremoso (o no)"));
		items.add(new ShoppingItem("Morron"));
		items.add(new ShoppingItem("Sal gruesa"));
		items.add(new ShoppingItem("Sal fina"));
		items.add(new ShoppingItem("Arroz Blanco"));
		items.add(new ShoppingItem("Galletitas dulces"));
		items.add(new ShoppingItem("Galletitas de agua"));
		items.add(new ShoppingItem("Talitas"));
		items.add(new ShoppingItem("Madalenas"));
		items.add(new ShoppingItem("Vainillas"));
		items.add(new ShoppingItem("Nesquik"));
		items.add(new ShoppingItem("Azucar"));
		items.add(new ShoppingItem("Capuccino"));
		items.add(new ShoppingItem("Zucaritas"));
		items.add(new ShoppingItem("Provenzal"));
		items.add(new ShoppingItem("Pimienta"));
		items.add(new ShoppingItem("Pimenton Extra"));
		items.add(new ShoppingItem("Aji Molido"));
		items.add(new ShoppingItem("Barritas de Cereal"));
		items.add(new ShoppingItem("Chuker"));
		items.add(new ShoppingItem("Miel"));
		items.add(new ShoppingItem("Papa"));
		items.add(new ShoppingItem("Aceite de girasol"));
		items.add(new ShoppingItem("Pan Lactal"));
		items.add(new ShoppingItem("Harina"));
		items.add(new ShoppingItem("Fideos"));
		items.add(new ShoppingItem("Municiones"));
		items.add(new ShoppingItem("Fideos con Salsa"));
		items.add(new ShoppingItem("Arroz con Salsa"));
		items.add(new ShoppingItem("Sabor en cubos"));
		items.add(new ShoppingItem("Caldo en cubos"));
		items.add(new ShoppingItem("7up"));
		items.add(new ShoppingItem("Jabon Blanco"));
		items.add(new ShoppingItem("Lavandina"));
		items.add(new ShoppingItem("Poett"));
		items.add(new ShoppingItem("Cif Crema"));
		items.add(new ShoppingItem("Blem"));
		items.add(new ShoppingItem("Mr Musculo Antigrasa"));
		items.add(new ShoppingItem("Detergente (Lavavajillas) Ala Blanco"));
		items.add(new ShoppingItem("Ceramicol Incoloro"));
		items.add(new ShoppingItem("Lamparitas bajo consumo"));
		items.add(new ShoppingItem("Lamparitas"));
		items.add(new ShoppingItem("Cuaderno"));
		items.add(new ShoppingItem("Marcador Indeleble"));
		return items;
	}

}
