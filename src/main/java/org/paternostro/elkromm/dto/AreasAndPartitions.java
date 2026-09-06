package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Areas and partitions configuration ("blocco A"): the panel's arming
 * structure, read/written as a whole via the {@code AREE & SETTORI}/
 * {@code SET_PARTITIONS_AND_AREAS} commands.
 * <p>
 * Exposes its {@link Area} and {@link Partition} collections through the
 * usual {@code List}-style accessors (size, get-by-index, iterators,
 * streams) rather than returning the backing lists directly.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class AreasAndPartitions implements Serializable
{
    private List<Area>      areas;
    private List<Partition> partitions;

    /** Creates a new, empty areas/partitions configuration. */
    public AreasAndPartitions()
    {
        this.areas = new ArrayList<>();
        this.partitions = new ArrayList<>();
    }

    /**
     * Adds an area.
     *
     * @param area the area to add
     */
    public void addArea(Area area)
    {
        areas.add(area);
    }

    /**
     * Adds a partition.
     *
     * @param partition the partition to add
     */
    public void addPartition(Partition partition)
    {
        partitions.add(partition);
    }

    /**
     * Returns how many areas are configured.
     *
     * @return the area count
     */
    public int getAreaNum()
    {
        return areas.size();
    }

    /**
     * Returns how many partitions are configured.
     *
     * @return the partition count
     */
    public int getPartitionNum()
    {
        return partitions.size();
    }

    /**
     * Returns a single area.
     *
     * @param index 0-based position
     * @return the area
     */
    public Area getArea(int index)
    {
        return areas.get(index);
    }

    /**
     * Returns a single partition.
     *
     * @param index 0-based position
     * @return the partition
     */
    public Partition getPartition(int index)
    {
        return partitions.get(index);
    }

    /**
     * Returns whether no areas are configured.
     *
     * @return {@code true} if empty
     */
    public boolean isAreasEmpty() {
        return areas.isEmpty();
    }

    /**
     * Runs an action on each area.
     *
     * @param action the action to run
     */
    public void forEachArea(Consumer<? super Area> action) {
        areas.forEach(action);
    }

    /**
     * Returns an iterator over the areas.
     *
     * @return the iterator
     */
    public Iterator<Area> areasIterator() {
        return areas.iterator();
    }

    /**
     * Returns a stream over the areas.
     *
     * @return the stream
     */
    public Stream<Area> areasStream() {
        return areas.stream();
    }

    /**
     * Returns a parallel stream over the areas.
     *
     * @return the parallel stream
     */
    public Stream<Area> areasParallelStream() {
        return areas.parallelStream();
    }

    /**
     * Returns a list iterator over the areas.
     *
     * @return the list iterator
     */
    public ListIterator<Area> areasListIterator() {
        return areas.listIterator();
    }

    /**
     * Returns a list iterator over the areas, starting at a given position.
     *
     * @param index the starting position
     * @return the list iterator
     */
    public ListIterator<Area> areasListIterator(int index) {
        return areas.listIterator(index);
    }

    /**
     * Returns a spliterator over the areas.
     *
     * @return the spliterator
     */
    public Spliterator<Area> areasSpliterator() {
        return areas.spliterator();
    }

    /**
     * Returns whether no partitions are configured.
     *
     * @return {@code true} if empty
     */
    public boolean isPartitionsEmpty() {
        return partitions.isEmpty();
    }

    /**
     * Runs an action on each partition.
     *
     * @param action the action to run
     */
    public void forEachPartition(Consumer<? super Partition> action) {
        partitions.forEach(action);
    }

    /**
     * Returns an iterator over the partitions.
     *
     * @return the iterator
     */
    public Iterator<Partition> partitionsIterator() {
        return partitions.iterator();
    }

    /**
     * Returns a stream over the partitions.
     *
     * @return the stream
     */
    public Stream<Partition> partitionsStream() {
        return partitions.stream();
    }

    /**
     * Returns a parallel stream over the partitions.
     *
     * @return the parallel stream
     */
    public Stream<Partition> partitionsParallelStream() {
        return partitions.parallelStream();
    }

    /**
     * Returns a list iterator over the partitions.
     *
     * @return the list iterator
     */
    public ListIterator<Partition> partitionsListIterator() {
        return partitions.listIterator();
    }

    /**
     * Returns a list iterator over the partitions, starting at a given position.
     *
     * @param index the starting position
     * @return the list iterator
     */
    public ListIterator<Partition> partitionsListIterator(int index) {
        return partitions.listIterator(index);
    }

    /**
     * Returns a spliterator over the partitions.
     *
     * @return the spliterator
     */
    public Spliterator<Partition> partitionsSpliterator() {
        return partitions.spliterator();
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{areas=").append(areas).append(", partitions=").append(partitions).append("}");

        return sb.toString();
    }
}
