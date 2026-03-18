package com.gsc.gsc.constants;

public class NotificationTypes {

    private NotificationTypes() {}

    /** Notification sent manually by an admin to one or more users. */
    public static final String ADMIN    = "ADMIN";

    /** Notification triggered by a Job Card event (update, submit, etc.). */
    public static final String JOB_CARD = "JOB_CARD";

    /** Notification triggered by a Bill event. */
    public static final String BILL     = "BILL";
}
