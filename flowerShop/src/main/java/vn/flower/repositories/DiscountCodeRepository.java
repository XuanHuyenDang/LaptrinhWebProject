package vn.flower.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // *** Import Query ***
import org.springframework.data.repository.query.Param; // *** Import Param ***
import vn.flower.entities.DiscountCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);

    List<DiscountCode> findByIsActiveTrueOrderByEndDateDesc();

    // *** FIXED: Replaced complex method name with @Query ***
    /**
     * Finds discount codes that are active, currently valid within the date range,
     * and either have unlimited usage (maxUsage is null) or have been used less
     * than their maximum allowed usage.
     * @param now The current date and time to check against start/end dates.
     * @return A list of valid DiscountCode entities.
     */
    @Query("SELECT dc FROM DiscountCode dc WHERE dc.isActive = true " +
           "AND dc.startDate <= :now AND dc.endDate >= :now " +
           "AND (dc.maxUsage IS NULL OR dc.currentUsage < dc.maxUsage)")
    List<DiscountCode> findValidDiscountCodes(@Param("now") LocalDateTime now);

    // You can keep the simpler version if needed, using query derivation:
    List<DiscountCode> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime nowStart, LocalDateTime nowEnd);
}