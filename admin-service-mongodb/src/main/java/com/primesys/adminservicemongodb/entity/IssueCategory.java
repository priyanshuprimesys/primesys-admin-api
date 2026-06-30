package com.primesys.adminservicemongodb.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document("issue_categories")
public class IssueCategory {
    private String category;
    private List<String> subcategories;
}
