package org.paternostro.elkromm.dto;

import java.io.Serializable;

/**
 * The plant and installer technical codes sent during the login handshake.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Login implements Serializable
{
    private int plantCode;
    private int technicalCode;

    /**
     * Creates new login credentials.
     *
     * @param plantCode installer/plant identification code, in range [0, 99999999]
     * @param technicalCode installer's technical access code, in range [0, 999999]
     */
    public Login(int plantCode, int technicalCode)
    {
        setPlantCode(plantCode);
        setTechnicalCode(technicalCode);
    }

    /**
     * Returns the plant identification code.
     *
     * @return the plant code
     */
    public int getPlantCode()
    {
        return plantCode;
    }

    /**
     * Sets the plant identification code.
     *
     * @param plantCode the code to set, in range [0, 99999999]
     * @throws IllegalArgumentException if out of range
     */
    public void setPlantCode(int plantCode)
    {
        if (plantCode < 0 || plantCode > 99999999) throw new IllegalArgumentException("Wrong plantCode " + plantCode + ", expected between 0 and 99999999");

        this.plantCode = plantCode;
    }

    /**
     * Returns the installer's technical access code.
     *
     * @return the technical code
     */
    public int getTechnicalCode()
    {
        return technicalCode;
    }

    /**
     * Sets the installer's technical access code.
     *
     * @param technicalCode the code to set, in range [0, 999999]
     * @throws IllegalArgumentException if out of range
     */
    public void setTechnicalCode(int technicalCode)
    {
        if (technicalCode < 0 || technicalCode > 999999) throw new IllegalArgumentException("Wrong technicalCode " + technicalCode + ", expected between 0 and 999999");

        this.technicalCode = technicalCode;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{plantCode=").append(plantCode).append(", technicalCode=").append(technicalCode).append("}");

        return sb.toString();
    }
}
