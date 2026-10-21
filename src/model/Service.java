package model;

import java.math.BigDecimal;

public class Service {

    private int serviceId;
    private String serviceName;
    private String description;
    private String category;
    private BigDecimal price;
    private boolean available;

    public Service(int serviceId, String serviceName,
                   String description, String category,
                   BigDecimal price, boolean available) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.category = category;
        this.price = price;
        this.available = available;
    }

    public Service(String serviceName, String description,
                   String category, BigDecimal price,
                   boolean available) {
        this(0, serviceName, description, category,
             price, available);
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
