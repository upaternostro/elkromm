# Analisi protocollo Elkron Hi-Connect

Premessa:

* utilizzo Hi-Connect v3.30 / 3.70;
* utilizzo di una centrale MP-508 v03.01;
* codice impianto 55555555 (default);
* codice installatore 000000 (default);
* connessione diretta ad IP.

Il colloquio avviene sulla porta TCP 8030.  
Quando si apre il sistema in gestione, Hi-Connect apre una socket TCP verso l'IP/port indicati.  
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
1	| 0x55		| ??? costante? Codice impianto?
2	| 0x55		| ??? costante? Codice impianto?
3	| (variabile)	| numero dei pacchetti costituenti la sequenza
4	| (variabile)	| progressivo del pacchetto nella sequenza
5	| (variabile)	| lunghezza dei dati inviati
6	| 0x00		| ??? costante?
7	| (variabile)	| comando?
8 → n	| (variabile)	| dati trasmessi (possono essere assenti se il byte di posizione 5 vale zero)
n+1 → n+2	| (variabile)	| checksum big endian del pacchetto calcolato in modo che la somma di tutti i byte dall'offset 1 a n, sommato a questo checksum, fornisca 0x10000. In altre parole, il checksum è `0x10000 – SUM(offset1... offsetn)`
n+3	| 0x03		| ETX

Nota: il byte 3 e 4 valgono entrambi 0x00 se la risposta è contenuta in un solo pacchetto (max 140 (0x8c) byte di dati, al netto degli escape). Se viceversa i dati da trasmettere eccedono il massimo indicato, la risposta viene spezzata in più pacchetti, il byte 3 viene valorizzato con l'indice dl pacchetto massimo e il byte 4 con l'indice del pacchetto corrente (base 0). In altre parole, se la risposta è spezzata in 3 pacchetti, il byte 3 vale 0x02 ed il byte 4 rispettivamente 0x00, 0x01 e 0x02 per il primo, secondo e terzo pacchetto. Il ricevitore conferma la ricezione con un pacchetto di tipo 0x65 (SEND).

Comandi riconosciuti:  
` `  

Comando	| Significato	| Dati		| Risposta centrale	| Note
--------|---------------|---------------|-----------------------|-------
0x60	| HELLO		| nessuno	| 0x06 (ACK)	
0x49	| LOGIN		| codice impianto e codice installatore in BCD (rispettivamente 4 + 3 byte)	| 0x06 (ACK)	
0x65	| SEND		| nessuno	| 0x16 (SYN) se non deve inviare nulla	| Hp: richiesta di invio dati
	| 		| 		|
	| 		| 		| oppure
	| 		| 		|
	| 		| 		| pacchetti dati successivi al primo
	| 		| 		|
	| 		| 		| ipotesi: eventi?
0x62	| ADDRESSES?	| nessuno	| 0x16 (SYN) + pacchetto dati 0x62 contente il numero di tastiere (1 byte), i loro indirizzi, il numero di lettori (1 byte), i loro indirizzi, il numero di espansioni (1 byte) ed i loro indirizzi	| Hp: indirizzi delle periferiche?
0x50	| CHECKSUM	| nessuno	| 0x16 (SYN) + pacchetto dati 0x50 13 long word (32 bit) dati di checksum big endian, rispettivamente: nodi, tastiere, inseritori, sistema, programmatore orario, aree settori, com tel, num tel, eventi, sms, pstn gsm, utenti, chiavi	
0x84	| INPUT STATUS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x84 con un byte per ogni ingresso attivo (in ordine di indirizzo?). Il byte vale 0x00 se l'ingresso è chiuso, 0x02 se l'ingresso è aperto, 0x10 se l’ingresso è escluso, 0x04 in caso di memoria di allarme	
0x63	| LOGOUT	| nessuno	| 0x16 (SYN)	
0x55	| AREE & SETTORI	| nessuno	| 0x16 (SYN) + pacchetto dati 0x55 meglio descritto sotto	| Questi 4 comandi coprono il “blocco A”, che a differenza del “blocco B” non esiste come comando singolo
0x5b	| UTENTI	| nessuno	| 0x16 (SYN) + pacchetto dati 0x5b meglio descritto sotto	
0x5c	| CHIAVI	| nessuno	| 0x16 (SYN) + pacchetto dati 0x5c meglio descritto sotto	
0x26	| PARAMETERS & ENABLINGS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x84 meglio descritto sotto	
0x80	| SYSTEM STATUS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x80 con un singolo byte di dati, 1 bit ogni settore, LSB = settore 1	
0x81	| ARM/DISARM SYSTEM	| due byte di dati, 1 bit ogni settore, LSB = settore 1, in caso di attivazione i due byte sono uguali (ad es. 0x02 + 0x02 per armare il settore 2), mentre in caso di disattivazione il primo indica il settore, il secondo vale 0x00	| 0x16 (SYN)	
0x96	| WRITE PARAMETERS & ENABLINGS	| vedi dati di 0x26. Attenzione: ESCAPE con 0x11 anche di 0x01	| 0x16 (SYN)	
0x95	| AGGIUNTA UTENTE	| Progressivo utente incrementato di 1 + aree + settori + nome (24 byte)	| 0x16 (SYN)	
0xe7	| MODIFICA NUMERI TELEFONICI	| pacchetto dati 0xe7 meglio descritto sotto	| 0x16 (SYN)	
0x51	| EXPANSIONS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x51 meglio descritto sotto	| Aka Blocco B
0x83	| EXCLUDE/INCLUDE INPUT	| due byte di dati, il primo indica il numero di ingresso, il secondo vale 0x01 (attenzione all’escape) per escludere l’ingresso, 0x00 per includerlo	| 0x16 (SYN)	
0x87	| USER STATUS	| nessuno	| 0x16 (SYN) + pacchetto dati 0x87 contenente 4 byte di dati con i flag che indicano l’attivazione degli utenti. Il primo byte contiene nell’MSB (0x80) lo stato dell’utente 0 (TECNICO), nel bit immediatamente successivo (0x40) lo stato dell’utente 1 (MASTER) e così via per gli altri bit. Il secondo byte indica gli stati degli utenti 8-15, il terzo 16-23 e l’ultimo 24-31	
0x88	| ENABLE/DISABLE USER	| due byte di dati, il primo indica il numero di utente (base 1 = TECNICO), il secondo vale 0x01 (attenzione all’escape) per attivare l’utente, 0x00 per disattivarlo	| 0x16 (SYN)	

` `  
` `  
La centrale risponde con 0x06 (ACK) solo fintanto che non sia stato effettuato il login, compreso il pacchetto di login stesso. Hi-Connect quindi invia un SEND dopo il login per verificare che la risposta sia 0x16 (SYN). L'intera procedura di login è quindi costituita da tre pacchetti: HELLO, LOGIN, SEND. Se si invia il LOGIN senza HELLO, si ottiene in risposta 0x15 (NAK).

Il software Hi-Connect continua ad inviare pacchetti di tipo SEND che mentre è in idle. Da tentativi empirici, è bene che questi pacchetti vengano inviati, altrimenti ad un certo punto la centrale inizia a rispondere NAK anche ai pacchetti corretti.

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

Dati settori:

`00 # numero di aree`  
`01 00 00 00 # settori assegnati alle 4 aree (bit mask)`  
`2e 2e 2e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # 24 bytes`  
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`2e 2e 2e 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20`   
`04 # numero di settori`  
`0c # tipo (self exclusion) Se bit a 1 self excl`  
*`11`* `03 # tipo (arming block) Se bit a 1 arming block`  
`00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 # Entry/Exit delay 16 byte`  
`00 1e 00 1e 00 1e 00 1e 00 00 00 00 00 00 00 00 # Entry/Exit delay`  
`43 41 53 41 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 # CASA`  
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

`00 ff # Aree e settori associati?`  
`54 45 43 4e 49 43 4f 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 00 # TECNICO (24 byte)`  
`02 ff # Cosa significa 02? Hp.: sempre abilitato?`  
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
`0a` *`11`* `03 # DST ottobre/marzo?`  
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

`20 1f ff ff ff ff ff ff ff ff ff ff ff ff ff # Numero telefonico codificato in BCD (15 bytes)`  
`00 # Rete telefonica (00 = PSTN, 01 = GSM)`  
`00 # Tipo di segnalazione (00 = Voce, 06 = SMS)`  
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
