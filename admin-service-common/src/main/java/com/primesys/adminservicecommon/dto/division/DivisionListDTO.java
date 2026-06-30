package com.primesys.adminservicecommon.dto.division;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DivisionListDTO {
    String id;
    String userName;
    String name;
    Integer deptId;
    String trackDivisionId;
    Integer roleId;
}
