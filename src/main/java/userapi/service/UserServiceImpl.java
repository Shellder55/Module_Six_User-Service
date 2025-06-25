package userapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userapi.mapper.UserMapper;
import userapi.exception.UserNotFoundException;
import userapi.dto.UserDto;
import userapi.model.User;
import userapi.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());
        updatedUser.setAge(userDto.getAge());

        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Override
    public void deleteUser(Long id) {
        User idUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(idUser.getId());
    }
}
