#ifndef JNC_JNC_TYPE_TRAITS_H
#define JNC_JNC_TYPE_TRAITS_H

// Remove this file if supports only darwin 10.7+

namespace jnc_type_traits {

    template<class _Tp>
    struct remove_const {
        using type = _Tp;
    };

    template<class _Tp>
    struct remove_const<const _Tp> {
        using type = _Tp;
    };

    template<class _Tp>
    struct remove_volatile {
        using type = _Tp;
    };

    template<class _Tp>
    struct remove_volatile<volatile _Tp> {
        using type = _Tp;
    };

    template<class _Tp>
    struct remove_cv {
        using type = typename remove_volatile<typename remove_const<_Tp>::type>::type;
    };

    template<class _Tp, _Tp _v>
    struct integral_constant {
        using type = integral_constant; // using injected-class-name
        using value_type = _Tp;
        // work with enum, avoid compiler generate actual static value for this struct
        enum : _Tp {
            value = _v
        };
    };

    // feature of c++14
    template<bool _v> using bool_constant = integral_constant<bool, _v>;

    using true_type = bool_constant<true>;
    using false_type = bool_constant<false>;

    namespace impl {

        template<class>
        struct is_pointer_impl : false_type {
        };

        template<class _Tp>
        struct is_pointer_impl<_Tp *> : true_type {
        };
    }

    template<class _Tp>
    struct is_pointer : impl::is_pointer_impl<typename remove_cv<_Tp>::type>::type {
    };

    namespace impl {

        template<class>
        struct is_integral_impl : false_type {
        };

#define DEFINE_INTEGRAL(T) template<> struct is_integral_impl<T> : true_type {}
        DEFINE_INTEGRAL(bool);
        DEFINE_INTEGRAL(char);
        DEFINE_INTEGRAL(signed char);
        DEFINE_INTEGRAL(unsigned char);
#if __cplusplus > 201703L
        DEFINE_INTEGRAL(char8_t);
#endif
        DEFINE_INTEGRAL(char16_t);
        DEFINE_INTEGRAL(char32_t);
        DEFINE_INTEGRAL(wchar_t);
        DEFINE_INTEGRAL(short);
        DEFINE_INTEGRAL(unsigned short);
        DEFINE_INTEGRAL(int);
        DEFINE_INTEGRAL(unsigned);
        DEFINE_INTEGRAL(long);
        DEFINE_INTEGRAL(unsigned long);
        DEFINE_INTEGRAL(long long);
        DEFINE_INTEGRAL(unsigned long long);
#undef DEFINE_INTEGRAL

    }

    template<class _Tp>
    struct is_integral : impl::is_integral_impl<typename remove_cv<_Tp>::type>::type {
    };

}

#endif //JNC_JNC_TYPE_TRAITS_H
