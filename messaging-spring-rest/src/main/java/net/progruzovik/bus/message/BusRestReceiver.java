package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.IntegrationPlatformResponse;
import net.progruzovik.bus.message.model.RestMessageDto;
import net.progruzovik.bus.message.model.SerializedMessage;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class BusRestReceiver implements RestReceiver {

    private final @NonNull String integrationPlatformUrl;
    private final @NonNull RestTemplate restTemplate;
    private final @NonNull BusHandler busHandler;

    public BusRestReceiver(@NonNull String integrationPlatformUrl,
                           @NonNull RestTemplate restTemplate, @NonNull BusHandler busHandler) {
        this.integrationPlatformUrl = integrationPlatformUrl;
        this.restTemplate = restTemplate;
        this.busHandler = busHandler;
    }

    @Override
    public void receiveMessages(@Nullable Instant fromTime) throws IOException {
        final String requestUrl;
        if (fromTime == null) {
            requestUrl = integrationPlatformUrl;
        } else {
            requestUrl = String.format("%s?after=%d", integrationPlatformUrl, fromTime.toEpochMilli());
        }
        final List<RestMessageDto> messages = restTemplate.getForObject(requestUrl, IntegrationPlatformResponse.class);
        if (messages != null) {
            for (final RestMessageDto message : messages) {
                final SerializedMessage serializedMessage = new SerializedMessage(message.getSubject(), message.getData(), null);
                busHandler.handleMessage(message.getFrom(), serializedMessage);
            }
        }
    }

    @Override
    public void receiveMessages() throws IOException {
        receiveMessages(null);
    }
}
