package com.irose000.interviewBrewery.services;

import com.irose000.interviewBrewery.models.*;
import org.springdoc.core.converters.models.Sort;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.reflect.Field;

@Service
@Slf4j
public class BreweryService {
	private final RestTemplate restTemplate = new RestTemplate();
	private final String BREWERY_SEARCH = "https://api.openbrewerydb.org/v1/breweries";
	@Autowired
	private BreweryRepository repository;
	
	public BreweryService() {
	}
	
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
	public List<Brewery> getAll() {
		return this.repository.findAll();
	}
	
	/**
	 * Fetches a sorted list of breweries, starting with the closest to the given location.
	 * 
	 * @param coordinates Latitude and longitude together as a comma-separated {@link String}
	 * @return {@link List} of {@link Brewery} ordered by distance to the given coordinates
	 */
	public List<Brewery> getByDistance(String coordinates) {
		String[] coords = coordinates.split(",");
		Double latitude = Double.parseDouble(coords[0].trim());
		Double longitude = Double.parseDouble(coords[1].trim());
		
		return this.repository.findByDistance(latitude, longitude);
	}
}