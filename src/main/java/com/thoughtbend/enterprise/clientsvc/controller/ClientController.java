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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtbend.enterprise.clientsvc.entity.ClientDocument;
import com.thoughtbend.enterprise.clientsvc.event.ClientEventPublisher;
import com.thoughtbend.enterprise.clientsvc.event.OutboundClientDataEvent;
import com.thoughtbend.enterprise.clientsvc.event.OutboundClientDataEventType;
import com.thoughtbend.enterprise.clientsvc.event.OutboundDeleteClientEvent;
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
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("ClientResource::createClient() called");
		}

		final String id = UUID.randomUUID().toString();
		clientResource.setId(id);

		final ClientDocument dataDoc = this.transform(clientResource);
		this.clientRepository.save(dataDoc);
		
		final OutboundClientDataEvent event = OutboundClientDataEventType.NEW.createEvent(clientResource);
		this.clientEventPublisher.publish(event);
		
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Finished creating client [%1$s]", id));
		}

		return ResponseEntity.created(new URI(Const.ApiPath.VERSION + "/client/" + id)).body(clientResource);
	}

	@GetMapping(path = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)
	@PreAuthorize("hasAuthority('SCOPE_read:client')")
	public ClientResource getClientById(@PathVariable(name = "clientId") final String clientId) {

		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("ClientResource::getClientById() called [%1$s]", clientId));
		}
		
		final Optional<ClientDocument> optionalClientDocument = this.clientRepository.findByDocId(clientId);

		final ClientDocument clientDocument = optionalClientDocument.orElseThrow(NotFoundException::new);
		final ClientResource result = this.transform(clientDocument);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("ClientResource::getClientById() fetched successfully [%1$s]", clientId));
		}

		return result;
	}
	
	@PutMapping(path = "/{clientId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_update:client')")
	public void updateClientbyId(@PathVariable(name = "clientId") final String clientId,
			@RequestBody @Valid final ClientResource clientResource) {

		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("ClientResource::updateClientbyId() called [%1$s]", clientId));
		}
		
		if (clientId.equals(clientResource.getId()) == false) {
			LOG.info(String.format("Incoming clientId did not match between path and document body [path=%1$s, body=%2$s]", clientId, clientResource.getId()));
			throw new BadRequestException();
		}

		final ClientDocument clientDocument = this.clientRepository.findByDocId(clientId)
				.orElseThrow(NotFoundException::new);
		
		final ClientDocument mergedClientDocument = this.merge(clientDocument, clientResource);
		this.clientRepository.save(mergedClientDocument);
		this.clientEventPublisher.publish(OutboundClientDataEventType.UPDATE.createEvent(clientResource));
		
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Finished updating client [%1$s]", clientId));
		}
		
	}
	
	@DeleteMapping(path = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('SCOPE_delete:client')")
	public void deleteClientById(@PathVariable(name = "clientId") final String clientId) {
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("ClientResource::deleteClientbyId() called [%1$s]", clientId));
		}
		
		final ClientDocument originalClientDocument =
				this.clientRepository.findByDocId(clientId).orElseThrow(NotFoundException::new);
		
		this.clientRepository.deleteByDocId(clientId);
		final ClientResource clientResource = this.transform(originalClientDocument);
		final OutboundDeleteClientEvent deleteClientEvent = 
				new OutboundDeleteClientEvent(clientId, clientResource);
		this.clientEventPublisher.publish(deleteClientEvent);
		
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Finished deleting client [%1$s]", clientId));
		}
	}

	private ClientResource transform(ClientDocument source) {

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("transform ClientDocument as source [%1$s]", source));
		}
		
		final ClientResource target = new ClientResource();

		target.setId(source.getDocId());
		target.setName(source.getName());
		target.setContactNumber(source.getContactNumber());
		target.setClientExecutiveId(source.getClientExecutiveId());
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("transform ClientResource as target [%1$s]", target));
		}

		return target;
	}

	private ClientDocument transform(ClientResource source) {

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("transform ClientResource as source [%1$s]", source));
		}
		
		final ClientDocument target = new ClientDocument();

		target.setDocId(source.getId());
		target.setName(source.getName());
		target.setContactNumber(source.getContactNumber());
		target.setClientExecutiveId(source.getClientExecutiveId());
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("transform ClientDocument as target [%1$s]", target));
		}

		return target;
	}
	
	private ClientDocument merge(ClientDocument target, ClientResource source) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("merge ClientResource as source [%1$s]", source));
		}
		
		target.setName(source.getName());
		target.setClientExecutiveId(source.getClientExecutiveId());
		target.setContactNumber(source.getContactNumber());
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("merge ClientDocument as target [%1$s]", target));
		}
		
		return target;
	}
}
