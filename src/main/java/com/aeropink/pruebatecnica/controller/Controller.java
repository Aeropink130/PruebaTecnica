package com.aeropink.pruebatecnica.controller;

import com.aeropink.pruebatecnica.entity.Client;
import com.aeropink.pruebatecnica.repository.ClientRepository;
import com.aeropink.pruebatecnica.service.GoogleSheetsService;
import com.aeropink.pruebatecnica.service.StoreDataService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class Controller {
    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    private final GoogleSheetsService googleSheetsService;
    private final StoreDataService storeDataService;
    private final ClientRepository clientRepository;

    @Autowired
    public Controller(GoogleSheetsService googleSheetsService, ClientRepository clientRepository, StoreDataService storeDataService) {
        this.googleSheetsService = googleSheetsService;
        this.clientRepository = clientRepository;
        this.storeDataService = storeDataService;
    }

    @GetMapping("/sync")
    public ResponseEntity<String> syncGoogleToPostgres() {
        try {
            String spreadsheetId = "1Agm1UCzeNlEN0hB0l2XznvngYVCWS1uxLT56MY33Wo4";
            String range = "clients!A2:E";
            List<List<Client>> googleSheetsData = Collections.singletonList(googleSheetsService.readData(spreadsheetId, range));

            // Save the data to the database
            storeDataService.processAndSaveInDB(spreadsheetId, range);

            // Send notifications to clients with delay
            sendMessagesToClientsWithDelay(googleSheetsData);

            return new ResponseEntity<>("Data synced, saved to database, and notifications sent successfully", HttpStatus.OK);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to sync data, save to database, or send notifications", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAll")
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @GetMapping("/TwilioNotification")
    public ResponseEntity<String> sendTwilioNotification() {
        try {
            // Get clients with delay from the database
            List<Client> clientsWithDelay = clientRepository.findByDelayGreaterThanEqual("5");

            // Send notifications to clients with delay
            for (Client client : clientsWithDelay) {
                sendMessageToClient(client);
            }

            return new ResponseEntity<>("Twilio notifications sent successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to send Twilio notifications", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ... (rest of the controller code)

    private void sendMessagesToClientsWithDelay(List<List<Client>> googleSheetsData) {
        for (List<Client> clientsRow : googleSheetsData) {
            for (Client client : clientsRow) {
                // Check if the delay is equal to or greater than 5
                if (Integer.parseInt(client.getDelay()) >= 5) {
                    // Send a message using Twilio
                    sendMessageToClient(client);
                }
            }
        }
    }

    private void sendMessageToClient(Client client) {
        // Use injected Twilio properties
        Twilio.init(twilioAccountSid, twilioAuthToken);

        String toPhoneNumber = "+528713784495";
        String fromPhoneNumber = "+0987654321";  // Replace with your Twilio phone number

        String message = "Tu cuenta presenta un atraso de " + client.getDelay() + " días. Por favor, regulariza tu situación.";

        Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                message).create();
    }

}
