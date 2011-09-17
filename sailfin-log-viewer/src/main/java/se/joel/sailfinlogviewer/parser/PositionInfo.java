/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * Copyright (c) Ericsson AB, 2004-2008. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package se.joel.sailfinlogviewer.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class PositionInfo {
    private String rawData;
    private List<String> fields = new ArrayList<String>();
    private String threadId = "0";
    private String threadName;
    private String className;
    private String methodName;
    private String requestId;
    private String miscData;

    public PositionInfo(String rawData) {
        this.rawData = rawData;
        parse(this.rawData);
    }

    public PositionInfo() {
    }

    /**
     * @return the rawData
     */
    public String getRawData() {
        return rawData;
    }

    /**
     * @return the fields
     */
    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @return the threadId
     */
    public String getThreadId() {
        return threadId;
    }

    /**
     * @return the threadName
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @return the miscData
     */
    public String getMiscData() {
        return miscData;
    }

    private void parse(String rawData) {
        StringTokenizer st = new StringTokenizer(rawData, ";");

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            fields.add(token);

            if (token.startsWith("_ThreadID=")) {
                threadId = token.substring("_ThreadID=".length());
            } else if (token.startsWith("_ThreadName=")) {
                threadName = token.substring("_ThreadName=".length());
            } else if (token.startsWith("ClassName=")) {
                className = token.substring("ClassName=".length());
            } else if (token.startsWith("MethodName=")) {
                methodName = token.substring("MethodName=".length());
            } else if (token.startsWith("_RequestID=")) {
                requestId = token.substring("_RequestID=".length());
            } else {
                miscData = (miscData == null) ? token : (miscData + ";" + token);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PositionInfo{threadId=" + threadId + ",threadName=" + threadName + ",className=" + className + ",methodName=" + methodName + ",miscData=" + miscData + ",requestId=" + requestId;
    }
}
