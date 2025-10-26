package com.sever0x.asi.flair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuranceEntities {

	@JsonProperty("contract_number")
	private List<String> contractNumbers = new ArrayList<>();

	@JsonProperty("customer_number")
	private List<String> customerNumbers = new ArrayList<>();

	@JsonProperty("company_name")
	private List<String> companyNames = new ArrayList<>();

	@JsonProperty("person_name")
	private List<String> personNames = new ArrayList<>();

	private List<String> ibans = new ArrayList<>();

	private List<String> dates = new ArrayList<>();

	private List<String> emails = new ArrayList<>();

	public List<String> getContractNumbers() {
		return contractNumbers;
	}

	public void setContractNumbers(List<String> contractNumbers) {
		this.contractNumbers = contractNumbers;
	}

	public List<String> getCustomerNumbers() {
		return customerNumbers;
	}

	public void setCustomerNumbers(List<String> customerNumbers) {
		this.customerNumbers = customerNumbers;
	}

	public List<String> getCompanyNames() {
		return companyNames;
	}

	public void setCompanyNames(List<String> companyNames) {
		this.companyNames = companyNames;
	}

	public List<String> getPersonNames() {
		return personNames;
	}

	public void setPersonNames(List<String> personNames) {
		this.personNames = personNames;
	}

	public List<String> getIbans() {
		return ibans;
	}

	public void setIbans(List<String> ibans) {
		this.ibans = ibans;
	}

	public List<String> getDates() {
		return dates;
	}

	public void setDates(List<String> dates) {
		this.dates = dates;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}
}