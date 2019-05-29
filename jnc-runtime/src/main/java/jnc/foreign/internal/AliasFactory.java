/*
 * Copyright 2019 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnc.foreign.internal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import jnc.foreign.enums.TypeAlias;

/**
 * @author zhanhb
 */
class AliasFactory {

    private static TypeAlias toTypeAlias(String name) {
        try {
            return TypeAlias.valueOf(name);
        } catch (IllegalArgumentException ex) {
            // ok might be int,long
        }
        if (name.equals("int")) {
            return TypeAlias.cint;
        } else if (name.equals("long")) {
            return TypeAlias.clong;
        }
        return null;
    }

    private final Map<TypeAlias, Alias> MAP;

    AliasFactory(TypeFactory typeFactory) {
        HashMap<String, Integer> nativeAliasMap = new HashMap<>(50);
        NativeLoader.getAccessor().initAlias(nativeAliasMap);
        EnumMap<TypeAlias, Alias> map = new EnumMap<>(TypeAlias.class);
        for (Map.Entry<String, Integer> entry : nativeAliasMap.entrySet()) {
            String key = entry.getKey();
            int type = entry.getValue();
            TypeAlias typeAlias = toTypeAlias(key);
            if (typeAlias != null) {
                TypeInfo typeInfo = typeFactory.findByType(type);
                map.put(typeAlias, new Alias(typeAlias, typeInfo));
            }
        }
        MAP = map;
    }

    Alias find(TypeAlias name) {
        Alias alias = MAP.get(name);
        if (alias == null) {
            throw new UnsupportedOperationException("type " + name + " is not supported on current platform");
        }
        return alias;
    }

}
