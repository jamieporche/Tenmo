package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TenmoService;
import com.techelevator.view.ConsoleService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
    private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
    private static final String[] LOGIN_MENU_OPTIONS = {LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
    private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
    private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
    private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
    private static final String[] MAIN_MENU_OPTIONS = {MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
            MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS,
            MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private final int PENDING_TRANSFER_STATUS_ID = 1;
    private final int APPROVED_TRANSFER_STATUS_ID = 2;
    private final int REJECTED_TRANSFER_STATUS_ID = 3;
    private final int REQUEST_TRANSFER_TYPE_ID = 1;
    private final int SEND_TRANSFER_TYPE_ID = 2;

    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TenmoService tenmoService;

    public static void main(String[] args) {
        App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
        app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
        this.console = console;
        this.authenticationService = authenticationService;
        this.tenmoService = new TenmoService();
    }

    public void run() {
        System.out.println("***************************");
        System.out.println("**** Welcome to TEnmo! ****");
        System.out.println("***************************");

        registerAndLogin();
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            pause();
            String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
            if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
                viewCurrentBalance();
            } else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
                displayCompletedTransfers(APPROVED_TRANSFER_STATUS_ID);
            } else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
                displayPendingTransfers(PENDING_TRANSFER_STATUS_ID);
            } else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
                sendBucks();
            } else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
                requestBucks();
            } else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else {
                exitProgram();
            }
        }
    }

    private void viewCurrentBalance() {
        System.out.println("Your current account balance is: $" + tenmoService.getBalance());
    }

    private void sendBucks() {
        displayUsers();
        Integer idChoice = console.getUserInputInteger("\nEnter the ID of user you are sending TE bucks to (0 to cancel)");
        if (idChoice == 0) {
            return;
        }
        String amountChoice = console.getUserInput("Enter amount to send: $");
        while (!validateCurrencyInput(amountChoice)) {
            System.out.println("\nInvalid currency input, please try again...");
            amountChoice = console.getUserInput("\nEnter amount to send: $");
        }
        Transfer transfer = new Transfer(new BigDecimal(amountChoice), SEND_TRANSFER_TYPE_ID, APPROVED_TRANSFER_STATUS_ID,
                tenmoService.getAccountIdByUserId(idChoice), tenmoService.getAccountIdByUserId(currentUser.getUser().getId()));
        Transfer createdTransfer = tenmoService.createTransfer(transfer);
        if (createdTransfer != null) {
            System.out.println("\nYou sent $" + transfer.getTransferAmount() + " to " +
                    tenmoService.getUserByAccountId(createdTransfer.getPayeeAccount()).getUsername());
            System.out.println("Your new balance is $" + tenmoService.getBalance());
        }
    }

    private void requestBucks() {
        displayUsers();
		Integer idChoice = console.getUserInputInteger("\nEnter the ID of user you are requesting TE bucks from (0 to cancel)");
		if (idChoice == 0) {
			return;
		}
		String amountChoice = console.getUserInput("Enter amount requested: $");
		while (!validateCurrencyInput(amountChoice)) {
			System.out.println("\nInvalid currency input, please try again...");
			amountChoice = console.getUserInput("\nEnter amount requested: $");
		}
		Transfer transfer = new Transfer(new BigDecimal(amountChoice), REQUEST_TRANSFER_TYPE_ID, PENDING_TRANSFER_STATUS_ID,
				tenmoService.getAccountIdByUserId(currentUser.getUser().getId()), tenmoService.getAccountIdByUserId(idChoice));
		Transfer createdTransfer = tenmoService.createTransfer(transfer);
		if (createdTransfer != null) {
            System.out.println("\nYour request of $" + transfer.getTransferAmount() + " from " +
                    tenmoService.getUserByAccountId(createdTransfer.getPayerAccount()).getUsername() + " has been sent.");
        }
    }

    private boolean validateCurrencyInput(String input) {
        String amountChoiceCopy = input.replaceAll("[^0-9.]", "");
        return input.equals(amountChoiceCopy);
    }

    private void exitProgram() {
        System.exit(0);
    }

    private void registerAndLogin() {
        while (!isAuthenticated()) {
            String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
            if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
                register();
            } else {
                // the only other option on the login menu is to exit
                exitProgram();
            }
        }
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    private void register() {
        System.out.println("Please register a new user account");
        boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                authenticationService.register(credentials);
                isRegistered = true;
                System.out.println("\nRegistration successful. You can now login.");
            } catch (AuthenticationServiceException e) {
                System.out.println("REGISTRATION ERROR: " + e.getMessage());
                System.out.println("Please attempt to register again.");
            }
        }
    }

    private void login() {
        System.out.println("Please log in");
        currentUser = null;
        while (currentUser == null) //will keep looping until user is logged in
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                currentUser = authenticationService.login(credentials);
                tenmoService.setAuthToken(currentUser.getToken());
            } catch (AuthenticationServiceException e) {
                System.out.println("\nLOGIN ERROR: " + e.getMessage());
                System.out.println("\nPlease attempt to login again.");
            }
        }
        System.out.println("\nLogin successful!");
    }

    private UserCredentials collectUserCredentials() {
        String username = console.getUserInput("Username: ");
        String password = console.getUserInput("Password: ");
        return new UserCredentials(username, password);
    }

    private void displayUsers() {
        System.out.println("----------------------------");
        System.out.println(String.format("| %-25s|", "Users"));
        System.out.println("----------------------------");
        System.out.println(String.format("| %-8s| %-15s|","ID", "Name"));
        System.out.println("----------------------------");
        User[] users = tenmoService.getAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(currentUser.getUser().getUsername())) {
                continue;
            }
            System.out.println(String.format("| %-8s| %-15s|", user.getId(), user.getUsername()));
        }
        System.out.println("----------------------------");
    }

    private void displayCompletedTransfers(int statusId) {
        Transfer[] transfers = tenmoService.getTransfersByUserId(statusId);

        if (transfers.length == 0) {
            System.out.println("There are no completed transfers to display.");
        } else {
            System.out.println("-----------------------------------------------");
            System.out.println(String.format("| %-44s|", "Completed Transfers"));
            System.out.println("-----------------------------------------------");
            System.out.println(String.format("| %-8s| %-20s| %-12s|", "ID", "To/From", "Amount"));
            System.out.println("-----------------------------------------------");
            List<Integer> transferIds = new ArrayList<>();
            for (Transfer transfer : transfers) {
                String direction = "";
                int accountId = 0;
                if (transfer.getPayerAccount() == tenmoService.getAccountIdByUserId(currentUser.getUser().getId())) {
                    direction = "To:";
                    accountId = transfer.getPayeeAccount();
                } else {
                    direction = "From:";
                    accountId = transfer.getPayerAccount();
                }
                transferIds.add(transfer.getTransferId());
                System.out.println(String.format("| %-8s| %-6s%-13s | %-12s|", transfer.getTransferId(), direction,
                        tenmoService.getUserByAccountId(accountId).getUsername(),
                        (direction.equals("To:") ? "- $" : "+ $") + transfer.getTransferAmount()));
            }
            System.out.println("-----------------------------------------------");
            Integer selectedTransferId = selectTransfer(transferIds, statusId);
            if (selectedTransferId != 0) {
                displayTransferDetails(selectedTransferId);
            }
        }
    }

    private void displayPendingTransfers(int statusId) {
        Transfer[] transfers = tenmoService.getTransfersByUserId(statusId);

        if (transfers.length == 0) {
            System.out.println("There are no transfer requests to display.");
        } else {
            System.out.println("-----------------------------------------------------------------");
            System.out.println(String.format("| %-62s|", "Pending Transfer Requests"));
            System.out.println("-----------------------------------------------------------------");
            System.out.println(String.format("| %-8s| %-50s  |", "ID", "Details"));
            System.out.println("-----------------------------------------------------------------");
            List<Integer> transferIds = new ArrayList<>();
            for (Transfer transfer : transfers) {
                String details = "";
                int accountId = 0;
                if (transfer.getPayerAccount() == tenmoService.getAccountIdByUserId(currentUser.getUser().getId())) {
                    accountId = transfer.getPayeeAccount();
                    details = tenmoService.getUserByAccountId(accountId).getUsername() + " is requesting $" +
                            transfer.getTransferAmount();
                } else {
                    accountId = transfer.getPayerAccount();
                    details = "You are requesting $" + transfer.getTransferAmount() + " from " +
                            tenmoService.getUserByAccountId(accountId).getUsername();
                }
                transferIds.add(transfer.getTransferId());
                System.out.println(String.format("| %-8s| %-50s  |", transfer.getTransferId(), details));
            }
            System.out.println("-----------------------------------------------------------------");
            Integer selectedTransferId = selectTransfer(transferIds, statusId);
            if (selectedTransferId != 0) {
                displayTransferDetails(selectedTransferId);
                updateTransfer(selectedTransferId);
            }
        }
    }

    private Integer selectTransfer(List<Integer> transferIds, int statusId){
        Integer transferId = 0;

        if (statusId == APPROVED_TRANSFER_STATUS_ID){
            transferId = console.getUserInputInteger("\nPlease enter transfer ID to view details (0 to cancel)");
            while (!transferIds.contains(transferId) && transferId != 0) {
                transferId = console.getUserInputInteger("\nPlease enter transfer ID to view details (0 to cancel)");
            }
        } else if (statusId == PENDING_TRANSFER_STATUS_ID) {
            transferId = console.getUserInputInteger("\nPlease enter transfer ID to approve or reject (0 to cancel)");
            while (!transferIds.contains(transferId) && transferId != 0) {
                transferId = console.getUserInputInteger("\nPlease enter transfer ID to approve or reject (0 to cancel)");
            }
        }
        return transferId;
    }

    private void updateTransfer(int transferId) {

        Transfer transferToUpdate = tenmoService.getTransferById(transferId);
        Integer userApprovalCode = console.getUserInputInteger("\nPlease enter 1 to approve, or 2 to reject (0 to cancel)");
        while (userApprovalCode > 3 && userApprovalCode < 0){
            userApprovalCode = console.getUserInputInteger("\nPlease enter 1 to approve, or 2 to reject (0 to cancel)");
        }
        if (userApprovalCode == 1) {
            transferToUpdate.setTransferStatus(APPROVED_TRANSFER_STATUS_ID);
            System.out.println(tenmoService.updateTransfer(transferToUpdate));
            System.out.println("Your new balance is $" + tenmoService.getBalance());

        } else if (userApprovalCode == 2){
            transferToUpdate.setTransferStatus(REJECTED_TRANSFER_STATUS_ID);
            System.out.println(tenmoService.updateTransfer(transferToUpdate));
        }
    }

    private void displayTransferDetails(int id) {
        Transfer transfer = tenmoService.getTransferById(id);
        System.out.println(System.lineSeparator() + "-------------------------------");
        System.out.println(String.format("| %-28s|", "Transfer Details"));
        System.out.println("-------------------------------");
        System.out.println(String.format("| %7s %-20s|", "Id:", transfer.getTransferId()));
        System.out.println(String.format("| %7s %-20s|", "To:", tenmoService.getUserByAccountId(transfer.getPayeeAccount()).getUsername()));
        System.out.println(String.format("| %7s %-20s|", "From:", tenmoService.getUserByAccountId(transfer.getPayerAccount()).getUsername()));
        System.out.println(String.format("| %7s %-20s|", "Type:", (transfer.getTransferType() == 1 ? "Request" : "Send")));
        String status = "";
        switch (transfer.getTransferStatus()) {
            case 1:
                status = "Pending";
                break;
            case 2:
                status = "Approved";
                break;
            case 3:
                status = "Rejected";
                break;
        }
        System.out.println(String.format("| %7s %-20s|", "Status:", status));
        System.out.println(String.format("| %7s%-19s|", "Amount: $", transfer.getTransferAmount()));
        System.out.println("-------------------------------");
    }

    public static void pause() {
        System.out.println(System.lineSeparator() + "Press Enter to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
