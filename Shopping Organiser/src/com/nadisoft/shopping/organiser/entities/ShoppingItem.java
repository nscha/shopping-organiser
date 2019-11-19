package com.nadisoft.shopping.organiser.entities;

public class ShoppingItem extends BaseEntity {
	private static final long serialVersionUID = 3062881872777235580L;

	private String name;
	private boolean needed;
	private boolean bought;

	public ShoppingItem(String name) {
		super();
		this.name = name;
	}

	public ShoppingItem(String name, boolean needed, boolean bought) {
		super();
		this.name = name;
		this.needed = needed;
		this.bought = bought;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public boolean isNeeded() {
		return needed;
	}

	public boolean isBought() {
		return bought;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNeeded(boolean needed) {
		this.needed = needed;
	}

	public void setBought(boolean bought) {
		this.bought = bought;
	}

}
