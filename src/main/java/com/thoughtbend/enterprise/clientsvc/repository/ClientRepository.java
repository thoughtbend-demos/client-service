package com.thoughtbend.enterprise.clientsvc.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thoughtbend.enterprise.clientsvc.entity.ClientDocument;

public interface ClientRepository extends MongoRepository<ClientDocument, String> {

	Optional<ClientDocument> findByDocId(String docId);
}
