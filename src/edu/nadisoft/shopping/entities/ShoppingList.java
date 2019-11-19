package edu.nadisoft.shopping.entities;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

	private int id;
	private String name;
	private boolean needFilter;
	private List<ShoppingItem> items = new ArrayList<ShoppingItem>();

	public ShoppingList(int id, String name, boolean needFilter) {
		super();
		this.id = id;
		this.name = name;
		this.needFilter = needFilter;
	}

	public ShoppingList(ShoppingList shoppingList) {
		super();
		this.id = shoppingList.id;
		this.name = shoppingList.name;
		this.needFilter = shoppingList.needFilter;
		this.items = new ArrayList<ShoppingItem>(shoppingList.items);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean filterByNeed() {
		return needFilter;
	}

	public List<ShoppingItem> getItems() {
		return items;
	}

	public List<ShoppingItem> getFilteredItems() {
		if (filterByNeed()) {
			List<ShoppingItem> filteredItems = new ArrayList<ShoppingItem>();
			for (ShoppingItem item : getItems()) {
				if (item.isNeeded()) {
					filteredItems.add(item);
				}
			}
			return filteredItems;
		} else {
			return getItems();
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addAll(List<ShoppingItem> shoppingItems) {
		items.addAll(shoppingItems);
	}

	public void addShoppingItem(ShoppingItem item) {
		items.add(item);
	}
	
	public void setFilterByNeed(boolean needFilter){
		this.needFilter = needFilter;
	}

	public int indexOf(ShoppingItem item) {
		return items.indexOf(item);
	}

	public void moveItemDown(ShoppingItem item) {
		moveItem(item, 1);
	}

	public void moveItemUp(ShoppingItem item) {
		moveItem(item, -1);
	}

	private void moveItem(ShoppingItem item, int modifier) {
		int position = items.indexOf(item);
		items.remove(item);
		int newPosition = position + modifier;
		if ( newPosition < 0 ) {
			newPosition = items.size();
		}
		if ( newPosition > items.size() ) {
			newPosition = 0;
		}
		items.add(newPosition, item);
	}

	@Override
	public String toString() {
		return getName();
	}

}
