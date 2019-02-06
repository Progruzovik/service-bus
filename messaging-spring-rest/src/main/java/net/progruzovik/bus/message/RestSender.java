package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.RestMessageDto;
import net.progruzovik.bus.message.model.SerializedMessage;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

public class RestSender implements MessageSender {

    private final @NonNull String address;
    private final @NonNull String integrationPlatformUrl;
    private final @NonNull RestTemplate restTemplate;

    public RestSender(@NonNull String address, @NonNull String integrationPlatformUrl, @NonNull RestTemplate restTemplate) {
        this.address = address;
        this.integrationPlatformUrl = integrationPlatformUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    @NonNull
    public String getAddress() {
        return address;
    }

    @Override
    public void sendMessage(@NonNull String to, @NonNull SerializedMessage message) {
        final RestMessageDto restMessage = new RestMessageDto(address, to, message.getSubject(), message.getData());
        restTemplate.postForEntity(integrationPlatformUrl, restMessage, String.class);
    }
}
