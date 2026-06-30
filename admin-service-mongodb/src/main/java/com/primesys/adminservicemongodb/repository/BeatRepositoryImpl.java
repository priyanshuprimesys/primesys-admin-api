package com.primesys.adminservicemongodb.repository;

import com.mongodb.BasicDBObject;
import com.primesys.adminservicemongodb.model.BeatGroupByFileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BeatRepositoryImpl implements BeatRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<BeatGroupByFileDTO> findUnapprovedGroupedByRefFileName() {
        MatchOperation match = Aggregation.match(new Criteria().andOperator(Criteria.where("approved_status").is(false),
                Criteria.where("active_status").is(true), Criteria.where("ref_file_name").exists(true)));

        // Group by ref_file_name and deviceImei
        GroupOperation groupByFileAndDevice = Aggregation.group("ref_file_name", "device_imei").push("$$ROOT")
                .as("records");

        // Group by ref_file_name, collect grouped devices
        GroupOperation groupByFile = Aggregation.group("_id.ref_file_name")
                .push(new BasicDBObject("deviceImei", "$_id.device_imei").append("beats", "$records")).as("devices");

        // Rename _id → refFileName
        ProjectionOperation project = Aggregation.project().and("_id").as("refFileName").and("devices").as("devices");

        Aggregation aggregation = Aggregation.newAggregation(match, groupByFileAndDevice, groupByFile, project);

        AggregationResults<BeatGroupByFileDTO> results = mongoTemplate.aggregate(aggregation, "trip_data", // collection
                                                                                                           // name
                BeatGroupByFileDTO.class);

        return results.getMappedResults();

    }
}
