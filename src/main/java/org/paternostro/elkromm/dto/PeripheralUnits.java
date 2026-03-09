package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PeripheralUnits implements Serializable
{
    private List<Integer> keypadsAddresses;
    private List<Integer> readersAddresses;
    private List<Integer> expansionsAddresses;

    public PeripheralUnits()
    {
        this.keypadsAddresses = new ArrayList<>();
        this.readersAddresses = new ArrayList<>();
        this.expansionsAddresses = new ArrayList<>();
    }

    public void addKeypad(int address)
    {
        keypadsAddresses.add(address);
    }

    public void addReader(int address)
    {
        readersAddresses.add(address);
    }

    public void addExpansion(int address)
    {
        expansionsAddresses.add(address);
    }

    public int getKeypadNum()
    {
        return keypadsAddresses.size();
    }

    public int getReaderNum()
    {
        return readersAddresses.size();
    }

    public int getExpansionNum()
    {
        return expansionsAddresses.size();
    }

    public int getKeypadAddress(int index)
    {
        return keypadsAddresses.get(index);
    }

    public int getReaderAddress(int index)
    {
        return readersAddresses.get(index);
    }

    public int getExpansionAddress(int index)
    {
        return expansionsAddresses.get(index);
    }

    @Override
    public String toString()
    {
        return "PeripheralUnits{" +
                "keypads=" + keypadsAddresses.size() +
                ", readers=" + readersAddresses.size() +
                ", expansions=" + expansionsAddresses.size() +
                '}';
    }
}
