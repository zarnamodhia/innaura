
package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Staff {

    private int staffId;
    private String fullName;
    private String role;
    private String phone;
    private String email;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private boolean active;

    // Constructor for an existing staff member
    public Staff(int staffId, String fullName, String role,
                 String phone, String email, BigDecimal salary,
                 LocalDate joiningDate, boolean active) {

        this.staffId = staffId;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.salary = salary;
        this.joiningDate = joiningDate;
        this.active = active;
    }

    // Constructor for a new staff member
    public Staff(String fullName, String role, String phone,
                 String email, BigDecimal salary,
                 LocalDate joiningDate, boolean active) {

        this(0, fullName, role, phone, email,
             salary, joiningDate, active);
    }

    public int getStaffId() {
        return staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
