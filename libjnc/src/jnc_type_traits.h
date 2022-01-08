#ifndef JNC_JNC_TYPE_TRAITS_H
#define JNC_JNC_TYPE_TRAITS_H

// Remove this file if supports only darwin 10.7+

namespace jnc_type_traits {

    template<class Tp>
    struct remove_const {
        using type = Tp;
    };

    template<class Tp>
    struct remove_const<const Tp> {
        using type = Tp;
    };

    template<class Tp>
    struct remove_volatile {
        using type = Tp;
    };

    template<class Tp>
    struct remove_volatile<volatile Tp> {
        using type = Tp;
    };

    template<class Tp>
    struct remove_cv {
        using type = typename remove_volatile<typename remove_const<Tp>::type>::type;
    };

    template<class Tp, Tp val>
    struct integral_constant {
        using type = integral_constant; // using injected-class-name
        using value_type = Tp;
        // work with enum, avoid compiler generate actual static value for this struct
        enum : Tp {
            value = val
        };
    };

    // feature of c++14
    template<bool val> using bool_constant = integral_constant<bool, val>;

    using true_type = bool_constant<true>;
    using false_type = bool_constant<false>;

    namespace impl {

        template<class>
        struct is_pointer_impl : false_type {
        };

        template<class Tp>
        struct is_pointer_impl<Tp *> : true_type {
        };
    }

    template<bool, class = void>
    struct enable_if {
    };

    template<class Tp>
    struct enable_if<true, Tp> {
        typedef Tp type;
    };

    template<class Tp>
    struct is_pointer : impl::is_pointer_impl<typename remove_cv<Tp>::type>::type {
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

    template<class Tp>
    struct is_integral : impl::is_integral_impl<typename remove_cv<Tp>::type>::type {
    };

}

#endif //JNC_JNC_TYPE_TRAITS_H
