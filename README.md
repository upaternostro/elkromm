# elkromm

A Java library that manages Elkron's alarm protocol.

The library reads and writes
panel configuration (areas, partitions, users, keys, keypads, readers, expansions, phone
numbers, time programmer...), reads live status (armed partitions, input states, user
enablings), and issues commands (arm/disarm, exclude/include inputs, enable/disable users).

## Disclaimer

Elkron is a trademark of Urmet S.p.A. Hi-Connect is a software product developed
and distributed by Urmet S.p.A. under the Elkron brand.

This project is not endorsed, sponsored, affiliated with, nor approved by Urmet.

All knowledge about the protocol implemented by this library has been obtained by
observing the network traffic generated during the normal operation of a licensed 
copy of Hi-Connect configuration software communicating with an Elkron control panel 
legitimately owned by the project's author — an activity permitted under Directive 2009/24/EC,
Art. 5(3), and corresponding national implementations. No part of Hi-Connect's or the
panel firmware's code has been decompiled, copied, or redistributed; only the on-the-wire
protocol has been documented and reimplemented independently, for the purpose of
achieving interoperability with third-party home automation systems (e.g. OpenHAB).

This library is intended for use with alarm systems that the user legitimately owns or
is otherwise authorized to access.

**This software interacts with a physical security system.** It is provided "as is",
without warranty of any kind, as detailed in the EUPL v1.2 (see [License](#license)).
It must not be relied upon as the sole means of protecting life, property, or safety.
Use entirely at your own risk.

## Features

- Full read/write access to panel configuration (areas & partitions, users, keys, keypads,
  proximity readers, expansions, phone numbers, PSTN/GSM, SMS messages, C200B remote
  receiver, time programmer)
- Live status: armed partitions, input states, user enablings
- Commands: arm/disarm partitions, exclude/include inputs, enable/disable users
- Bundled panel emulator, usable standalone or embedded in tests, to develop and test
  against without real hardware
- Optional round-trip integration test suite that validates the library against a real
  panel's actual configuration (see below)
- Protocol reference documentation ([PROTOCOL.md](PROTOCOL.md), English; also available in
  Italian as [PROTOCOL-ITA.md](PROTOCOL-ITA.md))

## Requirements

- Java 8 or later
- Maven
- [mock-ipc](https://github.com/upaternostro/mock-ipc), a small socket-abstraction library
  by the same author, published on Maven Central
- An Elkron MP-508 v3 alarm system equipped with a LAN expansion board (only required to
  talk to a real panel; the bundled emulator has no hardware requirement)

## License

This software is licensed under EUPL v1.2 or above. See [LICENSE.md](LICENSE.md) for full license text.

## Project structure

The library is organized in five packages:

- `org.paternostro.elkromm` — public API: the `ElkrommFacade` interface, `ElkrommFactory`
  to obtain instances, `ElkronCommand` (the protocol's command codes), and shared utilities
- `org.paternostro.elkromm.dto` — the data model: one class per configuration/status
  concept (areas, partitions, users, keys, inputs, keyboards, readers, expansions, phone
  numbers, ...), independent of how it's represented on the wire
- `org.paternostro.elkromm.packet` — on-the-wire packet framing, one class per protocol
  command
- `org.paternostro.elkromm.serializer` — conversion between DTOs and raw byte payloads
- `org.paternostro.elkromm.impl` — the default `ElkrommFacade`/`PacketQueue` implementation
- `org.paternostro.elkromm.emulator` — a standalone panel emulator (see below)

## Usage

The main entry point is the `ElkrommFacade` interface, obtained through `ElkrommFactory`.
The following code snippet shows the LOGIN-READ STATUS-ARM PARTITION-LOGOUT cycle:

```java
try (ElkrommFacade facade = ElkrommFactory.getFactory()
        .getElkrommFacade(InetAddress.getByName("192.168.1.100"), 8030, plantCode)) {
    facade.connect();
    facade.login(plantCode, technicalCode);

    SystemStatus status = facade.getSystemStatus();
    facade.armDisarmPartition(ElkrommFacade.Partition.P_ONE, true);
}
```

`ElkrommFacade` is `AutoCloseable`: `close()` logs out if a session is open and closes the connection,
so the explicit `logout()` and `disconnect()` calls (still available) are not needed.

The `getElkrommFacade(...)` factory call above can itself throw `IOException` (it calls
`init()` internally before returning); every other method shown can throw
`ElkrommException` (protocol/communication errors), as does `close()`. Omitted here for brevity — production
code should handle both.

See the Javadoc on `ElkrommFacade` for the full list of available operations.

## Emulator

The library bundles a standalone Elkron panel emulator (`org.paternostro.elkromm.emulator.Emulator`),
built into the jar's manifest as the default main class, so it can be executed with the following
command:

```
java -jar target/elkromm-<version>.jar
```

It listens on a TCP port and responds to the same protocol a real panel would, which is
useful both for elkromm's own test suite and to point Hi-Connect itself at it while
reverse engineering or documenting new parts of the protocol — no physical hardware needed.

### Configuration

The emulator reads an optional `elkromm-emulator.properties` file from the classpath, at
`org/paternostro/elkromm/emulator/elkromm-emulator.properties` (i.e. drop it under
`src/main/resources/org/paternostro/elkromm/emulator/` if building from source). Any key
not present falls back to its default value below.

| Property | Default | Notes |
|---|---|---|
| `org.paternostro.elkromm.emulator.port` | `8030` | TCP port the emulator listens on |
| `org.paternostro.elkromm.emulator.plant.code` | `55555555` | Plant code the emulator expects at login |
| `org.paternostro.elkromm.emulator.technical.code` | `000000` | Technician PIN the emulator expects at login |
| `org.paternostro.elkromm.emulator.keyboards` | `0` | Number of keypads connected to the emulated panel |
| `org.paternostro.elkromm.emulator.readers` | `0` | Number of readers connected to the emulated panel |
| `org.paternostro.elkromm.emulator.expansions` | `0` | Number of expansion boards connected to the emulated panel (not counting the one embedded in the panel) |
| `org.paternostro.elkromm.emulator.areas` | `0` | Number of areas of the emulated panel |
| `org.paternostro.elkromm.emulator.area.<N>.partitions` | `1` for area 1, `0` otherwise | Partitions of an area (`<N>`: 1-4) |
| `org.paternostro.elkromm.emulator.area.<N>.name` | `...` | Name of an area (`<N>`: 1-4) |
| `org.paternostro.elkromm.emulator.partitions` | `1` | Number of partitions of the emulated panel |
| `org.paternostro.elkromm.emulator.partition.<N>.entry.time` | `DISABLED` | Entry time of a partition (`<N>`: 1-8). Values: `DISABLED`, `TEN_SECONDS`, `TWENTY_SECONDS`, `THIRTY_SECONDS`, `ONE_MINUTE`, `ONE_MINUTE_THIRTY_SECONDS`, `FIVE_MINUTES` |
| `org.paternostro.elkromm.emulator.partition.<N>.exit.time` | `DISABLED` | Exit time of a partition (`<N>`: 1-8). Values: `DISABLED`, `TEN_SECONDS`, `TWENTY_SECONDS`, `THIRTY_SECONDS`, `ONE_MINUTE`, `ONE_MINUTE_THIRTY_SECONDS`, `FIVE_MINUTES` |
| `org.paternostro.elkromm.emulator.partition.<N>.name` | `...` | Name of a partition (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.partition.<N>.vocalName` | `false` | Vocal name of a partition (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.partition.<N>.type` | `STANDARD` | Type of a partition (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.user.<N>.name` | `TECNICO` for user 1, `MASTER` for user 2, `...` otherwise | Name of a user (`<N>`: 1-32) |
| `org.paternostro.elkromm.emulator.user.<N>.enablings` | `3` (enabled + always enabled) for user 2, `0` otherwise | Enablings of a user (`<N>`: 1-32) |
| `org.paternostro.elkromm.emulator.user.<N>.partitions` | `255` for users 1-2, `1` otherwise | Partitions of a user (`<N>`: 1-32) |
| `org.paternostro.elkromm.emulator.key.<N>.name` | `...` | Name of a key (`<N>`: 1-32) |
| `org.paternostro.elkromm.emulator.key.<N>.enablings` | `0` | Enablings of a key (`<N>`: 1-32) |
| `org.paternostro.elkromm.emulator.key.<N>.specialization` | `NONE` | Specialization of a key (`<N>`: 1-32). Values: `NONE`, `CHANGE_PARTITION_STATUS`, `CTRL_ACCESS`, `CTRL_ACCESS_PARTITION` |
| `org.paternostro.elkromm.emulator.key.<N>.partitions` | `1` | Partitions of a key (`<N>`: 1-32) |
| `org.paternostro.elkromm.emulator.expansion.<N>.name` | `UC` for expansion 0, `EP <NN>` otherwise | Name of an expansion (`<N>`: 0-7, 0 is the unit embedded in the panel) |
| `org.paternostro.elkromm.emulator.expansion.<N>.version` | `0301` for expansion 0, `0200` otherwise | Version of an expansion (`<N>`: 0-7, 0 is the unit embedded in the panel) |
| `org.paternostro.elkromm.emulator.input.<N>.configuration` | `NOT_USED` | Configuration of an input (`<N>`: 1-64). Values: `NOT_USED`, `NORMALLY_CLOSED`, `NORMALLY_OPEN`, `NORMALLY_CLOSED_BALANCED`, `NORMALLY_CLOSED_DOUBLE_BALANCED`, `SHOCK`, `ROLLER` |
| `org.paternostro.elkromm.emulator.input.<N>.specialization` | `IMMEDIATE` | Specialization of an input (`<N>`: 1-64). Values: constants of `Input.Specialization` |
| `org.paternostro.elkromm.emulator.input.<N>.sensitivity` | `HIGH` | Sensitivity of an input (`<N>`: 1-64). Values: `LOW`, `MEDIUM`, `HIGH` |
| `org.paternostro.elkromm.emulator.input.<N>.flags` | `0` | Flags of an input (`<N>`: 1-64) |
| `org.paternostro.elkromm.emulator.input.<N>.video` | `NONE` | Video of an input (`<N>`: 1-64). Values: `NONE`, `CAMERA_1`, `CAMERA_2`, `CAMERA_3`, `CAMERA_4` |
| `org.paternostro.elkromm.emulator.input.<N>.partitions` | `1` | Partitions of an input (`<N>`: 1-64) |
| `org.paternostro.elkromm.emulator.input.<N>.name` | `Input <N>` | Name of an input (`<N>`: 1-64) |
| `org.paternostro.elkromm.emulator.input.<N>.delay` | `5_SECS` | Delay of an input (`<N>`: 1-64). Values: `5_SECS`, `10_SECS`, `30_SECS`, `60_SECS`, `90_SECS`, `5_MINS`, `20_SECS` |
| `org.paternostro.elkromm.emulator.output.<N>.type` | `NORMALLY_LOW` | Type of an output (`<N>`: 1-48). Values: `NOT_USED`, `NORMALLY_LOW`, `NORMALLY_HIGH` |
| `org.paternostro.elkromm.emulator.output.<N>.partitions` | `1` | Partitions of an output (`<N>`: 1-48) |
| `org.paternostro.elkromm.emulator.output.<N>.specialization` | `OR_TC` | Specialization of an output (`<N>`: 1-48). Values: constants of `Output.Specialization` |
| `org.paternostro.elkromm.emulator.output.<N>.name` | `Output <N>` | Name of an output (`<N>`: 1-48) |
| `org.paternostro.elkromm.emulator.keypad.<N>.name` | `KP <NN>` | Name of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.version` | `0200` | Version of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.enablings` | `6` | Enablings of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.partitions` | `1` | Partitions of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.audioFeatures` | `0` | Audio features of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.configuration` | `NOT_USED` | Configuration of an input of a keypad (`<N>`: 1-8). Values: `NOT_USED`, `NORMALLY_CLOSED`, `NORMALLY_OPEN`, `NORMALLY_CLOSED_BALANCED`, `NORMALLY_CLOSED_DOUBLE_BALANCED`, `SHOCK`, `ROLLER` |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.specialization` | `IMMEDIATE` | Specialization of an input of a keypad (`<N>`: 1-8). Values: constants of `Input.Specialization` |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.sensitivity` | `HIGH` | Sensitivity of an input of a keypad (`<N>`: 1-8). Values: `LOW`, `MEDIUM`, `HIGH` |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.flags` | `0` | Flags of an input of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.video` | `NONE` | Video of an input of a keypad (`<N>`: 1-8). Values: `NONE`, `CAMERA_1`, `CAMERA_2`, `CAMERA_3`, `CAMERA_4` |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.partitions` | `1` | Partitions of an input of a keypad (`<N>`: 1-8) |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.name` | `Input <N>` | Name of an input of a keypad (`<N>`: 1-8; in the default, `<N>` is the input address) |
| `org.paternostro.elkromm.emulator.keypad.<N>.<I>Input.delay` | `5_SECS` | Delay of an input of a keypad (`<N>`: 1-8). Values: `5_SECS`, `10_SECS`, `30_SECS`, `60_SECS`, `90_SECS`, `5_MINS`, `20_SECS` |
| `org.paternostro.elkromm.emulator.reader.<N>.name` | `DK <NN>` | Name of a reader (`<N>`: 1-16) |
| `org.paternostro.elkromm.emulator.reader.<N>.enablings` | `0` | Enablings of a reader (`<N>`: 1-16) |
| `org.paternostro.elkromm.emulator.reader.<N>.led<L>` | `NONE` | LED of a reader (`<N>`: 1-16). Values: `NONE`, `ONE`, `TWO`, `THREE`, `FOUR`, `FIVE`, `SIX`, `SEVEN`, `EIGHT`, `ALL` |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.configuration` | `NOT_USED` | Configuration of an input of a reader (`<N>`: 1-16). Values: `NOT_USED`, `NORMALLY_CLOSED`, `NORMALLY_OPEN`, `NORMALLY_CLOSED_BALANCED`, `NORMALLY_CLOSED_DOUBLE_BALANCED`, `SHOCK`, `ROLLER` |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.specialization` | `IMMEDIATE` | Specialization of an input of a reader (`<N>`: 1-16). Values: constants of `Input.Specialization` |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.sensitivity` | `HIGH` | Sensitivity of an input of a reader (`<N>`: 1-16). Values: `LOW`, `MEDIUM`, `HIGH` |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.flags` | `0` | Flags of an input of a reader (`<N>`: 1-16) |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.video` | `NONE` | Video of an input of a reader (`<N>`: 1-16). Values: `NONE`, `CAMERA_1`, `CAMERA_2`, `CAMERA_3`, `CAMERA_4` |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.partitions` | `1` | Partitions of an input of a reader (`<N>`: 1-16) |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.name` | `Input <N>` | Name of an input of a reader (`<N>`: 1-16; in the default, `<N>` is the input address) |
| `org.paternostro.elkromm.emulator.reader.<N>.<I>Input.delay` | `5_SECS` | Delay of an input of a reader (`<N>`: 1-16). Values: `5_SECS`, `10_SECS`, `30_SECS`, `60_SECS`, `90_SECS`, `5_MINS`, `20_SECS` |
| `org.paternostro.elkromm.emulator.parametersEnablings.burglarTime` | `180_SECS` | Burglar time (general parameters and enablings of the panel). Values: `30_SECS`, `60_SECS`, `90_SECS`, `180_SECS`, `9_MINS` |
| `org.paternostro.elkromm.emulator.parametersEnablings.emergencyTime` | `180_SECS` | Emergency time (general parameters and enablings of the panel). Values: `30_SECS`, `60_SECS`, `90_SECS`, `180_SECS`, `9_MINS` |
| `org.paternostro.elkromm.emulator.parametersEnablings.preAlarmTime` | `180_SECS` | Pre alarm time (general parameters and enablings of the panel). Values: `30_SECS`, `60_SECS`, `90_SECS`, `180_SECS`, `9_MINS` |
| `org.paternostro.elkromm.emulator.parametersEnablings.alarmCount` | `NO_COUNT` | Alarm count (general parameters and enablings of the panel). Values: `NO_COUNT`, `TWO`, `FOUR`, `SIX`, `EIGHT` |
| `org.paternostro.elkromm.emulator.parametersEnablings.powerLack` | `1_HOUR` | Power lack (general parameters and enablings of the panel). Values: `1_HOUR`, `2_HOUR`, `4_HOUR` |
| `org.paternostro.elkromm.emulator.parametersEnablings.play` | `0` | Play (general parameters and enablings of the panel) |
| `org.paternostro.elkromm.emulator.parametersEnablings.help` | `0` | Help (general parameters and enablings of the panel) |
| `org.paternostro.elkromm.emulator.parametersEnablings.lan` | `DISABLE` | Lan (general parameters and enablings of the panel). Values: `DISABLE`, `ENABLE` |
| `org.paternostro.elkromm.emulator.parametersEnablings.timeProgrammer` | `DISABLE` | Time programmer (general parameters and enablings of the panel). Values: `DISABLE`, `ENABLE` |
| `org.paternostro.elkromm.emulator.parametersEnablings.notice` | `NO_NOTICE` | Notice (general parameters and enablings of the panel). Values: `NO_NOTICE`, `5_MINS`, `10_MINS`, `15_MINS`, `20_MINS` |
| `org.paternostro.elkromm.emulator.parametersEnablings.dst` | `0` | Dst (general parameters and enablings of the panel) |
| `org.paternostro.elkromm.emulator.parametersEnablings.dst.on` | `OCTOBER` | Dst on (general parameters and enablings of the panel). Values: `JANUARY`, `FEBRUARY`, `MARCH`, `APRIL`, `MAY`, `JUNE`, `JULY`, `AUGUST`, `SEPTEMBER`, `OCTOBER`, `NOVEMBER`, `DECEMBER` |
| `org.paternostro.elkromm.emulator.parametersEnablings.dst.off` | `MARCH` | Dst off (general parameters and enablings of the panel). Values: `JANUARY`, `FEBRUARY`, `MARCH`, `APRIL`, `MAY`, `JUNE`, `JULY`, `AUGUST`, `SEPTEMBER`, `OCTOBER`, `NOVEMBER`, `DECEMBER` |
| `org.paternostro.elkromm.emulator.timeProgrammer.command.<D>.<N>.action` | `NONE` | Action of a command of the time programmer. Values: `NONE`, `ENABLE`, `DISABLE` |
| `org.paternostro.elkromm.emulator.timeProgrammer.command.<D>.<N>.object` | `0` | Object of a command of the time programmer |
| `org.paternostro.elkromm.emulator.timeProgrammer.command.<D>.<N>.objectType` | `NONE` | Object type of a command of the time programmer. Values: `NONE`, `PARTITIONS`, `USER` |
| `org.paternostro.elkromm.emulator.timeProgrammer.command.<D>.<N>.hour` | `0` | Hour of a command of the time programmer |
| `org.paternostro.elkromm.emulator.timeProgrammer.command.<D>.<N>.minute` | `0` | Minute of a command of the time programmer |
| `org.paternostro.elkromm.emulator.timeProgrammer.dayClasses.<W>` | `WORKING_DAY` Monday-Friday, `PRE_HOLIDAY` Saturday, `HOLIDAY` Sunday | Day class of a weekday. Values: `WORKING_DAY`, `PRE_HOLIDAY`, `HOLIDAY` |
| `org.paternostro.elkromm.emulator.phoneParameters.callDelay` | `DISABLED` | Call delay (phone parameters). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.phoneParameters.returnCall` | `DISABLED` | Return call (phone parameters). Values: `DISABLED`, `TYPE_A`, `TYPE_B` |
| `org.paternostro.elkromm.emulator.phoneParameters.remoteSurveillance` | `DISABLED` | Remote surveillance (phone parameters). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.phoneParameters.voiceMessagesSendingMode` | `NONE` | Voice message sending mode (phone parameters). Values: `NONE`, `MODE_1`, `MODE_2`, `MODE_3`, `MODE_4` |
| `org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallFrequency` | `DISABLE` | Cyclic test call frequency (phone parameters). Values: `DISABLE`, `24_HOURS`, `SYSTEM_ON` |
| `org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallPhoneNumber` | `1` | Cyclic test call phone number (phone parameters) |
| `org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallHour` | `0` | Cyclic test call hour (phone parameters) |
| `org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallMinute` | `0` | Cyclic test call minute (phone parameters) |
| `org.paternostro.elkromm.emulator.phoneParameters.cyclicTestCallInterval` | `1_HOUR` | Cyclic test call interval (phone parameters). Values: `1_HOUR`, `4_HOURS`, `8_HOURS`, `12_HOURS`, `24_HOURS`, `48_HOURS`, `72_HOURS`, `96_HOURS`, `120_HOURS`, `144_HOURS`, `168_HOURS` |
| `org.paternostro.elkromm.emulator.PSTNGSM.enablePSTN` | `DISABLED` | Enable pstn (PSTN/GSM module). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.PSTNGSM.country` | `ITALY` | Country (PSTN/GSM module). Values: `ITALY`, `FRANCE`, `GERMANY`, `CZECH_REPUBLIC`, `POLAND`, `SPAIN`, `PORTUGAL`, `GREECE`, `ENGLAND` |
| `org.paternostro.elkromm.emulator.PSTNGSM.PABXLocalAccessDigit` | `DISABLE` | Pabx local access digit (PSTN/GSM module). Values: `0`, `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `DISABLE` |
| `org.paternostro.elkromm.emulator.PSTNGSM.toneControl` | `DISABLED` | Tone control (PSTN/GSM module). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.PSTNGSM.answerControl` | `DISABLED` | Answer control (PSTN/GSM module). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.PSTNGSM.PSTNLineTestFrequency` | `DISABLE` | Pstn line test frequency (PSTN/GSM module). Values: `DISABLE`, `24_HOURS`, `SYSTEM_ON` |
| `org.paternostro.elkromm.emulator.PSTNGSM.PSTNAnsweringMachineRings` | `DISABLE` | Pstn answering machine rings (PSTN/GSM module). Values: `DISABLE`, `2_RINGS`, `4_RINGS`, `8_RINGS` |
| `org.paternostro.elkromm.emulator.PSTNGSM.enableGSM` | `DISABLED` | Enable gsm (PSTN/GSM module). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.PSTNGSM.enableGSMAnsweringMachine` | `DISABLED` | Enable gsm answering machine (PSTN/GSM module). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.PSTNGSM.enableIncomingSMS` | `DISABLED` | Enable incoming sms (PSTN/GSM module). Values: `DISABLED`, `ENABLED` |
| `org.paternostro.elkromm.emulator.PSTNGSM.GSMPin` | `-1` | Gsm pin (PSTN/GSM module) |
| `org.paternostro.elkromm.emulator.PSTNGSM.expirationMonth` | `1` | Expiration month (PSTN/GSM module) |
| `org.paternostro.elkromm.emulator.PSTNGSM.expirationYear` | `0` | Expiration year (PSTN/GSM module) |
| `org.paternostro.elkromm.emulator.phoneNumbers.<N>.phoneNumber` | (empty) | Phone number of a phone number (`<N>`: 1-12) |
| `org.paternostro.elkromm.emulator.phoneNumbers.<N>.partitions` | `0` | Partitions of a phone number (`<N>`: 1-12) |
| `org.paternostro.elkromm.emulator.phoneNumbers.<N>.type` | `PSTN` | Type of a phone number (`<N>`: 1-12). Values: `PSTN`, `GSM`, `LAN` |
| `org.paternostro.elkromm.emulator.phoneNumbers.<N>.sendingMode` | `VOICE` | Sending mode of a phone number (`<N>`: 1-12). Values: `VOICE`, `IDP`, `ADF`, `MODEM`, `SMS`, `C200B` |
| `org.paternostro.elkromm.emulator.phoneNumbers.<N>.assignedEvents` | (empty) | Assigned events of a phone number (`<N>`: 1-12). Values: constants of `PhoneNumber.Event` |
| `org.paternostro.elkromm.emulator.sms.<N>` | English texts, e.g. `Burglar`, `Technical alarm 1`... | SMS text (`<N>`: 1-9) |
| `org.paternostro.elkromm.emulator.c200b.reserved` | all zeros | 100 hexadecimal digits (50 bytes; spaces, commas and colons are ignored): opaque block at the start of the C200B payload, see [PROTOCOL.md](PROTOCOL.md) |
| `org.paternostro.elkromm.emulator.c200b.event.<E>` | `255` | Event code of the C200b block. `<E>` is the event name in camel case |
| `org.paternostro.elkromm.emulator.c200b.inputCodes` | all `FF` (`0xff`, no input) | Input codes (C200b block). 128 hexadecimal digits, one byte per logical input |

In the keys, `<N>` is the (1-based, unless noted) number of the object; the other placeholders are
`<I>` the input of a keypad or reader, `first` or `second`; `<L>` the LED number of a reader;
`<D>` the day class in camel case (`workingDay`, `preHoliday`, `holiday`); `<W>` the weekday in lowercase
(`monday`...`sunday`); `<E>` the event name of the C200B block in camel case. In the defaults, `<NN>` is the number
on two digits. Enumerations are written with the name of the constant **without its prefix** (`COT_PARTITIONS`
becomes `PARTITIONS`, `IC_NOT_USED` becomes `NOT_USED`), in upper case, as listed in the Notes column; a name that
does not belong to the enumeration makes reading that property fail with an `IllegalArgumentException`, while an invalid
number or hexadecimal string is logged and replaced by the default. Names are fixed-width on the wire (24 characters): default names are shown here without their
trailing spaces. Properties holding a bitmask or a raw byte take a decimal number.

Minimal example, a single area/partition setup listening on the default port:

```properties
org.paternostro.elkromm.emulator.areas=1
org.paternostro.elkromm.emulator.area.1.name=Home
org.paternostro.elkromm.emulator.partitions=1
org.paternostro.elkromm.emulator.partition.1.name=Ground floor
org.paternostro.elkromm.emulator.partition.1.entry.time=THIRTY_SECONDS
org.paternostro.elkromm.emulator.partition.1.exit.time=THIRTY_SECONDS
```

If the file is missing entirely, the emulator still starts, using every default above.

## Round-trip integration tests

Besides the regular unit test suite (which runs against the bundled emulator), elkromm
includes a set of `mvn verify`-only integration tests that connect to a **real** panel,
read back its actual configuration, and verify that deserializing and re-serializing it
reproduces the exact same bytes. This is how several protocol quirks documented in
[PROTOCOL.md](PROTOCOL.md) were found — most notably the "ghost bit" that Elkron
panels raise on an excluded input, in a byte otherwise dedicated to static configuration.

Unlike the unit tests, these also run with mvn install/mvn verify: they need a real panel to be
reachable, so they are skipped by mvn test, which is what the CI runs — they use the Maven
Failsafe plugin (test classes named `*IT.java`, as opposed to the `*Test.java` picked up
by Surefire) instead.

### Configuration

The integration tests read an optional `elkron-it.properties` file from the classpath, at
`org/paternostro/elkromm/impl/elkron-it.properties` (i.e.
`src/test/resources/org/paternostro/elkromm/impl/` if building from source). **If the file
is missing, or any required property is empty, the integration tests are silently
skipped** rather than failing — so a plain `mvn install`/`mvn test` on a fresh clone is
never affected, and no personal panel configuration needs to be committed to run the
regular build.

| Property | Required | Notes |
|---|---|---|
| `org.paternostro.elkromm.impl.IT.host` | Yes | Hostname/IP of the panel's LAN expansion board |
| `org.paternostro.elkromm.impl.IT.port` | Yes | TCP port (usually `8030`) |
| `org.paternostro.elkromm.impl.IT.plantCode` | Yes | Installer/plant identification code |
| `org.paternostro.elkromm.impl.IT.technicalPin` | Yes | Installer's technical access code |

Example:

```properties
org.paternostro.elkromm.impl.IT.host=192.168.1.100
org.paternostro.elkromm.impl.IT.port=8030
org.paternostro.elkromm.impl.IT.plantCode=12345678
org.paternostro.elkromm.impl.IT.technicalPin=123456
```

### Running

```
mvn verify
```

runs the full suite (unit tests, then integration tests, then checks the results). To run
only the integration tests, without repeating the unit tests:

```
mvn failsafe:integration-test
```

**This connects to and reads from a real, live alarm panel.** No configuration is ever
written back — the tests only read and compare — but running them requires the panel to
be reachable and its access codes to be known; use only against a system you own or are
authorized to access (see [Disclaimer](#disclaimer)).

## Compile

```
mvn install
```

## Documentation

The public API is documented with Javadoc. Generate it locally with:

```
mvn javadoc:javadoc
```

The generated HTML is written to an `apidocs` folder under `target` (with the current version of
the Maven Javadoc plugin, `target/reports/apidocs`; the exact location depends on the plugin version).

## Roadmap

* add log access methods
* implementation of event reception
* publish the Wireshark dissector (currently a single, undocumented Lua script)


## Credits

* Anthropic Claude for Javadocs, README and PROTOCOL review, CHANGELOG
