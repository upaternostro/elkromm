# Changelog

All notable changes to this project are documented in this file. Entries are grouped
following the [Keep a Changelog](https://keepachangelog.com/) convention (Added / Changed
/ Fixed), summarized from the commit history for readability.

> **A bit of history.** `elkromm` began as a low-level package inside `Elkroid`, an early,
> native Android app talking directly to the panel's LAN interface — raw stream reads/writes,
> almost no abstraction, a handful of DTOs added only where strictly needed. `getInputStatus()`'s
> map-based return type traces back to that code. In parallel, `elkrtest` (never published, now
> retired) is where the real restructuring happened: proper DTOs, packets and serializers, built
> from scratch. Once `elkrtest` matured, the `elkromm` package was spun out of `Elkroid` into its
> own library and merged with `elkrtest`'s code — `ElkrommPacket` and `ElkronCommand` came along
> this way, and so did `ClientConnection`, later repurposed as the library's bundled emulator once
> `mock-ipc` (built to unit-test the new library without manually spawning separate processes) made
> talking to it efficient.
> Even before (summer 2016) the project spawn as a Go PoC.

## [Unreleased] — v0.5

### Added
- `toString()` methods on DTOs, for easier debugging/logging
- Iterator-style accessors (iterator, stream, parallelStream, listIterator, spliterator)
  for the Areas, Partitions, Inputs and Outputs collections
- `getRawInputStatus()` API, returning the raw per-input status byte array
- Full Javadoc across the library's public API, `impl`, `dto` and `serializer` packages (the
  latter including the on-the-wire payload structure handled by each serializer), plus
  `package-info.java` for every package
- `CHANGELOG.md` (this file), a substantially expanded `PROTOCOL-ITA.md` and its English
  translation `PROTOCOL.md` (now the primary protocol reference), both including a
  byte-level payload table for every command, verified against the serializers rather than
  deduced from captures alone
- `setUser()`/`setKey()` on `ElkrommFacade`, to write a single user/key credential
  (`USER_PROGRAMMING`/`KEY_PROGRAMMING`), previously implemented at the protocol level but
  unreachable from the public API (fixes #4)
- Optional round-trip integration test suite (`mvn verify`, Maven Failsafe), connecting to
  a real panel to verify that deserializing and re-serializing its actual configuration
  reproduces the exact same bytes — see the README for setup
- `ElkrommFacade.setDelay()`, to tune the pause the library inserts while talking to the
  panel (`DEFAULT_DELAY`, 100 ms, was previously a fixed constant); `0` gives maximum speed
  and is what the unit tests use against the emulator
- `ElkrommFacade.DEFAULT_NAME` (placeholder name of unused entries) and
  `ElkrommFacade.MAX_LOGICAL_OUTPUTS` constants
- Emulator: inputs and outputs are now configurable through properties
  (`org.paternostro.elkron.input.<N>.*` and `org.paternostro.elkron.output.<N>.*`), see the
  README
- `ElkrommUtils.CHECKSUM_SIZE`, `getBlockChecksum()` and `setBlockChecksum()`, replacing the
  literal `4` and the open-coded checksum access scattered across serializers, packets and
  emulator
- `PAYLOAD_SIZE` constant in each fixed-size serializer (`Expansions` defines
  `EXPANSION_PAYLOAD_SIZE` for a single expansion), the size in bytes of the serialized payload
- Public offset constants (`*_OFFSET`) in the serializers, one for each field of the payload,
  linked from a new "Constant" column in the Javadoc payload tables; each constant is derived
  from the previous fields and from the sizes involved wherever the layout allows it.
  Also `ElkrommFacade.VERSION_LENGTH`, the length of the firmware version of expansions and keypads

### Fixed
- Accept a zero phone number as a valid test-call number when cyclic test calls are disabled
- Use `-1` (`0xffffff`) to represent "no GSM PIN configured", instead of an invalid sentinel value
- Flush the output stream after each write, avoiding unnecessary delays
- `PSTNGSM.setEnableIncomingSMS()` validating the wrong field due to a copy-paste error (fixes #1)
- Incorrect handling of `Input.Sensitivity` (fixes #6)
- Block checksum verification failing on `EXPANSIONS`/`KEYPADS`, caused by two
  previously-undocumented behaviors of the physical panel: a couple of trailing,
  panel-computed bytes per expansion record never covered by the checksum, and a "ghost"
  bit (`0x10`) that the panel raises on an input's configuration byte when that input is
  currently excluded — live status living inside an otherwise-static configuration byte,
  identical in value to `ElkrommFacade.InputStatus.IS_EXCLUDED`. The same fix was applied,
  by analogy and untested, to `READERS` (kept as a logged warning rather than a hard
  failure, pending confirmation on real hardware) (fixes #8)
- `PhoneNumbersSendingCodes` writing the "partitions/system on-off" event's assignment bits
  to the wrong offset, found by the new round-trip test suite
- `Keyboards` serializer now uses `Keyboard` serializer to transform each keyboard
- Emulator: the number of areas and partitions is now taken from its configuration, instead of
  being inferred from the size of the lists (see the `AreasAndPartitions` change below)
- Emulator: the default entry/exit time of partition 2 was 30 seconds, unlike all the other
  partitions; it is now 0

### Changed
- **Breaking:** `ElkrommFacade.MAX_EXP_OUTPUTS` is now 3, the number of outputs of a single
  expansion board. The new `ElkrommFacade.MAX_PANEL_OUTPUTS` (6) is the number of outputs of the
  main unit, which is also expansion 0. The payload record of an expansion is unchanged: it
  always has 6 output slots (`Expansions.EXPANSION_OUTPUTS_OFFSET`)
- Emulator: expansions mimic the real hardware, with 6 outputs on expansion 0 (embedded in the
  panel) and 3 on each expansion board; their default names are now `UC` and `EP <nn>`
- Removed `ordinal` from `Credential`: redundant with the position in its containing array,
  and with the index carried explicitly by `SingleCredential` for single-instance writes (fixes #7)
- `Input` deserialization no longer skips "not used" slots: the panel does not reliably
  reset their other fields (in particular `Specialization`, observed to retain the value
  from the input's last real configuration), so the only way to preserve round-trip
  fidelity is to keep whatever is actually there
- `AreasAndPartitions` now tracks the actual number of areas/partitions in dedicated
  fields, rather than inferring it from list size — a consequence of no longer discarding
  unused slots
- Updated the `mock-ipc` dependency version
- Renamed `ElkronCommand.CONTROL_PANEL_PROGRAMMING` → `EXPANSION_PROGRAMMING` and
  `KEYBOARD_PROGRAMMING` → `KEYPADS_PROGRAMMING`, to match what those commands were found
  to actually do (single-expansion and all-keypads programming, respectively)
- Moved `DEFAULT_DELAY` from `ElkrommFacadeImpl` to `ElkrommFacade`, next to `setDelay()`
- Consolidated per-record payload size constants, previously duplicated across several
  `packet`/`serializer` classes, into a `PAYLOAD_SIZE` constant defined by the serializer each
  payload belongs to (`serializer.SerializersConstants` only keeps `COMMAND_LENGTH`)
- Factored the ghost-bit patching logic, previously duplicated across the round-trip test
  cases, into a shared helper
- **Breaking:** "sector" renamed to "partition" throughout the library, to match the English
  terminology of Hi-Connect (the panel itself, in Italian, says "settore"):
  `ElkrommFacade.armDisarmSector()`/`armDisarmSectors()` → `armDisarmPartition()`/
  `armDisarmPartitions()`, `ElkronCommand.ARM_DISARM_SECTOR` → `ARM_DISARM_PARTITION`,
  `packet.ArmDisarmSector` → `packet.ArmDisarmPartition`, `Command.ObjectType.COT_SECTORS` →
  `COT_PARTITIONS` and `Input.Flags.IF_OR_SECTORS` → `IF_OR_PARTITIONS`
- **Breaking (emulator):** the emulator's properties follow the same rename:
  `org.paternostro.elkron.sectors` → `partitions`, `sector.<N>.*` → `partition.<N>.*` and
  `area.<N>.sectors` → `area.<N>.partitions` (and the matching `emulator.Config` constants and
  accessors); an existing `elkron.properties` file must be updated
- `ElkrommUtils.dumpPayload()` prints only ASCII characters in its text column
- Factored the emulator's configuration handling (`emulator.Config`), removing a large amount
  of duplicated code
- **Breaking:** `ElkrommSerializer.length()` removed: it was only used internally to allocate
  the payload, and serializers with a variable payload (`Expansions`, `Keyboards`,
  `PeripheralUnits`, `Readers`) could not implement it, throwing `UnsupportedOperationException`.
  Custom serializers plugged in through `ElkrommFactory` must drop their `length()`
  implementation (and the `@Override` on it)
- **Breaking:** `SerializersConstants.C200B_INPUT_CODES_OFFSET` moved to
  `C200bParameters.INPUT_CODES_OFFSET`
- Serializers use named constants (offsets, `ElkrommFacade.NAME_LENGTH`, `ElkrommFacade.VERSION_LENGTH`,
  `ElkrommUtils.CHECKSUM_SIZE`, `MAX_*`) instead of numeric literals; no change in behavior
- Offsets in `PROTOCOL.md`, `PROTOCOL-ITA.md` and in the Javadoc payload tables are now
  hexadecimal (convention stated in the introduction of the protocol documents)

## [0.4] — 2026-07-20

### Added
- Panel emulator (`org.paternostro.elkromm.emulator`), integrated into the unit test suite
- `KEYPAD_PROGRAMMING` command support in the emulator
- Additional tests covering the v0.3 API, including setter methods

### Fixed
- `SetAreasAndPartitions`, `SetC200bParameters`, `SetKeys`, `SetPhoneNumbers` and `SetSMS`
  packets exceeding the 140-byte payload limit
- `PSTNGSM` serializer computing BCD length from byte size rather than digit count
- IP addresses now handled correctly as LAN-type phone numbers
- User checksum not being updated when modifying users

### Changed
- Moved shared test input-building logic into a dedicated test utility class

## [0.3] — 2026-07-17

### Added
- New serializer-based API surface on `ElkrommFacade`, replacing direct packet handling
- Exception handling throughout the facade (`ElkrommException`)
- User/input enabling model and serializer, wired to enable/disable users and inputs
- Key specialization handling
- Single-instance user & key programming support (`SingleCredential`); array serializers
  reworked to build on the single-instance ones internally
- Single keypad model and serializer (`SingleKeyboard`)
- Login model and serializer
- `UserEnablings` model and serializer
- `ElkrommPacket`, carried over from `elkrtest` (see the history note above)
- `Serializable` implemented on all DTOs

### Changed
- `ElkrommFacade.Partition` reworked as a proper bitmask
- `ElkrommFacade.InputStatus` marked as a bitmask, with missing values added
- `ElkrommFactory` refactored to allocate a default serializer
- De-escaping of control bytes (SYN, ACK, ...) restricted to packet data only
- `Command` DTO now accepts zero as a valid "disabled" object reference

### Fixed
- Several packet/serializer issues found while wiring up the new API (including
  `SetParametersEnablings` and a visibility typo in `getSerializer`)

## [0.2] — 2026-07-03

### Added
- Serializer layer (`ElkrommSerializer`) for converting DTOs to/from byte payloads
- `ElkronCommand`, carried over from `elkrtest` (see the history note above)
- DTOs and serializers for: `SystemStatus`, `Input`/`Output`, `Keyboard`, `Reader`,
  `ParametersEnablings` (system parameters), `Command`/`DayClassCommands`/`TimeProgrammer`,
  `PhoneParameters`, `PSTNGSM`, `PhoneNumber`/`PhoneNumbersSendingCodes`, `SMS`,
  `C200bParameters`
- `CONTROL_PANEL_PROGRAMMING`, `KEYPAD_PROGRAMMING`, `EXPANSIONS_PROGRAMMING`,
  `KEYBOARD_PROGRAMMING` and `KEYPADS` commands
- Factory-level tests

### Changed
- Enums refactored to expose a consistent `valueOf` method
- BCD helper functions moved into `ElkrommUtils`
- `ElkrommUtils.dumpPayload` now takes an `ElkronCommand` parameter and logs via SLF4J
  instead of `System.out`
- `Input`/`Output` serializers renamed to their singular form

### Fixed
- Zero address on an expansion now correctly treated as the central unit
- Enum `isValid()` methods

## [0.1] — 2026-06-16

### Added
- Initial import of the project
- Core `ElkrommFacade` API and implementation
- Plant code handling in message header bytes 1 & 2
- Initial test suite

### Changed
- Constants moved from `ElkrommFacadeImpl` to `ElkrommFacade`
- Initialization separated from the constructor in `ElkrommFacadeImpl`, with the
  corresponding getter renamed for clarity
