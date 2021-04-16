package com.thoughtbend.enterprise.clientsvc.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtbend.enterprise.clientsvc.entity.ClientDocument;
import com.thoughtbend.enterprise.clientsvc.event.ClientEventPublisher;
import com.thoughtbend.enterprise.clientsvc.event.OutboundNewClientEvent;
import com.thoughtbend.enterprise.clientsvc.repository.ClientRepository;
import com.thoughtbend.enterprise.clientsvc.resource.ClientResource;
import com.thoughtbend.enterprise.clientsvc.util.Const;

@RestController
@RequestMapping(Const.ApiPath.VERSION + "/client")
@CrossOrigin
public class ClientController {

	private static final Logger LOG = LoggerFactory.getLogger(ClientController.class);

	@Autowired
	private ClientRepository clientRepository;
	
	@Autowired
	private ClientEventPublisher clientEventPublisher;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	@PreAuthorize("hasAuthority('SCOPE_read:client')")
	public List<ClientResource> getClients() {

		if (LOG.isTraceEnabled()) {
			LOG.trace("ClientResource::getClients() called");
		}

		// @formatter:off
		var result = this.clientRepository.findAll().stream()
							.map(this::transform)
							.collect(Collectors.toList());
		// @formatter:on

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("ClientResource::getClients() returned %1$s results", result.size()));
		}

		return result;
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('SCOPE_create:client')")
	public ResponseEntity<ClientResource> createClient(@RequestBody @Valid ClientResource clientResource)
			throws Exception {

		final String id = UUID.randomUUID().toString();
		clientResource.setId(id);

		final ClientDocument dataDoc = this.transform(clientResource);
		this.clientRepository.save(dataDoc);
		
		final OutboundNewClientEvent event = new OutboundNewClientEvent("NEW_CLIENT", clientResource);
		this.clientEventPublisher.publish(event);

		return ResponseEntity.created(new URI(Const.ApiPath.VERSION + "/client/" + id)).body(clientResource);
	}

	@GetMapping(path = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	@PreAuthorize("hasAuthority('SCOPE_read:client')")
	public ClientResource getClientById(@PathVariable(name = "clientId") final String clientId) {

		final Optional<ClientDocument> optionalClientDocument = this.clientRepository.findByDocId(clientId);

		final ClientDocument clientDocument = optionalClientDocument.orElseThrow(NotFoundException::new);
		final ClientResource result = this.transform(clientDocument);

		return result;
	}
	
	@DeleteMapping(path = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteClientById(@PathVariable(name = "clientId") final String clientId) {
		
		if (!this.clientRepository.existsByDocId(clientId)) {
			throw new NotFoundException();
		}
		
		this.clientRepository.deleteByDocId(clientId);
	}

	private ClientResource transform(ClientDocument source) {

		final ClientResource target = new ClientResource();

		target.setId(source.getDocId());
		target.setName(source.getName());
		target.setContactNumber(source.getContactNumber());
		target.setClientExecutiveId(source.getClientExecutiveId());

		return target;
	}

	private ClientDocument transform(ClientResource source) {

		final ClientDocument target = new ClientDocument();

		target.setDocId(source.getId());
		target.setName(source.getName());
		target.setContactNumber(source.getContactNumber());
		target.setClientExecutiveId(source.getClientExecutiveId());

		return target;
	}
}
