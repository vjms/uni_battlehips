package fi.utu.tech.sinktheship.network.packet;

public class JoinRequestResponse extends Packet {
	private static final long serialVersionUID = -3329027820218891534L;

	/**
	 * negative if join refused
	 */
	public int clientId;

	public JoinRequestResponse(int clientId) {
		this.clientId = clientId;
	}
}
