package com.nadisoft.shopping.organiser.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ShoppingContract {
	/**
     * The authority for app contents.
     */
    public static final String CONTENT_AUTHORITY = "com.nadisoft.shopping.organiser.provider";
    /**
     * Base URI to access provider's content. 
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Messages Path
     */
    private static final String PATH_ITEMS = "items";
    private static final String PATH_LISTS = "lists";
    private static final String PATH_LIST_ITEMS_1 = "list";
    private static final String PATH_LIST_ITEMS_2 = "items";
    private static final String PATH_LIST_ITEMS_3 = "from";
    private static final String PATH_LIST_ITEMS_4 = "to";
    private static final String PATH_LIST_ITEMS_5 = "move";

    interface MessageColumns {
		String MESSAGE_TEXT = "message_text";
	}

    interface ItemColumns {
		String ITEM_NAME = "item_name";
		String ITEM_NEEDED = "item_needed";
		String ITEM_BOUGHT = "item_bought";
	}

    interface ListColumns {
		String LIST_NAME = "list_name";
		String LIST_SETS_FILTER = "list_sets_filter";
	}

    interface OrderingColumns {
		String ORDERING_ITEM_ID = "ordering_item_id";
		String ORDERING_LIST_ID = "ordering_list_id";
		String ORDERING_POSITION = "ordering_pos";
	}

    public static class Items implements ItemColumns, BaseColumns {
    	/** Items content Uri. */
    	public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEMS).build();

    	/** Items content type */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cursoandroid.items";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cursoandroid.items";

		/** Default projection */
		public static final String[] DEFAULT_PROJECTION = new String[] {
				ShoppingContract.Items._ID, ShoppingContract.Items.ITEM_NAME,
				ShoppingContract.Items.ITEM_NEEDED, ShoppingContract.Items.ITEM_BOUGHT };
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";

		public static final String DEFAULT_JOIN_TABLES = 
				ShoppingDatabase.Tables.ITEMS + " JOIN " + ShoppingDatabase.Tables.ORDERINGS +
				" ON (" + ShoppingDatabase.Tables.ITEMS+"."+ShoppingContract.Items._ID + " = " +
				ShoppingContract.Orderings.ORDERING_ITEM_ID + ")"; 
		/** Default projection for joining with Orderings*/
		public static final String[] DEFAULT_JOIN_PROJECTION = new String[] {
				ShoppingDatabase.Tables.ITEMS+"."+ShoppingContract.Items._ID,
				ShoppingContract.Items.ITEM_NAME,
				ShoppingContract.Items.ITEM_NEEDED, ShoppingContract.Items.ITEM_BOUGHT,
				ShoppingContract.Orderings.ORDERING_POSITION,
				ShoppingContract.Orderings.ORDERING_LIST_ID};
        /** Default "ORDER BY" clause for joining with Orderings. */
        public static final String DEFAULT_JOIN_SORT = Orderings.ORDERING_POSITION + " ASC";

        /** Build {@link Uri} for request ordered items according to a list. */
        public static Uri buildListItemsUri(long listId) {
            return BASE_CONTENT_URI.buildUpon()
            	.appendPath(PATH_LIST_ITEMS_1)
            	.appendPath(Long.toString(listId))
            	.appendPath(PATH_LIST_ITEMS_2)
            	.build();
        }

        /** Build {@link Uri} for update ordering for items in a list. */
        public static Uri buildMoveListItemUri(long listId, long itemId, int from, int to) {
            return BASE_CONTENT_URI.buildUpon()
	        	.appendPath(PATH_LIST_ITEMS_1)
	        	.appendPath(Long.toString(listId))
	        	.appendPath(PATH_LIST_ITEMS_2)
	        	.appendPath(PATH_LIST_ITEMS_3)
	        	.appendPath(Integer.toString(from))
	        	.appendPath(PATH_LIST_ITEMS_4)
	        	.appendPath(Integer.toString(to))
	        	.appendPath(PATH_LIST_ITEMS_5)
	        	.appendPath(Long.toString(itemId))
	        	.build();
        }

    	/** Build {@link Uri} for reference to all items. */
        public static Uri buildItemsUri() {
            return CONTENT_URI.buildUpon().build();
        }

        /** Build {@link Uri} for requested message. */
        public static Uri buildItemUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }

    public static class Lists implements ListColumns, BaseColumns {
    	/** Lists content Uri. */
    	public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LISTS).build();
		/** Lists content type */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cursoandroid.lists";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cursoandroid.lists";

		/** Default projection */
		public static final String[] DEFAULT_PROJECTION = new String[] {
				ShoppingContract.Lists._ID, ShoppingContract.Lists.LIST_NAME,
				ShoppingContract.Lists.LIST_SETS_FILTER };
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = LIST_SETS_FILTER + " DESC, " +BaseColumns._ID + " ASC";

    	/** Build {@link Uri} for request all messages. */
        public static Uri buildListsUri() {
            return CONTENT_URI.buildUpon().build();
        }

        /** Build {@link Uri} for requested message. */
        public static Uri buildListUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }

    public static class Orderings implements OrderingColumns, BaseColumns {
    }
}
