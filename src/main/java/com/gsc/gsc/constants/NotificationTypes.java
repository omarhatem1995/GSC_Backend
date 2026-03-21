package com.gsc.gsc.constants;

import java.util.HashMap;
import java.util.Map;

public class NotificationTypes {

    private NotificationTypes() {}

    /** Notification sent manually by an admin to one or more users. */
    public static final String ADMIN    = "ADMIN";

    /** Notification triggered by a Job Card event (update, submit, etc.). */
    public static final String JOB_CARD = "JOB_CARD";

    /** Notification triggered by a Bill event. */
    public static final String BILL     = "BILL";

    /** Notification triggered when points are added or deducted. */
    public static final String POINTS   = "POINTS";

    /** Default type for notifications with no type set. */
    public static final String GLOBAL   = "GLOBAL";

    /**
     * Human-readable display names for each notification type.
     * Used when returning notifications to the client.
     */
    private static final Map<String, String> DISPLAY_NAMES = new HashMap<>();
    static {
        DISPLAY_NAMES.put(ADMIN,    "Admin");
        DISPLAY_NAMES.put(JOB_CARD, "Job Card");
        DISPLAY_NAMES.put(BILL,     "Bill");
        DISPLAY_NAMES.put(POINTS,   "GSC Points");
        DISPLAY_NAMES.put(GLOBAL,   "Global");
    }

    /**
     * Returns the display name for a given notification type.
     * Falls back to "Global" if the type is null, blank, or unrecognised.
     */
    public static String displayName(String type) {
        if (type == null || type.isBlank()) return DISPLAY_NAMES.get(GLOBAL);
        return DISPLAY_NAMES.getOrDefault(type, type);
    }
}
