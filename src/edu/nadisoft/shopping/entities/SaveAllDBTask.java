package edu.nadisoft.shopping.entities;

import java.util.List;

import android.os.AsyncTask;
import edu.nadisoft.shopping.ShoppingListApplication;
import edu.nadisoft.shopping.db.DBHelper;

public class SaveAllDBTask extends AsyncTask<List<ShoppingList>, Integer, Long> {

	@Override
	protected Long doInBackground(List<ShoppingList>... params) {
		List<ShoppingList> lists = params[0];
		DBHelper db = new DBHelper(ShoppingListApplication.getContext());
		db.save(lists);
		return null;
	}

}