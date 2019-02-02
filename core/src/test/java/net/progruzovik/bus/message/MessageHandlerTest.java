package net.progruzovik.bus.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.progruzovik.bus.dao.EntityDao;
import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.model.SerializedMessage;
import net.progruzovik.bus.message.model.Subject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class MessageHandlerTest {

    public static final String ADDRESS = "testAddress";

    private final ObjectMapper mapper = new ObjectMapper();

    private final Writer writer = mock(Writer.class);
    private final InstanceDao instanceDao = mock(InstanceDao.class);
    private final EntityDao entityDao = mock(EntityDao.class);
    private final MessageHandler messageHandler = new MessageHandler(writer, instanceDao, entityDao);

    @Before
    public void setUp() {
        reset(writer);
        reset(instanceDao);
        reset(entityDao);
    }

    @Test(expected = DuplicateCustomReaderException.class)
    public void createHandlerWithDuplicateCustomReader() {
        final Map<String, Reader> customReaders = new HashMap<>(1);
        final Reader testReader = mock(Reader.class);
        customReaders.put(Subject.INIT_INSTANCE.toString(), testReader);
        new MessageHandler(writer, instanceDao, entityDao, customReaders);
    }

    @Test
    public void handleUnknownMessage() throws Exception {
        final SerializedMessage message = new SerializedMessage("testSubject", "testData", null, mapper);
        messageHandler.handleMessage(ADDRESS, message);
        verify(writer).responseWithError(ADDRESS, message);
    }

    @Test
    public void invokeCustomReader() throws Exception {
        final Map<String, Reader> customReaders = new HashMap<>(1);
        final Reader testReader = mock(Reader.class);
        customReaders.put("testSubject", testReader);
        final MessageHandler messageHandler = new MessageHandler(writer, instanceDao, entityDao, customReaders);
        final SerializedMessage message = new SerializedMessage("testSubject", "testData", null, mapper);
        messageHandler.handleMessage(ADDRESS, message);
        verify(testReader).readMessage(ADDRESS, message);
    }

    @Test(expected = DuplicateCustomReaderException.class)
    public void addDuplicateReader() {
        final Map<String, Reader> customReaders = new HashMap<>(1);
        final Reader testReader = mock(Reader.class);
        customReaders.put(Subject.INIT_INSTANCE.toString(), testReader);
        new MessageHandler(writer, instanceDao, entityDao, customReaders);
    }
}
