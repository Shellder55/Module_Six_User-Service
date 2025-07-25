package userapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import userapi.dto.ErrorResponse;
import userapi.dto.UserDto;
import userapi.service.UserServiceImpl;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "Api для работы с пользователями. Поддерживает CRUD операции и отправку event в Kafka")
public class UserController {
    private final UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Добавить пользователя",
            description = "Добавляет пользователя в базу данных и отправляет UserEvent (event и email) в Kafka"

    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверные данные.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<UserDto>> createUser(@RequestBody UserDto userDto) {
        UserDto user = userService.createUser(userDto);
        EntityModel<UserDto> entityModel = getUserDtoEntityModel(user);

        return ResponseEntity.created(linkTo(methodOn(UserController.class).getUserById(user.getId())).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Найти пользователя",
            description = "Находит пользователя по ID из базы данных и вывод все поля пользователя в консоль"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно найден.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        EntityModel<UserDto> entityModel = getUserDtoEntityModel(user);

        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}/update")
    @Operation(
            summary = "Обновить пользователя",
            description = "Находит пользователя по ID из базы данных. " +
                    "Обновляет его данные и вывод все поля обновленного пользователя в консоль"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Внутреняя ошибка сервера.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<UserDto>> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto user = userService.updateUser(id, userDto);
        EntityModel<UserDto> entityModel = getUserDtoEntityModel(user);

        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}/delete")
    @Operation(
            summary = "Удалить пользователя",
            description = "Находит пользователя по ID из базы данных. " +
                    "Удаляет пользователя из базы данных и отправляет UserEvent (event и email) в Kafka."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален."),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден."),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.")
    })
    public ResponseEntity<RepresentationModel<?>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        RepresentationModel<?> representationModel = new RepresentationModel<>();
        representationModel.add(linkTo(methodOn(UserController.class).createUser(null)).withRel("create-user"));

        return ResponseEntity.ok(representationModel);
    }

    private static EntityModel<UserDto> getUserDtoEntityModel(UserDto user) {
        EntityModel<UserDto> entityModel = EntityModel.of(user);
        entityModel.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
        entityModel.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), user)).withRel("update"));
        entityModel.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
        return entityModel;
    }
}
