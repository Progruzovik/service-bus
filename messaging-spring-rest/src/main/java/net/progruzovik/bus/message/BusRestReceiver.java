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

    @NonNull
    @Override
    public Instant receiveMessages(@Nullable Instant fromTime) throws IOException {
        String requestUrl = String.format("%s?to=%s", integrationPlatformUrl, busHandler.getAddress());
        if (fromTime != null) {
            requestUrl += String.format("&after=%d", fromTime.toEpochMilli());
        }
        final Instant timeBeforeCheck = Instant.now();
        final List<RestMessageDto> messages = restTemplate.getForObject(requestUrl, IntegrationPlatformResponse.class);
        if (messages != null) {
            for (final RestMessageDto message : messages) {
                final SerializedMessage serializedMessage = new SerializedMessage(message.getSubject(), message.getData(), null);
                busHandler.handleMessage(message.getFrom(), serializedMessage);
            }
        }
        return timeBeforeCheck;
    }

    @NonNull
    @Override
    public Instant receiveMessages() throws IOException {
        return receiveMessages(null);
    }
}
