package org.paternostro.elkromm.dto;

import java.io.Serializable;

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

    public int getNodes()
    {
        return nodes;
    }

    public void setNodes(int nodes)
    {
        this.nodes = nodes;
    }

    public int getKeypads()
    {
        return keypads;
    }

    public void setKeypads(int keypads)
    {
        this.keypads = keypads;
    }

    public int getReaders()
    {
        return readers;
    }

    public void setReaders(int readers)
    {
        this.readers = readers;
    }

    public int getSystem()
    {
        return system;
    }

    public void setSystem(int system)
    {
        this.system = system;
    }

    public int getTimeProgrammer()
    {
        return timeProgrammer;
    }

    public void setTimeProgrammer(int timeProgrammer)
    {
        this.timeProgrammer = timeProgrammer;
    }

    public int getAreasAndPartitions() {
        return areasAndPartitions;
    }

    public void setAreasAndPartitions(int areasAndPartitions)
    {
        this.areasAndPartitions = areasAndPartitions;
    }

    public int getTelephoneParameters() {
        return telephoneParameters;
    }

    public void setTelephoneParameters(int telephoneParameters)
    {
        this.telephoneParameters = telephoneParameters;
    }

    public int getTelephoneNumbers()
    {
        return telephoneNumbers;
    }

    public void setTelephoneNumbers(int telephoneNumbers)
    {
        this.telephoneNumbers = telephoneNumbers;
    }

    public int getEvents()
    {
        return events;
    }

    public void setEvents(int events)
    {
        this.events = events;
    }

    public int getSms()
    {
        return sms;
    }

    public void setSms(int sms)
    {
        this.sms = sms;
    }

    public int getPstnGsm()
    {
        return pstnGsm;
    }

    public void setPstnGsm(int pstnGsm)
    {
        this.pstnGsm = pstnGsm;
    }

    public int getUsers()
    {
        return users;
    }

    public void setUsers(int users)
    {
        this.users = users;
    }

    public int getKeys()
    {
        return keys;
    }

    public void setKeys(int keys)
    {
        this.keys = keys;
    }

    @Override
    public String toString()
    {
        return "Checksums{" +
                "nodes=" + nodes +
                ", keypads=" + keypads +
                ", readers=" + readers +
                ", system=" + system +
                ", timeProgrammer=" + timeProgrammer +
                ", areasAndPartitions=" + areasAndPartitions +
                ", telephoneParameters=" + telephoneParameters +
                ", telephoneNumbers=" + telephoneNumbers +
                ", events=" + events +
                ", sms=" + sms +
                ", pstnGsm=" + pstnGsm +
                ", users=" + users +
                ", keys=" + keys +
                '}';
    }
}
