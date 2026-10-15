package com.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import java.sql.*;

@SpringBootApplication
public class HospitalManagementApplication {
    public static void main(String[] args) { SpringApplication.run(HospitalManagementApplication.class, args); }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:hospital.db");
        return ds;
    }

    @Bean
    public CommandLineRunner initializeDatabase(DataSource ds) {
        return args -> {
            try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
                s.execute("CREATE TABLE IF NOT EXISTS patients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER NOT NULL, gender TEXT, phone TEXT, email TEXT, address TEXT, diagnosis TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
                s.execute("CREATE TABLE IF NOT EXISTS doctors (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, specialization TEXT NOT NULL, phone TEXT, email TEXT, consultation_fee REAL DEFAULT 0, available_days TEXT)");
                s.execute("CREATE TABLE IF NOT EXISTS appointments (id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER NOT NULL, doctor_id INTEGER NOT NULL, appointment_date TEXT NOT NULL, appointment_time TEXT NOT NULL, reason TEXT, status TEXT DEFAULT 'Scheduled', FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE, FOREIGN KEY(doctor_id) REFERENCES doctors(id) ON DELETE CASCADE)");
                s.execute("CREATE TABLE IF NOT EXISTS bills (id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER NOT NULL, appointment_id INTEGER, description TEXT, amount REAL NOT NULL, payment_status TEXT DEFAULT 'Unpaid', created_at TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE, FOREIGN KEY(appointment_id) REFERENCES appointments(id) ON DELETE SET NULL)");
            }
        };
    }
}
