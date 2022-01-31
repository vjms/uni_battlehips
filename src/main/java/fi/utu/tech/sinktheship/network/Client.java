package fi.utu.tech.sinktheship.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import fi.utu.tech.sinktheship.network.packet.Packet;
import fi.utu.tech.sinktheship.utility.events.EventDispatcher;
import fi.utu.tech.sinktheship.utility.events.NullEvent;
import fi.utu.tech.sinktheship.utility.events.ObjectEvent;

public abstract class Client {
	public final EventDispatcher<ObjectEvent<Packet>> packetReceived = new EventDispatcher<>();
	public final EventDispatcher<NullEvent> clientClosed = new EventDispatcher<>();
	private int id = -1;

	private ConcurrentLinkedQueue<Packet> outboundPackets = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<Packet> incomingPackets = new ConcurrentLinkedQueue<>();
	private AtomicBoolean continueRunning = new AtomicBoolean(false);

	private Thread outputThread;
	private Thread inputThread;
	private Thread packetDispatchThread;
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;

	public int getId() {
		return id;
	}

	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Checks whether this client is the authorative client on the server. Server
	 * returns id#0 for the authorative client on join. Server also checks on
	 * authorative requests whether they came from this client.
	 * 
	 * @return true if has authority over the server.
	 */
	public abstract boolean hasAuthority();

	/**
	 * 
	 */
	public void disconnect() {
		continueRunning.set(false);
		synchronized (incomingPackets) {
			incomingPackets.notifyAll();
		}
		synchronized (outboundPackets) {
			outboundPackets.notifyAll();
		}

	}

	public void join() throws InterruptedException {
		outputThread.join();
		inputThread.join();
		packetDispatchThread.join();
	}

	public ObjectOutputStream getOutput() {
		return output;
	}

	public ObjectInputStream getInput() {
		return input;
	}

	protected void start() {
		System.out.println("Creating a " + getClass().getSimpleName());
		outputThread = new Thread(() -> outputHandler());
		inputThread = new Thread(() -> inputHandler());
		packetDispatchThread = new Thread(() -> packetDispatching());
		continueRunning.set(true);
		outputThread.start();
		packetDispatchThread.start();
		inputThread.start();
	}

	private void packetDispatching() {
		while (continueRunning.get()) {
			try {
				synchronized (incomingPackets) {
					incomingPackets.wait();
				}
				Packet packet;
				while ((packet = incomingPackets.poll()) != null) {
					packetReceived.dispatch(new ObjectEvent<Packet>(packet));
				}
			} catch (InterruptedException ex) {
			}
		}
		System.out.println(getClass().getSimpleName() + " packet thread closed");
	}

	private void outputHandler() {
		while (continueRunning.get()) {
			try {
				Packet packet;
				while ((packet = outboundPackets.poll()) != null) {
					output.writeObject(packet);
				}
				synchronized (outboundPackets) {
					outboundPackets.wait();
				}
			} catch (InvalidClassException | InterruptedException | NotSerializableException ex) {
			} catch (EOFException ex) {
				continueRunning.set(false);
			} catch (SocketException ex) {
				var msg = ex.getMessage().toLowerCase();
				if (msg.contains("connection reset") || msg.contains("socket closed") || msg.contains("aborted")) {
					continueRunning.set(false);
				} else {
					ex.printStackTrace();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.println(getClass().getSimpleName() + " output thread closed");
		closeSocket();
		clientClosed.dispatch(new NullEvent());
	}

	private void inputHandler() {
		while (continueRunning.get()) {
			try {
				Packet packet = (Packet) input.readObject();
				incomingPackets.add(packet);
				synchronized (incomingPackets) {
					incomingPackets.notifyAll();
				}
			} catch (InvalidClassException | ClassNotFoundException | ClassCastException | NotSerializableException
					| SocketTimeoutException ex) {
			} catch (EOFException ex) {
				continueRunning.set(false);
			} catch (SocketException ex) {
				var msg = ex.getMessage().toLowerCase();
				if (msg.contains("connection reset") || msg.contains("socket closed") || msg.contains("aborted")) {
					continueRunning.set(false);
				} else {
					ex.printStackTrace();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.println(getClass().getSimpleName() + " input thread closed");
		closeSocket();
		synchronized (incomingPackets) {
			incomingPackets.notifyAll();
		}
		synchronized (outboundPackets) {
			outboundPackets.notifyAll();
		}
	}

	public void transmit(Packet packet) {
		outboundPackets.add(packet);
		synchronized (outboundPackets) {
			outboundPackets.notifyAll();
		}
	}

	protected void setSocket(Socket socket) throws IOException {
		this.socket = socket;
	}

	protected void setStreams(ObjectOutputStream output, ObjectInputStream input) {
		this.output = output;
		this.input = input;

	}

	protected void createStreams() throws IOException {
		setStreams(new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
	}

	protected Socket getSocket() {
		return socket;
	}

	/**
	 * utility to close socket without caring about exceptions
	 * 
	 * @param socket socket to close
	 */
	protected void closeSocket() {
		try {
			socket.close();
		} catch (IOException ex) {
		} catch (NullPointerException ex) {
		}
	}
}
