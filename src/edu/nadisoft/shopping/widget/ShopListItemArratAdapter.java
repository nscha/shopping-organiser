package edu.nadisoft.shopping.widget;

import java.util.List;

import android.content.Context;
import edu.nadisoft.shopping.entities.ShoppingItem;

public class ShopListItemArratAdapter extends ShoppingItemArrayAdapter {

	public ShopListItemArratAdapter(Context context, int textViewResourceId,
			List<ShoppingItem> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean itemShouldBeChecked(ShoppingItem item) {
		return item.isBought();
	}

}
