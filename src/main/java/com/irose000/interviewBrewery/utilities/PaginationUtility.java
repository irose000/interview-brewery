package com.irose000.interviewBrewery.utilities;

import java.util.List;
import java.util.Collections;

public class PaginationUtility {
	public static <T> List<T> getPage(List<T> sourceList, int page, int size) {
		if (sourceList == null || sourceList.isEmpty()) {
			return Collections.emptyList();
		}
		
		int totalItems = sourceList.size();
		int fromIndex = page * size;
		if (fromIndex >= totalItems) {
			return Collections.emptyList();
		}
		
		int toIndex = Math.min(fromIndex + size, totalItems);
		return sourceList.subList(fromIndex, toIndex);
	}
}
