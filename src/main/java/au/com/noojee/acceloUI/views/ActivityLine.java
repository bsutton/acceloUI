package au.com.noojee.acceloUI.views;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.money.Money;

import au.com.noojee.acceloapi.AcceloException;
import au.com.noojee.acceloapi.dao.StaffDao;
import au.com.noojee.acceloapi.entities.Activity;
import au.com.noojee.acceloapi.entities.Activity.Standing;
import au.com.noojee.acceloapi.entities.Staff;

public class ActivityLine
{

	static Logger logger = LogManager.getLogger();


	private Activity activity;

	public ActivityLine(Activity activity)
	{
		this.activity = activity;
	}

	public String getSubject()
	{
		return this.activity.getSubject();
	}
	
	public String getDetails()
	{
		return this.activity.getDetails();
	}

	
	public Standing getStanding()
	{
		return this.activity.getStanding();
	}

	
	String getAssignee()
	{
		int staffId = activity.getStaff();

		String staffName = "Error";
		try
		{
			Staff staff = new StaffDao().getById(staffId);
			staffName = (staff == null ? "" : staff.getFullName());
		}
		catch (AcceloException e)
		{
			logger.error(e, e);
		}
		return staffName;
	}
	
	public LocalDateTime getDateTimeStarted()
	{
		return this.activity.getDateTimeStarted();
	}

	public LocalDateTime getDateTimeCreated()
	{
		return this.activity.getDateTimeCreated();
	}

	public Duration getBillable()
	{
		return this.activity.getBillable();
	}

	
	public Duration getNonBillable()
	{
		return this.activity.getNonBillable();
	}

	public  Money getRateCharged()
	{
		return this.activity.getRateCharged();
	}
	
	public boolean isApproved()
	{
		return this.activity.isApproved();
	}

	public String getBody()
	{
		return this.activity.getBody();
	}

	public Activity getActivity()
	{
		return this.activity;
	}

	public void setActivity(Activity newActivity)
	{
		this.activity = newActivity;
	}

}
