package healthy.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Customer {
    private String email;
    private List<String> dietaryPreferences;
    private List<String> allergies;

    public Customer(String email) {
        this.email = email;
        this.dietaryPreferences = new ArrayList<>();
        this.allergies = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public List<String> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void addDietaryPreference(String preference) {
        if (preference != null && !preference.trim().isEmpty() && !this.dietaryPreferences.contains(preference.trim())) {
            this.dietaryPreferences.add(preference.trim());
        }
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void addAllergy(String allergy) {
        if (allergy != null && !allergy.trim().isEmpty() && !this.allergies.contains(allergy.trim())) {
            this.allergies.add(allergy.trim());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "email='" + email + '\'' +
                ", dietaryPreferences=" + dietaryPreferences +
                ", allergies=" + allergies +
                '}';
    }
}