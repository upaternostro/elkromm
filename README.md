# elkromm

A Java library that manages Elkron's alarm protocol.

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

## Requirements

- Java 8 or later
- Maven
- Elkron MP-508 v3 alarm system equipped with a LAN expansion board

## License

This software is licensed under EUPL v1.2 or above. See [LICENSE.md](LICENSE.md) for full license text.

## Usage

The main entry point of this library is the `ElkrommFacade` interface, that exposes all the methods this library manages.

An instance of a concrete class implementing the `ElkrommFacade` interface can be obtained through the `ElkrommFactory` class.

## Compile

Compile the library using Maven with:

```
mvn install
```

## Roadmap

* add log access methods
* implementation of event reception