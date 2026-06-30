package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.model.ReportTemplateModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

/**
 * @author primesysindia Division will only contain parent Division ID No child can have different report only parent
 *         will have the report control
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("division_report_template_entity")
public class DivisionReportTemplateEntity {

    @MongoId
    private ObjectId id;

    @Field("division_id")
    private String divisionId;

    @Field("device_type_id")
    private Integer deviceTypeId;

    @Field("report_templates")
    List<ReportTemplateModel> reportTemplates;
}
