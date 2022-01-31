package fi.utu.tech.sinktheship.utility;

import java.util.Locale;

public class OSUtils {
	public enum OSType {
		WINDOWS, LINUX, MAC, OTHER
	}

	private static OSType osType = null;

	/**
	 * https://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
	 * 
	 * @return detected os type
	 */
	public static OSType getOs() {
		if (osType == null) {
			String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
				osType = OSType.MAC;
			} else if (OS.indexOf("win") >= 0) {
				osType = OSType.WINDOWS;
			} else if (OS.indexOf("nux") >= 0) {
				osType = OSType.LINUX;
			} else {
				osType = OSType.OTHER;
			}
		}
		return osType;
	}

}
