package com.plumelog.trace.annotation;

import java.lang.annotation.*;

/**
 * className：Trace
 * description：
 * time：2020-05-09.14:15
 *
 * @author Tank
 * @version 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Trace {


}
