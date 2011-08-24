package org.ohmage.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.dao.CampaignDocumentDaos;
import org.ohmage.dao.ClassDocumentDaos;
import org.ohmage.dao.DocumentDaos;
import org.ohmage.dao.UserDocumentDaos;
import org.ohmage.domain.DocumentInformation;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for gathering and writing information about 
 * user-document relationships.
 * 
 * @author John Jenkins
 */
public class UserDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserDocumentServices() {}
	
	/**
	 * Retrieves the ID for all documents directly associated with the user.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @return A list of document IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getDocumentsSpecificToUser(Request request, String username) throws ServiceException {
		try {
			return UserDocumentDaos.getVisibleDocumentsSpecificToUser(username);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can read a document.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user that is being checked that they
	 * 				   can read this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot read this document or
	 * 							if there is an error.
	 */
	public static void userCanReadDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To read a document, it simply has to be visible to the user in
			// some capacity.
			if(roles.size() == 0) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user does not have sufficient permissions to read the document.");
				throw new ServiceException("The user does not have sufficient permissions to read the document.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can modify a document.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user that is being checkec that they
	 * 				   can modify this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot modify this document
	 * 							or if there is an error.
	 */
	public static void userCanModifyDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To modify a document, the user must be a writer or owner or a
			// supervisor in any of the campaigns to which the document is 
			// associated or privileged in any of the classes to which the 
			// document is associated.
			if((! roles.contains(DocumentRoleCache.ROLE_OWNER)) && 
			   (! roles.contains(DocumentRoleCache.ROLE_WRITER)) &&
			   (! UserCampaignDocumentServices.getUserIsSupervisorInAnyCampaignAssociatedWithDocument(request, username, documentId)) &&
			   (! UserClassDocumentServices.getUserIsPrivilegedInAnyClassAssociatedWithDocument(request, username, documentId))) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user does not have sufficient permissions to modify the document.");
				throw new ServiceException("The user does not have sufficient permissions to modify the document.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can delete a document.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user that is being checked that they
	 * 				   can delete this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot modify this document
	 * 							or if there is an error.
	 */
	public static void userCanDeleteDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To modify a document, the user must be a writer or owner or a
			// supervisor in any of the campaigns to which the document is 
			// associated or privileged in any of the classes to which the 
			// document is associated.
			if((! roles.contains(DocumentRoleCache.ROLE_OWNER)) &&
			   (! UserCampaignDocumentServices.getUserIsSupervisorInAnyCampaignAssociatedWithDocument(request, username, documentId)) &&
			   (! UserClassDocumentServices.getUserIsPrivilegedInAnyClassAssociatedWithDocument(request, username, documentId))) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user does not have sufficient permissions to delete the document.");
				throw new ServiceException("The user does not have sufficient permissions to delete the document.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns the highest role for a user for a document or null if the user 
	 * is not associated with the document. This is across all possible 
	 * relationships, campaign, class, and direct.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user whose highest role with the 
	 * 				   document is desired.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return Returns the users highest role for the document across all 
	 * 		   campaigns, classes, and direct associations. If the user is not
	 * 		   associated with the document in any capacity or the document
	 * 		   doesn't exist, null is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static String getHighestDocumentRoleForUserForDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			if(roles.contains(DocumentRoleCache.ROLE_OWNER)) {
				return DocumentRoleCache.ROLE_OWNER;
			}
			else if(roles.contains(DocumentRoleCache.ROLE_WRITER)) {
				return DocumentRoleCache.ROLE_WRITER;
			}
			else if(roles.contains(DocumentRoleCache.ROLE_READER)) {
				return DocumentRoleCache.ROLE_READER;
			}
			else {
				return null;
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the information about a document and also populates the role
	 * of a specific user, all of the campaigns, and all of the classes.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The user's username.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return A DocumentInformation object that contains all of the 
	 * 		   information about a single doucment as well as a user's specific
	 * 		   role, all of the campaigns associated with the document and 
	 * 		   their role, and all of the classes associated with the document
	 * 		   and their role.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static DocumentInformation getDocumentInformationForDocumentWithUser(Request request, String username, String documentId) throws ServiceException {
		try {
			// Get the document's basic information.
			DocumentInformation result = DocumentDaos.getDocumentInformation(documentId);
			
			// Get the user's specific role.
			String userRole = UserDocumentDaos.getDocumentRoleForDocumentSpecificToUser(username, documentId);
			if(! StringUtils.isEmptyOrWhitespaceOnly(userRole)) {
				result.addUserRole(userRole);
			}
			
			// For all of the campaigns associated with the document, get their
			// role.
			for(String campaignId : CampaignDocumentDaos.getCampaignsAssociatedWithDocument(documentId)) {
				String campaignRole = CampaignDocumentDaos.getCampaignsDocumentRole(campaignId, documentId);
				if(! StringUtils.isEmptyOrWhitespaceOnly(campaignRole)) {
					result.addCampaignRole(campaignId, campaignRole);
				}
			}
			
			// For all of the classes associated with the document, get their
			// role.
			for(String classId : ClassDocumentDaos.getClassesAssociatedWithDocument(documentId)) {
				String classRole = ClassDocumentDaos.getClassDocumentRole(classId, documentId);
				if(! StringUtils.isEmptyOrWhitespaceOnly(classRole)) {
					result.addClassRole(classId, classRole);
				}
			}
			
			return result;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Creates a list of DocumentInformation objects, one for each document,
	 * populating each with the user's document role, all of the campaigns
	 * associated with the document and their role, and all of the classes 
	 * associated with the document and their role.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The user's username.
	 * 
	 * @param documentIds The document's unique identifier.
	 * 
	 * @return A list of DocumentInformation objects each representing one of
	 * 		   the document IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 * 
	 * @see #getDocumentInformationForDocumentWithUser(Request, String, String)
	 */
	public static List<DocumentInformation> getDocumentInformationForDocumentsWithUser(Request request, String username, Collection<String> documentIds) throws ServiceException {
		List<DocumentInformation> result = new LinkedList<DocumentInformation>();
		for(String documentId : documentIds) {
			result.add(getDocumentInformationForDocumentWithUser(request, username, documentId));
		}
		return result;
	}
}