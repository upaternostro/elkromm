# Elkron Hi-Connect protocol analysis

## Index

* [Foreword](#foreword)
* [Framing and commands](#framing-and-commands)
* [Command types](#command-types)
* [Recognized commands](#recognized-commands)
* [Partitions](#partitions) aka "Areas and partitions"
* [Users](#users)
* [Keys](#keys)
* [Parameters](#parameters)
* [Phone numbers](#phone-numbers)
* [Phone parameters](#phone-parameters)
* [PSTN GSM](#pstn-gsm)
* [SMS](#sms)
* [C200B](#c200b) aka "Events"
* [Time programmer and Day class commands](#time-programmer-and-day-class-commands)
* [Block B](#block-b) aka "Expansions" aka "Nodes"
* [Keypads](#keypads)
* [Readers](#readers)
* [Keypad programming](#keypad-programming)
* [Credits](#credits)

## Foreword

This study is based on the following requirements:

* Hi-Connect v3.30 / 3.70 in use;
* an MP-508 v03.01 control panel;
* plant code 55555555 (default);
* installer code 000000 (default);
* direct IP connection.

**Conventions**: every offset is 0-based and expressed in hexadecimal (`0x` prefix, lowercase); sizes and counts are decimal. The only exception are the offsets of the captured streams (*TX Offset* / *RX Offset* columns), which are stream positions printed as 8 hexadecimal digits, as in a dump.

## Framing and commands

Communication happens over TCP port 8030.  
When opening the system for management, Hi-Connect opens a TCP socket to the given IP/port.  
The conversation is detailed below (net of the usual TCP-level packets).

Keep in mind the meaning of the following ASCII codes:  
0x01: SOH (start of heading)  
0x03: ETX (end of text)  
0x06: ACK (acknowledge)  
0x11: DC1 (device control 1, used to escape 0x03 in panel → Hi-Connect communication within configuration blocks; not counted in block checksums, packet checksums, or data lengths — evidently added right before shooting the packet out on the wire. WARNING: in Hi-Connect → panel communication, both 0x03 and 0x01 must be escaped, or the panel replies with NAK)  
0x15: NAK (negative acknowledge)  
0x16: SYN (synchronous idle)

The conversation always originates from the Hi-Connect software; the panel is a slave and replies to received commands. Since the panel's hardware isn't particularly performant, it's a good idea to introduce delays (sleeps of a few hundred milliseconds) between sending and attempting to receive, or otherwise handle the possible absence of data.

Packets can be split into two types:

1. packets containing no data;
2. packets containing data.

Both can be sent by either party, depending on the needs of the conversation at that point. A party can also send two consecutive packets.

Packets with no data are typically made up of a single byte. They're mostly observed in the panel's replies to the Hi-Connect software. The following such packets have been observed:

* 0x06 (ACK)
* 0x15 (NAK)
* 0x16 (SYN)

Data-carrying packets are structured as follows:  
` `  

Offset	| Value	| Meaning
--------|---------------|------------
0x00	| 0x01		| SOH
0x01	| 0x55		| First two BCD digits of the plant code
0x02	| 0x55		| Third and fourth BCD digit of the plant code
0x03	| (variable)	| number of packets making up the sequence (0-based)
0x04	| (variable)	| this packet's index within the sequence (0-based)
0x05	| (variable)	| length of the data being sent (max 140 (0x8c) data bytes, net of escaping)
0x06	| 0x00		| ??? constant?
0x07	| (variable)	| command
0x08 → n	| (variable)	| transmitted data (can be absent if the byte at position 0x05 is zero)
n+1 → n+2	| (variable)	| big-endian packet checksum, computed so that the sum of every byte from offset 0x01 to n (i.e. excluding the initial SOH), added to this checksum, yields 0x10000. In other words, the checksum is `0x10000 – SUM(offset1... offsetn)`
n+3	| 0x03		| ETX

Note: bytes 0x03 and 0x04 are both 0x00 if the reply fits in a single packet (max 140 (0x8c) data bytes, net of escaping). If instead the data to transmit exceeds that maximum, the reply is split across multiple packets: byte 0x03 is set to the highest packet index, and byte 0x04 to the current packet's index (0-based). In other words, if the reply is split into 3 packets, byte 0x03 is 0x02, and byte 0x04 is respectively 0x00, 0x01 and 0x02 for the first, second and third packet. The receiver acknowledges each fragment with a 0x65 (SEND) packet.

## Command types

Excluding single-byte control packets (SYN/ACK/NAK), application-level commands fall into the following types:

* **Session control**: opening, keeping alive, and closing the application-level connection (HELLO, LOGIN, SEND, LOGOUT); doesn't read or write configuration data.
* **Read**: the client sends the code with no data (barring any request parameters), the panel replies with SYN followed by the first data packet under the same command code, the client sends SEND and the panel replies with SYN followed by the second data packet if present, and so on.
* **Action**: the client sends the code with a few data bytes representing a specific, immediate command (e.g. arming a partition, excluding an input, enabling a user); the panel replies with SYN only, without returning a data packet.
* **Write**: the client sends the command code with the entire data block, split into packets if the data size exceeds 140 bytes (structurally analogous to what the corresponding read returns); the panel replies with SYN and the client proceeds with the next packet, and so on.
* **Single-instance write**: the client sends the command code with the index of the element within its own array (e.g. the i-th user, keypad, reader, key, SMS, day class) followed only by that instance's data; the panel replies with SYN. Unlike a block write, this doesn't require retransmitting the whole array, and there's no block checksum. Note: some objects (e.g. [Expansions](#block-b)) carry the index within themselves, so no separate index is prepended to the packet.


## Recognized commands

Command	| Type	| Meaning	| Data		| Panel reply	| Notes
--------|-------|---------------|---------------|-----------------------|-------
0x60	| Session control	| HELLO		| none	| 0x06 (ACK)	
0x49	| Session control	| LOGIN		| plant code and installer code in BCD (4 + 3 bytes respectively)	| 0x06 (ACK)	
0x65	| Session control	| SEND		| none	| 0x16 (SYN) if it has nothing to send	| Hp: data-send request
 |	|	| 		| or		| or
 |	|	| 		| 0x16 (SYN) + next data packet		| data packets after the first
 |	|	|		|		| hypothesis: events?
0x62	| Read	| ADDRESSES	| none	| 0x16 (SYN) + data packet 0x62 containing the number of keypads (1 byte), their addresses (1 byte each), the number of readers (1 byte), their addresses (1 byte each), the number of expansions (1 byte) and their addresses (1 byte each)	| peripheral unit addresses
0x50	| Read	| CHECKSUM	| none	| 0x16 (SYN) + data packet 0x50, 13 big-endian 32-bit long words of checksum data, respectively: nodes, keypads, readers, system, time programmer, areas/partitions, phone parameters, phone numbers, events, sms, pstn gsm, users, keys. Note: the checksums are also contained in their respective payloads.	| Hi-Connect uses this command to quickly detect misalignments between panel config and the application copy.
0x84	| Read	| INPUT STATUS	| none	| 0x16 (SYN) + data packet 0x84 with one byte per active input (in address order?). The byte is 0x00 if the input is closed, 0x02 if the input is open, 0x10 if the input is excluded, 0x04 for alarm memory, ...	| See ElkrommFacade.InpuStatus
0x63	| Session control	| LOGOUT	| none	| 0x16 (SYN)	
0x55	| Read	| AREAS & PARTITIONS	| none	| 0x16 (SYN) + data packet 0x55, see [Partitions](#partitions)	| This and the next 3 commands (USERS, KEYS, PARAMETERS & ENABLINGS) cover "block A", which unlike "block B" doesn't exist as a single command
0x5b	| Read	| USERS	| none	| 0x16 (SYN) + data packet 0x5b, see [Users](#users)	
0x5c	| Read	| KEYS	| none	| 0x16 (SYN) + data packet 0x5c, see [Keys](#keys)	
0x26	| Read	| PARAMETERS & ENABLINGS	| none	| 0x16 (SYN) + data packet 0x84, see [Parameters](#parameters)	
0x80	| Read	| SYSTEM STATUS	| none	| 0x16 (SYN) + data packet 0x80 with a single data byte, 1 bit per partition, LSB = partition 1	
0x81	| Action	| ARM/DISARM SYSTEM	| two data bytes, 1 bit per partition, LSB = partition 1; when arming, the two bytes are identical (e.g. 0x02 + 0x02 to arm partition 2), while when disarming the first indicates the partition and the second is 0x00. Multiple partitions can be armed/disarmed at once by OR-ing together the bits representing them, e.g. 0xFF 0x00 to disarm everything.	| 0x16 (SYN)	
0x96	| Write	| WRITE PARAMETERS & ENABLINGS	| see 0x26 data, [Parameters](#parameters). Watch out: 0x11 escapes 0x01 too	| 0x16 (SYN)	
0x95	| Single-instance write	| USER PROGRAMMING	| user progressive incremented by 1 + areas + partitions + name (24 bytes)	| 0x16 (SYN)
0xe7	| Write	| EDIT PHONE NUMBERS	| data packet 0xe7, see [Phone numbers](#phone-numbers)	| 0x16 (SYN)	
0x51	| Read	| EXPANSIONS	| none	| 0x16 (SYN) + data packet 0x51, see [Block B](#block-b)	| Aka Block B aka Nodes
0x83	| Action	| EXCLUDE/INCLUDE INPUT	| two data bytes, the first indicates the input number, the second is 0x01 (watch out for escaping) to exclude the input, 0x00 to include it	| 0x16 (SYN)	| WARNING: here the semantic is reversed as 0x01 = TRUE means EXCLUDE, so deactivate the input, while 0x00 = FALSE means INCLUDE
0x87	| Read	| USER STATUS	| none	| 0x16 (SYN) + data packet 0x87 containing 4 data bytes with flags indicating user activation. The first byte's MSB (0x80) holds user 0's status (TECNICO/technician), the next bit (0x40) user 1's status (MASTER), and so on for the other bits. The second byte holds the status of users 8-15, the third 16-23, and the last 24-31	
0x88	| Action	| ENABLE/DISABLE USER	| two data bytes, the first indicates the user number (base 1 = TECNICO), the second is 0x01 (watch out for escaping) to enable the user, 0x00 to disable it	| 0x16 (SYN)	
0x57	| Read	| PHONE NUMBERS	| none	| 0x16 (SYN) + data packet 0x57, see [Phone numbers](#phone-numbers)
0x56	| Read	| PHONE PARAMETERS	| none	| 0x16 (SYN) + data packet 0x56, see [Phone parameters](#phone-parameters)
0x5a	| Read	| PSTN GSM	| none	| 0x16 (SYN) + data packet 0x5a, see [PSTN GSM](#pstn-gsm)
0x59	| Read	| SMS	| none	| 0x16 (SYN) + data packet 0x59, see [SMS](#sms)	| see also 0xa0 SMS PROGRAMMING for writing a single SMS
0x58	| Read	| C200B	| none	| 0x16 (SYN) + data packet 0x58, see [C200B](#c200b)
0x54	| Read	| TIME PROGRAMMER	| none	| 0x16 (SYN) + data packet 0x54, see [Time programmer and Day class commands](#time-programmer-and-day-class-commands)
0x8b	| Read (hypothesis)	| KEY STATUS	| TBD	| TBD	| packetClass not implemented (FIXME in the code); hypothesis based on naming analogy with INPUT/SYSTEM/USER STATUS
0x70	| Read (hypothesis)	| EVENT LOG	| TBD	| TBD	| packetClass not implemented (FIXME in the code) — log reading
0x91	| Single-instance write	| EXPANSION PROGRAMMING	| TBD	| TBD	| packetClass not implemented (FIXME in the code)
0xe1	| Single-instance write (hypothesis)	| EXPANSIONS PROGRAMMING	| TBD	| TBD	| packetClass not implemented (FIXME in the code); hypothesis based on the "PROGRAMMING" naming analogy
0xe2	| Write	| KEYPADS PROGRAMMING	| TBD	| TBD	| packetClass not implemented (FIXME in the code)
0xe4	| Write	| SET TIME PROGRAMMER	| 131-byte packet, see [Time programmer and Day class commands](#time-programmer-and-day-class-commands)	| 0x16 (SYN)	| packetClass: `SetTimeProgrammer`
0xe5	| Write	| SET PARTITIONS AND AREAS	| TBD	| TBD	| packetClass: `SetAreasAndPartitions`, never documented
0xe6	| Write	| SET PHONE PARAMETERS	| 20-byte packet, see [Phone parameters](#phone-parameters)	| 0x16 (SYN)	| packetClass: `SetPhoneParameters`
0xe8	| Write	| SET C200B	| 168-byte packet, see [C200B](#c200b)	| 0x16 (SYN)	| packetClass: `SetC200bParameters`
0xe9	| Write	| SET SMS	| TBD	| TBD	| packetClass: `SetSMS`, never documented
0xea	| Write	| SET PSTN GSM	| 21-byte packet, see [PSTN GSM](#pstn-gsm)	| 0x16 (SYN)	| packetClass: `SetPSTNGSM`
0xeb	| Write	| SET USERS	| TBD	| TBD	| packetClass: `SetUsers`, never documented
0xec	| Write	| SET KEYS	| TBD	| TBD	| packetClass: `SetKeys`, never documented
0xa0	| Single-instance write	| SMS PROGRAMMING	| `SMSIndex` index + ASCII text, see [SMS](#sms)	| 0x16 (SYN)	| packetClass: `SMSProgramming`. Confirmed via `ElkrommFacadeImpl.setSMS()`, DTO `SingleSMS`(index: `SMSIndex`, `SMS`), fully reachable from the public facade
0x92	| Single-instance write	| KEYPAD PROGRAMMING	| TBD	| TBD	| packetClass: `KeypadProgramming`, see [Keypad programming](#keypad-programming) (data present, command code not yet linked in this document). Confirmed via `ElkrommFacadeImpl.setKeyboard()`, DTO `SingleKeyboard`(index, `Keyboard`), fully reachable from the public facade
0x52	| Read	| KEYPADS	| TBD	| TBD	| packetClass: `Keypads`, see [Keypads](#keypads) (data present, command code not yet linked)
0x53	| Read	| READERS	| TBD	| TBD	| packetClass: `Readers`, never documented
0x93	| Single-instance write	| READER PROGRAMMING	| TBD	| TBD	| packetClass: `Reader`, never documented. Reachable via `ElkrommFacadeImpl.setReader()`, but marked `// FIXME: da provare!?!?` (to be tested) in the code — implemented but **unverified**. Note: here the index is implicit in the `Reader` DTO's own `address` field, there's no separate `SingleReader` wrapper
0xa3	| Single-instance write	| KEY PROGRAMMING	| index + `Key` data (name, specialization)	| 0x16 (SYN)	| packetClass: `KeyProgramming`. Confirmed via DTO `SingleCredential`(index, `Key`)
0xe3	| Write	| SET READERS	| TBD	| TBD	| packetClass: `SetReaders`, never documented
0xa1	| Single-instance write	| DAY CLASS CMDS	| `Command[]` list for the day class, see [Time programmer and Day class commands](#time-programmer-and-day-class-commands)	| 0x16 (SYN)	| packetClass: `DayClassCommands`. Confirmed via `ElkrommFacadeImpl.setDayClassCommands()`, fully reachable from the public facade — here the "instance" is the day class itself (`DayClass`: WORKING_DAY/PRE_HOLIDAY/HOLIDAY), not a numeric array index



The panel replies with 0x06 (ACK) only until login has been performed, including the login packet itself. Hi-Connect then sends a SEND after login to verify the reply is 0x16 (SYN). The whole login procedure thus consists of three packets: HELLO, LOGIN, SEND. Sending LOGIN without HELLO gets you a 0x15 (NAK) in reply.

The Hi-Connect software keeps sending SEND packets even while idle (one every 500 ms). From empirical attempts, it's a good idea to keep sending these packets, otherwise at some point the panel starts replying NAK even to otherwise correct packets. Hp.: keepalive? Note: if the login sequence is performed before every command, followed by logout, keepalives aren't necessary and the socket can even be closed and reopened for the next command. This obviously slows down interaction, but guarantees there's no misalignment of any kind between panel and client (which has been observed despite keepalives).

The panel replies with 0x15 (NAK) if the packet sent by Hi-Connect contains a wrong checksum, or in the face of other errors (e.g. login not performed, wrong login sequence).

Captured stream (simple session with connection and input status refresh):

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
00000000	| 01 55 55 00 00 00 00 60 fe f6 03	| 	| 	| HELLO
	| 	| 00000000	| 06	| ACK
0000000B	| 01 55 55 00 00 07 00 49 **55 55 55 55 00 00 00** fd b2 03	| 	| 	| LOGIN
	| 	| 00000001	| 06	| ACK
0000001D	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000002	| 16	| SYN
00000028	| 01 55 55 00 00 00 00 62 fe f4 03	| 	| 	| ADDRESSES
	| 	| 00000003	| 16	| SYN
	| 	| 00000004	| 01 55 55 00 00 06 00 62 **01 01 00 02 01 02** fe e7 03	| ADDRESSES
00000033	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000015	| 16	| SYN
0000003E	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000016	| 16	| SYN
00000049	| 01 55 55 00 00 00 00 50 ff 06 03	| 	| 	| CHECKSUM
	| 	| 00000017	| 16	| SYN
	|	|  00000018	| 01 55 55 00 00 34 00 50 **ff ff 44 48 ff ff f1 64 00 00 00 00 ff ff fd 8e ff ff ff be ff ff df fb ff ff fe 27 ff ff 2b b5 ff ff 78 86 ff fe d0 49 ff ff fb eb ff ff 94 93 ff ff 9a a0** d7 85 03	| CHECKSUM
00000054	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000057	| 16	| SYN
0000005F	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000058	| 16	| SYN
0000006A	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000059	| 16	| SYN
00000075	| 01 55 55 00 00 00 00 84 fe d2 03	| 	| 	| INPUT STATUS
	| 	| 0000005A	| 16	| SYN
	| 	| 0000005B	| 01 55 55 00 00 16 00 84 00 **00 00 00 10 04 02 02 02 02 02 02 02 02 02 02 00 00 00 00 00 00** fe 94 03                          	| INPUT STATUS
00000080	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 0000006F	| 16	| SYN
*...omitted...* | *(series of SEND)*
000001B4	| 01 55 55 00 00 00 00 63 fe f3 03	| 	| 	| LOGOUT
	| 	| 0000008B	| 16	| SYN

## Partitions

Reports the data for (optional) areas and partitions (aka sectors).

### Stream capture

Stream excerpt for reading partitions:  

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000000C6	| 01 55 55 00 00 00 00 55 ff 11 01 03	| 	| 	| AREAS & PARTITIONS
	| 	| 00000012	| 16	| SYN
	| 	| 00000013	| 01 55 55 02 00 8c 00 55 **00 01 00 00 00 2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 04 0c 11 03 00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 43 41 53 41** f2 4f 03	
000000D2	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000000AC	| 16	| SYN
	| 	| 000000AD	| 01 55 55 02 01 8c 00 55 **00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 46 49 4e 45 53 54 52 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 50 45 52 53 49 41 4e 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2e 2e 2e 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20** f1 c0 03	
000000DD	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 144	| 16	| SYN
	| 	| 145	| 01 55 55 02 02 35 00 55 **2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 ff ff e0 b6** f4 c0 03	
000000E8	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 185	| 16	| SYN

### Payload structure

Payload definition "Areas and partitions":  
` `  

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Number of areas	| Can be zero if unused
0x01-0x04	| Bitmask of partitions assigned to the area	| LSB = partition 1, one byte per area
0x05-0x1c	| First area name	|
0x1d-0x34	| Second area name	|
0x35-0x4c	| Third area name	|
0x4d-0x64	| Fourth area name	|
0x65	| Number of partitions	|
0x66	| Self-exclusion bitmask	| LSB = partition 1. A partition cannot be both self-exclusion and arming block. Standard partitions have both flags to zero.
0x67	| Arming block bitmask	| LSB = partition 1
0x68-0x77 | Entry delay	| 2 bytes per partition (starting at 0x68-0x69 for partition 1 and so on), big endian
0x78-0x87 | Exit delay	| 2 bytes per partition (starting at 0x78-0x79 for partition 1 and so on), big endian
0x88-0x9f	| First partition name	|
0xa0-0xb7	| Second partition name	|
0xb8-0xcf	| Third partition name	|
0xd0-0xe7	| Fourth partition name	|
0xe8-0xff	| Fifth partition name	|
0x100-0x117	| Sixth partition name	|
0x118-0x12f	| Seventh partition name	|
0x130-0x147	| Eighth partition name	|
0x148	| ?	| The serializer explicitly zeroes it on write (`data[328] = 0; // ???`); same signature as the other "status" bytes already documented (see [Input](#input)) — suspected dynamic content not handled by the client, not yet identified but computed in checksum
0x149-0x14c	| Block checksum	|

` `  
` `  

### Payload example

Sample partitions data capture:  

`00 # number of areas (max 4)`  
`01 00 00 00 # partitions assigned to the 4 areas (bit mask, one byte per area)`  
`2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # 24 bytes, area names`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`04 # number of partitions (max 8)`  
`0c # type (self exclusion, bitmask) 1 bit means self excl`  
*`11`* `03 # type (arming block, bitmask) 1 bit means arming block`  
`00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 # Entry/Exit delay, 16 bytes, 2 per partition, big endian`  
`00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 # Entry/Exit delay, 16 bytes, 2 per partition, big endian`  
`43 41 53 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # 24 bytes, partition names. CASA`  
`47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # GARAGE`  
`46 49 4e 45 53 54 52 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # FINESTRE`  
`50 45 52 53 49 41 4e 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # PERSIANE`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`20 `  
`ff ff e0 b6 # Block checksum`  

Another example:  

`00 01 00 00 00`  
`2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`02 00` *`11`* `03 `  
`00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00 `  
`00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00`  
`43 41 53 41 f3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00` That F3 seems to be an intruder (stray byte)  
`47 41 52 41 47 45 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00`  
`2e 2e 2e  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20`  
`00`  
`ff ff df fb`

## Users

Reading users.

### Stream capture

Stream excerpt for reading users:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000001E0	| 01 55 55 00 00 00 00 5b fe fb 03	| 	| 	| UTENTI
	| 	| 0000007C	| 16	| SYN
	| 	| 0000007D	| 01 55 55 05 00 8c 00 5b **00 ff 54 45 43 4e 49 43 4f 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 02 ff 4d 41 53 54 45 52 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 0f 55 47 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0f 41 4e 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20** ed fc 03	
000001EB	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000114	| 16	| SYN
	| 	| 00000115	| 01 55 55 05 01 8c 00 5b **20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ed 52 03	
000001F6	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000001AC	| 16	| SYN
	| 	| 000001AD	| 01 55 55 05 02 8c 00 5b **20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e** ed 74 03	
00000201	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000244	| 16	| SYN
	| 	| 00000245	| 01 55 55 05 11 03 8c 00 5b **2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20** ed 42 03	
0000020C	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000002DD	| 16	| SYN
	| 	| 000002DE	| 01 55 55 05 04 8c 00 5b **20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ed 4f 03	
00000217	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000375	| 16	| SYN
	| 	| 00000376	| 01 55 55 05 05 88 00 5b **20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 9d d4** ed 83 03	

### Payload structure

Payload definition "Users" — 26 bytes per user (`Credential.CREDENTIAL_SIZE`), 32 consecutive users (`MAX_CREDENTIALS`) + 4 bytes of trailing checksum. For each user:

Offset (relative to the user)	| Meaning	| Note
--------|---------------|-----
0x00	| Enabling	| `Credential.Enabling`: 0=disabled, 1=enabled, 2=always enabled
0x01	| Associated partitions	| Bitmask, LSB = partition 1
0x02-0x19	| Name	| 24 bytes

WARNING: a user's enabling state here does **NOT** imply the user can't log into a keypad to arm/disarm the system, since keypad login relies on the status reported by command 0x87 (and possibly changed via 0x88). The enabling byte here is always 0x00 for non-system users.

### Payload example

User data:

`00 ff # Enabling (1 byte) + associated partitions`  
`54 45 43 4e 49 43 4f 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 # TECNICO/technician (24 bytes)`  
`02 ff # 0x02 = always enabled`  
`4d 41 53 54 45 52 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 # MASTER`  
`00 0f `  
`55 47 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # UGO`  
`00 0f `  
`41 4e 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # ANNA`  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 # ...`  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00`  
`ff ff 9d d4 # Block checksum`

## Keys

Key (badge) data.

### Stream capture

Stream excerpt for reading keys:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000001B4	| 01 55 55 00 00 00 00 5c fe fa 03	| 	| 	| CHIAVI
	| 	| 00000078	| 16	| SYN
	| 	| 00000079	| 01 55 55 05 00 8c 00 5c **00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20** ed 67 03	
000001BF	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000110	| 16	| SYN
	| 	| 00000111	| 01 55 55 05 01 8c 00 5c **20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ed 51 03	
000001CA	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000001A8	| 16	| SYN
	| 	| 000001A9	| 01 55 55 05 02 8c 00 5c **20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e** ed 73 03	
000001D5	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000240	| 16	| SYN
	| 	| 00000241	| 01 55 55 05 11 03 8c 00 5c **2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20** ed 41 03	
000001E0	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000002D9	| 16	| SYN
	| 	| 000002DA	| 01 55 55 05 04 8c 00 5c **20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ed 4e 03	
000001EB	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000371	| 16	| SYN
	| 	| 00000372	| 01 55 55 05 05 88 00 5c **20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 ff ff 9a a0** eb 19 03	

### Payload structure

Payload definition "Keys" — same structure as [Users](#users) (26 bytes per key, 32 keys + 4 bytes of checksum), with byte 0x00 packing two fields instead of one. For each key:

Offset (relative to the key)	| Meaning	| Note
--------|---------------|-----
0x00	| Enabling (bit 0) + Specialization (bits 2-3)	| Enabling: `Credential.Enabling` on the least significant bit. Specialization: `Key.Specialization` via `(byte & 0x0C) >> 2` — 0=none, 1=change partition status, 2=access control, 3=access control limited to associated partitions
0x01	| Associated partitions	| Bitmask, LSB = partition 1
0x02-0x19	| Name	| 24 bytes

### Payload example

Key data:

`00 01 # Enabling + specialization and associated partitions`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 # key name (24 bytes)`  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`00 01 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`ff ff 9a a0 # Block checksum`  

## Parameters

System configuration parameters.

### Stream capture

Stream excerpt for reading parameters:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
0000011A	| 01 55 55 00 00 00 00 26 ff 30 03	| 	| 	| PARAMETERS
	| 	| 0000006A	| 16	| SYN
	| 	| 00000079	| 01 55 55 00 00 1e 00 26 **00 00 00 00 00 01 01 01 01 01 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 01 0f 00 ff ff fe 82** fa 16 03	

### Payload structure

Payload definition "Parameters" (30 bytes, offset 0-based), verified directly against `serializer.ParametersEnablings`:

Offset	| Meaning	| Note
--------|---------------|-----
0x00-0x04	| ?	| Not mapped by any DTO field
0x05, 0x07, 0x09	| Burglar time	| `Time`: 0=30s, 1=60s, 2=90s, 3=180s, 4=9min. **All three offsets always hold the same value** (`data[5]=data[7]=data[9]`)
0x06	| Pre-alarm time	| `Time`, same values
0x08	| Emergency time	| `Time`, same values
0x0a	| ?	| Not mapped by any DTO field
0x0b	| Power lack	| `PowerLack`: 0=1h, 1=2h, 2=4h
0x0c	| ?	| Not mapped by any DTO field
0x0d	| Alarm count	| `AlarmCount`: 0=none, 1=two, 2=four, 3=six, 4=eight
0x0e	| Notice	| `Notice`: 0=none, 5=5min, 10=10min, 15=15min, 20=20min
0x0f	| Time programmer	| `Enabling`: 0=disabled, 1=enabled
0x10	| DST	| Bitmask: bit0=enabled, bit1=last Sunday (instead of first)
0x11-0x14	| ?	| Not mapped by any DTO field
0x15	| DST OFF month	| `Month`: 1-12
0x16	| DST ON month	| `Month`: 1-12
0x17	| LAN	| `Enabling`: 0=disabled, 1=enabled
0x18	| Play	| Bitmask: bit0=fault, bit1=partitions, bit2=system, bit3=service
0x19	| Help	| Bit 7 (`0x80`)=enabled, bits 0-2=keypad address - 1
0x1a-0x1d	| Block checksum	|

### Payload example

Parameter data:

`00 00 00 00 00 01 01 01 01 01 00 00 00 00 05 00` *`11`* `03 55 55 55 55 `  
`0a` *`11`* `03 # DST October/March`  
`01 0f 00`  
`ff ff fe 82 # Block checksum`

Stream excerpt for writing parameters:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000002E8	| 01 55 55 00 00 1e 00 96 **00 00 00 00 00 02 11 01 02 11 01 02 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 11 01 0f 00 ff ff fe 7f** f9 a6 03	| 	| 	| PARAMETERS
	| 	| 000000CB	| 16	| SYN

Parameter data:

`00 00 00 00 00 02` *`11`* `01 02` *`11`* `01 02 00 00 00 00 05 00` *`11`* `03 55 55 55 55 0a` *`11`* `03` *`11`* `01 0f 00 `  
`ff ff fe 7f # Block checksum`

Stream excerpt for writing parameters:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000004B8	| 01 55 55 00 00 1e 00 96 **00 00 00 00 00 11 01 11 01 11 01 11 01 11 01 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 11 01 0f 00 ff ff fe 82** f9 a6 03	| 	| 	| PARAMETERS
	| 	| 000000FE	| 16	| SYN

Parameter data:

`00 00 00 00 00` *`11`* `01` *`11`* `01` *`11`* `01` *`11`* `01` *`11`* `01 00 00 00 00 05 00` *`11`* `03 55 55 55 55 0a` *`11`* `03` *`11`* `01 0f 00 `  
`ff ff fe 82 # Block checksum`

## Phone numbers

Data for the phone numbers and their related events.

### Stream capture

Stream excerpt for writing phone numbers:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000003B9	| 01 55 55 02 00 8c 00 e7 **ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff** 87 85 03	| 	| 	| EDIT PHONE NUMBERS
	| 	| 000000B3	| 16	| SYN
00000450	| 01 55 55 02 11 01 8c 00 e7 **ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00** c9 e8 03			
	| 	| 000000B4	| 16	| SYN
000004E8	| 01 55 55 02 02 80 00 e7 **00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 55 80** fa ec 03			
	| 	| 000000B5	| 16	| SYN

### Payload structure

Payload definition "Phone numbers": 12 phone numbers are listed, each with the following data:  
` `  

Offset (relative to the phone number)	| Meaning	| Note
----------------------------------------|---------------|-----
0x00-0x0d	| Phone number	| Phone number encoded in BCD (14 bytes - 28 digits)
0x0e	| Bitmask of associated partitions	| LSB = partition 1
0x0f	| Phone network	| 00 = PSTN, 01 = GSM, 02 = LAN
0x10	| Sending mode	| 00 = Voice, 01 = IDP, 02 = ADF, 04 = Modem, 06 = SMS, 07 = C200b

**LAN/IP numbers**: when the "Phone network" field is `0x02` (LAN), the phone number is replaced by an address in the fixed format `DDD.DDD.DDD.DDD:DDDDD` (each octet zero-padded to 3 digits, trailing part to 5 digits). Encoding **directly confirmed by the code** — `serializer.PhoneNumber.serialize()` implements exactly this logic: each decimal digit in BCD, the dot `.` as nibble `0x0B`, the colon `:` as nibble `0x0C`, with the author's own original comment (`"In generale gli IP sono 001B002B003B004C00005"`, i.e. "IPs are generally 001B002B003B004C00005") present both there and in `ClientConnection`. The padding of the last, unpaired nibble (for an odd number of digits/separators, as in the example below) is `0x0F` — also in the code (`bcdByte | 0x0F`).

Example verified with a real capture, for the address `192.168.001.100:00080`:

* digit/separator sequence: `1 9 2 B 1 6 8 B 0 0 1 B 1 0 0 C 0 0 0 8 0`
* resulting bytes: `19 2b 16 8b 00 1b 10 0c 00 08 0f` (11 bytes, the last nibble `f` is padding) — matches the capture exactly

Consistent with the `// FIXME: IP addresses in phone numbers!` comment still present in the DTO's `PhoneNumber.setPhoneNumber()` setter — that comment only flags that the DTO doesn't yet explicitly validate/recognize the IP format (it accepts the string as-is), not that the encoding itself is uncertain: the encoding, on the serializer side, is complete.

**Event assignment table**: after the 12 phone records (204 bytes) comes, at the absolute offsets returned by `PhoneNumber.Event.getOffset()`, a **2-byte word** for each reportable event:

* The word is a **bitmask of the phones** assigned to that event (bit *i* = phone *i+1*), confirmed empirically: a capture with only phone 1 enabled on an event → `0x0001`; same event with phone 1 **and** 2 → `0x0003`
* The offsets are **not consecutive/ordered** as in the enum: they're scattered across the remaining ~200 bytes of the payload (between the end of the phone records and the checksum), with large unused stretches between one event and the next — consistent with the long `00 00 00 00...` sequences observed in the dumps
* The serializer also **duplicates** some events' value across multiple offsets at once (mirroring, as already seen for [C200B](#c200b)): `PNSCE_BURGLAR_ALARM` → also `0x00e8`, `0x00ec`, `0x00f0`; `PNSCE_INPUT_INCLUSION_EXCLUSION` → also `0x0148`; `PNSCE_TAMPERING` → also `0x0104`, `0x0170`; `PNSCE_SYSTEM_FAULT` → also `0x0130`; `PNSCE_PARTITIONS_SYSTEM_ON_OFF` → primary offset `0x0134` → also `0x0138` and `0x0140` (not only `0x0140` as reported in a previous version of this document — a mirroring bug found and fixed thanks to the round-trip test suite)

The complete layout of the 408-byte payload, verified against `serializer.PhoneNumbersSendingCodes` (rows marked `?` are not mapped by any DTO field):

Offset	| Meaning	| Note
--------|---------------|-----
0x00-0xcb	| Phone numbers	| 12 records of 17 bytes each, see above
0xcc-0xcd	| Tampering	| `Event.PNSCE_TAMPERING`; the serializer also writes the same value at `0x104-0x105` and `0x170-0x171` (mirrors, not distinct events)
0xce-0xd3	| ?	| Not mapped by any DTO field
0xd4-0xd5	| Low battery	| `Event.PNSCE_LOW_BATTERY`
0xd6-0xd7	| ?	|
0xd8-0xd9	| Mains power	| `Event.PNSCE_MAINS_POWER`
0xda-0xe3	| ?	|
0xe4-0xe5	| Burglary alarm	| `Event.PNSCE_BURGLAR_ALARM`; the serializer also writes the same value at `0xe8-0xe9`, `0xec-0xed`, `0xf0-0xf1` (mirrors, not distinct events)
0xe6-0xe7	| ?	|
0xe8-0xe9	| (mirror of 0xe4-0xe5, burglary alarm)	|
0xea-0xeb	| ?	|
0xec-0xed	| (mirror of 0xe4-0xe5, burglary alarm)	|
0xee-0xef	| ?	|
0xf0-0xf1	| (mirror of 0xe4-0xe5, burglary alarm)	|
0xf2-0xf3	| ?	|
0xf4-0xf5	| Pre-alarm	| `Event.PNSCE_PRE_ALARM`
0xf6-0x103	| ?	|
0x104-0x105	| (mirror of 0xcc-0xcd, tampering)	|
0x106-0x107	| ?	|
0x108-0x109	| Panic	| `Event.PNSCE_PANIC`
0x10a-0x10b	| ?	|
0x10c-0x10d	| Silent panic	| `Event.PNSCE_SILENT_PANIC`
0x10e-0x10f	| ?	|
0x110-0x111	| Fire	| `Event.PNSCE_FIRE_ALARM`
0x112-0x11f	| ?	|
0x120-0x121	| Medical emergency	| `Event.PNSCE_MEDICAL_EMERGENCY`
0x122-0x123	| ?	|
0x124-0x125	| System fault	| `Event.PNSCE_SYSTEM_FAULT`; the serializer also writes the same value at `0x130-0x131` (mirror, not a distinct event)
0x126-0x12f	| ?	|
0x130-0x131	| (mirror of 0x124-0x125, system fault)	|
0x132-0x133	| ?	|
0x134-0x135	| Partition/system arm/disarm	| `Event.PNSCE_PARTITIONS_SYSTEM_ON_OFF`; the serializer also writes the same value at `0x138-0x139` and `0x140-0x141` (mirrors, not distinct events)
0x136-0x137	| ?	|
0x138-0x139	| (mirror of 0x134-0x135, partition/system arm/disarm)	|
0x13a-0x13b	| ?	|
0x13c-0x13d	| Hold-up	| `Event.PNSCE_HOLD_UP`
0x13e-0x13f	| ?	|
0x140-0x141	| (mirror of 0x134-0x135, partition/system arm/disarm)	|
0x142-0x143	| ?	|
0x144-0x145	| Input exclusion/inclusion	| `Event.PNSCE_INPUT_INCLUSION_EXCLUSION`; the serializer also writes the same value at `0x148-0x149` (mirror, not a distinct event)
0x146-0x147	| ?	|
0x148-0x149	| (mirror of 0x144-0x145, input exclusion/inclusion)	|
0x14a-0x14f	| ?	|
0x150-0x151	| Maintenance	| `Event.PNSCE_MAINTENANCE`
0x152-0x153	| ?	|
0x154-0x155	| False code	| `Event.PNSCE_FALSE_CODE`
0x156-0x157	| ?	|
0x158-0x159	| Generic notice	| `Event.PNSCE_NOTICES`
0x15a-0x163	| ?	|
0x164-0x165	| Technical alarm type 1	| `Event.PNSCE_TECHNOLOGICAL_ALARM_TYPE_1`
0x166-0x167	| ?	|
0x168-0x169	| Technical alarm type 2	| `Event.PNSCE_TECHNOLOGICAL_ALARM_TYPE_2`
0x16a-0x16b	| ?	|
0x16c-0x16d	| Technical alarm type 3	| `Event.PNSCE_TECHNOLOGICAL_ALARM_TYPE_3`
0x16e-0x16f	| ?	|
0x170-0x171	| (mirror of 0xcc-0xcd, tampering)	|
0x172-0x193	| ?	|
0x194-0x197	| Block checksum	|

### Payload example

Phone data:

`20 1f ff ff ff ff ff ff ff ff ff ff ff ff # Phone number encoded in BCD (14 bytes — ElkrommFacade.PHONE_NUMBER_LENGTH = 28 digits)`  
`ff # Bitmask of associated partitions (ElkrommUtils.packPartitions/unpackPartitions; here it's 0xff = all partitions, which is why it was initially mistaken for standard padding and looked like part of the number)`  
`00 # Phone network (00 = PSTN, 01 = GSM, 02 = LAN)`  
`00 # Sending mode (00 = Voice, 01 = IDP, 02 = ADF, 04 = Modem, 06 = SMS, 07 = C200b)`  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 `  
`00 00 `  
`00` *`11`* `01 00 00 # Event activation flags for the 12 phone numbers (2 bytes, LSB = phone 1) + 2 unknown bytes (Hp: C200b protocol)`  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00` *`11`* `01 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`00 00 00 00 `  
`ff ff 57 75 # Block checksum`

## Phone parameters

Phone dialer parameters.

### Payload structure

Payload definition "Phone parameters" (20 bytes, offset 0-based), verified against `serializer.PhoneParameters`:

Offset	| Meaning	| Note
--------|---------------|-----
0x00-0x04	| ?	| Not mapped by any DTO field
0x05	| Call delay	| `Enabling`: 0=disabled, 1=enabled
0x06	| ?	| Not mapped by any DTO field
0x07	| Return call	| `ReturnCall`: 0=disabled, 1=type A, 2=type B
0x08	| Remote surveillance	| `Enabling`
0x09	| Voice message sending mode	| `VoiceMessagesSendingMode`: 0=none, 1-4=mode 1-4
0x0a	| ?	| Not mapped by any DTO field
0x0b	| Cyclic test call frequency	| `CyclicTestCallFrequency`: 0=disabled, 1=24h, 2=when system armed
0x0c	| Phone number index for the test call	| 0-12 (0 if disabled)
0x0d	| Test call hour	| 0-23
0x0e	| Test call minute	| 0-59
0x0f	| Test call interval	| `CyclicTestCallInterval`: 0=1h, 1=4h, 2=8h, 3=12h, 4=24h, 5=48h, 6=72h, 7=96h, 8=120h, 9=144h, 10=168h
0x10-0x13	| Block checksum	|

**Note**: offset→field mapping verified directly against the serializer; command `0xe6` has never been linked in the table to a corresponding `0x56 PHONE PARAMETERS` read — worth checking whether they share the same data format, as happens for `0x26`/`0x96`.

## PSTN GSM

Parameters for the traditional (PSTN) and mobile (GSM) phone networks, where the GSM board is installed.

### Payload structure

Payload definition "PSTN GSM" (21 bytes, offset 0-based), verified against `serializer.PSTNGSM`:

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Enable PSTN network	| `Enabling`
0x01	| Country	| `Country`: 0=Italy, 1=France, 2=Germany, 3=Czech Rep., 4=Poland, 5=Spain, 6=Portugal, 7=Greece, 8=England
0x02-0x03	| ?	| Not mapped by any DTO field
0x04	| PABX local access digit	| `PABXLocalAccessDigit`: 0-9, `0xff`=disabled
0x05	| Tone control	| `Enabling`
0x06	| Answer control	| `Enabling`
0x07	| PSTN line test	| `PSTNLineTestFrequency`: 0=disabled, 1=24h, 2=when system armed
0x08	| PSTN answering machine rings	| `PSTNAnsweringMachineRings`: 0=disabled, 2/4/8=number of rings
0x09	| Enable GSM network	| `Enabling`
0x0a	| GSM answering machine (no ring)	| `Enabling`
0x0b	| Incoming SMS	| `Enabling`
0x0c-0x0e	| GSM PIN	| BCD, 3 bytes; `0xff 0xff 0xff` = no PIN set
0x0f	| Expiration month	|
0x10	| Expiration year	|
0x11-0x14	| Block checksum	|

## SMS

Command `0x59 SMS` (bulk read of all messages, see [Command types](#command-types))

### Payload structure

364-byte payload, a 40-character block per message (padding `0xff`), **in the same order** as the `SMSs.SMSIndex` enum.

Offset	| Meaning	| Note
--------|---------------|-----
0x00-0x27	| Message 1	| `SMS_BURLGAR` (burglary), ASCII, padding `0xff`
0x28-0x4f	| Message 2	| `SMS_TECHNICAL_ALARM_1`
0x50-0x77	| Message 3	| `SMS_TECHNICAL_ALARM_2`
0x78-0x9f	| Message 4	| `SMS_TECHNICAL_ALARM_3`
0xa0-0xc7	| Message 5	| `SMS_FIRE`
0xc8-0xef	| Message 6	| `SMS_PARTITION_ON`
0xf0-0x117	| Message 7	| `SMS_PARTITION_OFF`
0x118-0x13f	| Message 8	| `SMS_TAMPERING`
0x140-0x167	| Message 9	| `SMS_NOTICE`
0x168-0x16b	| Block checksum	|

Command `0xa0 SMS PROGRAMMING` (single-instance write, confirmed reachable via `ElkrommFacadeImpl.setSMS()`, see [Command types](#command-types)): 41-byte payload, verified against `serializer.SingleSMS`.

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Index	| **1-based** (`SMSIndex.ordinal() + 1`): 1=burglar, 2-4=tech.alarm 1-3, 5=fire, 6=partition on, 7=partition off, 8=tampering, 9=notice
0x01-0x28	| Message	| 40 ASCII bytes, padding `0xff`

### Payload example

Captured dump:

```
0000: 01 41 67 61 69 6e 3f ff  ff ff ff ff ff ff ff ff
0010: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff
0020: ff ff ff ff ff ff ff ff  ff
```

The text `41 67 61 69 6e 3f` decodes in ASCII as **"Again?"** — test text entered by the author in Hi-Connect. Index `0x01` = `SMS_BURLGAR`.

## C200B

Command: `0xe8 SET C200B` (write, never read/linked to `0x58 C200B` in the command table).

### Payload structure

168-byte payload, verified against `serializer.C200bParameters`.

Offset	| Meaning	| Note
--------|---------------|-----
0x00-0x31	| ?	| Not mapped by any DTO field
0x32	| Tampering	| `Event.C2PE_TAMPERING`; the serializer also writes the same value at `0x40` and `0x5b` (mirrors, not distinct events)
0x33	| ?	|
0x34	| Low battery	| `Event.C2PE_LOW_BATTERY`
0x35	| Mains power	| `Event.C2PE_MAINS_POWER`
0x36-0x37	| ?	|
0x38	| Burglary alarm	| `Event.C2PE_BURGLAR_ALARM`; also mirrored at `0x39`, `0x3a`, `0x3b`
0x39-0x3b	| (mirror of 0x38)	|
0x3c	| Pre-alarm	| `Event.C2PE_PRE_ALARM`
0x3d-0x3f	| ?	|
0x40	| (mirror of 0x32, tampering)	|
0x41	| Panic	| `Event.C2PE_PANIC`
0x42	| Silent panic	| `Event.C2PE_SILENT_PANIC`
0x43	| Fire	| `Event.C2PE_FIRE_ALARM`
0x44-0x46	| ?	|
0x47	| Medical emergency	| `Event.C2PE_MEDICAL_EMERGENCY`
0x48	| System fault	| `Event.C2PE_SYSTEM_FAULT`; also mirrored at `0x4b`
0x49	| ?	|
0x4a	| Partition arm/disarm	| `Event.C2PE_PARTITIONS_ON_OFF`
0x4b	| (mirror of 0x48, system fault)	|
0x4c	| System arm/disarm	| `Event.C2PE_SYSTEM_ON_OFF`; also mirrored at `0x4d`, `0x4f`
0x4d	| (mirror of 0x4c)	|
0x4e	| Hold-up	| `Event.C2PE_HOLD_UP`
0x4f	| (mirror of 0x4c)	|
0x50	| Input exclusion/inclusion	| `Event.C2PE_INPUT_INCLUSION_EXCLUSION`; also mirrored at `0x51`
0x51	| (mirror of 0x50)	|
0x52	| Cyclical test call	| `Event.C2PE_CYCLICAL_TEST_CALL`
0x53	| Maintenance	| `Event.C2PE_MAINTENANCE`
0x54	| False code	| `Event.C2PE_FALSE_CODE`
0x55-0x57	| ?	|
0x58	| Technical alarm type 1	| `Event.C2PE_TECHNOLOGICAL_ALARM_TYPE_1`
0x59	| Technical alarm type 2	| `Event.C2PE_TECHNOLOGICAL_ALARM_TYPE_2`
0x5a	| Technical alarm type 3	| `Event.C2PE_TECHNOLOGICAL_ALARM_TYPE_3`
0x5b	| (mirror of 0x32, tampering)	|
0x5c-0x63	| ?	|
0x64-0xa3	| Input codes	| 64 bytes, one per logical input (`MAX_LOGICAL_INPUTS`), `0xff` if the input doesn't exist
0xa4-0xa7	| Block checksum	|

## Time programmer and Day class commands

Commands involved: `0xe4 SET TIME PROGRAMMER` (write, whole block) and `0xa1 DAY CLASS CMDS` (single-instance write, confirmed reachable via `ElkrommFacadeImpl.setDayClassCommands()`). The "instance" for `DAY_CLASS_CMDS` is the day class itself, enum `DayClassCommands.DayClass`: `DCCDC_WORKING_DAY` (`0x00`), `DCCDC_PRE_HOLIDAY` (`0x01`), `DCCDC_HOLIDAY` (`0x02`).

### Payload structure

Payload definition "SET TIME PROGRAMMER" (131 bytes), verified against `serializer.TimeProgrammer`:

Offset	| Meaning	| Note
--------|---------------|-----
0x00-0x27	| Working day commands	| 8 commands × 5 bytes, see "Command" table below
0x28-0x4f	| Pre-holiday commands	| 8 commands × 5 bytes
0x50-0x77	| Holiday commands	| 8 commands × 5 bytes
0x78	| Day class — Monday	| `DayClass`: 0=working day, 1=pre-holiday, 2=holiday
0x79	| Day class — Tuesday	|
0x7a	| Day class — Wednesday	|
0x7b	| Day class — Thursday	|
0x7c	| Day class — Friday	|
0x7d	| Day class — Saturday	|
0x7e	| Day class — Sunday	|
0x7f-0x82	| Block checksum	|

Payload definition "DAY CLASS CMDS" (41 bytes, single-instance write — no block checksum, consistent with other single-instance writes), verified against `serializer.DayClassCommands`:

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Day class	| `DayClass`, identifies which of the three command sets is being written
0x01-0x28	| 8 commands	| 5 bytes each, see "Command" table below

**Command** (`Command` DTO, 5 bytes, used both inside `SET_TIME_PROGRAMMER` and in `DAY_CLASS_CMDS`; if `Action` is `CA_NONE` bytes 0x01-0x04 aren't written by the serializer):

Offset (relative to the command)	| Meaning	| Note
--------|---------------|-----
0x00	| Action	| `Command.Action`: 0=none, 1=enable, 2=disable
0x01	| Object	| Object index (partition or user, depending on the next byte)
0x02	| Object type	| `Command.ObjectType`: `0x10`=partitions, `0x40`=user — a code comment notes "more to come: keys, outputs", so the list may not be complete
0x03	| Hour	|
0x04	| Minute	|

Example from the `WORKING_DAY_CMD` dump: command 1 → hour 1, minute 2, enable, user 13 (inferred from the author's original comment, not re-verified byte-by-byte against the hex).

## Input

Shared structure (38 bytes), used within each expansion (up to 8 per expansion, [Block B](#block-b)) and for the two onboard inputs of each [Keypad](#keypads)/[Reader](#readers). A slot with `logicNumber` (offset 0x00) set to `0x00` is considered unused — **the panel does not reliably zero the other fields in this case**: `Specialization` in particular can retain the value left over from the input's last real configuration (verified: an input reconfigured from `DELAYED` to `NOT_USED` kept reporting `DELAYED`, then later changed to `IMMEDIATE` after that input was used again for other tests). For this reason `serializer.Input` **always deserializes** an object (even for unused slots), instead of returning `null` as in an earlier version of the code — it's the only way to preserve round-trip fidelity, since the content can't be standardized to a fixed default. This behavior doesn't occur on [Output](#output), which the panel reliably zeroes when unused.

### Payload structure

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Input's logical number	| `0x00` = unused slot (see the note above on leftover content)
0x01	| Configuration	| `Configuration`: 0=unused, 1=NC, 2=NO, 3=single-balanced NC, 4=double-balanced NC, 7=shock, 8=roller
0x02	| Specialization	| `Specialization`: see enum, 20 values (immediate, delayed, first entry, fire, tamper, ...)
0x03	| Sensitivity (high bits) + Flags (low bits) + live exclusion bit	| Sensitivity: `0x80`=low, `0x40`=medium, `0x00`=high (shock/roller only). Flags (bitmask): `0x01`=exclusion enabled, `0x02`=double release, `0x08`=OR partitions. **Bit `0x10` isn't handled by any static field**: the panel raises it when the input is **currently excluded** (live status, not configuration — matches exactly `ElkrommFacade.InputStatus.IS_EXCLUDED`) and lowers it again when the input is re-included. Confirmed on real hardware in three independent contexts: expansions, keypads (on their own onboard inputs too), and by analogy (unverified) on readers. The bit must always be excluded from the block checksum calculation (see [Block B](#block-b)); Hi-Connect always writes it as zero
0x04	| Associated camera	| `Video`: 0=none, then bitmask 0x10/0x20/0x40/0x80 for cameras 1-4
0x05	| Associated partitions	| Bitmask, LSB = partition 1
0x06-0x1d	| Name	| 24 bytes
0x1e-0x21	| ?	| Not mapped by any DTO field
0x22	| Delay	| `Delay`: 0=5s, 1=10s, 2=30s, 3=60s, 4=90s, 5=5min, 6=20s
0x23-0x25	| ?	| Not mapped by any DTO field

## Output

Shared structure (37 bytes), used within each expansion (up to 6 per expansion, [Block B](#block-b)). As with [Input](#input), a slot with `logicNumber` (offset 0x00) set to `0x00` is considered unused.

### Payload structure

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Output's logical number	| `0x00` = unused slot
0x01	| Type	| `Type`: 0=unused, 1=normally low, 2=normally high
0x02	| Associated partitions	| Bitmask, LSB = partition 1
0x03	| Specialization	| `Specialization`: 31 values (burglar, pre-alarm, tamper, gong, buzzer, partition status, ...)
0x04-0x07	| ?	| Not mapped by any DTO field
0x08-0x1f	| Name	| 24 bytes
0x20-0x24	| ?	| Not mapped by any DTO field

## Block B

Expansion data.

### Stream capture

Stream excerpt for reading block B:

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
000001EB	| 01 55 55 00 00 00 00 51 ff 05 03	| 	| 	| BLOCK B
	| 	| 0000007D	| 16	| SYN
	| 	| 0000007E	| 01 55 55 0c 00 8c 00 51 **00 00 00 30 33 30 31 01 04 11 03 09 00 01 56 4f 4c 20 5a 4f 4e 41 20 47 49 4f 52 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 02 ff ff 00 02 04 00 09 00 01 56 4f 4c 20 43 41 4d 45 52 49 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 11 03 04 00 09 00 01 56 4f 4c 20 53 54 55 44 49 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 04 04 00 09 00 01 56 4f 4c 20 42 41 47 4e 4f 00 00 00 00** ea 7f 03	
000001F6	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000117	| 16	| SYN
	| 	| 00000118	| 01 55 55 0c 01 8c 00 51 **00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 05 04 00 19 00 01 56 4f 4c 20 43 41 4d 45 52 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 06 04 05 09 00 0d 50 4f 52 54 4f 4e 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 07 04 00 09 00 08 50 45 52 53 49 41 4e 41 20 5a 4f 4e 41 20 47 49 4f 52 4e 4f 00 00 00 00 00 00 00 00 00 ff ff 00 08 04 00 09 00 04 46** eb 37 03	
00000201	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000001AF	| 16	| SYN
	| 	| 000001B0	| 01 55 55 0c 02 8c 00 51 **49 4e 45 53 54 52 41 20 5a 4f 4e 41 20 47 49 4f 52 4e 4f 00 00 00 00 00 00 00 00 00 ff ff 00 01 02 ff 1d 00 00 00 00 53 49 52 45 4e 41 20 45 53 54 45 52 4e 41 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 02 02 ff 1d 00 00 00 00 53 49 52 45 4e 41 20 49 4e 54 45 52 4e 41 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 11 03 02 ff 14 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00** e6 29 03	
0000020C	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000248	| 16	| SYN
	| 	| 00000249	| 01 55 55 0c 11 03 8c 00 51 **00 00 04 02 f1 15 00 00 00 00 43 41 53 41 20 53 45 54 54 4f 52 45 20 41 54 54 49 56 4f 00 00 00 00 00 00 00 00 00 00 05 02 ff 0e 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 06 02 ff 01 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 55 43 20 20 20 20 20 20 20 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 c9 20 00** ed 18 03	
00000217	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000002E1	| 16	| SYN
	| 	| 000002E2	| 01 55 55 0c 04 8c 00 51 **01 00 30 32 30 30 09 04 00 09 00 08 50 45 52 53 49 41 4e 41 20 43 41 4d 45 52 49 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 0a 04 00 09 00 04 46 49 4e 45 53 54 52 41 20 43 41 4d 45 52 49 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 0b 04 00 09 00 08 50 45 52 53 49 41 4e 41 20 53 54 55 44 49 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 0c 04 00 09 00 04 46 49 4e 45 53 54 52 41 20 53 54 55 44 49** e5 77 03	
00000222	| 01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000379	| 16	| SYN
	| 	| 0000037A	| 01 55 55 0c 05 8c 00 51 **4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 0d 04 00 09 00 08 50 45 52 53 49 41 4e 41 20 42 41 47 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 0e 04 00 09 00 04 46 49 4e 45 53 54 52 41 20 42 41 47 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 0f 04 00 09 00 08 50 45 52 53 49 41 4e 41 20 43 41 4d 45 52 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 10 04 00 09 00 04 46 49** e9 33 03	
0000022D	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000411	| 16	| SYN
	| 	| 00000412	| 01 55 55 0c 06 8c 00 51 **4e 45 53 54 52 41 20 43 41 4d 45 52 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 07 02 ff 00 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 08 02 ff 11 03 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 09 02 ff 14 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00** ec 23 03	
00000238	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000004AA	| 16	| SYN
	| 	| 000004AB	| 01 55 55 0c 07 8c 00 51 **00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 45 50 20 30 31 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 21 20 00 02** fa ad 03	
00000243	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000542	| 16	| SYN
	| 	| 00000543	| 01 55 55 0c 08 8c 00 51 **00 30 32 30 30 11 11 04 00 09 00 02 50 4f 52 54 41 20 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 12 04 11 03 09 00 02 56 4f 4c 20 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 13 04 00 09 00 02 42 41 53 43 55 4c 41 4e 54 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 14 04 05 09 00 02 50 45 44 4f 4e 41 4c 45 00 00 00 00 00 00 00** eb ec 03	
0000024E	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000005DC	| 16	| SYN
	| 	| 000005DD	| 01 55 55 0c 09 8c 00 51 **00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 15 04 00 09 00 02 53 4f 54 54 4f 53 43 41 4c 41 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 16 02 07 09 00 ff 50 4f 4d 50 45 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e** e8 bb 03	
00000259	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 00000674	| 16	| SYN
	| 	| 00000675	| 01 55 55 0c 0a 8c 00 51 **20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 0a 01 f2 14 00 00 00 00 47 41 52 41 47 45 20 53 45 54 54 4f 52 45 20 41 54 54 49 56 4f 00 00 00 00 00 00 00 00 0b 02 ff 11 03 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 0c 02 ff 14 00 00 00 00 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00** ea 4c 03	
00000264	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 0000070D	| 16	| SYN
	| 	| 0000070E	| 01 55 55 0c 0b 8c 00 51 **00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 45 50 20 30 32 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 f2 16 ff ff 2c** f7 b9 03	
0000026F	01 55 55 00 00 00 00 65 fe f1 03	| 	| 	| SEND
	| 	| 000007A5	| 16	| SYN
	| 	| 000007A6	| 01 55 55 0c 0c 01 00 51 **af** fe 3d 03	

### Payload structure

Each expansion occupies a fixed 559-byte block (`EXPANSION_SIZE`), repeated for the number of expansions present (0-7); the whole block ends with the usual 4 checksum bytes.

Offset (relative to the expansion)	| Meaning	| Note
--------|---------------|-----
0x00	| ?	| Not mapped by any DTO field
0x01	| Bus address	| `0x00` = central unit, `0x01` = first expansion and so on
0x02	| ?	| Not mapped by any DTO field
0x03-0x06	| Firmware version	| ASCII string, e.g. `"0301"`
0x07-0x136	| 8 inputs	| 38 bytes each, see [Input](#input)
0x137-0x214	| 6 outputs	| 37 bytes each, see [Output](#output). **WARNING:** actual expansions just have 3 outputs, only the one embedded in the central unit (address = 0x00) has 6 outputs
0x215-0x22c	| Name	| 24 bytes
0x22d-0x22e	| ?	| **Never sent by the client on write** (Hi-Connect always sends `0x00 0x00` on `EXPANSION PROGRAMMING`/0x91), populated by the panel on read with content not yet identified — must be excluded from the block checksum calculation (see note below)
0x22f	| Second expansion	| The preceding fields repeat
...	|
x-3,x	| Checksum	| Last four bytes are block checksum

**Note on the checksum**: on a real MP-508 v03.01 panel, the block checksum computed with the standard algorithm (see [Command types](#command-types)) doesn't match the one embedded by the panel, unless, for each expansion, the following are zeroed out beforehand: the 2 bytes at relative offset 0x22d-0x22e described above, **and** bit `0x10` of *every* input (relative offset `7 + i*38 + 3` for the i-th input) — see [Input](#input). The current code (`serializer.Expansions`) applies this double correction before verifying the checksum, and throws an exception if it still doesn't match.

**The exact same phenomenon has been confirmed on real hardware on [Keypads](#keypads) too** (the only difference being that a single byte is enough there instead of two — see the dedicated section), reinforcing the idea that it's an architectural pattern of the panel (dynamic status "tucked into" otherwise-static configuration bytes), not a peculiarity of expansions alone. The same fix (2 trailing bytes + bit `0x10` on the two onboard inputs) has been applied **by analogy, "on faith"**, to [Readers](#readers) too — unverifiable on the author's hardware, who owns no physical readers. For this reason, on Readers only, the checksum check remains a `logger.warn()` instead of an exception: if the hypothesis turned out to be wrong on some real installation, the symptom would be a log warning, not a hard failure. The "mysterious" byte in [Partitions](#partitions) (offset 0x148) might belong to the same family, but there's no per-input granularity there, so the connection remains a weak, unverified hypothesis.

What those 2 bytes at offset 0x22d-0x22e of every expansion actually **are**, though, remains entirely unresolved (code comment: `// FIXME: single expansion checksum???`) — we only know they must be excluded from the calculation, not what they contain. It's unclear whether the general behavior is specific to v03.01 firmware or holds across other versions/models.

### Payload example

Block B data:

`00 00 00`   
`30 33 30 31 # 0301 panel version`  
`01 04` *`11`* `03 09 00 01 # Panel input: logical number, configuration (4 = NC Double Bal, 2 = NO), specialization (3 = way, 0 = immediate, 5 = first/last entry, 7 = techno type 1), excludable etc?, aux?, partitions`  
`56 4f 4c 20 5a 4f 4e 41 20 47 49 4f 52 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 02 ff ff 00 `  
`02 04 00 09 00 01`   
`56 4f 4c 20 43 41 4d 45 52 49 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
*`11`* `03 04 00 09 00 01`   
`56 4f 4c 20 53 54 55 44 49 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`04 04 00 09 00 01`   
`56 4f 4c 20 42 41 47 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`05 04 00 19 00 01`   
`56 4f 4c 20 43 41 4d 45 52 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`06 04 05 09 00 0d`   
`50 4f 52 54 4f 4e 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`07 04 00 09 00 08`   
`50 45 52 53 49 41 4e 41 20 5a 4f 4e 41 20 47 49 4f 52 4e 4f 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`08 04 00 09 00 04`   
`46 49 4e 45 53 54 52 41 20 5a 4f 4e 41 20 47 49 4f 52 4e 4f 00 00 00 00 00 00 00 00 00 ff ff 00`   
`01 02 ff 1d 00 00 00 00 # Panel output: logical number, output type (1 = NL, 2 = NH), partitions, specialization (1d = burglar/tamper, 14 = and TC, 15 = or TC, 0e = tel fault, 01 = pre alarm, 00 = burglar, 03 = tampering), ?, ?,`   `?, ?`  
`53 49 52 45 4e 41 20 45 53 54 45 52 4e 41 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`02 02 ff 1d 00 00 00 00`   
`53 49 52 45 4e 41 20 49 4e 54 45 52 4e 41 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
*`11`* `03 02 ff 14 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`04 02 f1 15 00 00 00 00`   
`43 41 53 41 20 53 45 54 54 4f 52 45 20 41 54 54 49 56 4f 00 00 00 00 00 00 00 00 00 00 `  
`05 02 ff 0e 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`06 02 ff 01 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`55 43 20 20 20 20 20 20 20 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # UC`  
`c9 20`   
`00 01 00 # New unit address`  
`30 32 30 30 # 0200 expansion version`  
`09 04 00 09 00 08`   
`50 45 52 53 49 41 4e 41 20 43 41 4d 45 52 49 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`0a 04 00 09 00 04`   
`46 49 4e 45 53 54 52 41 20 43 41 4d 45 52 49 4e 41 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`0b 04 00 09 00 08`   
`50 45 52 53 49 41 4e 41 20 53 54 55 44 49 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`0c 04 00 09 00 04`   
`46 49 4e 45 53 54 52 41 20 53 54 55 44 49 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`0d 04 00 09 00 08`   
`50 45 52 53 49 41 4e 41 20 42 41 47 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`0e 04 00 09 00 04`   
`46 49 4e 45 53 54 52 41 20 42 41 47 4e 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`0f 04 00 09 00 08`   
`50 45 52 53 49 41 4e 41 20 43 41 4d 45 52 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`10 04 00 09 00 04`   
`46 49 4e 45 53 54 52 41 20 43 41 4d 45 52 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`07 02 ff 00 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`08 02 ff` *`11`* `03 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`09 02 ff 14 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00`   
`00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00`   
`00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00`   
`00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 `  
`45 50 20 30 31 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 # EP 01`  
`21 20`   
`00 02 00 # New unit address`  
`30 32 30 30 # 0200 expansion version`  
*`11`* `11 04 00 09 00 02`   
`50 4f 52 54 41 20 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`12 04` *`11`* `03 09 00 02`   
`56 4f 4c 20 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`13 04 00 09 00 02`   
`42 41 53 43 55 4c 41 4e 54 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`14 04 05 09 00 02`   
`50 45 44 4f 4e 41 4c 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 00 `  
`15 04 00 09 00 02`   
`53 4f 54 54 4f 53 43 41 4c 41 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 `  
`16 02 07 09 00 ff`   
`50 4f 4d 50 45 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 `  
`00 00 00 09 00 01`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 `  
`00 00 00 09 00 01`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 `  
`0a 01 f2 14 00 00 00 00`   
`47 41 52 41 47 45 20 53 45 54 54 4f 52 45 20 41 54 54 49 56 4f 00 00 00 00 00 00 00 00 `  
`0b 02 ff` *`11`* `03 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`0c 02 ff 14 00 00 00 00`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00`   
`00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00`   
`00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00 `  
`00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 `  
`45 50 20 30 32 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 # EP 02`  
`f2 16 `  
`ff ff 2c af # Block checksum`

## Keypads

Keypad data.

### Stream capture

TX Offset	| Transmission	| RX Offset	| Reception	| Meaning
----------------|---------------|---------------|---------------|------------
  | 01 55 55 00 00 00 00 52 ff 04 03	|	|	| KEYPADS
  |	|	| 16 | SYN
  |	|	| 01 55 55 01 00 8c 00 52 **01 00 30 34 31 30 00 00 01 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 06 ff 06 49 4e 47 52 45 53 53 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 b0 00 02 00 30 32 30 30 00 00 01 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ec 0e 03
| 01 55 55 00 00 00 00 65 fe f1 03	|	|	| SEND
  |	|	| 16 | SYN
  |	|	| 01 55 55 01 01 56 00 52 **20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 06 ff 00 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 e5 00 ff ff e3 8b** ef 9f 03

### Payload structure

Payload definition "Keypads":  
` `  

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Address	| Keypad address (base 1)
0x01	| ?	|
0x02-0x05	| Version	| ASCII
0x06-0x2b	| First onboard input	| 38 bytes, see [Input](#input)
0x2c-0x51	| Second onboard input	| 38 bytes, see [Input](#input)
0x52	| Enablings bitmask	| GONG, ENTRY, EXIT, MASKING, FIRE, PANIC, HELP
0x53	| Bitmask of associated partitions	| LSB = partition 1
0x54	| Audio feature bitmask	| CAPABLE, ENABLED
0x55-0x6c	| Keypad name	|
0x6d	| ?	| Must be excluded from the block checksum calculation (see [Input](#input) and the note in [Block B](#block-b)) — confirmed on real hardware
0x6e	| ?	| Always observed as `0x00` in the available captures; the code zeroes it anyway for symmetry with the Expansions code, but it doesn't appear necessary
0x6f	| Second keypad	| The preceding fields repeat
...	|	|

` `  
` `  

### Payload example

Sample keypad data capture:  

`01 @ Address`  
`00`  
`30343130 # Version`  
`000001090001 # Input 1`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`000000090001 # Input 2`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`06 # Enablings`  
`ff # Associated partitions`  
`06 # Audio`  
`494e47524553534f00000000000000000000000000000000 # Name`  
`b000`  

`02 # Address`  
`00`  
`30323030 # Version`  
`000001090001 # Input 1`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`000000090001 # Input 2`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`06 # Enablings`  
`ff # Associated partitions`  
`00 # Audio`  
`474152414745000000000000000000000000000000000000 # Name`  
`e500`  

`ffffe38b # Block checksum`  

## Readers

Structure of a reader (113 bytes), read in bulk with `READERS` (0x53) and writable individually with `READER PROGRAMMING` (0x93, unverified on real hardware — see [Command types](#command-types)).

### Payload structure

Offset	| Meaning	| Note
--------|---------------|-----
0x00	| Bus address	| Also identifies the reader in single-instance writes (there's no separate `SingleReader` wrapper)
0x01-0x05	| ?	| Not mapped by any DTO field
0x06-0x2b	| First onboard input	| 38 bytes, see [Input](#input)
0x2c-0x51	| Second onboard input	| 38 bytes, see [Input](#input)
0x52	| LED 1	| Associated partition (`ElkrommFacade.Partition`), `0x00` = unused
0x53	| LED 2	| Associated partition
0x54	| LED 3	| Associated partition
0x55	| LED 4	| Associated partition
0x56	| Enablings bitmask	| `Reader.Enablings`: only `MASKING` (0x01) known
0x57-0x6e	| Name	| 24 bytes
0x6f-0x70	| ?	| Not mapped by any DTO field. Zeroed on write by analogy with [Expansions](#block-b)/[Keypads](#keypads), **unverified on real hardware** (the author owns no physical readers) — for this reason the block checksum remains a warning, not an exception, on this structure

## Keypad programming

Single keypad parameter write.

### Payload structure

See [Keypads](#keypads), offsets 0x00-0x6e. The single keypad's address is carried at relative offset 0x00.

Keypad data:

`Dumping KEYPAD_PROGRAMMING, payload size: 111`  
`0000: 01 00 30 34 31 30 00 00  01 09 00 01 2e 2e 2e 20   ..0410.. .......  `  
`0010: 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20                     `  
`0020: 20 20 20 20 00 00 00 00  00 ff ff 00 00 00 00 09       .... .￿￿..... `  
`0030: 00 01 2e 2e 2e 20 20 20  20 20 20 20 20 20 20 20   .....             `  
`0040: 20 20 20 20 20 20 20 20  20 20 00 00 00 00 00 ff              .....￿ `  
`0050: ff 00 07 ff 06 49 4e 47  52 45 53 53 4f 00 00 00   ￿..￿.ING RESSO... `  
`0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00      ........ .......`  

## Credits

Anthrop\c Claude translated from Italian version.
Reviewed by the author.
