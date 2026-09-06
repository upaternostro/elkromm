package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The bus addresses of every keypad, proximity reader and expansion unit
 * connected to the panel, as returned by the {@code ADDRESSES} command.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class PeripheralUnits implements Serializable
{
    private List<Integer> keypadsAddresses;
    private List<Integer> readersAddresses;
    private List<Integer> expansionsAddresses;

    /** Creates a new, empty peripheral units listing. */
    public PeripheralUnits()
    {
        this.keypadsAddresses = new ArrayList<>();
        this.readersAddresses = new ArrayList<>();
        this.expansionsAddresses = new ArrayList<>();
    }

    /**
     * Adds a keypad address.
     *
     * @param address the bus address to add
     */
    public void addKeypad(int address)
    {
        keypadsAddresses.add(address);
    }

    /**
     * Adds a proximity reader address.
     *
     * @param address the bus address to add
     */
    public void addReader(int address)
    {
        readersAddresses.add(address);
    }

    /**
     * Adds an expansion unit address.
     *
     * @param address the bus address to add
     */
    public void addExpansion(int address)
    {
        expansionsAddresses.add(address);
    }

    /**
     * Returns how many keypads are connected.
     *
     * @return the keypad count
     */
    public int getKeypadNum()
    {
        return keypadsAddresses.size();
    }

    /**
     * Returns how many proximity readers are connected.
     *
     * @return the reader count
     */
    public int getReaderNum()
    {
        return readersAddresses.size();
    }

    /**
     * Returns how many expansion units are connected.
     *
     * @return the expansion count
     */
    public int getExpansionNum()
    {
        return expansionsAddresses.size();
    }

    /**
     * Returns the bus address of a keypad.
     *
     * @param index 0-based position in this listing
     * @return the bus address
     */
    public int getKeypadAddress(int index)
    {
        return keypadsAddresses.get(index);
    }

    /**
     * Returns the bus address of a proximity reader.
     *
     * @param index 0-based position in this listing
     * @return the bus address
     */
    public int getReaderAddress(int index)
    {
        return readersAddresses.get(index);
    }

    /**
     * Returns the bus address of an expansion unit.
     *
     * @param index 0-based position in this listing
     * @return the bus address
     */
    public int getExpansionAddress(int index)
    {
        return expansionsAddresses.get(index);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{keypadsAddresses=").append(keypadsAddresses).append(", readersAddresses=").append(readersAddresses
               ).append(", expansionsAddresses=").append(expansionsAddresses).append("}");
        
        return sb.toString();
    }
}
