package org.ohmage.auth.provider;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;
import org.ohmage.domain.user.ProviderUserInformation;

/**
 * <p>
 * A OAuth client authentication provider that can transform a validated token
 * into a unique identifier for a user and supply additional information.
 * </p>
 *
 * @author John Jenkins
 */
public interface Provider {
	/**
	 * <p>
	 * An exception for when a provider's token is invalid.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static final class InvalidTokenException extends OhmageException {
		/**
		 * A unique version number for this class for serialization purposes.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception for when a provider's token is invalid.
		 * 
		 * @param reason
		 *        The reason the token was invalid.
		 */
		public InvalidTokenException(final String reason) {
			super(reason);
		}
	}
	
	/**
	 * Returns the unique identifier for this provider.
	 * 
	 * @return The unique identifier for this provider.
	 */
	public String getId();
	
	/**
	 * Retrieves the user's information from this provider.
	 * 
	 * @param accessToken
	 *        An access token provided to the user when they authorized ohmage.
	 * 
	 * @return The user's information as generated by the provider.
	 * 
	 * @throws IllegalArgumentException
	 *         The access token was null.
	 * 
	 * @throws IllegalStateException
	 *         An error occurred while communicating with the provider.
	 * 
	 * @throws InvalidArgumentException
	 *         The provider reported the token as invalid.
	 */
	public ProviderUserInformation getUserInformation(
		final String accessToken)
		throws
			IllegalArgumentException,
			IllegalStateException,
			InvalidArgumentException;
}