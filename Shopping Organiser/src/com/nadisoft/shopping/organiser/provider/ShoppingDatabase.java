package com.nadisoft.shopping.organiser.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nadisoft.shopping.organiser.provider.ShoppingContract.ItemColumns;
import com.nadisoft.shopping.organiser.provider.ShoppingContract.Items;
import com.nadisoft.shopping.organiser.provider.ShoppingContract.ListColumns;
import com.nadisoft.shopping.organiser.provider.ShoppingContract.OrderingColumns;
//import com.nadisoft.shopping.organiser.provider.ShoppingOrganiserContract.OrderingColumns;
import com.nadisoft.shopping.organiser.provider.ShoppingContract.Orderings;

public class ShoppingDatabase extends SQLiteOpenHelper {
	/** Database Name */
	private static final String DATABASE_NAME = "shoppingOrganiser.db";
	/** Database version */
	private static final int DATABASE_VERSION = 1;

	/** DB table names. */
	interface Tables {
		String ITEMS = "items";
		String LISTS = "lists";
		String ORDERINGS = "orderings";
	}

	private Context tmpContext;

	public ShoppingDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.tmpContext = context; 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
		createFirstLists(db);
		initItemsOnDevsDevice(db);
		this.tmpContext = null;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != DATABASE_VERSION) {
            dropTables(db);
            onCreate(db);
        }
	}

	protected void createTables(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.ITEMS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ItemColumns.ITEM_NAME + " TEXT NOT NULL,"
				+ ItemColumns.ITEM_NEEDED + " BOOLEAN NOT NULL,"
				+ ItemColumns.ITEM_BOUGHT + " BOOLEAN NOT NULL,"
				+ "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.LISTS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ListColumns.LIST_NAME + " TEXT NOT NULL,"
				+ ListColumns.LIST_SETS_FILTER + " BOOLEAN NOT NULL,"
				+ "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.ORDERINGS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ OrderingColumns.ORDERING_ITEM_ID + " INTEGER NOT NULL,"
				+ OrderingColumns.ORDERING_LIST_ID + " INTEGER NOT NULL,"
				+ OrderingColumns.ORDERING_POSITION + " INTEGER NOT NULL,"
				+ "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");
	}

	protected void dropTables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.LISTS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.ORDERINGS);
	}

	protected void createFirstLists(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(ListColumns.LIST_NAME, "Home");
		values.put(ListColumns.LIST_SETS_FILTER, true);
		db.insert(Tables.LISTS, null, values);

		values = new ContentValues();
		values.put(ListColumns.LIST_NAME, "Shop");
		values.put(ListColumns.LIST_SETS_FILTER, false);
		db.insert(Tables.LISTS, null, values);
	}

    private void initItemsOnDevsDevice(SQLiteDatabase db) {
    	String devsDeviceID = "9f756f52c0f9df08";
		String android_ID = android.provider.Settings.Secure.getString(tmpContext.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		if ( android_ID == null || devsDeviceID.equals(android_ID) )
		{
			insertHCitems(db);
		}
	}

	public void insertHCitems(SQLiteDatabase db){
		insertHCItem(db,"7up",1,88,2,1);
		insertHCItem(db,"Aceite de girasol",1,80,2,22);
		insertHCItem(db,"Aceitunas",1,29,2,7);
		insertHCItem(db,"Aji molido",1,74,2,18);
		insertHCItem(db,"Alfajorcitos",1,58,2,88);
		insertHCItem(db,"Arroz Blanco",1,57,2,3);
		insertHCItem(db,"Arroz con Salsa",1,85,2,6);
		insertHCItem(db,"Atun",1,50,2,11);
		insertHCItem(db,"Azucar",1,67,2,96);
		insertHCItem(db,"Barritas de Cereal",1,75,2,20);
		insertHCItem(db,"Blem",1,93,2,34);
		insertHCItem(db,"Bolsas de residuo Num 3",1,48,2,45);
		insertHCItem(db,"Caldo en cubos",1,87,2,10);
		insertHCItem(db,"Capuccino",1,68,2,97);
		insertHCItem(db,"Carilinas",1,11,2,41);
		insertHCItem(db,"Carne picada",1,19,2,62);
		insertHCItem(db,"Cebolla de verdeo",1,42,2,60);
		insertHCItem(db,"Cebollin (Ciboulette)",1,43,2,61);
		insertHCItem(db,"Ceramicol Incoloro",1,98,2,37);
		insertHCItem(db,"Choclo en lata",1,52,2,14);
		insertHCItem(db,"Chuker",1,76,2,98);
		insertHCItem(db,"Churrasco de cuadril",1,20,2,64);
		insertHCItem(db,"Cif Crema",1,92,2,32);
		insertHCItem(db,"Colita de cuadril",1,21,2,66);
		insertHCItem(db,"Crema",1,27,2,78);
		insertHCItem(db,"Cuaderno",1,101,2,101);
		insertHCItem(db,"Dentifrico",1,3,2,46);
		insertHCItem(db,"Desodorante de ambientes",1,10,2,42);
		insertHCItem(db,"Desodorante Maxi",1,5,2,51);
		insertHCItem(db,"Desodorante Nai",1,4,2,50);
		insertHCItem(db,"Detergente",1,97,2,36);
		insertHCItem(db,"Dulce de batata",1,36,2,67);
		insertHCItem(db,"Dulce de leche",1,32,2,92);
		insertHCItem(db,"Fideos",1,82,2,2);
		insertHCItem(db,"Fideos con Salsa",1,84,2,5);
		insertHCItem(db,"Frutigran",1,59,2,89);
		insertHCItem(db,"Galletitas de agua",1,61,2,91);
		insertHCItem(db,"Galletitas dulces",1,60,2,90);
		insertHCItem(db,"Gillette Foamy Piel Sensible",1,13,2,56);
		insertHCItem(db,"Gillette Match 3 Turbo",1,12,2,55);
		insertHCItem(db,"Harina",1,78,2,21);
		insertHCItem(db,"Huevos",1,23,2,68);
		insertHCItem(db,"Jabon",1,9,2,54);
		insertHCItem(db,"Jabon blanco",1,89,2,28);
		insertHCItem(db,"Jabon en polvo",1,95,2,29);
		insertHCItem(db,"Jamon cocido",1,37,2,84);
		insertHCItem(db,"Jugo de Naranja",1,28,2,79);
		insertHCItem(db,"Lamparitas",1,100,2,38);
		insertHCItem(db,"Lavandina",1,90,2,31);
		insertHCItem(db,"Leche",1,44,2,69);
		insertHCItem(db,"Lechuga",1,41,2,58);
		insertHCItem(db,"Limon minerva",1,45,2,23);
		insertHCItem(db,"Madalenas",1,64,2,94);
		insertHCItem(db,"Manteca",1,24,2,74);
		insertHCItem(db,"Manteca untable",1,25,2,75);
		insertHCItem(db,"Marcador indeleble",1,102,2,102);
		insertHCItem(db,"Mayonesa",1,30,2,24);
		insertHCItem(db,"Medallones de pollo",1,16,2,70);
		insertHCItem(db,"Miel",1,77,2,99);
		insertHCItem(db,"Morron",1,51,2,12);
		insertHCItem(db,"Mr Musculo Antigrasa",1,94,2,35);
		insertHCItem(db,"Municiones",1,83,2,4);
		insertHCItem(db,"Nesquik",1,66,2,25);
		insertHCItem(db,"Pan lactal",1,81,2,100);
		insertHCItem(db,"Papa",1,79,2,57);
		insertHCItem(db,"Papel higienico",1,8,2,39);
		insertHCItem(db,"Papel metalico",1,47,2,44);
		insertHCItem(db,"Papel transparente",1,46,2,43);
		insertHCItem(db,"Pascualina",1,39,2,83);
		insertHCItem(db,"Patitas de pollo",1,17,2,71);
		insertHCItem(db,"Pecetto de vaca",1,22,2,65);
		insertHCItem(db,"Pechugas de pollo",1,18,2,63);
		insertHCItem(db,"Pilas",1,99,2,27);
		insertHCItem(db,"Pimenton extra",1,73,2,19);
		insertHCItem(db,"Pimienta blanca",1,71,2,15);
		insertHCItem(db,"Pimienta negra",1,72,2,16);
		insertHCItem(db,"Poett",1,91,2,33);
		insertHCItem(db,"Provenzal",1,70,2,17);
		insertHCItem(db,"Pure Chef",1,54,2,8);
		insertHCItem(db,"Queso blanco",1,26,2,76);
		insertHCItem(db,"Queso de maquina",1,38,2,85);
		insertHCItem(db,"Queso de rallar",1,35,2,82);
		insertHCItem(db,"Queso port Salut",1,34,2,81);
		insertHCItem(db,"Rapiditas",1,15,2,73);
		insertHCItem(db,"Rolisec",1,49,2,40);
		insertHCItem(db,"Sabor en cubos",1,86,2,9);
		insertHCItem(db,"Sal fina",1,56,2,87);
		insertHCItem(db,"Sal gruesa",1,55,2,86);
		insertHCItem(db,"Salchichas",1,14,2,72);
		insertHCItem(db,"Shampoo Maxi",1,7,2,53);
		insertHCItem(db,"Shampoo Nai",1,6,2,52);
		insertHCItem(db,"Sobres de jugo",1,62,2,0);
		insertHCItem(db,"Suavizante",1,96,2,30);
		insertHCItem(db,"Talitas",1,63,2,93);
		insertHCItem(db,"Tholem",1,33,2,80);
		insertHCItem(db,"Toallitas diarias",1,0,2,47);
		insertHCItem(db,"Toallitas nocturnas",1,2,2,49);
		insertHCItem(db,"Toallitas normales",1,1,2,48);
		insertHCItem(db,"Tomate",1,40,2,59);
		insertHCItem(db,"Tomate en lata",1,53,2,13);
		insertHCItem(db,"Vainillas",1,65,2,95);
		insertHCItem(db,"Yoghurt",1,31,2,77);
		insertHCItem(db,"Zucaritas",1,69,2,26);
	}

	protected void insertHCItem(SQLiteDatabase db, String name, long listId1, int pos1, long listId2, int pos2) {
		ContentValues values = new ContentValues();
		values.put(Items.ITEM_NAME, name);
		values.put(Items.ITEM_NEEDED, false);
		values.put(Items.ITEM_BOUGHT, false);
		long itemId = db.insert(Tables.ITEMS, null, values);
		values = new ContentValues();
		values.put(Orderings.ORDERING_ITEM_ID, itemId);
		values.put(Orderings.ORDERING_LIST_ID, listId1);
		values.put(Orderings.ORDERING_POSITION, pos1);
		db.insert(Tables.ORDERINGS, null, values);
		values = new ContentValues();
		values.put(Orderings.ORDERING_ITEM_ID, itemId);
		values.put(Orderings.ORDERING_LIST_ID, listId2);
		values.put(Orderings.ORDERING_POSITION, pos2);
		db.insert(Tables.ORDERINGS, null, values);
	}
}
