//package batch.scheduler.config
//
//import io.micronaut.context.annotation.Requires
//
//import javax.sql.DataSource;
//import java.lang.annotation.*;
//
//
//@MustBeDocumented
//@Retention(AnnotationRetention.RUNTIME)
//@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
//
//
//@Target([ElementType.PACKAGE, ElementType.TYPE])
//@Requires(property = "datasources.default.url")
////@Requires(notEnv = arrayOf(Environment.TEST))
//@interface RequiresJdbc