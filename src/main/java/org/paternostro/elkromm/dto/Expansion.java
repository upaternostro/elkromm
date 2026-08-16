package org.paternostro.elkromm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    public void forEachInput(Consumer<? super Input> action) {
        inputs.forEach(action);
    }

    public boolean isInputsEmpty() {
        return inputs.isEmpty();
    }

    public Iterator<Input> inputsIterator() {
        return inputs.iterator();
    }

    public Stream<Input> inputsStream() {
        return inputs.stream();
    }

    public Stream<Input> inputsParallelStream() {
        return inputs.parallelStream();
    }

    public ListIterator<Input> inputsListIterator() {
        return inputs.listIterator();
    }

    public ListIterator<Input> inputsListIterator(int index) {
        return inputs.listIterator(index);
    }

    public Spliterator<Input> inputsSpliterator() {
        return inputs.spliterator();
    }

    public void forEachOutput(Consumer<? super Output> action) {
        outputs.forEach(action);
    }

    public boolean isOutputsEmpty() {
        return outputs.isEmpty();
    }

    public Iterator<Output> outputsIterator() {
        return outputs.iterator();
    }

    public Stream<Output> outputsStream() {
        return outputs.stream();
    }

    public Stream<Output> outputsParallelStream() {
        return outputs.parallelStream();
    }

    public ListIterator<Output> outputsListIterator() {
        return outputs.listIterator();
    }

    public ListIterator<Output> outputsListIterator(int index) {
        return outputs.listIterator(index);
    }

    public Spliterator<Output> outputsSpliterator() {
        return outputs.spliterator();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{address=").append(address).append(", version=").append(version).append(", inputs=").append(inputs).append(", outputs=").append(outputs
               ).append(", name=").append(name).append("}");
        
        return sb.toString();
    }

    @Override
    public int compareTo(Expansion o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
