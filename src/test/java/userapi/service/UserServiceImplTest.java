package userapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import userapi.dto.UserDto;
import userapi.handler.exception.EmailExistsException;
import userapi.handler.exception.UserNotFoundException;
import userapi.mapper.UserMapper;
import userapi.model.User;
import userapi.producer.KafkaProducer;
import userapi.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private KafkaProducer kafkaProducer;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;
    private User updatedUser;
    private UserDto userDto;
    private UserDto updatedUserDto;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test", "test@test@gmail.com", 20, LocalDateTime.now(), LocalDateTime.now());
        updatedUser = new User(1L, "admin", "admin@admin.com", 30, LocalDateTime.now(), LocalDateTime.now());

        userDto = UserDto.builder()
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
    void createUser_Success() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto savedUser = userService.createUser(userDto);

        assertNotNull(savedUser);
        assertEquals(userDto, savedUser);
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(1)).save(user);
        verify(kafkaProducer, times(1)).sendUser(anyString(), anyString());
    }

    @Test
    void createUser_ThrowEmailExistsException() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(EmailExistsException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto getUser = userService.getUserById(userId);

        assertNotNull(getUser);
        assertEquals(userDto, getUser);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_ThrowUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updatedUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        UserDto updatedDto = userService.updateUser(userId, updatedUserDto);

        assertNotNull(updatedDto);
        assertEquals(updatedUserDto, updatedDto);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ThrowUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updatedUserDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ThrowEmailExistsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updatedUserDto.getEmail())).thenReturn(true);

        assertThrows(EmailExistsException.class, () -> userService.updateUser(userId, updatedUserDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(kafkaProducer, times(1)).sendUser(anyString(),anyString());
    }

    @Test
    void deleteUser_ThrowUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(any());
    }
}
