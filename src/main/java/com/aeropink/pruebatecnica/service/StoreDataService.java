package com.aeropink.pruebatecnica.service;

import com.aeropink.pruebatecnica.entity.Client;
import com.aeropink.pruebatecnica.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StoreDataService {

    private final GoogleSheetsService googleSheetsService;
    private final ClientRepository clientRepository;

    @Autowired
    public StoreDataService(GoogleSheetsService googleSheetsService, ClientRepository clientRepository) {
        this.googleSheetsService = googleSheetsService;
        this.clientRepository = clientRepository;
    }

    public void processAndSaveInDB(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        List<List<Client>> googlesheetsData = Collections.singletonList(googleSheetsService.readData(spreadsheetId, range));

        List<Client> clientsToSave = new ArrayList<>();
        for (List<Client> clientsRow : googlesheetsData) {
            clientsToSave.addAll(clientsRow);
        }

        clientRepository.saveAll(clientsToSave);
    }
}
