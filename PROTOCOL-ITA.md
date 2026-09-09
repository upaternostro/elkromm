# Analisi protocollo Elkron Hi-Connect

## Indice

* [Premessa](#premessa)
* [Framing e comandi](#framing-e-comandi)
* [Tipologie di comando](#tipologie-di-comando)
* [Comandi riconosciuti](#comandi-riconosciuti)
* [Settori](#settori) aka "Aree e settori"
* [Utenti](#utenti)
* [Chiavi](#chiavi)
* [Parametri](#parametri)
* [Numeri telefonici](#numeri-telefonici)
* [Parametri telefonici](#parametri-telefonici)
* [PSTN GSM](#pstn-gsm)
* [SMS](#sms)
* [C200B](#c200b) aka "Eventi"
* [Time programmer e Day class commands](#time-programmer-e-day-class-commands)
* [Blocco B](#blocco-b) aka "Espansioni" aka "Nodi"
* [Keypads](#keypads)
* [Readers](#readers)
* [Keypad programming](#keypad-programming)

## Premessa

Il presente studio si basa sui seguenti requisiti:

* utilizzo Hi-Connect v3.30 / 3.70;
* utilizzo di una centrale MP-508 v03.01;
* codice impianto 55555555 (default);
* codice installatore 000000 (default);
* connessione diretta ad IP.

## Framing e comandi

Il colloquio avviene sulla porta TCP 8030.  
Quando si apre il sistema in gestione, Hi-Connect apre una socket TCP verso l`IP/port indicati.  
Nel seguito viene dettagliata la conversazione (al netto dei pacchetti classici del TCP).

Tenere presente il significato dei seguenti codici ASCII:  
0x01: SOH (start of heading)  
0x03: ETX (end of text)  
0x06: ACK (acknowledge)  
0x11: DC1 (device control 1, usato come escape dello 0x03 nella comunicazione centrale -> Hi-Connect nei blocchi di configurazione, non conteggiato nei checksum di blocco, né in quello di pacchetto, né nelle lunghezze dei dati, evidentemente aggiunto subito prima di sparare il pacchetto on the wire. ATTENZIONE: nella comunicazione Hi-Connect -> centrale si fa escape sia di 0x03 che di 0x01,pena un NAK da parte della centrale)  
0x15: NAK (negative acknowledge)  
0x16: SYN (synchronous idle)

La conversazione origina sempre dal software Hi-Connect, la centrale è slave e risponde ai comandi ricevuti. Dato che l’hardware della centrale non è particolarmente performante, è bene introdurre dei delay (sleep di qualche centinaio di microsecondi) tra invio e tentativo di ricezione, o gestire l’eventuale mancanza dei dati.

I pacchetti possono essere distinti in due tipi:

1. pacchetti che non contengono dati;
2. pacchetti che contengono dati.

Entrambi possono essere inviati da tutti e due gli interlocutori, in base alle necessità del colloquio. Un interlocutore può inviare anche due pacchetti consecutivi.

I pacchetti che non contengono dati sono tipicamente costituiti da un solo byte. Si osservano particolarmente nelle risposte della centrale al software Hi-Connect. Sono stati osservati i seguenti pacchetti:

* 0x06 (ACK)
* 0x15 (NAK)
* 0x16 (SYN)

I pacchetti contenenti dati sono così strutturati:  
` `  

Offset	| Carattere	| Significato
--------|---------------|------------
0	| 0x01		| SOH
1	| 0x55		| Prime due cifre BCD del codice impianto
2	| 0x55		| Terza e quarta cifra BCD del codice impianto
3	| (variabile)	| numero dei pacchetti costituenti la sequenza
4	| (variabile)	| progressivo del pacchetto nella sequenza
5	| (variabile)	| lunghezza dei dati inviati
6	| 0x00		| ??? costante?
7	| (variabile)	| comando
8 → n	| (variabile)	| dati trasmessi (possono essere assenti se il byte di posizione 5 vale zero)
n+1 → n+2	| (variabile)	| checksum big endian del pacchetto calcolato in modo che la somma di tutti i byte dall'offset 1 a n (quindi escludendo il SOH iniziale), sommato a questo checksum, fornisca 0x10000. In altre parole, il checksum è `0x10000 – SUM(offset1... offsetn)`
n+3	| 0x03		| ETX

Nota: il byte 3 e 4 valgono entrambi 0x00 se la risposta è contenuta in un solo pacchetto (max 140 (0x8c) byte di dati, al netto degli escape). Se viceversa i dati da trasmettere eccedono il massimo indicato, la risposta viene spezzata in più pacchetti, il byte 3 viene valorizzato con l'indice dl pacchetto massimo e il byte 4 con l'indice del pacchetto corrente (base 0). In altre parole, se la risposta è spezzata in 3 pacchetti, il byte 3 vale 0x02 ed il byte 4 rispettivamente 0x00, 0x01 e 0x02 per il primo, secondo e terzo pacchetto. Il ricevitore conferma la ricezione con un pacchetto di tipo 0x65 (SEND).

## Tipologie di comando

Escludendo i pacchetti di controllo a byte singolo (SYN/ACK/NAK), i comandi applicativi si distinguono in:

* **Controllo sessione**: apertura, mantenimento e chiusura della connessione applicativa (HELLO, LOGIN, SEND, LOGOUT); non legge né scrive dati di configurazione.
* **Lettura**: il client invia il codice senza dati (salvo eventuali parametri di richiesta), la centrale risponde con SYN seguito da uno o più pacchetti dati con lo stesso codice comando.
* **Azione**: il client invia il codice con pochi byte di dati che rappresentano un comando puntuale e immediato (es. armare un settore, escludere un ingresso, abilitare un utente); la centrale risponde solo con SYN, senza restituire un pacchetto dati.
* **Scrittura**: il client invia il codice comando con l'intero blocco dati, strutturalmente analogo a quanto restituito dalla corrispondente lettura; la centrale risponde con SYN.
* **Scrittura singola istanza**: il client invia il codice comando con l'indice dell'elemento all'interno del proprio array (es. l'i-esimo utente, tastiera, lettore, chiave, SMS, day class) seguito dai soli dati di quell'istanza; la centrale risponde con SYN. A differenza della scrittura di blocco, non richiede di ritrasmettere l'intero array e non presenta il checksum di blocco. Nota: alcuni oggetti (ad es. [Espansioni](#blocco-b)) hanno l'indice al loro interno, quindi non viene aggiunto un ulteriore indice in vetta al pacchetto.


## Comandi riconosciuti

Comando	| Tipo	| Significato	| Dati		| Risposta centrale	| Note
--------|-------|---------------|---------------|-----------------------|-------
0x60	| Controllo sessione	| HELLO		| nessuno	| 0x06 (ACK)	
0x49	| Controllo sessione	| LOGIN		| codice impianto e codice installatore in BCD (rispettivamente 4 + 3 byte)	| 0x06 (ACK)	
0x65	| Controllo sessione	| SEND		| nessuno	| 0x16 (SYN) se non deve inviare nulla	| Hp: richiesta di invio dati
 |	|	| 		| 		| oppure
 |	|	| 		| 		| pacchetti dati successivi al primo
 |	|	|		|		| ipotesi: eventi?
0x62	| Lettura	| ADDRESSES	| nessuno	| 0x16 (SYN) + pacchetto dati 0x62 contente il numero di tastiere (1 byte), i loro indirizzi, il numero di lettori (1 byte), i loro indirizzi, il numero di espansioni (1 byte) ed i loro indirizzi	| indirizzi delle periferiche
0x50	| Lettura	| CHECKSUM	| nessuno	| 0x16 (SYN) + pacchetto dati 0x50 13 long word (32 bit) dati di checksum big endian, rispettivamente: nodi, tastiere, inseritori, sistema, programmatore orario, aree settori, com tel, num tel, eventi, sms, pstn gsm, utenti, chiavi. Nota: i checksum sono contenuti nei rispettivi payload.
0x84	| Lettura	| INPUT STATUS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x84 con un byte per ogni ingresso attivo (in ordine di indirizzo?). Il byte vale 0x00 se l'ingresso è chiuso, 0x02 se l'ingresso è aperto, 0x10 se l’ingresso è escluso, 0x04 in caso di memoria di allarme, ...
0x63	| Controllo sessione	| LOGOUT	| nessuno	| 0x16 (SYN)	
0x55	| Lettura	| AREE & SETTORI	| nessuno	| 0x16 (SYN) + pacchetto dati 0x55, vedi [Settori](#settori)	| Questo ed i successivi 3 comandi (UTENTI, CHIAVI, PARAMETERS & ENABLINGS) coprono il “blocco A”, che a differenza del “blocco B” non esiste come comando singolo
0x5b	| Lettura	| UTENTI	| nessuno	| 0x16 (SYN) + pacchetto dati 0x5b, vedi [Utenti](#utenti)	
0x5c	| Lettura	| CHIAVI	| nessuno	| 0x16 (SYN) + pacchetto dati 0x5c, vedi [Chiavi](#chiavi)	
0x26	| Lettura	| PARAMETERS & ENABLINGS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x84, vedi [Parametri](#parametri)	
0x80	| Lettura	| SYSTEM STATUS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x80 con un singolo byte di dati, 1 bit ogni settore, LSB = settore 1	
0x81	| Azione	| ARM/DISARM SYSTEM	| due byte di dati, 1 bit ogni settore, LSB = settore 1, in caso di attivazione i due byte sono uguali (ad es. 0x02 + 0x02 per armare il settore 2), mentre in caso di disattivazione il primo indica il settore, il secondo vale 0x00. È possibile attivare/disattivare oiù settori contemporaneamente effettuando l'OR logico dei bit che rappsentano i settori, ad es. 0xFF 0x00 per disattivare tutto.	| 0x16 (SYN)	
0x96	| Scrittura	| WRITE PARAMETERS & ENABLINGS	| vedi dati di 0x26, [Parametri](#parametri). Attenzione: ESCAPE con 0x11 anche di 0x01	| 0x16 (SYN)	
0x95	| Scrittura singola istanza	| AGGIUNTA UTENTE	| Progressivo utente incrementato di 1 + aree + settori + nome (24 byte)	| 0x16 (SYN)	| Confermato da DTO `SingleCredential`(index, `User`); gestito anche dall'emulatore
0xe7	| Scrittura	| MODIFICA NUMERI TELEFONICI	| pacchetto dati 0xe7, vedi [Numeri telefonici](#numeri-telefonici)	| 0x16 (SYN)	
0x51	| Lettura	| EXPANSIONS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x51, vedi [Blocco B](#blocco-b)	| Aka Blocco B
0x83	| Azione	| EXCLUDE/INCLUDE INPUT	| due byte di dati, il primo indica il numero di ingresso, il secondo vale 0x01 (attenzione all’escape) per escludere l’ingresso, 0x00 per includerlo	| 0x16 (SYN)	
0x87	| Lettura	| USER STATUS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x87 contenente 4 byte di dati con i flag che indicano l’attivazione degli utenti. Il primo byte contiene nell’MSB (0x80) lo stato dell’utente 0 (TECNICO), nel bit immediatamente successivo (0x40) lo stato dell’utente 1 (MASTER) e così via per gli altri bit. Il secondo byte indica gli stati degli utenti 8-15, il terzo 16-23 e l’ultimo 24-31	
0x88	| Azione	| ENABLE/DISABLE USER	| due byte di dati, il primo indica il numero di utente (base 1 = TECNICO), il secondo vale 0x01 (attenzione all’escape) per attivare l’utente, 0x00 per disattivarlo	| 0x16 (SYN)	
0x57	| Lettura	| PHONE NUMBERS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x57
0x56	| Lettura	| PHONE PARAMETERS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x56
0x5a	| Lettura	| PSTN GSM	| nessuno	| 0x16 (SYN) + pacchetto dati 0x5a
0x59	| Lettura	| SMS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x59, vedi [SMS](#sms)	| vedi anche 0xa0 SMS PROGRAMMING per la scrittura del singolo SMS
0x58	| Lettura	| C200B	| nessuno	| 0x16 (SYN) + pacchetto dati 0x58
0x54	| Lettura	| TIME PROGRAMMER	| nessuno	| 0x16 (SYN) + pacchetto dati 0x54
0x8b	| Lettura (ipotesi)	| KEY STATUS	| TBD	| TBD	| packetClass non implementata (FIXME nel codice); ipotesi basata sull'analogia col naming di INPUT/SYSTEM/USER STATUS
0x70	| Lettura (ipotesi)	| EVENT LOG	| TBD	| TBD	| packetClass non implementata (FIXME nel codice) — lettura log, vedi chat dedicata
0x91	| Scrittura singola istanza	| EXPANSION PROGRAMMING	| TBD	| TBD	| packetClass non implementata (FIXME nel codice)
0xe1	| Scrittura singola istanza (ipotesi)	| EXPANSIONS PROGRAMMING	| TBD	| TBD	| packetClass non implementata (FIXME nel codice); ipotesi basata sull'analogia col naming "PROGRAMMING"
0xe2	| Scrittura	| KEYPADS PROGRAMMING	| TBD	| TBD	| packetClass non implementata (FIXME nel codice)
0xe4	| Scrittura	| SET TIME PROGRAMMER	| pacchetto 131 byte, vedi [Time programmer e Day class commands](#time-programmer-e-day-class-commands)	| 0x16 (SYN)	| packetClass: `SetTimeProgrammer`
0xe5	| Scrittura	| SET PARTITIONS AND AREAS	| TBD	| TBD	| packetClass: `SetAreasAndPartitions`, mai documentata
0xe6	| Scrittura	| SET PHONE PARAMETERS	| pacchetto 20 byte, vedi [Parametri telefonici](#parametri-telefonici)	| 0x16 (SYN)	| packetClass: `SetPhoneParameters`
0xe8	| Scrittura	| SET C200B	| pacchetto 168 byte, vedi [C200B](#c200b)	| 0x16 (SYN)	| packetClass: `SetC200bParameters`
0xe9	| Scrittura	| SET SMS	| TBD	| TBD	| packetClass: `SetSMS`, mai documentata
0xea	| Scrittura	| SET PSTN GSM	| pacchetto 21 byte, vedi [PSTN GSM](#pstn-gsm)	| 0x16 (SYN)	| packetClass: `SetPSTNGSM`
0xeb	| Scrittura	| SET USERS	| TBD	| TBD	| packetClass: `SetUsers`, mai documentata
0xec	| Scrittura	| SET KEYS	| TBD	| TBD	| packetClass: `SetKeys`, mai documentata
0xa0	| Scrittura singola istanza	| SMS PROGRAMMING	| indice `SMSIndex` + testo ASCII, vedi [SMS](#sms)	| 0x16 (SYN)	| packetClass: `SMSProgramming`. Confermato via `ElkrommFacadeImpl.setSMS()`, DTO `SingleSMS`(index: `SMSIndex`, `SMS`), pienamente raggiungibile dalla facade pubblica
0x92	| Scrittura singola istanza	| KEYPAD PROGRAMMING	| TBD	| TBD	| packetClass: `KeypadProgramming`, vedi [Keypad programming](#keypad-programming) (dati presenti, codice comando non ancora collegato nel documento). Confermato via `ElkrommFacadeImpl.setKeyboard()`, DTO `SingleKeyboard`(index, `Keyboard`), pienamente raggiungibile dalla facade pubblica
0x52	| Lettura	| KEYPADS	| TBD	| TBD	| packetClass: `Keypads`, vedi [Keypads](#keypads) (dati presenti, codice comando non ancora collegato)
0x53	| Lettura	| READERS	| TBD	| TBD	| packetClass: `Readers`, mai documentata
0x93	| Scrittura singola istanza	| READER PROGRAMMING	| TBD	| TBD	| packetClass: `Reader`, mai documentata. Raggiungibile via `ElkrommFacadeImpl.setReader()`, ma marcato `// FIXME: da provare!?!?` nel codice — implementato ma **non verificato**. Nota: qui l'indice è implicito nel campo `address` del DTO `Reader` stesso, non c'è un wrapper `SingleReader` separato
0xa3	| Scrittura singola istanza	| KEY PROGRAMMING	| indice + dati `Key` (nome, specializzazione)	| 0x16 (SYN)	| packetClass: `KeyProgramming`. Confermato da DTO `SingleCredential`(index, `Key`)
0xe3	| Scrittura	| SET READERS	| TBD	| TBD	| packetClass: `SetReaders`, mai documentata
0xa1	| Scrittura singola istanza	| DAY CLASS CMDS	| lista `Command[]` per la day class, vedi [Time programmer e Day class commands](#time-programmer-e-day-class-commands)	| 0x16 (SYN)	| packetClass: `DayClassCommands`. Confermato via `ElkrommFacadeImpl.setDayClassCommands()`, pienamente raggiungibile dalla facade pubblica — qui l'"istanza" è la classe giorno stessa (`DayClass`: WORKING_DAY/PRE_HOLIDAY/HOLIDAY), non un indice numerico in un array



La centrale risponde con 0x06 (ACK) solo fintanto che non sia stato effettuato il login, compreso il pacchetto di login stesso. Hi-Connect quindi invia un SEND dopo il login per verificare che la risposta sia 0x16 (SYN). L'intera procedura di login è quindi costituita da tre pacchetti: HELLO, LOGIN, SEND. Se si invia il LOGIN senza HELLO, si ottiene in risposta 0x15 (NAK).

Il software Hi-Connect continua ad inviare pacchetti di tipo SEND anche mentre è in idle (uno ogni 500 ms). Da tentativi empirici, è bene che questi pacchetti vengano inviati, altrimenti ad un certo punto la centrale inizia a rispondere NAK anche ai pacchetti corretti. Hp.: keepalive?

La centrale risponde con 0x15 (NAK) nel caso in cui il pacchetto inviato da Hi-Connect contenga un checksum errato, o a fronte di altri errori (ad es. login non effettuato, sequenza di login errata).

Stream catturato (semplice sessione con connessione e refresh stato ingressi):

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
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
*...omissis...* | *(serie di SEND)*
000001B4	| 01 55 55 00 00 00 00 63 fe f3 03	| 	| 	| LOGOUT
	| 	| 0000008B	| 16	| SYN

## Settori

Riporta i dati delle aree (opzionali) e dei settori (aka partizioni).

### Cattura stream

Porzione di stream relativo alla lettura dei settori:  

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
000000C6	| 01 55 55 00 00 00 00 55 ff 11 01 03	| 	| 	| AREE & SETTORI
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

### Struttura payload

Definizione del payload "Aree e settori":  
` `  

Offset	| Significato	| Note
--------|---------------|-----
0	| Numero di aree	| Può valere zero se non sono utilizzate
1-4	| Bitmask settori assegnati all'area	| LSB = settore 1, un byte ogni area
5-28	| Nome prima area	|
29-52	| Nome seconda area	|
53-76	| Nome terza area	|
77-100	| Nome quarta area	|
101	| Numero di settori	|
102	| Bitmask self exclusion	| LSB = settore 1
103	| Bitmask arming block	| LSB = settore 1
104-119 | Entry delay	| 2 byte per settore (partendo da 104-105 per settore 1 e così via), big endian
120-135 | Exit delay	| 2 byte per settore (partendo da 120-121 per settore 1 e così via), big endian
136-159	| Nome del primo settore	|
160-183	| Nome del secondo settore	|
184-207	| Nome del terzo settore	|
208-231	| Nome del quarto settore	|
232-255	| Nome del quinto settore	|
256-279	| Nome del sesto settore	|
280-303	| Nome del settimo settore	|
304-327	| Nome del ottavo settore	|
328	| ?	|
329-332	| Checksum blocco	|

` `  
` `  

### Esempio payload

Esempio di cattura dati settori:  

`00 # numero di aree (max 4)`  
`01 00 00 00 # settori assegnati alle 4 aree (bit mask, un byte per ogni area)`  
`2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # 24 bytes, nomi delle aree`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`04 # numero di settori (max 8)`  
`0c # tipo (self exclusion, bitmask) Se bit a 1 self excl`  
*`11`* `03 # tipo (arming block, bitmask) Se bit a 1 arming block`  
`00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 # Entry/Exit delay 16 byte, 2 per settore, big endian`  
`00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 # Entry/Exit delay 16 byte, 2 per settore, big endian`  
`43 41 53 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # 24 bytes, nomi dei settori. CASA`  
`47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # GARAGE`  
`46 49 4e 45 53 54 52 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # FINESTRE`  
`50 45 52 53 49 41 4e 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # PERSIANE`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 `  
`20 `  
`ff ff e0 b6 # Checksum blocco`  

Altro esempio:  

`00 01 00 00 00`  
`2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`02 00` *`11`* `03 `  
`00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00 `  
`00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00`  
`43 41 53 41 f3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00` Quell'F3 sebra essere un intruso  
`47 41 52 41 47 45 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00`  
`2e 2e 2e  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20  20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20 20 20 20`  
`00`  
`ff ff df fb`

## Utenti

Lettura degli utenti.

### Cattura stream

Porzione di stream relativo alla lettura degli utenti:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
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

### Struttura payload

Definizione del payload "Utenti" — 26 byte per utente (`Credential.CREDENTIAL_SIZE`), 32 utenti consecutivi (`MAX_CREDENTIALS`) + 4 byte di checksum finale. Per ogni utente si ha:

Offset (relativo all'utente)	| Significato	| Note
--------|---------------|-----
0	| Abilitazione	| `Credential.Enabling`: 0=disabilitato, 1=abilitato, 2=sempre abilitato
1	| Partizioni associate	| Bitmask, LSB = partizione 1
2-25	| Nome	| 24 byte

### Esempio payload

Dati utenti:

`00 ff # Abilitazione (1 byte) + settori associati`  
`54 45 43 4e 49 43 4f 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 # TECNICO (24 byte)`  
`02 ff # 0x02 = sempre abilitato`  
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
`ff ff 9d d4 # Checksum blocco`

## Chiavi

Dati delle chiavi (inseritori).

### Cattura stream

Porzione di stream relativo alla lettura delle chiavi:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
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

### Struttura payload

Definizione del payload "Chiavi" — stessa struttura di [Utenti](#utenti) (26 byte per chiave, 32 chiavi + 4 byte di checksum), con il byte 0 che impacchetta due campi anziché uno. Per ogni chiave si ha:

Offset (relativo alla chiave)	| Significato	| Note
--------|---------------|-----
0	| Abilitazione (bit 0) + Specializzazione (bit 2-3)	| Abilitazione: `Credential.Enabling` sul bit meno significativo. Specializzazione: `Key.Specialization` su `(byte & 0x0C) >> 2` — 0=nessuna, 1=cambio stato partizione, 2=controllo accessi, 3=controllo accessi limitato alle partizioni associate
1	| Partizioni associate	| Bitmask, LSB = partizione 1
2-25	| Nome	| 24 byte

### Esempio payload

Dati chiavi:

`00 01 # Ablitazione + specializzazione e settori associati`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 # nome chiave (24 byte)`  
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
`ff ff 9a a0 # Checksum blocco`  

## Parametri

Parametri della conigurazione di sistema.

### Cattura stream

Porzione di stream relativo alla lettura dei parametri:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
0000011A	| 01 55 55 00 00 00 00 26 ff 30 03	| 	| 	| PARAMETRI
	| 	| 0000006A	| 16	| SYN
	| 	| 00000079	| 01 55 55 00 00 1e 00 26 **00 00 00 00 00 01 01 01 01 01 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 01 0f 00 ff ff fe 82** fa 16 03	

### Struttura payload

Definizione del payload "Parametri" (30 byte, offset 0-based), verificata direttamente su `serializer.ParametersEnablings`:

Offset	| Significato	| Note
--------|---------------|-----
0-4	| ?	| Non mappato da nessun campo del DTO
5, 7, 9	| Tempo bulgar	| `Time`: 0=30s, 1=60s, 2=90s, 3=180s, 4=9min. **I tre offset contengono sempre lo stesso valore** (`data[5]=data[7]=data[9]`)
6	| Tempo pre-allarme	| `Time`, stessi valori
8	| Tempo emergenza	| `Time`, stessi valori
10	| ?	| Non mappato da nessun campo del DTO
11	| Mancanza rete	| `PowerLack`: 0=1h, 1=2h, 2=4h
12	| ?	| Non mappato da nessun campo del DTO
13	| Conteggio allarmi	| `AlarmCount`: 0=nessuno, 1=due, 2=quattro, 3=sei, 4=otto
14	| Preavviso	| `Notice`: 0=nessuno, 5=5min, 10=10min, 15=15min, 20=20min
15	| Programmatore orario	| `Enabling`: 0=disabilitato, 1=abilitato
16	| DST	| Bitmask: bit0=abilitato, bit1=ultima domenica (invece di prima)
17-20	| ?	| Non mappato da nessun campo del DTO
21	| Mese OFF DST	| `Month`: 1-12
22	| Mese ON DST	| `Month`: 1-12
23	| LAN	| `Enabling`: **qui `0x00`=disabilitato** (convenzione invertita rispetto agli altri campi enable/disable del protocollo, dove tipicamente 0=disabilitato ma con valore di default diverso)
24	| Play	| Bitmask: bit0=fault, bit1=settori, bit2=sistema, bit3=servizio
25	| Help	| Bit 7 (`0x80`)=abilitato, bit 0-2=indirizzo tastiera - 1
26-29	| Checksum blocco	|

### Esempio payload

Dati parametri:

`00 00 00 00 00 01 01 01 01 01 00 00 00 00 05 00` *`11`* `03 55 55 55 55 `  
`0a` *`11`* `03 # DST ottobre/marzo`  
`01 0f 00`  
`ff ff fe 82 # Checksum blocco`

Porzione di stream relativo alla scrittura dei parametri:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
000002E8	| 01 55 55 00 00 1e 00 96 **00 00 00 00 00 02 11 01 02 11 01 02 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 11 01 0f 00 ff ff fe 7f** f9 a6 03	| 	| 	| PARAMETRI
	| 	| 000000CB	| 16	| SYN

Dati parametri:

`00 00 00 00 00 02` *`11`* `01 02` *`11`* `01 02 00 00 00 00 05 00` *`11`* `03 55 55 55 55 0a` *`11`* `03` *`11`* `01 0f 00 `  
`ff ff fe 7f # Checksum blocco`

Porzione di stream relativo alla scrittura dei parametri:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
000004B8	| 01 55 55 00 00 1e 00 96 **00 00 00 00 00 11 01 11 01 11 01 11 01 11 01 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 11 01 0f 00 ff ff fe 82** f9 a6 03	| 	| 	| PARAMETRI
	| 	| 000000FE	| 16	| SYN

Dati parametri:

`00 00 00 00 00` *`11`* `01` *`11`* `01` *`11`* `01` *`11`* `01` *`11`* `01 00 00 00 00 05 00` *`11`* `03 55 55 55 55 0a` *`11`* `03` *`11`* `01 0f 00 `  
`ff ff fe 82 # Checksum blocco`

## Numeri telefonici

Dati dei numeri telefonici e dei relativi eventi che vengono trasmessi.

### Cattura stream

Porzione di stream relativo alla scrittura dei numeri telefonici:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
000003B9	| 01 55 55 02 00 8c 00 e7 **ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff** 87 85 03	| 	| 	| MODIFICA NUMERI TELEFONICI
	| 	| 000000B3	| 16	| SYN
00000450	| 01 55 55 02 11 01 8c 00 e7 **ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00** c9 e8 03			
	| 	| 000000B4	| 16	| SYN
000004E8	| 01 55 55 02 02 80 00 e7 **00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 55 80** fa ec 03			
	| 	| 000000B5	| 16	| SYN

### Struttura payload

Definizione del payload "Phone numbers": sono riportati 12 numeri telefonici, ognuno dei quali presenta i seguenti dati:  
` `  

Offset (relativo al numero telefonico)	| Significato	| Note
----------------------------------------|---------------|-----
0-13	| Numero telefonico	| Numero telefonico codificato in BCD (14 byte - 28 cifre)
14	| Bitmask settori associati	| LSB = settore 1
15	| Rete telefonica	| 00 = PSTN, 01 = GSM, 02 = LAN
16	| Modalità di invio	| 00 = Voce, 01 = IDP, 02 = ADF, 04 = Modem, 06 = SMS, 07 = C200b

**Numeri LAN/IP**: quando il campo "Rete telefonica" vale `0x02` (LAN), il numero di telefono viene sostituito da un indirizzo nel formato fisso `DDD.DDD.DDD.DDD:DDDDD` (ogni ottetto zero-paddato a 3 cifre, parte finale a 5 cifre). Codifica **confermata direttamente dal codice** — `serializer.PhoneNumber.serialize()` implementa esattamente questa logica: ogni cifra decimale in BCD, il punto `.` come nibble `0x0B`, i due punti `:` come nibble `0x0C`, con lo stesso commento originale dell'autore (`"In generale gli IP sono 001B002B003B004C00005"`) presente sia lì sia in `ClientConnection`. Il padding dell'ultimo nibble spaiato (per un numero dispari di cifre/separatori, come nell'esempio sotto) è `0x0F` — anch'esso nel codice (`bcdByte | 0x0F`).

Esempio verificato con una cattura reale, per l'indirizzo `192.168.001.100:00080`:

* sequenza cifre/separatori: `1 9 2 B 1 6 8 B 0 0 1 B 1 0 0 C 0 0 0 8 0`
* byte risultanti: `19 2b 16 8b 00 1b 10 0c 00 08 0f` (11 byte, l'ultimo nibble `f` di padding) — combacia esattamente con la cattura

Coerente con il `// FIXME: IP addresses in phone numbers!` ancora presente nel setter `PhoneNumber.setPhoneNumber()` del DTO — quel commento segnala solo che il DTO non valida/riconosce ancora esplicitamente il formato IP (accetta la stringa così com'è), non che la codifica sia incerta: la codifica stessa, lato serializer, è completa.

**Tabella di assegnazione eventi**: dopo i 12 record telefonici (204 byte) segue, negli offset assoluti restituiti da `PhoneNumber.Event.getOffset()`, una **word a 2 byte** per ciascun evento riportabile:

* La word è un **bitmask dei telefoni** assegnati a quell'evento (bit *i* = telefono *i+1*), confermato empiricamente: cattura con solo il telefono 1 abilitato su un evento → `0x0001`; stesso evento con telefono 1 **e** 2 → `0x0003`
* Gli offset **non sono consecutivi/ordinati** come nell'enum: sono sparsi nei restanti ~200 byte del payload (tra il termine dei record telefonici e il checksum), con ampie zone non utilizzate tra un evento e l'altro — coerente con le lunghe sequenze di `00 00 00 00...` osservate nei dump
* Il serializer inoltre **duplica** il valore di alcuni eventi su più offset contemporaneamente (mirror, come già visto per [C200B](#c200b)): `PNSCE_BURGLAR_ALARM` → anche `0x00e8`, `0x00ec`, `0x00f0`; `PNSCE_INPUT_INCLUSION_EXCLUSION` → anche `0x0148`; `PNSCE_TAMPERING` → anche `0x0104`, `0x0170`; `PNSCE_SYSTEM_FAULT` → anche `0x0130`

### Esempio payload

Dati telefoni:

`20 1f ff ff ff ff ff ff ff ff ff ff ff ff # Numero telefonico codificato in BCD (14 byte — ElkrommFacade.PHONE_NUMBER_LENGTH = 28 cifre)`  
`ff # Maschera partizioni associate (bitmask, ElkrommUtils.packPartitions/unpackPartitions; qui vale 0xff = tutte le partizioni, motivo per cui inizialmente si confondeva col padding standard e sembrava parte del numero)`  
`00 # Rete telefonica (00 = PSTN, 01 = GSM, 02 = LAN)`  
`00 # Modalità di invio (00 = Voce, 01 = IDP, 02 = ADF, 04 = Modem, 06 = SMS, 07 = C200b)`  
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
`00` *`11`* `01 00 00 # Flag attivazione eventi per i 12 numeri telefonici (2 byte, LSB = telefono 1) + 2 byte ignoti (Hp: protocollo C200b)`  
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
`ff ff 57 75 # Checksum blocco`

## Parametri telefonici

Parametri del compositore telefonico.

### Struttura payload

Definizione del payload "Parametri telefonici" (20 byte, offset 0-based), verificata su `serializer.PhoneParameters`:

Offset	| Significato	| Note
--------|---------------|-----
0-4	| ?	| Non mappato da nessun campo del DTO
5	| Ritardo chiamata	| `Enabling`: 0=disabilitato, 1=abilitato
6	| ?	| Non mappato da nessun campo del DTO
7	| Richiamata	| `ReturnCall`: 0=disabilitato, 1=tipo A, 2=tipo B
8	| Telesorveglianza	| `Enabling`
9	| Modalità invio messaggi vocali	| `VoiceMessagesSendingMode`: 0=nessuno, 1-4=modalità 1-4
10	| ?	| Non mappato da nessun campo del DTO
11	| Frequenza chiamata di test ciclica	| `CyclicTestCallFrequency`: 0=disabilitato, 1=24h, 2=a sistema inserito
12	| Indice numero telefonico per la chiamata di test	|
13	| Ora chiamata di test	|
14	| Minuto chiamata di test	|
15	| Intervallo chiamata di test	| `CyclicTestCallInterval`: 0=1h, 1=4h, 2=8h, 3=12h, 4=24h, 5=48h, 6=72h, 7=96h, 8=120h, 9=144h, 10=168h
16-19	| Checksum blocco	|

**Nota**: mappatura offset→campo verificata direttamente sul serializer; il comando `0xe6` non è mai stato collegato in tabella a una lettura `0x56 PHONE PARAMETERS` corrispondente — verificare se condividono lo stesso formato dati, come avviene per `0x26`/`0x96`.

## PSTN GSM

Parametri delle reti telefoniche tradizionale (PSTN) e cellulare (GSM), ove installata la scheda GSM.

### Struttura payload

Definizione del payload "PSTN GSM" (21 byte, offset 0-based), verificata su `serializer.PSTNGSM`:

Offset	| Significato	| Note
--------|---------------|-----
0	| Abilita rete PSTN	| `Enabling`
1	| Paese	| `Country`: 0=Italia, 1=Francia, 2=Germania, 3=Rep. Ceca, 4=Polonia, 5=Spagna, 6=Portogallo, 7=Grecia, 8=Inghilterra
2-3	| ?	| Non mappato da nessun campo del DTO
4	| Cifra accesso PABX locale	| `PABXLocalAccessDigit`: 0-9, 0xff=disabilitato
5	| Controllo tono	| `Enabling`
6	| Controllo risposta	| `Enabling`
7	| Test linea PSTN	| `PSTNLineTestFrequency`: 0=disabilitato, 1=24h, 2=a sistema inserito
8	| Squilli segreteria PSTN	| `PSTNAnsweringMachineRings`: 0=disabilitato, 2/4/8=numero di squilli
9	| Abilita rete GSM	| `Enabling`
10	| Segreteria GSM (nessuno squillo)	| `Enabling`
11	| SMS in ingresso	| `Enabling`
12-14	| PIN GSM	| BCD, 3 byte; `0xff 0xff 0xff` = nessun PIN impostato
15	| Mese di scadenza	|
16	| Anno di scadenza	|
17-20	| Checksum blocco	|

## SMS

Comando `0x59 SMS` (lettura bulk di tutti i messaggi, vedi [Tipologie di comando](#tipologie-di-comando))

### Struttura payload

Payload 364 byte, un blocco di 40 caratteri per messaggio (padding `0xff`), **nello stesso ordine** dell'enum `SMSs.SMSIndex`.

Offset	| Significato	| Note
--------|---------------|-----
0-39	| Messaggio 1	| `SMS_BURLGAR` (intrusione), ASCII, padding `0xff`
40-79	| Messaggio 2	| `SMS_TECHNICAL_ALARM_1`
80-119	| Messaggio 3	| `SMS_TECHNICAL_ALARM_2`
120-159	| Messaggio 4	| `SMS_TECHNICAL_ALARM_3`
160-199	| Messaggio 5	| `SMS_FIRE` (incendio)
200-239	| Messaggio 6	| `SMS_PARTITION_ON` (partizione attivata)
240-279	| Messaggio 7	| `SMS_PARTITION_OFF` (partizione disattivata)
280-319	| Messaggio 8	| `SMS_TAMPERING` (manomissione)
320-359	| Messaggio 9	| `SMS_NOTICE` (nota)
360-363	| Checksum blocco	|

Comando `0xa0 SMS PROGRAMMING` (scrittura singola istanza, confermata raggiungibile via `ElkrommFacadeImpl.setSMS()`, vedi [Tipologie di comando](#tipologie-di-comando)): payload 41 byte, verificato su `serializer.SingleSMS`.

Offset	| Significato	| Note
--------|---------------|-----
0	| Indice	| **1-based** (`SMSIndex.ordinal() + 1`): 1=burglar, 2-4=tech.alarm 1-3, 5=fire, 6=partition on, 7=partition off, 8=tampering, 9=notice
1-40	| Messaggio	| 40 byte ASCII, padding `0xff`

### Esempio payload

Dump catturato:

```
0000: 01 41 67 61 69 6e 3f ff  ff ff ff ff ff ff ff ff
0010: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff
0020: ff ff ff ff ff ff ff ff  ff
```

Il testo `41 67 61 69 6e 3f` decodifica in ASCII come **"Again?"** — testo di prova inserito dall'autore in Hi-Connect. Indice `0x01` = `SMS_BURLGAR`.

## C200B

Comando: `0xe8 SET C200B` (scrittura, mai letta/collegata a `0x58 C200B` in tabella comandi).

### Struttura payload

Payload 168 byte, verificato su `serializer.C200bParameters`.

Offset	| Significato	| Note
--------|---------------|-----
0x00-0x31	| ?	| Non mappato da nessun campo del DTO
0x32	| Tampering	| `Event.C2PE_TAMPERING`; il serializer scrive lo stesso valore anche a `0x40` e `0x5b` (mirror, non eventi distinti)
0x33	| ?	|
0x34	| Batteria scarica	| `Event.C2PE_LOW_BATTERY`
0x35	| Mancanza rete	| `Event.C2PE_MAINS_POWER`
0x36-0x37	| ?	|
0x38	| Allarme intrusione	| `Event.C2PE_BURGLAR_ALARM`; mirror anche a `0x39`, `0x3a`, `0x3b`
0x39-0x3b	| (mirror di 0x38)	|
0x3c	| Pre-allarme	| `Event.C2PE_PRE_ALARM`
0x3d-0x3f	| ?	|
0x40	| (mirror di 0x32, tampering)	|
0x41	| Panico	| `Event.C2PE_PANIC`
0x42	| Panico silenzioso	| `Event.C2PE_SILENT_PANIC`
0x43	| Incendio	| `Event.C2PE_FIRE_ALARM`
0x44-0x46	| ?	|
0x47	| Emergenza medica	| `Event.C2PE_MEDICAL_EMERGENCY`
0x48	| Guasto sistema	| `Event.C2PE_SYSTEM_FAULT`; mirror anche a `0x4b`
0x49	| ?	|
0x4a	| Attivazione/disattivazione partizioni	| `Event.C2PE_PARTITIONS_ON_OFF`
0x4b	| (mirror di 0x48, guasto sistema)	|
0x4c	| Attivazione/disattivazione sistema	| `Event.C2PE_SYSTEM_ON_OFF`; mirror anche a `0x4d`, `0x4f`
0x4d	| (mirror di 0x4c)	|
0x4e	| Rapina	| `Event.C2PE_HOLD_UP`
0x4f	| (mirror di 0x4c)	|
0x50	| Esclusione/inclusione ingresso	| `Event.C2PE_INPUT_INCLUSION_EXCLUSION`; mirror anche a `0x51`
0x51	| (mirror di 0x50)	|
0x52	| Chiamata di test ciclica	| `Event.C2PE_CYCLICAL_TEST_CALL`
0x53	| Manutenzione	| `Event.C2PE_MAINTENANCE`
0x54	| Codice falso	| `Event.C2PE_FALSE_CODE`
0x55-0x57	| ?	|
0x58	| Allarme tecnico tipo 1	| `Event.C2PE_TECHNOLOGICAL_ALARM_TYPE_1`
0x59	| Allarme tecnico tipo 2	| `Event.C2PE_TECHNOLOGICAL_ALARM_TYPE_2`
0x5a	| Allarme tecnico tipo 3	| `Event.C2PE_TECHNOLOGICAL_ALARM_TYPE_3`
0x5b	| (mirror di 0x32, tampering)	|
0x5c-0x63	| ?	|
0x64-0xa3	| Codici ingresso	| 64 byte, uno per ingresso logico (`MAX_LOGICAL_INPUTS`), `0xff` se l'ingresso non esiste
0xa4-0xa7	| Checksum blocco	|

## Time programmer e Day class commands

Comandi coinvolti: `0xe4 SET TIME PROGRAMMER` (scrittura, blocco intero) e `0xa1 DAY CLASS CMDS` (scrittura singola istanza, confermata raggiungibile via `ElkrommFacadeImpl.setDayClassCommands()`). L'"istanza" per `DAY_CLASS_CMDS` è la classe giorno stessa, enum `DayClassCommands.DayClass`: `DCCDC_WORKING_DAY` (`0x00`), `DCCDC_PRE_HOLIDAY` (`0x01`), `DCCDC_HOLIDAY` (`0x02`).

### Struttura payload

Definizione del payload "SET TIME PROGRAMMER" (131 byte), verificata su `serializer.TimeProgrammer`:

Offset	| Significato	| Note
--------|---------------|-----
0-39	| Comandi giorno lavorativo	| 8 comandi × 5 byte, vedi tabella "Comando" sotto
40-79	| Comandi pre-festivo	| 8 comandi × 5 byte
80-119	| Comandi festivo	| 8 comandi × 5 byte
120	| Classe giorno — lunedì	| `DayClass`: 0=lavorativo, 1=pre-festivo, 2=festivo
121	| Classe giorno — martedì	|
122	| Classe giorno — mercoledì	|
123	| Classe giorno — giovedì	|
124	| Classe giorno — venerdì	|
125	| Classe giorno — sabato	|
126	| Classe giorno — domenica	|
127-130	| Checksum blocco	|

Definizione del payload "DAY CLASS CMDS" (41 byte, scrittura singola istanza — nessun checksum di blocco, coerente con le altre scritture di singola istanza), verificata su `serializer.DayClassCommands`:

Offset	| Significato	| Note
--------|---------------|-----
0	| Classe giorno	| `DayClass`, identifica quale dei tre set di comandi si sta scrivendo
1-40	| 8 comandi	| 5 byte ciascuno, vedi tabella "Comando" sotto

**Comando** (`Command` DTO, 5 byte, usato sia dentro `SET_TIME_PROGRAMMER` sia in `DAY_CLASS_CMDS`; se `Action` vale `CA_NONE` i byte 1-4 non vengono scritti dal serializer):

Offset (relativo al comando)	| Significato	| Note
--------|---------------|-----
0	| Azione	| `Command.Action`: 0=nessuna, 1=abilita, 2=disabilita
1	| Oggetto	| Indice dell'oggetto (settore o utente, in base al byte successivo)
2	| Tipo oggetto	| `Command.ObjectType`: `0x10`=settori, `0x40`=utente — commento nel codice segnala "more to come: keys, outputs", quindi la lista potrebbe non essere completa
3	| Ora	|
4	| Minuto	|

Esempio dal dump `WORKING_DAY_CMD`: comando 1 → ora 1, minuto 2, enable, user 13 (dedotto dal commento originale, non riverificato byte-per-byte contro l'esadecimale).

## Input

Struttura condivisa (38 byte), usata all'interno di ogni espansione (fino a 8 per espansione, [Blocco B](#blocco-b)) e per i due ingressi di bordo di ciascun [Keypad](#keypads)/[Reader](#readers). Uno slot con `logicNumber` (offset 0) a `0x00` è considerato non usato: la centrale non ne inizializza necessariamente gli altri campi, e `serializer.Input` lo restituisce come `null` in fase di lettura.

### Struttura payload

Offset	| Significato	| Note
--------|---------------|-----
0	| Numero logico dell'ingresso	| `0x00` = slot non usato
1	| Configurazione	| `Configuration`: 0=non usato, 1=NC, 2=NA, 3=NC bilanciato singolo, 4=NC doppio bilanciato, 7=shock, 8=roller
2	| Specializzazione	| `Specialization`: vedi enum, 20 valori (immediato, ritardato, primo ingresso, incendio, tamper, ...)
3	| Sensibilità (bit alti) + Flags (bit bassi)	| Sensibilità: `0x80`=bassa, `0x40`=media, `0x00`=alta (solo per shock/roller). Flags (bitmask): `0x01`=esclusione abilitata, `0x02`=doppio rilascio, `0x08`=OR settori. **Attenzione**: il bit `0x10` non è gestito da nessun campo — la centrale (almeno un MP-508 v03.01) lo alza in modo apparentemente casuale su alcuni ingressi; va escluso dal calcolo del checksum di blocco (vedi [Blocco B](#blocco-b))
4	| Telecamera associata	| `Video`: 0=nessuna, poi bitmask 0x10/0x20/0x40/0x80 per le camere 1-4
5	| Partizioni associate	| Bitmask, LSB = partizione 1
6-29	| Nome	| 24 byte
30-33	| ?	| Non mappato da nessun campo del DTO
34	| Ritardo	| `Delay`: 0=5s, 1=10s, 2=30s, 3=60s, 4=90s, 5=5min, 6=20s
35-37	| ?	| Non mappato da nessun campo del DTO

## Output

Struttura condivisa (37 byte), usata all'interno di ogni espansione (fino a 6 per espansione, [Blocco B](#blocco-b)). Come per [Input](#input), uno slot con `logicNumber` (offset 0) a `0x00` è considerato non usato.

### Struttura payload

Offset	| Significato	| Note
--------|---------------|-----
0	| Numero logico dell'uscita	| `0x00` = slot non usato
1	| Tipo	| `Type`: 0=non usata, 1=normalmente bassa, 2=normalmente alta
2	| Partizioni associate	| Bitmask, LSB = partizione 1
3	| Specializzazione	| `Specialization`: 31 valori (burglar, pre-alarm, tamper, gong, buzzer, stato partizioni, ...)
4-7	| ?	| Non mappato da nessun campo del DTO
8-31	| Nome	| 24 byte
32-36	| ?	| Non mappato da nessun campo del DTO

## Blocco B

Dati delle espansioni.

### Cattura stream

Porzione di stream relativo alla lettura del blocco B:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
000001EB	| 01 55 55 00 00 00 00 51 ff 05 03	| 	| 	| BLOCCO B
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

### Struttura payload

Ogni espansione occupa un blocco fisso di 559 byte (`EXPANSION_SIZE`), ripetuto per il numero di espansioni presenti (0-7); il blocco complessivo termina con i consueti 4 byte di checksum.

Offset (relativo all'espansione)	| Significato	| Note
--------|---------------|-----
0	| ?	| Non mappato da nessun campo del DTO
1	| Indirizzo bus	| `0x00` = espansione non presente/non usata
2	| ?	| Non mappato da nessun campo del DTO
3-6	| Versione firmware	| stringa ASCII, es. `"0301"`
7-310	| 8 ingressi	| 38 byte ciascuno, vedi [Input](#input)
311-532	| 6 uscite	| 37 byte ciascuna, vedi [Output](#output)
533-556	| Nome	| 24 byte
557-558	| ?	| **Non fornito dal client in scrittura** (Hi-Connect invia sempre `0x00 0x00` su `EXPANSION PROGRAMMING`/0x91), valorizzato dalla centrale in lettura con contenuto non ancora identificato — va escluso dal calcolo del checksum di blocco (vedi nota sotto)

**Nota sul checksum**: su una centrale MP-508 v03.01 reale, il checksum di blocco calcolato secondo l'algoritmo standard (vedi [Tipologie di comando](#tipologie-di-comando)) non combacia con quello incorporato dalla centrale, a meno di azzerare preventivamente, per ciascuna espansione: i 2 byte a offset relativo 557-558 sopra descritti, **e** il bit `0x10` a offset relativo 10 (byte 3 del primo... in realtà di *ogni* ingresso, offset relativo `7 + i*38 + 3` per l'ingresso i-esimo) — vedi [Input](#input). Il codice attuale (`serializer.Expansions`) applica questa doppia correzione prima di verificare il checksum. Non è chiaro se questo comportamento sia specifico del firmware v03.01 o generale; va verificato su altre versioni/modelli.

### Esempio payload

Dati blocco B:

`00 00 00`   
`30 33 30 31 # 0301 versione centrale`  
`01 04` *`11`* `03 09 00 01 # Ingresso centrale: numero logico, configurazione (4 = NC Double Bal, 2 = NO), specializzazione (3 = way, 0 = immediate, 5 = first/last entry, 7 = tecno type 1), escludibile etc?, aux?, settori`  
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
`01 02 ff 1d 00 00 00 00 # Uscita centrale: numero logico, tipo uscita (1 = NL, 2 = NH), settori, specializzazione (1d = burglar/tamper, 14 = and TC, 15 = or TC, 0e = tel fault, 01 = pre alarm, 00 = burglar, 03 = tampering), ?, ?,`   `?, ?`  
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
`00 01 00 # Indirizzo nuova unità`  
`30 32 30 30 # 0200 versione espansione`  
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
`00 02 00 # Indirizzo nuova unità`  
`30 32 30 30 # 0200 versione espansione`  
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
`ff ff 2c af # Checksum blocco`

## Keypads

Dati delle tastiere.

### Cattura stream

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
  | 01 55 55 00 00 00 00 52 ff 04 03	|	|	| KEYPADS
  |	|	| 16 | SYN
  |	|	| 01 55 55 01 00 8c 00 52 **01 00 30 34 31 30 00 00 01 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 06 ff 06 49 4e 47 52 45 53 53 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 b0 00 02 00 30 32 30 30 00 00 01 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ec 0e 03
| 01 55 55 00 00 00 00 65 fe f1 03	|	|	| SEND
  |	|	| 16 | SYN
  |	|	| 01 55 55 01 01 56 00 52 **20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 06 ff 00 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 e5 00 ff ff e3 8b** ef 9f 03

### Struttura payload

Definizione del payload "Keypads":  
` `  

Offset	| Significato	| Note
--------|---------------|-----
0	| Indirizzo	| Indirizzo della tastiera (base 1)
1	| ?	|
2-5	| Versione	| ASCII
6-43	| Primo ingresso della tastiera	| 38 byte, vedi [Input](#input)
44-81	| Secondo ingresso della tastiera	| 38 byte, vedi [Input](#input)
82	| Bitmask abilitazioni	| GONG, ENTRY, EXIT, MASKING, FIRE, PANIC, HELP
83	| Bitmask settori associati	| LSB = settore 1
84	| Bitmask feature audio	| CAPABLE, ENABLED
85-108	| Nome della tastiera	|
109-110	| ?	|
111	| Seconda tastiera	| Si ripetono i campi precedenti
...	|	|

` `  
` `  

### Esempio payload

Esempio di cattura dati tastiere:  

`01 @ Address`  
`00`  
`30343130 # Version`  
`000001090001 # Ingresso 1`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`000000090001 # Ingresso 2`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`06 # Abilitazioni`  
`ff # Settori associati`  
`06 # Audio`  
`494e47524553534f00000000000000000000000000000000 # Name`  
`b000`  

`02 # Address`  
`00`  
`30323030 # Version`  
`000001090001 # Ingresso 1`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`000000090001 # Ingresso 2`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`06 # Abilitazioni`  
`ff # Settori associati`  
`00 # Audio`  
`474152414745000000000000000000000000000000000000 # Name`  
`e500`  

`ffffe38b # Checksum blocco`  

## Readers

Struttura di un reader (113 byte), letta in blocco con `READERS` (0x53) e scrivibile singolarmente con `READER PROGRAMMING` (0x93, non verificato su hardware reale — vedi [Tipologie di comando](#tipologie-di-comando)).

### Struttura payload

Offset	| Significato	| Note
--------|---------------|-----
0	| Indirizzo bus	| Identifica il reader anche in scrittura singola (non c'è un wrapper `SingleReader` separato)
1-5	| ?	| Non mappato da nessun campo del DTO
6-43	| Primo ingresso del reader	| 38 byte, vedi [Input](#input)
44-81	| Secondo ingresso del reader	| 38 byte, vedi [Input](#input)
82	| LED 1	| Partizione associata (`ElkrommFacade.Partition`), `0x00` = non usato
83	| LED 2	| Partizione associata
84	| LED 3	| Partizione associata
85	| LED 4	| Partizione associata
86	| Bitmask abilitazioni	| `Reader.Enablings`: solo `MASKING` (0x01) noto
87-110	| Nome	| 24 byte
111-112	| ?	| Non mappato da nessun campo del DTO

## Keypad programming

Scrittura parametri singola tastiera

### Struttura payload

Vedi [Keypads](#keypads), offset 0-110. L'indirizzo della singola tastiera è entrocontenuto all'offset relativo 0.

Dati keypad:

`Dumping KEYPAD_PROGRAMMING, payload size: 111`  
`0000: 01 00 30 34 31 30 00 00  01 09 00 01 2e 2e 2e 20   ..0410.. .......  `  
`0010: 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20                     `  
`0020: 20 20 20 20 00 00 00 00  00 ff ff 00 00 00 00 09       .... .￿￿..... `  
`0030: 00 01 2e 2e 2e 20 20 20  20 20 20 20 20 20 20 20   .....             `  
`0040: 20 20 20 20 20 20 20 20  20 20 00 00 00 00 00 ff              .....￿ `  
`0050: ff 00 07 ff 06 49 4e 47  52 45 53 53 4f 00 00 00   ￿..￿.ING RESSO... `  
`0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00      ........ .......`  
