package us.dot.faa.swim.fns.notamdb;

import us.dot.faa.swim.fns.FnsMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class NotamUtils {

	private static final String ICAO_TAG = "<ns9:locationIndicatorICAO>";
	private static final String ICAO_TAG_ALT = "<ns6:icaoLocation>";

	/**
	 * Extracts the ICAO location from the AIXM NOTAM message.
	 * Checks both ns9:locationIndicatorICAO and ns6:icaoLocation tags.
	 * 
	 * @param fnsMessage The FNS message containing the AIXM NOTAM message.
	 * @return The ICAO location as a string.
	 * @throws IllegalArgumentException if the message is null or doesn't contain a
	 *                                  valid ICAO location
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

	public static String extractIcaoMessage(String aixmNotam) {
		String startTag = "<ns11:NOTAMTranslation";
		String endTag = "</ns11:NOTAMTranslation>";
		String typeTag = "<ns11:type>OTHER:ICAO</ns11:type>";

		int currentIndex = 0;
		while (true) {
			int startIndex = aixmNotam.indexOf(startTag, currentIndex);
			if (startIndex == -1) {
				return null;
			}

			int endIndex = aixmNotam.indexOf(endTag, startIndex);
			if (endIndex == -1) {
				return null;
			}

			String notamTranslation = aixmNotam.substring(startIndex, endIndex + endTag.length());

			// Check if this translation has type "OTHER:ICAO"
			if (notamTranslation.contains(typeTag)) {
				// Extract the formatted text content
				int formattedTextStart = notamTranslation.indexOf("<html:div");
				if (formattedTextStart != -1) {
					int formattedTextEnd = notamTranslation.indexOf("</html:div>", formattedTextStart);
					if (formattedTextEnd != -1) {
						String formattedText = notamTranslation.substring(formattedTextStart, formattedTextEnd + 11);
						// Remove the HTML div tags and extract just the text content
						formattedText = formattedText.replaceAll("<[^>]*>", "").replaceAll("&lt;pre&gt;", "")
								.replaceAll("&lt;/pre&gt;", "");
						return formattedText.trim();
					}
				}
				return null;
			}

			currentIndex = endIndex + endTag.length();
		}
	}
}
