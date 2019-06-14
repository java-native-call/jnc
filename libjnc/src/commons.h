#ifndef JNC_COMMONS_H
#define JNC_COMMONS_H

#include <jni.h>
#include <stdint.h>
#include "jnc_type_traits.h"

// limit should not be negative
// usually should be checked before pass to this function
// or this function will return true
inline bool is_sizet_large_enough(jlong value) {
    // maybe the value here we got is calculated by minus something.
    // treat it as unlimited if the value is greater than a quart of the whole memory can be presented.
    return value >> (8 * sizeof (size_t) - 2);
}

#endif /* JNC_COMMONS_H */
