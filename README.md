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
- Protocol reference documentation ([PROTOCOL-ITA.md](PROTOCOL-ITA.md), Italian)

## Requirements

- Java 8 or later
- Maven
- [mock-ipc](https://github.com/upaternostro/mock-ipc), a small socket-abstraction library
  by the same author — not published to a public Maven repository, so it must be built and
  `mvn install`ed locally before building elkromm
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
ElkrommFacade facade = ElkrommFactory.getFactory()
    .getElkrommFacade(InetAddress.getByName("192.168.1.100"), 8030, plantCode);

facade.connect();
facade.login(plantCode, technicalCode);

SystemStatus status = facade.getSystemStatus();
facade.armDisarmSector(ElkrommFacade.Partition.P_ONE, true);

facade.logout();
facade.disconnect();
```

The `getElkrommFacade(...)` factory call above can itself throw `IOException` (it calls
`init()` internally before returning); every other method shown can throw
`ElkrommException` (protocol/communication errors). Omitted here for brevity — production
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

The emulator reads an optional `elkron.properties` file from the classpath, at
`org/paternostro/elkromm/emulator/elkron.properties` (i.e. drop it under
`src/main/resources/org/paternostro/elkromm/emulator/` if building from source). Any key
not present falls back to its default value below. Note the property key prefix is
`org.paternostro.elkron.` (without elkromm's double "m").

| Property | Default | Notes |
|---|---|---|
| `org.paternostro.elkron.port` | `8030` | TCP port the emulator listens on |
| `org.paternostro.elkron.plant.code` | `55555555` | Plant code expected at login |
| `org.paternostro.elkron.technical.code` | `000000` | Technical/installer code expected at login |
| `org.paternostro.elkron.keyboards` | `0` | Number of emulated keypads |
| `org.paternostro.elkron.readers` | `0` | Number of emulated proximity readers |
| `org.paternostro.elkron.expansions` | `0` | Number of emulated expansion units |
| `org.paternostro.elkron.areas` | `0` | Number of emulated areas (0-4), zero means no areas |
| `org.paternostro.elkron.area.<N>.sectors` | `1` for area 1, `0` otherwise | Partitions belonging to area `<N>` (1-4), expressed as bitmask |
| `org.paternostro.elkron.area.<N>.name` | `...` | Display name of area `<N>` |
| `org.paternostro.elkron.sectors` | `1` | Number of emulated partitions (1-8) |
| `org.paternostro.elkron.sector.<N>.entry.time` | `30` for sector 2, `0` otherwise | Entry delay, in seconds, for partition `<N>` (1-8) |
| `org.paternostro.elkron.sector.<N>.exit.time` | `30` for sector 2, `0` otherwise | Exit delay, in seconds, for partition `<N>` (1-8) |
| `org.paternostro.elkron.sector.<N>.name` | `...` | Display name of partition `<N>` |
| `org.paternostro.elkron.sector.<N>.type` | `STANDARD` | Partition behavior type: `STANDARD`, `SELF_EXCLUSION`, or `ARMING_BLOCK` |

Minimal example, a single area/partition setup listening on the default port:

```properties
org.paternostro.elkron.areas=1
org.paternostro.elkron.area.1.name=Home
org.paternostro.elkron.sectors=1
org.paternostro.elkron.sector.1.name=Ground floor
org.paternostro.elkron.sector.1.entry.time=30
org.paternostro.elkron.sector.1.exit.time=30
```

If the file is missing entirely, the emulator still starts, using every default above.

## Round-trip integration tests

Besides the regular unit test suite (which runs against the bundled emulator), elkromm
includes a set of `mvn verify`-only integration tests that connect to a **real** panel,
read back its actual configuration, and verify that deserializing and re-serializing it
reproduces the exact same bytes. This is how several protocol quirks documented in
[PROTOCOL-ITA.md](PROTOCOL-ITA.md) were found — most notably the "ghost bit" that Elkron
panels raise on an excluded input, in a byte otherwise dedicated to static configuration.

Unlike the unit tests, these are **not** run by `mvn install`/`mvn test`, precisely
because they require a real panel to be reachable on the network — they use the Maven
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

Build `mock-ipc` and install it locally first (see Requirements), then:

```
mvn install
```

## Documentation

The public API is documented with Javadoc. Generate it locally with:

```
mvn javadoc:javadoc
```

The generated HTML is written to `target/site/apidocs`.

## Roadmap

* add log access methods
* implementation of event reception
* publish the Wireshark dissector (currently a single, undocumented Lua script)


## Credits

* Anthropic Claude for Javadocs, README and PROTOCOL review, CHANGELOG
