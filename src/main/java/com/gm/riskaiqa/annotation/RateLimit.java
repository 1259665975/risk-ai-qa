<<<<<<< HEAD
package com.gm.riskaiqa.annotation;

import java.lang.annotation.*;

/**
 * Marks a method to be protected by a Redis fixed-window rate limiter.
 * When unset, the values fall back to {@code risk-ai.rate-limit.*} configuration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** Logical name used in the Redis key; defaults to the method signature. */
    String key() default "";

    /** Max requests per window; -1 means use the configured default. */
    int maxRequests() default -1;

    /** Window length in seconds; -1 means use the configured default. */
    int windowSeconds() default -1;
}
=======
package com.gm.riskaiqa.annotation;

import java.lang.annotation.*;

/**
 * Marks a method to be protected by a Redis fixed-window rate limiter.
 * When unset, the values fall back to {@code risk-ai.rate-limit.*} configuration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** Logical name used in the Redis key; defaults to the method signature. */
    String key() default "";

    /** Max requests per window; -1 means use the configured default. */
    int maxRequests() default -1;

    /** Window length in seconds; -1 means use the configured default. */
    int windowSeconds() default -1;
}
>>>>>>> 7998cf43f5debde367904ed821ec8539275331cc
