package org.paternostro.elkromm.emulator;

import java.util.HashMap;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.Command.Action;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Input.Configuration;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.Output;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.PSTNGSM.Country;
import org.paternostro.elkromm.dto.PSTNGSM.PABXLocalAccessDigit;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNAnsweringMachineRings;
import org.paternostro.elkromm.dto.PSTNGSM.PSTNLineTestFrequency;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.ParametersEnablings.AlarmCount;
import org.paternostro.elkromm.dto.ParametersEnablings.DST;
import org.paternostro.elkromm.dto.ParametersEnablings.Enabling;
import org.paternostro.elkromm.dto.ParametersEnablings.Help;
import org.paternostro.elkromm.dto.ParametersEnablings.Month;
import org.paternostro.elkromm.dto.ParametersEnablings.Notice;
import org.paternostro.elkromm.dto.ParametersEnablings.Play;
import org.paternostro.elkromm.dto.ParametersEnablings.PowerLack;
import org.paternostro.elkromm.dto.ParametersEnablings.Time;
import org.paternostro.elkromm.dto.Partition;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumber;
import org.paternostro.elkromm.dto.PhoneNumber.Event;
import org.paternostro.elkromm.dto.PhoneNumber.SendingMode;
import org.paternostro.elkromm.dto.PhoneNumber.Type;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallFrequency;
import org.paternostro.elkromm.dto.PhoneParameters.CyclicTestCallInterval;
import org.paternostro.elkromm.dto.PhoneParameters.ReturnCall;
import org.paternostro.elkromm.dto.PhoneParameters.VoiceMessagesSendingMode;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMS;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
import org.paternostro.elkromm.dto.User;

/**
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Model {
    private SystemStatus                systemStatus;
    private PeripheralUnits             peripheralUnits;
    private AreasAndPartitions          areasAndPartitions;
    private User[]                      users;
    private Key[]                       keys;
    private Expansion[]                 expansions;
    private Keyboard[]                  keyboards;
    private Reader[]                    readers;
    private ParametersEnablings         parametersEnablings;
    private TimeProgrammer              timeProgrammer;
    private PhoneParameters             phoneParameters;
    private PSTNGSM                     pSTNgSM;
    private PhoneNumbersSendingCodes    phoneNumbers;
    private SMSs                        sMSs;
    private C200bParameters             c200bParameters;

    private Checksums                   checksums;
    private byte[]                      inputStatus;
    
    public Model() {
        Config  config = Config.getInstance();

        boolean[]   activepartitions = { false};
        
        this.systemStatus       = new SystemStatus(activepartitions);
        this.peripheralUnits    = new PeripheralUnits();
        this.areasAndPartitions = new AreasAndPartitions();
        this.users              = new User[ElkrommFacade.MAX_CREDENTIALS];
        this.keys               = new Key[ElkrommFacade.MAX_CREDENTIALS];
        this.expansions         = new Expansion[config.getExpansions()];
        this.keyboards          = new Keyboard[config.getKeyboards()];
        this.readers            = new Reader[config.getReaders()];

        // Init PeripheralUnits
        for (int i = 0; i < config.getKeyboards(); i++) {
            this.peripheralUnits.addKeypad(i + 1);
        }

        for (int i = 0; i < config.getReaders(); i++) {
            this.peripheralUnits.addReader(i + 1);
        }

        for (int i = 0; i < config.getExpansions(); i++) {
            this.peripheralUnits.addExpansion(i + 1);
        }

        // Init AreasAndPartitions
        for (int i = 1; i <= config.getAreas(); i++) {
            this.areasAndPartitions.addArea(new Area(i, config.getAreaName(i), ElkrommUtils.unpackPartitions((byte)(config.getAreaSectors(i) & 0xFF))));
        }

        for (int i = 1; i <= config.getSectors(); i++) {
            this.areasAndPartitions.addPartition(new Partition(i, config.getSectorName(i), false, config.getSectorType(i), config.getSectorEntryTime(i), config.getSectorExitTime(i)));
        }

        // Init Users
        this.users[0] = new User("TECNICO                 ", User.Enabling.DISABLED, ElkrommUtils.unpackPartitions((byte)0xFF));
        this.users[1] = new User("MASTER                  ", User.Enabling.ALWAYS_ENABLED, ElkrommUtils.unpackPartitions((byte)0xFF));

        for (int i = 2; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            this.users[i] = new User("...                     ", User.Enabling.DISABLED, ElkrommUtils.unpackPartitions((byte)0x01));
        }

        // Init Keys
        for (int i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            this.keys[i] = new Key("...                     ", Key.Enabling.DISABLED, Key.Specialization.KS_CHANGE_PARTITION_STATUS, ElkrommUtils.unpackPartitions((byte)0x01));
        }

        // Init Expansions
        int inputNum = 1;
        int outputNum = 1;

        for (int i = 0; i < expansions.length; i++) {
            this.expansions[i] = new Expansion(i, String.format("%01d.%02d", 1, i), String.format("Espansione %02d", i));

            for (int j = 1; j <= ElkrommFacade.MAX_EXP_INPUTS; j++) {
                this.expansions[i].addInput(new Input(inputNum, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_IMMEDIATE, Input.Sensitivity.IS_HIGH, Input.Flags.IF_NONE.getValue(), Input.Video.IV_NONE, ElkrommUtils.unpackPartitions((byte)0x01), String.format("Input %d", inputNum), null));
                inputNum++;
            }

            for (int j = 1; j <= ElkrommFacade.MAX_EXP_OUTPUTS; j++) {
                this.expansions[i].addOutput(new Output(outputNum, Output.Type.OT_NORMALLY_LOW, ElkrommUtils.unpackPartitions((byte)0x01), Output.Specialization.OS_OR_TC, String.format("Output %d", outputNum)));
                outputNum++;
            }
        }

        // Init Keyboards
        for (int i = 0; i < keyboards.length; i++) {
            this.keyboards[i] = new Keyboard(i+1, "2.71", 
                                                new Input(inputNum, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, ElkrommUtils.unpackPartitions((byte)0x01), "input " + inputNum++, Input.Delay.ID_30_SECS),
                                                new Input(inputNum, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, ElkrommUtils.unpackPartitions((byte)0x01), "input " + inputNum++, Input.Delay.ID_20_SECS),
                                            Keyboard.Enablings.KE_ENTRY.getValue(), ElkrommUtils.unpackPartitions((byte)0x01), i % 2 == 0 ? Keyboard.AudioFeatures.KA_CAPABLE.getValue() : Keyboard.AudioFeatures.KA_NONE.getValue(), "Keyboard " + (i + 1));
        }

        // Init Readers
        for (int i = 0; i < readers.length; i++) {
            this.readers[i] = new Reader(i+1, 
                                            new Input(i*2 + 1, Input.Configuration.IC_NORMALLY_CLOSED_DOUBLE_BALANCED, Input.Specialization.IS_WAY, Input.Sensitivity.IS_HIGH, Input.Flags.IF_EXCLUSION_ENABLED.getValue(), Input.Video.IV_CAMERA_3, ElkrommUtils.unpackPartitions((byte)0x01), "input " + (i*2+1), Input.Delay.ID_30_SECS),
                                            new Input(i*2 + 2, Input.Configuration.IC_NORMALLY_CLOSED_BALANCED, Input.Specialization.IS_DELAYED, Input.Sensitivity.IS_HIGH, Input.Flags.IF_OR_SECTORS.getValue(), Input.Video.IV_CAMERA_2, ElkrommUtils.unpackPartitions((byte)0x01), "input " + (i*2+2), Input.Delay.ID_20_SECS),
                                        ElkrommFacade.Partition.P_ONE, ElkrommFacade.Partition.P_TWO, ElkrommFacade.Partition.P_THREE, ElkrommFacade.Partition.P_FOUR, Reader.Enablings.RE_MASKING.getValue(), "Reader " + (i + 1));
        }

        // Init ParametersEnablins
        this.parametersEnablings = new ParametersEnablings(Time.PET_30_SECS, Time.PET_60_SECS, Time.PET_90_SECS, AlarmCount.PEAC_FOUR, PowerLack.PEPL_4_HOUR, Play.PEP_SECTS.getValue(), (byte)(Help.PEH_ENABLE.getValue() | 0x01), Enabling.PEE_ENABLE, Enabling.PEE_DISABLE, Notice.PEN_15_MINS, DST.PED_ENABLE.getValue(), Month.PEM_OCTOBER, Month.PEM_MARCH);

        // Init TimeProgrammer
        Command[]   workingDayCommands = new Command[8];
        Command[]   preHolidayDayCommands = new Command[8];
        Command[]   holidayDayCommands = new Command[8];
        DayClass[]  dayClasses = { DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_WORKING_DAY, DayClass.DCCDC_PRE_HOLIDAY, DayClass.DCCDC_HOLIDAY };

        for (int i = 0; i < workingDayCommands.length; i++) {
            workingDayCommands[i] = new Command(Action.CA_NONE, (byte)1 , null, (byte)0, (byte)0);
            preHolidayDayCommands[i] = new Command(Action.CA_NONE, (byte)1 , null, (byte)0, (byte)0);
            holidayDayCommands[i] = new Command(Action.CA_NONE, (byte)1 , null, (byte)0, (byte)0);
        }

        this.timeProgrammer     = new TimeProgrammer(workingDayCommands, preHolidayDayCommands, holidayDayCommands, dayClasses);

        // Init PhoneParameters
        this.phoneParameters = new PhoneParameters(org.paternostro.elkromm.dto.PhoneParameters.Enabling.PPE_DISABLED, ReturnCall.PPRC_DISABLED, org.paternostro.elkromm.dto.PhoneParameters.Enabling.PPE_DISABLED, VoiceMessagesSendingMode.PPVMSM_NONE, CyclicTestCallFrequency.PPCTCF_DISABLE, (byte)1, (byte)0, (byte)0, CyclicTestCallInterval.PPCTCI_1_HOUR);

        // Init PSTNGSM
        this.pSTNgSM = new PSTNGSM(org.paternostro.elkromm.dto.PSTNGSM.Enabling.PGE_DISABLED, Country.PGC_ITALY, PABXLocalAccessDigit.PGPLAD_DISABLE, org.paternostro.elkromm.dto.PSTNGSM.Enabling.PGE_DISABLED, org.paternostro.elkromm.dto.PSTNGSM.Enabling.PGE_DISABLED, PSTNLineTestFrequency.PGPLTF_DISABLE, PSTNAnsweringMachineRings.PGPAMR_DISABLE, org.paternostro.elkromm.dto.PSTNGSM.Enabling.PGE_DISABLED, org.paternostro.elkromm.dto.PSTNGSM.Enabling.PGE_DISABLED, org.paternostro.elkromm.dto.PSTNGSM.Enabling.PGE_DISABLED, 0, (byte)1, (byte)0);

        // Init PhoneNumbers
        boolean[]       associatedPartitions = { false, false, false, false, false, false, false, false };
        PhoneNumber     emptyPhoneNumber = new PhoneNumber("", associatedPartitions, Type.PNT_PSTN, SendingMode.PNSM_VOICE, new Event[0]);
        PhoneNumber[]   phoneNumbers = new org.paternostro.elkromm.dto.PhoneNumber[ElkrommFacade.MAX_PHONE_NUMBERS];

        for (int i = 0; i < ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            phoneNumbers[i] = emptyPhoneNumber;
        }

        this.phoneNumbers = new PhoneNumbersSendingCodes(phoneNumbers);

        // Init SMSs
        SMS[]   texts = new SMS[ElkrommFacade.MAX_SMS];

        texts[0] = new SMS("Burlgar");
        texts[1] = new SMS("Technical alarm 1");
        texts[2] = new SMS("Technical alarm 2");
        texts[3] = new SMS("Technical alarm 3");
        texts[4] = new SMS("Fire");
        texts[5] = new SMS("Partition activated");
        texts[6] = new SMS("Partition deactivated");
        texts[7] = new SMS("Tampering");
        texts[8] = new SMS("Notice");

        this.sMSs = new SMSs(texts);

        // Init C200b parameters
        this.c200bParameters = new C200bParameters(new HashMap<>(), new byte[0]);

        computeChecksum();

        // Init input status
        int inputs = 0;
        
        for (Expansion pivot : this.expansions) {
            for (int i = 0; i < pivot.getInputNum(); i++) {
                inputs += pivot.getInput(i).getConfiguration() == Configuration.IC_NOT_USED ? 0 : 1;
            }
        }

        for (Keyboard pivot : this.keyboards) {
            inputs += pivot.getFirstInput().getConfiguration() == Configuration.IC_NOT_USED ? 0 : 1;
            inputs += pivot.getSecondInput().getConfiguration() == Configuration.IC_NOT_USED ? 0 : 1;
        }

        for (Reader pivot : this.readers) {
            inputs += pivot.getFirstInput().getConfiguration() == Configuration.IC_NOT_USED ? 0 : 1;
            inputs += pivot.getSecondInput().getConfiguration() == Configuration.IC_NOT_USED ? 0 : 1;
        }

        inputStatus = new byte[inputs];

        for (int i = 0; i < inputs; i++) {
            inputStatus[i] = (byte)i;
        }
    }

    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    public PeripheralUnits getPeripheralUnits() {
        return peripheralUnits;
    }

    public AreasAndPartitions getAreasAndPartitions() {
        return areasAndPartitions;
    }

    public void setAreasAndPartitions(AreasAndPartitions areasAndPartitions) {
        this.areasAndPartitions = areasAndPartitions;
    }

    public User[] getUsers() {
        return users;
    }

    public Key[] getKeys() {
        return keys;
    }

    public Expansion[] getExpansions() {
        return expansions;
    }

    public Keyboard[] getKeyboards() {
        return keyboards;
    }

    public Reader[] getReaders() {
        return readers;
    }

    public ParametersEnablings getParametersEnablings() {
        return parametersEnablings;
    }

    public void setParametersEnablings(ParametersEnablings parametersEnablings) {
        this.parametersEnablings = parametersEnablings;
    }

    public TimeProgrammer getTimeProgrammer() {
        return timeProgrammer;
    }

    public void setTimeProgrammer(TimeProgrammer timeProgrammer) {
        this.timeProgrammer = timeProgrammer;
    }

    public PhoneParameters getPhoneParameters() {
        return phoneParameters;
    }

    public void setPhoneParameters(PhoneParameters phoneParameters) {
        this.phoneParameters = phoneParameters;
    }

    public PSTNGSM getPSTNGSM() {
        return pSTNgSM;
    }

    public void setPSTNGSM(PSTNGSM pSTNgSM) {
        this.pSTNgSM = pSTNgSM;
    }

    public PhoneNumbersSendingCodes getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(PhoneNumbersSendingCodes phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public SMSs getSMSs() {
        return sMSs;
    }

    public void setSMSs(SMSs sMSs) {
        this.sMSs = sMSs;
    }

    public C200bParameters getC200bParameters() {
        return c200bParameters;
    }

    public void setC200bParameters(C200bParameters c200bParameters) {
        this.c200bParameters = c200bParameters;
    }

    public void computeChecksum() {
        ElkrommFactory  factory = ElkrommFactory.getFactory();
        byte[]          data;
        int             expansionsChecksum = 0;

        if (this.expansions != null && this.expansions.length > 0) {
            data = factory.getExpansionsSerializer().serialize(this.expansions);
            expansionsChecksum = ElkrommUtils.getLong(data, data.length - 4);
        }

        int             keyboardsChecksum = 0;

        if (this.keyboards != null && this.keyboards.length > 0) {
            data = factory.getKeyboardsSerializer().serialize(this.keyboards);
            keyboardsChecksum = ElkrommUtils.getLong(data, data.length - 4);
        }

        data = factory.getAreasAndPartitionsSerializer().serialize(this.areasAndPartitions);
        
        int             areasAndPartitionsChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getUsersSerializer().serialize(this.users);
        
        int             usersChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getKeysSerializer().serialize(this.keys);
        
        int             keysChecksum = ElkrommUtils.getLong(data, data.length - 4);
        int             readersChecksum = 0;

        if (this.readers != null && this.readers.length > 0) {
            data = factory.getReadersSerializer().serialize(this.readers);
            readersChecksum = ElkrommUtils.getLong(data, data.length - 4);
        }

        data = factory.getParametersEnablingsSerializer().serialize(this.parametersEnablings);
        
        int             systemChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getTimeProgrammerSerializer().serialize(this.timeProgrammer);
        
        int             timeProgrammerChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getPhoneParametersSerializer().serialize(this.phoneParameters);
        
        int             phoneParametersChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getPSTNGSMSerializer().serialize(this.pSTNgSM);
        
        int             pSTNgSMChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getPhoneNumbersSendingCodesSerializer().serialize(this.phoneNumbers);
        
        int             phoneNumbersChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getSMSsSerializer().serialize(this.sMSs);
        
        int             sMSChecksum = ElkrommUtils.getLong(data, data.length - 4);

        data = factory.getC200bParametersSerializer().serialize(this.c200bParameters);
        
        int             c200bChecksum = ElkrommUtils.getLong(data, data.length - 4);

        this.checksums = new Checksums(expansionsChecksum, keyboardsChecksum, readersChecksum, systemChecksum, timeProgrammerChecksum, areasAndPartitionsChecksum, phoneParametersChecksum, phoneNumbersChecksum, c200bChecksum, sMSChecksum, pSTNgSMChecksum, usersChecksum, keysChecksum);
    }

    public Checksums getChecksums() {
        return checksums;
    }

    public byte[] getInputStatus() {
        return inputStatus;
    }

    public boolean[] getUserEnablings() {
        boolean[]   userEnablings = new boolean[ElkrommFacade.MAX_CREDENTIALS];

        for (int i = 0; i < ElkrommFacade.MAX_CREDENTIALS; i++) {
            userEnablings[i] = this.users[i].getEnabling() != Credential.Enabling.DISABLED;
        }
        
        return userEnablings;
    }
}
