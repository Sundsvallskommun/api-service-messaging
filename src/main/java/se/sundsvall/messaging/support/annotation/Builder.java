package se.sundsvall.messaging.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder.Template(options = @RecordBuilder.Options(
    setterPrefix = "with",
    builderMethodName = "newBuilder",
    copyMethodName = "copy"
))
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Inherited
public @interface Builder {

}
