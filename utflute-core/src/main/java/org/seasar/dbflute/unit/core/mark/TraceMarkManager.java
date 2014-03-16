package org.seasar.dbflute.unit.core.mark;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.AssertionFailedError;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;

/**
 * @author jflute
 */
public class TraceMarkManager {

    public void assertMarked(Map<String, TraceMarkInfo> markMap, String mark) {
        boolean existsMark = false;
        if (markMap != null) {
            final TraceMarkInfo info = markMap.get(mark);
            if (info != null) {
                existsMark = true;
                info.finishAssertion();
            }
        }
        if (!existsMark) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The mark was NOT marked. (not found)");
            br.addItem("NotFound Mark");
            br.addElement(mark);
            br.addItem("Mark Map");
            if (markMap != null && !markMap.isEmpty()) {
                for (Entry<String, TraceMarkInfo> entry : markMap.entrySet()) {
                    br.addElement(entry.getValue());
                }
            } else {
                br.addItem("*no mark");
            }
            final String msg = br.buildExceptionMessage();
            throw new AssertionFailedError(msg);
        }
    }

    public void checkNonAssertedMark(Map<String, TraceMarkInfo> markMap) {
        if (markMap == null) {
            return;
        }
        TraceMarkInfo nonAssertedInfo = null;
        for (Entry<String, TraceMarkInfo> entry : markMap.entrySet()) {
            final TraceMarkInfo info = entry.getValue();
            if (!info.isAsserted()) {
                nonAssertedInfo = info;
                break;
            }
        }
        if (nonAssertedInfo != null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Found the non-asserted mark.");
            br.addItem("Advice");
            br.addElement("The mark should be asserted like this:");
            br.addElement("  (x):");
            br.addElement("    markHere(\"foo\");");
            br.addElement("    markHere(\"bar\");");
            br.addElement("    ...");
            br.addElement("    assertMarked(\"foo\");");
            br.addElement("  (o):");
            br.addElement("    markHere(\"foo\");");
            br.addElement("    markHere(\"bar\");");
            br.addElement("    ...");
            br.addElement("    assertMarked(\"foo\");");
            br.addElement("    assertMarked(\"bar\");");
            br.addItem("Non-Asserted Mark");
            br.addElement(nonAssertedInfo);
            br.addItem("Mark Map");
            for (Entry<String, TraceMarkInfo> entry : markMap.entrySet()) {
                br.addElement(entry.getValue());
            }
            final String msg = br.buildExceptionMessage();
            throw new AssertionFailedError(msg);
        }
    }
}
