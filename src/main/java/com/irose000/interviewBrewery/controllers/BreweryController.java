package com.irose000.interviewBrewery.controllers;

import com.irose000.interviewBrewery.models.*;
import com.irose000.interviewBrewery.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.converters.models.Sort;
import org.springframework.data.jpa.domain.Specification;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/breweries")
public class BreweryController {
	private final BreweryService breweryService;
	private final BreweryRepository repository;
	private static final String SEARCH_DESCRIPTION = "For inequality comparisons, append _gt or _lt to the attribute name."
			+ "\nList of available filters:"
			+ "		\nname"
			+ "		\ntype: []"
			+ "		\naddress"
			+ "		\ncity"
			+ "		\nstate_province"
			+ "		\nzipcode"
			+ "		\ncountry"
			+ "		\nphone"
			+ "		\nwebsite_url"
			+ "		\nby_dist";
	private static final String EXAMPLE_SEARCH = """
			{
			  "by_dist": "45.490507,-122.497291",
			  "type": "nano",
			  "per_page": "5"
			}
			""";
	
	/**
	 * An example location to use for searching by distance
	 */
	private static final String EXAMPLE_LOCATION = "45.490507,-122.497291";
	
	/**
	 * @param breweryService Auto-injected service
	 * @param repository Auto-injected repository
	 */
	public BreweryController(BreweryService breweryService, BreweryRepository repository) {
		this.breweryService = breweryService;
		this.repository = repository;
	}
	
	/**
	 * 
	 */
	@PostConstruct
	private void postConstruct() {
		log.info("attempting to preload database");
		this.breweryService.preLoadDatabase();
	}
	
	
	/**
	 * @return @List of all {@link Brewery} objects
	 */
	@GetMapping("")
	@Operation(summary = "Fetch all breweries")
	public ResponseEntity<List<Brewery>> getAll() {
		return new ResponseEntity<>(this.breweryService.getAll(), HttpStatus.OK);
	}
	
	/**
	 * @param coordinates Latitude and longitude together as one {@link String}
	 * @return {@link List} of {@link Brewery}, sorted by distance to given coordinates
	 */
	@GetMapping("?by_dist=")
	@Operation(summary = "Fetch breweries near me")
	public ResponseEntity<List<Brewery>> getByDist(@Parameter(description = "latitude,longitude", required = false, example = EXAMPLE_LOCATION) String coordinates) {
		return new ResponseEntity<>(this.breweryService.getByDistance(coordinates), HttpStatus.OK);
	}
	
	@GetMapping("/search")
	@Operation(summary = "Custom search with any combination of attributes.")
	public ResponseEntity<List<Brewery>> getCustom(@RequestParam @Parameter(description = SEARCH_DESCRIPTION, required = true, example = EXAMPLE_SEARCH) Map<String, String> params) {
		Specification<Brewery> spec = new EntitySpecification(params);
		List<Brewery> filteredResults = this.repository.findAll(spec);
		
		String latLong = params.get("by_dist");
		if (latLong != null) {
			List<Brewery> sortedByDistance = this.breweryService.getByDistance(latLong);
			
			List<Brewery> combinedResults = sortedByDistance.stream()
					.filter(filteredResults::contains)
					.collect(Collectors.toList());
			
			return new ResponseEntity<>(combinedResults, HttpStatus.OK);
		}
		return new ResponseEntity<>(filteredResults, HttpStatus.OK);
	}
	
}