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
* **Scrittura singola istanza**: il client invia il codice comando con l'indice dell'elemento all'interno del proprio array (es. l'i-esimo utente, tastiera, lettore, chiave, SMS, day class) seguito dai soli dati di quell'istanza; la centrale risponde con SYN. A differenza della scrittura di blocco, non richiede di ritrasmettere l'intero array.

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
00000028	| 01 55 55 00 00 00 00 62 fe f4 03	| 	| 	| ADDRESSES?
	| 	| 00000003	| 16	| SYN
	| 	| 00000004	| 01 55 55 00 00 06 00 62 **01 01 00 02 01 02** fe e7 03	| ADDRESSES?
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

Dati utenti:

`00 ff # Abilitazione (1 byte) + Aree e settori associati?`  
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

Dati chiavi:

`00 01 # aree settori?`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 # nome chiave? (24 byte)`  
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

Porzione di stream relativo alla lettura dei parametri:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
0000011A	| 01 55 55 00 00 00 00 26 ff 30 03	| 	| 	| PARAMETRI
	| 	| 0000006A	| 16	| SYN
	| 	| 00000079	| 01 55 55 00 00 1e 00 26 **00 00 00 00 00 01 01 01 01 01 00 00 00 00 05 00 11 03 55 55 55 55 0a 11 03 01 0f 00 ff ff fe 82** fa 16 03	

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

Mappa (parziale) byte → significato, dedotta da `ClientConnection` (payload di scrittura, 30 byte, offset 0-based). Valori enum lato codice: `ParametersEnablings.Time`, `.AlarmCount`, `.PowerLack`, `.Play` (bitmask), `.Notice`, `.DST` (bitmask), `.Month`:

* **offset 5, 7, 9** — timer (30s/60s/90s/180s/9min); il commento originale li raggruppa genericamente come "bulgar/pre-alarm/emerg" tutti a 30s nella cattura, il mapping preciso offset↔timer specifico resta da confermare
* **offset 8** — emergency time
* **offset 6** — pre-alarm time
* **offset 13** — alarm count (`0x00`=nessuno, `0x01`=due, `0x02`=quattro, `0x03`=sei, `0x04`=otto)
* **offset 11** — power lack (`0x00`=1h, `0x01`=2h, `0x02`=4h)
* **offset 14** — notice (`0x00`=nessuno, `0x05`=5min, `0x0a`=10min, `0x0f`=15min, `0x14`=20min)
* **offset 15** — time programmer enable (`0x01`=abilitato)
* **offset 16** — auto DST (`0x01`=abilitato) + sunday (bit `0x02`: `0`=prima, `1`=ultima)
* **offset 21** — mese OFF DST (es. ottobre)
* **offset 22** — mese ON DST (es. marzo)
* **offset 23** — LAN (`0x00`=disabilitato, invece del solito `0x01`)
* **offset 24** — play flags (bitmask: bit0 `0x01`=fault, bit1 `0x02`=sectors, bit2 `0x04`=system, bit3 `0x08`=service)
* **offset 25** — help message enable (`0x80`) + nibble basso = indirizzo tastiera - 1

**Nota**: numerazione offset dedotta da commenti originali dell'autore scritti durante il reverse engineering, non riverificata sistematicamente byte-per-byte contro il dump — da considerare un buon punto di partenza, non un mapping definitivo.

## Numeri telefonici

Porzione di stream relativo alla scrittura dei numeri telefonici:

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
000003B9	| 01 55 55 02 00 8c 00 e7 **ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff f0 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff** 87 85 03	| 	| 	| MODIFICA NUMERI TELEFONICI
	| 	| 000000B3	| 16	| SYN
00000450	| 01 55 55 02 11 01 8c 00 e7 **ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 ff ff ff ff ff ff ff ff ff ff ff ff ff ff 00 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00** c9 e8 03			
	| 	| 000000B4	| 16	| SYN
000004E8	| 01 55 55 02 02 80 00 e7 **00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ff ff 55 80** fa ec 03			
	| 	| 000000B5	| 16	| SYN

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

**Tabella di assegnazione eventi**: dopo i 12 record telefonici segue una tabella con un blocco da 4 byte per ciascun evento riportabile (stesso ordine e stessi eventi dell'enum `PhoneNumber.Event`, che ne fissa già gli offset — vedi Javadoc), con questa struttura dedotta da più catture comparate in `ClientConnection`:

* **byte 0** — maschera partizioni (valori osservati: `0x00`, `0x02`, `0x04`, `0x08`; il significato esatto del bit resta da confermare)
* **byte 1** — **bitmask dei telefoni** assegnati a quell'evento (bit *i* = telefono *i+1*), confermato empiricamente: cattura con solo il telefono 1 abilitato su un evento → `0x01`; stesso evento con telefono 1 **e** 2 abilitati → `0x03`. Anche i valori "a scalare" osservati nelle prime catture (`0x01`, `0x03`, `0x05`, `0x09`, `0x11`, `0x21`, `0x41`, `0x81` per eventi diversi con solo telefono 1) sono coerenti con questa lettura se combinati col byte 0
* **byte 2-3** — sempre `0x00` in tutte le catture disponibili, probabile riservato

**Numeri LAN/IP**: quando il campo rete vale `0x02` (LAN), il numero di telefono viene sostituito da un indirizzo nel formato fisso `DDD.DDD.DDD.DDD:DDDDD` (ogni ottetto zero-paddato a 3 cifre, parte finale a 5 cifre). Codifica **confermata direttamente dal codice** — `serializer.PhoneNumber.serialize()` implementa esattamente questa logica: ogni cifra decimale in BCD, il punto `.` come nibble `0x0B`, i due punti `:` come nibble `0x0C`, con lo stesso commento originale dell'autore (`"In generale gli IP sono 001B002B003B004C00005"`) presente sia lì sia in `ClientConnection`. Il padding dell'ultimo nibble spaiato (per un numero dispari di cifre/separatori, come nell'esempio sotto) è `0x0F` — anch'esso nel codice (`bcdByte | 0x0F`), non un'ipotesi.

Esempio verificato con una cattura reale, per l'indirizzo `192.168.001.100:00080`:

* sequenza cifre/separatori: `1 9 2 B 1 6 8 B 0 0 1 B 1 0 0 C 0 0 0 8 0`
* byte risultanti: `19 2b 16 8b 00 1b 10 0c 00 08 0f` (11 byte, l'ultimo nibble `f` di padding) — combacia esattamente con la cattura

Coerente con il `// FIXME: IP addresses in phone numbers!` ancora presente nel setter `PhoneNumber.setPhoneNumber()` del DTO — quel commento segnala solo che il DTO non valida/riconosce ancora esplicitamente il formato IP (accetta la stringa così com'è), non che la codifica sia incerta: la codifica stessa, lato serializer, è completa.

## Parametri telefonici

Comando: `0xe6 SET PHONE PARAMETERS` (scrittura, mai letta/collegata a `0x56 PHONE PARAMETERS` in tabella comandi — verificare se condividono lo stesso formato dati, come avviene per `0x26`/`0x96`). Payload di scrittura, 20 byte, dump catturato da `ClientConnection`:

```
0000: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00
0010: 00 00 00 00
Checksum: 00000000
```

Mappa byte (offset 0-based), enum lato codice `PhoneParameters.*`:

* **offset 5** — call delay enable (`0x01`)
* **offset 7** — return call (`ReturnCall`: `0x00`=disabilitato, `0x01`=tipo A, `0x02`=tipo B)
* **offset 8** — remote surveillance enable (`0x01`)
* **offset 9** — voice messages sending mode (`VoiceMessagesSendingMode`: `0x00`=nessuno, `0x01`-`0x04`=modalità 1-4)
* **offset 11** — cyclic test call (`CyclicTestCallFrequency`: `0x00`=disabilitato, `0x01`=24h, `0x02`=a sistema inserito)
* **offset 12** — indice numero telefonico per cyclic test call
* **offset 13** — ora cyclic test call
* **offset 14** — minuto cyclic test call
* **offset 15** — intervallo cyclic test call (`CyclicTestCallInterval`: `0x00`=1h ... `0x0a`=168h, in progressione — vedi enum completa nel codice)

**Nota**: dump con tutti zero (nessun dato configurato/testato con valori reali) — la mappa byte è dedotta dai commenti originali, non da un dump con valori non-default per ogni campo.

## PSTN GSM

Comando: `0xea SET PSTN GSM` (scrittura, mai letta/collegata a `0x5a PSTN GSM` in tabella comandi). Payload di scrittura, 21 byte:

```
0000: 00 01 00 00 00 00 00 00  00 00 00 00 00 00 00 00
0010: 00 ff ff ff ff
Checksum: ffffffff
```

Mappa byte (offset 0-based), enum lato codice `PSTNGSM.*`:

* **offset 0** — enable rete PSTN (`0x01`)
* **offset 1** — paese (`Country`: `0x00`=Italia, `0x01`=Francia, `0x02`=Germania, `0x03`=Rep. Ceca, `0x04`=Polonia, `0x05`=Spagna, `0x06`=Portogallo, `0x07`=Grecia, `0x08`=Inghilterra)
* **offset 4** — cifra accesso PABX locale (`PABXLocalAccessDigit`: `0x00`-`0x09`, `0xff`=disabilitato)
* **offset 5** — tone control enable (`0x01`)
* **offset 6** — answer control enable (`0x01`)
* **offset 7** — PSTN line test (`PSTNLineTestFrequency`: `0x00`=disabilitato, `0x01`=24h, `0x02`=a sistema inserito)
* **offset 8** — PSTN answering machine rings (`PSTNAnsweringMachineRings`: `0x00`=disabilitato, `0x02`/`0x04`/`0x08`=numero di squilli)
* **offset 9** — enable rete GSM (`0x01`)
* **offset 10** — GSM answering machine (nessuno squillo) enable (`0x01`)
* **offset 11** — incoming SMS enable (`0x01`)
* **offset 12-14** — PIN GSM in BCD
* **offset 15** — mese di scadenza
* **offset 16** — anno di scadenza

## SMS

Comando `0x59 SMS` (lettura bulk di tutti i messaggi, vedi [Tipologie di comando](#tipologie-di-comando)): payload suddiviso su 3 pacchetti (140+140+84 byte, checksum incluso), un blocco di 40 caratteri per messaggio (padding `0xff`), **nello stesso ordine** dell'enum `SMSs.SMSIndex`: Intrusione (burglar), Allarme tecnico 1/2/3, Incendio, Partizione attivata, Partizione disattivata, Manomissione, Note — conferma diretta che l'ordine dell'enum nel DTO rispecchia esattamente l'ordine on-the-wire.

Comando: `0xa0 SMS PROGRAMMING` (scrittura singola istanza, confermata raggiungibile via `ElkrommFacadeImpl.setSMS()`, vedi [Tipologie di comando](#tipologie-di-comando)). L'indice identifica il tipo di evento tramite l'enum `SMSs.SMSIndex` (9 valori: `SMS_BURLGAR`, `SMS_TECHNICAL_ALARM_1/2/3`, `SMS_FIRE`, `SMS_PARTITION_ON`, `SMS_PARTITION_OFF`, `SMS_TAMPERING`, `SMS_NOTICE`). Dump catturato (payload 41 byte, checksum non valido nella cattura):

```
0000: 01 41 67 61 69 6e 3f ff  ff ff ff ff ff ff ff ff
0010: ff ff ff ff ff ff ff ff  ff ff ff ff ff ff ff ff
0020: ff ff ff ff ff ff ff ff  ff
```

Il testo `41 67 61 69 6e 3f` decodifica in ASCII come **"Again?"** — testo di prova inserito dall'autore in Hi-Connect. Struttura dedotta: byte 0 = indice (`SMSIndex`, qui `0x01` = `SMS_TECHNICAL_ALARM_1`?, da confermare), seguito dal testo del messaggio in ASCII, padding `0xff` fino a fine payload.

## C200B

Comando: `0xe8 SET C200B` (scrittura, mai letta/collegata a `0x58 C200B` in tabella comandi). Payload 168 byte. Due catture disponibili — la prima con tutti i codici evento a zero (checksum valido, quindi configurazione "vuota" legittima), la seconda con i codici popolati:

```
0030: 00 00 0f 00 11 10 00 00  01 01 01 01 02 00 00 00
0040: 0f 07 08 06 00 00 00 09  12 00 0c 12 0b 0b 0a 0b
0050: 0e 0e 14 0d 13 00 00 00  03 04 05 0f 00 00 00 00
```

I codici evento (offset ~0x30-0x57, uno o più byte ciascuno a seconda del tipo di allarme) **coincidono esattamente** con i valori dell'enum `C200bParameters.Event` già presente nei DTO:

* burglar `0x38` (prima occorrenza, poi `0x39`, `0x3a`, `0x3b`)
* pre-alarm `0x3c`
* tech type 1/2/3: `0x58`/`0x59`/`0x5a`
* fire `0x43`, panic `0x41`, silent panic `0x42`
* medical aid `0x47`, hold up `0x4e`
* system on/off `0x4c` (poi `0x4d`, `0x4f`)
* partition on/off `0x4a`
* maintenance `0x53`
* input incl/escl `0x50` (poi `0x51`)
* tampering `0x32` (poi `0x40`, `0x5b`)
* mains power `0x35`, low battery `0x34`
* system fault `0x48` (poi `0x4b`)
* false code `0x54`
* cyclical test call `0x52`

Dall'offset `0x64` per 64 byte (`0x40`) seguono gli **input code** di ciascun ingresso in sequenza, `0xff` se l'ingresso non esiste (probabile codifica per il protocollo di allarme remoto C200b, coerente con l'uso generale di questo comando per la trasmissione eventi a centrale ricezione allarmi).

## Time programmer e Day class commands

Comandi coinvolti: `0xe4 SET TIME PROGRAMMER` (scrittura, blocco intero) e `0xa1 DAY CLASS CMDS` (scrittura singola istanza, confermata raggiungibile via `ElkrommFacadeImpl.setDayClassCommands()`). L'"istanza" per `DAY_CLASS_CMDS` è la classe giorno stessa, enum `DayClassCommands.DayClass`: `DCCDC_WORKING_DAY` (`0x00`), `DCCDC_PRE_HOLIDAY` (`0x01`), `DCCDC_HOLIDAY` (`0x02`).

**SET_TIME_PROGRAMMER** (payload 131 byte) — mappa giorni della settimana → tipo di giorno, offset dedotti da `ClientConnection`:

* lunedì → offset 120, martedì-sabato in sequenza, domenica → offset 126
* valore: `0x00`=Working day, `0x01`=Pre holiday, `0x02`=Holiday

**Comandi orari per giorno** (`Command` DTO, usato sia dentro `SET_TIME_PROGRAMMER` sia per i comandi di dettaglio di ciascuna day class): 5 byte a comando:

* byte 0 — azione (`Command.Action`: `0x00`=nessuna, `0x01`=abilita, `0x02`=disabilita)
* byte 1 — indice oggetto (numero settore o utente, a seconda del byte successivo)
* byte 2 — tipo oggetto (`Command.ObjectType`: `0x10`=settori, `0x40`=utente — commento nel codice segnala "more to come: keys, outputs", quindi la lista potrebbe non essere completa)
* byte 3 — ora
* byte 4 — minuto

Esempio dal dump `WORKING_DAY_CMD` (payload 41 byte): comando 1 → ora 1, minuto 2, enable, user 13 (dedotto dal commento originale, non riverificato byte-per-byte contro l'esadecimale).

## Blocco B

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

Dati blocco B:

`00 00 00`   
`30 33 30 31 # 0301 versione centrale???`  
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
`55 43 20 20 20 20 20 20 20 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # UC?`  
`c9 20`   
`00 01 00 # Indirizzo nuova unità?`  
`30 32 30 30 # 0200 versione espansione???`  
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
`00 02 00 # Indirizzo nuova unità?`  
`30 32 30 30 # 0200 versione espansione?`  
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

Offset TX	| Trasmissione	| Offset RX	| Ricezione	| Significato
----------------|---------------|---------------|---------------|------------
  | 01 55 55 00 00 00 00 52 ff 04 03	|	|	| KEYPADS
  |	|	| 16 | SYN
  |	|	| 01 55 55 01 00 8c 00 52 **01 00 30 34 31 30 00 00 01 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 06 ff 06 49 4e 47 52 45 53 53 4f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 b0 00 02 00 30 32 30 30 00 00 01 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20** ec 0e 03
| 01 55 55 00 00 00 00 65 fe f1 03	|	|	| SEND
  |	|	| 16 | SYN
  |	|	| 01 55 55 01 01 56 00 52 **20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 00 00 00 09 00 01 2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 00 00 00 00 ff ff 00 06 ff 00 47 41 52 41 47 45 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 e5 00 ff ff e3 8b** ef 9f 03

Definizione del payload "Keypads":  
` `  

Offset	| Significato	| Note
--------|---------------|-----
0	| Indirizzo	| Indirizzo della tastiera (base 1)
1	| ?	|
2-5	| Versione	| ASCII
6-43	| Primo ingresso della tastiera	|
44-81	| Secondo ingresso della tastiera	|
82	| Bitmask abilitazioni	| GONG, ENTRY, EXIT, MASKING, FIRE, PANIC, HELP
83	| Bitmask settori associati	| LSB = settore 1
84	| Bitmask feature audio	| CAPABLE, ENABLED
85-108	| Nome della tastiera	|
109-110	| ?	|
111	| Seconda tastiera	| Si ripetono i campi precedenti
...	|	|

` `  
` `  
Esempio di cattura dati tastiere:  

`01 Address`  
`00`  
`30343130 Version`  
`000001090001`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`000000090001`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`06`  
`ff Settori associati`  
`06`  
`494e47524553534f00000000000000000000000000000000 # Name`  
`b000`  

`02 Address`  
`00`  
`30323030 Version`  
`000001090001`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`000000090001`  
`2e2e2e202020202020202020202020202020202020202020`  
`0000000000ffff00`  
`06`  
`ff Settori associati`  
`00`  
`474152414745000000000000000000000000000000000000 # Name`  
`e500`  

`ffffe38b checksum blocco`  

## Readers

TBD

## Keypad programming

Scrittura parametri singola tastiera

Dati keypad:

`Dumping KEYPAD_PROGRAMMING, payload size: 111`  
`0000: 01 00 30 34 31 30 00 00  01 09 00 01 2e 2e 2e 20   ..0410.. .......  `  
`0010: 20 20 20 20 20 20 20 20  20 20 20 20 20 20 20 20                     `  
`0020: 20 20 20 20 00 00 00 00  00 ff ff 00 00 00 00 09       .... .￿￿..... `  
`0030: 00 01 2e 2e 2e 20 20 20  20 20 20 20 20 20 20 20   .....             `  
`0040: 20 20 20 20 20 20 20 20  20 20 00 00 00 00 00 ff              .....￿ `  
`0050: ff 00 07 ff 06 49 4e 47  52 45 53 53 4f 00 00 00   ￿..￿.ING RESSO... `  
`0060: 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00      ........ .......`  
