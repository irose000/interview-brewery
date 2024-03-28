package com.irose000.interviewBrewery.models;

import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Brewery {
	private @Id String id;
	private String name;
	private String type;
    private String address;
    private String city;
    private String stateOrProvince;
    private String postalCode;
    private String country;
    private Double longitude;
    private Double latitude;
    private String phone;
    private String website_url;
    private String street;    
}