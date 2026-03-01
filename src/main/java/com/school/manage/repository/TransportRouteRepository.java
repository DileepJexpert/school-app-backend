package com.school.manage.repository;

import com.school.manage.model.TransportRoute;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransportRouteRepository extends MongoRepository<TransportRoute, String> {
}
