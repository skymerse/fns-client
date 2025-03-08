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
