package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Creates a new CampaignCreationAwRequest object for handling the rest of the
 * request.
 * 
 * @author John Jenkins
 */
public class CampaignCreationAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(CampaignCreationAwRequestCreator.class);

	/**
	 * Default constructor.
	 */
	public CampaignCreationAwRequestCreator() {
		// Does nothing.
	}
	
	/**
	 * Gets the CampaignCreationAwRequest object out of the request where it
	 * was stashed during HTTP validation to prevent us from parsing the XML
	 * twice.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating new AwRequest object for creating a new campaign.");

		CampaignCreationAwRequest awRequest;
		try {
			awRequest = (CampaignCreationAwRequest) request.getAttribute("awRequest");
		}
		catch(ClassCastException e) {
			throw new IllegalStateException("Invalid awRequest object in HTTPServlet. Must be CampaignCreationAwRequest.");
		}
		if(awRequest == null) {
			throw new IllegalStateException("Missing awRequest in HTTPServlet - Did the HTTPValidator run?");
		}
		
		return awRequest;
	}

}
