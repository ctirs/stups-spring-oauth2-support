package org.zalando.stups.oauth2.spring.server;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author jbellmann
 *
 */
public class LeakTokenTest {

	private static final String TOKEN = "123456789";
	private static final String URL = "http://172.34.23.12/tokeninfo";
	private static final String JUST_FOR_TESTING = "JUST FOR TESTING";
	private static final String MESSAGE_STARTSWITH = "I/O error on ";

	@Test
	public void testSocketTimeoutException() {

		ResourceAccessException targetException = null;

		RestTemplate restTemplate = new TestInternalRestTemplate(new HttpComponentsClientHttpRequestFactory());
		try {
			restTemplate.exchange(TokenInfoResourceServerTokenServices.buildRequestEntity(URI.create(URL), TOKEN), Map.class);
		} catch (ResourceAccessException e) {
			targetException = e;
		}

		// WE EXPECT NOT TO SEE ANYTHING FROM THE TOKEN
		Assertions.assertThat(targetException.getMessage()).startsWith(MESSAGE_STARTSWITH);
		Assertions.assertThat(targetException.getMessage()).doesNotContain(TOKEN);
		Assertions.assertThat(targetException.getCause().getMessage()).startsWith(JUST_FOR_TESTING);
		Assertions.assertThat(targetException.getCause().getMessage()).doesNotContain(TOKEN);
	}

	//PROVIDE AN REQUEST THAT THROWS AN IOEXCEPTION
	static class TestInternalRestTemplate extends RestTemplate {

		TestInternalRestTemplate(ClientHttpRequestFactory requestFactory) {
			super(requestFactory);
		}

		@Override
		protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
			ClientHttpRequest request = Mockito.mock(ClientHttpRequest.class);
			Mockito.when(request.execute()).thenThrow(new IOException(JUST_FOR_TESTING));
			Mockito.when(request.getHeaders()).thenReturn(new HttpHeaders());
			return request;
		}

	}

}
