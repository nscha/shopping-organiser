package edu.nadisoft.shopping.widget;

import java.util.List;

import edu.nadisoft.shopping.entities.ShoppingItem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

abstract public class ShoppingItemArrayAdapter extends ArrayAdapter<ShoppingItem> {

	public ShoppingItemArrayAdapter(Context context, int textViewResourceId,
			List<ShoppingItem> objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		ShoppingItem item = getItem(position);
		if ( view instanceof CheckedTextView ){
			((CheckedTextView)view).setChecked(itemShouldBeChecked(item));
		}
		return view;
	}

	abstract protected boolean itemShouldBeChecked(ShoppingItem item);

}
