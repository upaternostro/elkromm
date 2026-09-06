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
 * An expansion unit ("blocco B"): an addressable board adding inputs and/or
 * outputs to the panel (e.g. a LAN expansion, an I/O expansion).
 * <p>
 * Exposes its {@link Input} and {@link Output} collections through the
 * usual {@code List}-style accessors rather than returning the backing
 * lists directly.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Expansion implements Serializable, Comparable<Expansion>
{
    private int             address;
    private String          version;
    private List<Input>     inputs;
    private List<Output>    outputs;
    private String          name;

    /**
     * Creates a new expansion unit, with no inputs or outputs yet.
     *
     * @param address bus address of this unit, non-negative
     * @param version firmware version string
     * @param name display name
     */
    public Expansion(int address, String version, String name)
    {
        setAddress(address);
        setVersion(version);
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        setName(name);
    }

    /**
     * Adds an input to this expansion unit.
     *
     * @param input the input to add
     */
    public void addInput(Input input)
    {
        inputs.add(input);
    }

    /**
     * Adds an output to this expansion unit.
     *
     * @param output the output to add
     */
    public void addOutput(Output output)
    {
        outputs.add(output);
    }

    /**
     * Returns how many inputs this unit has.
     *
     * @return the input count
     */
    public int getInputNum()
    {
        return inputs.size();
    }

    /**
     * Returns how many outputs this unit has.
     *
     * @return the output count
     */
    public int getOutputNum()
    {
        return outputs.size();
    }

    /**
     * Returns a single input.
     *
     * @param index 0-based position
     * @return the input
     */
    public Input getInput(int index)
    {
        return inputs.get(index);
    }

    /**
     * Returns a single output.
     *
     * @param index 0-based position
     * @return the output
     */
    public Output getOutput(int index)
    {
        return outputs.get(index);
    }

    /**
     * Returns this unit's bus address.
     *
     * @return the address
     */
    public int getAddress()
    {
        return address;
    }

    /**
     * Sets this unit's bus address.
     *
     * @param address the address to set, non-negative
     * @throws IllegalArgumentException if negative
     */
    public void setAddress(int address)
    {
        if (address < 0) throw new IllegalArgumentException("Wrong address " + address + ", expected greater or equal than 0");

        this.address = address;
    }

    /**
     * Returns this unit's firmware version string.
     *
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets this unit's firmware version string.
     *
     * @param version the version to set, not {@code null}
     * @throws IllegalArgumentException if {@code version} is {@code null}
     */
    public void setVersion(String version)
    {
        if (version == null) throw new IllegalArgumentException("Missing mandatory version");

        this.version = version;
    }

    /**
     * Returns the display name of this unit.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the display name of this unit.
     *
     * @param name the name to set, not {@code null}
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public void setName(String name)
    {
        if (name == null) throw new IllegalArgumentException("Missing mandatory name");

        this.name = name;
    }

    /**
     * Runs an action on each input.
     *
     * @param action the action to run
     */
    public void forEachInput(Consumer<? super Input> action) {
        inputs.forEach(action);
    }

    /**
     * Returns whether this unit has no inputs.
     *
     * @return {@code true} if empty
     */
    public boolean isInputsEmpty() {
        return inputs.isEmpty();
    }

    /**
     * Returns an iterator over the inputs.
     *
     * @return the iterator
     */
    public Iterator<Input> inputsIterator() {
        return inputs.iterator();
    }

    /**
     * Returns a stream over the inputs.
     *
     * @return the stream
     */
    public Stream<Input> inputsStream() {
        return inputs.stream();
    }

    /**
     * Returns a parallel stream over the inputs.
     *
     * @return the parallel stream
     */
    public Stream<Input> inputsParallelStream() {
        return inputs.parallelStream();
    }

    /**
     * Returns a list iterator over the inputs.
     *
     * @return the list iterator
     */
    public ListIterator<Input> inputsListIterator() {
        return inputs.listIterator();
    }

    /**
     * Returns a list iterator over the inputs, starting at a given position.
     *
     * @param index the starting position
     * @return the list iterator
     */
    public ListIterator<Input> inputsListIterator(int index) {
        return inputs.listIterator(index);
    }

    /**
     * Returns a spliterator over the inputs.
     *
     * @return the spliterator
     */
    public Spliterator<Input> inputsSpliterator() {
        return inputs.spliterator();
    }

    /**
     * Runs an action on each output.
     *
     * @param action the action to run
     */
    public void forEachOutput(Consumer<? super Output> action) {
        outputs.forEach(action);
    }

    /**
     * Returns whether this unit has no outputs.
     *
     * @return {@code true} if empty
     */
    public boolean isOutputsEmpty() {
        return outputs.isEmpty();
    }

    /**
     * Returns an iterator over the outputs.
     *
     * @return the iterator
     */
    public Iterator<Output> outputsIterator() {
        return outputs.iterator();
    }

    /**
     * Returns a stream over the outputs.
     *
     * @return the stream
     */
    public Stream<Output> outputsStream() {
        return outputs.stream();
    }

    /**
     * Returns a parallel stream over the outputs.
     *
     * @return the parallel stream
     */
    public Stream<Output> outputsParallelStream() {
        return outputs.parallelStream();
    }

    /**
     * Returns a list iterator over the outputs.
     *
     * @return the list iterator
     */
    public ListIterator<Output> outputsListIterator() {
        return outputs.listIterator();
    }

    /**
     * Returns a list iterator over the outputs, starting at a given position.
     *
     * @param index the starting position
     * @return the list iterator
     */
    public ListIterator<Output> outputsListIterator(int index) {
        return outputs.listIterator(index);
    }

    /**
     * Returns a spliterator over the outputs.
     *
     * @return the spliterator
     */
    public Spliterator<Output> outputsSpliterator() {
        return outputs.spliterator();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{address=").append(address).append(", version=").append(version).append(", inputs=").append(inputs).append(", outputs=").append(outputs
               ).append(", name=").append(name).append("}");
        
        return sb.toString();
    }

    /**
     * Compares two expansion units by their bus address.
     *
     * @param o the other unit to compare against
     * @return the result of comparing the two addresses
     */
    @Override
    public int compareTo(Expansion o) {
        return Integer.compare(this.getAddress(), o.getAddress());
    }
}
