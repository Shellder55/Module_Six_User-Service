package userapi.service;

import org.springframework.stereotype.Service;
import userapi.dto.UserDto;

@Service
public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserById(Long id);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);
}
