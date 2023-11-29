package com.aeropink.pruebatecnica.repository;

import com.aeropink.pruebatecnica.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByDelayGreaterThanEqual(String i);
    //List<Client> findByDelayGreaterThanEqual(int i);
}
