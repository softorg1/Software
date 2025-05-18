package healthy.com;

public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer registerOrGetCustomer(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("Customer email cannot be null or empty for registration/retrieval.");
            return null;
        }
        Customer customer = customerRepository.findCustomerByEmail(email);
        if (customer == null) {
            customer = new Customer(email);
            customerRepository.saveCustomer(customer);
        }
        return customer;
    }

    public void addDietaryPreference(String email, String preference) {
        if (email == null || preference == null || preference.trim().isEmpty()) {
            System.err.println("Email or preference cannot be null or empty.");
            return;
        }
        Customer customer = customerRepository.findCustomerByEmail(email);
        if (customer != null) {
            customer.addDietaryPreference(preference);
            customerRepository.saveCustomer(customer);
        } else {
            System.err.println("Customer not found for adding preference: " + email);
        }
    }

    public void addAllergy(String email, String allergy) {
        if (email == null || allergy == null || allergy.trim().isEmpty()) {
            System.err.println("Email or allergy cannot be null or empty.");
            return;
        }
        Customer customer = customerRepository.findCustomerByEmail(email);
        if (customer != null) {
            customer.addAllergy(allergy);
            customerRepository.saveCustomer(customer);
        } else {
            System.err.println("Customer not found for adding allergy: " + email);
        }
    }

    public Customer getCustomerDietaryInfo(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("Email cannot be null or empty for fetching dietary info.");
            return null;
        }
        return customerRepository.findCustomerByEmail(email);
    }
}