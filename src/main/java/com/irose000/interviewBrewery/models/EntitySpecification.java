package com.irose000.interviewBrewery.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.Entity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class EntitySpecification implements Specification<Brewery> {
	private Map<String, String> params;
	
	public EntitySpecification(Map<String, String> params) {
		// TODO: sanitize and validate input
		this.params = params;
	}
	
	@Override
	public Predicate toPredicate(Root<Brewery> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		List<Predicate> predicates = new ArrayList<>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			// Ignore the calculated value filter/sort, this is handled separately by the controller
			if (entry.getKey().contains("by_dist")) {
				continue;
			}
			
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.endsWith("_gt")) {
				String field = key.substring(0, key.length() - 3); 
				predicates.add(criteriaBuilder.greaterThan(root.get(field), value));
			} else if (key.endsWith("_lt")) {
				String field = key.substring(0, key.length() - 3);
				predicates.add(criteriaBuilder.lessThan(root.get(field), value));
			} else {
				predicates.add(criteriaBuilder.equal(root.get(key), value));
			}
		}
		return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
	}
}
