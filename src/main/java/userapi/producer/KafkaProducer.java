package userapi.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import userapi.dto.UserEvent;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUser(String eventType, String email) {
        UserEvent event = new UserEvent(eventType, email);
        kafkaTemplate.send("user-events", event);
    }
}
