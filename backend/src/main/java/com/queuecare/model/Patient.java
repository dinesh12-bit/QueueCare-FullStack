package com.queuecare.model;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name="patients", indexes={@Index(name="idx_patient_day_doctor",columnList="visit_date,doctor_id,token_number"),@Index(name="idx_patient_phone",columnList="phone")})
public class Patient {
 public enum Status { WAITING, CALLED, DONE, SKIPPED }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String name;
 @Column(nullable=false) private int age;
 @Column(nullable=false,length=20) private String phone;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="doctor_id",nullable=false) private Doctor doctor;
 @Column(name="token_number",nullable=false) private int tokenNumber;
 @Column(name="visit_date",nullable=false) private LocalDate visitDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.WAITING;
 @Column(nullable=false) private boolean priority;
 private LocalDateTime registeredAt; private LocalDateTime calledAt; private LocalDateTime completedAt;
 @Transient private Integer estimatedWaitMinutes;
 @Transient private Integer queuePosition;
 public Patient(){}
 @PrePersist void beforeSave(){if(registeredAt==null)registeredAt=LocalDateTime.now();if(visitDate==null)visitDate=LocalDate.now();}
 public Long getId(){return id;} public String getName(){return name;} public int getAge(){return age;} public String getPhone(){return phone;} public Doctor getDoctor(){return doctor;} public int getTokenNumber(){return tokenNumber;} public LocalDate getVisitDate(){return visitDate;} public Status getStatus(){return status;} public boolean isPriority(){return priority;} public LocalDateTime getRegisteredAt(){return registeredAt;} public LocalDateTime getCalledAt(){return calledAt;} public LocalDateTime getCompletedAt(){return completedAt;} public Integer getEstimatedWaitMinutes(){return estimatedWaitMinutes;} public Integer getQueuePosition(){return queuePosition;}
 public void setId(Long v){id=v;} public void setName(String v){name=v;} public void setAge(int v){age=v;} public void setPhone(String v){phone=v;} public void setDoctor(Doctor v){doctor=v;} public void setTokenNumber(int v){tokenNumber=v;} public void setVisitDate(LocalDate v){visitDate=v;} public void setStatus(Status v){status=v;} public void setPriority(boolean v){priority=v;} public void setRegisteredAt(LocalDateTime v){registeredAt=v;} public void setCalledAt(LocalDateTime v){calledAt=v;} public void setCompletedAt(LocalDateTime v){completedAt=v;} public void setEstimatedWaitMinutes(Integer v){estimatedWaitMinutes=v;} public void setQueuePosition(Integer v){queuePosition=v;}
}
