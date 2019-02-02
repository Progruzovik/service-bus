package net.progruzovik.bus.message.model;

public enum Subject {
    INIT_INSTANCE,
    ADD_INSTANCE,
    REMOVE_INSTANCE,

    ADD_ENTITY,
    REMOVE_ENTITY,

    UPDATE_SUBSCRIPTION,
    CREATE_ENTITY,
    ADD_ROW,
    REMOVE_ROW,

    ECHO_REQUEST,
    ECHO_RESPONSE,
    ERROR_RESPONSE
}
