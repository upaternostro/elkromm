package org.paternostro.elkromm.dto;

import java.io.Serializable;

public class Login implements Serializable
{
    private int plantCode;
    private int technicalCode;

    public Login(int plantCode, int technicalCode)
    {
        setPlantCode(plantCode);
        setTechnicalCode(technicalCode);
    }

    public int getPlantCode()
    {
        return plantCode;
    }

    public void setPlantCode(int plantCode)
    {
        if (plantCode < 0 || plantCode > 99999999) throw new IllegalArgumentException("Wrong plantCode " + plantCode + ", expected between 0 and 99999999");

        this.plantCode = plantCode;
    }

    public int getTechnicalCode()
    {
        return technicalCode;
    }

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
