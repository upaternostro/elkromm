package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * Costants defining payload sizes and other useful values.
 * 
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class SerializersConstants {
    /** Offset for input codes in C200B payload */
    public static final int C200B_INPUT_CODES_OFFSET    = 0x64;

    /** Time programmer command length */
    public final static int COMMAND_LENGTH              = 5;

    /** Credential (user/key) payload size */
    public static final int CREDENTIAL_SIZE             = 1+1+ElkrommFacade.NAME_LENGTH;  // 2 byte di flag e 24 di nome

    /** Expansion payload size */
    public static final int EXPANSION_SIZE              = 559;
    /** Input payload size */
    public static final int INPUT_SIZE                  = 38;
    /** Output payload size */
    public static final int OUTPUT_SIZE                 = 37;

    /** Single keyboard payload size */
    public static final int KEYBOARD_SIZE               = 111;

    /** Login payload size */
    public static final int LOGIN_SIZE                  = 7;
    
    /** Parameters &amp; enablings (aka system parameters) payload size */
    public static final int PARAMETERS_ENABLINGS_SIZE   = 30;

    /** Single phone number payload size */
    public static final int PHONE_NUMBER_SIZE           = 17;
    
    /** Phone parameters payload size */
    public static final int PHONE_PARAMETERS_SIZE       = 20;
    
    /** PSTN GSM parameters payload size */
    public static final int PSTN_GSM_SIZE               = 21;
    
    /** Reader payload size */
    public static final int READER_SIZE                 = 113;
    
    /** Single SMS payload size */
    public static final int SMS_SIZE                    = 40;
}
