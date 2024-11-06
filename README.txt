The NightOwl Scheduler was created for the purpose of keeping track of customers and
appointments in a consulting environment, as well as offering simple reporting features.

How to use the provided software:
1.) Log-in with username and password given by administrator.
2.) Create a customer by filling out the provided fields in the customer tab and pressing
the add button. 
3.) You can edit customers by selecting the customer in the table, editing the fields 
again, and pressing the edit button.
3a.) Editing a customer can be made even easier by using the Copy Data button, which
automatically transfers data from a customer in the table to the appropriate fields
in the form.
4.) To delete a customer, simply select the customer on the table and press delete. (NOTE:
This will also delete any related appointments.)
5.) Create an appointment by going to the appointments tab, filling out the provided form,
then pressing the add button. (Note, be sure not to make an appointment's start time after
its end time or overlap with another appointment's time.)
6.) Similarly to customers, you can edit an appointment by selecting it from the table,
filling out the form again, and pressing update.
6a.) This can once again be made easier by pressing the Copy Data button, which copies
the information from an appointment in the table to the appropriate fields in the form.
7.) You can delete appointments by selecting one from the appointments table and pressing
the delete button.
8.) You can also change the view mode of the appointments table to show all appointments,
appointments up to a week from now, and appointments up to a month from now.
9.) In order to use the reports menu, simply select the reports tab, and go to the report
you'd like to view. In order to use the contact report, select a contact from the
dropdown.

NOTES:
- If you delete a customer or appointment, there is no way to recover it.
- Times in tables/menus related to appointments are automatically converted to the
system's time zone from UTC in the database.
- In addition to the contact, type, and month reports required, there is now also a
country report, which automatically shows how many appointments each country currently has.

SOFTWARE USED:
IntelliJ IDEA 2023.2.2 (Community Edition)
Java SE 17.0.1
JavaFX Runtime 17.0.1
MySQL Connector 8.0.30