package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @author primesysindia This is the global report template that will be used for report display in the web UI In this
 *         we will decide what will be in the email and what will be on the UI but email and UI can be different if
 *         admin wants <br/>
 *         if(activeStatus == false){ every else variable will be false }
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportTemplateModel {
    private String key;
    private String value;
    private String description;
    private Boolean activeStatus;
}
