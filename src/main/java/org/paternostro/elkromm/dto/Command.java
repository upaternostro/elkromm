package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

/**
 * A single scheduled action within the weekly time programmer: enable or
 * disable a given object (a partition or a user, so far) at a given time of day.
 * <p>
 * Copyright Ugo Paternostro 2017-2026. Licensed under the EUPL-1.2 or later.
 */
public class Command implements Serializable {
    /** What a scheduled {@link Command} does to its target object. */
    public enum Action {
        /** No action (empty slot). */
        CA_NONE(0x00),
        /** Enables/arms the target object. */
        CA_ENABLE(0x01),
        /** Disables/disarms the target object. */
        CA_DISABLE(0x02);

        private byte value;

        Action(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this action.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code Action} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching action, or {@code null} if none matches
         */
        public static Action valueOf(byte value) {
            Action  retval = null;

            for (Action pivot : Action.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    /**
     * What kind of object a scheduled {@link Command} targets.
     * <p>
     * Only sectors (partitions) and users are known so far; the underlying
     * protocol likely supports more (keys, outputs).
     */
    public enum ObjectType {
        /** The target is a partition/sector. */
        COT_SECTORS(0x10),
        /** The target is a user. */
        COT_USER(0x40);
        // FIXME: More to come (keys, outputs)

        private byte value;

        ObjectType(int value)
        {
            this.value = (byte)value;
        }

        /**
         * Returns the raw byte value of this object type.
         *
         * @return the raw value
         */
        public byte getValue()
        {
            return value;
        }

        /**
         * Looks up the {@code ObjectType} matching a raw byte value.
         *
         * @param value the raw value to look up
         * @return the matching object type, or {@code null} if none matches
         */
        public static ObjectType valueOf(byte value) {
            ObjectType  retval = null;

            for (ObjectType pivot : ObjectType.values()) {
                if (pivot.getValue() == value) {
                    retval = pivot;
                    break;
                }
            }

            return retval;
        }
    }

    private Action      action;
    private byte        object;
    private ObjectType  objectType;
    private byte        hour;
    private byte        minute;

    /**
     * Creates a new scheduled command.
     *
     * @param action what to do
     * @param object 0-based ordinal of the target object
     * @param objectType what kind of object {@code object} refers to
     * @param hour hour of day, in range [0, 23]
     * @param minute minute of the hour, in range [0, 59]
     */
    public Command(Action action, byte object, ObjectType objectType, byte hour, byte minute) {
        setAction(action);
        setObject(object);
        setObjectType(objectType);
        setHour(hour);
        setMinute(minute);
    }

    /**
     * Returns what this command does.
     *
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets what this command does.
     *
     * @param action the action to set, not {@code null}
     * @throws IllegalArgumentException if {@code action} is {@code null}
     */
    public void setAction(Action action) {
        if (action == null) throw new IllegalArgumentException("Missing mandatory action");

        this.action = action;
    }

    /**
     * Returns the 0-based ordinal of the target object.
     *
     * @return the target object ordinal
     */
    public byte getObject() {
        return object;
    }

    /**
     * Sets the 0-based ordinal of the target object.
     *
     * @param object the ordinal to set, in range [0, {@link ElkrommFacade#MAX_CREDENTIALS})
     * @throws IllegalArgumentException if out of range
     */
    public void setObject(byte object) {
        if (object < 0 || object >= ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong object " + object + ", expected value between 0 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.object = object;
    }

    /**
     * Returns what kind of object {@link #getObject()} refers to.
     *
     * @return the object type
     */
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * Sets what kind of object {@link #getObject()} refers to.
     *
     * @param objectType the object type to set
     */
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns the scheduled hour of day.
     *
     * @return the hour, in range [0, 23]
     */
    public byte getHour() {
        return hour;
    }

    /**
     * Sets the scheduled hour of day.
     *
     * @param hour the hour to set, in range [0, 23]
     * @throws IllegalArgumentException if out of range
     */
    public void setHour(byte hour) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("Illegal hour value");

        this.hour = hour;
    }

    /**
     * Returns the scheduled minute of the hour.
     *
     * @return the minute, in range [0, 59]
     */
    public byte getMinute() {
        return minute;
    }

    /**
     * Sets the scheduled minute of the hour.
     *
     * @param minute the minute to set, in range [0, 59]
     * @throws IllegalArgumentException if out of range
     */
    public void setMinute(byte minute) {
        if (minute < 0 || minute > 59) throw new IllegalArgumentException("Illegal minute value");

        this.minute = minute;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("{action=").append(action).append(", object=").append(object).append(", objectType=").append(objectType).append(", hour=").append(hour
               ).append(", minute=").append(minute).append("}");
        
        return sb.toString();
    }
}
