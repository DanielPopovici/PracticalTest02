package ro.pub.cs.systems.pdsd.practicaltest02;

public class Alarm {

	private int hour;
	private int minute;
	
	enum AlarmStatus {Inactive, Active};
	
	private AlarmStatus status = AlarmStatus.Inactive;

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public AlarmStatus getStatus() {
		return status;
	}

	public void setStatus(AlarmStatus status) {
		this.status = status;
	}

	public Alarm(int hour, int minute) {
		super();
		this.hour = hour;
		this.minute = minute;
		status = AlarmStatus.Inactive;
	}
	
	
}
