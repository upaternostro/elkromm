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

## [Unreleased] — v0.5

### Added
- `toString()` methods on DTOs, for easier debugging/logging
- Iterator-style accessors (iterator, stream, parallelStream, listIterator, spliterator)
  for the Areas, Partitions, Inputs and Outputs collections
- `getRawInputStatus()` API, returning the raw per-input status byte array

### Fixed
- Accept a zero phone number as a valid test-call number when cyclic test calls are disabled
- Use `-1` (`0xffffff`) to represent "no GSM PIN configured", instead of an invalid sentinel value
- Flush the output stream after each write, avoiding unnecessary delays

### Changed
- Updated the `mock-ipc` dependency version
- Documentation updates (README, `PROTOCOL-ITA.md`, Javadoc)

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
