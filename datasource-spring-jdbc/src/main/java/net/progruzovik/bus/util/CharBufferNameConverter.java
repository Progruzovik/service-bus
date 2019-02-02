package net.progruzovik.bus.util;

import org.springframework.lang.NonNull;

import java.nio.CharBuffer;

public class CharBufferNameConverter implements EntityNameConverter {

    @NonNull
    @Override
    public String toDatabase(String name) {
        if (name.equals(name.toUpperCase()) || name.equals(name.toLowerCase())) return name;
        final StringBuilder databaseName = new StringBuilder();
        final CharBuffer buffer = CharBuffer.wrap(name);
        char letter;
        while (buffer.hasRemaining()) {
            letter = buffer.get();
            if (letter >= 'A' && letter <= 'Z') {
                databaseName.append('_');
            }
            databaseName.append(Character.toLowerCase(letter));
        }
        return databaseName.toString();
    }

    @NonNull
    @Override
    public String fromDatabase(String name) {
        final StringBuilder busName = new StringBuilder();
        final CharBuffer buffer = CharBuffer.wrap(name);
        char letter;
        boolean isNewWord = false;
        while (buffer.hasRemaining()) {
            letter = buffer.get();
            if (letter == '_') {
                isNewWord = true;
            } else {
                busName.append(isNewWord ? Character.toUpperCase(letter) : Character.toLowerCase(letter));
                isNewWord = false;
            }
        }
        return busName.toString();
    }
}
