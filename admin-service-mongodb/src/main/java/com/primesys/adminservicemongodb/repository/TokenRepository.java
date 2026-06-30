package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.TokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<TokenEntity, Integer> {

    List<TokenEntity> findAllValidTokenByUserAndRevokedAndExpired(String user, Boolean revoked, Boolean expired);

    List<TokenEntity> findAllValidTokenByUser(String user);

    Optional<TokenEntity> findByUser(String user);

    Optional<TokenEntity> findByToken(String token);
}