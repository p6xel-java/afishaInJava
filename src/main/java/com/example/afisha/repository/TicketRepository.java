package com.example.afisha.repository;

import com.example.afisha.dto.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByYookassaPaymentId(String paymentId);

    Optional<Ticket> findByTicketTokenHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id=:id")
    Optional<Ticket> findWithLockById(@Param("id") Long id);

}
