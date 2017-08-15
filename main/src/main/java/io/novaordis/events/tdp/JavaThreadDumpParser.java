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

package io.novaordis.events.tdp;

import io.novaordis.events.api.event.EndOfStreamEvent;
import io.novaordis.events.api.event.Event;
import io.novaordis.events.api.parser.ParserBase;
import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.events.tdp.event.JavaThreadDumpEvent;
import io.novaordis.events.tdp.event.StackTraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation is NOT thread safe.
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 8/14/17
 */
public class JavaThreadDumpParser extends ParserBase {

    // Constants -------------------------------------------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(JavaThreadDumpParser.class);

    public static final String[] THREAD_DUMP_TIMESTAMP_FORMAT_STRINGS = new String[] {

            // 2016-08-13 17:42:10
            "yyyy-MM-dd HH:mm:ss",
    };

    public static final Pattern[] THREAD_DUMP_TIMESTAMP_PATTERNS = new Pattern[] {

            // 2016-08-13 17:42:10
            Pattern.compile("^[1-3]\\d\\d\\d-[0-1]\\d-[0-3]\\d [0-2]\\d:[0-5]\\d:[0-5]\\d *$"),
    };

    public static final DateFormat[] THREAD_DUMP_TIMESTAMP_FORMATS = new DateFormat[] {

            // 2016-08-13 17:42:10
            new SimpleDateFormat(THREAD_DUMP_TIMESTAMP_FORMAT_STRINGS[0]),
    };

    //
    // IMPORTANT: every time a new timestamp pattern is added, the corresponding String, Format and a Pattern must be
    //            added on the same position in the corresponding arrays.
    //

    //
    // Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.51-b03 mixed mode):
    //

    public static final Pattern[] THREAD_DUMP_HEADER_PATTERNS = new Pattern[] {

            Pattern.compile("^Full thread dump.*$"),
    };

    private static final List<Event> EMPTY_EVENT_LIST = Collections.emptyList();

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private StackTraceParser stackTraceParser;

    private String threadDumpTimestamp;

    private JavaThreadDumpEvent currentJavaThreadDumpEvent;

    private boolean discardEmptyLine;

    // Constructors ----------------------------------------------------------------------------------------------------

    public JavaThreadDumpParser() {

        this.stackTraceParser = new StackTraceParser();
    }

    // ParserBase overrides --------------------------------------------------------------------------------------------

    @Override
    protected List<Event> parse(long lineNumber, String line) throws ParsingException {

        if (log.isDebugEnabled()) {

            log.debug("parsing line " + lineNumber + ": " + line);
        }

        List<Event> result = null;

        //
        // a thread dump event is marked by a consecutive succession of a timestamp, a header and an empty line
        //

        if (discardEmptyLine) {

            //
            // the previous line was a thread dump header and we expect this line to be empty
            //

            discardEmptyLine = false;

            if (line.trim().isEmpty()) {

                if (log.isDebugEnabled()) {

                    log.debug("discarded empty line " + lineNumber);
                }
            }
            else {

                //
                // we are expecting an empty line and we got a non-empty line, the format of the thread dump is
                // invalid, drop the current thread dump event and warn through the rest of the lines associated
                // with this thread dump event
                //

                log.warn("line " + lineNumber + " is invalid: expecting an empty line that follows a thread dump header but got: " + line);
                currentJavaThreadDumpEvent = null;
            }

        }
        else if (threadDumpTimestamp != null) {

            //
            // we identified a thread dump timestamp on the previous line, this line must be a thread dump header

            Matcher m = THREAD_DUMP_HEADER_PATTERNS[0].matcher(line);

            if (!m.find()) {

                threadDumpTimestamp = null;
                log.warn("skipping thread dump started at line " + (lineNumber - 1) + " because thread dump header missing on line " + lineNumber + ": " + line);
            }
            else {

                if (log.isDebugEnabled()) {

                    log.debug("thread dump header found, building the thread dump event ...");
                }

                //
                // parse the timestamp, we keep the format at this layer
                //

                long timestamp;

                try {

                    timestamp = THREAD_DUMP_TIMESTAMP_FORMATS[0].parse(threadDumpTimestamp).getTime();
                }
                catch(ParseException e) {

                    //
                    // this indicates a programming error, as the correct pattern was already identified; it means
                    // there is a mismatch between pattern and format, so we signal invalid state
                    //

                    throw new IllegalStateException(
                            "mismatch between thread dump timestamp pattern and format, line: " + lineNumber, e);
                }

                threadDumpTimestamp = null;
                currentJavaThreadDumpEvent = new JavaThreadDumpEvent(lineNumber, timestamp);
                discardEmptyLine = true;
            }
        }
        else {

            //
            // TODO: currently we only check a single set of patterns, generalize when we need to check the second
            //

            Matcher m = THREAD_DUMP_TIMESTAMP_PATTERNS[0].matcher(line);

            if (m.matches()) {

                //
                // we identified a new thread dump in the same file, put the thread dump parser in "expect a header
                // line" mode ...
                //

                this.threadDumpTimestamp = line.trim();

                if (log.isDebugEnabled()) {

                    log.debug("thread dump timestamp found: " + threadDumpTimestamp);
                }

                if (currentJavaThreadDumpEvent != null) {

                    //
                    // since we established that another thread dump is starting, we are wrapping up the current thread
                    // dump event, if any. Collect all leftovers from the stack trace parser, but don't close the stack
                    // trace parser, we'll need it for the upcoming thread dump events
                    //

                    List<Event> stackTraces = stackTraceParser.flush();
                    currentJavaThreadDumpEvent.addStackTraces(stackTraces);
                    result = Collections.singletonList(currentJavaThreadDumpEvent);

                    if (log.isDebugEnabled()) {

                        log.debug(currentJavaThreadDumpEvent.toString() + " parsing complete");
                    }

                    currentJavaThreadDumpEvent = null;
                }
            }
            else {

                if (currentJavaThreadDumpEvent == null) {

                    //
                    // we ignore this line, there's nothing we can do with it; normally there should be no ignored
                    // lines in a valid thread dump file, so we make it visible and warn
                    //

                    log.warn("discarding line " + lineNumber + ": " + line);
                }
                else {

                    //
                    // engage the stack trace parser and identify individual stack traces
                    //

                    List<Event> stackTraces = stackTraceParser.parse(lineNumber, line);
                    currentJavaThreadDumpEvent.addStackTraces(stackTraces);
                }
            }
        }

        if (result == null) {

            return EMPTY_EVENT_LIST;
        }
        else {

            return result;
        }
    }

    @Override
    protected List<Event> close(long lineNumber) throws ParsingException {

        if (currentJavaThreadDumpEvent == null) {

            return EMPTY_EVENT_LIST;
        }

        //
        // collect all leftovers from the stack trace parser
        //

        List<Event> stackTraces = stackTraceParser.close();

        for(Event e: stackTraces) {

            if (e instanceof EndOfStreamEvent) {

                break;
            }

            StackTraceEvent ste = (StackTraceEvent)e;
            currentJavaThreadDumpEvent.addStackTrace(ste);
        }

        return Collections.singletonList(currentJavaThreadDumpEvent);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    // Inner classes ---------------------------------------------------------------------------------------------------

}
