package fi.utu.tech.sinktheship.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import fi.utu.tech.sinktheship.network.packet.*;
import fi.utu.tech.sinktheship.game.Game;
import fi.utu.tech.sinktheship.game.GameState;
import fi.utu.tech.sinktheship.game.Grid;
import fi.utu.tech.sinktheship.game.MoveStatus;

/**
 * 
 */
public class Server extends Thread {
	private String serverName;
	public static final int defaultPort = 32666;
	private int runningClientId = 0;
	private LinkedList<ServerClient> clients = new LinkedList<>();

	private ServerClient owner;
	private ArrayList<ServerClient> players = new ArrayList<>();
	private Game game = new Game();
	private Thread listenConnectionsThread = new Thread(() -> listenConnections());
	private AtomicBoolean continueRunning = new AtomicBoolean(false);

	private int turnPlayerId = -1;

	/**
	 * 
	 */
	private class ServerClient extends Client {
		public String name = "";

		public ServerClient() {
			setId(runningClientId++);
			packetReceived.addListener(event -> handlePacket(event.getValue()));
			clientClosed.addListener(event -> broadcast(null, new ChatPacket(name + " left!")));
		}

		@Override
		public boolean hasAuthority() {
			return this == owner;
		}

		public boolean isPlayer() {
			synchronized (players) {
				return players.contains(this);
			}
		}

		public boolean isMyTurn() {
			return isPlayer() && turnPlayerId == getId();
		}

		public int playerIndex() {
			synchronized (players) {
				return players.indexOf(this);
			}
		}

		synchronized private Packet handlePacket(Packet packet) {
			if (packet instanceof GameStateRequestPacket) {
				return new GameStatePacket(game.getGameState());
			}

			// Ignore the packet if the client doesn't have authority.
			if (packet instanceof GameRulesPacket && hasAuthority()) {
				var rp = (GameRulesPacket) packet;
				game.setRules(rp.rules);
				broadcast(this, packet);
			}

			// start game packet is also a game rules packet. For not having to write
			// duplicate code, we can just omit else, since we still want to get the rules
			// from this packet.
			if (packet instanceof StartGamePacket && hasAuthority()) {
				if (!enoughPlayers()) {
					// TODO send something.
					return null;
				}
				if (game.getRules().rulesOk()) {
					broadcast(null, new StartGamePacket(game.getRules()));
					game.setGameState(GameState.SETUP);
					broadcast(null, new GameStatePacket(GameState.SETUP));
				}
			}
			// validates the move and sends a response + broadcast to all clients if a valid
			// move
			else if (packet instanceof MovePacket) {
				if (!isPlayer()) {
					return null;
				}
				var mp = (MovePacket) packet;
				if (!isMyTurn()) {
					return new MoveResponsePacket(getId(), mp.index, MoveStatus.NotMyTurn);
				}
				var player = game.getPlayers()[playerIndex()];
				var opponent = game.getPlayers()[getOpponentPlayerIndex(playerIndex())];
				var valid = player.addMove(mp.index);
				if (!valid) {
					return new MoveResponsePacket(getId(), mp.index, MoveStatus.InvalidIndex);
				}
				var ship = opponent.getGrid().shootAt(mp.index);
				if (ship == null) {
					broadcast(null, new MoveResponsePacket(getId(), mp.index, MoveStatus.HitEmpty));
				} else {
					broadcast(null, new MoveResponsePacket(getId(), mp.index, MoveStatus.HitShip));
					if (ship.isDestroyed()) {
						broadcast(null, new ShipDestroyedPacket(getId(), opponent.getGrid().getShipPlacement(ship)));
						if (opponent.getGrid().allShipsDestroyed()) {
							broadcast(null, new GameOverPacket(getId()));
							game.setGameState(GameState.END);
							broadcast(null, new GameStatePacket(GameState.END));
						}
					}
				}
				if (!game.getRules().fastMode) {
					switchTurns();
				}
			}
			//
			else if (packet instanceof NewGamePacket && hasAuthority()) {
				game.setGameState(GameState.NEW_GAME);
				broadcast(null, new GameStatePacket(GameState.NEW_GAME));
			}
			// when the clients press ready get the ships and create a grid for them.
			else if (packet instanceof SetupReadyPacket) {
				int playerIndex = playerIndex();
				// only the players can be ready.
				if (playerIndex < 0) {
					// TODO send something.
					return null;
				}
				if (game.getGameState() != GameState.SETUP) {
					return null;
				}
				var srp = (SetupReadyPacket) packet;
				var p = game.getPlayers()[playerIndex];
				p.setGrid(new Grid(game.getRules(), srp.shipPlacements));
				if (!p.getGrid().isOk()) {
					// TODO gridnot ok response ( cheating not allowed packet? )
				} else {
					p.setReady(srp.ready);
					if (game.playersReady()) {
						broadcast(null, new PlayersReadyPacket());
						game.setGameState(GameState.STARTING);
						broadcast(null, new GameStatePacket(GameState.STARTING));
						var timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								players.get(0).transmit(new SetupPacket(srp.shipPlacements));
								players.get(1).transmit(new SetupPacket(srp.shipPlacements));
								game.setGameState(GameState.PLAY);
								broadcast(null, new GameStatePacket(GameState.PLAY));
								setTurn(0);
								timer.cancel();
							}
						}, 3000);

					}
				}

			}
			//
			else if (packet instanceof ChatPacket) {
				broadcast(null, new ChatPacket("<" + name + "> " + ((ChatPacket) packet).text));
			}
			//
			else if (packet instanceof PlayerInfoPacket) {
				var pif = (PlayerInfoPacket) packet;
				name = pif.name;
				if (name.length() == 0) {
					name = "Anon#" + getId();
				}
				pif.clientId = getId();
				pif.isPlayer = isPlayer();
				broadcast(null, pif);
				if (pif.isPlayer && enoughPlayers()) {
					var opponent = players.get(getOpponentPlayerIndex(playerIndex()));
					var opponentInfo = new PlayerInfoPacket(opponent.name);
					opponentInfo.clientId = opponent.getId();
					opponentInfo.isPlayer = true;
					broadcast(null, opponentInfo);
					System.out.println(opponentInfo);
				}
			}
			//
			else if (packet instanceof ForfeitPacket) {
			}
			return null;
		}
	}

	public boolean enoughPlayers() {
		synchronized (players) {
			return players.size() == 2;
		}
	}

	private int getOpponentPlayerIndex(int index) {
		return index == 0 ? 1 : 0;
	}

	private void switchTurns() {
		if (players.get(0).isMyTurn()) {
			setTurn(1);
		} else {
			setTurn(0);
		}
	}

	private void setTurn(int index) {
		turnPlayerId = players.get(index).getId();
		broadcast(null, new TurnPacket(turnPlayerId));
	}

	/**
	 * 
	 * @param serverName
	 */
	public Server(String serverName) {
		this.serverName = serverName;

	}

	/**
	 * The meat of the game server.
	 * 
	 * Handles packets received from the clients.
	 * 
	 * @param client Client where the packet came from
	 * @param packet Packet that needs to be handled
	 * @return Response packet, or null if no response needed.
	 */

	/**
	 * Server main thread
	 */
	@Override
	public void run() {
		System.out.println("Booting up a server...");
		continueRunning.set(true);
		listenConnectionsThread.start();

		while (continueRunning.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				continueRunning.set(false);
			}
		}

		System.out.println("Shutting down the server...");
		try {
			listenConnectionsThread.join();
			for (var client : clients) {
				client.disconnect();
			}
		} catch (InterruptedException ex) {
		}
		System.out.println("Server shut down...");
	}

	/**
	 * 
	 * @param from   excludes this client from the broadcast
	 * @param packet packet to broadcast
	 */
	private void broadcast(ServerClient from, Packet packet) {
		for (var client : clients) {
			if (client != from) {
				client.transmit(packet);
			}
		}
	}

	/**
	 * Handles join game requests and handshakes.
	 * 
	 * @param socket newly bound socket from the serversocket
	 */
	private void validateClient(Socket socket) {
		try {
			socket.setSoTimeout(10000);
			var os = new ObjectOutputStream(socket.getOutputStream());
			var is = new ObjectInputStream(socket.getInputStream());
			var obj = is.readObject();
			if (obj instanceof HandShakePacket) {
				var handShake = (HandShakePacket) obj;
				handShake.serverName = serverName;
				synchronized (players) {
					handShake.playerCount = players.size();
				}
				os.writeObject(handShake);

			} else if (obj instanceof JoinRequestPacket) {
				var request = (JoinRequestPacket) obj;
				ServerClient client;

				// id < 0 means a new client is trying to connect
				if (request.id < 0) {
					client = new ServerClient();
					if (client.getId() == 0) {
						owner = client;
					}
					clients.add(client);
				} else {
					// find the client instance if it exists, when the request id >= 0
					client = clients.stream().filter(c -> c.getId() == request.id && c.getSocket().isClosed()).findAny()
							.orElse(null);
				}
				var response = new JoinRequestResponse(client != null ? client.getId() : -1);
				response.copyId(request);
				if (client != null) {
					if (!request.spectator && !client.isPlayer()) {
						synchronized (players) {
							if (players.size() >= 2 && players.contains(client)) {
								((JoinRequestResponse) response).clientId = -1;
							} else {
								players.add(client);
							}
						}
					}
					socket.setSoTimeout(0);

					client.name = request.name;
					client.setSocket(socket);
					client.setStreams(os, is);
					client.start();
					os.writeObject(response);
					if (client.getId() >= 0) {
						broadcast(null, new ChatPacket(client.name + " joined!"));
					}
				}
			}
		} catch (Exception ex) {
			closeSocket(socket);
		}
	}

	/**
	 * listen to new socket connections and try to validate them.
	 */
	private void listenConnections() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(defaultPort);
			serverSocket.setSoTimeout(100);
			while (continueRunning.get()) {
				try {
					var socket = serverSocket.accept();
					var thread = new Thread(() -> validateClient(socket));
					thread.setDaemon(true);
					thread.start();
				} catch (IOException ex) {

				}
			}
		} catch (Exception ex) {

		}
		closeSocket(serverSocket);
	}

	/**
	 * force socket to close without caring about exceptions
	 * 
	 * @param socket socket to close
	 */
	static private void closeSocket(Closeable socket) {
		try {
			socket.close();
		} catch (IOException ex) {
		} catch (NullPointerException ex) {
		}
	}

	/**
	 * Scans the ip for an active game.
	 * 
	 * @param ip      ip to scan
	 * @param port    port to scan
	 * @param timeout connection timeout
	 * @return null if not found
	 */
	static public ServerInfo scan(String ip, int port, int timeout) {
		var address = new InetSocketAddress(ip, port);
		var socket = new Socket();
		ServerInfo ret = null;
		try {
			socket.connect(address, timeout);
			socket.setSoTimeout(timeout);
			var os = new ObjectOutputStream(socket.getOutputStream());
			var is = new ObjectInputStream(socket.getInputStream());
			var handshake = new HandShakePacket();
			var ping = System.currentTimeMillis();
			os.writeObject(handshake);
			var resp = (HandShakePacket) is.readObject();
			ping = System.currentTimeMillis() - ping;
			if (resp.getId() == handshake.getId()) {
				ret = new ServerInfo(resp.serverName, resp.playerCount, resp.spectatorCount, (int) ping,
						address.getAddress(), port);
			}
		} catch (UnknownHostException ex) {
		} catch (SocketTimeoutException ex) {
		} catch (IllegalArgumentException ex) {
		} catch (IOException ex) {
		} catch (ClassNotFoundException ex) {
		} catch (ClassCastException ex) {
		}
		closeSocket(socket);
		return ret;
	}

}
