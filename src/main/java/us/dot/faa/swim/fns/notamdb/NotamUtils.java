package us.dot.faa.swim.fns.notamdb;

import us.dot.faa.swim.fns.FnsMessage;

public class NotamUtils {

	private static final String ICAO_TAG = "<ns9:locationIndicatorICAO>";
	private static final String ICAO_TAG_ALT = "<ns6:icaoLocation>";
	
	/**
	 * Extracts the ICAO location from the AIXM NOTAM message.
	 * Checks both ns9:locationIndicatorICAO and ns6:icaoLocation tags.
	 * 
	 * @param fnsMessage The FNS message containing the AIXM NOTAM message.
	 * @return The ICAO location as a string.
	 * @throws IllegalArgumentException if the message is null or doesn't contain a valid ICAO location
	 */
	public static String extractIcaoLocation(FnsMessage fnsMessage) {
		if (fnsMessage == null || fnsMessage.getAixmNotamMessage() == null) {
			throw new IllegalArgumentException("FNS message or AIXM NOTAM message is null");
		}
		
		String aixmNotam = fnsMessage.getAixmNotamMessage();
		int startIndex = aixmNotam.indexOf(ICAO_TAG);
		
		// If primary tag not found, try alternative tag
		if (startIndex == -1) {
			startIndex = aixmNotam.indexOf(ICAO_TAG_ALT);
			if (startIndex != -1) {
				startIndex += ICAO_TAG_ALT.length();
			}
		} else {
			startIndex += ICAO_TAG.length();
		}
		
		if (startIndex == -1) {
			return null;
		}
		
		if (startIndex + 4 > aixmNotam.length()) {
			throw new IllegalArgumentException("Invalid ICAO location format");
		}
		
		return aixmNotam.substring(startIndex, startIndex + 4);
	}
}
