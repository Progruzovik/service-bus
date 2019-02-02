package net.progruzovik.bus.util;

import org.springframework.lang.NonNull;

public interface EntityNameConverter {

    @NonNull
    String toDatabase(String name);

    @NonNull
    String fromDatabase(String name);
}
