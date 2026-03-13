/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jnbt;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class NBTInputStream implements Closeable {
    private final DataInputStream input;

    public NBTInputStream(InputStream is) {
        this.input = new DataInputStream(is);
    }

    public Tag readTag() throws IOException {
        int type = input.readByte() & 0xFF;
        if (type == 0) {
            return null;
        }
        input.readUTF(); // Read name
        return readTagPayload(type);
    }

    private Tag readTagPayload(int type) throws IOException {
        switch (type) {
            case 1: // Byte
                return new IntTag(input.readByte());
            case 2: // Short
                return new ShortTag(input.readShort());
            case 3: // Int
                return new IntTag(input.readInt());
            case 7: // Byte Array
                int length = input.readInt();
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                return new ByteArrayTag(bytes);
            case 9: // List
                input.readByte(); // List type
                int listLength = input.readInt();
                for (int i = 0; i < listLength; i++) {
                    readTagPayload(3); // Skip list items
                }
                return new IntTag(0);
            case 10: // Compound
                Map<String, Tag> map = new HashMap<>();
                while (true) {
                    int childType = input.readByte() & 0xFF;
                    if (childType == 0) break;
                    String name = input.readUTF();
                    Tag child = readTagPayload(childType);
                    map.put(name, child);
                }
                return new CompoundTag(map);
            default:
                throw new IOException("Unknown tag type: " + type);
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
