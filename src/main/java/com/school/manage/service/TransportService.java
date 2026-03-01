package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Bus;
import com.school.manage.model.StudentTransportAssignment;
import com.school.manage.model.TransportRoute;
import com.school.manage.repository.BusRepository;
import com.school.manage.repository.StudentTransportAssignmentRepository;
import com.school.manage.repository.TransportRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final BusRepository busRepository;
    private final TransportRouteRepository routeRepository;
    private final StudentTransportAssignmentRepository assignmentRepository;

    // ── Buses ─────────────────────────────────────────────────────────────────

    /** Returns all buses with real-time assignedCount computed from assignments. */
    public List<Bus> getAllBuses() {
        List<Bus> buses = busRepository.findAll();
        buses.forEach(b -> b.setAssignedCount(
                (int) assignmentRepository.countByBusIdAndStatus(b.getId(), "ACTIVE")));
        return buses;
    }

    public Bus createBus(Bus bus) {
        bus.setCreatedAt(LocalDateTime.now());
        bus.setAssignedCount(0);
        return busRepository.save(bus);
    }

    public Bus updateBus(String id, Bus updated) {
        Bus existing = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found: " + id));
        existing.setBusNumber(updated.getBusNumber());
        existing.setDriverName(updated.getDriverName());
        existing.setDriverMobile(updated.getDriverMobile());
        existing.setRouteId(updated.getRouteId());
        existing.setCapacity(updated.getCapacity());
        existing.setStatus(updated.getStatus());
        existing.setInsuranceExpiry(updated.getInsuranceExpiry());
        existing.setNotes(updated.getNotes());
        Bus saved = busRepository.save(existing);
        saved.setAssignedCount((int) assignmentRepository.countByBusIdAndStatus(id, "ACTIVE"));
        return saved;
    }

    public void deleteBus(String id) {
        if (!busRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bus not found: " + id);
        }
        busRepository.deleteById(id);
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    /** Returns all routes with real-time assignedCount. */
    public List<TransportRoute> getAllRoutes() {
        List<TransportRoute> routes = routeRepository.findAll();
        routes.forEach(r -> r.setAssignedCount(
                (int) assignmentRepository.countByRouteIdAndStatus(r.getId(), "ACTIVE")));
        return routes;
    }

    public TransportRoute createRoute(TransportRoute route) {
        route.setCreatedAt(LocalDateTime.now());
        return routeRepository.save(route);
    }

    public TransportRoute updateRoute(String id, TransportRoute updated) {
        TransportRoute existing = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + id));
        existing.setZoneName(updated.getZoneName());
        existing.setDisplayName(updated.getDisplayName());
        existing.setAreasCovered(updated.getAreasCovered());
        existing.setStops(updated.getStops());
        existing.setFirstPickupTime(updated.getFirstPickupTime());
        existing.setMonthlyFee(updated.getMonthlyFee());
        TransportRoute saved = routeRepository.save(existing);
        saved.setAssignedCount((int) assignmentRepository.countByRouteIdAndStatus(id, "ACTIVE"));
        return saved;
    }

    public void deleteRoute(String id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route not found: " + id);
        }
        routeRepository.deleteById(id);
    }

    // ── Assignments ───────────────────────────────────────────────────────────

    /** Returns all active student-transport assignments. */
    public List<StudentTransportAssignment> getAllActiveAssignments() {
        return assignmentRepository.findAll().stream()
                .filter(a -> "ACTIVE".equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    /** Returns all active students on a specific bus. */
    public List<StudentTransportAssignment> getAssignmentsByBus(String busId) {
        return assignmentRepository.findByBusIdAndStatus(busId, "ACTIVE");
    }

    /** Returns the active assignment for a student, if any. */
    public Optional<StudentTransportAssignment> getAssignmentByStudent(String studentId) {
        return assignmentRepository.findByStudentIdAndStatus(studentId, "ACTIVE");
    }

    /**
     * Assigns a student to a bus + route. If the student already has an active
     * assignment it is deactivated first (re-assignment flow).
     */
    public StudentTransportAssignment assignStudent(StudentTransportAssignment assignment) {
        assignmentRepository
                .findByStudentIdAndStatus(assignment.getStudentId(), "ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("INACTIVE");
                    assignmentRepository.save(existing);
                });
        assignment.setStatus("ACTIVE");
        assignment.setAssignedDate(LocalDate.now());
        assignment.setCreatedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }

    /** Soft-deletes an assignment by setting its status to INACTIVE. */
    public void removeAssignment(String id) {
        StudentTransportAssignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));
        a.setStatus("INACTIVE");
        assignmentRepository.save(a);
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public Map<String, Object> getStats() {
        long totalBuses = busRepository.count();
        long activeBuses = busRepository.findByStatus("ACTIVE").size();
        long maintenanceBuses = busRepository.findByStatus("MAINTENANCE").size();
        long totalRoutes = routeRepository.count();
        long totalAssigned = assignmentRepository.findAll().stream()
                .filter(a -> "ACTIVE".equals(a.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBuses", totalBuses);
        stats.put("activeBuses", activeBuses);
        stats.put("maintenanceBuses", maintenanceBuses);
        stats.put("totalRoutes", totalRoutes);
        stats.put("totalStudentsAssigned", totalAssigned);
        return stats;
    }
}
