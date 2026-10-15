package com.queuecare.controller;
import com.queuecare.dto.PatientCreateRequest; import com.queuecare.model.*; import com.queuecare.service.QueueService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api") @CrossOrigin(origins={"http://localhost:5173","http://127.0.0.1:5173"})
public class ApiController { private final QueueService service; public ApiController(QueueService service){this.service=service;}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("status","UP","application","QueueCare");}
 @GetMapping("/doctors") public List<Doctor> doctors(){return service.doctors();}
 @PostMapping("/patients") public ResponseEntity<Patient> register(@Valid @RequestBody PatientCreateRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(service.register(req));}
 @GetMapping("/queue") public List<Patient> queue(){return service.today();}
 @PostMapping("/doctors/{doctorId}/call-next") public Patient callNext(@PathVariable Long doctorId){return service.callNext(doctorId);}
 @PostMapping("/patients/{id}/done") public Patient done(@PathVariable Long id){return service.done(id);}
 @PostMapping("/patients/{id}/skip") public Patient skip(@PathVariable Long id){return service.skip(id);}
 @PostMapping("/patients/{id}/recall") public Patient recall(@PathVariable Long id){return service.recall(id);}
 @GetMapping("/display") public List<Map<String,Object>> display(){return service.display();}
 @GetMapping("/reports/daily") public List<Map<String,Object>> report(){return service.report();}
 @GetMapping("/patients/search") public List<Patient> search(@RequestParam String phone){return service.search(phone);}
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class,NoSuchElementException.class}) public ResponseEntity<Map<String,String>> badRequest(RuntimeException ex){return ResponseEntity.badRequest().body(Map.of("message",ex.getMessage()));}
}
