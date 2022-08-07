package net.dollmar.web.apicatalogue.filters;

import spark.FilterImpl;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

import static spark.Spark.halt;

import java.util.Base64;

import net.dollmar.web.apicatalogue.data.Users;

public class BasicAuthenticationFilter extends FilterImpl
{
	private static final String AUTH_HEADER = "Authorization";
	
	private static final String BASIC_AUTHENTICATION_TYPE = "Basic";

	private static final int NUMBER_OF_AUTHENTICATION_FIELDS = 2;

	private static final String ACCEPT_ALL_TYPES = "*";


	public BasicAuthenticationFilter()
	{
		this(SparkUtils.ALL_PATHS);
	}

	public BasicAuthenticationFilter(final String path)
	{
		super(path, ACCEPT_ALL_TYPES);
	}

	@Override
	public void handle(final Request request, final Response response)
	{
		String authHeaderValue = request.headers(AUTH_HEADER);
		final String encodedHeader = (authHeaderValue == null)
				? null
				: ((authHeaderValue.startsWith(BASIC_AUTHENTICATION_TYPE))
						?  authHeaderValue.substring(BASIC_AUTHENTICATION_TYPE.length()).trim()
						: null);

		if (notAuthenticatedWith(credentialsFrom(encodedHeader))) {
			response.header("WWW-Authenticate", BASIC_AUTHENTICATION_TYPE);
			halt(401);
		}
	}

	private String[] credentialsFrom(final String encodedHeader)
	{
		return (encodedHeader != null)
				? decodeHeader(encodedHeader).split(":")
				: null;
	}

	private String decodeHeader(final String encodedHeader)
	{
		String cred =  new String(Base64.getDecoder().decode(encodedHeader.trim()));
		return cred;
	}

	private boolean notAuthenticatedWith(final String[] credentials)
	{
		return !authenticatedWith(credentials);
	}

	private boolean authenticatedWith(final String[] credentials)
	{
		return (credentials != null && credentials.length == NUMBER_OF_AUTHENTICATION_FIELDS) 
			? new Users().authenticateUser(credentials[0], credentials[1])
			: false;		
	}


	public static void main(String[] args) {
		String es = "YWRtaW46UGE1NXcwcmQ=";
		System.out.println(String.format("%s --> %s", es, new BasicAuthenticationFilter().decodeHeader(es)));
	}
}
