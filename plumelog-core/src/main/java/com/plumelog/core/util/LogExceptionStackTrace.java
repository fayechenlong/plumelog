package com.plumelog.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogExceptionStackTrace {

    public static Object erroStackTrace(Object obj) {
        if (obj instanceof Exception) {
            Exception eObj = (Exception) obj;
            StringWriter sw = null;
            PrintWriter pw = null;
            try {
                sw = new StringWriter();
                pw = new PrintWriter(sw);
                String exceptionStack = "\r\n";
                eObj.printStackTrace(pw);
                exceptionStack = sw.toString();
                return exceptionStack;
            } catch (Exception e) {
                e.printStackTrace();
                return obj;
            } finally {
                try {
                    pw.close();
                    sw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return obj;
        }
    }
}
