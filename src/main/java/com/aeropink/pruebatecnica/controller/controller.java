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
public class controller {

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    private final GoogleSheetsService googleSheetsService;
    private final StoreDataService storeDataService;
    private final ClientRepository clientRepository;

    @Autowired
    public controller(GoogleSheetsService googleSheetsService, ClientRepository clientRepository, StoreDataService storeDataService) {
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

            return new ResponseEntity<>("Data synced and saved to database successfully", HttpStatus.OK);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to sync data and save to database", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAll")
    public List<Client> getAll(){
        return clientRepository.findAll();
    }

    @PostMapping("/sync")
    public ResponseEntity<String> saveToDatabase(@RequestBody List<Client> clients) {
        try {
            // Save the list of clients to the database
            clientRepository.saveAll(clients);
            return new ResponseEntity<>("Data saved to database successfully", HttpStatus.OK);
        } catch (Exception e) {
            // Handle any exceptions that occur during the save operation
            e.printStackTrace();
            return new ResponseEntity<>("Failed to save data to database", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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

        String toPhoneNumber = "+528713784495";  // Replace with the client's phone number
        String fromPhoneNumber = "+0987654321";  // Replace with your Twilio phone number

        String message = "Tu cuenta presenta un atraso de " + client.getDelay() + " días. Por favor, regulariza tu situación.";

        Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                message).create();
    }

}
