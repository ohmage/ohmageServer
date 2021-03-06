/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.User;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Class to contain all of the functionality for validating class information.
 * 
 * @author John Jenkins
 */
public final class ClassValidators {
	private static final Logger LOGGER = Logger.getLogger(ClassValidators.class);
	
	/**
	 * The maximum length for a class name.
	 */
	public static final int MAX_NAME_LENGTH = 255;
	
	/**
	 * Default constructor. Private so that no one can instantiate it.
	 */
	private ClassValidators() {}
	
	/**
	 * Validates that the 'classId' is either null or whitespace only in which
	 * case null is returned or is a valid class identifier in which case the 
	 * class ID is returned; however, if it is not null, not whitespace only,  
	 * and not a valid class identifier, a ValidationException is thrown.
	 *  
	 * @param classId The class identifier to be validated.
	 * 
	 * @return Returns the class ID if it is not null, not whitespace, and 
	 * 		   valid. If it is null or whitespace only, null is returned.
	 * 
	 * @throws ValidationException Thrown if the class ID is not null, not
	 * 		   whitespace only, and not a valid class ID.
	 */
	public static String validateClassId(final String classId) 
			throws ValidationException {
		
		// If the value is null or whitespace only, return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			return null;
		}
		
		// If the value is a valid URN, meaning that it is a plausible class 
		// ID, return the class ID back to the caller.
		if(StringUtils.isValidUrn(classId.trim())) {
			return classId.trim();
		}
		// If the class ID is not null, not whitespace only, and not a valid
		// URN, set the request as failed and throw a ValidationException to
		// warn the caller.
		else {
			throw new ValidationException(
					ErrorCode.CLASS_INVALID_ID, 
					"The class identifier is invalid: " + classId);
		}
	}
	
	/**
	 * Validates that all of the class identifiers in a list String are valid 
	 * class identifiers. If the class identifier list String is null or 
	 * whitespace only, it will return null. If not, it will attempt to parse
	 * the String and evaluate each of the class identifiers in the list. If 
	 * any are invalid, it will return an error message stating which one in
	 * the list was invalid.
	 *  
	 * @param classIdListString The class list as a String where each item is
	 * 							separated by
	 * 						  	{@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.
	 * 
	 * @return Returns a List of class identifiers from the 'classListString'
	 * 		   or null if the class ID list String is null, whitespace only, or
	 * 		   would otherwise be empty because it doesn't contain any 
	 * 		   meaningful data.
	 * 
	 * @throws ValidationException Thrown if the class Id list String contains a
	 * 							  class ID that is an invalid class ID.
	 */
	public static Set<String> validateClassIdList(
			final String classIdListString) throws ValidationException {
		
		LOGGER.info("Validating the list of classes.");
		
		// If the class list is an empty string, then we return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(classIdListString)) {
			return null;
		}
		
		// Create the list of class IDs to be returned to the caller.
		Set<String> classIdList = new HashSet<String>();
		
		// Otherwise, attempt to parse the class list and evaluate each of the
		// class IDs.
		String[] classListArray = classIdListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classListArray.length; i++) {
			// Validate the current class ID.
			String currClassId = validateClassId(classListArray[i].trim());
			
			// If it returned null, then the current class ID in the array
			// was probably whitespace only because the class list had two
			// list item separators in a row.
			if(currClassId != null) {
				classIdList.add(currClassId);
			}
		}
		
		if(classIdList.size() == 0) {
			return null;
		}
		else {
			return classIdList;
		}
	}
	
	/**
	 * Validates that a class name is a valid class name by ensuring that it is
	 * not profane and not too long.
	 * 
	 * @param name The name to validate.
	 * 
	 * @return Returns null if the name is null or whitespace only; otherwise,
	 * 		   the name is returned.
	 * 
	 * @throws ValidationException Thrown if the name is profane or too long.
	 */
	public static String validateName(final String name) 
			throws ValidationException {
		
		LOGGER.info("Validating a class name.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			return null;
		}
		
		if(StringUtils.isProfane(name.trim())) {
			throw new ValidationException(
					ErrorCode.CLASS_INVALID_NAME, 
					"The class name contains profanity: " + name);
		}
		else if(! StringUtils.lengthWithinLimits(name.trim(), 0, MAX_NAME_LENGTH)) {
			throw new ValidationException(
					ErrorCode.CLASS_INVALID_NAME, 
					"The class name is too long. The maximum length of the class name is " + 
						MAX_NAME_LENGTH + 
						" characters");
		}
		else {
			return name.trim();
		}
	}
	
	/**
	 * Tokenizes the name search string into search tokens. No real validation
	 * is performed as the search tokens are allowed to be anything.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return The set of search tokens or null if the string was null or only
	 * 		   whitespace.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static Set<String> validateNameSearch(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that a class description is a valid class description by 
	 * ensuring that it doesn't contain profanity.
	 * 
	 * @param description The description to be validated.
	 * 
	 * @return Returns null if the description is null or whitespace only;
	 * 		   otherwise, it returns the description.
	 * 
	 * @throws ValidationException Thrown if the description contains 
	 * 							   profanity.
	 */
	public static String validateDescription(final String description) 
			throws ValidationException {
		
		LOGGER.info("Validating a class description.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(description)) {
			return null;
		}
		
		if(StringUtils.isProfane(description.trim())) {
			throw new ValidationException(
					ErrorCode.CLASS_INVALID_DESCRIPTION, 
					"The class description contains profanity: " + 
						description);
		}
		else {
			return description.trim();
		}
	}
	
	/**
	 * Tokenizes the description search string into search tokens. No real 
	 * validation is performed as the search tokens are allowed to be anything.
	 * 
	 * @param value The value to validate.
	 * 
	 * @return The set of search tokens or null if the string was null or only
	 * 		   whitespace.
	 * 
	 * @throws ValidationException Never thrown.
	 */
	public static Set<String> validateDescriptionSearch(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		return StringUtils.decodeSearchString(value);
	}
	
	/**
	 * Validates that the given class role exists. If it is null or whitespace
	 * only, null is returned. If it is not a valid role, a ValidationException
	 * is thrown. If it is a valid role, it is returned.
	 * 
	 * @param role The class role to validate.
	 * 
	 * @return Returns null if the class role is null or whitespace only; 
	 * 		   otherwise, the class role is returned.
	 * 
	 * @throws ValidationException Thrown if the class role is not a valid class
	 * 							  role.
	 */
	public static Clazz.Role validateClassRole(final String role) 
			throws ValidationException {
		
		LOGGER.info("Validating a class role.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			return null;
		}
		
		try {
			return Clazz.Role.getValue(role.trim());
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.CLASS_INVALID_ROLE, 
					"Unkown class role: " + role, 
					e);
		}
	}
	
	/**
	 * Validates that a byte array is a valid class roster. If it is null or 
	 * has no length, null is returned. Otherwise, a Map of class IDs to Maps
	 * of usernames to class roles is returned. If the roster is not a valid 
	 * roster a ValidationException is thrown and the request is failed with 
	 * the error code, {@value ErrorCode.CLASS_INVALID_ROSTER}.
	 * 
	 * @param roster The class roster as a byte array. This should a series of
	 * 				 newline-deliminated rows where each row is a comma-
	 * 				 separated list of class ID, username, and the user's role
	 * 				 in the class. Excel for any OS, and many other Microsoft
	 * 				 products, deliminate lines with a carriage return instead 
	 * 				 of a newline; this is taken care of. 
	 * 
	 * @return Returns null if the roster is null or has a length of zero;
	 * 		   otherwise, it returns a Map of class IDs to Maps of usernames to
	 * 		   class roles.
	 * 
	 * @throws ValidationException Thrown if the roster is not a valid roster.
	 */
	public static Map<String, Map<String, Clazz.Role>> validateClassRoster(
			final byte[] roster) throws ValidationException {
		
		LOGGER.info("Validating a class roster.");
		
		if((roster == null) || (roster.length == 0)) {
			return null;
		}
		
		String rosterString = new String(roster);
		
		// Excel (and most of Microsoft) saves newlines as carriage returns 
		// instead of newlines, so we substitute those here as we only deal 
		// with newlines.
		rosterString = rosterString.replace('\r', '\n');
		
		Map<String, Map<String, Clazz.Role>> result = new HashMap<String, Map<String, Clazz.Role>>();
		
		String[] rosterLines = rosterString.split("\n");
		for(int i = 0; i < rosterLines.length; i++) {
			if(StringUtils.isEmptyOrWhitespaceOnly(rosterLines[i])) {
				continue;
			}
			
			String[] rosterLine = rosterLines[i].split(",");
			
			if(rosterLine.length != 3) {
				throw new ValidationException(
						ErrorCode.CLASS_INVALID_ROSTER, 
						"The following line is malformed in the class roster: " + 
							rosterLines[i]);
			}
			
			String classId = ClassValidators.validateClassId(rosterLine[0]);
			String username = UserValidators.validateUsername(rosterLine[1]);
			Clazz.Role classRole = ClassValidators.validateClassRole(rosterLine[2]);
			
			Map<String, Clazz.Role> userRoleMap = result.get(classId);
			if(userRoleMap == null) {
				userRoleMap = new HashMap<String, Clazz.Role>();
				result.put(classId, userRoleMap);
			}
			
			Clazz.Role originalRole = userRoleMap.put(username, classRole);
			// Add the role but keep track of whether or not a role already 
			// existed for this user in this class. It is an error only if the
			// two roles do not match.
			if((originalRole != null) && (! originalRole.equals(classRole))) {
				throw new ValidationException(
						ErrorCode.CLASS_INVALID_ROSTER, 
						"Two different roles were found for the same user in the same class. The user was '" + 
							username + 
							"' and the class was '" + 
							classId + "'. The first role was '" + 
							originalRole + 
							"' and the second role was '" + 
							classRole + 
							"'");
			}
		}
		
		return result;
	}
	
	/**
	 * Validates that a "with user list" value is a valid boolean and returns 
	 * it. If it is null or whitespace-only, the default, true, is returned.
	 * 
	 * @param value The boolean value to validate.
	 * 
	 * @return A boolean generated from this value.
	 * 
	 * @throws ValidationException The value could not be decoded.
	 */
	public static boolean validateWithUserListValue(
			final String value)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return true;
		}
		
		Boolean result = StringUtils.decodeBoolean(value);
		if(result == null) {
			throw new ValidationException(
					ErrorCode.CLASS_INVALID_WITH_USER_LIST_VALUE,
					"The \"with user list\" value is not a valid boolean: " +
						value);
		}
		
		return result;
	}

	/**
	 * Validates that the number of classes to skip is a non-negative number.
	 * 
	 * @param value The value to validate.
	 *  
	 * @return The validated number of classes to skip.
	 * 
	 * @throws ValidationException There was a problem decoding the number or 
	 * 							   it was an invalid number.
	 */
	public static int validateNumToSkip(
			final String value) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return 0;
		}
		
		try {
			int numToSkip = Integer.decode(value);
			
			if(numToSkip < 0) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
						"The number of classes to skip is negative: " +
							value);
			}
			
			return numToSkip;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
					"The number of classes to skip is not a number: " +
							value);
		}
	}
	
	/**
	 * Validates that a number of classes to return is a non-negative number
	 * less than or equal to the maximum allowed number of classes to return.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return A number between 0 and {@link Clazz#MAX_NUM_TO_RETURN}.
	 * 
	 * @throws ValidationException The number was not valid.
	 */
	public static int validateNumToReturn(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating that a number of users to return is valid.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return Clazz.MAX_NUM_TO_RETURN;
		}
		
		try {
			int numToSkip = Integer.decode(value);
			
			if(numToSkip < 0) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
						"The number of classes to return cannot be negative: " +
								value);
			}
			else if(numToSkip > User.MAX_NUM_TO_RETURN) {
				throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
						"The number of classes to return is greater than the max allowed: " +
							Clazz.MAX_NUM_TO_RETURN);
			}
			
			return numToSkip;
		}
		catch(NumberFormatException e) {
			throw new ValidationException(
					ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
					"The number of classes to return is not a number: " +
							value);
		}
	}
}