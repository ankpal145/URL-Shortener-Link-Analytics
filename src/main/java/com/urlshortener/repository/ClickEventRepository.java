package com.urlshortener.repository;

import com.urlshortener.domain.ClickEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByLinkId(Long linkId);

    @Query("select count(distinct c.ipHash) from ClickEvent c where c.linkId = :linkId and c.ipHash is not null")
    long countDistinctIpHashByLinkId(@Param("linkId") Long linkId);

    List<ClickEvent> findByLinkIdOrderByClickedAtDesc(Long linkId, Pageable pageable);

    @Query("select min(c.clickedAt) from ClickEvent c where c.linkId = :linkId")
    Instant findFirstClickAt(@Param("linkId") Long linkId);

    @Query("select max(c.clickedAt) from ClickEvent c where c.linkId = :linkId")
    Instant findLastClickAt(@Param("linkId") Long linkId);

    @Query("""
            select coalesce(c.referer, '(direct)') as referer, count(c) as cnt
            from ClickEvent c
            where c.linkId = :linkId
            group by coalesce(c.referer, '(direct)')
            order by cnt desc
            """)
    List<Object[]> topReferrers(@Param("linkId") Long linkId, Pageable pageable);

    @Query("""
            select coalesce(c.userAgent, '(unknown)') as ua, count(c) as cnt
            from ClickEvent c
            where c.linkId = :linkId
            group by coalesce(c.userAgent, '(unknown)')
            order by cnt desc
            """)
    List<Object[]> topUserAgents(@Param("linkId") Long linkId, Pageable pageable);

    @Query("select c.clickedAt from ClickEvent c where c.linkId = :linkId and c.clickedAt >= :since")
    List<Instant> findClickedAtSince(@Param("linkId") Long linkId, @Param("since") Instant since);
}
