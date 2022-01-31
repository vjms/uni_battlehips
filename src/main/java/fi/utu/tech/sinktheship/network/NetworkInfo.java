package fi.utu.tech.sinktheship.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fi.utu.tech.sinktheship.utility.OSUtils;

public class NetworkInfo {

	public String gateway = null;
	public String netmask = null;

	private NetworkInfo() {
	}

	public int maskedAsNum() {
		return ipv4ToNum(gateway) & ipv4ToNum(netmask);
	}

	public String masked() {
		return ipv4NumToString(maskedAsNum());
	}

	public int addressCount() {
		return 0xFFFFFFFF - ipv4ToNum(netmask) + 1;

	}

	static public int ipv4ToNum(String ip) {
		var split = ip.split("\\.");
		return Integer.parseInt(split[0]) << 24 | Integer.parseInt(split[1]) << 16 | Integer.parseInt(split[2]) << 8
				| Integer.parseInt(split[3]);
	}

	static public String ipv4NumToString(int ip) {
		return Integer.toString((ip >> 24) & 0xff) + "." + Integer.toString((ip >> 16) & 0xff) + "."
				+ Integer.toString((ip >> 8) & 0xff) + "." + Integer.toString((ip) & 0xff);

	}

	static private String zeroDest(String ip) {
		String[] split = ip.split("\\.");
		return split[0] + "." + split[1] + "." + split[2] + ".0";
	}

	/**
	 * 
	 * @return
	 */
	static public NetworkInfo getNetworkInfo() {
		switch (OSUtils.getOs()) {
			case WINDOWS:
				return getGatewayWindows();
			case LINUX:
				return getGatewayLinux();
			default:
		}
		return null;
	}

	/**
	 * https://www.ireasoning.com/articles/find_local_ip_address.htm
	 * 
	 * @return
	 */
	static private NetworkInfo getGatewayWindows() {
		var ret = new NetworkInfo();
		try {
			Process pro = Runtime.getRuntime().exec("cmd.exe /c route print");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll("( )+", " ");
				String[] tokens = line.split(" ");
				if (tokens.length == 5) {
					if (tokens[0].equals("0.0.0.0")) {
						ret.gateway = tokens[2];
					} else if (ret.gateway != null && tokens[0].equals(zeroDest(ret.gateway))) {
						ret.netmask = tokens[1];
						break;
					}

				}
			}
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * https://www.ireasoning.com/articles/find_local_ip_address.htm
	 * 
	 * @return
	 */
	static private NetworkInfo getGatewayLinux() {
		var ret = new NetworkInfo();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/proc/net/route"));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				line = line.replaceAll("( )+", " ");
				String[] tokens = line.split(" ");
				if (tokens.length > 1) {
					if (tokens[1].equals("00000000")) {
						ret.gateway = ipv4NumToString(Integer.parseInt(tokens[2]));
					} else if (ret.gateway != null && tokens[1].equals(zeroDest(ret.gateway))) {
						ret.netmask = tokens[7];
						break;
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();

		}
		return ret;
	}

}
