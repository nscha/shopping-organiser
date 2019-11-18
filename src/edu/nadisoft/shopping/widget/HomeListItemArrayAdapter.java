package edu.nadisoft.shopping.widget;

import java.util.List;

import android.content.Context;
import edu.nadisoft.shopping.entities.ShoppingItem;

public class HomeListItemArrayAdapter extends ShoppingItemArrayAdapter {

	public HomeListItemArrayAdapter(Context context, int textViewResourceId,
			List<ShoppingItem> objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	protected boolean itemShouldBeChecked(ShoppingItem item) {
		return item.isNeeded();
	}

}
