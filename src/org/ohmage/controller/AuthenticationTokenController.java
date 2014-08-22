package org.ohmage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import org.ohmage.auth.provider.Provider;
import org.ohmage.auth.provider.ProviderRegistry;
import org.ohmage.bin.AuthorizationTokenBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.HttpStatusCodeExceptionResponder;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;
import org.ohmage.domain.user.ProviderUserInformation;
import org.ohmage.domain.user.User;
import org.ohmage.javax.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all requests for authentication tokens.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(AuthenticationTokenController.ROOT_MAPPING)
public class AuthenticationTokenController extends OhmageController {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/auth_token";

	/**
	 * The parameter key for the user's email address.
	 */
	public static final String PARAMETER_EMAIL = "email";
	/**
	 * The parameter key for passwords.
	 */
	public static final String PARAMETER_PASSWORD = "password";
	/**
	 * The parameter key for ohmage refresh tokens.
	 */
	public static final String PARAMETER_REFRESH_TOKEN = "refresh_token";
	/**
	 * The parameter key for provider identifiers.
	 */
	public static final String PARAMETER_PROVIDER = "provider";
	/**
	 * The parameter key for provider access tokens.
	 */
	public static final String PARAMETER_ACCESS_TOKEN = "access_token";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(AuthenticationTokenController.class.getName());

	/**
	 * <p>
	 * An exception when a user attempts to create
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class AccountNotSetupException
		extends OhmageException
		implements HttpStatusCodeExceptionResponder {

		/**
		 * The default serial version used for serializing an instance of this
		 * class.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception with only a reason.
		 *
		 * @param reason The reason this exception was thrown.
		 */
		public AccountNotSetupException(final String reason) {
			super(reason);
		}

		/**
		 * Creates a new exception with a reason and an underlying cause.
		 *
		 * @param reason The reason this exception was thrown.
		 *
		 * @param cause The underlying exception that caused this exception.
		 */
		public AccountNotSetupException(
			final String reason,
			final Throwable cause) {

			super(reason, cause);
		}

		/**
		 * @return {@link HttpServletResponse#SC_CONFLICT}
		 */
		@Override
		public int getStatusCode() {
			return HttpServletResponse.SC_CONFLICT;
		}
	}

	/**
	 * Creates a new authentication token for a user using their ohmage
	 * credentials.
	 *
	 * @param email
	 *        The user's email address.
	 *
	 * @param password
	 *        The user's password.
	 *
	 * @return A new authentication token for the user.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = { RequestMethod.GET, RequestMethod.POST },
		params = { PARAMETER_EMAIL, PARAMETER_PASSWORD })
	public static @ResponseBody AuthorizationToken getTokenFromOhmageAccount(
		@RequestParam(value = PARAMETER_EMAIL, required = true)
			final String email,
		@RequestParam(value = PARAMETER_PASSWORD, required = true)
			final String password) {

		LOGGER.info("Creating an authentication token from an email address and " +
					"password.");

		// Create a universal response to make it less obvious as to why we are
		// rejecting the request.
		final String errorResponse = "Unknown user or incorrect password.";

		LOGGER.info("Retrieving the user based on the email address: " + email);
		User user = UserBin.getInstance().getUserFromEmail(email);
		if(user == null) {
			LOGGER.info("The user is unknown: " + email);
			throw new AuthenticationException(errorResponse);
		}

		LOGGER.info("Validating the user's password.");
		if(user.getPassword() == null) {
			LOGGER.info("The user's account does not have a password.");
			throw
				new AuthenticationException(
					"The account uses a provider's access token for " +
						"authentication.");
		}
		else if(
		    (user.getRegistration() != null) &&
		    (user.getRegistration().getActivationTimestamp() == null)) {

		    LOGGER.info("The account was never activated.");
		    throw
		        new AccountNotSetupException(
		            "The account has not yet been activated.");
		}
		else if(! user.verifyPassword(password)) {
			LOGGER.info("The given password is incorrect.");
			throw new AuthenticationException(errorResponse);
		}

		LOGGER.info("Creating a new authentication token.");
		AuthorizationToken token = new AuthorizationToken(user);

		LOGGER.info("Adding the authentication token to the bin.");
		AuthorizationTokenBin.getInstance().addToken(token);

		LOGGER.info("Returning the token to the user.");
		return token;
	}

	/**
	 * Creates a new authentication token for a user by validating a provider's
	 * access token and looking up the existing, associated ohmage account.
	 *
	 * @param providerId
	 *        The provider's internal identifier.
	 *
	 * @param accessToken
	 *        The access token the provider generated after the user
	 *        authenticated themselves.
	 *
	 * @return A new authentication token for the user.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = { RequestMethod.GET, RequestMethod.POST },
		params = { PARAMETER_PROVIDER, PARAMETER_ACCESS_TOKEN })
	public static @ResponseBody AuthorizationToken getTokenFromProvider(
		@RequestParam(value = PARAMETER_PROVIDER, required = true)
			final String providerId,
		@RequestParam(value = PARAMETER_ACCESS_TOKEN, required = true)
			final String accessToken) {

		LOGGER.info("Creating an authentication token from a provider's access " +
					"token.");

		LOGGER.debug("Retrieving the implementation for this provider.");
		Provider provider = ProviderRegistry.get(providerId);

		LOGGER.info("Retrieving the user based on the access token.");
		ProviderUserInformation newUserInformation =
			provider.getUserInformation(accessToken);

		LOGGER.info("Retrieving the ohmage account linked with the " +
					"provider-given ID.");
		User user =
			UserBin
				.getInstance()
				.getUserFromProvider(
					newUserInformation.getProviderId(),
					newUserInformation.getUserId());
		if(user == null) {
			LOGGER.info("The user has not yet linked this provider's " +
						"information to their ohmage account.");
			throw
				new AccountNotSetupException(
					"The user has not yet created an ohmage account.");
		}

		LOGGER.debug("Pulling out the saved information from the provider.");
		ProviderUserInformation savedUserInformation =
			user.getProvider(newUserInformation.getProviderId());

		LOGGER.debug("Determining if the user's information has been updated.");

		if(! newUserInformation.equals(savedUserInformation)) {
			LOGGER.info("Updating the user's information.");
			user = user.updateProvider(newUserInformation);

			LOGGER.info("Updating the user with the new information from the " +
						"provider.");

            // TODO if this fails then what?

			UserBin.getInstance().updateUser(user);
		}

		LOGGER.info("Creating a new authentication token.");
		AuthorizationToken token = new AuthorizationToken(user);

		LOGGER.info("Adding the authentication token to the bin.");
		AuthorizationTokenBin.getInstance().addToken(token);

		LOGGER.info("Returning the token to the user.");
		return token;
	}

	/**
	 * Creates a new authentication token for a user using the ohmage refresh
	 * they were given when they last authenticated.
	 *
	 * @param refreshToken
	 *        The refresh token they were given from their last access token or
	 *        refresh token request.
	 *
	 * @return A new authentication token for the user.
	 */
	@RequestMapping(
		value = { "", "/" },
		method = { RequestMethod.GET, RequestMethod.POST },
		params = { PARAMETER_REFRESH_TOKEN })
	public static @ResponseBody AuthorizationToken refreshToken(
		@RequestParam(value = PARAMETER_REFRESH_TOKEN, required = true)
			final String refreshToken) {

		LOGGER.info("Creating an authentication token from a refresh token.");

		LOGGER.info("Retrieving the authentication token based on the refresh " +
					"token.");

		AuthorizationToken oldToken =
			AuthorizationTokenBin
				.getInstance()
				.getTokenFromRefreshToken(refreshToken);

		LOGGER.debug("Ensuring that the refresh token is valid.");
		if(oldToken == null) {
			throw
				new AuthenticationException(
					"The given refresh token is unknown.");
		}

		LOGGER.info("Checking if this token was granted via OAuth.");
		if(oldToken.getAuthorizationCode() != null) {
		    throw
		        new InvalidArgumentException(
		            "This API may not be used to refresh OAuth-based tokens.");
		}

		LOGGER.trace("Checking if the token was invalidated.");
		if(oldToken.wasInvalidated()) {
			throw
				new AuthenticationException(
					"This token has been invalidated.");
		}

		LOGGER.trace("Checking if the token was refreshed.");
		AuthorizationToken token;
		if(oldToken.wasRefreshed()) {
		    LOGGER.info("The token has already been refreshed.");

		    LOGGER.info("Retrieving the next token in the chain.");
		    token =
		        AuthorizationTokenBin
		            .getInstance()
		            .getTokenFromAccessToken(oldToken.getNextToken());

		    LOGGER.debug("Verifying the next token was retrieved.");
		    if(token == null) {
		        throw
		            new IllegalStateException(
		                "A token '" +
		                    oldToken.getAccessToken() +
		                    "' has a next token value of '" +
		                    oldToken.getNextToken() +
		                    "' which is unknown.");
		    }

		    LOGGER.info("Checking if the next token had already been refreshed.");
		    if(token.wasRefreshed()) {
    			throw
    				new AuthenticationException(
    					"This token has already been refreshed.");
		    }
		}
		else {
    		LOGGER.info("Creating a new authentication token.");
    		token = new AuthorizationToken(oldToken);

    		LOGGER.info("Adding the authentication token to the bin.");
    		AuthorizationTokenBin.getInstance().addToken(token);

            LOGGER.info("Invalidating the old token.");
            AuthorizationToken invalidatedOldToken =
                (new AuthorizationToken.Builder(oldToken))
                    .setNextToken(token.getAccessToken())
                    .build();

            LOGGER.info("Updating the invalidated old token.");
            AuthorizationTokenBin
                .getInstance()
                .updateToken(invalidatedOldToken);
		}

		LOGGER.info("Returning the token to the user.");
		return token;
	}

	/**
	 * Invalidates an authentication token. This would most likely be used on
	 * logout.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
	 *
	 * @throws AuthenticationException
	 *         The authentication was not given.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.DELETE)
	public static @ResponseBody void invalidateAuthToken(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken)
		throws IllegalArgumentException {

	    LOGGER.info("Creating a request to invalidate a token.");

        LOGGER.info("Verifying that auth information was given.");
        if(authToken == null) {
            throw
                new AuthenticationException("No auth information was given.");
        }

        LOGGER.info("Invalidating the token.");
        AuthorizationToken updatedToken =
            (new AuthorizationToken.Builder(authToken))
                .setValid(false)
                .build();

		LOGGER.info("Updating the token.");
		AuthorizationTokenBin.getInstance().updateToken(updatedToken);
	}
}