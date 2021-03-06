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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.events.api.event.BooleanProperty;
import io.novaordis.events.api.event.GenericEvent;
import io.novaordis.events.api.event.IntegerProperty;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.utilities.parsing.ParsingException;

/**
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class StackTraceEvent extends GenericEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(StackTraceEvent.class);

    public static final String THREAD_NAME_PROPERTY_NAME = "thread-name";
    public static final String OS_PRIO_PROPERTY_NAME = "os-prio";
    public static final String PRIO_PROPERTY_NAME = "prio";

    // the TID is maintained internally as a hexadecimal string
    public static final String TID_PROPERTY_NAME = "tid";

    // the NID is maintained internally as a hexadecimal string
    public static final String NID_PROPERTY_NAME = "nid";

    public static final String THREAD_STATE_PROPERTY_NAME = "thread-state";

    public static final String DAEMON_PROPERTY_NAME = "daemon";

    //
    // if the thread is in "Object.wait()" state, this property may carry the monitor the thread is waiting on.
    //
    public static final String OBJECT_WAIT_MONITOR_PROPERTY_NAME = "object-wait-monitor";

    //
    // the literal, multi-line stack, as read from the dump
    //
    public static final String STACK_PROPERTY_NAME = "stack";

    // Static ----------------------------------------------------------------------------------------------------------

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid long.
     */
    public static long longFromHexString(String s) throws NumberFormatException {

        if (s.startsWith("0x") || s.startsWith("0X")) {

            s = s.substring(2);
        }

        return Long.parseUnsignedLong(s, 16);
    }

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid int.
     */
    public static int intFromHexString(String s) throws NumberFormatException {

        if (s.startsWith("0x") || s.startsWith("0X")) {

            s = s.substring(2);
        }

        return Integer.parseUnsignedInt(s, 16);
    }

    private static final byte THREAD_STATE_MODE = 0;
    private static final byte STACK_MODE = 1;
    private static final byte LOCKING_INFO_MODE = 2;
    private static final byte CLOSED_MODE = 3;

    // Attributes ------------------------------------------------------------------------------------------------------

    private byte mode = THREAD_STATE_MODE;
    private transient String stack;

    // Constructors ----------------------------------------------------------------------------------------------------

    public StackTraceEvent(Long lineNumber) {

        setLineNumber(lineNumber);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public String getThreadName() {

        StringProperty p =  getStringProperty(THREAD_NAME_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    public void setThreadName(String s) {

        if (log.isDebugEnabled()) {

            log.debug(this + " setting " + THREAD_NAME_PROPERTY_NAME + " to " + s);
        }

        setStringProperty(THREAD_NAME_PROPERTY_NAME, s);
    }

    public Integer getOsPrio() {

        IntegerProperty p =  getIntegerProperty(OS_PRIO_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getInteger();
    }

    public void setOsPrio(int i) {

        if (log.isDebugEnabled()) {

            log.debug(this + " setting " + OS_PRIO_PROPERTY_NAME + " to " + i);
        }

        setIntegerProperty(OS_PRIO_PROPERTY_NAME, i);
    }

    public Integer getPrio() {

        IntegerProperty p =  getIntegerProperty(PRIO_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getInteger();
    }

    public void setPrio(int i) {

        if (log.isDebugEnabled()) {

            log.debug(this + " setting " + PRIO_PROPERTY_NAME + " to " + i);
        }

        setIntegerProperty(PRIO_PROPERTY_NAME, i);
    }

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid long.
     */
    public void setTid(String hexadecimalString) throws NumberFormatException {

        //
        // sanity check - if it does not throw exception, we're good
        //
        longFromHexString(hexadecimalString);

        if (log.isDebugEnabled()) {

            log.debug(this + " setting " + TID_PROPERTY_NAME + " to " + hexadecimalString);
        }

        setStringProperty(TID_PROPERTY_NAME, hexadecimalString);
    }

    /**
     * @return the thread ID. May return null, which means the thread ID could not be extracted from the stack trace.
     */
    public Long getTidAsLong() {

        String s = getTid();

        if (s == null) {

            return null;
        }

        return longFromHexString(s);
    }

    /**
     * @return the thread ID in a hexadecimal representation, similar to the one recorded in the stack trace. May
     * return null, which means the thread ID could not be extracted from the stack trace.
     */
    public String getTid() {

        StringProperty p = getStringProperty(TID_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    /**
     * @throws NumberFormatException if the hexadecimal string cannot be converted to a valid int.
     */
    public void setNid(String hexadecimalString) throws NumberFormatException {

        //
        // sanity check - if it does not throw exception, we're good
        //
        intFromHexString(hexadecimalString);

        if (log.isDebugEnabled()) {

            log.debug(this + " setting " + NID_PROPERTY_NAME + " to " + hexadecimalString);
        }

        setStringProperty(NID_PROPERTY_NAME, hexadecimalString);
    }

    /**
     * @return the NID. May return null, which means the NID could not be extracted from the stack trace.
     */
    public Integer getNidAsInt() {

        String s = getNid();

        if (s == null) {

            return null;
        }

        return intFromHexString(s);
    }

    /**
     * @return the NID in a hexadecimal representation, similar to the one recorded in the stack trace. May return null,
     * which means the NID could not be extracted from the stack trace.
     */
    public String getNid() {

        StringProperty p = getStringProperty(NID_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    /**
     * The method updates the event with details extracted from the state string.
     *
     * @param threadStateRepresentation a string representing the thread state, as reflected by the stack trace. May
     *                                  be one of the following: "runnable", "in Object.wait() [0x00007f6209147000]",
     *                                  etc.
     *
     * @throws IllegalArgumentException if the thread state string cannot be converted to a known ThreadState
     */
    public void setThreadState(String threadStateRepresentation) throws IllegalArgumentException {

        threadStateRepresentation = threadStateRepresentation.trim();

        ThreadState ts = ThreadState.fromString(threadStateRepresentation);

        if (ts == null) {

            throw new IllegalArgumentException("unknown thread state: " + threadStateRepresentation);
        }
        else {

            if (log.isDebugEnabled()) {

                log.debug(this + " setting " + THREAD_STATE_PROPERTY_NAME + " to " + ts);
            }

            setStringProperty(THREAD_STATE_PROPERTY_NAME, ts.toString());
            ThreadState.setMonitor(this, threadStateRepresentation);
        }
    }

    /**
     * @throws IllegalStateException in case the thread state stored by the event is invalid.
     */
    public ThreadState getThreadState() throws IllegalStateException {

        StringProperty p = getStringProperty(THREAD_STATE_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        String s = p.getString();

        if (s == null) {

            return null;
        }

        ThreadState ts = ThreadState.fromString(s);

        if (ts == null) {

            throw new IllegalStateException("invalid stored thread state: " + s);
        }

        return ts;
    }

    public void setDaemon(boolean isDaemon) {

        if (isDaemon) {

            setBooleanProperty(DAEMON_PROPERTY_NAME, true);
        }
        else {

            removeBooleanProperty(DAEMON_PROPERTY_NAME);
        }
    }

    public boolean isDaemon() {

        BooleanProperty p = getBooleanProperty(DAEMON_PROPERTY_NAME);

        if (p == null) {

            return false;
        }

        Boolean b = p.getBoolean();

        if (b == null) {

            return false;
        }

        return b;
    }

    /**
     * If the thread is in ThreadState.OBJECT_WAIT state, this method returns the monitor the thread is waiting on,
     * if available. May return null.
     */
    public String getMonitor() {

        StringProperty p = getStringProperty(OBJECT_WAIT_MONITOR_PROPERTY_NAME);

        if (p == null) {

            return null;
        }

        return p.getString();
    }

    public void setObjectWaitMonitor(String s) {

        setStringProperty(OBJECT_WAIT_MONITOR_PROPERTY_NAME, s);
    }

    /**
     * After the stack trace event header is identified and parsed by the StackTraceParser, based on the header pattern,
     * the parser sends incoming lines to the event for interpretation. These lines contain the thread state, the raw
     * stack trace lines, locking information, etc; this includes empty lines as well. The parser should not send
     * another stack trace header or another thread dump header, but if it does, update() must return false
     * ("not accepted")
     *
     * The method updates the raw representation of the event, as well.
     *
     * @return true if the line is accepted, false if it does not belong with this event.
     *
     * @throws ParsingException
     */
    public boolean update(long lineNumber, String line) throws ParsingException {

        appendRawLine(line);

        if (mode == THREAD_STATE_MODE) {

            line = line.trim();

            if (line.isEmpty()) {

                //
                // "short" stack trace event
                //
                mode = CLOSED_MODE;
            }
            else {

                if (!line.startsWith("java.lang.Thread.State")) {

                    log.warn("line " + lineNumber + ": expecting thread state information but got \"" + line + "\"");
                }

                //
                // we don't do anything with it just yet
                //

                mode = STACK_MODE;
            }
        }
        else if (mode == STACK_MODE) {

            //
            // accumulate stack up to the first empty line
            //

            if (line.trim().isEmpty()) {

                //
                // transfer the stack to a property
                //

                if (stack != null) {

                    setStringProperty(STACK_PROPERTY_NAME, stack);
                }

                mode = LOCKING_INFO_MODE;
            }
            else {

                if (stack == null) {

                    stack = line;
                }
                else {

                    stack += "\n";
                    stack += line;
                }
            }
        }
        else if (mode == LOCKING_INFO_MODE) {

            //
            // simply drop for the time being
            //

            if (log.isDebugEnabled()) {

                log.debug("discarding logging information");
            }
        }
        else if (mode == CLOSED_MODE) {

            return false;
        }
        else {

            throw new IllegalStateException("illegal mode: " + mode);
        }

        return true;
    }

    @Override
    public String toString() {

        return "StackTraceEvent[" + getThreadName() + ", line " + getLineNumber() + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
