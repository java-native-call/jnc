#ifndef JNC_COMMONS_H
#define JNC_COMMONS_H

#include <jni.h>
#include <stdint.h>
#include "jnc_type_traits.h"

template<int = sizeof (size_t)>
struct large_test;

template<>
struct large_test<4> : jnc_type_traits::integral_constant<int, 30> {
};

template<>
struct large_test<8> : jnc_type_traits::integral_constant<int, 62> {
};

// limit should not be negative
// usually should be checked before pass to this function
// or this function will return true
inline bool is_sizet_large_enough(jlong value) {
    static_assert(sizeof (void *) == sizeof (size_t), "require pointer and size_t same size");
    // maybe the value here we got is calculated by minus something.
    // treat it as unlimited if the value is greater than a quart of the whole memory can be presented.
    return value >> large_test<>::value;
}

#endif /* JNC_COMMONS_H */
