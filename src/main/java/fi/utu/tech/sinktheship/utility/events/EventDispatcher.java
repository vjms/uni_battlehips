package fi.utu.tech.sinktheship.utility.events;

import java.util.LinkedList;
import java.util.List;

public class EventDispatcher<E extends Event> {
	private List<EventListener<E>> listeners = new LinkedList<>();

	public void dispatch(E event) {
		for (var l : listeners) {
			l.handle(event);
		}
	}

	public void addListener(EventListener<E> listener) {
		listeners.add(listener);
	}

	public void removeListener(EventListener<E> listener) {
		listeners.remove(listener);
	}

}
