package com.nadisoft.shopping.organiser.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.nadisoft.shopping.organiser.provider.ShoppingContract.Items;
import com.nadisoft.shopping.organiser.provider.ShoppingContract.Lists;

public class ShoppingProvider extends ContentProvider {
	/**
     * {@link UriMatcher} to determine what is requested to this {@link ContentProvider}.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    /**
     * URI IDs to match
     */
    private static final int ITEMS = 100; // all items
    private static final int ITEMS_ID = 101; // one item by id
    private static final int ITEMS_MOVE = 102; // move an item
    private static final int LISTS = 110; // all lists
    private static final int LISTS_ID = 111; // one list by id
    private static final int ITEMS_LIST_ID = 121; // items on a list by list id

    /**
     * Local DB Helper
     */
    private ShoppingDatabase mOpenHelper;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ShoppingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "items", ITEMS);
        matcher.addURI(authority, "items/*", ITEMS_ID);
        matcher.addURI(authority, "items/*/*/*", ITEMS_MOVE);
        matcher.addURI(authority, "lists", LISTS);
        matcher.addURI(authority, "lists/*", LISTS_ID);
/*
        matcher.addURI(authority, "items/list/*", ITEMS_LIST_ID);
*/
        return matcher;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        mOpenHelper = new ShoppingDatabase(getContext());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
	        case ITEMS:
	    		return Items.CONTENT_TYPE;
	        case ITEMS_ID:
	    		return Items.CONTENT_ITEM_TYPE;
	        case LISTS:
	    		return Lists.CONTENT_TYPE;
	        case LISTS_ID:
	    		return Lists.CONTENT_ITEM_TYPE;
/*
	        case ITEMS_LIST_ID:
	    		return Items.CONTENT_TYPE;
*/
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] defaultProjection;
        String defaultSortOrder;        
        switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	            qb.setTables(ShoppingDatabase.Tables.ITEMS);
	        	defaultProjection = ShoppingContract.Items.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingContract.Items.DEFAULT_SORT;
	            break;
	        case ITEMS_ID:
	            qb.setTables(ShoppingDatabase.Tables.ITEMS);
	        	defaultProjection = ShoppingContract.Items.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingContract.Items.DEFAULT_SORT;
	        	qb.appendWhere(ShoppingContract.Items._ID + "=" + uri.getPathSegments().get(1));
	            break;
	        case LISTS:
	            qb.setTables(ShoppingDatabase.Tables.LISTS);
	        	defaultProjection = ShoppingContract.Lists.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingContract.Lists.DEFAULT_SORT;
	            break;
	        case LISTS_ID:
	            qb.setTables(ShoppingDatabase.Tables.LISTS);
	        	defaultProjection = ShoppingContract.Lists.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingContract.Lists.DEFAULT_SORT;
	        	qb.appendWhere(ShoppingContract.Lists._ID + "=" + uri.getPathSegments().get(1));
	            break;
/*
	        case ITEMS_LIST_ID:
	            qb.setTables(MyDatabase.Tables.ITEMS+", "+MyDatabase.Tables.LISTS+", "+MyDatabase.Tables.ORDERINGS);
	            //setTables("foo, bar") setTables("foo LEFT OUTER JOIN bar ON (foo.id = bar.foo_id)")
	        	defaultProjection = ShoppingOrganiserContract.Orderings.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingOrganiserContract.Orderings.DEFAULT_SORT;
	        	qb.appendWhere(ShoppingOrganiserContract.Orderings.ORDERING_ITEM_ID + "=" + ShoppingOrganiserContract.Items._ID);
	            qb.appendWhere(ShoppingOrganiserContract.Orderings.ORDERING_LIST_ID + "=" + uri.getPathSegments().get(2));
	        	break;
*/
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no projection is specified use the default.
        String[] queryProjection;
        if (projection == null) {
        	queryProjection = defaultProjection;
        } else {
        	queryProjection = projection;
        }

        // If no sort order is specified use the default
        String querySortOrder;
        if (TextUtils.isEmpty(sortOrder)) {
        	querySortOrder = defaultSortOrder;
        } else {
        	querySortOrder = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor cursor = qb.query(db, queryProjection, selection, selectionArgs,
				null, null, querySortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != ITEMS && sUriMatcher.match(uri) != LISTS ) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId;
        Uri noteUri;
        switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	        	Integer intendedPosition = values.getAsInteger(ShoppingContract.Items.ITEM_TMP_POSITION);
	        	if (intendedPosition == null) { //DELME
	        		values.put(ShoppingContract.Items.ITEM_TMP_POSITION, getAvailablePosition(db));
	        	}
	        	rowId = db.insert(ShoppingDatabase.Tables.ITEMS, null, values);
	        	noteUri = ShoppingContract.Items.buildItemUri(rowId);
	            break;
	        case LISTS:
	        	rowId = db.insert(ShoppingDatabase.Tables.LISTS, null, values);
	        	noteUri = ShoppingContract.Lists.buildListUri(rowId);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowId > 0) {
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    private int getAvailablePosition(SQLiteDatabase db) { //DELME
    	String[] projection = new String[] { "max("+ShoppingContract.Items.ITEM_TMP_POSITION+")"}; 
		Cursor cursor = db.query(ShoppingDatabase.Tables.ITEMS, projection, null, null, null, null, null);
		cursor.moveToFirst();
		if (cursor.isNull(0)){
			Log.d("NADIA","EMPTY -> available position "+0);
			return 0;
		}
		int pos = cursor.getInt(0) + 1;
		cursor.close();
		Log.d("NADIA","available position "+pos);
		return pos;
	}

	@Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	            count = db.delete(ShoppingDatabase.Tables.ITEMS, where, whereArgs);
	            break;
	        case ITEMS_ID:
	            String itemId = uri.getPathSegments().get(1);
	            Cursor cursor = query(uri,null,null,null,null);
	            cursor.moveToFirst();
	            int pos = cursor.getInt(cursor.getColumnIndex(ShoppingContract.Items.ITEM_TMP_POSITION));
	            count = db.delete(ShoppingDatabase.Tables.ITEMS,
	            		ShoppingContract.Items._ID + "=" + itemId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            arrangeItemsForDelete(db, pos);
	            break;
	        case LISTS:
	            count = db.delete(ShoppingDatabase.Tables.LISTS, where, whereArgs);
	            break;
	        case LISTS_ID:
	            String listId = uri.getPathSegments().get(1);
	            count = db.delete(ShoppingDatabase.Tables.LISTS,
	            		ShoppingContract.Lists._ID + "=" + listId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        Uri extraUriToNotify = null;
        switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	        	Log.i("NADIA","Provider Update on ShoppingDatabase.Tables.ITEMS SET:"+values+" WHERE:"+where+" wA:"+Arrays.toString(whereArgs));
	            count = db.update(ShoppingDatabase.Tables.ITEMS, values, where, whereArgs);
	            break;
	        case ITEMS_MOVE:
	        	Log.i("NADIA","ITEMS_MOVE");
	        	String id = uri.getPathSegments().get(1);
	        	String from = uri.getPathSegments().get(2);
	        	String to = uri.getPathSegments().get(3);
	        	db.beginTransaction();
	        	count = arrangeItemsForMove(db, from,to);
	    		String moveItemSelection = ShoppingContract.Items._ID + "= ?";
	    		String[] moveItemSelectionValue = new String[]{String.valueOf(id)};
	    		ContentValues newValue = new ContentValues();
	    		newValue.put(ShoppingContract.Items.ITEM_TMP_POSITION, to);
	    		Log.i("NADIA","ITEMS_MOVE MOVE: id"+id+" to "+to);
	    		count += db.update(ShoppingDatabase.Tables.ITEMS, newValue, moveItemSelection, moveItemSelectionValue);
	        	db.setTransactionSuccessful();
	        	db.endTransaction();
	        	extraUriToNotify = ShoppingContract.Items.buildItemsUri();
	        	break;
	        case ITEMS_ID:
	            String itemId = uri.getPathSegments().get(1);
	            count = db.update(ShoppingDatabase.Tables.ITEMS, values,
	            		ShoppingContract.Items._ID + "=" + itemId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	        case LISTS:
	            count = db.update(ShoppingDatabase.Tables.LISTS, values, where, whereArgs);
	            break;
	        case LISTS_ID:
	            String listId = uri.getPathSegments().get(1);
	            count = db.update(ShoppingDatabase.Tables.LISTS, values,
	            		ShoppingContract.Lists._ID + "=" + listId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        if ( extraUriToNotify != null ){
        	getContext().getContentResolver().notifyChange(extraUriToNotify, null);
        }
        return count;
    }

	private int arrangeItemsForMove(SQLiteDatabase db, String sFrom, String sTo) {
		int from = Integer.parseInt(sFrom);
		int to = Integer.parseInt(sTo);
		if ( from == to ){
			return 0;
		}
		String selection;
		String newValue;
		if ( from > to ) {
			selection = ShoppingContract.Items.ITEM_TMP_POSITION + "< "+from+" AND "+
				ShoppingContract.Items.ITEM_TMP_POSITION + ">= "+to;
			newValue = ShoppingContract.Items.ITEM_TMP_POSITION +"+ 1";
		} else { // if ( from < to )
			selection = ShoppingContract.Items.ITEM_TMP_POSITION + "> "+from+" AND "+
				ShoppingContract.Items.ITEM_TMP_POSITION + "<= "+to;
			newValue = ShoppingContract.Items.ITEM_TMP_POSITION +"- 1";
		}

		StringBuilder sql = new StringBuilder(120);
		sql.append("UPDATE ");
        sql.append(ShoppingDatabase.Tables.ITEMS);
        sql.append(" SET ");
        sql.append(ShoppingContract.Items.ITEM_TMP_POSITION);
        sql.append("="+newValue);
        sql.append(" WHERE ");
        sql.append(selection);

        db.execSQL(sql.toString());

        Log.i("NADIA","ITEMS_MOVE SQL: " + sql.toString());

		return Math.abs(from - to);
	}

	private void arrangeItemsForDelete(SQLiteDatabase db, int pos) {
		String selection = ShoppingContract.Items.ITEM_TMP_POSITION + "> "+pos;
		String newValue = ShoppingContract.Items.ITEM_TMP_POSITION +"- 1";

		StringBuilder sql = new StringBuilder(120);
		sql.append("UPDATE ");
        sql.append(ShoppingDatabase.Tables.ITEMS);
        sql.append(" SET ");
        sql.append(ShoppingContract.Items.ITEM_TMP_POSITION);
        sql.append("="+newValue);
        sql.append(" WHERE ");
        sql.append(selection);

        db.execSQL(sql.toString());
        
        Log.i("NADIA","ITEMS_DEL SQL: " + sql.toString());
	}
}
