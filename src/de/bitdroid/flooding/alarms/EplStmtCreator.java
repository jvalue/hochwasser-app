package de.bitdroid.flooding.alarms;

import de.bitdroid.flooding.pegelonline.PegelOnlineSource;


final class EplStmtCreator implements AlarmVisitor<Void, String> {

	@Override
	public String visit(LevelAlarm alarm, Void param) {
		String filter = "(longname = '" + alarm.getStation() + "' "
			+ "and BodyOfWater.longname = '" + alarm.getRiver() + "' "
			+ "and timeseries.firstof(i => i.shortname = 'W') is not null)";

		String object1 = "measurement1";
		String object2 = "measurement2";

		String dataType = "`" + PegelOnlineSource.INSTANCE.getSourceId() + "`";
		boolean alarmWhenAbove = alarm.getAlarmWhenAbove();
		double level = alarm.getLevel();

		return "select " + object1 + ", " + object2 + " from pattern [every "
			+ object1 + "=" + dataType + filter + " -> " + object2 + "=" + dataType + filter + "]"
			+ " where " + getWhere(object1, !alarmWhenAbove, level)
			+ " and " + getWhere(object2, alarmWhenAbove, level);
	}


	private String getWhere(String objectName, boolean greater, double level) {
		String comparison = null; 
		if (greater) comparison = ">=";
		else comparison = "<";

		return objectName + ".timeseries.firstof(i => i.shortname = 'W' "
			+ "and i.currentMeasurement.value " + comparison + " " + level + ") is not null";
	}

}
