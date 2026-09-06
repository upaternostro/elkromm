package org.paternostro.elkromm.dto;

import java.io.Serializable;

/**
 * The panel's per-block configuration checksums, as returned by the
 * {@code CHECKSUM} command. Comparing these against a previous read is a
 * cheap way to detect that some part of the configuration changed, without
 * re-reading every block.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Checksums implements Serializable
{
    private int nodes;
    private int keypads;
    private int readers;
    private int system;
    private int timeProgrammer;
    private int areasAndPartitions;
    private int telephoneParameters;
    private int telephoneNumbers;
    private int events;
    private int sms;
    private int pstnGsm;
    private int users;
    private int keys;

    /**
     * Creates a new checksums snapshot.
     *
     * @param nodes checksum of the peripheral addresses block
     * @param keypads checksum of the keypads block
     * @param readers checksum of the proximity readers block
     * @param system checksum of the system parameters block
     * @param timeProgrammer checksum of the time programmer block
     * @param areasAndPartitions checksum of the areas/partitions block
     * @param telephoneParameters checksum of the telephone parameters block
     * @param telephoneNumbers checksum of the phone numbers block
     * @param events checksum of the event log block
     * @param sms checksum of the SMS messages block
     * @param pstnGsm checksum of the PSTN/GSM configuration block
     * @param users checksum of the users block
     * @param keys checksum of the keys block
     */
    public Checksums(int nodes, int keypads, int readers, int system, int timeProgrammer, int areasAndPartitions, int telephoneParameters, int telephoneNumbers, int events, int sms, int pstnGsm, int users, int keys)
    {
        this.nodes = nodes;
        this.keypads = keypads;
        this.readers = readers;
        this.system = system;
        this.timeProgrammer = timeProgrammer;
        this.areasAndPartitions = areasAndPartitions;
        this.telephoneParameters = telephoneParameters;
        this.telephoneNumbers = telephoneNumbers;
        this.events = events;
        this.sms = sms;
        this.pstnGsm = pstnGsm;
        this.users = users;
        this.keys = keys;
    }

    /**
     * Returns the checksum of the peripheral addresses block.
     *
     * @return the checksum
     */
    public int getNodes()
    {
        return nodes;
    }

    /**
     * Sets the checksum of the peripheral addresses block.
     *
     * @param nodes the checksum to set
     */
    public void setNodes(int nodes)
    {
        this.nodes = nodes;
    }

    /**
     * Returns the checksum of the keypads block.
     *
     * @return the checksum
     */
    public int getKeypads()
    {
        return keypads;
    }

    /**
     * Sets the checksum of the keypads block.
     *
     * @param keypads the checksum to set
     */
    public void setKeypads(int keypads)
    {
        this.keypads = keypads;
    }

    /**
     * Returns the checksum of the proximity readers block.
     *
     * @return the checksum
     */
    public int getReaders()
    {
        return readers;
    }

    /**
     * Sets the checksum of the proximity readers block.
     *
     * @param readers the checksum to set
     */
    public void setReaders(int readers)
    {
        this.readers = readers;
    }

    /**
     * Returns the checksum of the system parameters block.
     *
     * @return the checksum
     */
    public int getSystem()
    {
        return system;
    }

    /**
     * Sets the checksum of the system parameters block.
     *
     * @param system the checksum to set
     */
    public void setSystem(int system)
    {
        this.system = system;
    }

    /**
     * Returns the checksum of the time programmer block.
     *
     * @return the checksum
     */
    public int getTimeProgrammer()
    {
        return timeProgrammer;
    }

    /**
     * Sets the checksum of the time programmer block.
     *
     * @param timeProgrammer the checksum to set
     */
    public void setTimeProgrammer(int timeProgrammer)
    {
        this.timeProgrammer = timeProgrammer;
    }

    /**
     * Returns the checksum of the areas/partitions block.
     *
     * @return the checksum
     */
    public int getAreasAndPartitions() {
        return areasAndPartitions;
    }

    /**
     * Sets the checksum of the areas/partitions block.
     *
     * @param areasAndPartitions the checksum to set
     */
    public void setAreasAndPartitions(int areasAndPartitions)
    {
        this.areasAndPartitions = areasAndPartitions;
    }

    /**
     * Returns the checksum of the telephone parameters block.
     *
     * @return the checksum
     */
    public int getTelephoneParameters() {
        return telephoneParameters;
    }

    /**
     * Sets the checksum of the telephone parameters block.
     *
     * @param telephoneParameters the checksum to set
     */
    public void setTelephoneParameters(int telephoneParameters)
    {
        this.telephoneParameters = telephoneParameters;
    }

    /**
     * Returns the checksum of the phone numbers block.
     *
     * @return the checksum
     */
    public int getTelephoneNumbers()
    {
        return telephoneNumbers;
    }

    /**
     * Sets the checksum of the phone numbers block.
     *
     * @param telephoneNumbers the checksum to set
     */
    public void setTelephoneNumbers(int telephoneNumbers)
    {
        this.telephoneNumbers = telephoneNumbers;
    }

    /**
     * Returns the checksum of the event log block.
     *
     * @return the checksum
     */
    public int getEvents()
    {
        return events;
    }

    /**
     * Sets the checksum of the event log block.
     *
     * @param events the checksum to set
     */
    public void setEvents(int events)
    {
        this.events = events;
    }

    /**
     * Returns the checksum of the SMS messages block.
     *
     * @return the checksum
     */
    public int getSms()
    {
        return sms;
    }

    /**
     * Sets the checksum of the SMS messages block.
     *
     * @param sms the checksum to set
     */
    public void setSms(int sms)
    {
        this.sms = sms;
    }

    /**
     * Returns the checksum of the PSTN/GSM configuration block.
     *
     * @return the checksum
     */
    public int getPstnGsm()
    {
        return pstnGsm;
    }

    /**
     * Sets the checksum of the PSTN/GSM configuration block.
     *
     * @param pstnGsm the checksum to set
     */
    public void setPstnGsm(int pstnGsm)
    {
        this.pstnGsm = pstnGsm;
    }

    /**
     * Returns the checksum of the users block.
     *
     * @return the checksum
     */
    public int getUsers()
    {
        return users;
    }

    /**
     * Sets the checksum of the users block.
     *
     * @param users the checksum to set
     */
    public void setUsers(int users)
    {
        this.users = users;
    }

    /**
     * Returns the checksum of the keys block.
     *
     * @return the checksum
     */
    public int getKeys()
    {
        return keys;
    }

    /**
     * Sets the checksum of the keys block.
     *
     * @param keys the checksum to set
     */
    public void setKeys(int keys)
    {
        this.keys = keys;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{nodes=").append(nodes).append(", keypads=").append(keypads).append(", readers=").append(readers).append(", system=").append(system
               ).append(", timeProgrammer=").append(timeProgrammer).append(", areasAndPartitions=").append(areasAndPartitions
               ).append(", telephoneParameters=").append(telephoneParameters).append(", telephoneNumbers=").append(telephoneNumbers
               ).append(", events=").append(events).append(", sms=").append(sms).append(", pstnGsm=").append(pstnGsm).append(", users=").append(users).append(", keys=").append(keys
               ).append("}");
        
        return sb.toString();
    }
}
