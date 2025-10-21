package vn.flower.repositories;

import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.flower.entities.Account;
import vn.flower.entities.CustomerDTO;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("""
    	    SELECT new vn.flower.entities.CustomerDTO(a.id, a.fullName, a.email, a.createdAt, COUNT(o))
    	    FROM Account a
    	    LEFT JOIN Order o ON a.id = o.account.id
    	    GROUP BY a.id, a.fullName, a.email, a.createdAt
    	""")
    	List<CustomerDTO> findAllCustomersWithOrderCount();
    long countByRole(String role);
    
    @Query("SELECT DISTINCT a.email FROM Account a JOIN ChatMessage m ON (m.sender.id = a.id OR m.receiver.id = a.id) " +
            "WHERE (m.sender.id = :adminId OR m.receiver.id = :adminId) AND a.id != :adminId")
     List<String> findDistinctChatPartners(@Param("adminId") Integer adminId);
}  

