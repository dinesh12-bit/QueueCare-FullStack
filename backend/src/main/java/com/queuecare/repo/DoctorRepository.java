package com.queuecare.repo;
import com.queuecare.model.Doctor; import org.springframework.data.jpa.repository.JpaRepository;
public interface DoctorRepository extends JpaRepository<Doctor,Long>{}
