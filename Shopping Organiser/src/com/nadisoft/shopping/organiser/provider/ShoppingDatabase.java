package com.nadisoft.shopping.organiser.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.nadisoft.shopping.organiser.provider.ShoppingContract.ItemColumns;
import com.nadisoft.shopping.organiser.provider.ShoppingContract.ListColumns;
//import com.nadisoft.shopping.organiser.provider.ShoppingOrganiserContract.OrderingColumns;

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

	public ShoppingDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + Tables.ITEMS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ItemColumns.ITEM_NAME + " TEXT NOT NULL,"
				+ ItemColumns.ITEM_NEEDED + " BOOLEAN NOT NULL,"
				+ ItemColumns.ITEM_BOUGHT + " BOOLEAN NOT NULL,"
				+ ItemColumns.ITEM_TMP_POSITION + " INT NOT NULL," //DELME
				+ "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.LISTS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ListColumns.LIST_NAME + " TEXT NOT NULL,"
				+ ListColumns.LIST_SETS_FILTER + " BOOLEAN NOT NULL,"
				+ "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");

		ContentValues values = new ContentValues();
		values.put(ListColumns.LIST_NAME, "Home");
		values.put(ListColumns.LIST_SETS_FILTER, true);
		db.insert(Tables.LISTS, null, values);

		values = new ContentValues();
		values.put(ListColumns.LIST_NAME, "Shop");
		values.put(ListColumns.LIST_SETS_FILTER, false);
		db.insert(Tables.LISTS, null, values);
/*
		db.execSQL("CREATE TABLE " + Tables.ORDERINGS + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ OrderingColumns.ORDERING_ITEM_ID + " INTEGER NOT NULL,"
				+ OrderingColumns.ORDERING_LIST_ID + " INTEGER NOT NULL,"
				+ OrderingColumns.ORDERING_POSITION + " INTEGER NOT NULL,"
				+ "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");
*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.LISTS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ORDERINGS);
            onCreate(db);
        }
	}
}
