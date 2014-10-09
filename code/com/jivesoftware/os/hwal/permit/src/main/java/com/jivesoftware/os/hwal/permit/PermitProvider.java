package com.jivesoftware.os.hwal.permit;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.List;

public interface PermitProvider {

    /**
     * @param tenant
     * @param permitGroup
     * @param config
     * @param count
     * @return a new permit from the pool of expired or not-yet-issued permits or an empty collection if all permits are taken.
     */
    List<Permit> requestPermit(String tenant, String permitGroup, PermitConfig config, int count);

    /**
     * Attempts to renew a permit.
     *
     * The original permit should not be used after calling this method.
     *
     * @param oldPermits
     * @return a renewed Permit, or nothing in the case that the Permit was already expired
     */
    List<Optional<Permit>> renewPermit(List<Permit> oldPermits);

    /**
     * Releases the provided permit if it is still valid
     *
     * @param permits
     */
    void releasePermit(Collection<Permit> permits);

    /**
     * Returns the number of distinct permit holders
     *
     * @param tenant
     * @param permitGroup
     * @param permitConfig
     * @return
     */
    List<Permit> getAllIssuedPermits(String tenant, String permitGroup, PermitConfig permitConfig);

    /**
     *
     * @param permit if permit is null you will get an Optional.absent();
     * @return Optional absent if expired else return provided or a renewed permit.
     */
    Optional<Permit> isExpired(Permit permit);

}
