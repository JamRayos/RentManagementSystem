package rentalmanagementsystem.rent_management;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Tenant {
    private int tenantId;
    private String name;
    private String username;
    private String email;
    private String contact;
    private int unitId;
    private String password;
    private boolean archived;
    private int leaseAgreementId;

    public Tenant(int tenantId, String name, String username, String email, String contact, int unitId, String password, boolean archived, int leaseAgreementId){
        this.tenantId = tenantId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.contact = contact;
        this.unitId = unitId;
        this.password = password;
        this.archived = archived;
        this.leaseAgreementId = leaseAgreementId;
    }
    public Tenant(int tenantId, String name, String username, String email, String contact, int unitId, String password, boolean archived){
        this.tenantId = tenantId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.contact = contact;
        this.unitId = unitId;
        this.password = password;
        this.archived = archived;
    }

    public Tenant(int tenantId, String name, String username, String email, String contact, int unitId) {
        this.tenantId = tenantId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.contact = contact;
        this.unitId = unitId;
    }

//    getters
    public int getTenantId() {return tenantId;}
    public String getName() {return name;}
    public String getUsername() {return username;}
    public String getEmail() {return email;}
    public String getContact() {return contact;}
    public int getUnitId() {return unitId;}
    public String getPassword() {return password;}
    public boolean getArchived() {return archived;}
    public int getLeaseAgreementId() {return leaseAgreementId;}

//    setters
    public void setTenantId(int tenantId) {this.tenantId = tenantId;}
    public void setName(String name) {this.name = name;}
    public void setUsername(String username) {this.username = username;}
    public void setEmail(String email) {this.email = email;}
    public void setContact(String contact) {this.contact = contact;}
    public void setUnitId(int unitId) {this.unitId = unitId;}
    public void setPassword(String password) {this.password = password;}
    public void setArchived(boolean archived) {this.archived = archived;}
    public void setLeaseAgreementId(int leaseAgreementId) {this.leaseAgreementId = leaseAgreementId;}
}
