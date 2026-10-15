package com.queuecare.config;

import com.queuecare.model.Doctor;
import com.queuecare.model.Patient;
import com.queuecare.repo.DoctorRepository;
import com.queuecare.repo.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedDemoData(DoctorRepository doctorRepository, PatientRepository patientRepository) {
        return args -> {
            if (doctorRepository.count() == 0) {
                doctorRepository.saveAll(List.of(
                    new Doctor("Dr. Sharma", "General Physician"),
                    new Doctor("Dr. Mehta", "Cardiology"),
                    new Doctor("Dr. Iyer", "Dermatology")
                ));
            }
            // Assignment sample data: three doctors with five waiting patients each.
            // It is inserted only when the patients table is empty, so it never duplicates on restart.
            if (patientRepository.count() == 0) {
                List<Doctor> doctors = doctorRepository.findAll().stream()
                    .sorted((a, b) -> a.getId().compareTo(b.getId())).toList();
                String[][] names = {
                    {"Aarav Singh", "Priya Verma", "Rohan Patel", "Ananya Gupta", "Kabir Shah"},
                    {"Meera Joshi", "Arjun Rao", "Ishita Nair", "Vivaan Das", "Sara Khan"},
                    {"Aditya Roy", "Diya Kapoor", "Reyansh Jain", "Myra Sen", "Ishaan Bose"}
                };
                int[][] ages = {{34, 62, 27, 45, 51}, {58, 41, 67, 32, 24}, {29, 72, 39, 55, 44}};
                String[][] phones = {
                    {"9000000001", "9000000002", "9000000003", "9000000004", "9000000005"},
                    {"9000000011", "9000000012", "9000000013", "9000000014", "9000000015"},
                    {"9000000021", "9000000022", "9000000023", "9000000024", "9000000025"}
                };
                for (int d = 0; d < Math.min(3, doctors.size()); d++) {
                    for (int i = 0; i < names[d].length; i++) {
                        Patient patient = new Patient();
                        patient.setName(names[d][i]);
                        patient.setAge(ages[d][i]);
                        patient.setPhone(phones[d][i]);
                        patient.setDoctor(doctors.get(d));
                        patient.setTokenNumber(i + 1);
                        patient.setVisitDate(LocalDate.now());
                        patient.setPriority(ages[d][i] >= 60);
                        patient.setStatus(Patient.Status.WAITING);
                        patientRepository.save(patient);
                    }
                }
            }
        };
    }
}
