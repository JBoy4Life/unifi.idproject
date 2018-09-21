package id.unifi.service.common.api.annotations;

import id.unifi.service.common.api.access.Access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiOperation {
    String name() default "";
    String resultType() default "";
    Access access() default Access.PERMISSIONED;
    String description() default "";
}
