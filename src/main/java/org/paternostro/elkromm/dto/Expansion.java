package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Expansion implements Serializable, Comparable<Expansion>
{
    private int             address;
    private String          version;
    private List<Input>     inputs;
    private List<Output>    outputs;
    private String          name;

    public Expansion(int address, String version, String name)
    {
        setAddress(address);
        setVersion(version);
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        setName(name);
    }

    public void addInput(Input input)
    {
        inputs.add(input);
    }

    public void addOutput(Output output)
    {
        outputs.add(output);
    }

    public int getInputNum()
    {
        return inputs.size();
    }

    public int getOutputNum()
    {
        return outputs.size();
    }

    public Input getInput(int index)
    {
        return inputs.get(index);
    }

    public Output getOutput(int index)
    {
        return outputs.get(index);
    }

    public int getAddress()
    {
        return address;
    }

    public void setAddress(int address)
    {
        if (address < 0) throw new IllegalArgumentException("Wrong address " + address + ", expected greater or equal than 0");

        this.address = address;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        if (version == null) throw new IllegalArgumentException("Missing mandatory version");

        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

        this.name = name;
    }

    @Override
    public String toString() {
        return "Expansion{" +
                "name='" + name + '\'' +
                ", address=" + address +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public int compareTo(Expansion o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
