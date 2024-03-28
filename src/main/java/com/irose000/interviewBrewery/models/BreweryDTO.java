package com.irose000.interviewBrewery.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
public class BreweryDTO {
	private String id;
	private String name;
	private String brewery_type;
    private String address_1;
    private String address_2;
    private String address_3;
    private String city;
    private String state_province;
    private String postal_code;
    private String country;
    private String longitude;
    private String latitude;
    private String phone;
    private String website_url;
    private String state;
    private String street;   
    
    public static Brewery mapDtoToEntity(BreweryDTO dto) {
    	Brewery brewery = new Brewery();
    	brewery.setId(dto.getId());
    	brewery.setName(dto.getName());
        brewery.setType(dto.getBrewery_type());
        brewery.setAddress(dto.getAddress_1());
        brewery.setCity(dto.getCity());
        brewery.setStateOrProvince(dto.getState_province());
        brewery.setPostalCode(dto.getPostal_code());
        brewery.setCountry(dto.getCountry());
        
        if (dto.getLatitude() != null && !dto.getLatitude().trim().isEmpty()) {
            brewery.setLatitude(Double.parseDouble(dto.getLatitude().trim()));
        } else {
            brewery.setLatitude(null); // or some default value
        }
        
        if (dto.getLongitude() != null && !dto.getLongitude().trim().isEmpty()) {
            brewery.setLongitude(Double.parseDouble(dto.getLongitude().trim()));
        } else {
            brewery.setLongitude(null); // or some default value
        }
        
        //brewery.setLatitude(Double.parseDouble(dto.getLatitude()));
        //brewery.setLongitude(Double.parseDouble(dto.getLongitude()));
        
        brewery.setPhone(dto.getPhone());
        brewery.setWebsite_url(dto.getWebsite_url());
        brewery.setStreet(dto.getStreet());
        
        return brewery;
    }
}
