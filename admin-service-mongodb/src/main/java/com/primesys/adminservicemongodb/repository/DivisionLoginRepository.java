package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DivisionLoginRepository extends MongoRepository<DivisionLoginEntity, String> {
    DivisionLoginEntity findByUserNameAndPassword(String userName, String password);

    boolean existsById(String id);

    @Query("{ path: /,?0,/ }")
    List<DivisionLoginEntity> findByPath(String divisionID);

    @Query(value = "{ 'path': { '$regex': ?0 } }", fields = "{ 'device_list': 1, 'user_name': 1, 'name': 1 , 'modules_list':1, 'path':1 }")
    List<DivisionLoginEntity> findDeviceListByPath(String divisionId);

    DivisionLoginEntity findByTrackDivisionId(String id);

    List<DivisionLoginEntity> findAllByActiveStatusTrue();

    List<DivisionLoginEntity> findByRoleId(Integer roleId);

    DivisionLoginEntity findFirstByRoleIdAndWhatsappGroupName(Integer roleId, String name);

    // Method to find a list of DivisionLoginEntity by a list of roleIds
    List<DivisionLoginEntity> findByRoleIdInAndActiveStatusTrue(List<Integer> roleIds);

    List<DivisionLoginEntity> findByTrackDivisionIdAndDeptIdAndActiveStatusTrue(String divId, Integer deptId);

    @Query("{'path': { $regex: ?0, $options: 'i' }, $or: [ {'deptId': ?1}, {'deptId': ?2} ]}")
    List<DivisionLoginEntity> findByPathContainsAndDeptId(String pathContains, int deptId1, int deptId2);

    Optional<DivisionLoginEntity> findByUserName(String userName);

    @Query("{ 'track_division_id': ?0, $or: [ { 'device_list': { $regex: ?1 } }, { 'device_list': { $regex: ?2 } } ] }")
    List<DivisionLoginEntity> findByDeviceListContainsWithDivId(String divisionId, String device, String deviceInt);

    Optional<DivisionLoginEntity> findById(String id);

    List<DivisionLoginEntity> findByRoleIdIn(Collection<Integer> roleIds);

    List<DivisionLoginEntity> findByReportEmailId(String email);

    @Query(value = "{ 'track_division_id': ?0, 'device_list': { $regex: ',\\\\d+,' } }", fields = "{ 'path': 1, 'track_division_id': 1,'name':1 ,'device_list':1 }")
    List<DivisionLoginEntity> findDivisionUsersWithDevices(String divisionId);

    @Query("{ 'path': { $regex: ?0 } }")
    List<DivisionLoginEntity> findParentAndChildren(String divisionId);

    @Query("{ 'path': { $regex: ?0 } }")
    List<DivisionLoginEntity> findHierarchyByRootDivisionId(String rootDivisionId);

    List<DivisionLoginEntity> findByIsRailwayUserFalse();

    @Query(value = "{ 'path': { $regex: ?0 }, 'device_list': { $regex: ',\\\\d+,' } }")
    List<DivisionLoginEntity> findByPathContainingWithDeviceList(String divisionId);

    /// fetch device list
    @Query(value = "{ 'path': { $regex: ?0 }, 'device_list': { $nin: ['', ',,'] } }", fields = "{ 'device_list': 1, '_id': 0 }")
    List<DivisionLoginEntity> findValidDeviceLists(String divisionId);

}
