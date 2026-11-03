package org.paternostro.elkromm.emulator;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommFactory;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.AreasAndPartitions;
import org.paternostro.elkromm.dto.C200bParameters;
import org.paternostro.elkromm.dto.Checksums;
import org.paternostro.elkromm.dto.Command;
import org.paternostro.elkromm.dto.Credential;
import org.paternostro.elkromm.dto.DayClassCommands.DayClass;
import org.paternostro.elkromm.dto.Expansion;
import org.paternostro.elkromm.dto.Input;
import org.paternostro.elkromm.dto.Input.Configuration;
import org.paternostro.elkromm.dto.Key;
import org.paternostro.elkromm.dto.Keyboard;
import org.paternostro.elkromm.dto.Output;
import org.paternostro.elkromm.dto.PSTNGSM;
import org.paternostro.elkromm.dto.ParametersEnablings;
import org.paternostro.elkromm.dto.Partition;
import org.paternostro.elkromm.dto.PeripheralUnits;
import org.paternostro.elkromm.dto.PhoneNumber;
import org.paternostro.elkromm.dto.PhoneNumbersSendingCodes;
import org.paternostro.elkromm.dto.PhoneParameters;
import org.paternostro.elkromm.dto.Reader;
import org.paternostro.elkromm.dto.SMS;
import org.paternostro.elkromm.dto.SMSs;
import org.paternostro.elkromm.dto.SystemStatus;
import org.paternostro.elkromm.dto.TimeProgrammer;
import org.paternostro.elkromm.dto.User;
import org.paternostro.elkromm.emulator.Config.Weekday;

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
        this.expansions         = new Expansion[config.getExpansions() + 1];
        this.keyboards          = new Keyboard[config.getKeyboards()];
        this.readers            = new Reader[config.getReaders()];

        // Init PeripheralUnits
        for (int i = 0; i < config.getKeyboards(); i++) {
            this.peripheralUnits.addKeypad(i + 1);
        }

        for (int i = 0; i < config.getReaders(); i++) {
            this.peripheralUnits.addReader(i + 1);
        }

        for (int i = 0; i <= config.getExpansions(); i++) {
            this.peripheralUnits.addExpansion(i + 1);
        }

        // Init AreasAndPartitions
        for (int i = 1; i <= config.getAreas(); i++) {
            this.areasAndPartitions.addArea(new Area(config.getAreaName(i), ElkrommUtils.unpackPartitions((byte)(config.getAreaPartitions(i) & 0xFF))));
        }

        this.areasAndPartitions.setAreaNum((byte)config.getAreas());

        for (int i = 1; i <= config.getPartitions(); i++) {
            this.areasAndPartitions.addPartition(new Partition(config.getPartitionName(i), config.getPartitionVocalName(i), config.getPartitionType(i), config.getPartitionEntryTime(i), config.getPartitionExitTime(i)));
        }

        this.areasAndPartitions.setPartitionNum((byte)config.getPartitions());

        // Init Users
        for (int i = 1; i <= ElkrommFacade.MAX_CREDENTIALS; i++) {
            this.users[i - 1] = new User(config.getUserName(i), config.getUserEnablings(i), ElkrommUtils.unpackPartitions(config.getUserPartitions(i)));
            }

        // Init Keys
        for (int i = 1; i <= ElkrommFacade.MAX_CREDENTIALS; i++) {
            this.keys[i - 1] = new Key(config.getKeyName(i), config.getKeyEnablings(i), config.getKeySpecialization(i), ElkrommUtils.unpackPartitions(config.getKeyPartitions(i)));
        }

        // Init Expansions
        int inputNum = 1;
        int outputNum = 1;

        for (int i = 0; i < expansions.length; i++) {
            this.expansions[i] = new Expansion(i, config.getExpansionVersion(i), config.getExpansionName(i));

            for (int j = 1; j <= ElkrommFacade.MAX_EXP_INPUTS; j++) {
                this.expansions[i].addInput(new Input(inputNum, config.getInputConfiguration(inputNum), config.getInputSpecialization(inputNum), config.getInputSensitivity(inputNum), config.getInputFlags(inputNum), config.getInputVideo(inputNum), ElkrommUtils.unpackPartitions(config.getInputPartitions(inputNum)), config.getInputName(inputNum), config.getInputDelay(inputNum)));
                inputNum++;
            }

            for (int j = 1; j <= (i == 0 ? ElkrommFacade.MAX_PANEL_OUTPUTS : ElkrommFacade.MAX_EXP_OUTPUTS); j++) {
                this.expansions[i].addOutput(new Output(outputNum, config.getOutputType(outputNum), ElkrommUtils.unpackPartitions(config.getOutputPartitions(outputNum)), config.getOutputSpecialization(outputNum), config.getOutputName(outputNum)));
                outputNum++;
            }
        }

        // Init Keyboards
        for (int i = 1; i <= keyboards.length; i++) {
            this.keyboards[i-1] = new Keyboard(i, config.getKeypadVersion(i), 
                                                new Input(inputNum, config.getKeypadInputConfiguration(i, Config.InputOrdinal.FIRST), config.getKeypadInputSpecialization(i, Config.InputOrdinal.FIRST), config.getKeypadInputSensitivity(i, Config.InputOrdinal.FIRST), config.getKeypadInputFlags(i, Config.InputOrdinal.FIRST), config.getKeypadInputVideo(i, Config.InputOrdinal.FIRST), ElkrommUtils.unpackPartitions(config.getKeypadInputPartitions(i, Config.InputOrdinal.FIRST)), config.getKeypadInputName(i, Config.InputOrdinal.FIRST, inputNum++), config.getKeypadInputDelay(i, Config.InputOrdinal.FIRST)),
                                                new Input(inputNum, config.getKeypadInputConfiguration(i, Config.InputOrdinal.SECOND), config.getKeypadInputSpecialization(i, Config.InputOrdinal.SECOND), config.getKeypadInputSensitivity(i, Config.InputOrdinal.SECOND), config.getKeypadInputFlags(i, Config.InputOrdinal.SECOND), config.getKeypadInputVideo(i, Config.InputOrdinal.SECOND), ElkrommUtils.unpackPartitions(config.getKeypadInputPartitions(i, Config.InputOrdinal.SECOND)), config.getKeypadInputName(i, Config.InputOrdinal.SECOND, inputNum++), config.getKeypadInputDelay(i, Config.InputOrdinal.SECOND)),
                                            config.getKeypadEnablings(i), ElkrommUtils.unpackPartitions(config.getKeypadPartitions(i)), config.getKeypadAudioFeatures(i), config.getKeypadName(i));
        }

        // Init Readers
        for (int i = 1; i <= readers.length; i++) {
            this.readers[i-1] = new Reader(i, 
                                            new Input(inputNum, config.getReaderInputConfiguration(i, Config.InputOrdinal.FIRST), config.getReaderInputSpecialization(i, Config.InputOrdinal.FIRST), config.getReaderInputSensitivity(i, Config.InputOrdinal.FIRST), config.getReaderInputFlags(i, Config.InputOrdinal.FIRST), config.getReaderInputVideo(i, Config.InputOrdinal.FIRST), ElkrommUtils.unpackPartitions(config.getReaderInputPartitions(i, Config.InputOrdinal.FIRST)), config.getReaderInputName(i, Config.InputOrdinal.FIRST, inputNum++), config.getReaderInputDelay(i, Config.InputOrdinal.FIRST)),
                                            new Input(inputNum, config.getReaderInputConfiguration(i, Config.InputOrdinal.SECOND), config.getReaderInputSpecialization(i, Config.InputOrdinal.SECOND), config.getReaderInputSensitivity(i, Config.InputOrdinal.SECOND), config.getReaderInputFlags(i, Config.InputOrdinal.SECOND), config.getReaderInputVideo(i, Config.InputOrdinal.SECOND), ElkrommUtils.unpackPartitions(config.getReaderInputPartitions(i, Config.InputOrdinal.SECOND)), config.getReaderInputName(i, Config.InputOrdinal.SECOND, inputNum++), config.getReaderInputDelay(i, Config.InputOrdinal.SECOND)),
                                        config.getReaderLed(i, 1), config.getReaderLed(i, 2), config.getReaderLed(i, 3), config.getReaderLed(i, 4), config.getReaderEnablings(i), config.getReaderName(i));
        }

        // Init ParametersEnablings
        this.parametersEnablings = new ParametersEnablings(config.getParametersEnablingsBurglarTime(), config.getParametersEnablingsEmergencyTime(), config.getParametersEnablingsPreAlarmTime(), 
                                                            config.getParametersEnablingsAlarmCount(), config.getParametersEnablingsPowerLack(), config.getParametersEnablingsPlay(), 
                                                            (byte)(config.getParametersEnablingsHelp() | 0x01), config.getParametersEnablingsLAN(), config.getParametersEnablingsTimeProgrammer(), 
                                                            config.getParametersEnablingsNotice(), config.getParametersEnablingsDST(), config.getParametersEnablingsDSTOn(), 
                                                            config.getParametersEnablingsDSTOff());

        // Init TimeProgrammer
        Command[]   workingDayCommands = new Command[8];
        Command[]   preHolidayDayCommands = new Command[8];
        Command[]   holidayDayCommands = new Command[8];
        DayClass[]  dayClasses = { config.getTimeProgrammerDayClass(Weekday.MONDAY), config.getTimeProgrammerDayClass(Weekday.TUESDAY), config.getTimeProgrammerDayClass(Weekday.WEDNESDAY), 
                                    config.getTimeProgrammerDayClass(Weekday.THURSDAY), config.getTimeProgrammerDayClass(Weekday.FRIDAY), config.getTimeProgrammerDayClass(Weekday.SATURDAY), 
                                    config.getTimeProgrammerDayClass(Weekday.SUNDAY) };

        for (int i = 1; i <= workingDayCommands.length; i++) {
            workingDayCommands[i-1] = new Command(config.getTimeProgrammerCommandAction(DayClass.DCCDC_WORKING_DAY, i), config.getTimeProgrammerCommandObject(DayClass.DCCDC_WORKING_DAY, i), config.getTimeProgrammerCommandObjectType(DayClass.DCCDC_WORKING_DAY, i), config.getTimeProgrammerCommandHour(DayClass.DCCDC_WORKING_DAY, i), config.getTimeProgrammerCommandMinute(DayClass.DCCDC_WORKING_DAY, i));
            preHolidayDayCommands[i-1] = new Command(config.getTimeProgrammerCommandAction(DayClass.DCCDC_PRE_HOLIDAY, i), config.getTimeProgrammerCommandObject(DayClass.DCCDC_PRE_HOLIDAY, i), config.getTimeProgrammerCommandObjectType(DayClass.DCCDC_PRE_HOLIDAY, i), config.getTimeProgrammerCommandHour(DayClass.DCCDC_PRE_HOLIDAY, i), config.getTimeProgrammerCommandMinute(DayClass.DCCDC_PRE_HOLIDAY, i));
            holidayDayCommands[i-1] = new Command(config.getTimeProgrammerCommandAction(DayClass.DCCDC_HOLIDAY, i), config.getTimeProgrammerCommandObject(DayClass.DCCDC_HOLIDAY, i), config.getTimeProgrammerCommandObjectType(DayClass.DCCDC_HOLIDAY, i), config.getTimeProgrammerCommandHour(DayClass.DCCDC_HOLIDAY, i), config.getTimeProgrammerCommandMinute(DayClass.DCCDC_HOLIDAY, i));
        }

        this.timeProgrammer     = new TimeProgrammer(workingDayCommands, preHolidayDayCommands, holidayDayCommands, dayClasses);

        // Init PhoneParameters
        this.phoneParameters = new PhoneParameters(config.getPhoneParametersCallDelay(), config.getPhoneParametersReturnCall(), config.getPhoneParametersRemoteSurveillance(), 
                                                    config.getPhoneParametersVoiceMessagesSendingMode(), config.getPhoneParametersCyclicTestCallFrequency(), config.getPhoneParametersCyclicTestCallPhoneNumber(), 
                                                    config.getPhoneParametersCyclicTestCallHour(), config.getPhoneParametersCyclicTestCallMinute(), config.getPhoneParametersCyclicTestCallInterval());

        // Init PSTNGSM
        this.pSTNgSM = new PSTNGSM(config.getPSTNGSMEnablePSTN(), config.getPSTNGSMCountry(), config.getPSTNGSMPABXLocalAccessDigit(), config.getPSTNGSMToneControl(), 
                                    config.getPSTNGSMAnswerControl(), config.getPSTNGSMPSTNLineTestFrequency(), config.getPSTNGSMPSTNAnsweringMachineRings(), 
                                    config.getPSTNGSMEnableGSM(), config.getPSTNGSMEnableGSMAnsweringMachine(), config.getPSTNGSMEnableIncomingSMS(), config.getPSTNGSMGSMPIN(), 
                                    config.getPSTNGSMExpirationMonth(), config.getPSTNGSMExpirationYear());

        // Init PhoneNumbers
        PhoneNumber[]   phoneNumbers = new PhoneNumber[ElkrommFacade.MAX_PHONE_NUMBERS];

        for (int i = 1; i <= ElkrommFacade.MAX_PHONE_NUMBERS; i++) {
            phoneNumbers[i-1] = new PhoneNumber(config.getPhoneNumberPhoneNumber(i), ElkrommUtils.unpackPartitions(config.getPhoneNumberPartitions(i)), config.getPhoneNumberType(i), 
                                                config.getPhoneNumberSendingMode(i), config.getPhoneNumberAssignedEvents(i));
        }

        this.phoneNumbers = new PhoneNumbersSendingCodes(phoneNumbers);

        // Init SMSs
        SMS[]   texts = new SMS[ElkrommFacade.MAX_SMS];

        for (int i = 1; i <= ElkrommFacade.MAX_SMS; i++) {
            texts[i-1] = new SMS(config.getSMSesText(i));
        }

        this.sMSs = new SMSs(texts);

        // Init C200b parameters
        this.c200bParameters = new C200bParameters(config.getC200bReserved(), config.getC200bEventCodes(), config.getC200bInputCodes());

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
            expansionsChecksum = ElkrommUtils.getBlockChecksum(data);
        }

        int             keyboardsChecksum = 0;

        if (this.keyboards != null && this.keyboards.length > 0) {
            data = factory.getKeyboardsSerializer().serialize(this.keyboards);
            keyboardsChecksum = ElkrommUtils.getBlockChecksum(data);
        }

        data = factory.getAreasAndPartitionsSerializer().serialize(this.areasAndPartitions);
        
        int             areasAndPartitionsChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getUsersSerializer().serialize(this.users);
        
        int             usersChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getKeysSerializer().serialize(this.keys);
        
        int             keysChecksum = ElkrommUtils.getBlockChecksum(data);
        int             readersChecksum = 0;

        if (this.readers != null && this.readers.length > 0) {
            data = factory.getReadersSerializer().serialize(this.readers);
            readersChecksum = ElkrommUtils.getBlockChecksum(data);
        }

        data = factory.getParametersEnablingsSerializer().serialize(this.parametersEnablings);
        
        int             systemChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getTimeProgrammerSerializer().serialize(this.timeProgrammer);
        
        int             timeProgrammerChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getPhoneParametersSerializer().serialize(this.phoneParameters);
        
        int             phoneParametersChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getPSTNGSMSerializer().serialize(this.pSTNgSM);
        
        int             pSTNgSMChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getPhoneNumbersSendingCodesSerializer().serialize(this.phoneNumbers);
        
        int             phoneNumbersChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getSMSsSerializer().serialize(this.sMSs);
        
        int             sMSChecksum = ElkrommUtils.getBlockChecksum(data);

        data = factory.getC200bParametersSerializer().serialize(this.c200bParameters);
        
        int             c200bChecksum = ElkrommUtils.getBlockChecksum(data);

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
            userEnablings[i] = Credential.Enabling.is(users[i].getEnabling(), Credential.Enabling.ENABLED);
        }
        
        return userEnablings;
    }
}
