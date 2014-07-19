package de.bitdroid.flooding.alarms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
abstract class Alarm {

	protected Alarm() { }

	public abstract <P,R> R accept(AlarmVisitor<P,R> visitor, P param);

}
