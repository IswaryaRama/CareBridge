
package com.hospital;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HospitalController {

    private final JdbcTemplate db;

    public HospitalController(JdbcTemplate db) {
        this.db = db;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> m = new LinkedHashMap<>();

        m.put("patients", count("patients"));
        m.put("doctors", count("doctors"));
        m.put("appointments", count("appointments"));

        m.put("unpaidBills", db.queryForObject(
                "SELECT COUNT(*) FROM bills WHERE payment_status='Unpaid'",
                Integer.class));

        m.put("revenue", db.queryForObject(
                "SELECT COALESCE(SUM(amount),0) FROM bills WHERE payment_status='Paid'",
                Double.class));

        return m;
    }

    private int count(String table) {
        return db.queryForObject(
                "SELECT COUNT(*) FROM " + table,
                Integer.class);
    }

    // ==================== PATIENTS ====================

    @GetMapping("/patients")
    public List<Map<String, Object>> patients() {
        return db.queryForList(
                "SELECT * FROM patients ORDER BY id DESC");
    }

    @PostMapping("/patients")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addPatient(
            @RequestBody Map<String, Object> p) {

        String name = required(p, "name");
        int age = integer(p.get("age"), "age");

        if (age < 0 || age > 130) {
            throw bad("Age must be between 0 and 130");
        }

        db.update(
                "INSERT INTO patients " +
                "(name, age, gender, phone, email, address, diagnosis) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                name,
                age,
                str(p, "gender"),
                str(p, "phone"),
                str(p, "email"),
                str(p, "address"),
                str(p, "diagnosis")
        );

        // Retrieve the most recently inserted patient.
        return db.queryForMap(
                "SELECT * FROM patients ORDER BY id DESC LIMIT 1");
    }

    @PutMapping("/patients/{id}")
    public Map<String, Object> updatePatient(
            @PathVariable int id,
            @RequestBody Map<String, Object> p) {

        exists("patients", id);

        int age = integer(p.get("age"), "age");

        if (age < 0 || age > 130) {
            throw bad("Age must be between 0 and 130");
        }

        db.update(
                "UPDATE patients SET name=?, age=?, gender=?, " +
                "phone=?, email=?, address=?, diagnosis=? WHERE id=?",
                required(p, "name"),
                age,
                str(p, "gender"),
                str(p, "phone"),
                str(p, "email"),
                str(p, "address"),
                str(p, "diagnosis"),
                id
        );

        return db.queryForMap(
                "SELECT * FROM patients WHERE id=?", id);
    }

    @DeleteMapping("/patients/{id}")
    public Map<String, String> deletePatient(
            @PathVariable int id) {

        exists("patients", id);
        db.update("DELETE FROM patients WHERE id=?", id);

        return ok("Patient deleted");
    }

    // ==================== DOCTORS ====================

    @GetMapping("/doctors")
    public List<Map<String, Object>> doctors() {
        return db.queryForList(
                "SELECT * FROM doctors ORDER BY id DESC");
    }

    @PostMapping("/doctors")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addDoctor(
            @RequestBody Map<String, Object> p) {

        db.update(
                "INSERT INTO doctors " +
                "(name, specialization, phone, email, consultation_fee, available_days) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                required(p, "name"),
                required(p, "specialization"),
                str(p, "phone"),
                str(p, "email"),
                number(p.get("consultationFee")),
                str(p, "availableDays")
        );

        return db.queryForMap(
                "SELECT * FROM doctors ORDER BY id DESC LIMIT 1");
    }

    @PutMapping("/doctors/{id}")
    public Map<String, Object> updateDoctor(
            @PathVariable int id,
            @RequestBody Map<String, Object> p) {

        exists("doctors", id);

        db.update(
                "UPDATE doctors SET name=?, specialization=?, phone=?, " +
                "email=?, consultation_fee=?, available_days=? WHERE id=?",
                required(p, "name"),
                required(p, "specialization"),
                str(p, "phone"),
                str(p, "email"),
                number(p.get("consultationFee")),
                str(p, "availableDays"),
                id
        );

        return db.queryForMap(
                "SELECT * FROM doctors WHERE id=?", id);
    }

    @DeleteMapping("/doctors/{id}")
    public Map<String, String> deleteDoctor(
            @PathVariable int id) {

        exists("doctors", id);
        db.update("DELETE FROM doctors WHERE id=?", id);

        return ok("Doctor deleted");
    }

    // ==================== APPOINTMENTS ====================

    @GetMapping("/appointments")
    public List<Map<String, Object>> appointments() {
        return db.queryForList(
                "SELECT a.*, p.name AS patient_name, " +
                "d.name AS doctor_name, d.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON p.id=a.patient_id " +
                "JOIN doctors d ON d.id=a.doctor_id " +
                "ORDER BY a.appointment_date DESC, " +
                "a.appointment_time DESC");
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addAppointment(
            @RequestBody Map<String, Object> p) {

        int patientId = integer(p.get("patientId"), "patientId");
        int doctorId = integer(p.get("doctorId"), "doctorId");

        exists("patients", patientId);
        exists("doctors", doctorId);

        db.update(
                "INSERT INTO appointments " +
                "(patient_id, doctor_id, appointment_date, " +
                "appointment_time, reason, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                patientId,
                doctorId,
                required(p, "appointmentDate"),
                required(p, "appointmentTime"),
                str(p, "reason"),
                "Scheduled"
        );

        return db.queryForMap(
                "SELECT * FROM appointments ORDER BY id DESC LIMIT 1");
    }

    @PutMapping("/appointments/{id}/status")
    public Map<String, Object> appointmentStatus(
            @PathVariable int id,
            @RequestBody Map<String, Object> p) {

        exists("appointments", id);

        String status = required(p, "status");

        if (!List.of("Scheduled", "Completed", "Cancelled")
                .contains(status)) {
            throw bad(
                    "Status must be Scheduled, Completed or Cancelled");
        }

        db.update(
                "UPDATE appointments SET status=? WHERE id=?",
                status, id);

        return db.queryForMap(
                "SELECT * FROM appointments WHERE id=?", id);
    }

    @DeleteMapping("/appointments/{id}")
    public Map<String, String> deleteAppointment(
            @PathVariable int id) {

        exists("appointments", id);
        db.update("DELETE FROM appointments WHERE id=?", id);

        return ok("Appointment deleted");
    }

    // ==================== BILLS ====================

    @GetMapping("/bills")
    public List<Map<String, Object>> bills() {
        return db.queryForList(
                "SELECT b.*, p.name AS patient_name " +
                "FROM bills b " +
                "JOIN patients p ON p.id=b.patient_id " +
                "ORDER BY b.id DESC");
    }

    @PostMapping("/bills")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addBill(
            @RequestBody Map<String, Object> p) {

        int patientId = integer(p.get("patientId"), "patientId");
        exists("patients", patientId);

        Integer appointmentId = null;

        if (p.get("appointmentId") != null
                && !p.get("appointmentId").toString().isBlank()) {

            appointmentId = integer(
                    p.get("appointmentId"), "appointmentId");

            exists("appointments", appointmentId);
        }

        double amount = number(p.get("amount"));

        if (amount < 0) {
            throw bad("Amount cannot be negative");
        }

        db.update(
                "INSERT INTO bills " +
                "(patient_id, appointment_id, description, amount, payment_status) " +
                "VALUES (?, ?, ?, ?, ?)",
                patientId,
                appointmentId,
                str(p, "description"),
                amount,
                "Unpaid"
        );

        return db.queryForMap(
                "SELECT * FROM bills ORDER BY id DESC LIMIT 1");
    }

    @PutMapping("/bills/{id}/payment")
    public Map<String, Object> payment(
            @PathVariable int id,
            @RequestBody Map<String, Object> p) {

        exists("bills", id);

        String status = required(p, "paymentStatus");

        if (!List.of("Paid", "Unpaid").contains(status)) {
            throw bad("Payment status must be Paid or Unpaid");
        }

        db.update(
                "UPDATE bills SET payment_status=? WHERE id=?",
                status, id);

        return db.queryForMap(
                "SELECT * FROM bills WHERE id=?", id);
    }

    @DeleteMapping("/bills/{id}")
    public Map<String, String> deleteBill(
            @PathVariable int id) {

        exists("bills", id);
        db.update("DELETE FROM bills WHERE id=?", id);

        return ok("Bill deleted");
    }

    // ==================== COMMON METHODS ====================

    private void exists(String table, int id) {
        Integer n = db.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE id=?",
                Integer.class, id);

        if (n == null || n == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Record not found");
        }
    }

    private String required(Map<String, Object> p, String key) {
        String value = str(p, key);

        if (value.isBlank()) {
            throw bad(key + " is required");
        }

        return value;
    }

    private String str(Map<String, Object> p, String key) {
        Object value = p.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private int integer(Object value, String key) {
        try {
            if (value == null) {
                throw new Exception();
            }

            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            throw bad(key + " must be a valid number");
        }
    }

    private double number(Object value) {
        try {
            return value == null || value.toString().isBlank()
                    ? 0
                    : Double.parseDouble(value.toString());
        } catch (Exception e) {
            throw bad("Amount must be a valid number");
        }
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message);
    }

    private Map<String, String> ok(String message) {
        return Map.of("message", message);
    }
}
