package com.nadisoft.shopping.organiser.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ShoppingOrganiserContract {
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
    //private static final String PATH_MESSAGES = "messages";
    private static final String PATH_ITEMS = "items";
    private static final String PATH_LISTS = "lists";
    private static final String PATH_ORDERINGS = "orderings";

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
/*
    public static class Messages implements MessageColumns, BaseColumns {
    	// Messages content Uri.
    	public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();
		// Messages content type
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cursoandroid.messages";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cursoandroid.messages";
		
		// Default projection
		public static final String[] DEFAULT_PROJECTION = new String[] {
				ShoppingOrganiserContract.Messages._ID, ShoppingOrganiserContract.Messages.MESSAGE_TEXT };
        // Default "ORDER BY" clause.
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";
    	
    	// Build {@link Uri} for request all messages.
        public static Uri buildMessagesUri() {
            return CONTENT_URI.buildUpon().build();
        }

        // Build {@link Uri} for requested message.
        public static Uri buildMessageUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }
*/
    public static class Items implements ItemColumns, BaseColumns {
    	/** Items content Uri. */
    	public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEMS).build();
		/** Items content type */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cursoandroid.items";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cursoandroid.items";

		/** Default projection */
		public static final String[] DEFAULT_PROJECTION = new String[] {
				ShoppingOrganiserContract.Items._ID, ShoppingOrganiserContract.Items.ITEM_NAME,
				ShoppingOrganiserContract.Items.ITEM_NEEDED, ShoppingOrganiserContract.Items.ITEM_BOUGHT };
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";

    	/** Build {@link Uri} for request all messages. */
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
				ShoppingOrganiserContract.Lists._ID, ShoppingOrganiserContract.Lists.LIST_NAME,
				ShoppingOrganiserContract.Lists.LIST_SETS_FILTER };
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";

    	/** Build {@link Uri} for request all messages. */
        public static Uri buildListsUri() {
            return CONTENT_URI.buildUpon().build();
        }

        /** Build {@link Uri} for requested message. */
        public static Uri buildListUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }

    /** chequear si se usa! */
    public static class Orderings implements OrderingColumns, BaseColumns {
    	/** Orderings content Uri. */
    	public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORDERINGS).build();
		/** Orderings content type */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cursoandroid.orderings";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cursoandroid.orderings";

		/** Default projection */
		public static final String[] DEFAULT_PROJECTION = new String[] {
				ShoppingOrganiserContract.Orderings._ID, ShoppingOrganiserContract.Items.ITEM_NAME,
				ShoppingOrganiserContract.Items.ITEM_NEEDED, ShoppingOrganiserContract.Items.ITEM_BOUGHT, 
				ShoppingOrganiserContract.Orderings.ORDERING_POSITION };
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ORDERING_POSITION + " ASC";

    	/** Build {@link Uri} for request all messages. */
        public static Uri buildOrderingsUri() {
            return CONTENT_URI.buildUpon().build();
        }

        /** Build {@link Uri} for requested message. */
        public static Uri buildOrderingUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }
}
