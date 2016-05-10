package YaraParser.Structures;

import java.io.Serializable;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 2/5/15
 * Time: 10:27 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class CompactArray implements Serializable {
    float[] array;
    int offset;

    public CompactArray(int offset, float[] array) {
        this.offset = offset;
        this.array = array;
    }

    public void expandArray(int index, float value) {
        if (index < offset + array.length && index >= offset) {
            array[index - offset] += value;
        } else if (index < offset) {  //expand from left
            int gap = offset - index;
            int newSize = gap + array.length;
            float[] newArray = new float[newSize];
            newArray[0] = value;
            for (int i = 0; i < array.length; i++) {
                newArray[gap + i] = array[i];
            }
            this.offset = index;
            this.array = newArray;
        } else {
            int gap = index - (array.length + offset - 1);
            int newSize = array.length + gap;
            float[] newArray = new float[newSize];
            newArray[newSize - 1] = value;
            for (int i = 0; i < array.length; i++) {
                newArray[i] = array[i];
            }
            this.array = newArray;
        }
    }

    public float[] getArray() {
        return array;
    }

    public int getOffset() {
        return offset;
    }

    public int length() {
        return array.length;
    }
}
