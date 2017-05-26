package com.sik.markiv.api;
/**
 * @author sik
 */
import org.joda.time.LocalDateTime;

public class CalendarEvent {
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String summary;
	private String location;
	private String notes;
	private LocalDateTime lastUpdated;
	private String lastUpdatedBy;
	private EventType eventType;
	private Boolean eventPrivate;
	    
    public CalendarEvent(final LocalDateTime startDate,
            final LocalDateTime endDate,
            final String summary,
            final String location,
            final String notes,
            final LocalDateTime lastUpdated,
            final String lastUpdatedBy,
            final EventType eventType,
            final Boolean eventPrivate) {
            super();
            this.startDate = startDate;
            this.endDate = endDate;
            this.summary = summary;
            this.notes = notes;
            this.location = location;
            this.lastUpdated = lastUpdated;
            this.lastUpdatedBy = lastUpdatedBy;
            this.eventType = eventType;
            this.eventPrivate = eventPrivate;
        }

    public LocalDateTime getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public String getSummary() {
		return this.summary;
	}

	public void setSummary(final String remarks) {
		this.summary = remarks;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public String getNotes() {
        return this.notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

	public LocalDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(final LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastUpdatedBy() {
        return this.lastUpdatedBy;
    }

    public void setLastUpdatedBy(final String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }

    public Boolean isEventPrivate() {
        return this.eventPrivate;
    }

    public void setEventPrivate(final Boolean eventPrivate) {
        this.eventPrivate = eventPrivate;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.endDate == null) ? 0 : this.endDate.hashCode());
		result = prime * result
				+ ((this.location == null) ? 0 : this.location.hashCode());
		result = prime * result + ((this.summary == null) ? 0 : this.summary.hashCode());
		result = prime * result
				+ ((this.startDate == null) ? 0 : this.startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final CalendarEvent other = (CalendarEvent) obj;
		if (this.endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!this.endDate.equals(other.endDate))
			return false;
		if (this.location == null) {
			if (other.location != null)
				return false;
		} else if (!this.location.equals(other.location))
			return false;
		if (this.summary == null) {
			if (other.summary != null)
				return false;
		} else if (!this.summary.equals(other.summary))
			return false;
		if (this.startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!this.startDate.equals(other.startDate))
			return false;
		return true;
	}

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CalendarEvent [startDate=");
        builder.append(this.startDate);
        builder.append(", endDate=");
        builder.append(this.endDate);
        builder.append(", remarks=");
        builder.append(this.summary);
        builder.append(", notes=");
        builder.append(this.notes);
        builder.append(", location=");
        builder.append(this.location);
        builder.append(", lastUpdated=");
        builder.append(this.lastUpdated);
        builder.append(", eventType=");
        builder.append(this.eventType);
        builder.append(", eventPrivate=");
        builder.append(this.eventPrivate);
        builder.append("]");
        return builder.toString();
    }

}
