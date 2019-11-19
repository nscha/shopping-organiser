package com.nadisoft.shopping.organiser.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

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
    private static final int LISTS = 110; // all lists
    private static final int LISTS_ID = 111; // one list by id
    private static final int LIST_ITEMS = 120; // all items ordered by list
    private static final int LIST_ITEMS_MOVE = 121; // move item in list

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
        matcher.addURI(authority, "lists", LISTS);
        matcher.addURI(authority, "lists/*", LISTS_ID);
        matcher.addURI(authority, "list/#/items", LIST_ITEMS);
        matcher.addURI(authority, "list/#/items/from/#/to/#/move/#", LIST_ITEMS_MOVE);

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
	        case LIST_ITEMS:
	    		return Items.CONTENT_TYPE;
	        case ITEMS_ID:
	    		return Items.CONTENT_ITEM_TYPE;
	        case LISTS:
	    		return Lists.CONTENT_TYPE;
	        case LISTS_ID:
	    		return Lists.CONTENT_ITEM_TYPE;
	        case LIST_ITEMS_MOVE:
	        	return null;
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
        Uri extraUriToListenTo = null;
		switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	            qb.setTables(ShoppingDatabase.Tables.ITEMS);
	        	defaultProjection = ShoppingContract.Items.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingContract.Items.DEFAULT_SORT;
	            break;
	        case LIST_ITEMS:
	            qb.setTables(ShoppingContract.Items.DEFAULT_JOIN_TABLES);
	        	defaultProjection = ShoppingContract.Items.DEFAULT_JOIN_PROJECTION;
	        	defaultSortOrder = ShoppingContract.Items.DEFAULT_JOIN_SORT;
	        	qb.appendWhere(ShoppingContract.Orderings.ORDERING_LIST_ID + "=" + uri.getPathSegments().get(1));
	        	extraUriToListenTo = ShoppingContract.Items.buildItemsUri();
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
        if ( extraUriToListenTo != null ){
        	cursor.setNotificationUri(getContext().getContentResolver(), extraUriToListenTo);
        }
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
	            db.beginTransaction();
	        	rowId = db.insert(ShoppingDatabase.Tables.ITEMS, null, values);
	        	insertItemInAllLists(db,rowId);
	            db.setTransactionSuccessful();
	        	db.endTransaction();
	        	noteUri = ShoppingContract.Items.buildItemUri(rowId);
	            break;
	        case LISTS:
	            db.beginTransaction();
	        	rowId = db.insert(ShoppingDatabase.Tables.LISTS, null, values);
	        	arrangeOrderingsForNewList(db, rowId);
	            db.setTransactionSuccessful();
	        	db.endTransaction();
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

	@Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
	        case ITEMS_ID:
	            String itemId = uri.getPathSegments().get(1);
	            db.beginTransaction();
	            arrangeItemOrderingsForDelete(db, itemId);
	            count = db.delete(ShoppingDatabase.Tables.ITEMS,
	            		ShoppingContract.Items._ID + "=" + itemId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            db.delete(ShoppingDatabase.Tables.ORDERINGS,
	            		ShoppingContract.Orderings.ORDERING_ITEM_ID + "=" + itemId, null);
	            db.setTransactionSuccessful();
	        	db.endTransaction();
	            break;
	        case LISTS:
	            count = db.delete(ShoppingDatabase.Tables.LISTS, where, whereArgs);
	            break;
	        case LISTS_ID:
	            long listId = ContentUris.parseId(uri);
	            db.beginTransaction();
	            count = db.delete(ShoppingDatabase.Tables.LISTS,
	            		ShoppingContract.Lists._ID + "=" + listId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            arrangeOrderingsForRemoveList(db, listId);
	            db.setTransactionSuccessful();
	        	db.endTransaction();
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
        long itemId;
        String listId;
        Uri extraUriToNotify = null;
        switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	            count = db.update(ShoppingDatabase.Tables.ITEMS, values, where, whereArgs);
	            break;
	        case LIST_ITEMS_MOVE:
	        	listId = uri.getPathSegments().get(1);
	        	itemId = ContentUris.parseId(uri);
	        	String from = uri.getPathSegments().get(4);
	        	String to = uri.getPathSegments().get(6);
	        	db.beginTransaction();
	        	count = arrangeItemsForMove(db, listId, from, to);
	    		String moveItemSelection = ShoppingContract.Orderings.ORDERING_ITEM_ID + "= ?" +
	    		" AND " + ShoppingContract.Orderings.ORDERING_LIST_ID + "=?";
	    		String[] moveItemSelectionValue = new String[]{String.valueOf(itemId), listId};
	    		ContentValues newValue = new ContentValues();
	    		newValue.put(ShoppingContract.Orderings.ORDERING_POSITION, to);
	    		count += db.update(ShoppingDatabase.Tables.ORDERINGS, newValue, moveItemSelection, moveItemSelectionValue);
	        	db.setTransactionSuccessful();
	        	db.endTransaction();
	        	extraUriToNotify = ShoppingContract.Items.buildItemsUri();
	        	break;
	        case ITEMS_ID:
	            itemId = ContentUris.parseId(uri);
	            count = db.update(ShoppingDatabase.Tables.ITEMS, values,
	            		ShoppingContract.Items._ID + "=" + Long.toString(itemId) +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	        case LISTS:
	            count = db.update(ShoppingDatabase.Tables.LISTS, values, where, whereArgs);
	            break;
	        case LISTS_ID:
	            listId = uri.getPathSegments().get(1);
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

    private int insertItemInAllLists(SQLiteDatabase db, long newItemId){
    	int pos = getAvailablePosition(db);
    	Cursor cursor = db.query(ShoppingDatabase.Tables.LISTS, new String[]{ShoppingContract.Lists._ID}, null, null, null, null, null);
		ContentValues values = new ContentValues();
		values.put(ShoppingContract.Orderings.ORDERING_ITEM_ID, newItemId);
		values.put(ShoppingContract.Orderings.ORDERING_POSITION, pos);
		int count = 0;
    	while ( cursor.moveToNext() ){
    		long listId = cursor.getLong(0);
    		values.put(ShoppingContract.Orderings.ORDERING_LIST_ID, listId);
    		count += db.insert(ShoppingDatabase.Tables.ORDERINGS, null, values);
    	}
    	return count;
    }

    private void arrangeOrderingsForNewList(SQLiteDatabase db, long listId) {
		// insert into orderings (ordering_item_id, ordering_list_id, ordering_pos) select 
    	// ordering_item_id, 3, ordering_pos from orderings where ordering_list_id = 1
    	Cursor cursor = db.query(ShoppingDatabase.Tables.LISTS, new String[]{ShoppingContract.Lists._ID},
    			ShoppingContract.Lists.LIST_SETS_FILTER+"=1", null, null, null, null);
    	cursor.moveToFirst();
    	long baseListId = cursor.getLong(0);

    	StringBuilder sql = new StringBuilder(120);
		sql.append("INSERT INTO ");
        sql.append(ShoppingDatabase.Tables.ORDERINGS);
        sql.append("(" + ShoppingContract.Orderings.ORDERING_ITEM_ID + ",");
        sql.append(ShoppingContract.Orderings.ORDERING_LIST_ID + ",");
        sql.append(ShoppingContract.Orderings.ORDERING_POSITION + ")");
        sql.append(" SELECT ");
        sql.append(ShoppingContract.Orderings.ORDERING_ITEM_ID + ",");
        sql.append(listId);
        sql.append(","+ShoppingContract.Orderings.ORDERING_POSITION);
        sql.append(" FROM ");
        sql.append(ShoppingDatabase.Tables.ORDERINGS);
        sql.append(" WHERE ");
        sql.append(ShoppingContract.Orderings.ORDERING_LIST_ID + "=");
        sql.append(baseListId);

        db.execSQL(sql.toString());
	}

    private void arrangeOrderingsForRemoveList(SQLiteDatabase db, long listId) {
    	db.delete(ShoppingDatabase.Tables.ORDERINGS, 
    			ShoppingContract.Orderings.ORDERING_LIST_ID + "=?",
    			new String[]{Long.toString(listId)});
    }

    private int getAvailablePosition(SQLiteDatabase db) {
    	String[] projection = new String[] { "max("+ShoppingContract.Orderings.ORDERING_POSITION+")"}; 
		Cursor cursor = db.query(ShoppingDatabase.Tables.ORDERINGS, projection, null, null, null, null, null);
		cursor.moveToFirst();
		if (cursor.isNull(0)){
			return 0;
		}
		int pos = cursor.getInt(0) + 1;
		cursor.close();
		return pos;
	}

	private int arrangeItemsForMove(SQLiteDatabase db, String listId, String sFrom, String sTo) {
		int from = Integer.parseInt(sFrom);
		int to = Integer.parseInt(sTo);
		if ( from == to ){
			return 0;
		}
		StringBuilder selection = new StringBuilder(120);
		selection.append(ShoppingContract.Orderings.ORDERING_LIST_ID + "=" + listId + " AND ");
		String newValue;
		if ( from > to ) {
			selection.append(ShoppingContract.Orderings.ORDERING_POSITION + "< "+from+" AND "+
				ShoppingContract.Orderings.ORDERING_POSITION + ">= "+to);
			newValue = ShoppingContract.Orderings.ORDERING_POSITION +"+ 1";
		} else { // if ( from < to )
			selection.append(ShoppingContract.Orderings.ORDERING_POSITION + "> "+from+" AND "+
				ShoppingContract.Orderings.ORDERING_POSITION + "<= "+to);
			newValue = ShoppingContract.Orderings.ORDERING_POSITION +"- 1";
		}

		StringBuilder sql = new StringBuilder(120);
		sql.append("UPDATE ");
        sql.append(ShoppingDatabase.Tables.ORDERINGS);
        sql.append(" SET ");
        sql.append(ShoppingContract.Orderings.ORDERING_POSITION);
        sql.append("="+newValue);
        sql.append(" WHERE ");
        sql.append(selection);

        db.execSQL(sql.toString());

		return Math.abs(from - to);
	}

	private void arrangeItemOrderingsForDelete(SQLiteDatabase db, String itemId) {
		Cursor cursor = db.query(ShoppingDatabase.Tables.ORDERINGS,
				new String[]{ShoppingContract.Orderings.ORDERING_LIST_ID,ShoppingContract.Orderings.ORDERING_POSITION},
				ShoppingContract.Orderings.ORDERING_ITEM_ID + " = ?",
				new String[]{itemId}, null, null, null);
		StringBuilder selection = new StringBuilder(120);
		while( cursor.moveToNext() ){
			long listId = cursor.getLong(0);
			int pos = cursor.getInt(1);
			selection.append("(");
			selection.append(ShoppingContract.Orderings.ORDERING_POSITION + "> "+pos);
			selection.append(" AND ");
			selection.append(ShoppingContract.Orderings.ORDERING_LIST_ID + "= "+listId);
			selection.append(")");
			if ( !cursor.isLast() ){
				selection.append(" OR ");
			}
		}

		String newValue = ShoppingContract.Orderings.ORDERING_POSITION +"- 1";

		StringBuilder sql = new StringBuilder(120);
		sql.append("UPDATE ");
        sql.append(ShoppingDatabase.Tables.ORDERINGS);
        sql.append(" SET ");
        sql.append(ShoppingContract.Orderings.ORDERING_POSITION);
        sql.append("="+newValue);
        sql.append(" WHERE ");
        sql.append(selection);

        db.execSQL(sql.toString());
	}
}
