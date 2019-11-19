package com.nadisoft.shopping.organiser.entities;

public class ShoppingList extends BaseEntity{

	private String name;
	private boolean setsFilter;
	//private List<ShoppingItem> items = new ArrayList<ShoppingItem>();

	public ShoppingList(String name, boolean setsFilter) {
		super();
		this.name = name;
		this.setsFilter = setsFilter;
	}

	public ShoppingList(ShoppingList shoppingList) {
		super();
		this.name = shoppingList.name;
		this.setsFilter = shoppingList.setsFilter;
		//this.items = new ArrayList<ShoppingItem>(shoppingList.items);
	}

	public String getName() {
		return name;
	}

	public boolean setsFilter() {
		return setsFilter;
	}
/*
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
*/
	public void setName(String name) {
		this.name = name;
	}
/*
	public void addAll(List<ShoppingItem> shoppingItems) {
		items.addAll(shoppingItems);
	}

	public void addShoppingItem(ShoppingItem item) {
		items.add(item);
	}
*/
	public void setFilterByNeed(boolean needFilter){
		this.setsFilter = needFilter;
	}
/*
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
*/
	@Override
	public String toString() {
		return getName();
	}

}
