package userapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userapi.dto.UserDto;
import userapi.handler.exception.EmailExistsException;
import userapi.handler.exception.UserNotFoundException;
import userapi.mapper.UserMapper;
import userapi.model.User;
import userapi.producer.KafkaProducer;
import userapi.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProducer kafkaProducer;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        if (userRepository.existsByEmail(userDto.getEmail())) throw new EmailExistsException();

        UserDto savedUser = userMapper.toDto(userRepository.save(user));

        kafkaProducer.sendUser("USER_CREATED", savedUser.getEmail());
        return savedUser;
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (userRepository.existsByEmail(userDto.getEmail())) throw new EmailExistsException();

        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());
        updatedUser.setAge(userDto.getAge());

        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        kafkaProducer.sendUser("USER_DELETED", user.getEmail());
        userRepository.deleteById(user.getId());
    }
}
