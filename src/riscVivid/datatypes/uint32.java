/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * (C)opyright 2013-2016 The riscVivid project, University of Augsburg, Germany
 * https://github.com/unia-sik/riscVivid
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, see <LICENSE>. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package riscVivid.datatypes;

public class uint32 extends BasicNumber
{
    private int value;

    public uint32(int value)
    {
        this.value = value;
    }

    public uint32(short value)
    {
        this.value = (int) value;
    }

    public uint32(byte value)
    {
        this.value = (int) value;
    }

    public uint32(uint32 value)
    {
        this.value = (int) value.getValue();
    }

    public uint32()
    {
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public String getValueAsHexString()
    {
        String s = Integer.toHexString(value);
        int diff = 8 - s.length();

        for (; diff > 0; diff--)
            s = "0" + s;

        return "0x" + s;
    }

    public void setValue(uint32 value)
    {
        this.value = value.getValue();
    }

    public int hashCode()
    {
        return getValue();
    }

    public boolean equals(Object o)
    {
        if (o.getClass() == this.getClass())
        {
            return equals((uint32) o);
        }
        return false;
    }

    public boolean equals(uint32 other)
    {
        return (getValue() == other.getValue());
    }

    public String getValueAsDecimalString()
    {
        return Integer.toString(getValue());
    }
}
