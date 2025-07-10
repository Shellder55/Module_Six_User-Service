package userapi.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import userapi.controller.UserController;
import userapi.dto.UserDto;
import userapi.handler.exception.EmailExistsException;
import userapi.handler.exception.UserNotFoundException;

import java.util.Scanner;

@Profile("!test")
@Component
public class Console implements CommandLineRunner {
    private final Scanner scanner;
    private final static Logger logger = LoggerFactory.getLogger(Console.class);
    private final UserController userController;
    private final ApplicationContext applicationContext;

    public Console(UserController userController, ApplicationContext applicationContext) {
        this.userController = userController;
        this.applicationContext = applicationContext;
        this.scanner = new Scanner(System.in);

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
        try {
            ResponseEntity<EntityModel<UserDto>> response = userController.createUser(newUser);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("User added successfully. ID: \n");
            } else {
                logger.error("Failed to create user: {}", response.getBody());
            }
        } catch (EmailExistsException e) {
            logger.error("Failed to create user: {}", e.getMessage());
            throw e;
        }
    }

    private void getUserById() {
        System.out.print("Enter user ID to search: ");
        long id = scanner.nextLong();

        logger.info("Getting user by ID: '{}'...", id);
        try {
            ResponseEntity<EntityModel<UserDto>> response = userController.getUserById(id);
            if (response.getStatusCode().is2xxSuccessful()) {
                UserDto user = response.getBody().getContent();
                printUser(user);
                logger.info("User by ID: '{}' successfully retrieved \n", id);
            }
        } catch (UserNotFoundException e) {
            logger.error("User not found");
        }
    }

    private void updateUser() {
        System.out.print("Enter user ID to change: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        logger.info("Changing user data by ID: '{}'...", id);

        try {
            UserDto updatedUser = builderUserDto();
            ResponseEntity<EntityModel<UserDto>> response = userController.updateUser(id, updatedUser);
            if (response.getStatusCode().is2xxSuccessful()) {
                UserDto user = response.getBody().getContent();
                printUser(user);
                logger.info("User data by ID: '{}' successfully changed \n", id);
            }
        } catch (EmailExistsException e) {
            logger.error("Failed to update user: {}", e.getMessage());
            throw e;
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private void deleteUser() {
        System.out.print("Enter user id to delete: ");
        long id = scanner.nextLong();

        logger.info("Deleting a user by ID: '{}'...", id);

        try {
            userController.deleteUser(id);
            logger.info("User by ID: '{}' delete successfully \n", id);
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage());
        }
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
        System.out.printf("ID: %d | Name: %s | Email: %s | Age: %d\n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge());
    }

    private void exitApplication() {
        int exit = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exit);
    }
}
