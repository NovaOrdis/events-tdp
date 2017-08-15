package io.novaordis.events.tdp;

import io.novaordis.events.api.parser.ParsingException;
import io.novaordis.utilities.UserErrorException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A log file (or a stdout dump file) containing one or more thread dumps.
 *
 * @see ThreadDump
 *
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 *
 * Copyright 2010 Ovidiu Feodorov
 */
@Deprecated
public class ThreadDumpFile {

    // Constants -------------------------------------------------------------------------------------------------------

    public static final String TIMESTAMP_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public static final Format TIMESTAMP_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT_STRING);

    // Static ----------------------------------------------------------------------------------------------------------

    // Attributes ------------------------------------------------------------------------------------------------------

    private File file;

    private List<ThreadDump> threadDumps;

    // Constructors ----------------------------------------------------------------------------------------------------

    /**
     * @param fileName a thread dump file name
     *
     * @throws IOException
     * @throws UserErrorException
     */
    public ThreadDumpFile(String fileName) throws IOException, ParsingException, UserErrorException {

        this(new File(fileName));
    }

    /**
     * @throws FileNotFoundException if the file is not found
     * @throws IOException
     * @throws UserErrorException
     */
    public ThreadDumpFile(File file) throws IOException, ParsingException, UserErrorException {

        this.file = file;

        if (!file.isFile()) {

            throw new UserErrorException("no such file: " + file);
        }

        threadDumps = new ArrayList<>();

        parse(file);
    }

    // Public ----------------------------------------------------------------------------------------------------------

    public Iterator<ThreadDump> iterator()
    {
        return threadDumps.iterator();
    }

    public File getFile()
    {
        return file;
    }

    public int getCount()
    {
        return threadDumps.size();
    }

    public ThreadDump get(int index)
    {
        return threadDumps.get(index);
    }

    // Package protected -----------------------------------------------------------------------------------------------

    // Protected -------------------------------------------------------------------------------------------------------

    // Private ---------------------------------------------------------------------------------------------------------

    private void parse(File f) throws IOException, ParsingException, UserErrorException {

//        if (!f.isFile()) {
//
//            throw new UserErrorException("no such file: " + f);
//        }
//
//        // look for "Full thread dump" and grab the time stamp from the previous line, if no
//        // "Full thread dump" is found, handle the entire file as a single thread dump
//
//        long lineNumber = 1;
//        BufferedReader br = null;
//
//        int expected = -1;
//
//        try {
//
//            br = new BufferedReader(new FileReader(f));
//
//
//            // we start with a default thread dump to be able to process fragments that do
//            // not contain a THREAD_DUMP_HEADER
//            ThreadDump current = new ThreadDump(file, null, null, -1L);;
//
//            boolean expectEmptyLine = false;
//            String previous = null;
//            String line;
//
//            while((line = br.readLine()) != null) {
//
//                if (expectEmptyLine) {
//
//                    // sanity check, this is one way to insure that the thread dump is reasonably
//                    // syntactically correct
//                    if (line.trim().length() != 0) {
//
//                        throw new ParsingException( "\"" + previous + "\" not followed by an empty line", lineNumber);
//
//
//                    }
//
//                    expectEmptyLine = false;
//                }
//                else if (line.startsWith(THREAD_DUMP_HEADER)) {
//
//                    // extract the timestamp, if possible and install the new "current" thread dump
//
//                    Date timestamp = null;
//
//                    if (previous != null) {
//
//                        //
//                        // attempt to convert
//                        //
//
//                        try {
//
//                            timestamp = (Date)TIMESTAMP_FORMAT.parseObject(previous);
//                        }
//                        catch(Exception e) {
//
//                            //
//                            // that is fine, we will just ignore it
//                            //
//                        }
//                    }
//
//                    // only add the previous one if it contains at least one valid stack trace; we
//                    // need to do this because we start with a default thread dump that collects
//                    // everything from the beginning of the file - we need this to be able to
//                    // process fragments
//                    current.close();
//
//                    if (current.getThreadCount() > 0) {
//
//                        threadDumps.add(current);
//                    }
//
//                    // a new thread dumps starts ...
//                    current = new ThreadDump(file, timestamp, line, lineNumber);
//                    expectEmptyLine = true;
//                }
//                else {
//
//                    current.append(line, lineNumber);
//                }
//
//                previous = line;
//                lineNumber ++;
//            }
//
//            current.close();
//
//            // first check for error conditions
//            if (current.getThreadCount() == 0 && current.getHeader() != null) {
//
//                throw new ParsingException("Empty thread dump", current.getHeaderLineNumber());
//            }
//
//            if (current.getThreadCount() > 0) {
//
//                threadDumps.add(current);
//            }
//        }
//        finally {
//
//            if (br != null) {
//
//                br.close();
//            }
//        }

    }

    // Inner classes ---------------------------------------------------------------------------------------------------
}
