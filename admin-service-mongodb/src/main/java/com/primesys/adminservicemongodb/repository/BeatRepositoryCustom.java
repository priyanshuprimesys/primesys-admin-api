package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.model.BeatGroupByFileDTO;

import java.util.List;

public interface BeatRepositoryCustom {
    List<BeatGroupByFileDTO> findUnapprovedGroupedByRefFileName();
}
