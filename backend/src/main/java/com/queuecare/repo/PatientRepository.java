package com.queuecare.repo;
import com.queuecare.model.Patient; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface PatientRepository extends JpaRepository<Patient,Long>{
 List<Patient> findByVisitDateOrderByDoctorIdAscTokenNumberAsc(LocalDate date);
 List<Patient> findByDoctorIdAndVisitDateOrderByTokenNumberAsc(Long doctorId,LocalDate date);
 Optional<Patient> findFirstByDoctorIdAndVisitDateOrderByTokenNumberDesc(Long doctorId,LocalDate date);
 List<Patient> findByPhoneContainingOrderByVisitDateDesc(String phone);
}
