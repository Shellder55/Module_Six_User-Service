package userapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import userapi.dto.UserDto;
import userapi.handler.exception.EmailExistsException;
import userapi.handler.exception.UserNotFoundException;
import userapi.service.UserServiceImpl;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private final UserServiceImpl userService;
    private UserDto savedUserDto;
    private UserDto updatedUserDto;
    private Long userId = 1L;

    @Autowired
    public UserControllerTest(UserServiceImpl userService) {
        this.userService = userService;
    }

    @BeforeEach
    void setUp() {
        savedUserDto = UserDto.builder()
                .id(1L)
                .name("test")
                .email("test@test.com")
                .age(20)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        updatedUserDto = UserDto.builder()
                .id(1L)
                .name("admin")
                .email("admin@admin.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userId = 1L;
    }

    @Test
    void createUser_Success() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(savedUserDto);
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    void createUser_ThrowEmailExistsException() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenThrow(new EmailExistsException());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(userId)).thenReturn(savedUserDto);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(savedUserDto.getName()))
                .andExpect(jsonPath("$.email").value(savedUserDto.getEmail()))
                .andExpect(jsonPath("$.age").value(savedUserDto.getAge()));
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@admin.com"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void updateUser_ThrowEmailExistsException() throws Exception {
        when(userService.updateUser(eq(userId), any(UserDto.class))).thenThrow(new EmailExistsException());

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(userId);
    }

    @Test
    void getUserById_UserNotFoundException() throws Exception {
        Long userId = 9999L;

        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found by ID: " + userId));
    }
}
