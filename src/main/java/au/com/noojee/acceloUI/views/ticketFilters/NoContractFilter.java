package au.com.noojee.acceloUI.views.ticketFilters;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.com.noojee.acceloapi.dao.CompanyDao;
import au.com.noojee.acceloapi.dao.ContractDao;
import au.com.noojee.acceloapi.dao.TicketDao;
import au.com.noojee.acceloapi.entities.Company;
import au.com.noojee.acceloapi.entities.Contract;
import au.com.noojee.acceloapi.entities.Ticket;
import au.com.noojee.acceloapi.entities.meta.Company_;
import au.com.noojee.acceloapi.entities.meta.Ticket_;
import au.com.noojee.acceloapi.filter.AcceloFilter;
import au.com.noojee.acceloapi.filter.Expression;

public class NoContractFilter extends TicketFilter
{

	@Override
	public String getName()
	{
		return "No Contracts";
	}

	public List<Ticket> getTickets(boolean refresh)
	{
		List<Ticket> unassignedTickets = new ArrayList<>();
		
		// Get a list of companies that have a contract.
		ContractDao daoContract = new ContractDao();
		List<Contract> contracts  = daoContract.getAll();
		
		CompanyDao daoCompany = new CompanyDao();
		AcceloFilter<Company> companyFilter = new AcceloFilter<>();
		boolean firstpass = true; 
		for (Contract contract : contracts)
		{
			Expression exp = companyFilter.eq(Company_.id, contract.getCompanyId());
			if (firstpass)
				companyFilter.where(exp);
			
			companyFilter.or(exp);
			firstpass = false;
		}
		List<Integer> companiesWithContracts = daoCompany.getByFilter(companyFilter).stream().map(company -> company.getId()).collect(Collectors.toList());
		// contracts.parallelStream().map(contract -> daoCompany.getCompanyByContactId(contract.getId()).getId()).collect(Collectors.toList());
		

		AcceloFilter<Ticket> filter = new AcceloFilter<>();
		if (refresh)
			filter.refreshCache();
		filter.limit(1);
		
		int maxTickets = 10;

		int page = 0;
		while (unassignedTickets.size() < maxTickets)
		{
			filter.offset(page++);
			
			// Find all tickets without a contract.
			// assigned.
			filter.where(
					filter.eq(Ticket_.contract, 0).and(filter.after(Ticket_.date_started, LocalDate.of(2017, 03, 01))));
			List<Ticket>list = new TicketDao().getByFilter(filter);
			
			if (list.isEmpty())
				break; // no more tickets.
			
			// only interested in companies that have a contract.
			Stream<Ticket> stream = list.stream().filter(ticket -> companiesWithContracts.contains(ticket.getCompanyId()));
			
			unassignedTickets.addAll(stream.collect(Collectors.toList()));
		}

		return unassignedTickets.subList(0, Math.min(maxTickets, unassignedTickets.size()));
	}

	@Override
	public String buildURL(Ticket ticket)
	{
		return  "?action=edit_support_issue&id=" + ticket.getId();
	}

}
