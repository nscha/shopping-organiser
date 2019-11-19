package com.nadisoft.shopping.organiser.entities;

import java.io.Serializable;

public class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1596450066696262923L;

	private long _id;

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		this._id = id;
	}
	
}
