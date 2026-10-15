package com.queuecare.service;

import com.queuecare.dto.PatientCreateRequest;
import com.queuecare.model.Doctor;
import com.queuecare.model.Patient;
import com.queuecare.repo.DoctorRepository;
import com.queuecare.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class QueueService {

    private static final int DEFAULT_CONSULT_MINUTES = 10;

    private final PatientRepository patients;
    private final DoctorRepository doctors;

    public QueueService(PatientRepository patients, DoctorRepository doctors) {
        this.patients = patients;
        this.doctors = doctors;
    }

    public List<Doctor> doctors() {
        return doctors.findAll();
    }

    @Transactional
    public Patient register(PatientCreateRequest req) {
        Doctor doctor = doctors.findById(req.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        LocalDate today = LocalDate.now();

        int nextToken = patients
                .findFirstByDoctorIdAndVisitDateOrderByTokenNumberDesc(
                        doctor.getId(), today)
                .map(p -> p.getTokenNumber() + 1)
                .orElse(1);

        Patient patient = new Patient();
        patient.setName(req.name().trim());
        patient.setAge(req.age());
        patient.setPhone(req.phone().trim());
        patient.setDoctor(doctor);
        patient.setTokenNumber(nextToken);
        patient.setVisitDate(today);
        patient.setPriority(req.age() >= 60);
        patient.setStatus(Patient.Status.WAITING);

        return patients.save(patient);
    }

    public List<Patient> today() {
        List<Patient> list =
                patients.findByVisitDateOrderByDoctorIdAscTokenNumberAsc(
                        LocalDate.now());

        addWaitEstimates(list);
        return list;
    }

    @Transactional
    public Patient callNext(Long doctorId) {
        if (!doctors.existsById(doctorId)) {
            throw new NoSuchElementException("Doctor not found");
        }

        List<Patient> all =
                patients.findByDoctorIdAndVisitDateOrderByTokenNumberAsc(
                        doctorId, LocalDate.now());

        if (all.stream().anyMatch(
                p -> p.getStatus() == Patient.Status.CALLED)) {
            throw new IllegalStateException(
                    "Complete or skip the currently called patient first");
        }

        List<Patient> waiting = orderedWaiting(all);

        if (waiting.isEmpty()) {
            throw new IllegalStateException(
                    "No waiting patients for this doctor");
        }

        Patient selected = selectNext(waiting, all);
        selected.setStatus(Patient.Status.CALLED);
        selected.setCalledAt(LocalDateTime.now());

        return patients.save(selected);
    }

    private List<Patient> orderedWaiting(List<Patient> all) {
        return all.stream()
                .filter(p -> p.getStatus() == Patient.Status.WAITING)
                .sorted(
                        Comparator.comparing(Patient::isPriority)
                                .reversed()
                                .thenComparingInt(Patient::getTokenNumber))
                .toList();
    }

    private Patient selectNext(
            List<Patient> waiting, List<Patient> all) {

        List<Patient> priority = waiting.stream()
                .filter(Patient::isPriority)
                .toList();

        List<Patient> regular = waiting.stream()
                .filter(p -> !p.isPriority())
                .toList();

        if (!priority.isEmpty() && !regular.isEmpty()) {
            long priorityDone = all.stream()
                    .filter(p -> p.getStatus() == Patient.Status.DONE
                            && p.isPriority())
                    .count();

            long regularDone = all.stream()
                    .filter(p -> p.getStatus() == Patient.Status.DONE
                            && !p.isPriority())
                    .count();

            return priorityDone <= regularDone
                    ? priority.get(0)
                    : regular.get(0);
        }

        return !priority.isEmpty()
                ? priority.get(0)
                : regular.get(0);
    }

    @Transactional
    public Patient done(Long id) {
        Patient p = get(id);

        if (p.getStatus() != Patient.Status.CALLED) {
            throw new IllegalStateException(
                    "Only a called patient can be marked done");
        }

        p.setStatus(Patient.Status.DONE);
        p.setCompletedAt(LocalDateTime.now());

        return patients.save(p);
    }

    @Transactional
    public Patient skip(Long id) {
        Patient p = get(id);

        if (p.getStatus() != Patient.Status.CALLED
                && p.getStatus() != Patient.Status.WAITING) {
            throw new IllegalStateException(
                    "Only waiting or called patients can be skipped");
        }

        p.setStatus(Patient.Status.SKIPPED);
        p.setCalledAt(null);

        return patients.save(p);
    }

    @Transactional
    public Patient recall(Long id) {
        Patient p = get(id);

        if (p.getStatus() != Patient.Status.SKIPPED) {
            throw new IllegalStateException(
                    "Only skipped patients can be recalled");
        }

        p.setStatus(Patient.Status.WAITING);
        return patients.save(p);
    }

    private Patient get(Long id) {
        return patients.findById(id)
                .orElseThrow(
                        () -> new NoSuchElementException("Patient not found"));
    }

    public List<Map<String, Object>> display() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Doctor doctor : doctors.findAll()) {
            List<Patient> list =
                    patients.findByDoctorIdAndVisitDateOrderByTokenNumberAsc(
                            doctor.getId(), LocalDate.now());

            addWaitEstimates(list);

            Patient current = list.stream()
                    .filter(p -> p.getStatus() == Patient.Status.CALLED)
                    .findFirst()
                    .orElse(null);

            List<Patient> next =
                    orderedWaiting(list).stream().limit(3).toList();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("doctor", doctor);
            row.put("current", current);
            row.put("next", next);
            row.put("waitingCount", list.stream()
                    .filter(p -> p.getStatus() == Patient.Status.WAITING)
                    .count());

            result.add(row);
        }

        return result;
    }

    public List<Map<String, Object>> report() {
        return doctors.findAll().stream().map(doctor -> {
            List<Patient> list =
                    patients.findByDoctorIdAndVisitDateOrderByTokenNumberAsc(
                            doctor.getId(), LocalDate.now());

            List<Patient> completed = list.stream()
                    .filter(p -> p.getStatus() == Patient.Status.DONE
                            && p.getCalledAt() != null
                            && p.getCompletedAt() != null)
                    .toList();

            double avgSeconds = completed.stream()
                    .mapToLong(p -> Math.max(
                            1,
                            ChronoUnit.SECONDS.between(
                                    p.getCalledAt(), p.getCompletedAt())))
                    .average()
                    .orElse(0);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("doctor", doctor);
            row.put("registered", list.size());
            row.put("seen", completed.size());
            row.put("waiting", list.stream()
                    .filter(p -> p.getStatus() == Patient.Status.WAITING)
                    .count());
            row.put("skipped", list.stream()
                    .filter(p -> p.getStatus() == Patient.Status.SKIPPED)
                    .count());
            row.put("averageConsultMinutes",
                    Math.round(avgSeconds / 60.0 * 10.0) / 10.0);

            return row;
        }).toList();
    }

    public List<Patient> search(String phone) {
        List<Patient> matches =
                patients.findByPhoneContainingOrderByVisitDateDesc(
                        phone.trim());

        addWaitEstimates(matches.stream()
                .filter(p -> p.getVisitDate().equals(LocalDate.now()))
                .toList());

        return matches;
    }

    private void addWaitEstimates(List<Patient> list) {
        Map<Long, List<Patient>> byDoctor = new HashMap<>();

        for (Patient p : list) {
            byDoctor.computeIfAbsent(
                    p.getDoctor().getId(),
                    ignored -> new ArrayList<>()
            ).add(p);
        }

        for (List<Patient> doctorPatients : byDoctor.values()) {
            List<Patient> completed = doctorPatients.stream()
                    .filter(p -> p.getStatus() == Patient.Status.DONE
                            && p.getCalledAt() != null
                            && p.getCompletedAt() != null)
                    .toList();

            double averageMinutes = completed.stream()
                    .mapToLong(p -> Math.max(
                            1,
                            ChronoUnit.SECONDS.between(
                                    p.getCalledAt(), p.getCompletedAt())))
                    .average()
                    .orElse(DEFAULT_CONSULT_MINUTES * 60.0) / 60.0;

            List<Patient> waiting = orderedWaiting(doctorPatients);

            boolean currentlyCalled = doctorPatients.stream()
                    .anyMatch(
                            p -> p.getStatus() == Patient.Status.CALLED);

            for (int i = 0; i < waiting.size(); i++) {
                Patient p = waiting.get(i);
                int position = i + 1;

                p.setQueuePosition(position);
                p.setEstimatedWaitMinutes(
                        (int) Math.ceil(
                                averageMinutes
                                        * (i + (currentlyCalled ? 1 : 0))));
            }
        }
    }
}