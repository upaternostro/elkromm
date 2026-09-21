package org.paternostro.elkromm.serializer;

import org.paternostro.elkromm.ElkrommFacade;
import org.paternostro.elkromm.ElkrommUtils;
import org.paternostro.elkromm.dto.Area;
import org.paternostro.elkromm.dto.Partition;

/**
 * {@link org.paternostro.elkromm.dto.AreasAndPartitions} serializer, DTO &harr; byte array.
 * <p>
 * Payload structure:
 * <p>
 * <table>
 *  <tr><th>Offset</th><th>Meaning</th><th>Note</th><th>Constant</th></tr>
 *  <tr><td>0x00</td><td>Number of areas</td><td>Can be zero if unused</td><td>{@link #AREAS_COUNT_OFFSET}</td></tr>
 *  <tr><td>0x01-0x04</td><td>Bitmask of partitions assigned to the area</td><td>LSB = partition 1, one byte per area</td><td>{@link #AREAS_MASKS_OFFSET}</td></tr>
 *  <tr><td>0x05-0x1c</td><td>First area name</td><td></td><td>{@link #AREAS_NAMES_OFFSET}</td></tr>
 *  <tr><td>0x1d-0x34</td><td>Second area name</td><td></td><td></td></tr>
 *  <tr><td>0x35-0x4c</td><td>Third area name</td><td></td><td></td></tr>
 *  <tr><td>0x4d-0x64</td><td>Fourth area name</td><td></td><td></td></tr>
 *  <tr><td>0x65</td><td>Number of partitions</td><td></td><td>{@link #PARTITIONS_COUNT_OFFSET}</td></tr>
 *  <tr><td>0x66</td><td>Self-exclusion bitmask</td><td>LSB = partition 1. A partition cannot be both self-exclusion and arming block. Standard partitions have both flags to zero.</td><td>{@link #PARTITIONS_SELF_EXCLUSION_MASK_OFFSET}</td></tr>
 *  <tr><td>0x67</td><td>Arming block bitmask</td><td>LSB = partition 1</td><td>{@link #PARTITIONS_ARMING_BLOCK_MASK_OFFSET}</td></tr>
 *  <tr><td>0x68-0x77</td><td>Entry delay</td><td>2 bytes per partition (starting at 0x68-0x69 for partition 1 and so on), big endian</td><td>{@link #PARTITIONS_ENTRY_DELAYS_OFFSET}</td></tr>
 *  <tr><td>0x78-0x87</td><td>Exit delay</td><td>2 bytes per partition (starting at 0x78-0x79 for partition 1 and so on), big endian</td><td>{@link #PARTITIONS_EXIT_DELAYS_OFFSET}</td></tr>
 *  <tr><td>0x88-0x9f</td><td>First partition name</td><td></td><td>{@link #PARTITIONS_NAMES_OFFSET}</td></tr>
 *  <tr><td>0xa0-0xb7</td><td>Second partition name</td><td></td><td></td></tr>
 *  <tr><td>0xb8-0xcf</td><td>Third partition name</td><td></td><td></td></tr>
 *  <tr><td>0xd0-0xe7</td><td>Fourth partition name</td><td></td><td></td></tr>
 *  <tr><td>0xe8-0xff</td><td>Fifth partition name</td><td></td><td></td></tr>
 *  <tr><td>0x100-0x117</td><td>Sixth partition name</td><td></td><td></td></tr>
 *  <tr><td>0x118-0x12f</td><td>Seventh partition name</td><td></td><td></td></tr>
 *  <tr><td>0x130-0x147</td><td>Eighth partition name</td><td></td><td></td></tr>
 *  <tr><td>0x148</td><td>?</td><td>The serializer explicitly zeroes it on write ({@code data[328] = 0; // ???}); same signature as the other "status" bytes already documented (see {@link Input}) — suspected dynamic content not handled by the client, not yet identified but computed in checksum</td><td>{@link #UNKNOWN_OFFSET}</td></tr>
 *  <tr><td>0x149-0x14c</td><td>Block checksum</td><td></td><td></td></tr>
 * </table>
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class AreasAndPartitions implements ElkrommSerializer<org.paternostro.elkromm.dto.AreasAndPartitions>
{
    /** Size of a partition entry or exit delay, a big endian word */
    public static final int DELAY_SIZE  = 2;

    /** Offset of the number of areas */
    public static final int AREAS_COUNT_OFFSET                    = 0x00;

    /** Offset of the bitmasks of the partitions assigned to each area, one byte per area */
    public static final int AREAS_MASKS_OFFSET                    = AREAS_COUNT_OFFSET + 1;

    /** Offset of the area names, {@link ElkrommFacade#NAME_LENGTH} bytes each */
    public static final int AREAS_NAMES_OFFSET                    = AREAS_MASKS_OFFSET + ElkrommFacade.MAX_AREAS;

    /** Offset of the number of partitions */
    public static final int PARTITIONS_COUNT_OFFSET               = AREAS_NAMES_OFFSET + ElkrommFacade.MAX_AREAS * ElkrommFacade.NAME_LENGTH;

    /** Offset of the self-exclusion bitmask */
    public static final int PARTITIONS_SELF_EXCLUSION_MASK_OFFSET = PARTITIONS_COUNT_OFFSET + 1;

    /** Offset of the arming block bitmask */
    public static final int PARTITIONS_ARMING_BLOCK_MASK_OFFSET   = PARTITIONS_SELF_EXCLUSION_MASK_OFFSET + 1;

    /** Offset of the partitions entry delays, {@link #DELAY_SIZE} bytes each */
    public static final int PARTITIONS_ENTRY_DELAYS_OFFSET        = PARTITIONS_ARMING_BLOCK_MASK_OFFSET + 1;

    /** Offset of the partitions exit delays, {@link #DELAY_SIZE} bytes each */
    public static final int PARTITIONS_EXIT_DELAYS_OFFSET         = PARTITIONS_ENTRY_DELAYS_OFFSET + ElkrommFacade.MAX_PARTITIONS * DELAY_SIZE;

    /** Offset of the partition names, {@link ElkrommFacade#NAME_LENGTH} bytes each */
    public static final int PARTITIONS_NAMES_OFFSET               = PARTITIONS_EXIT_DELAYS_OFFSET + ElkrommFacade.MAX_PARTITIONS * DELAY_SIZE;

    /** Offset of a byte of unknown meaning, zeroed on write */
    public static final int UNKNOWN_OFFSET                        = PARTITIONS_NAMES_OFFSET + ElkrommFacade.MAX_PARTITIONS * ElkrommFacade.NAME_LENGTH;

    /** Payload size */
    public static final int PAYLOAD_SIZE = 1                                                           // numero di aree
                                            + ElkrommFacade.MAX_AREAS                                  // settori nell'area i-esima (bit mask)
                                            + ElkrommFacade.MAX_AREAS * ElkrommFacade.NAME_LENGTH      // nomi delle aree
                                            + 1                                                        // numero di settori
                                            + 1                                                        // self exclusion (bit mask)
                                            + 1                                                        // arming block (bit mask)
                                            + ElkrommFacade.MAX_PARTITIONS * DELAY_SIZE                // tempi di ingresso
                                            + ElkrommFacade.MAX_PARTITIONS * DELAY_SIZE                // tempi di uscita
                                            + ElkrommFacade.MAX_PARTITIONS * ElkrommFacade.NAME_LENGTH // nomi dei settori
                                            + 1                                                        // ???
                                            + ElkrommUtils.CHECKSUM_SIZE;                              // checksum (333 byte in totale)

    @Override
    public byte[] serialize(org.paternostro.elkromm.dto.AreasAndPartitions obj) {
        byte[]  data = new byte[PAYLOAD_SIZE];

        data[AREAS_COUNT_OFFSET] = (byte)obj.getAreaNum(); // numero di aree

        for (int i = 0; i < ElkrommFacade.MAX_AREAS; i++) {
            data[i + AREAS_MASKS_OFFSET] = (byte)ElkrommUtils.packPartitions(obj.getArea(i).getAssociatedPartitions()); // settori nell'area i-esima (bit mask)
            ElkrommUtils.setText(data, AREAS_NAMES_OFFSET + i*ElkrommFacade.NAME_LENGTH, obj.getArea(i).getName(), ElkrommFacade.NAME_LENGTH); // nome dell'area i-esima
        }
        
        data[PARTITIONS_COUNT_OFFSET] = (byte)obj.getPartitionNum(); // numero di settori
        data[PARTITIONS_SELF_EXCLUSION_MASK_OFFSET] = 0; // self exclusion (bit mask)
        data[PARTITIONS_ARMING_BLOCK_MASK_OFFSET] = 0; // arming block (bit mask)

        for (int i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            switch (obj.getPartition(i).getType()) { // tipo del settore i-esimo
                case STANDARD:
                    break;
                case SELF_EXCLUSION:
                    data[PARTITIONS_SELF_EXCLUSION_MASK_OFFSET] |= ElkrommFacade.Partition.values()[i].getValue();
                    break;
                case ARMING_BLOCK:
                    data[PARTITIONS_ARMING_BLOCK_MASK_OFFSET] |= ElkrommFacade.Partition.values()[i].getValue();
                    break;
                case UNKNOWN:
                    break;
            }

            ElkrommUtils.setWord(data, PARTITIONS_ENTRY_DELAYS_OFFSET + i*DELAY_SIZE, obj.getPartition(i).getEntryDelay()); // tempo di ingresso
            ElkrommUtils.setWord(data, PARTITIONS_EXIT_DELAYS_OFFSET + i*DELAY_SIZE, obj.getPartition(i).getExitDelay()); // tempo di uscita
            ElkrommUtils.setText(data, PARTITIONS_NAMES_OFFSET + i*ElkrommFacade.NAME_LENGTH, obj.getPartition(i).getName(), ElkrommFacade.NAME_LENGTH); // nome del settore
        }

        data[UNKNOWN_OFFSET] = 0; // ???

        ElkrommUtils.setBlockChecksum(data);

        return data;
    }

    @Override
    public org.paternostro.elkromm.dto.AreasAndPartitions deserialize(byte[] data) {
        if (data == null) throw new IllegalArgumentException("Missing mandatory data");
        if (data.length != PAYLOAD_SIZE) throw new IllegalArgumentException("Wrong data length");
        if (ElkrommUtils.computeBlockChecksum(data) != ElkrommUtils.getBlockChecksum(data)) throw new IllegalArgumentException("Wrong checksum");

        org.paternostro.elkromm.dto.AreasAndPartitions  retval = new org.paternostro.elkromm.dto.AreasAndPartitions();
        int                                             selfExclusion;
        int                                             armingBlock;
        Partition.Type                                  type;
        byte                                            partitionBitMask;

        retval.setAreaNum(data[AREAS_COUNT_OFFSET]); // numero di aree

        for (byte i = 0; i < ElkrommFacade.MAX_AREAS; i++) {
            retval.addArea(new Area(ElkrommUtils.getText(data, AREAS_NAMES_OFFSET + i * ElkrommFacade.NAME_LENGTH, ElkrommFacade.NAME_LENGTH), ElkrommUtils.unpackPartitions(data[i + AREAS_MASKS_OFFSET])));
        }

        retval.setPartitionNum(data[PARTITIONS_COUNT_OFFSET]); // numero di settori
        selfExclusion = data[PARTITIONS_SELF_EXCLUSION_MASK_OFFSET];
        armingBlock = data[PARTITIONS_ARMING_BLOCK_MASK_OFFSET];

        for (byte i = 0; i < ElkrommFacade.MAX_PARTITIONS; i++) {
            partitionBitMask = ElkrommFacade.Partition.values()[i].getValue();

            if ((selfExclusion & partitionBitMask) == 0x00) {
                if ((armingBlock & partitionBitMask) == 0x00) {
                    type = Partition.Type.STANDARD;
                } else {
                    type = Partition.Type.ARMING_BLOCK;
                }
            } else {
                if ((armingBlock & partitionBitMask) == 0x00) {
                    type = Partition.Type.SELF_EXCLUSION;
                } else {
                    type = Partition.Type.UNKNOWN;
                }
            }

            retval.addPartition(new Partition(ElkrommUtils.getText(data, PARTITIONS_NAMES_OFFSET + i * ElkrommFacade.NAME_LENGTH, ElkrommFacade.NAME_LENGTH), false, type, ElkrommUtils.getWord(data, PARTITIONS_ENTRY_DELAYS_OFFSET + i * DELAY_SIZE), ElkrommUtils.getWord(data, PARTITIONS_EXIT_DELAYS_OFFSET + i * DELAY_SIZE)));
        }

        return retval;
    }
}
