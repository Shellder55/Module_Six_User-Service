package userapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import userapi.exception.UserNotFoundException;
import userapi.dto.UserDto;
import userapi.service.UserServiceImpl;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserServiceImpl userService;

    private UserDto userDto;
    private UserDto savedUserDto;
    private UserDto updatedUserDto;
    private Long userId = 1L;

    @BeforeEach
    void setUp(){
        userDto = UserDto.builder()
                .name("test")
                .email("test@test.com")
                .age(20)
                .build();

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
        Mockito.when(userService.createUser(any(UserDto.class))).thenReturn(savedUserDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.age").value(userDto.getAge()));
    }

    @Test
    void getUserById_Success() throws Exception {
        Mockito.when(userService.getUserById(userId)).thenReturn(savedUserDto);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUserDto.getId()))
                .andExpect(jsonPath("$.name").value(savedUserDto.getName()))
                .andExpect(jsonPath("$.email").value(savedUserDto.getEmail()))
                .andExpect(jsonPath("$.age").value(savedUserDto.getAge()));
    }

    @Test
    void updateUser_Success() throws Exception {
        Mockito.when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(updatedUserDto.getName()))
                .andExpect(jsonPath("$.email").value(updatedUserDto.getEmail()))
                .andExpect(jsonPath("$.age").value(updatedUserDto.getAge()));
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

        Mockito.when(userService.getUserById(userId))
                .thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found by ID: " + userId));
    }
}
