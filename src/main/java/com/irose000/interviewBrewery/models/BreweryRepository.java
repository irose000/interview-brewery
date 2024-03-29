package com.irose000.interviewBrewery.models;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BreweryRepository extends JpaRepository<Brewery, String>, JpaSpecificationExecutor<Brewery> {
	@Query("SELECT b FROM Brewery b WHERE b.latitude IS NOT NULL AND b.longitude IS NOT NULL ORDER BY SQRT(POWER((?1 - b.latitude), 2) + POWER((?2 - b.longitude), 2)) ASC")
	List<Brewery> findByDistance(Double latitude, Double longitude);
	
	@Query("SELECT b FROM Brewery b WHERE b.latitude IS NOT NULL AND b.longitude IS NOT NULL ORDER BY SQRT(POWER((?1 - b.latitude), 2) + POWER((?2 - b.longitude), 2)) ASC")
	Page<Brewery> findByDistance(Double latitude, Double longitude, Pageable pageable);
	
	List<Brewery> findByType(String type);
	
	List<Brewery> findByName(String name);
	
	List<Brewery> findByCity(String city);
	
	List<Brewery> findByPostalCode(String postal_code);
	
	@Query("SELECT b FROM Brewery b WHERE b.type = ?1 ORDER BY SQRT(POWER((?2 - b.latitude), 2) + POWER((?3 - b.longitude), 2)) ASC LIMIT 50")
	List<Brewery> findByTypeAndDistance(String type, Double latitude, Double longitude);

	List<Brewery> findByTypeAndCity(String type, String city);
}