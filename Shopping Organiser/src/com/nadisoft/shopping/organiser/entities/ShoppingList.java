package com.nadisoft.shopping.organiser.entities;

public class ShoppingList extends BaseEntity{

	private String name;
	private boolean setsFilter;

	public ShoppingList(String name, boolean setsFilter) {
		super();
		this.name = name;
		this.setsFilter = setsFilter;
	}

	public ShoppingList(ShoppingList shoppingList) {
		super();
		this.name = shoppingList.name;
		this.setsFilter = shoppingList.setsFilter;
	}

	public String getName() {
		return name;
	}

	public boolean setsFilter() {
		return setsFilter;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFilterByNeed(boolean needFilter){
		this.setsFilter = needFilter;
	}

	@Override
	public String toString() {
		return getName();
	}

}
