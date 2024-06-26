package com.irose000.interviewBrewery.services;

import com.irose000.interviewBrewery.models.*;
import com.irose000.interviewBrewery.utilities.PaginationUtility;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.reflect.Field;

/**
 * 
 */
/**
 * 
 */
@Service
@Slf4j
public class BreweryService {
	private final RestTemplate restTemplate = new RestTemplate();
	private final String BREWERY_SEARCH = "https://api.openbrewerydb.org/v1/breweries";
	@Autowired
	private BreweryRepository repository;
	
	public BreweryService() {
	}
	
	
	/**
	 * Using openbrewerydb's documented max page size of 200, pull every page from the remote database.
	 * This method is called on init from the {@link BreweryController} via a "@PostConstruct" tag
	 */
	public void preLoadDatabase() {
		int counter = 1;
		String page = "?page=%d&per_page=200";
		BreweryDTO[] breweries;
		do {
			String url = String.format(BREWERY_SEARCH + page, counter++);
			breweries = this.getAllRemote(url);
			//log.info("fetched {} breweries from remote db", breweries.length);
			for (BreweryDTO b : breweries) {
				repository.save(BreweryDTO.mapDtoToEntity(b));
			}
			log.info("Found and saved {} breweries to local repository", breweries.length);
		} while (breweries.length > 0);
	}
	
	/**
	 * Fetches breweries from the remote database for local caching. The limit set by the endpoint is currently 200, so this method will be called repeatedly with a pagination modified URL.
	 * 
	 * @param url {@link String} url of the remote db
	 * @return {@link Brewery[]}
	 */
	public BreweryDTO[] getAllRemote(String url) {
		//log.info("fetching all breweries from remote db");
		return this.restTemplate.getForObject(url, BreweryDTO[].class);
	}
	
	/**
	 * Fetches all {@link Brewery} objects from the local repository. 
	 * 
	 * @return {@link List} of all {@link Brewery} objects
	 */
	public Page<Brewery> getAll(Pageable pageable) {
		return this.repository.findAll(pageable);
	}
	
	/**
	 * Fetches a sorted list of breweries, starting with the closest to the given location.
	 * 
	 * @param coordinates Latitude and longitude together as a comma-separated {@link String}
	 * @return {@link List} of {@link Brewery} ordered by distance to the given coordinates
	 */
	public List<Brewery> getByDistance(String coordinates) {
		String[] coords = coordinates.split(",");
		try {
			Double latitude = Double.parseDouble(coords[0].trim());
			Double longitude = Double.parseDouble(coords[1].trim());
			return this.repository.findByDistance(latitude, longitude);
		} catch (NumberFormatException e) {
			log.info("Error: unable to process non-numeric coordinate values");
			return null;
		}
	}
	
	/**
	 * Fetches a page from a sorted list of breweries, starting with the closest to the given location.
	 * 
	 * @param coordinates Latitude and longitude together as a comma-separated {@link String}
	 * @return {@link List} of {@link Brewery} ordered by distance to the given coordinates
	 */
	public Page<Brewery> getByDistance(String coordinates, Pageable pageable) {
		String[] coords = coordinates.split(",");
		try {
			Double latitude = Double.parseDouble(coords[0].trim());
			Double longitude = Double.parseDouble(coords[1].trim());
			
			List<Brewery> allResults = this.repository.findByDistance(latitude, longitude);
			
			List<Brewery> onePageList = PaginationUtility.getPage(allResults, pageable.getPageNumber(), pageable.getPageSize());
			Page<Brewery> onePage = new PageImpl<>(
					onePageList,
					PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted()),
					onePageList.size()
			);
			
			return onePage;
		} catch (NumberFormatException e) {
			log.info("Error: unable to process non-numeric coordinate values");
			return null;
		}
	}
	
	/**
	 * Helper method to validate that user-supplied filters match field names
	 * 
	 * @param entityClass The entity class whose fields will be matched
	 * @param params A {@link Map} of {@link String}, {@link String} where the keys are field names of entityClass and the values are field values to match
	 * @return boolean
	 */
	public boolean areValidEntityFields(Class<?> entityClass, Map<String, String> params) {
		// debug
		for (String field : params.keySet()) {
			System.out.println("params key: " + field);
		}
		
		Set<String> entityFields = Stream.of(entityClass.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toSet());
		
		Set<String> allowedSpecialKeys = Set.of("by_dist");
		
		return params.keySet().stream()
				.allMatch(key -> entityFields.contains(key) || allowedSpecialKeys.contains(key));
	}
}