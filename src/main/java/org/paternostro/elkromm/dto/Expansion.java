package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Expansion implements Serializable
{
    private int             address;
    private String          version;
    private List<Input>     inputs;
    private List<Output>    outputs;
    private String          name;

    public Expansion(int address, String version, String name)
    {
        this.address = address;
        this.version = version;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.name = name;
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
        this.address = address;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
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
}
