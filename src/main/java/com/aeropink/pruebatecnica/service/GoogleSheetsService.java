package com.aeropink.pruebatecnica.service;

import com.aeropink.pruebatecnica.entity.Client;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleSheetsService {

    private final Sheets sheetService;

    @Autowired
    public GoogleSheetsService(Sheets sheetService) {
        this.sheetService = sheetService;
    }

    public List<Client> readData(String spreadsheetId, String range) throws IOException {
        ValueRange response = sheetService.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();

        List<Client> clientsData = new ArrayList<>();

        if (values != null && !values.isEmpty()) {
            for (List<Object> row : values) {
                if (row.size() == 5) { // Assuming each row has 5 values
                    String name = ((String) row.get(0)).trim();
                    String last_name = ((String) row.get(1)).trim();
                    String second_last_name = ((String) row.get(2)).trim();
                    String curp = ((String) row.get(3)).trim();
                    String delay = ((String) row.get(4)).trim();

                    Client client = new Client();
                    client.setName(name);
                    client.setLast_name(last_name);
                    client.setSecond_last_name(second_last_name);
                    client.setCurp(curp);
                    client.setDelay(delay);

                    clientsData.add(client);
                } else {
                    System.out.println("Invalid data: " + row);
                }
            }
        }
        return clientsData;
    }



}