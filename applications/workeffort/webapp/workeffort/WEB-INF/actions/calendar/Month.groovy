/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.sql.*;
import java.text.*;
import java.util.*;
import org.ofbiz.security.*;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;
import org.ofbiz.webapp.pseudotag.*;
import org.ofbiz.workeffort.workeffort.*;

startParam = parameters.start;

facilityId = parameters.facilityId;
fixedAssetId = parameters.fixedAssetId;
partyId = parameters.partyId;
workEffortTypeId = parameters.workEffortTypeId;
entityExprList = context.entityExprList;

start = null;
if (UtilValidate.isNotEmpty(startParam)) {
    start = new Timestamp(Long.parseLong(startParam));
}
if (start == null) {
    start = UtilDateTime.getMonthStart(nowTimestamp, timeZone, locale);
} else {
    start = UtilDateTime.getMonthStart(start, timeZone, locale);
}

tempCal = UtilDateTime.toCalendar(start, timeZone, locale);
numDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

prev = UtilDateTime.getMonthStart(start, -1, timeZone, locale);
context.prevMillis = new Long(prev.getTime()).toString();
next = UtilDateTime.getDayStart(start, numDays+1, timeZone, locale);
context.nextMillis = new Long(next.getTime()).toString();
end = UtilDateTime.getDayStart(start, numDays, timeZone, locale);

//Find out what date to get from
getFrom = null;
prevMonthDays =  tempCal.get(Calendar.DAY_OF_WEEK) - tempCal.getFirstDayOfWeek();
if (prevMonthDays < 0) prevMonthDays = 7 + prevMonthDays;
tempCal.add(Calendar.DATE,-(prevMonthDays));
numDays += prevMonthDays;
getFrom = new Timestamp(tempCal.getTimeInMillis());
firstWeekNum = tempCal.get(Calendar.WEEK_OF_YEAR);
context.put("firstWeekNum", new Integer(firstWeekNum));

// also get days until the end of the week at the end of the month
lastWeekCal = UtilDateTime.toCalendar(UtilDateTime.addDaysToTimestamp(start, numDays - prevMonthDays), timeZone, locale);
followingMonthDays = 7 + lastWeekCal.getFirstDayOfWeek() - lastWeekCal.get(Calendar.DAY_OF_WEEK);
if (followingMonthDays < 7) numDays += followingMonthDays; 

serviceCtx = UtilMisc.toMap("userLogin", userLogin, "start", getFrom,"numPeriods", new Integer(numDays), "periodType", new Integer(Calendar.DATE));
serviceCtx.putAll(UtilMisc.toMap("partyId", partyId, "facilityId", facilityId, "fixedAssetId", fixedAssetId, "workEffortTypeId", workEffortTypeId, "locale", locale, "timeZone", timeZone));
if (entityExprList) {
	serviceCtx.putAll(["entityExprList" : entityExprList]);
}

result = dispatcher.runSync("getWorkEffortEventsByPeriod", serviceCtx);

context.put("periods",result.get("periods"));
context.put("maxConcurrentEntries", result.get("maxConcurrentEntries"));
context.put("start", start);
context.put("end", end);
context.put("prev", prev);
context.put("next", next);
