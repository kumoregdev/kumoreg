package org.kumoricon.attendee;

import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.model.computer.ComputerService;
import org.kumoricon.util.BadgeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class BadgePrintService {
    private static final Logger log = LoggerFactory.getLogger(BadgePrintService.class);

    @Value("${kumoreg.printing.enablePrintingFromServer}")
    private Boolean enablePrintingFromServer;

    @Autowired
    private ComputerService computerService;

    /**
     * Prints badges for a given list of attendees to either the appropriate printer name (from
     * the computers table), or the default printer on the server.
     * @param attendees List of attendees
     * @param clientIPAddress Client computer's IP address
     * @return
     */
    public String printBadgesForAttendees(List<Attendee> attendees, String clientIPAddress) {
        if (enablePrintingFromServer != null && enablePrintingFromServer) {
            BadgeGenerator badgeGenerator = new BadgeGenerator(attendees);
            String printerName = computerService.findPrinterNameForComputer(clientIPAddress);
            try {
                printDocument(badgeGenerator.getStream(), printerName);
            } catch (PrintException e) {
                log.error(String.format("Error printing badge for %s: %s",
                        clientIPAddress, e.getMessage()), e);
                return("Error printing. No printers found? More information in server logs");
            }
        } else {
            return("Printing from server not enabled. Select \"Show Badges in Browser\".");
        }
        return "Printed";
    }


    /**
     * Prints the given inputStream to the printer with the given name, or the default printer
     * if a printer with that name isn't found.
     *
     * @param inputStream Data stream (Usually PDF formatted)
     * @param printerName Destination printer name (case insensitive)
     */
    public void printDocument(InputStream inputStream, String printerName) throws PrintException {
        PrintService printService = findPrinter(printerName);

        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        DocPrintJob job = printService.createPrintJob();
        PrintRequestAttributeSet printRequestSet = new HashPrintRequestAttributeSet();

        Doc doc = new SimpleDoc(inputStream, flavor, null);
        job.print(doc, printRequestSet);
    }

    /**
     * Returns a PrintService object for the printer with the given name, or the default printer if
     * no match is found.
     * @param name Printer name (case insensitive)
     * @return Print Service
     */
    public PrintService findPrinter(String name) {
        name = name.toLowerCase().trim();
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService printer : printServices) {
            String thisPrinterName = printer.getName().trim().toLowerCase();
            if (name.equals(thisPrinterName)) { return printer; }
        }

        PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
        if (printer != null) {
            log.warn("Printer \"{}\" not found, using default printer \"{}\"", name, printer.getName());
            return printer;
        }
        log.error("Printer \"%s\" not found, no default printer found. Set a printer as default.", name);
        return null;
    }

    /**
     * Gets list of installed printers from system
     * @return List of PrintService objects
     */
    public List<PrintService> getAvailablePrinters() {
        return Arrays.asList(PrintServiceLookup.lookupPrintServices(null, null));
    }

}
