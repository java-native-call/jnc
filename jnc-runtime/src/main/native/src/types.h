#pragma once

#include "jnc.h"

#define PUT_BY_TYPE_E(j2n, addr, name, type, value) \
    case JNC_TYPE(name): {                          \
        type _tmp = (type) j2n(value);              \
        memcpy(addr, &_tmp, sizeof _tmp);           \
        break;                                      \
    }

#define PUT_BY_TYPE(...) PUT_BY_TYPE_E(NOOP, __VA_ARGS__)

#define PUT_ALL(ptr, addr, value)                       \
switch (ptr->type) {                                    \
    case JNC_TYPE(VOID): break;                         \
    PUT_BY_TYPE(addr, INT, int, value)                  \
    PUT_BY_TYPE(addr, FLOAT, float, value)              \
    PUT_BY_TYPE(addr, DOUBLE, double, value)            \
    PUT_BY_TYPE(addr, UINT8, uint8_t, value)            \
    PUT_BY_TYPE(addr, SINT8, int8_t, value)             \
    PUT_BY_TYPE(addr, UINT16, uint16_t, value)          \
    PUT_BY_TYPE(addr, SINT16, int16_t, value)           \
    PUT_BY_TYPE(addr, UINT32, uint32_t, value)          \
    PUT_BY_TYPE(addr, SINT32, int32_t, value)           \
    PUT_BY_TYPE(addr, UINT64, uint64_t, value)          \
    PUT_BY_TYPE(addr, SINT64, int64_t, value)           \
    PUT_BY_TYPE_E(j2vp, addr, POINTER, void*, value)    \
    default: {                                          \
        memcpy(addr, &value,                            \
            MIN(ptr->size, sizeof(value)));             \
        break;                                          \
    }                                                   \
}

#define RET_BY_TYPE_E(n2j, cast, name, ctype, addr) \
    case JNC_TYPE(name): {                          \
        ctype _t = 0;                               \
        memcpy(&_t, addr, sizeof _t);               \
        return cast(n2j(_t));                       \
    }
#define RET_BY_TYPE(...) RET_BY_TYPE_E(NOOP, __VA_ARGS__)

#define RET(cast, me, atype, addr)                  \
switch (atype->type) {                              \
    case JNC_TYPE(VOID): return cast(0);            \
    RET_BY_TYPE(cast, INT, int, addr)               \
    RET_BY_TYPE(cast, FLOAT, float, addr)           \
    RET_BY_TYPE(cast, DOUBLE, double, addr)         \
    RET_BY_TYPE(cast, UINT8, uint8_t, addr)         \
    RET_BY_TYPE(cast, SINT8, int8_t, addr)          \
    RET_BY_TYPE(cast, UINT16, uint16_t, addr)       \
    RET_BY_TYPE(cast, SINT16, int16_t, addr)        \
    RET_BY_TYPE(cast, UINT32, uint32_t, addr)       \
    RET_BY_TYPE(cast, SINT32, int32_t, addr)        \
    RET_BY_TYPE(cast, UINT64, uint64_t, addr)       \
    RET_BY_TYPE(cast, SINT64, int64_t, addr)        \
    RET_BY_TYPE_E(p2j, cast, POINTER, void*, addr)  \
    default: {                                      \
        me res = 0;                                 \
        memcpy(&res, addr,                          \
            MIN(sizeof(me), atype->size));          \
        return cast(res);                           \
    }                                               \
}

#define TO_TYPE(type, x) (type)(x)
#define TO_BOOLEAN(x)   !!(x)
#define TO_JBYTE(x)     TO_TYPE(jbyte, x)
#define TO_JCHAR(x)     TO_TYPE(jchar, x)
#define TO_JSHORT(x)    TO_TYPE(jshort, x)
#define TO_JINT(x)      TO_TYPE(jint, x)
#define TO_JLONG(x)     TO_TYPE(jlong, x)
#define TO_JFLOAT(x)    TO_TYPE(jfloat, x)
#define TO_JDOUBLE(x)   TO_TYPE(jdouble, x)
#define RET_void(...)
#define RET_jboolean(...)   RET(TO_BOOLEAN, jboolean, __VA_ARGS__)
#define RET_jbyte(...)      RET(TO_JBYTE, jbyte, __VA_ARGS__)
#define RET_jchar(...)      RET(TO_JCHAR, jchar, __VA_ARGS__)
#define RET_jshort(...)     RET(TO_JSHORT, jshort, __VA_ARGS__)
#define RET_jint(...)       RET(TO_JINT, jint, __VA_ARGS__)
#define RET_jlong(...)      RET(TO_JLONG, jlong, __VA_ARGS__)
#define RET_jfloat(...)     RET(TO_JFLOAT, jfloat, __VA_ARGS__)
#define RET_jdouble(...)    RET(TO_JDOUBLE, jdouble, __VA_ARGS__)
