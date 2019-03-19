package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.IntegrationPlatformResponse;
import net.progruzovik.bus.message.model.RestMessageDto;
import net.progruzovik.bus.message.model.SerializedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class BusRestReceiver implements RestReceiver {

    private static final Logger log = LoggerFactory.getLogger(BusRestReceiver.class);

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
    public Instant receiveMessages(@Nullable Instant fromTime) {
        String requestUrl = String.format("%s?to=%s", integrationPlatformUrl, busHandler.getAddress());
        if (fromTime != null) {
            requestUrl += String.format("&after=%d", fromTime.toEpochMilli());
        }
        Instant timeBeforeCheck = Instant.now();
        List<RestMessageDto> messages = restTemplate.getForObject(requestUrl, IntegrationPlatformResponse.class);
        if (messages != null) {
            for (final RestMessageDto message : messages) {
                try {
                    SerializedMessage serializedMessage = new SerializedMessage(message.getSubject(), message.getData(), null);
                    busHandler.handleMessage(message.getFrom(), serializedMessage);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        return timeBeforeCheck;
    }

    @NonNull
    @Override
    public Instant receiveMessages() {
        return receiveMessages(null);
    }
}
