package com.primesys.adminserviceserver.dtos.issue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferMemberDto {

    private String id;
    private String name;
    private String userName;
    private Integer roleId;
    private String mobileNo;
    private String trackDivisionId;

    public static TransferMemberDto from(DivisionLoginEntity entity) {
        return TransferMemberDto.builder().id(entity.getId()).name(entity.getName()).userName(entity.getUserName())
                .roleId(entity.getRoleId()).mobileNo(entity.getMobileNo()).trackDivisionId(entity.getTrackDivisionId())
                .build();
    }
}
