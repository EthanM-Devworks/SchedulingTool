package com.example.software_ii_project;

import com.example.software_ii_project.DAO.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

import static com.example.software_ii_project.TimeHelper.formatTime;


public class MainViewController implements Initializable {
    //Customers Tab

    @FXML
    private TableView<Customers> customerTable;
    @FXML
    private TableColumn customerIDColumn;
    @FXML
    private TableColumn customerNameColumn;
    @FXML
    private TableColumn customerAddressColumn;
    @FXML
    private TableColumn customerPostalColumn;
    @FXML
    private TableColumn customerPhoneColumn;
    @FXML
    private TableColumn customerDivisionColumn;
    @FXML
    private Button customerDeleteButton;
    @FXML
    private Button customerExitButton;
    @FXML
    private Text customerDeleteLabel;
    @FXML
    private TextField customerIDField;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField customerAddressField;
    @FXML
    private TextField customerPostalCodeField;
    @FXML
    private TextField customerPhoneNumberField;
    @FXML
    private Button customerAddUpdateButton;
    @FXML
    private Button customerEditButton;
    @FXML
    private Button customerCopyButton;
    @FXML
    private ComboBox<String> customerCountryCombo;
    @FXML
    private ComboBox customerDivisionCombo;

    /**
     * Asks the user if they would like to delete the currently selected customer, and if so, deletes the customer from the database.
     * - If a user has related appointments, those are also automatically deleted so that no NREs occur.
     * - Related tables are all refreshed.
     * @param event
     * @throws Exception
     */
    @FXML private void handleCustomerDeleteButtonAction(ActionEvent event) throws Exception {
        try {
            Connection connection = JDBC.openConnection();
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();

            if (!customerTable.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you would like to delete the selected customer (ID #" + customerTable.getSelectionModel().getSelectedItem().getCustomerId() + ") and their related appointments?");
                Optional<ButtonType> confirm = alert.showAndWait();

                if (confirm.isPresent() && confirm.get() == ButtonType.OK) {
                    int tableCustomerID = customerTable.getSelectionModel().getSelectedItem().getCustomerId();
                    AppointmentsDAO.deleteAppointmentByCustomer(tableCustomerID, connection);

                    String sqlStatement = "DELETE FROM customers WHERE Customer_ID=?";
                    JDBC.setPreparedStatement(JDBC.getConnection(), sqlStatement);
                    PreparedStatement ps = JDBC.getPreparedStatement();

                    ps.setInt(1, tableCustomerID);
                    ps.execute();

                    appointmentUserCombo.setValue(null);

                    ObservableList<Customers> customerRefresh = CustomersDAO.getAllCustomers();
                    ObservableList<String> customerNames = FXCollections.observableArrayList();
                    // LAMBDA #1 (This is to avoid having to use a for loop here. This particular line is used multiple times throughout the code, so I'll mark the rest of these as dittos.)
                    customerRefresh.forEach(customers -> customerNames.add(customers.getCustomerName()));
                    appointmentCustomerCombo.setItems(customerNames);
                    customerTable.setItems(customerRefresh);

                    if (viewMode == 1){
                        ObservableList<Appointments> appointmentsRefresh = AppointmentsDAO.getAllAppointments();
                        appointmentTable.setItems(appointmentsRefresh);
                    }
                    if (viewMode == 2) {
                        ObservableList<Appointments> refreshAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> weekAppointments = FXCollections.observableArrayList();

                        LocalDateTime weekStart = LocalDateTime.now();
                        LocalDateTime weekEnd = LocalDateTime.now().plusWeeks(1);

                        // LAMBDA #2 (Once again used to avoid a for loop. Multiple dittos follow.)
                        if (!refreshAppointments.isEmpty()) {
                            refreshAppointments.forEach(a -> {
                                if (a.getAppointmentEnd().isAfter(weekStart) && a.getAppointmentEnd().isBefore(weekEnd)) {
                                    weekAppointments.add(a);
                                }
                                appointmentTable.setItems(weekAppointments);
                            });
                        }
                    }
                    if (viewMode == 3) {
                        ObservableList<Appointments> refreshAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> monthAppointments = FXCollections.observableArrayList();

                        LocalDateTime monthStart = LocalDateTime.now();
                        LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1);

                        // LAMBDA #3 (Very similar purpose to #2, just for setting up the monthly view instead of the weekly view. Multiple dittos follow.)
                        if (!refreshAppointments.isEmpty()) {
                            refreshAppointments.forEach(a -> {
                                if (a.getAppointmentEnd().isAfter(monthStart) && a.getAppointmentEnd().isBefore(monthEnd)) {
                                    monthAppointments.add(a);
                                }
                                appointmentTable.setItems(monthAppointments);
                            });
                        }
                    }

                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must select an entry to delete in the table.", ButtonType.OK);
                alert.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the application. What else would this do?
     * @param event
     */
    @FXML
    private void handleCustomerExitButtonAction(ActionEvent event) {
        Platform.exit();
    }

    /**
     * Handles the action of the add customer button, which includes the following:
     * - Checks if customerValidateFields returns an empty list, and if so:
     * - - Sets the new customer's ID to the earliest available open number.
     * - - Inserts a new customer into the database with the information in the customer form's fields.
     * - - All related tables are refreshes to be accurate.
     * - If validation fails:
     * - - An error message appears which tells the user their form was incorrectly set up.
     * @param event
     */
    @FXML
    private void handleCustomerAddUpdateButtonAction(ActionEvent event) {
        List<String> invalidFields = this.customerValidateFields();

        try {
            Connection connection = JDBC.openConnection();

            if (invalidFields.isEmpty()) {
                int id = 1;
                if (!CustomersDAO.getAllCustomers().isEmpty()) {
                    id = CustomersDAO.getAllCustomers().get(CustomersDAO.getAllCustomers().size() - 1).getCustomerId() + 1;
                }
                int flDivisionID = 0;

                for (FirstLevelDivisionsDAO fld : FirstLevelDivisionsDAO.getAllFirstLevelDivisions()) {
                    if (customerDivisionCombo.getSelectionModel().getSelectedItem().equals(fld.getDivision())) {
                        flDivisionID = fld.getDivisionId();
                    }
                }
                String sqlStatement = "INSERT INTO customers (Customer_ID, Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                JDBC.setPreparedStatement(JDBC.getConnection(), sqlStatement);
                PreparedStatement ps = JDBC.getPreparedStatement();

                String userName = UsersDAO.getCurrentUserName();

                System.out.println("Attempting to create user with ID " + id);
                ps.setInt(1, id);
                ps.setString(2, customerNameField.getText());
                ps.setString(3, customerAddressField.getText());
                ps.setString(4, customerPostalCodeField.getText());
                ps.setString(5, customerPhoneNumberField.getText());
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(7, userName);
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(9, userName);
                ps.setInt(10, flDivisionID);
                ps.execute();

                System.out.println("Ignore the following NRE, it does not effect the performance at all.");

                customerIDField.clear();
                customerNameField.clear();
                customerAddressField.clear();
                customerPostalCodeField.clear();
                customerPhoneNumberField.clear();
                customerCountryCombo.setValue(null);
                customerDivisionCombo.setValue(null);
                customerDivisionCombo.setDisable(true);
                appointmentUserCombo.setValue(null);

                ObservableList<Customers> customerRefresh = CustomersDAO.getAllCustomers();
                ObservableList<String> customerNames = FXCollections.observableArrayList();
                // LAMBDA #1 DITTO
                customerRefresh.forEach(customers -> customerNames.add(customers.getCustomerName()));
                appointmentCustomerCombo.setItems(customerNames);
                customerTable.setItems(customerRefresh);
            }
            else {
                StringJoiner joiner = new StringJoiner("\n", "The following errors must be fixed: \n", "");
                Objects.requireNonNull(joiner);
                invalidFields.forEach(joiner::add);
                Alert alert = new Alert(Alert.AlertType.ERROR, joiner.toString(), ButtonType.OK);
                alert.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action of the edit customer button, which includes the following:
     * - If a customer is selected in the table:
     * - - If customerValidateFields returns empty:
     * - - - The currently selected customer is edited with the information in the fields.
     * - - - All related tables are refreshed.
     * - - If validation fails:
     * - - - An error message appears which tells the user the fields were incorrectly formatted.
     * - If customer isn't selected:
     * - - An error message appears which tells the user they need to select a customer in the table.
     * @param event
     * @throws SQLException
     */
    @FXML private void handleCustomerEditButtonAction(ActionEvent event) throws SQLException {
        try {
            Connection connection = JDBC.openConnection();

            List<String> invalidFields = this.customerValidateFields();

            if (!customerTable.getSelectionModel().isEmpty()) {
                if (customerValidateFields().isEmpty()) {
                    int fldID = 0;
                    Customers selectedCustomer = (Customers) customerTable.getSelectionModel().getSelectedItem();

                    for (FirstLevelDivisionsDAO fld : FirstLevelDivisionsDAO.getAllFirstLevelDivisions()) {
                        if (customerDivisionCombo.getSelectionModel().getSelectedItem().equals(fld.getDivision())) {
                            fldID = fld.getDivisionId();
                        }
                    }

                    String sqlStatement = "UPDATE customers SET Customer_ID = ?, Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Last_Update = ?, Last_Updated_By = ?, Division_ID = ? WHERE Customer_ID = ?";
                    JDBC.setPreparedStatement(JDBC.getConnection(), sqlStatement);
                    PreparedStatement ps = JDBC.getPreparedStatement();
                    String userName = UsersDAO.getCurrentUserName();

                    System.out.println("User [" + userName + "] is making an update");
                    ps.setInt (1, selectedCustomer.getCustomerId());
                    ps.setString (2, customerNameField.getText());
                    ps.setString (3, customerAddressField.getText());
                    ps.setString (4, customerPostalCodeField.getText());
                    ps.setString (5, customerPhoneNumberField.getText());
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(7, userName);
                    ps.setInt(8, fldID);
                    ps.setInt(9, selectedCustomer.getCustomerId());
                    ps.execute();

                    customerIDField.clear();
                    customerNameField.clear();
                    customerAddressField.clear();
                    customerPostalCodeField.clear();
                    customerPhoneNumberField.clear();
                    customerCountryCombo.setValue(null);
                    customerDivisionCombo.setValue(null);
                    customerDivisionCombo.setDisable(true);
                    appointmentUserCombo.setValue(null);

                    ObservableList<Customers> customerRefresh = CustomersDAO.getAllCustomers();
                    ObservableList<String> customerNames = FXCollections.observableArrayList();
                    // LAMBDA #1 DITTO
                    customerRefresh.forEach(customers -> customerNames.add(customers.getCustomerName()));
                    appointmentCustomerCombo.setItems(customerNames);
                    customerTable.setItems(customerRefresh);
                }
                else {
                    StringJoiner joiner = new StringJoiner("\n", "The following errors must be fixed: \n", "");
                    Objects.requireNonNull(joiner);
                    invalidFields.forEach(joiner::add);
                    Alert alert = new Alert(Alert.AlertType.ERROR, joiner.toString(), ButtonType.OK);
                    alert.show();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must select a customer from the table to edit.", ButtonType.OK);
                alert.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies a selected customer's data from the table, if one is selected. This also updates the division combo's items with the divisions from the copied customer's country.
     * This was made as a quick solution to handle requirement A2's "All of the original customer information is displayed on the update form" stipulation without programming an entire extra update menu.
     * @param event
     */
    @FXML private void handleCustomerCopyButtonAction(ActionEvent event) {
        try {
            JDBC.openConnection();

            if (!customerTable.getSelectionModel().isEmpty()) {
                Customers customerToCopy = customerTable.getSelectionModel().getSelectedItem();
                ObservableList<CountriesDAO> allCountries = CountriesDAO.getAllCountries();
                ObservableList<FirstLevelDivisionsDAO> allFirstLevelDivisions = FirstLevelDivisionsDAO.getAllFirstLevelDivisions();
                ObservableList<String> fldNames = FXCollections.observableArrayList();
                String division = "";
                String country = "";

                for (FirstLevelDivisions fld: allFirstLevelDivisions) {
                    int countryID = fld.getCountryId();

                    if (fld.getDivisionId() == customerToCopy.getCustomerDivisionID()) {
                        division = fld.getDivision();

                        for (Countries c: allCountries) {
                            if (c.getCountryId() == countryID) {
                                country = c.getCountry();
                                for (FirstLevelDivisions fld2: allFirstLevelDivisions) {
                                    if (fld2.getCountryId() == c.getCountryId()) {
                                        fldNames.add(fld2.getDivision());
                                    }
                                }
                            }
                        }
                    }
                }

                customerIDField.setText(String.valueOf(customerToCopy.getCustomerId()));
                customerNameField.setText(customerToCopy.getCustomerName());
                customerAddressField.setText(customerToCopy.getCustomerAddress());
                customerPostalCodeField.setText(customerToCopy.getCustomerPostal());
                customerPhoneNumberField.setText(customerToCopy.getCustomerPhone());
                customerCountryCombo.setValue(country);
                customerDivisionCombo.setItems(fldNames);
                customerDivisionCombo.setValue(division);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must select an entry to copy from the table.", ButtonType.OK);
                alert.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Upon use of the country combobox, fills the division combobox with related divisions.
     * @param event
     */
    @FXML private void handleCustomerCountryComboAction(ActionEvent event) {
        try {
            customerDivisionCombo.setItems(null);
            JDBC.openConnection();

            String selectedCountry = customerCountryCombo.getSelectionModel().getSelectedItem();
            ObservableList<FirstLevelDivisionsDAO> allFirstLevelDivisions = FirstLevelDivisionsDAO.getAllFirstLevelDivisions();
            ObservableList<String> divisionsUS = FXCollections.observableArrayList();
            ObservableList<String> divisionsUK = FXCollections.observableArrayList();
            ObservableList<String> divisionsCanada = FXCollections.observableArrayList();

            // LAMBDA #4 (Once again used to avoid a for loop to separate divisions between three different countries into different lists.)
            allFirstLevelDivisions.forEach(FirstLevelDivisions -> {
                if (FirstLevelDivisions.getCountryId() == 1) {
                    divisionsUS.add(FirstLevelDivisions.getDivision());
                }
                else if (FirstLevelDivisions.getCountryId() == 2) {
                    divisionsUK.add(FirstLevelDivisions.getDivision());
                }
                else if (FirstLevelDivisions.getCountryId() == 3) {
                    divisionsCanada.add(FirstLevelDivisions.getDivision());
                }
            });

            switch (selectedCountry) {
                case "U.S" -> {
                    customerDivisionCombo.setItems(divisionsUS);
                    customerDivisionCombo.setDisable(false);
                }
                case "UK" -> {
                    customerDivisionCombo.setItems(divisionsUK);
                    customerDivisionCombo.setDisable(false);
                }
                case "Canada" -> {
                    customerDivisionCombo.setItems(divisionsCanada);
                    customerDivisionCombo.setDisable(false);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the customer form for incorrect formatting, and if an incorrectly formatted/missing field is found, it is added to a list.
     * @return invalidFields
     */
    private List<String> customerValidateFields() {
        List<String> invalidFields = new ArrayList();
        if (this.customerNameField.getText().equals("")) {
            invalidFields.add("Name is blank");
        }

        if (this.customerNameField.getText().length() > 50) {
            invalidFields.add("Name too long");
        }

        if (this.customerAddressField.getText().equals("")) {
            invalidFields.add("Address is blank");
        }

        if (this.customerAddressField.getText().length() > 100) {
            invalidFields.add("Address too long");
        }

        if (this.customerPostalCodeField.getText().equals("")) {
            invalidFields.add("Postal Code is blank");
        }

        if (this.customerPostalCodeField.getText().length() > 50) {
            invalidFields.add("Postal Code too long");
        }

        if (this.customerPhoneNumberField.getText().equals("")) {
            invalidFields.add("Phone Number is blank");
        }

        if (this.customerPhoneNumberField.getText().length() > 50) {
            invalidFields.add("Phone Number too long");
        }

        if (this.customerCountryCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("Country is blank");
        }

        if (this.customerDivisionCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("Division is blank");
        }

        return invalidFields;
    }

    //Appointments Tab
    @FXML
    private TableView<Appointments> appointmentTable;
    @FXML
    private TableColumn appointmentIDColumn;
    @FXML
    private TableColumn appointmentTitleColumn;
    @FXML
    private TableColumn appointmentDescriptionColumn;
    @FXML
    private TableColumn appointmentLocationColumn;
    @FXML
    private TableColumn appointmentContactColumn;
    @FXML
    private TableColumn appointmentTypeColumn;
    @FXML
    private TableColumn appointmentStartColumn;
    @FXML
    private TableColumn appointmentEndColumn;
    @FXML
    private TableColumn appointmentCustomerIDColumn;
    @FXML
    private TableColumn appointmentUserIDColumn;
    @FXML
    private TextField appointmentIDField;
    @FXML
    private TextField appointmentTitleField;
    @FXML
    private TextField appointmentDescriptionField;
    @FXML
    private TextField appointmentLocationField;
    @FXML
    private TextField appointmentTypeField;
    @FXML
    private DatePicker appointmentStartDate;
    @FXML
    private ComboBox<String> appointmentStartCombo;
    @FXML
    private DatePicker appointmentEndDate;
    @FXML
    private ComboBox<String> appointmentEndCombo;
    @FXML
    private Button appointmentAddButton;
    @FXML
    private Button appointmentEditButton;
    @FXML
    private Button appointmentCopyButton;
    @FXML
    private ComboBox appointmentCustomerCombo;
    @FXML
    private ComboBox appointmentContactCombo;
    @FXML
    private ComboBox appointmentUserCombo;
    @FXML
    private Text appointmentCancelLabel;
    @FXML
    private Button appointmentDeleteButton;
    @FXML
    private Button appointmentExitButton;
    @FXML
    private RadioButton appointmentAllRadio;
    @FXML
    private RadioButton appointmentWeekRadio;
    @FXML
    private RadioButton appointmentMonthRadio;

    int viewMode = 1;

    /**
     * Handles the action of the add appointment button, which includes the following:
     * - If appointmentValidateFields returns empty:
     * - - Appointment ID is set to the earliest available one.
     * - - Customer, User, and Contact IDs are pulled from the names provided in the comboboxes.
     * - - Start/End dates/times from the date pickers/comboboxes are formatted so that they can be easily inserted into the SQL database.
     * - - The previously discussed data, as well as data from the remaining fields, are inserted into the database as a new appointment.
     * - - Relevant tables are refreshed.
     * - If validation fails:
     * - - An error message is displayed stating that fields are missing/incorrectly formatted.
     * @param event
     * @throws IOException
     */
    @FXML private void handleAppointmentAddButtonAction(ActionEvent event) throws IOException {
        try {
            Connection connection = JDBC.openConnection();

            int id = 1;
            if (!AppointmentsDAO.getAllAppointments().isEmpty()) {
                id = AppointmentsDAO.getAllAppointments().get(AppointmentsDAO.getAllAppointments().size() - 1).getAppointmentID() + 1;
            }

            List<String> invalidFields = this.appointmentValidateFields(id);

            if (appointmentValidateFields(id).isEmpty()) {
                ObservableList<Customers> allCustomers = CustomersDAO.getAllCustomers();
                ObservableList<Users> allUsers = UsersDAO.getAllUsers();
                ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();

                String userName = UsersDAO.getCurrentUserName();

                int customerID = 0;
                for (Customers c: allCustomers) {
                    if (c.getCustomerName().equals(appointmentCustomerCombo.getValue())) {
                        customerID = c.getCustomerId();
                    }
                }

                int userID = 0;
                for (Users u: allUsers) {
                    if (u.getUserName().equals(appointmentUserCombo.getValue())) {
                        userID = u.getUserId();
                    }
                }

                int contactID = 0;
                for (Contacts c: allContacts) {
                    if (c.getContactName().equals(appointmentContactCombo.getValue())) {
                        contactID = c.getContactID();
                    }
                }

                String startDate = appointmentStartDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String startTime = appointmentStartCombo.getSelectionModel().getSelectedItem();
                String endDate = appointmentEndDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String endTime = appointmentEndCombo.getSelectionModel().getSelectedItem();
                String formattedStart = formatTime(startDate + " " + startTime + ":00");
                String formattedEnd  = formatTime(endDate + " " + endTime + ":00");

                String sqlStatement = "INSERT INTO appointments (Appointment_ID, Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                JDBC.setPreparedStatement(JDBC.getConnection(), sqlStatement);
                PreparedStatement ps = JDBC.getPreparedStatement();
                ps.setInt(1, id);
                ps.setString(2, appointmentTitleField.getText());
                ps.setString(3, appointmentDescriptionField.getText());
                ps.setString(4, appointmentLocationField.getText());
                ps.setString(5, appointmentTypeField.getText());
                ps.setTimestamp(6, Timestamp.valueOf(formattedStart));
                ps.setTimestamp(7, Timestamp.valueOf(formattedEnd));
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(9, userName);
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(11, userName);
                ps.setInt(12, customerID);
                ps.setInt(13, userID);
                ps.setInt(14, contactID);
                ps.execute();

                if (viewMode == 1){
                    ObservableList<Appointments> appointmentsRefresh = AppointmentsDAO.getAllAppointments();
                    appointmentTable.setItems(appointmentsRefresh);
                }
                if (viewMode == 2) {
                    ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                    ObservableList<Appointments> weekAppointments = FXCollections.observableArrayList();

                    LocalDateTime weekStart = LocalDateTime.now();
                    LocalDateTime weekEnd = LocalDateTime.now().plusWeeks(1);

                    // LAMBDA #2 DITTO
                    if (!allAppointments.isEmpty()) {
                        allAppointments.forEach(a -> {
                            if (a.getAppointmentEnd().isAfter(weekStart) && a.getAppointmentEnd().isBefore(weekEnd)) {
                                weekAppointments.add(a);
                            }
                            appointmentTable.setItems(weekAppointments);
                        });
                    }
                }
                if (viewMode == 3) {
                    ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                    ObservableList<Appointments> monthAppointments = FXCollections.observableArrayList();

                    LocalDateTime monthStart = LocalDateTime.now();
                    LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1);

                    // LAMBDA #3 DITTO
                    if (!allAppointments.isEmpty()) {
                        allAppointments.forEach(a -> {
                            if (a.getAppointmentEnd().isAfter(monthStart) && a.getAppointmentEnd().isBefore(monthEnd)) {
                                monthAppointments.add(a);
                            }
                            appointmentTable.setItems(monthAppointments);
                        });
                    }
                }

                if (!contactReportCombo.getSelectionModel().isEmpty()) {
                    int tempContactID = 0 ;
                    String contactName = contactReportCombo.getSelectionModel().getSelectedItem();

                    ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                    ObservableList<Appointments> contactAppointments = FXCollections.observableArrayList();

                    for (Contacts c: allContacts) {
                        if (contactName.equals(c.getContactName())) {
                            tempContactID = c.getContactID();
                        }
                    }

                    for (Appointments a: allAppointments) {
                        if (a.getAppointmentContactID() == tempContactID) {
                            contactAppointments.add(a);
                        }
                    }
                    contactReportTable.setItems(contactAppointments);
                }

                appointmentIDField.clear();
                appointmentTitleField.clear();
                appointmentDescriptionField.clear();
                appointmentLocationField.clear();
                appointmentTypeField.clear();
                appointmentStartDate.setValue(null);
                appointmentStartCombo.setValue(null);
                appointmentEndDate.setValue(null);
                appointmentEndCombo.setValue(null);
                appointmentCustomerCombo.setValue(null);
                appointmentUserCombo.setValue(null);
                appointmentContactCombo.setValue(null);
                appointmentCancelLabel.setText("");


            }
            else {
                StringJoiner joiner = new StringJoiner("\n", "The following errors must be fixed: \n", "");
                Objects.requireNonNull(joiner);
                invalidFields.forEach(joiner::add);
                Alert alert = new Alert(Alert.AlertType.ERROR, joiner.toString(), ButtonType.OK);
                alert.show();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the actions of the edit appointment button, including:
     * - If an appointment is selected from the appointments table:
     * - - If appointmentValidateFields returns empty:
     * - - - Customer, User, and Contact IDs are pulled from the names provided in the comboboxes.
     * - - - Start/End Dates/Times from date pickers/comboboxes are formatted for easy insertion to the SQL database.
     * - - - Selected appointment in SQL server is updated using provided information.
     * - - - Related tables are refreshed.
     * - - If validation fails:
     * - - - An error message appears which states that fields are missing/incorrectly formatted.
     * - If no appointment is selected:
     * - - An error message appears telling the user to select an appointment from the table.
     * @param event
     */
    @FXML private void handleAppointmentEditButtonAction(ActionEvent event) {
        Connection connection = JDBC.openConnection();

        try {
            if (!appointmentTable.getSelectionModel().isEmpty()) {
                int selectedAppointmentID = appointmentTable.getSelectionModel().getSelectedItem().getAppointmentID();
                List<String> invalidFields = this.appointmentValidateFields(selectedAppointmentID);

                if (appointmentValidateFields(selectedAppointmentID).isEmpty()) {
                    ObservableList<Customers> allCustomers = CustomersDAO.getAllCustomers();
                    ObservableList<Users> allUsers = UsersDAO.getAllUsers();
                    ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();

                    int customerID = 0;
                    for (Customers c: allCustomers) {
                        if (c.getCustomerName().equals(appointmentCustomerCombo.getValue())) {
                            customerID = c.getCustomerId();
                        }
                    }

                    int userID = 0;
                    for (Users u: allUsers) {
                        if (u.getUserName().equals(appointmentUserCombo.getValue())) {
                            userID = u.getUserId();
                        }
                    }

                    int contactID = 0;
                    for (Contacts c: allContacts) {
                        if (c.getContactName().equals(appointmentContactCombo.getValue())) {
                            contactID = c.getContactID();
                        }
                    }

                    String startDate = appointmentStartDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String endDate = appointmentEndDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String startTime = appointmentStartCombo.getValue();
                    String endTime = appointmentEndCombo.getValue();
                    String formattedStart = formatTime(startDate + " " + startTime + ":00");
                    String formattedEnd = formatTime(endDate + " " + endTime + ":00");

                    String userName = UsersDAO.getCurrentUserName();

                    String sqlStatement = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, Last_Update = ?, Last_Updated_By = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ? WHERE Appointment_ID = ?";

                    JDBC.setPreparedStatement(JDBC.getConnection(), sqlStatement);
                    PreparedStatement ps = JDBC.getPreparedStatement();
                    ps.setString(1, appointmentTitleField.getText());
                    ps.setString(2, appointmentDescriptionField.getText());
                    ps.setString(3, appointmentLocationField.getText());
                    ps.setString(4, appointmentTypeField.getText());
                    ps.setTimestamp(5, Timestamp.valueOf(formattedStart));
                    ps.setTimestamp(6, Timestamp.valueOf((formattedEnd)));
                    ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(8, userName);
                    ps.setInt(9, customerID);
                    ps.setInt(10, userID);
                    ps.setInt(11, contactID);
                    ps.setInt(12, selectedAppointmentID);
                    ps.execute();

                    if (viewMode == 1){
                        ObservableList<Appointments> appointmentsRefresh = AppointmentsDAO.getAllAppointments();
                        appointmentTable.setItems(appointmentsRefresh);
                    }
                    if (viewMode == 2) {
                        ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> weekAppointments = FXCollections.observableArrayList();

                        LocalDateTime weekStart = LocalDateTime.now();
                        LocalDateTime weekEnd = LocalDateTime.now().plusWeeks(1);

                        // LAMBDA #2 DITTO
                        if (!allAppointments.isEmpty()) {
                            allAppointments.forEach(a -> {
                                if (a.getAppointmentEnd().isAfter(weekStart) && a.getAppointmentEnd().isBefore(weekEnd)) {
                                    weekAppointments.add(a);
                                }
                                appointmentTable.setItems(weekAppointments);
                            });
                        }
                    }
                    if (viewMode == 3) {
                        ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> monthAppointments = FXCollections.observableArrayList();

                        LocalDateTime monthStart = LocalDateTime.now();
                        LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1);

                        // LAMBDA #3 DITTO
                        if (!allAppointments.isEmpty()) {
                            allAppointments.forEach(a -> {
                                if (a.getAppointmentEnd().isAfter(monthStart) && a.getAppointmentEnd().isBefore(monthEnd)) {
                                    monthAppointments.add(a);
                                }
                                appointmentTable.setItems(monthAppointments);
                            });
                        }
                    }

                    if (!contactReportCombo.getSelectionModel().isEmpty()) {
                        int tempContactID = 0 ;
                        String contactName = contactReportCombo.getSelectionModel().getSelectedItem();

                        ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> contactAppointments = FXCollections.observableArrayList();

                        for (Contacts c: allContacts) {
                            if (contactName.equals(c.getContactName())) {
                                tempContactID = c.getContactID();
                            }
                        }

                        for (Appointments a: allAppointments) {
                            if (a.getAppointmentContactID() == tempContactID) {
                                contactAppointments.add(a);
                            }
                        }
                        contactReportTable.setItems(contactAppointments);
                    }

                    appointmentIDField.clear();
                    appointmentTitleField.clear();
                    appointmentDescriptionField.clear();
                    appointmentLocationField.clear();
                    appointmentTypeField.clear();
                    appointmentStartDate.setValue(null);
                    appointmentStartCombo.setValue(null);
                    appointmentEndDate.setValue(null);
                    appointmentEndCombo.setValue(null);
                    appointmentCustomerCombo.setValue(null);
                    appointmentUserCombo.setValue(null);
                    appointmentContactCombo.setValue(null);
                    appointmentCancelLabel.setText("");

                }
                else {
                    StringJoiner joiner = new StringJoiner("\n", "The following errors must be fixed: \n", "");
                    Objects.requireNonNull(joiner);
                    invalidFields.forEach(joiner::add);
                    Alert alert = new Alert(Alert.AlertType.ERROR, joiner.toString(), ButtonType.OK);
                    alert.show();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must select an entry to edit from the table.", ButtonType.OK);
                alert.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies data from a selected appointment on the table to the form, if there is a selected appointment.
     * This is once again a quick and simple way for me to meet the requirement for copying data to the form when updating without me having to program an entirely separate menu.
     * @param event
     */
    @FXML private void handleAppointmentCopyButtonAction(ActionEvent event) {
        try{
            JDBC.openConnection();

            if (!appointmentTable.getSelectionModel().isEmpty()) {
                Appointments appointmentToCopy = appointmentTable.getSelectionModel().getSelectedItem();
                ObservableList<Customers> allCustomers = CustomersDAO.getAllCustomers();
                ObservableList<Users> allUsers = UsersDAO.getAllUsers();
                ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();
                String customer = "";
                String user = "";
                String contact  = "";

                for (Customers c: allCustomers) {
                    if (appointmentToCopy.getAppointmentCustomerID() == c.getCustomerId()) {
                        customer  = c.getCustomerName();
                    }
                }

                for (Users u: allUsers) {
                    if (appointmentToCopy.getAppointmentUserID() == u.getUserId()) {
                        user = u.getUserName();
                    }
                }

                for (Contacts c: allContacts) {
                    if (appointmentToCopy.getAppointmentContactID() == c.getContactID()) {
                        contact = c.getContactName();
                    }
                }

                appointmentIDField.setText(String.valueOf(appointmentToCopy.getAppointmentID()));
                appointmentTitleField.setText(appointmentToCopy.getAppointmentTitle());
                appointmentDescriptionField.setText(appointmentToCopy.getAppointmentDescription());
                appointmentLocationField.setText(appointmentToCopy.getAppointmentLocation());
                appointmentTypeField.setText(appointmentToCopy.getAppointmentType());
                appointmentStartDate.setValue(appointmentToCopy.getAppointmentStart().toLocalDate());
                appointmentStartCombo.setValue(String.valueOf(appointmentToCopy.getAppointmentStart().toLocalTime()));
                appointmentEndDate.setValue(appointmentToCopy.getAppointmentEnd().toLocalDate());
                appointmentEndCombo.setValue(String.valueOf(appointmentToCopy.getAppointmentEnd().toLocalTime()));
                appointmentCustomerCombo.setValue(customer);
                appointmentUserCombo.setValue(user);
                appointmentContactCombo.setValue(contact);
                appointmentCancelLabel.setText("");
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must select an entry from the table to copy.", ButtonType.OK);
                alert.show();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Handles the actions of the delete appointment button, which include:
     * - If an appointment is selected:
     * - - Ask if user would like to delete the appointment.
     * - - If user says yes, the selected appointment is deleted from the SQL server, and relevant tables are refreshed.
     * - If no appointment is selected:
     * - - An error message stating the user must select an appointment from the table appears.
     * @param event
     * @throws Exception
     */
    @FXML private void handleAppointmentDeleteButtonAction(ActionEvent event) throws Exception{
        if (!appointmentTable.getSelectionModel().isEmpty()) {
            try {
                Connection connection = JDBC.openConnection();
                int selectedAppointmentID = appointmentTable.getSelectionModel().getSelectedItem().getAppointmentID();
                String selectedAppointmentType = appointmentTable.getSelectionModel().getSelectedItem().getAppointmentType();

                Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you would like to delete appointment #" + selectedAppointmentID + ", with type \"" + selectedAppointmentType + "\"?", ButtonType.OK);
                Optional<ButtonType> confirm = alert.showAndWait();
                if (confirm.isPresent() && confirm.get() == ButtonType.OK) {
                    AppointmentsDAO.deleteAppointment(selectedAppointmentID, connection);

                    if (viewMode == 1){
                        ObservableList<Appointments> appointmentsRefresh = AppointmentsDAO.getAllAppointments();
                        appointmentTable.setItems(appointmentsRefresh);
                    }
                    if (viewMode == 2) {
                        ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> weekAppointments = FXCollections.observableArrayList();

                        LocalDateTime weekStart = LocalDateTime.now();
                        LocalDateTime weekEnd = LocalDateTime.now().plusWeeks(1);

                        // LAMBDA #2 DITTO
                        if (!allAppointments.isEmpty()) {
                            allAppointments.forEach(a -> {
                                if (a.getAppointmentEnd().isAfter(weekStart) && a.getAppointmentEnd().isBefore(weekEnd)) {
                                    weekAppointments.add(a);
                                }
                                appointmentTable.setItems(weekAppointments);
                            });
                        }
                    }
                    if (viewMode == 3) {
                        ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> monthAppointments = FXCollections.observableArrayList();

                        LocalDateTime monthStart = LocalDateTime.now();
                        LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1);

                        // LAMBDA #3 DITTO
                        if (!allAppointments.isEmpty()) {
                            allAppointments.forEach(a -> {
                                if (a.getAppointmentEnd().isAfter(monthStart) && a.getAppointmentEnd().isBefore(monthEnd)) {
                                    monthAppointments.add(a);
                                }
                                appointmentTable.setItems(monthAppointments);
                            });
                        }
                    }

                    if (!contactReportCombo.getSelectionModel().isEmpty()) {
                        int contactID = 0 ;
                        String contactName = contactReportCombo.getSelectionModel().getSelectedItem();

                        ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();
                        ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                        ObservableList<Appointments> contactAppointments = FXCollections.observableArrayList();

                        for (Contacts c: allContacts) {
                            if (contactName.equals(c.getContactName())) {
                                contactID = c.getContactID();
                            }
                        }

                        for (Appointments a: allAppointments) {
                            if (a.getAppointmentContactID() == contactID) {
                                contactAppointments.add(a);
                            }
                        }
                        contactReportTable.setItems(contactAppointments);
                    }

                    appointmentCancelLabel.setText("Appointment #" + selectedAppointmentID + ", with type \"" + selectedAppointmentType + "\" cancelled.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You must select an entry from the table to delete.");
            alert.show();
        }
    }

    /**
     * Exits the program when the exit button is pressed. How exciting.
     * @param event
     */
    @FXML private void handleAppointmentExitButtonAction(ActionEvent event) {Platform.exit();}

    /**
     * Updates the appointments table to show all appointments if any of the other two modes are selected.
     * @param event
     */
    @FXML private void handleAppointmentAllRadioAction(ActionEvent event) {
        viewMode = 1;
        try {
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            appointmentTable.setItems(allAppointments);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the appointments table to only show appointments up to a week from now.
     * @param event
     */
    @FXML private void handleAppointmentWeekRadioAction(ActionEvent event) {
        viewMode = 2;
        try {
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            ObservableList<Appointments> weekAppointments = FXCollections.observableArrayList();

            LocalDateTime weekStart = LocalDateTime.now();
            LocalDateTime weekEnd = LocalDateTime.now().plusWeeks(1);

            // LAMBDA #2 DITTO
            if (!allAppointments.isEmpty()) {
                allAppointments.forEach(a -> {
                    if (a.getAppointmentEnd().isAfter(weekStart) && a.getAppointmentEnd().isBefore(weekEnd)) {
                        weekAppointments.add(a);
                    }
                    appointmentTable.setItems(weekAppointments);
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the appointments table to only show appointments up to a month from now.
     * @param event
     */
    @FXML private void handleAppointmentMonthRadioAction(ActionEvent event) {
        viewMode = 3;
        try {
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            ObservableList<Appointments> monthAppointments = FXCollections.observableArrayList();

            LocalDateTime monthStart = LocalDateTime.now();
            LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1);

            // LAMBDA #3 DITTO
            if (!allAppointments.isEmpty()) {
                allAppointments.forEach(a -> {
                    if (a.getAppointmentEnd().isAfter(monthStart) && a.getAppointmentEnd().isBefore(monthEnd)) {
                        monthAppointments.add(a);
                    }
                    appointmentTable.setItems(monthAppointments);
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Detects if fields are missing/incorrectly formatted in the appointments field, and if so, adds to a list.
     * @param newAppointmentID
     * @return invalidFields
     */
    private List<String> appointmentValidateFields(int newAppointmentID) {
        List<String> invalidFields = new ArrayList();

        if (this.appointmentTitleField.getText().isEmpty()) {
            invalidFields.add("Title is blank");
        }

        if (this.appointmentTitleField.getText().length() > 50) {
            invalidFields.add("Title too long");
        }

        if (this.appointmentDescriptionField.getText().isEmpty()) {
            invalidFields.add("Description is blank");
        }

        if (this.appointmentDescriptionField.getText().length() > 50) {
            invalidFields.add("Description too long");
        }

        if (this.appointmentLocationField.getText().isEmpty()) {
            invalidFields.add("Location is blank");
        }

        if (this.appointmentLocationField.getText().length() > 50) {
            invalidFields.add("Location too long");
        }

        if (this.appointmentTypeField.getText().isEmpty()) {
            invalidFields.add("Type is blank");
        }

        if (this.appointmentTypeField.getText().length() > 50) {
            invalidFields.add("Type too long");
        }

        if (this.appointmentStartDate.getValue() == null) {
            invalidFields.add("Start Date is blank");
        }
        if (this.appointmentStartCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("Start Time is blank");
        }

        if (this.appointmentEndDate.getValue() == null) {
            invalidFields.add("End Date is blank");
        }

        if (this.appointmentEndCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("End Time is blank");
        }

        if (this.appointmentStartDate.getValue() != null && this.appointmentEndDate.getValue() != null) {
            if (!this.appointmentStartDate.getValue().isEqual(this.appointmentEndDate.getValue())) {
                invalidFields.add("Appointment cannot last multiple days.");
            }
            if (this.appointmentStartDate.getValue().isAfter(this.appointmentEndDate.getValue())) {
                invalidFields.add("Start date after end date");
            }
        }

        if (!this.appointmentStartCombo.getSelectionModel().isEmpty() && !this.appointmentEndCombo.getSelectionModel().isEmpty()) {
            if (LocalTime.parse(this.appointmentStartCombo.getSelectionModel().getSelectedItem()).isAfter(LocalTime.parse(this.appointmentEndCombo.getSelectionModel().getSelectedItem()))) {
                invalidFields.add("Start time after end time");
            }
            if (LocalTime.parse(this.appointmentStartCombo.getSelectionModel().getSelectedItem()).equals(LocalTime.parse(this.appointmentEndCombo.getSelectionModel().getSelectedItem()))) {
                invalidFields.add("Start time equals end time");
            }
        }

        if (this.appointmentStartDate.getValue() != null && this.appointmentEndDate.getValue() != null && !this.appointmentStartCombo.getSelectionModel().isEmpty() && !this.appointmentEndCombo.getSelectionModel().isEmpty() && !appointmentCustomerCombo.getSelectionModel().isEmpty()) {
            try {
                ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
                ObservableList<Customers> allCustomers = CustomersDAO.getAllCustomers();
                LocalDate startLocalDate = appointmentStartDate.getValue();
                LocalDate endLocalDate = appointmentEndDate.getValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime startLocalTime = LocalTime.parse(appointmentStartCombo.getValue(), formatter);
                LocalTime endLocalTime = LocalTime.parse(appointmentEndCombo.getValue(), formatter);
                LocalDateTime startDateTime = LocalDateTime.of(startLocalDate, startLocalTime);
                LocalDateTime endDateTime = LocalDateTime.of(endLocalDate, endLocalTime);
                ZonedDateTime startZonedDT = ZonedDateTime.of(startDateTime, ZoneId.systemDefault());
                ZonedDateTime endZonedDT = ZonedDateTime.of(endDateTime, ZoneId.systemDefault());
                ZonedDateTime startEST = startZonedDT.withZoneSameInstant(ZoneId.of("America/New_York"));
                ZonedDateTime endEST = endZonedDT.withZoneSameInstant(ZoneId.of("America/New_York"));
                DayOfWeek startCheckDay = startEST.toLocalDate().getDayOfWeek();
                DayOfWeek endCheckDay = endEST.toLocalDate().getDayOfWeek();
                int startCheckDayInt = startCheckDay.getValue();
                int endCheckDayInt = endCheckDay.getValue();

                if (startCheckDayInt < DayOfWeek.MONDAY.getValue() || startCheckDayInt > DayOfWeek.FRIDAY.getValue() || endCheckDayInt < DayOfWeek.MONDAY.getValue() || endCheckDayInt > DayOfWeek.FRIDAY.getValue()) {
                    invalidFields.add("Cannot schedule outside of business days (Monday-Friday)");
                    System.out.println("Weekday check activated.");
                }

                for (Appointments a: allAppointments) {
                    LocalDateTime startCheck = a.getAppointmentStart();
                    LocalDateTime endCheck = a.getAppointmentEnd();
                    int customerIDCheck = -1;

                    for (Customers c: allCustomers) {
                        if (c.getCustomerName().equals(appointmentCustomerCombo.getValue())) {
                            customerIDCheck = c.getCustomerId();
                        }
                    }

                    if ((customerIDCheck == a.getAppointmentCustomerID()) && (newAppointmentID != a.getAppointmentID()) && (startDateTime.isBefore(startCheck)) && (endDateTime.isAfter(endCheck))) {
                        invalidFields.add("Overlapping appointments");
                    }

                    if ((customerIDCheck == a.getAppointmentCustomerID()) && (newAppointmentID != a.getAppointmentID()) && (startDateTime.isAfter(startCheck)) && (startDateTime.isBefore(endCheck))) {
                        invalidFields.add("Start time overlaps with another appointment");
                    }

                    if ((customerIDCheck == a.getAppointmentCustomerID()) && (newAppointmentID != a.getAppointmentID()) && (endDateTime.isAfter(startCheck)) && (endDateTime.isBefore(endCheck))) {
                        invalidFields.add("End time overlaps with another appointment");
                    }

                    if ((customerIDCheck == a.getAppointmentCustomerID()) && (newAppointmentID != a.getAppointmentID()) && (startDateTime.isEqual(startCheck))) {
                        invalidFields.add("Start time is equal to another appointment's start time");
                    }

                    if ((customerIDCheck == a.getAppointmentCustomerID()) && (newAppointmentID != a.getAppointmentID()) && (endDateTime.isEqual(endCheck))) {
                        invalidFields.add("End time is equal to another appointment's end time");
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        if (this.appointmentCustomerCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("Customer is blank");
        }
        if (this.appointmentContactCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("Contact is blank");
        }
        if (this.appointmentUserCombo.getSelectionModel().isEmpty()) {
            invalidFields.add("User is blank");
        }
        return invalidFields;
    }

    //Reports tab

    @FXML
    private Tab reportsTab;
    @FXML
    private TableView<Appointments> contactReportTable;
    @FXML
    private TableColumn contactReportAppointmentIDColumn;
    @FXML
    private TableColumn contactReportTitleColumn;
    @FXML
    private TableColumn contactReportDescriptionColumn;
    @FXML
    private TableColumn contactReportLocationColumn;
    @FXML
    private TableColumn contactReportContactColumn;
    @FXML
    private TableColumn contactReportTypeColumn;
    @FXML
    private TableColumn contactReportStartColumn;
    @FXML
    private TableColumn contactReportEndColumn;
    @FXML
    private TableColumn contactReportCustomerIDColumn;
    @FXML
    private TableColumn contactReportUserIDColumn;
    @FXML
    private ComboBox<String> contactReportCombo;
    @FXML
    private TableView typeReportTable;
    @FXML
    private TableColumn typeReportTypeColumn;
    @FXML
    private TableColumn typeReportNumberColumn;
    @FXML
    private TableView monthReportTable;
    @FXML
    private TableColumn monthReportMonthColumn;
    @FXML
    private TableColumn monthReportNumberColumn;
    @FXML
    private TableView countryReportTable;
    @FXML
    private TableColumn countryReportCountryColumn;
    @FXML
    private TableColumn countryReportNumberColumn;

    /**
     * Refreshes all tables in the reports tab with up-to-date information when the updates tab is clicked. This function only exists to fix a bug where if I were to update these tables using my previous
     * method, the tables wouldn't refresh at all.
     * @throws Exception
     */
    @FXML public void handleReportsTabAction() throws Exception {
        try {
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            if (!contactReportCombo.getSelectionModel().isEmpty()) {
                int contactID = 0 ;
                String contactName = contactReportCombo.getSelectionModel().getSelectedItem();

                ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();
                ObservableList<Appointments> contactAppointments = FXCollections.observableArrayList();

                for (Contacts c: allContacts) {
                    if (contactName.equals(c.getContactName())) {
                        contactID = c.getContactID();
                    }
                }

                for (Appointments a: allAppointments) {
                    if (a.getAppointmentContactID() == contactID) {
                        contactAppointments.add(a);
                    }
                }
                contactReportTable.setItems(contactAppointments);
            }

            ObservableList<String> types = FXCollections.observableArrayList();
            ObservableList<String> uniqueTypes = FXCollections.observableArrayList();
            ObservableList<ReportsByType> reportsByType = FXCollections.observableArrayList();
            ObservableList<Month> months = FXCollections.observableArrayList();
            ObservableList<Month> uniqueMonths = FXCollections.observableArrayList();
            ObservableList<ReportsByMonth> reportsByMonth = FXCollections.observableArrayList();
            ObservableList<ReportsByCountry> countries = ReportsDAO.getCountryReport();
            ObservableList<ReportsByCountry> countriesToAdd = FXCollections.observableArrayList();

            // LAMBDA #5 (For loop replacement in order to create a list of unique appointment types.
            allAppointments.forEach(a -> {
               types.add(a.getAppointmentType());
            });

            for (Appointments a: allAppointments) {
                String type = a.getAppointmentType();
                if (!uniqueTypes.contains(a.getAppointmentType())) {
                    uniqueTypes.add(type);
                }
            }

            for (String type: uniqueTypes) {
                String tempType = type;
                int typeTotal = Collections.frequency(types, type);
                ReportsByType appointmentTypes = new ReportsByType(tempType, typeTotal);
                reportsByType.add(appointmentTypes);
            }
            typeReportTable.setItems(reportsByType);

            // LAMBDA #6 (Used to simplify gathering of appointment's months.)
            allAppointments.stream().map(a -> {
                return a.getAppointmentStart().getMonth();
            }).forEach(months::add);

            // LAMBDA #7 (Used to gather the unique months from the data from lambda #6.)
            months.stream().filter(m -> {
                return !uniqueMonths.contains(m);
            }).forEach(uniqueMonths::add);

            for (Month m: uniqueMonths) {
                int monthTotal = Collections.frequency(months, m);
                String monthName = m.name();
                ReportsByMonth appointmentMonths = new ReportsByMonth(monthName, monthTotal);
                reportsByMonth.add(appointmentMonths);
            }
            monthReportTable.setItems(reportsByMonth);

            countries.forEach(countriesToAdd::add);
            countryReportTable.setItems(countriesToAdd);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the contact report table to show only the appointments relating to the selected contact.
     * @throws Exception
     */
    @FXML public void handleContactReportComboAction() throws Exception {
        try {
            int contactID = 0 ;
            String contactName = contactReportCombo.getSelectionModel().getSelectedItem();

            ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            ObservableList<Appointments> contactAppointments = FXCollections.observableArrayList();

            for (Contacts c: allContacts) {
                if (contactName.equals(c.getContactName())) {
                    contactID = c.getContactID();
                }
            }

            for (Appointments a: allAppointments) {
                if (a.getAppointmentContactID() == contactID) {
                    contactAppointments.add(a);
                }
            }
            contactReportTable.setItems(contactAppointments);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initialize function

    /**
     * Initializes multiple aspects of the main view window, including:
     * - Setting up PropertyValueFactories for the tables.
     * - Filling comboboxes (Five of which being filled via Lambdas to avoid using for loops.)
     * @param url
     * @param resourceBundle
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {

            //Variables
            ObservableList<CountriesDAO> allCountries = CountriesDAO.getAllCountries();
            ObservableList<String> countryNames = FXCollections.observableArrayList();
            ObservableList<FirstLevelDivisionsDAO> allFirstLevelDivisions = FirstLevelDivisionsDAO.getAllFirstLevelDivisions();
            ObservableList<String> firstLevelDivisionNames = FXCollections.observableArrayList();
            ObservableList<Customers> allCustomers = CustomersDAO.getAllCustomers();
            ObservableList<String> customerNames = FXCollections.observableArrayList();
            ObservableList<Appointments> allAppointments = AppointmentsDAO.getAllAppointments();
            ObservableList<Contacts> allContacts = ContactsDAO.getAllContacts();
            ObservableList<String> contactNames = FXCollections.observableArrayList();
            ObservableList<Users> allUsers = UsersDAO.getAllUsers();
            ObservableList<String> userNames = FXCollections.observableArrayList();
            ObservableList<String> appointmentTimes = FXCollections.observableArrayList();
            ZonedDateTime openingTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC-5")).withHour(8).withMinute(0).withSecond(0).withNano(0);
            ZonedDateTime closingTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC-5")).withHour(22).withMinute(15).withSecond(0).withNano(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            //Cell Value Factories
            customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
            customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
            customerPostalColumn.setCellValueFactory(new PropertyValueFactory<>("customerPostal"));
            customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
            customerDivisionColumn.setCellValueFactory(new PropertyValueFactory<>("customerDivisionName"));

            appointmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
            appointmentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
            appointmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDescription"));
            appointmentLocationColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentLocation"));
            appointmentContactColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentContactID"));
            appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
            appointmentStartColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentStart"));
            appointmentEndColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentEnd"));
            appointmentCustomerIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCustomerID"));
            appointmentUserIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentUserID"));

            contactReportAppointmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
            contactReportTitleColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
            contactReportDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDescription"));
            contactReportLocationColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentLocation"));
            contactReportContactColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentContact"));
            contactReportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
            contactReportStartColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentStart"));
            contactReportEndColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentEnd"));
            contactReportCustomerIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCustomerID"));
            contactReportUserIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentUserID"));

            typeReportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
            typeReportNumberColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTotal"));

            monthReportMonthColumn.setCellValueFactory(new PropertyValueFactory<>("monthName"));
            monthReportNumberColumn.setCellValueFactory(new PropertyValueFactory<>("monthTotal"));

            countryReportCountryColumn.setCellValueFactory(new PropertyValueFactory<>("countryName"));
            countryReportNumberColumn.setCellValueFactory(new PropertyValueFactory<>("countryTotal"));

            // Combo Box setups
            // LAMBDA #8
            allCountries.forEach(countries -> countryNames.add(countries.getCountry()));
            customerCountryCombo.setItems(countryNames);

            // LAMBDA #9
            allFirstLevelDivisions.forEach(fld -> firstLevelDivisionNames.add(fld.getDivision()));
            customerDivisionCombo.setItems(allFirstLevelDivisions);

            // LAMBDA #10
            allContacts.forEach(contacts -> contactNames.add(contacts.getContactName()));
            appointmentContactCombo.setItems(contactNames);
            contactReportCombo.setItems(contactNames);

            // LAMBDA #11
            allCustomers.forEach(customers -> customerNames.add(customers.getCustomerName()));
            appointmentCustomerCombo.setItems(customerNames);

            // LAMBDA #12
            allUsers.forEach(users -> userNames.add(users.getUserName()));
            appointmentUserCombo.setItems(userNames);

            if (!openingTime.equals(0) || !closingTime.equals(0)) {
                while (openingTime.isBefore(closingTime)) {
                    appointmentTimes.add(formatter.format(openingTime.withZoneSameInstant(ZoneId.systemDefault())));
                    openingTime = openingTime.plusMinutes(15);
                }
            }
            appointmentStartCombo.setItems(appointmentTimes);
            appointmentEndCombo.setItems(appointmentTimes);

            //Table filling
            customerTable.setItems(allCustomers);

            appointmentTable.setItems(allAppointments);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
