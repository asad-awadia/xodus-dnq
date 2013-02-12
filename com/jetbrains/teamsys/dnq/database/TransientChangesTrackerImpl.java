package com.jetbrains.teamsys.dnq.database;

import jetbrains.exodus.core.dataStructures.decorators.HashMapDecorator;
import jetbrains.exodus.core.dataStructures.decorators.HashSetDecorator;
import jetbrains.exodus.core.dataStructures.decorators.LinkedHashSetDecorator;
import jetbrains.exodus.core.dataStructures.hash.HashMap;
import jetbrains.exodus.database.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Vadim.Gurov
 */
public final class TransientChangesTrackerImpl implements TransientChangesTracker {

    private static final Log log = LogFactory.getLog(TransientEntityStoreImpl.class);

    private Set<TransientEntity> changedEntities = new LinkedHashSetDecorator<TransientEntity>();
    private Set<TransientEntity> addedEntities = new HashSetDecorator<TransientEntity>();
    private Set<TransientEntity> removedEntities = new HashSetDecorator<TransientEntity>();
    private Map<TransientEntity, Map<String, LinkChange>> entityToChangedLinksDetailed = new HashMapDecorator<TransientEntity, Map<String, LinkChange>>();
    private Map<TransientEntity, Set<String>> entityToChangedProperties = new HashMapDecorator<TransientEntity, Set<String>>();
    private PersistentStoreTransaction snapshot;

    TransientChangesTrackerImpl(PersistentStoreTransaction snapshot) {
        this.snapshot = snapshot;
    }

    @NotNull
    public Set<TransientEntity> getChangedEntities() {
        return changedEntities;
    }

    public PersistentStoreTransaction getSnapshot() {
        return snapshot;
    }

    @Override
    public TransientEntity getSnapshotEntity(TransientEntity e) {
        final ReadOnlyPersistentEntity ro = e.getPersistentEntity().getSnapshot(snapshot);
        return new ReadonlyTransientEntityImpl(getChangeDescription(e), ro, (TransientEntityStore) e.getStore());
    }

    public Set<TransientEntityChange> getChangesDescription() {
      Set<TransientEntityChange> changesDescription = new HashSetDecorator<TransientEntityChange>();

      for (TransientEntity e : getChangedEntities()) {
          // do not notify about RemovedNew entities - such entities was created and removed during same transaction
          if (e.isRemoved() && !e.wasSaved()) continue;

          changesDescription.add(new TransientEntityChange(this, e, getChangedProperties(e), getChangedLinksDetailed(e), getEntityChangeType(e)));
      }

      return changesDescription;
    }

    private EntityChangeType getEntityChangeType(TransientEntity e) {
        if (addedEntities.contains(e)) return EntityChangeType.ADD;
        if (removedEntities.contains(e)) return EntityChangeType.REMOVE;
        return EntityChangeType.UPDATE;
    }

    public TransientEntityChange getChangeDescription(TransientEntity e) {
        return new TransientEntityChange(this, e, getChangedProperties(e), getChangedLinksDetailed(e), getEntityChangeType(e));
    }

    @Nullable
    public Map<String, LinkChange> getChangedLinksDetailed(@NotNull TransientEntity e) {
        return entityToChangedLinksDetailed.get(e);
    }

    @Nullable
    public Set<String> getChangedProperties(@NotNull TransientEntity e) {
        return entityToChangedProperties.get(e);
    }

    void linkChanged(@NotNull TransientEntity source, @NotNull String linkName, @NotNull TransientEntity target, @Nullable TransientEntity oldTarget, boolean add) {
        entityChanged(source);

        Map<String, LinkChange> linksDetailed = entityToChangedLinksDetailed.get(source);
        if (linksDetailed == null) {
            linksDetailed = new HashMap<String, LinkChange>();
            entityToChangedLinksDetailed.put(source, linksDetailed);
        }

        LinkChange lc = linksDetailed.get(linkName);
        if (lc == null) {
            lc = new LinkChange(linkName);
            linksDetailed.put(linkName, lc);
        }

        if (add) {
            if (oldTarget != null) {
                lc.addRemoved(oldTarget);
            }
            lc.addAdded(target);
        } else {
            lc.addRemoved(target);
        }

        if (lc.getAddedEntitiesSize() == 0 && lc.getRemovedEntitiesSize() == 0) {
            linksDetailed.remove(linkName);
            if (linksDetailed.size() == 0) {
                entityToChangedLinksDetailed.remove(source);
            }
        }
    }

    void propertyChanged(TransientEntity e, String propertyName) {
        entityChanged(e);

        Set<String> properties = entityToChangedProperties.get(e);
        if (properties == null) {
            properties = new HashSet<String>();
            entityToChangedProperties.put(e, properties);
        }

        properties.add(propertyName);
    }


    void entityChanged(TransientEntity e) {
        changedEntities.add(e);
    }

    void entityAdded(TransientEntity e) {
        entityChanged(e);
        addedEntities.add(e);
    }

    void entityRemoved(TransientEntity e) {
        entityChanged(e);
        removedEntities.add(e);
    }

    @Override
    public void dispose() {
        snapshot.abort();
    }
}
