package com.irose000.interviewBrewery.models;

public enum BreweryType {
	MICRO("micro"),
	NANO("nano"),
	REGIONAL("regional"),
	BREWPUB("brewpub"),
	LARGE("large"),
	PLANNING("planning"),
	BAR("bar"),
	CONTRACT("contract"),
	PROPRIETOR("proprietor"),
	CLOSED("closed");
	
	public final String label;
	
	BreweryType(String label) {
		this.label = label;
	}
}