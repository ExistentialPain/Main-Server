package com.SCI.db;

import java.util.Arrays;
import java.util.LinkedList;

public class Query {
    public Query(String format, Object... args) {
        LinkedList<Object> largs = new LinkedList<>(Arrays.asList(args));
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < format.length(); i++) {
             if (format.charAt(i) == '\\') {
                 if (i != format.length() - 1 && format.charAt(i + 1) == '?') {
                     ++i;
                     buf.append('?');
                 } else if (i != format.length() - 1 && format.charAt(i + 1) == '\\') {
                     ++i;
                     buf.append('\\');
                 } else {
                     buf.append('\\');
                 }
             } else if (format.charAt(i) == '?') {
                 if (i != format.length() - 1 && format.charAt(i + 1) == '?') {
                     ++i;
                     buf.append("`").append(largs.removeFirst()).append('`');
                 } else {
                     Object arg = largs.removeFirst();
                     if (arg instanceof String) {
                         buf.append("'").append(arg).append("'");
                     } else {
                         buf.append(arg);
                     }
                 }
             } else {
                 buf.append(format.charAt(i));
             }
        }
        query = buf.toString();
    }

    @Override
    public String toString() {
        return query;
    }

    private String query;
}
