package com.school.manage.repository;

import com.school.manage.model.Bus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BusRepository extends MongoRepository<Bus, String> {

    List<Bus> findByStatus(String status);

    List<Bus> findByRouteId(String routeId);

    boolean existsByBusNumber(String busNumber);
}
