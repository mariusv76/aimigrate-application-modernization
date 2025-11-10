package org.pwte.example.api.dto;

/**
 * Simple DTO for BusinessCustomer to avoid JPA entity serialization issues.
 */
public class BusinessCustomerDTO {
    private int customerId;
    private String name;
    private String username;
    private String description;
    private boolean volumeDiscount;
    private boolean businessPartner;

    public BusinessCustomerDTO() {
    }

    public BusinessCustomerDTO(int customerId, String name, String username, 
                               String description, boolean volumeDiscount, boolean businessPartner) {
        this.customerId = customerId;
        this.name = name;
        this.username = username;
        this.description = description;
        this.volumeDiscount = volumeDiscount;
        this.businessPartner = businessPartner;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVolumeDiscount() {
        return volumeDiscount;
    }

    public void setVolumeDiscount(boolean volumeDiscount) {
        this.volumeDiscount = volumeDiscount;
    }

    public boolean isBusinessPartner() {
        return businessPartner;
    }

    public void setBusinessPartner(boolean businessPartner) {
        this.businessPartner = businessPartner;
    }
}
