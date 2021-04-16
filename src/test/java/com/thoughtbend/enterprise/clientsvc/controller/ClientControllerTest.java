package com.thoughtbend.enterprise.clientsvc.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtbend.enterprise.clientsvc.entity.ClientDocument;
import com.thoughtbend.enterprise.clientsvc.event.ClientEventPublisher;
import com.thoughtbend.enterprise.clientsvc.event.OutboundNewClientEvent;
import com.thoughtbend.enterprise.clientsvc.repository.ClientRepository;

@WebMvcTest(controllers = ClientController.class)
public class ClientControllerTest {

	static class Field {
		final static String NAME = "name";
		final static String CONTACT_PHONE = "contactNumber";
		final static String CLIENT_EXEC_ID = "clientExecutiveId";
	}
	
	static class TestData {
		final static String ID = UUID.randomUUID().toString();
		final static String DOC_ID = UUID.randomUUID().toString();
		final static String NAME = "Test Client Name";
		final static String PHONE_VALID = "3125551212";
		final static String PHONE_INVALID_1 = "125551212";
		final static String CLIENT_EXEC_ID = UUID.randomUUID().toString();
	}

	// @formatter:off
	private static String AUTH_URL = "https://thoughtbend.us.auth0.com/oauth/token";
	private static String TOKEN = null;
	// @formatter:on

	private static final String CLIENT_ROOT_PATH = "/v1/client";
	private static ObjectMapper MAPPER = new ObjectMapper();
	
	@Value("${client-id}")
	private String clientId;
	
	@Value("${client-secret}")
	private String clientSecret;
	
	@Value("${auth0.audience}")
	private String audience;

	@Autowired
	private MockMvc mvc;

	@MockBean(name = "clientRepository")
	private ClientRepository mockClientRepo;
	
	@MockBean(name = "clientEventPublisher")
	private ClientEventPublisher mockClientEventPublisher;

	@BeforeEach
	public void beforeTest() throws Exception {
		
		if (TOKEN == null) {
			RestTemplate rest = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			JSONObject credObject = new JSONObject();
			credObject.put("client_id", "D8J0kRf7RmLzGCdLlW7CQpkWVHH6g3xT");
			credObject.put("client_secret", "vBxtD3yx_nVOffKy-dP403UN1KQzQArEltO5M0hb0lHOonAUwHAmaTybbytBDyuW");
			credObject.put("audience", "https://client-service.thoughtbend.com/");
			credObject.put("grant_type", "client_credentials");
	
			HttpEntity<String> request = new HttpEntity<>(credObject.toString(), headers);
	
			final String tokenAsJsonStr = rest.postForObject(AUTH_URL, request, String.class, headers);
			final JsonNode rootNode = MAPPER.readTree(tokenAsJsonStr);
			final String accessToken = rootNode.findValue("access_token").asText();
			TOKEN = accessToken;
	
			System.out.println(TOKEN);
		}
	}

	@Test
	public void test_getClients_success() throws Exception {

		final ClientDocument docFixture = new ClientDocument();
		docFixture.setDocId(TestData.DOC_ID);
		docFixture.setName(TestData.NAME);
		docFixture.setClientExecutiveId(TestData.CLIENT_EXEC_ID);

		final List<ClientDocument> docListFixture = new ArrayList<>();
		docListFixture.add(docFixture);

		when(this.mockClientRepo.findAll()).thenReturn(docListFixture);

		final ResultActions result = mvc.perform(get(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		verify(this.mockClientRepo, times(1)).findAll();

		result.andExpect(status().isOk()).andExpect(jsonPath("$.length()", is(1)))
				.andExpect(jsonPath("$[0].id", is(docFixture.getDocId())))
				.andExpect(jsonPath("$[0].name", is(docFixture.getName())))
				.andExpect(jsonPath("$[0].clientExecutiveId", is(docFixture.getClientExecutiveId())));
	}

	@Test
	public void test_getClients_noResult() throws Exception {
		
		// We simply want to verify that we return an empty result instead of an error
		// under this scenario,
		// it may be valid to have zero clients returned when none exist
		final List<ClientDocument> docListFixture = new ArrayList<>();

		when(this.mockClientRepo.findAll()).thenReturn(docListFixture);

		final ResultActions result = mvc.perform(get(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON));

		verify(this.mockClientRepo, times(1)).findAll();

		// We should return 200, not an error
		result.andExpect(status().isOk()).andExpect(jsonPath("$.length()", is(0)));
	}

	@Test
	public void test_createClient_success() throws Exception {

		JSONObject clientFixture = new JSONObject();
		clientFixture.put(Field.NAME, TestData.NAME);
		clientFixture.put(Field.CLIENT_EXEC_ID, TestData.CLIENT_EXEC_ID);
		clientFixture.put(Field.CONTACT_PHONE, TestData.PHONE_VALID);

		final ResultActions result = mvc.perform(
				post(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN).accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON).content(clientFixture.toString()));

		final ArgumentCaptor<ClientDocument> clientCaptor = ArgumentCaptor.forClass(ClientDocument.class);
		verify(this.mockClientRepo).save(clientCaptor.capture());

		final ClientDocument capturedClient = clientCaptor.getValue();
		assertNotNull(capturedClient, "capturedClient should not be null");
		assertNotNull(capturedClient.getDocId(), "capturedClient docId should not be null");
		
		final ArgumentCaptor<OutboundNewClientEvent> eventCaptor = ArgumentCaptor.forClass(OutboundNewClientEvent.class);
		verify(this.mockClientEventPublisher).publish(eventCaptor.capture());
		
		final OutboundNewClientEvent capturedEvent = eventCaptor.getValue();
		assertNotNull(capturedEvent, "capturedEvent should not be null");

		// @formatter:off
		result.andExpect(status().isCreated())
			.andExpect(header().string("Location", is(CLIENT_ROOT_PATH + "/" + capturedClient.getDocId())))
			.andExpect(jsonPath("$.id", is(capturedClient.getDocId())))
			.andExpect(jsonPath("$.name", is(TestData.NAME)))
			.andExpect(jsonPath("$.clientExecutiveId", is(TestData.CLIENT_EXEC_ID)))
			.andExpect(jsonPath("$.contactNumber", is(TestData.PHONE_VALID)));
		// @formatter:on
	}
	
	@Test
	public void test_createClient_contactNumberValidadtionError() throws Exception {

		// 1. Setup
		JSONObject clientFixture = new JSONObject();
		clientFixture.put("name", TestData.NAME);
		clientFixture.put("clientExecutiveId", TestData.CLIENT_EXEC_ID);
		clientFixture.put("contactNumber", TestData.PHONE_INVALID_1);

		// 2. Execute
		final ResultActions result = mvc.perform(
				post(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN).accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON).content(clientFixture.toString()));

		// 3. Validate
		verifyNoInteractions(this.mockClientRepo, this.mockClientEventPublisher);

		// @formatter:off
		result.andExpect(status().isBadRequest());
		// @formatter:on
	}
	
	@Test
	public void test_createClient_errorContactNumberMissing() throws Exception {

		JSONObject clientFixture = new JSONObject();
		clientFixture.put("name", TestData.NAME);
		clientFixture.put("clientExecutiveId", TestData.CLIENT_EXEC_ID);

		final ResultActions result = mvc.perform(
				post(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN).accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON).content(clientFixture.toString()));

		verifyNoInteractions(this.mockClientRepo, this.mockClientEventPublisher);
		
		result.andExpect(status().isBadRequest());
	}

	@Test
	public void test_createClient_errorClientNameMissing() throws Exception {

		JSONObject clientFixture = new JSONObject();
		clientFixture.put("clientExecutiveId", TestData.CLIENT_EXEC_ID);
		clientFixture.put("contactNumber", TestData.PHONE_VALID);

		final ResultActions result = mvc.perform(
				post(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN).accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON).content(clientFixture.toString()));

		verifyNoInteractions(this.mockClientRepo, this.mockClientEventPublisher);
		
		result.andExpect(status().isBadRequest());
	}

	@Test
	public void test_createClient_errorBodyInputMissing() throws Exception {

		JSONObject clientFixture = new JSONObject();
		clientFixture.put("clientExecutiveId", TestData.CLIENT_EXEC_ID);

		final ResultActions result = mvc.perform(post(CLIENT_ROOT_PATH).header("Authorization", "Bearer " + TOKEN)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON));

		verifyNoInteractions(this.mockClientRepo, this.mockClientEventPublisher);
		
		result.andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_getClientById_success() throws Exception {
		
		final ClientDocument dataFixture = new ClientDocument();
		dataFixture.setDocId(TestData.DOC_ID);
		dataFixture.setName(TestData.NAME);
		dataFixture.setClientExecutiveId(TestData.CLIENT_EXEC_ID);
		dataFixture.setContactNumber(TestData.PHONE_VALID);
		
		when(this.mockClientRepo.findByDocId(TestData.DOC_ID)).thenReturn(Optional.of(dataFixture));
		
		final ResultActions result = mvc.perform(
				get(CLIENT_ROOT_PATH + "/" + TestData.DOC_ID).header("Authorization", "Bearer " + TOKEN).accept(MediaType.APPLICATION_JSON));
		
		verify(this.mockClientRepo, times(1)).findByDocId(TestData.DOC_ID);
		
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(TestData.DOC_ID)))
			.andExpect(jsonPath("$.name", is(TestData.NAME)))
			.andExpect(jsonPath("$.clientExecutiveId", is(TestData.CLIENT_EXEC_ID)))
			.andExpect(jsonPath("$.contactNumber", is(TestData.PHONE_VALID)));
	}
	
	@Test
	public void test_getClientById_errorClientNotFound() throws Exception {
		
		when(this.mockClientRepo.findByDocId(TestData.DOC_ID)).thenReturn(Optional.empty());
		
		final ResultActions result = mvc.perform(
				get(CLIENT_ROOT_PATH + "/" + TestData.DOC_ID).header("Authorization", "Bearer " + TOKEN).accept(MediaType.APPLICATION_JSON));
		
		verify(this.mockClientRepo, times(1)).findByDocId(TestData.DOC_ID);
		
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void test_deleteClientById_success() throws Exception {
		
		// 1. Setup
		when(this.mockClientRepo.existsByDocId(TestData.DOC_ID)).thenReturn(true);
		
		// 2. Execute
		final ResultActions result = mvc.perform(
				delete(CLIENT_ROOT_PATH + "/" + TestData.DOC_ID).header("Authorization", "Bearer " + TOKEN));
		
		// 3. Validate
		verify(this.mockClientRepo).existsByDocId(TestData.DOC_ID);
		verify(this.mockClientRepo).deleteByDocId(TestData.DOC_ID);
		
		result.andExpect(status().isNoContent());
	}
	
	@Test
	public void test_deleteClientById_errorDocIdNotFound() throws Exception {
		
		// 1. Setup
		when(this.mockClientRepo.existsByDocId(TestData.DOC_ID)).thenReturn(false);
		
		// 2. Execute
		final ResultActions result = mvc.perform(
				delete(CLIENT_ROOT_PATH + "/" + TestData.DOC_ID).header("Authorization", "Bearer " + TOKEN));
		
		// 3. Validate
		verify(this.mockClientRepo).existsByDocId(TestData.DOC_ID);
		verifyNoMoreInteractions(this.mockClientRepo);
		
		result.andExpect(status().isNotFound());
	}
}
