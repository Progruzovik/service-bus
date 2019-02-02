package net.progruzovik.bus.message;

class DuplicateCustomReaderException extends IllegalArgumentException {

    DuplicateCustomReaderException(String subject) {
        super(String.format("Reader for subject \"%s\" already exists!", subject));
    }
}
