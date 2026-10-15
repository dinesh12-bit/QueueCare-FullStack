package com.queuecare.dto;
import jakarta.validation.constraints.*;
public record PatientCreateRequest(@NotBlank @Size(max=100) String name,@Min(0) @Max(120) int age,@NotBlank @Pattern(regexp="[0-9+() -]{7,20}",message="Enter a valid phone number") String phone,@NotNull Long doctorId){}
