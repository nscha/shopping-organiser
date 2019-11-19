package com.nadisoft.shopping.organiser.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.nadisoft.shopping.organiser.provider.ShoppingOrganiserContract.Items;
import com.nadisoft.shopping.organiser.provider.ShoppingOrganiserContract.Lists;

public class MyProvider extends ContentProvider {
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
    private static final int ITEMS_LIST_ID = 121; // items on a list by list id

    /**
     * Local DB Helper
     */
    private MyDatabase mOpenHelper;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ShoppingOrganiserContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "items", ITEMS);
        matcher.addURI(authority, "items/*", ITEMS_ID);
/*
        matcher.addURI(authority, "lists", LISTS);
        matcher.addURI(authority, "lists/*", LISTS_ID);
        matcher.addURI(authority, "items/list/*", ITEMS_LIST_ID);
*/
        return matcher;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MyDatabase(getContext());
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
/*
	        case LISTS:
	    		return Lists.CONTENT_TYPE;
	        case LISTS_ID:
	    		return Lists.CONTENT_ITEM_TYPE;
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
	            qb.setTables(MyDatabase.Tables.ITEMS);
	        	defaultProjection = ShoppingOrganiserContract.Items.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingOrganiserContract.Items.DEFAULT_SORT;
	            break;
	        case ITEMS_ID:
	            qb.setTables(MyDatabase.Tables.ITEMS);
	        	defaultProjection = ShoppingOrganiserContract.Items.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingOrganiserContract.Items.DEFAULT_SORT;
	        	qb.appendWhere(ShoppingOrganiserContract.Items._ID + "=" + uri.getPathSegments().get(1));
	            break;
/*
	        case LISTS:
	            qb.setTables(MyDatabase.Tables.LISTS);
	        	defaultProjection = ShoppingOrganiserContract.Lists.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingOrganiserContract.Lists.DEFAULT_SORT;
	            break;
	        case LISTS_ID:
	            qb.setTables(MyDatabase.Tables.LISTS);
	        	defaultProjection = ShoppingOrganiserContract.Lists.DEFAULT_PROJECTION;
	        	defaultSortOrder = ShoppingOrganiserContract.Lists.DEFAULT_SORT;
	        	qb.appendWhere(ShoppingOrganiserContract.Lists._ID + "=" + uri.getPathSegments().get(1));
	            break;
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
	        	rowId = db.insert(MyDatabase.Tables.ITEMS, null, values);
	        	noteUri = ShoppingOrganiserContract.Items.buildItemUri(rowId);
	            break;
	        case LISTS:
	        	rowId = db.insert(MyDatabase.Tables.LISTS, null, values);
	        	noteUri = ShoppingOrganiserContract.Lists.buildListUri(rowId);
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
	        case ITEMS:
	            count = db.delete(MyDatabase.Tables.ITEMS, where, whereArgs);
	            break;
	        case ITEMS_ID:
	            String messageId = uri.getPathSegments().get(1);
	            count = db.delete(MyDatabase.Tables.ITEMS,
	            		ShoppingOrganiserContract.Items._ID + "=" + messageId +
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
        switch (sUriMatcher.match(uri)) {
	        case ITEMS:
	            count = db.update(MyDatabase.Tables.ITEMS, values, where, whereArgs);
	            break;
	        case ITEMS_ID:
	            String messageId = uri.getPathSegments().get(1);
	            count = db.update(MyDatabase.Tables.ITEMS, values,
	            		ShoppingOrganiserContract.Items._ID + "=" + messageId +
	            		(!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
