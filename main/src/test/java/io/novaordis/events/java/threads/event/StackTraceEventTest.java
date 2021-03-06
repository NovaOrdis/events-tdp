/*
 * Copyright (c) 2017 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.events.java.threads.event;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import io.novaordis.events.api.event.StringProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceEventTest {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

    // Tests -----------------------------------------------------------------------------------------------------------

    @Test
    public void identity_Empty() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertEquals(7L, e.getLineNumber().longValue());
        assertNull(e.getThreadName());
        assertNull(e.getOsPrio());
        assertNull(e.getTidAsLong());
        assertNull(e.getTid());
        assertNull(e.getNidAsInt());
    }

    @Test
    public void identity() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);
        e.setThreadName("test thread name");
        e.setOsPrio(0);
        e.setTid("0x00007f6220025000");
        e.setNid("0x1829");
        e.setThreadState("runnable");

        assertEquals(7L, e.getLineNumber().longValue());
        assertEquals("test thread name", e.getThreadName());
        assertEquals(0, e.getOsPrio().intValue());
        assertEquals(StackTraceEvent.longFromHexString("0x00007f6220025000"), e.getTidAsLong().longValue());
        assertEquals("0x00007f6220025000", e.getTid());
        assertEquals(StackTraceEvent.intFromHexString("0x1829"), e.getNidAsInt().intValue());
        assertEquals("0x1829", e.getNid());
        assertEquals(ThreadState.RUNNABLE, e.getThreadState());
    }

    // thread name -----------------------------------------------------------------------------------------------------

    @Test
    public void threadName() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getThreadName());

        e.setThreadName("something");
        assertEquals("something", e.getThreadName());

        e.setThreadName("something else");
        assertEquals("something else", e.getThreadName());
    }

    // tid -------------------------------------------------------------------------------------------------------------

    @Test
    public void tid() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getTid());
        assertNull(e.getTidAsLong());

        String hexString = "0x00007f6220025000";

        e.setTid(hexString);

        assertEquals(hexString, e.getTid());

        long tid = StackTraceEvent.longFromHexString(hexString);

        assertEquals(tid, e.getTidAsLong().longValue());
    }

    @Test
    public void tid_InvalidHexStringValue() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getTid());
        assertNull(e.getTidAsLong());

        try {

            e.setTid("something that does not make any sense");
            fail("should have thrown exception");
        }
        catch(NumberFormatException iae) {

            String msg = iae.getMessage();
            assertTrue(msg.contains("something"));
        }

        assertNull(e.getTid());
        assertNull(e.getTidAsLong());
    }

    // longFromHexString() ---------------------------------------------------------------------------------------------

    @Test
    public void longFromHexString_InvalidValue() throws Exception {

        try {

            StackTraceEvent.longFromHexString("something");
            fail("should have thrown exception");
        }
        catch(NumberFormatException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("something"));
        }
    }

    @Test
    public void longFromHexString_Leading0X() throws Exception {

        long l = StackTraceEvent.longFromHexString("0Xf");
        assertEquals(15L, l);
    }

    @Test
    public void longFromHexString_Leading0x() throws Exception {

        long l = StackTraceEvent.longFromHexString("0xf");
        assertEquals(15L, l);
    }

    // intFromHexString() ----------------------------------------------------------------------------------------------

    @Test
    public void intFromHexString_InvalidValue() throws Exception {

        try {

            StackTraceEvent.intFromHexString("something");
            fail("should have thrown exception");
        }
        catch(NumberFormatException e) {

            String msg = e.getMessage();
            assertTrue(msg.contains("something"));
        }
    }

    @Test
    public void intFromHexString_Leading0X() throws Exception {

        int i = StackTraceEvent.intFromHexString("0Xf");
        assertEquals(15, i);
    }

    @Test
    public void intFromHexString_Leading0x() throws Exception {

        int i = StackTraceEvent.intFromHexString("0xf");
        assertEquals(15, i);
    }

    // nid -------------------------------------------------------------------------------------------------------------

    @Test
    public void nid() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getNid());
        assertNull(e.getNidAsInt());

        String hexString = "0x1829";

        e.setNid(hexString);

        assertEquals(hexString, e.getNid());

        int nid = StackTraceEvent.intFromHexString(hexString);

        assertEquals(nid, e.getNidAsInt().intValue());
    }

    @Test
    public void nid_InvalidHexStringValue() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getNid());
        assertNull(e.getNidAsInt());

        try {

            e.setNid("something that does not make any sense");
            fail("should have thrown exception");
        }
        catch(NumberFormatException iae) {

            String msg = iae.getMessage();
            assertTrue(msg.contains("something"));
        }

        assertNull(e.getNid());
        assertNull(e.getNidAsInt());
    }

    // thread state ----------------------------------------------------------------------------------------------------

    @Test
    public void threadState_Runnable() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getThreadState());

        String threadState = "runnable";

        e.setThreadState(threadState);

        assertEquals(ThreadState.RUNNABLE, e.getThreadState());
    }

    @Test
    public void threadState_InvalidValue() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getThreadState());

        try {
            e.setThreadState("something that does not make any sense");
            fail("should have thrown exception");
        }
        catch(IllegalArgumentException iae) {

            String msg = iae.getMessage();

            assertTrue(msg.contains("unknown thread state"));
            assertTrue(msg.contains("something"));
        }

        assertNull(e.getThreadState());
    }

    @Test
    public void getThreadState_InvalidValueMaintainedByProperty() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        e.setStringProperty(StackTraceEvent.THREAD_STATE_PROPERTY_NAME, "something");

        try {

            e.getThreadState();
            fail("should have thrown exception");
        }
        catch(IllegalStateException ise) {

            String msg = ise.getMessage();

            assertTrue(msg.contains("invalid stored thread state"));
            assertTrue(msg.contains("something"));
        }
    }

    @Test
    public void setThreadState_InObjectWait() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        e.setThreadState(" in Object.wait() [0x00007f6209147000]");
        assertEquals(ThreadState.OBJECT_WAIT, e.getThreadState());
        assertEquals("0x00007f6209147000", e.getMonitor());
    }

    @Test
    public void setThreadState_Sleeping() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        e.setThreadState(" sleeping[0x00007f013a8e8000]");
        assertEquals(ThreadState.SLEEPING, e.getThreadState());
        assertEquals("0x00007f013a8e8000", e.getMonitor());
    }

    @Test
    public void setThreadState_WaitingForMonitorEntry() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        e.setThreadState("waiting for monitor entry [0x00007f013b5f4000]");
        assertEquals(ThreadState.WAITING_FOR_MONITOR_ENTRY, e.getThreadState());
        assertEquals("0x00007f013b5f4000", e.getMonitor());
    }

    // prio ------------------------------------------------------------------------------------------------------------

    @Test
    public void prio() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertNull(e.getPrio());

        e.setPrio(1);

        assertEquals(1, e.getPrio().intValue());
    }

    // daemon ----------------------------------------------------------------------------------------------------------

    @Test
    public void daemon() throws Exception {

        StackTraceEvent e = new StackTraceEvent(7L);

        assertFalse(e.isDaemon());

        e.setDaemon(false);

        assertFalse(e.isDaemon());

        e.setDaemon(true);

        assertTrue(e.isDaemon());
    }

    // update() --------------------------------------------------------------------------------------------------------

    @Test
    public void update() throws Exception {

        long lineNumber = 10L;

        StackTraceEvent e = new StackTraceEvent(lineNumber);

        String threadStateLine = "   java.lang.Thread.State: RUNNABLE";

        String stack =
                "        at sun.awt.windows.WToolkit.eventLoop(Native Method)\n" +
                        "        at sun.awt.windows.WToolkit.run(WToolkit.java:306)\n" +
                        "        at java.lang.Thread.run(Thread.java:745)";

        String lockingInfo =
                "   Locked ownable synchronizers:\n" +
                        "        - None";

        String content =
                threadStateLine + "\n" +
                        stack + "\n" + "\n" +
                        lockingInfo + "\n";

        String contentWithTerminator = content + "\n";

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(contentWithTerminator.getBytes())));

        String line;
        while((line = br.readLine()) != null) {

            lineNumber ++;
            assertTrue(e.update(lineNumber, line));
        }

        br.close();

        //
        // make sure the raw representation is correctly updated
        //

        String raw = e.getRawRepresentation();
        assertEquals(content, raw);

        //
        // test the stack
        //

        StringProperty p = e.getStringProperty(StackTraceEvent.STACK_PROPERTY_NAME);
        assertNotNull(p);

        String parsedStack = p.getString();

        assertEquals(stack, parsedStack);
    }

    @Test
    public void update_OneLineStackTraceEvent() throws Exception {

        long lineNumber = 10L;

        StackTraceEvent e = new StackTraceEvent(lineNumber);

        assertTrue(e.update(11L, ""));

        //
        // make sure the raw representation is correctly updated
        //

        String raw = e.getRawRepresentation();
        assertEquals("", raw);

        //
        // test the stack
        //

        StringProperty p = e.getStringProperty(StackTraceEvent.STACK_PROPERTY_NAME);
        assertNull(p);

        assertFalse(e.update(12L, "something"));
    }

    @Test
    public void update_OneLineStackTraceEvent2() throws Exception {

        long lineNumber = 10L;

        StackTraceEvent e = new StackTraceEvent(lineNumber);

        assertTrue(e.update(11L, "  "));

        //
        // make sure the raw representation is correctly updated
        //

        String raw = e.getRawRepresentation();
        assertEquals("  ", raw);

        //
        // test the stack
        //

        StringProperty p = e.getStringProperty(StackTraceEvent.STACK_PROPERTY_NAME);
        assertNull(p);

        assertFalse(e.update(12L, "something"));
    }

    @Test
    public void update_OneLineStackTraceEvent3() throws Exception {

        long lineNumber = 10L;

        StackTraceEvent e = new StackTraceEvent(lineNumber);

        assertTrue(e.update(11L, "\t"));

        //
        // make sure the raw representation is correctly updated
        //

        String raw = e.getRawRepresentation();
        assertEquals("\t", raw);

        //
        // test the stack
        //

        StringProperty p = e.getStringProperty(StackTraceEvent.STACK_PROPERTY_NAME);
        assertNull(p);

        assertFalse(e.update(12L, "something"));
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
