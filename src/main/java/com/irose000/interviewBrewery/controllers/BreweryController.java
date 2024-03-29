package com.irose000.interviewBrewery.controllers;

import com.irose000.interviewBrewery.models.*;
import com.irose000.interviewBrewery.services.*;
import com.irose000.interviewBrewery.utilities.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/breweries")
public class BreweryController {
	private final BreweryService breweryService;
	private final BreweryRepository repository;
	private static final String SEARCH_DESCRIPTION = "List of available filters:"
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
			  "type": "micro"
			}
			""";
	private static final String EXAMPLE_PAGE = "{\"page\": 0, \"size\": 10}";
	
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
	public ResponseEntity<?> getAll(Pageable pageable) {
		return new ResponseEntity<>(this.breweryService.getAll(pageable), HttpStatus.OK);
	}
	
	/**
	 * @param coordinates Latitude and longitude together as one {@link String}
	 * @return {@link List} of {@link Brewery}, sorted by distance to given coordinates
	 */
	@GetMapping("?by_dist=")
	@Operation(summary = "Fetch breweries near me")
	public ResponseEntity<?> getByDist(@Parameter(description = "latitude,longitude", required = false, example = EXAMPLE_LOCATION) String coordinates, Pageable pageable) {
		return new ResponseEntity<>(this.breweryService.getByDistance(coordinates, pageable), HttpStatus.OK);
	}
	
	@GetMapping("/search")
	@Operation(summary = "Custom search with any combination of attributes.")
	public ResponseEntity<?> getCustom(
			@RequestParam 
			@Parameter(description = SEARCH_DESCRIPTION, example = EXAMPLE_SEARCH) 
			Map<String, String> params, 
			@PageableDefault(page = 0, size = 10)
			@Parameter(name = "pageable", description = "Pagination parameters", example = EXAMPLE_PAGE) 
			Pageable pageable) {
		
		// For some reason Spring is not automatically separating params from pageable
		params.remove("page");
		params.remove("size");
		params.remove("sort");
		
		if (!this.breweryService.areValidEntityFields(Brewery.class, params)) {
			return ResponseEntity.badRequest().body("Invalid field names in filters");
		}
		Specification<Brewery> spec = new EntitySpecification(params);
		List<Brewery> filteredResults = this.repository.findAll(spec);
		
		String latLong = params.get("by_dist");
		if (latLong != null) {
			List<Brewery> sortedByDistance = this.breweryService.getByDistance(latLong);
			if (sortedByDistance == null) {
				return ResponseEntity.badRequest().body("Invalid coordinates provided for latitude and/or longitude");
			}
			
			List<Brewery> combinedResults = sortedByDistance.stream()
					.filter(filteredResults::contains)
					.collect(Collectors.toList());
			
			// manually get page and set pagination metadata, since we can't automatically get valid pagination data when post-processing or combining two repository queries
			List<Brewery> paginatedCombinedResults = PaginationUtility.getPage(combinedResults, pageable.getPageNumber(), pageable.getPageSize());
			Page<Brewery> onePage = new PageImpl<>(
					paginatedCombinedResults,
					PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
					paginatedCombinedResults.size()
			);
			
			return new ResponseEntity<>(onePage, HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body("Invalid coordinates for distance sorting");
		}
	}
	
}