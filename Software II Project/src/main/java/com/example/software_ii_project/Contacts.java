package com.example.software_ii_project;
public class Contacts {
    public int contactID;
    public String contactName;
    public String email;

    public Contacts(int contactID, String contactName, String email) {
        this.contactID = contactID;
        this.contactName = contactName;
        this.email = email;
    }

    /**
     * Returns contact's ID.
     * @return contactID
     */
    public int getContactID() {
        return contactID;
    }

    /**
     * Returns a contact's name.
     * @return contactName
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * Returns a contact's email address.
     * @return email
     */
    public String getEmail() {
        return email;
    }
}
