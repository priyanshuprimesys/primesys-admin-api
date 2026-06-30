package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GroupCount {
    private Map<String, String> groupValues; // e.g., {"priority": "HIGH", "category": "BUG"}
    private long count;
}
