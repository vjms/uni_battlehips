package fi.utu.tech.sinktheship.utility.events;

public class ObjectEvent<Type> extends Event {
	private Type value;

	public ObjectEvent(Type value) {
		this.value = value;
	}

	public Type getValue() {
		return value;
	}

}
