package org.paternostro.elkromm.dto;

import java.io.Serializable;

import org.paternostro.elkromm.ElkrommFacade;

public class Command implements Serializable {
    public enum Action {
        CA_NONE(0x00),
        CA_ENABLE(0x01),
        CA_DISABLE(0x02);

        private byte value;

        Action(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

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

    public enum ObjectType {
        COT_SECTORS(0x10),
        COT_USER(0x40);
        // FIXME: More to come (keys, outputs)

        private byte value;

        ObjectType(int value)
        {
            this.value = (byte)value;
        }

        public byte getValue()
        {
            return value;
        }

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

    public Command(Action action, byte object, ObjectType objectType, byte hour, byte minute) {
        setAction(action);
        setObject(object);
        setObjectType(objectType);
        setHour(hour);
        setMinute(minute);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        if (action == null) throw new IllegalArgumentException("Missing mandatory action");

        this.action = action;
    }

    public byte getObject() {
        return object;
    }

    public void setObject(byte object) {
        if (object < 0 || object >= ElkrommFacade.MAX_CREDENTIALS) throw new IllegalArgumentException("Wrong object " + object + ", expected value between 0 and " + ElkrommFacade.MAX_CREDENTIALS);

        this.object = object;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public byte getHour() {
        return hour;
    }

    public void setHour(byte hour) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("Illegal hour value");

        this.hour = hour;
    }

    public byte getMinute() {
        return minute;
    }

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
