package de.jsf.model;

/**
 * @author Thomas Freese
 */
public class User {
    private final String abteilung = "Department";
    private final String nachname = "LastName";
    private final String umsUserId = "UserId";
    private final String vorname = "FirstName";

    public String getAbteilung() {
        return abteilung;
    }

    public String getNachname() {
        return nachname;
    }

    public String getUmsUserId() {
        return umsUserId;
    }

    public String getVorname() {
        return vorname;
    }
}
