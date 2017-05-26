package com.sik.markiv.api;

import java.util.Date;

import org.joda.time.LocalDateTime;

/**
 * @author sik
 *
 */
public class M4Date {
	
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	
	public M4Date() {
		LocalDateTime now = new LocalDateTime(new Date());
		this.startTime = now.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		this.endTime = now.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
	}
	
	public M4Date(LocalDateTime dateTime) {
		this.startTime = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		this.endTime = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
	}
	
	/**
	 * @return the startTime
	 */
	public LocalDateTime getStartTime() {
		return this.startTime;
	}
	/**
	 * @return the endTime
	 */
	public LocalDateTime getEndTime() {
		return this.endTime;
	}
	
	public void rollDate(int i) {
		this.startTime = this.startTime.plusDays(i);
		this.endTime = this.endTime.plusDays(i);
	}
	
	/**
	 * @return
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("M4Date [startTime=");
		builder.append(this.startTime);
		builder.append(", endTime=");
		builder.append(this.endTime);
		builder.append("]");
		return builder.toString();
	}
}
