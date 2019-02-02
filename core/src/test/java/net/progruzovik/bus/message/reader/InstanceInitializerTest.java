package net.progruzovik.bus.message.reader;

import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.InstanceInitializer;
import net.progruzovik.bus.message.MessageHandlerTest;
import net.progruzovik.bus.message.Writer;
import net.progruzovik.bus.message.model.DataMessage;
import net.progruzovik.bus.message.model.SerializedMessage;
import net.progruzovik.bus.message.model.Subject;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class InstanceInitializerTest {

    private final Writer writer = mock(Writer.class);
    private final InstanceDao instanceDao = mock(InstanceDao.class);
    private final InstanceInitializer initializer = new InstanceInitializer(writer, instanceDao);
    private final SerializedMessage message = new SerializedMessage(Subject.INIT_INSTANCE.toString(), "address", null);

    @Before
    public void setUp() {
        reset(writer);
        reset(instanceDao);
    }

    @Test
    public void initializeInstance() {
        initializer.readMessage(MessageHandlerTest.ADDRESS, message);
        verify(writer).broadcastMessage(new DataMessage<>(Subject.ADD_INSTANCE, MessageHandlerTest.ADDRESS));
        verify(instanceDao).isInstanceInitialized(eq(MessageHandlerTest.ADDRESS));
        verify(instanceDao).addInstance(eq(MessageHandlerTest.ADDRESS));
        verify(instanceDao).getAddresses();
        verify(instanceDao).getEntityNames();
    }

    @Test
    public void initializeInstanceTwice() {
        when(instanceDao.isInstanceInitialized(MessageHandlerTest.ADDRESS)).thenReturn(true);
        initializer.readMessage(MessageHandlerTest.ADDRESS, message);
        verify(writer).responseWithError(MessageHandlerTest.ADDRESS, message);
    }
}
