package userapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Формат ошибки API")
public class ErrorResponse {
    @Schema(description = "HTTP-статус код")
    private int status;

    @Schema(description = "Тип ошибки")
    private String error;

    @Schema(description = "Сообщение об ошибке")
    private String message;
}
