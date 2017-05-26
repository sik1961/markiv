package com.sik.markiv.api;
/**
 * @author sik
 *
 */
import org.joda.time.LocalDateTime;

public class UpdateRecord {
    private CalendarEvent event;
    private LocalDateTime lastUpdated;
    public UpdateRecord() {
        super();
        this.lastUpdated = new LocalDateTime(0);
    }
    public UpdateRecord(final CalendarEvent lastUpdatedBy, final LocalDateTime lastUpdated) {
        super();
        this.event = lastUpdatedBy;
        this.lastUpdated = lastUpdated;
    }
    public CalendarEvent getEvent() {
        return this.event;
    }
    public void setEvent(final CalendarEvent event) {
        this.event = event;
    }
    public LocalDateTime getLastUpdated() {
        return this.lastUpdated;
    }
    public void setLastUpdated(final LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
