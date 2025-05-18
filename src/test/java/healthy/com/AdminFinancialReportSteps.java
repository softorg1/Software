package healthy.com;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminFinancialReportSteps {

    private static class FinancialOrderRecord {
        String orderId;
        String customerEmail;
        LocalDate orderDate;
        double orderTotal;

        public FinancialOrderRecord(String orderId, String customerEmail, String orderDateStr, double orderTotal) {
            this.orderId = orderId;
            this.customerEmail = customerEmail;
            this.orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            this.orderTotal = orderTotal;
        }
    }

    private List<FinancialOrderRecord> allPaidOrders;
    private String currentAdminUsername;
    private boolean adminLoggedIn;
    private double reportedRevenue;
    private String reportPeriodMessage;

    public AdminFinancialReportSteps() {
        this.allPaidOrders = new ArrayList<>();
        this.adminLoggedIn = false;
    }

    @Given("the system has the following completed and paid orders with their totals:")
    public void the_system_has_the_following_completed_and_paid_orders(DataTable ordersTable) {
        this.allPaidOrders.clear();
        List<Map<String, String>> rows = ordersTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            this.allPaidOrders.add(new FinancialOrderRecord(
                    columns.get("Order ID"),
                    columns.get("Customer Email"),
                    columns.get("Order Date"),
                    Double.parseDouble(columns.get("Order Total"))
            ));
        }
    }

    @When("{string} requests a total revenue report for the month of {string}")
    public void requests_a_total_revenue_report_for_the_month_of(String adminUsername, String monthYearStr) {
        this.currentAdminUsername = adminUsername;
        this.adminLoggedIn = true;
        assertThat(this.adminLoggedIn).isTrue();
        assertThat(this.currentAdminUsername).isEqualTo(adminUsername);
        this.reportPeriodMessage = null;

        YearMonth targetMonthYear = YearMonth.parse(monthYearStr, DateTimeFormatter.ofPattern("MMMM yyyy"));

        this.reportedRevenue = this.allPaidOrders.stream()
                .filter(order -> YearMonth.from(order.orderDate).equals(targetMonthYear))
                .mapToDouble(order -> order.orderTotal)
                .sum();

        if (this.reportedRevenue == 0.0) {
            boolean monthHasOrders = this.allPaidOrders.stream()
                    .anyMatch(order -> YearMonth.from(order.orderDate).equals(targetMonthYear));
            if(!monthHasOrders) {
                this.reportPeriodMessage = "No sales were recorded for " + monthYearStr;
            }
        }
    }

    @Then("the generated financial report should show a total revenue of {double} for {string}")
    public void the_generated_financial_report_should_show_a_total_revenue_of_for(Double expectedRevenue, String monthYearStr) {
        assertThat(this.reportedRevenue).isEqualTo(expectedRevenue);
    }

    @And("the system should indicate that no sales were recorded for {string}")
    public void the_system_should_indicate_that_no_sales_were_recorded_for(String monthYearStr) {
        assertThat(this.reportPeriodMessage).isEqualTo("No sales were recorded for " + monthYearStr);
    }

    @When("{string} requests an overall total revenue report")
    public void requests_an_overall_total_revenue_report(String adminUsername) {
        this.currentAdminUsername = adminUsername;
        this.adminLoggedIn = true;
        assertThat(this.adminLoggedIn).isTrue();
        assertThat(this.currentAdminUsername).isEqualTo(adminUsername);
        this.reportPeriodMessage = null;

        this.reportedRevenue = this.allPaidOrders.stream()
                .mapToDouble(order -> order.orderTotal)
                .sum();
    }

    @Then("the generated financial report should show an overall total revenue of {double}")
    public void the_generated_financial_report_should_show_an_overall_total_revenue_of(Double expectedOverallRevenue) {
        assertThat(this.reportedRevenue).isEqualTo(expectedOverallRevenue);
    }
}