package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoLocation {
    String type;
    ArrayList<Double> coordinates;
}
