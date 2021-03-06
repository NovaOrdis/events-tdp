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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.event.EventProperty;
import io.novaordis.events.api.event.GenericTimedEvent;
import io.novaordis.events.api.event.Property;
import io.novaordis.events.api.event.StringProperty;
import io.novaordis.utilities.time.TimestampImpl;

/**
 * An individual thread dump, containing the snapshots of all threads in the JVM at a certain moment in time. A log
 * file (or a stdout dump file) may contain multiple thread dumps, so it may have associated multiple
 * JavaThreadDumpEvent instances.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class JavaThreadDumpEvent extends GenericTimedEvent {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JavaThreadDumpEvent.class);

    private static final DateFormat TIMESTAMP_DISPLAY_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    /**
     * The raw representation at this level is carried in two components: the usual RAW_PROPERTY_NAME property, for
     * anything that comes before the stack traces, and RAW_EPILOGUE_PROPERTY_NAME property for anything that comes
     * after the stack traces. This is necessary so we can reassemble the full raw representation in the correct order.
     */
    public static final String RAW_EPILOGUE_PROPERTY_NAME = "raw-epilogue";

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param timestamp timestamp extracted and parsed by the upper layer.
     */
    public JavaThreadDumpEvent(Long lineNumber, long timestamp) {

        if (lineNumber != null) {

            setLineNumber(lineNumber);
        }

        setTimestamp(new TimestampImpl(timestamp));

        log.debug(this + " constructed");
    }

    // Overrides -------------------------------------------------------------------------------------------------------

    /**
     * We need to override the method because JavaThreadDumpEvent maintains its raw representation split between itself
     * and its component StackTraceEvents.
     */
    @Override
    public String getRawRepresentation() {

        String s = super.getRawRepresentation();

        for(StackTraceEvent se: getStackTraceEvents()) {

            s += "\n";
            s += se.getRawRepresentation();
        }

        StringProperty ep = getStringProperty(RAW_EPILOGUE_PROPERTY_NAME);
        String epilogue;

        if (ep != null && (epilogue = ep.getString()) != null) {

            s += "\n";
            s += epilogue;
        }

        return s;
    }

    /**
     * If we carry no stack traces we prefer not to be displayed at all, otherwise we default to getRawRepresentation().
     */
    @Override
    public String getPreferredRepresentation(String fieldSeparator) {

        if (getThreadCount() == 0) {

            //
            // display nothing
            //

            return "";
        }

        //
        // defer to raw representation
        //

        return null;
    }

    // Public ----------------------------------------------------------------------------------------------------------

    /**
     * Preserves the order in which the stack traces were added.
     *
     * @exception IllegalArgumentException if the event is not a StackTraceEvent
     */
    public void addStackTraces(List<Event> stackTraces) {

        if (stackTraces == null) {

            throw new IllegalArgumentException("null stack trace list");
        }

        if (stackTraces.isEmpty()) {

            return;
        }

        for(Event e: stackTraces) {

            if (e instanceof StackTraceEvent) {

                addStackTrace((StackTraceEvent)e);
            }
            else {

                throw new IllegalArgumentException("not a StackTraceEvent: " + e);
            }
        }
    }

    /**
     * Preserves the order in which the stack traces were added.
     */
    public void addStackTrace(StackTraceEvent stackTrace) {

        if (stackTrace == null) {

            throw new IllegalArgumentException("null stack trace");
        }

        String tid = stackTrace.getTid();
        if (tid == null) {

            tid = UUID.randomUUID().toString();
        }

        setEventProperty(tid, stackTrace);
    }

    /**
     * @return the stack traces in order in which they were added.
     */
    public List<StackTraceEvent> getStackTraceEvents() {

        List<Property> properties = getProperties(StackTraceEvent.class);

        if (properties.isEmpty()) {

            return Collections.emptyList();
        }

        List<StackTraceEvent> result = new ArrayList<>(properties.size());

        //noinspection Convert2streamapi
        for(Property p: properties) {

            result.add((StackTraceEvent)p.getValue());
        }

        return result;
    }

    public int getThreadCount() {

        return getProperties(StackTraceEvent.class).size();
    }

    /**
     * @param index the index of the stack trace as it appears in the thread dump: first stack trace has the index 0,
     *              the next one 1, etc. If there is no corresponding stack trace, the method does not throw exception,
     *              but simply returns null, unless the index is obviously invalid - such as a negative index.
     *
     * @exception IllegalArgumentException on invalid index
     */
    public StackTraceEvent getStackTraceEvent(int index) throws IllegalArgumentException {

        if (index < 0) {

            throw new IllegalArgumentException("invalid index: " + index);
        }

        int i = 0;
        for(Property p: getProperties(StackTraceEvent.class)) {

            if (i == index) {

                return ((StackTraceEvent) ((EventProperty) p).getEvent());
            }

            i ++;
        }

        return null;
    }

    @Override
    public String toString() {

        Long time = getTime();

        if (time == null) {

            return "ThreadDump[UNINITIALIZED]";
        }

        Long lineNumber = getLineNumber();

        return "ThreadDump[" +
                (lineNumber != null ? "line " + lineNumber + ", "  : "") +
                TIMESTAMP_DISPLAY_FORMAT.format(time) + "]";
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
