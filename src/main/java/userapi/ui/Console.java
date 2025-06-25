package userapi.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import userapi.dto.UserDto;

import java.util.Scanner;

@Component
public class Console implements CommandLineRunner {
    private final Scanner scanner;
    private final static Logger logger = LoggerFactory.getLogger(Console.class);
    private final RestTemplate restTemplate;
    private final ApplicationContext applicationContext;
    private final String BASE_URL = "http://localhost:8080/api/users";

    public Console(RestTemplate restTemplate, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.scanner = new Scanner(System.in);
        this.restTemplate = restTemplate;
    }


    @Override
    public void run(String... args) {
        boolean running = true;
        while (running) {
            menu();
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> createUser();
                case 2 -> getUserById();
                case 3 -> updateUser();
                case 4 -> deleteUser();
                case 0 -> {
                    System.out.println("exit...");
                    running = false;
                    exitApplication();
                }
                default -> System.out.println("Wrong choice");
            }
        }
    }


    private void menu() {
        System.out.println("-----MENU----- \n");
        System.out.println("1. Create user");
        System.out.println("2. Get user");
        System.out.println("3. Update user");
        System.out.println("4. Delete user");
        System.out.println("0. Exit \n");
        System.out.print("Select action: ");
    }


    private void createUser() {
        UserDto newUser = builderUserDto();

        logger.info("Saving the user...");

            UserDto createdUser = restTemplate.postForObject(BASE_URL, newUser, UserDto.class);
            printUser(createdUser);

        logger.info("User added successfully. ID: \n");
    }


    private void getUserById() {
        System.out.print("Enter user ID to search: ");
        long id = scanner.nextLong();

        logger.info("Getting user by ID: '{}'...", id);
        try {
            UserDto user = restTemplate.getForObject(BASE_URL + "/" + id, UserDto.class);
            printUser(user);
            logger.info("User by ID: '{}' successfully retrieved \n", id);
        } catch (Exception e) {
            logger.error("User not found");
        }
    }


    private void updateUser() {
        System.out.print("Enter user ID to change: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        logger.info("Changing user data by ID: '{}'...", id);
        UserDto updatedUser = builderUserDto();
        restTemplate.put(BASE_URL + "/" + id, updatedUser);
        logger.info("User data by ID: '{}' successfully changed \n", id);
    }

    private void deleteUser() {
        System.out.print("Enter user id to delete: ");
        long id = scanner.nextLong();

        logger.info("Deleting a user by ID: '{}'...", id);
        restTemplate.delete(BASE_URL + "/" + id);
        logger.info("User by ID: '{}' delete successfully \n", id);
    }

    private UserDto builderUserDto() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter age: ");
        Integer age = scanner.nextInt();

        return UserDto.builder()
                .name(name)
                .email(email)
                .age(age)
                .build();
    }

    private void printUser(UserDto user) {
        System.out.printf("ID: %d | Name: %s | Email: %s | Age: %d | Created: %s | Updated: %s%n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private void exitApplication(){
        int exit = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exit);
    }
}
