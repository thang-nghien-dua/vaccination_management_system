package ut.edu.vaccinationmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Bật scheduled tasks cho cleanup job
public class VaccinationManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaccinationManagementSystemApplication.class, args);
    }

}
