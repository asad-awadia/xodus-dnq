package com.jetbrains.teamsys.dnq.database;

import jetbrains.exodus.database.LinkChange;
import jetbrains.exodus.database.TransientChangesTracker;
import jetbrains.exodus.database.TransientEntity;
import jetbrains.exodus.database.TransientEntityChange;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ReadOnlyTransientChangesTrackerImpl implements TransientChangesTracker {
    private PersistentStoreTransaction snapshot;

    ReadOnlyTransientChangesTrackerImpl(PersistentStoreTransaction snapshot) {
        this.snapshot = snapshot;
    }

    @NotNull
    @Override
    public Set<TransientEntityChange> getChangesDescription() {
        return Collections.EMPTY_SET;
    }

    @Override
    public TransientEntityChange getChangeDescription(TransientEntity e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getChangesDescriptionCount() {
        return 0;
    }

    @Nullable
    @Override
    public Map<String, LinkChange> getChangedLinksDetailed(@NotNull TransientEntity e) {
        return Collections.EMPTY_MAP;
    }

    @Nullable
    @Override
    public Set<String> getChangedProperties(@NotNull TransientEntity e) {
        return Collections.EMPTY_SET;
    }

    @Override
    public PersistentStoreTransaction getSnapshot() {
        return snapshot;
    }

    @Override
    public TransientEntity getSnapshotEntity(TransientEntity e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<TransientEntity> getChangedEntities() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<TransientEntity> getRemovedEntities() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<String> getAffectedEntityTypes() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean isNew(TransientEntity e) {
        return false;
    }

    @Override
    public boolean isSaved(TransientEntity transientEntity) {
        return true;
    }

    @Override
    public boolean isRemoved(TransientEntity transientEntity) {
        return false;
    }

    @Override
    public void linkChanged(TransientEntity source, String linkName, TransientEntity target, TransientEntity oldTarget, boolean add) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void linksRemoved(TransientEntity source, String linkName, Iterable<Entity> links) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void propertyChanged(TransientEntity e, String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePropertyChanged(TransientEntity e, String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void entityAdded(TransientEntity e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void entityRemoved(TransientEntity e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransientChangesTracker upgrade() {
        return new TransientChangesTrackerImpl(snapshot);
    }

    @Override
    public void dispose() {
        if (snapshot != null) {
            snapshot.abort();
            snapshot = null;
        }
    }
}
