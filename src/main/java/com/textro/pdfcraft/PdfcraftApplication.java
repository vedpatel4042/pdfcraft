//package com.textro.pdfcraft;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class PdfcraftApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(PdfcraftApplication.class, args);
//	}
//
//}

package com.textro.pdfcraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class PdfcraftApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfcraftApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openBrowserAfterStartup() {
		try {
			String url = "http://localhost:8080";

			// Try the Desktop API first
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(url));
			} else {
				// Fallback for different OSes
				Runtime runtime = Runtime.getRuntime();
				String os = System.getProperty("os.name").toLowerCase();

				if (os.contains("win")) {
					runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
				} else if (os.contains("mac")) {
					runtime.exec("open " + url);
				} else if (os.contains("nix") || os.contains("nux")) {
					runtime.exec("xdg-open " + url);
				}
			}
		} catch (Exception e) {
			System.err.println("Could not open browser automatically: " + e.getMessage());
			System.out.println("Please open your browser and navigate to: http://localhost:8080");
		}
	}
}
