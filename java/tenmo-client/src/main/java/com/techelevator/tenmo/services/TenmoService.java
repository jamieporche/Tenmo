package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

public class TenmoService {

    private final String API_BASE_URL = "http://localhost:8080/";
    private RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal getBalance() {
        return restTemplate.exchange(API_BASE_URL + "balance", HttpMethod.GET,
                makeAuthEntity(), Balance.class).getBody().getBalance();
    }

    public User[] getAllUsers() {
        return restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET,
                makeAuthEntity(), User[].class).getBody();
    }

    public User getUserByAccountId(int accountId) {
        return restTemplate.exchange(API_BASE_URL + "accounts/" + accountId + "/user", HttpMethod.GET, makeAuthEntity(), User.class).getBody();
    }

    public Transfer createTransfer(Transfer transfer){
        Transfer t = null;
        try{
            t = restTemplate.exchange(API_BASE_URL + "transfers", HttpMethod.POST, makeRequestBody(transfer), Transfer.class).getBody();
        }catch(RestClientResponseException | ResourceAccessException e){
            if (e.getMessage() != null && e.getMessage().contains("Insufficient funds for transaction.")) {
                System.out.println("\nYou do not have sufficient funds to complete this transaction");
            } else {
                System.out.println("\nThe transfer was unsuccessful. Please try again.");
            }
        }
        return t;
    }

    public String updateTransfer(Transfer transfer){
        String str = "";
        try{
            str = restTemplate.exchange(API_BASE_URL + "transfers/requests", HttpMethod.PUT, makeRequestBody(transfer), String.class).getBody();
        }catch(RestClientResponseException | ResourceAccessException e){
            if (e.getMessage() != null && e.getMessage().contains("Insufficient funds for transaction.")) {
                System.out.println("You do not have sufficient funds to complete this transaction");
            } else {
                System.out.println("There was an issue approving the request. Please try again.");
            }
        }
        return str;
    }

    public Transfer getTransferById(int transferId) {
        Transfer t = null;
        try {
            t = restTemplate.exchange(API_BASE_URL + "transfers/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("There was an issue retrieving the details of this transfer.");
        }
        return t;
    }

    public int getAccountIdByUserId(int userId) {
        int id = 0;
        try {
            id = restTemplate.exchange(API_BASE_URL + "users/" + userId + "/accounts", HttpMethod.GET, makeAuthEntity(), Integer.class).getBody();
        } catch(ResourceAccessException | RestClientResponseException e) {
            System.out.println("There was an issue retrieving the account ID for this user.");
        }
        return id;
    }

    public Transfer[] getTransfersByUserId(int statusId) {
        try {
            return restTemplate.exchange(API_BASE_URL + "transfers/status=" + statusId, HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();
        } catch(ResourceAccessException | RestClientResponseException e){
            System.out.println("There was an issue retrieving your transfers.");
            return null;
        }
    }

    public HttpEntity makeRequestBody(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(transfer, headers);
    }


    public HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        return new HttpEntity<>(headers);
    }
}
