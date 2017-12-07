package au.com.noojee.acceloUI.views.ticketFilters;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import au.com.noojee.acceloapi.dao.ActivityDao;
import au.com.noojee.acceloapi.dao.TicketDao;
import au.com.noojee.acceloapi.entities.Activity;
import au.com.noojee.acceloapi.entities.Priority;
import au.com.noojee.acceloapi.entities.Ticket;
import au.com.noojee.acceloapi.entities.meta.Ticket_;
import au.com.noojee.acceloapi.entities.meta.fieldTypes.OrderByField.Order;
import au.com.noojee.acceloapi.filter.AcceloFilter;

public class BillingAdjustmentRequired extends TicketFilter
{

	@Override
	public String getName()
	{
		return "Bill adjustment Required";
	}

	@Override
	public List<Ticket> getTickets(LocalDate cutoffDate, boolean refresh)
	{
		// get all unapproved tickets
		// LocalDate lastMonth = now.minusMonths(1).withDayOfMonth(1);

		List<Ticket> unapproved = new ArrayList<>();

		AcceloFilter<Ticket> filter = new AcceloFilter<>();
		if (refresh) filter.refreshCache();
		filter.limit(1);

		int offset = 0;

		// while (unapproved.size() < 200)
		while(true)
		{

			filter.offset(offset);

			// All closed tickets
//			filter.where(filter.eq(Ticket_.standing, Ticket.Standing.closed)
//					.and(filter.after(Ticket_.date_started, cutoffDate)))
//					.orderBy(Ticket_.id, Order.DESC);

//			filter.where(filter.eq(Ticket_.priority, Priority.NoojeePriority.Critical))
//			.and(filter.after(Ticket_.date_started, cutoffDate)))
//			.orderBy(Ticket_.id, Order.DESC);

			
			TicketDao daoTicket = new TicketDao();

			List<Ticket> closedTickets = daoTicket.getByFilter(filter);

			 // no more closed tickets so we are done here.
			if (closedTickets.isEmpty())
				break;

			// Find tickets that need a billing adjustment.
			unapproved.addAll(closedTickets.parallelStream().filter(ticket -> billAdjustmentRequired(ticket))
					.collect(Collectors.toList()));

			offset += 1;
		}

		return unapproved;
	}
	
	private boolean billAdjustmentRequired(Ticket ticket)
	{
		List<Activity> activities = new ActivityDao().getByTicket(ticket, true);
		
		// calculate the current total billable.
		Duration totalBillable = activities.stream().map(Activity::getBillable).reduce(Duration.ZERO,
				(lhs, rhs) -> lhs.plus(rhs));

		long minutes = totalBillable.toMinutes();
		long rounded = minutes;

		// We only round up if they are more than 5 minutes into the next block.
		long excess = minutes % 15;
		if (minutes < 15 || excess > 5)
			rounded = (long) (Math.ceil(minutes / 15.0f) * 15);

		return (rounded != minutes);
	}

	@Override
	public String buildURL(Ticket ticket)
	{
		return "?action=approve_object&object_id=" + ticket.getId() + "&object_table=issue";
	}

}
